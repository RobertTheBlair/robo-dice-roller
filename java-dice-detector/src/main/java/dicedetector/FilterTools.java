package dicedetector;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class FilterTools {

    public static final double[][] blurFilter = {
            {0.0625, 0.125, 0.0625},
            {0.125, 0.25, 0.125},
            {0.0625, 0.125, 0.0625}
    };
    public static final double[][] blur5x5Filter = {
            {1/256., 4/256., 6/256., 4/256., 1/256.},
            {4/256., 16/256., 24/256., 16/256., 4/256.},
            {6/256., 24/256., 36/256., 24/256., 6/256.},
            {4/256., 16/256., 24/256., 16/256., 4/256.},
            {1/256., 4/256., 6/256., 4/256., 1/256.},
    };

    public static final double[][] edgeFilter = {
            {0, -1, 0},
            {-1, 4, -1},
            {0, -1, 0}
    };

    public interface ImageAction {
        BufferedImage apply(BufferedImage sourceImaage);
    }

    public BufferedImage runMatrixFilter(BufferedImage inputImage, double[][] filter) {

        WritableRaster referenceRaster = inputImage.getRaster();
        int width = referenceRaster.getWidth();
        int height = referenceRaster.getHeight();
        BufferedImage newBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster blurredRaster = newBufferedImage.getRaster();
        int[] tempPixel = new int[4];
        int[] outPixel = new int[4];
        /* for each pixel, run a matrix to get a new output pixel based on neighbors */
        for (int y=0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                applyFilterAtPixel(referenceRaster, x, y, filter, outPixel, tempPixel);
                blurredRaster.setPixel(x, y, outPixel);
            }
        }
        return newBufferedImage;
    }
    void applyFilterAtPixel(Raster referenceRaster, int x, int y, double[][] filter, int[] outPixel, int[] tempPixel) {
        int width = referenceRaster.getWidth();
        int height = referenceRaster.getHeight();
        // assume filter h/w is same, and odd
        int size = filter.length;
        int mid = size/2; // round down - so 3 goes to 1

        double r = 0;
        double g = 0;
        double b = 0;
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                int pixRow = y+i-mid;
                int pixCol = x+j-mid;
                // if out of bounds, use the mirror pixel as a source
                if (pixCol<0) {
                    pixCol = -pixCol;
                } else if (pixCol >= width) {
                    pixCol = width - ((pixCol - width) + 1);
                }
                if (pixRow<0) {
                    pixRow = -pixRow;
                } else if (pixRow >= height) {
                    pixRow = height - ((pixRow - height) + 1);
                }
                referenceRaster.getPixel(pixCol, pixRow, tempPixel);
                r += (tempPixel[0]*filter[i][j]);
                g += (tempPixel[1]*filter[i][j]);
                b += (tempPixel[2]*filter[i][j]);
            }
        }
        outPixel[0] = clampValue(r);
        outPixel[1] = clampValue(g);
        outPixel[2] = clampValue(b);
        outPixel[3] = 255;
    }

    int clampValue(double input) {
        if (input<0) {
            return 0;
        } else if (input > 255) {
            return 255;
        }
        return (int)input;
    }


    BufferedImage thresholdImage(BufferedImage inputImage, int threshold) {
        int rgbThreshold = 3 * threshold;
        WritableRaster writableRaster = inputImage.getRaster();
        int[] pixel = new int[4];
        int[] outPixel = new int[4];
        int width = writableRaster.getWidth();
        int height = writableRaster.getHeight();
        for (int y=0;y<height; y++) {
            for (int x = 0; x< width; x++) {
                writableRaster.getPixel(x, y, pixel);
                int pixelSum = pixel[0] + pixel[1] + pixel[2];
                if (pixelSum > rgbThreshold) {
                    outPixel[0] = 255;
                    outPixel[1] = 255;
                    outPixel[2] = 255;
                } else {
                    outPixel[0] = 0;
                    outPixel[1] = 0;
                    outPixel[2] = 0;
                }
                outPixel[3] = 255;
                writableRaster.setPixel(x, y, outPixel);
            }
        }
        return inputImage;
    }

    BufferedImage resizeImageIfBig(BufferedImage inputImage, int targetMaxWidth, int targetMaxHeight) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        if (width > targetMaxWidth || height > targetMaxHeight) {
            double widthScale = (double)targetMaxWidth / width;
            double heightScale = (double)targetMaxHeight / height;
            double scale = Math.min(widthScale, heightScale);
            int newWidth = (int)(width * scale);
            int newHeight = (int)(height * scale);

            Image originalImage = inputImage.getScaledInstance(newWidth, (int)(height * scale), Image.SCALE_SMOOTH);

            int type = ((inputImage.getType() == 0) ? BufferedImage.TYPE_INT_ARGB : inputImage.getType());
            // create a buffer of the desired processing size
            BufferedImage resizedImage = new BufferedImage(targetMaxWidth, targetMaxHeight, type);

            // create a graphics context to write resized image into
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.setColor(Color.GRAY);
            g2d.fillRect(0, 0, targetMaxWidth, targetMaxHeight);

            // center the resized image inside the target area, and draw it
            int offsetX = (targetMaxWidth - newWidth)/2;
            int offsetY = (targetMaxHeight - newHeight)/2;
            g2d.drawImage(originalImage, offsetX, offsetY, null);
            g2d.dispose();

            return resizedImage;
        }
        return inputImage;
    }
}
