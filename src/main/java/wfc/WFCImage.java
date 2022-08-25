package wfc;

import javafx.scene.paint.Color;

public class WFCImage {
    int[] pixelData;
    int arrWidth;
    int width;
    int height;

    public WFCImage(int width, int height, int maxWidth, int maxHeight) {
        this.pixelData = new int[maxHeight * maxWidth * 4];
        this.arrWidth = maxWidth;
        this.width = width;
        this.height = height;
        for (int i = 0; i < pixelData.length; i += 4) {
            pixelData[i] = 255;
            pixelData[i + 1] = 255;
            pixelData[i + 2] = 255;
            pixelData[i + 3] = 255;
        }
    }

    public void setPixel(int x, int y, Color color) {
        int initialIndex = (y * this.arrWidth + x) * 4;
        pixelData[initialIndex] = (int) Math.round(color.getRed() * 255.0d);
        pixelData[initialIndex + 1] = (int) Math.round(color.getGreen() * 255.0d);
        pixelData[initialIndex + 2] = (int) Math.round(color.getBlue() * 255.0d);
        pixelData[initialIndex + 3] = (int) Math.round(color.getOpacity() * 255.0d);
    }

    public int[] getPixelData() {
        return pixelData;
    }

    public void setWidth(int width) {
        if (width > this.width) {
            for (int y = 0; y < this.height; y++) {
                for (int x = this.width; x < width; x++) {
                    int pixelIndex = (y * this.arrWidth + x) * 4;
                    pixelData[pixelIndex] = 255;
                    pixelData[pixelIndex + 1] = 255;
                    pixelData[pixelIndex + 2] = 255;
                    pixelData[pixelIndex + 3] = 255;
                }
            }
        }
        this.width = width;
    }

    public void setHeight(int height) {
        if (height > this.height) {
            for (int y = this.height; y < height; y++) {
                for (int x = 0; x < this.width; x++) {
                    int pixelIndex = (y * this.arrWidth + x) * 4;
                    pixelData[pixelIndex] = 255;
                    pixelData[pixelIndex + 1] = 255;
                    pixelData[pixelIndex + 2] = 255;
                    pixelData[pixelIndex + 3] = 255;
                }
            }
        }
        this.height = height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getArrWidth() {
        return this.arrWidth;
    }
}
