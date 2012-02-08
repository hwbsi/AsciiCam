package com.dozingcatsoftware.asciicam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class OverlayView extends View {

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    AsciiConverter.Result asciiResult;
    
    int charPixelHeight = 9;
    int charPixelWidth = 7;
    int textSize = 10;
    
    int imageWidth;
    int imageHeight;
    
    Paint textPaint = new Paint();
    
    public void setAsciiConverterResult(AsciiConverter.Result value) {
        asciiResult = value;
    }
    
    /**
     * Called to update the size of the camera preview image, which will be scaled to fit the view
     */
    public void setCameraPreviewSize(int width, int height) {
        float previewRatio = ((float)width) / height;
        float viewRatio = ((float)this.getWidth()) / this.getHeight();
        if (previewRatio < viewRatio) {
            // camera preview is narrower than view, scale to full height
            this.imageHeight = this.getHeight();
            this.imageWidth = (int)(this.imageHeight * previewRatio);
        }
        else {
            this.imageWidth = this.getWidth();
            this.imageHeight = (int)(this.imageWidth / previewRatio);
        }
    }
    
    public int asciiColumnsForWidth(int width) {
        return width / charPixelWidth;
    }
    
    public int asciiRowsForHeight(int height) {
        return height / charPixelHeight;
    }
    
    public int asciiRows() {
        return asciiRowsForHeight(this.imageHeight);
    }
    public int asciiColumns() {
        return asciiColumnsForWidth(this.imageWidth);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        int xoffset = (this.getWidth() - this.imageWidth) / 2;
        int yoffset = (this.getHeight() - this.imageHeight) / 2; 
        drawAscii(canvas, this.getWidth(), this.getHeight(), xoffset, yoffset);
    }
    
    public void drawAscii(Canvas canvas, int width, int height, int xoffset, int yoffset) {
        canvas.drawARGB(255, 0, 0, 0);
        textPaint.setARGB(255, 255, 255, 255);

        textPaint.setTextSize(textSize);
        int rows = asciiRows();
        int cols = asciiColumns();
        if (asciiResult!=null && asciiResult.rows==rows && asciiResult.columns==cols) {
            for(int r=0; r<rows; r++) {
                int y = charPixelHeight * (r+1) + yoffset;
                int x = xoffset;
                for(int c=0; c<cols; c++) {
                    String s = asciiResult.stringAtRowColumn(r, c);
                    if (asciiResult.hasColor()) {
                        textPaint.setColor(asciiResult.colorAtRowColumn(r, c));
                    }
                    canvas.drawText(s, x, y, textPaint);
                    x += charPixelWidth;
                }
            }
        }
        //textPaint.setARGB(255,255,0,0);
        //canvas.drawLine(104,0,104,480,textPaint);
        //canvas.drawLine(696,0,696,480,textPaint);
    }
    
    public Bitmap drawIntoNewBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(this.imageWidth, this.imageHeight, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        this.drawAscii(c, this.imageWidth, this.imageHeight, 0, 0);
        return bitmap;
    }
    
    // For thumbnails, create image one-fourth normal size, use every other row and column, and draw solid rectangles
    // instead of text because text won't scale down well for gallery view.
    public Bitmap drawIntoThumbnailBitmap() {
        int width = this.imageWidth / 4;
        int height = this.imageHeight / 4;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setARGB(255, 255, 255, 255);

        canvas.drawARGB(255, 0, 0, 0);
        int rows = asciiRows();
        int cols = asciiColumns();
        if (asciiResult!=null && asciiResult.rows==rows && asciiResult.columns==cols) {
            for(int r=0; r<asciiResult.rows; r+=2) {
                int ymin = (int)(height*r / asciiResult.rows);
                int ymax = (int)(height*(r+2) / asciiResult.rows);
                for(int c=0; c<asciiResult.columns; c+=2) {
                    int xmin = (int)(width*c / asciiResult.columns);
                    int xmax = (int)(width*(c+2) / asciiResult.columns);
                    float ratio = asciiResult.ratioAtRowColumn(r, c);
                    if (ratio > 0) {
                        if (asciiResult.hasColor()) {
                            paint.setColor(asciiResult.colorAtRowColumn(r, c));
                        }
                        if (ratio > 0.5) {
                            canvas.drawRect(xmin, ymin, xmax, ymax, paint);
                        }
                        else {
                            int x = (xmin + xmax) / 2 - 1;
                            int y = (ymin + ymax) / 2 - 1;
                            canvas.drawRect(x, y, x+2, y+2, paint);
                        }
                    }
                }
            }
        }
        return bitmap;
    }
}
