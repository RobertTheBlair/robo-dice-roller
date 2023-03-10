package dicedetector;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;

public class FilterTools {

    public static final double[][] blurFilter = {
            {0.0625, 0.125, 0.0625},
            {0.125, 0.25, 0.125},
            {0.0625, 0.125, 0.0625}
    };
    public static final double[][] strongBlurFilter = { //sigma of 1.5
        {0.0150, 0.0286, 0.0354, 0.0286, 0.0150},
        {0.0286, 0.0543, 0.0673, 0.0543, 0.0286},
        {0.0354, 0.0673, 0.0834, 0.0673, 0.0354},
        {0.0286, 0.0543, 0.0673, 0.0543, 0.0286},
        {0.0150, 0.0286, 0.0354, 0.0286, 0.0150,},
};

    public static final double[][] strongBlurFilter2 = { //sigma of 1.5, support 05
            {0.0150, 0.0286, 0.0354, 0.0286, 0.0150},
            {0.0286, 0.0543, 0.0673, 0.0543, 0.0286},
            {0.0354, 0.0673, 0.0834, 0.0673, 0.0354},
            {0.0286, 0.0543, 0.0673, 0.0543, 0.0286},
            {0.0150, 0.0286, 0.0354, 0.0286, 0.0150},
    };
    public static final double[][] blur5x5Filter = {
            {1/256., 4/256., 6/256., 4/256., 1/256.},
            {4/256., 16/256., 24/256., 16/256., 4/256.},
            {6/256., 24/256., 36/256., 24/256., 6/256.},
            {4/256., 16/256., 24/256., 16/256., 4/256.},
            {1/256., 4/256., 6/256., 4/256., 1/256.},
    };

    public static final double[][] unsharpMaskFilter = {
            {-1/256., -4/256., -6/256., -4/256., -1/256.},
            {-4/256., -16/256., -24/256., -16/256., -4/256.},
            {-6/256., -24/256., 476/256., -24/256., -6/256.},
            {-4/256., -16/256., -24/256., -16/256., -4/256.},
            {-1/256., -4/256., -6/256., -4/256., -1/256.},
    };

    public static final double[][] edgeFilter = {
            {0, -1, 0},
            {-1, 4, -1},
            {0, -1, 0}
    };

    public static final double[][] sharpenFilter = {
            {0, -1, 0},
            {-1, 5, -1},
            {0, -1, 0}
    };

    public interface ImageAction {
        ProcessedImage apply(ProcessedImage sourceImaage);
    }

    public ProcessedImage runMatrixFilter(ProcessedImage inputImage, double[][] filter) {

        WritableRaster referenceRaster = inputImage.alteredImage.getRaster();
        int width = referenceRaster.getWidth();
        int height = referenceRaster.getHeight();
        int[] imageData = referenceRaster.getPixels(0, 0, width, height, new int[ width * height * referenceRaster.getNumBands()]);

        BufferedImage newBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster blurredRaster = newBufferedImage.getRaster();
        int[] outPixel = new int[4];
        /* for each pixel, run a matrix to get a new output pixel based on neighbors */
        for (int y=0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                applyFilterAtPixel(referenceRaster, x, y, filter, outPixel, imageData, referenceRaster.getNumBands());
                blurredRaster.setPixel(x, y, outPixel);
            }
        }
        inputImage.alteredImage = newBufferedImage;
        return inputImage;
    }

    void applyFilterAtPixel(Raster referenceRaster, int x, int y, double[][] filter, int[] outPixel, int[] imageData, int bands) {
        int width = referenceRaster.getWidth();
        int height = referenceRaster.getHeight();
        // assume filter h/w is same, and odd
        int size = filter.length;
        int mid = size/2; // round down - so 3 goes to 1

        double r = 0;
        double g = 0;
        double b = 0;
        int rowStride = bands * width;
        for(int i = 0; i < size; i++) {
            int pixRow = y+i-mid;
            if (pixRow<0) {
                pixRow = -pixRow;
            } else if (pixRow >= height) {
                pixRow = height - ((pixRow - height) + 1);
            }
            int rowOffset = pixRow * rowStride;
            for(int j = 0; j < size; j++) {
                int pixCol = x+j-mid;
                // if out of bounds, use the mirror pixel as a source
                if (pixCol<0) {
                    pixCol = -pixCol;
                } else if (pixCol >= width) {
                    pixCol = width - ((pixCol - width) + 1);
                }
                double weight = filter[i][j];
                int offset = rowOffset + pixCol * bands;
                r += imageData[offset  ] * weight;
                g += imageData[offset+1] * weight;
                b += imageData[offset+2] * weight;
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


    public ProcessedImage runMedianBlur(ProcessedImage inputImage, int oddSize) {

        WritableRaster referenceRaster = inputImage.alteredImage.getRaster();
        int width = referenceRaster.getWidth();
        int height = referenceRaster.getHeight();
        int[] imageData = referenceRaster.getPixels(0, 0, width, height, new int[ width * height * referenceRaster.getNumBands()]);

        BufferedImage newBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster blurredRaster = newBufferedImage.getRaster();
        int[] outPixel = new int[4];
        /* for each pixel, run a matrix to get a new output pixel based on neighbors */
        for (int y=0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                medianBlur(referenceRaster, x, y, oddSize, outPixel, imageData, referenceRaster.getNumBands());
                blurredRaster.setPixel(x, y, outPixel);
            }
        }
        inputImage.alteredImage = newBufferedImage;
        return inputImage;
    }

    void medianBlur(Raster referenceRaster, int x, int y, int size, int[] outPixel, int[] imageData, int bands) {
        int width = referenceRaster.getWidth();
        int height = referenceRaster.getHeight();
        // assume filter h/w is same, and odd
        int mid = size/2; // round down - so 3 goes to 1

        int startY = Math.max(y - mid, 0);
        int endY = Math.min(y + mid, height-1);
        int startX = Math.max(x - mid, 0);
        int endX = Math.min(x + mid, width-1);

        int matrixSize = (1 + endY - startY) * (1 + endX - startX);

        int[] rpixels = new int[matrixSize];
        int[] gpixels = new int[matrixSize];
        int[] bpixels = new int[matrixSize];

        int rowStride = bands * width;
        int index = 0;
        for(int i = startY; i <= endY; i++) {
            int rowOffset = i * rowStride;
            for(int j = startX; j <= endX; j++) {
                int offset = rowOffset + j * bands;
                rpixels[index] = imageData[offset  ];
                gpixels[index] = imageData[offset+1];
                bpixels[index] = imageData[offset+2];
                index++;
            }
        }

        Arrays.sort(rpixels);
        Arrays.sort(gpixels);
        Arrays.sort(bpixels);

        outPixel[0] = rpixels[matrixSize/2];
        outPixel[1] = gpixels[matrixSize/2];
        outPixel[2] = bpixels[matrixSize/2];
        outPixel[3] = 255;
    }


    ProcessedImage scanAndThreshold(ProcessedImage inputImage) {
        WritableRaster referenceRaster = inputImage.alteredImage.getRaster();
        int width = referenceRaster.getWidth();
        int height = referenceRaster.getHeight();
        int[] imageData = referenceRaster.getPixels(0, 0, width, height, new int[width * height * referenceRaster.getNumBands()]);

        int[] pixelSumCount = new int[256];
        for(int i=0;i<imageData.length;i+=4) {
            int r, g, b;
            r = imageData[i+0];
            g = imageData[i+1];
            b = imageData[i+2];
            // we ignore the grey pixel, as our bg item
            if (r!=64 || g != 64 || b != 64) {
                int pixelSum = (r + g + b) / 3;
                pixelSumCount[pixelSum]++;
            }
        }
        int maxPixelSum = 0;
        int minPixelSum = Integer.MAX_VALUE;
        int firstNonZero = 0;
        int lastNonZero = pixelSumCount.length-1;
        for (int i= 0; i< pixelSumCount.length;i++) {
            int sum = pixelSumCount[i];
            maxPixelSum = Math.max(sum, maxPixelSum);
            minPixelSum = Math.min(sum, minPixelSum);
            if(sum != 0) {
                if (firstNonZero==0) {
                    firstNonZero = i;
                }
                lastNonZero = i;
            }
        }

        System.out.println("First non-zero: " + firstNonZero + " last non-zero " + lastNonZero
                                   + " min pix count " + minPixelSum + " max pix count " + maxPixelSum);

        int [] white = new int[4];
        int [] black = new int[4];
        int [] grey = new int[4];
        white[0] = 255;
        white[1] = 255;
        white[2] = 255;
        white[3] = 255;
        black[3] = 255;
        grey[0] = 64;
        grey[1] = 64;
        grey[2] = 64;
        grey[3] = 255;
        for (int y = 0; y<height;y++) {
            for (int x = 0; x<width; x++) {
                if (y > 255) {
                    referenceRaster.setPixel(x, y, grey);
                } else if(x >= pixelSumCount[y] * width / maxPixelSum ) {
                    referenceRaster.setPixel(x, y, black);
                } else {
                    referenceRaster.setPixel(x, y, white);
                }
            }
        }
        return inputImage;
    }

    ProcessedImage thresholdImage(ProcessedImage inputImage, int threshold) {
        int rgbThreshold = 3 * threshold;
        WritableRaster writableRaster = inputImage.alteredImage.getRaster();
        int[] pixel = new int[4];
        int[] outPixel = new int[4];
        int width = writableRaster.getWidth();
        int height = writableRaster.getHeight();
        for (int y=0;y<height; y++) {
            for (int x = 0; x < width; x++) {
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

    ProcessedImage scanRowColImage(ProcessedImage inputImage) {
        WritableRaster writableRaster = inputImage.alteredImage.getRaster();
        int width = writableRaster.getWidth();
        int height = writableRaster.getHeight();
        int[] rowBrightnessSum = new int[height];
        int[] colBrightnessSum = new int[width];

        int[] pixel = new int[4];
        int[] outPixel = new int[4];
        for (int y=0;y<height; y++) {
            for (int x = 0; x< width; x++) {
                writableRaster.getPixel(x, y, pixel);
                int pixelSum = (pixel[0] + pixel[1] + pixel[2])/3;
                rowBrightnessSum[y] += pixelSum;
                colBrightnessSum[x] += pixelSum;
            }
        }

        int maxColBright = 0;
        int minColBright = Integer.MAX_VALUE;
        for (int x = 0; x<width; x++) {
            maxColBright = Math.max(colBrightnessSum[x], maxColBright);
            minColBright = Math.max(colBrightnessSum[x], minColBright);
        }

        int maxRowBright = 0;
        int minRowBright = Integer.MAX_VALUE;
        for (int y = 0; y<height; y++) {
            maxRowBright = Math.max(rowBrightnessSum[y], maxRowBright);
            minRowBright = Math.max(rowBrightnessSum[y], minRowBright);
        }
        for (int x = 0; x<width; x++) {
            colBrightnessSum[x] = scaleValue(colBrightnessSum[x], minColBright, maxColBright, 255);
        }
        for (int y = 0; y<height; y++) {
            rowBrightnessSum[y] = scaleValue(rowBrightnessSum[y], minRowBright, maxRowBright, 255);
        }

        for (int y=0;y<height; y++) {
            for (int x = 0; x< width; x++) {
                outPixel[0] = 0;
                outPixel[1] = rowBrightnessSum[y];
                outPixel[2] = colBrightnessSum[x];
                outPixel[3] = 255;
                writableRaster.setPixel(x, y, outPixel);
            }
        }

        return inputImage;
    }

    int scaleValue(int value, int minValue, int maxValue, int range) {
        if (minValue == maxValue) {
            return range;
        }
        return (int)( (value-minValue) * (long)range) / (maxValue - minValue);
    }

    ProcessedImage resizeImageIfBig(ProcessedImage image, int targetMaxWidth, int targetMaxHeight) {
        BufferedImage inputImage = image.alteredImage;
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        if (width > targetMaxWidth || height > targetMaxHeight) {
            double widthScale = (double)targetMaxWidth / width;
            double heightScale = (double)targetMaxHeight / height;
            double scale = Math.min(widthScale, heightScale);
            int newWidth = (int)(width * scale);
            int newHeight = (int)(height * scale);

            Image originalImage = inputImage.getScaledInstance(newWidth, (int)(height * scale), Image.SCALE_SMOOTH);

            // always set type as argb, so data byte array stuff is consistent.
            // create a buffer of the desired processing size
            BufferedImage resizedImage = new BufferedImage(targetMaxWidth, targetMaxHeight, BufferedImage.TYPE_INT_ARGB);

            // create a graphics context to write resized image into
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, targetMaxWidth, targetMaxHeight);

            // center the resized image inside the target area, and draw it
            int offsetX = (targetMaxWidth - newWidth)/2;
            int offsetY = (targetMaxHeight - newHeight)/2;
            g2d.drawImage(originalImage, offsetX, offsetY, null);
            g2d.dispose();
            image.alteredImage = resizedImage;
        }
        return image;
    }

    ProcessedImage findDieInImage(ProcessedImage img) {
//        final int sizeThreshold = 80, pipSizeThrehold = 25;
        WritableRaster writableRaster = img.alteredImage.getRaster();
        int height = writableRaster.getHeight();
        int width = writableRaster.getWidth();
        int[] imageData = writableRaster.getPixels(0, 0, width, height, new int[ width * height * writableRaster.getNumBands()]);
        int sumVal = 0;
        int bands = writableRaster.getNumBands();
        int pixelRef = 0;
        int largestBlobSize = 0;
        img.dieObjects = new ArrayList<>();

        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                pixelRef = y * bands * width + x * bands;
                sumVal = imageData[pixelRef] + imageData[pixelRef+1] + imageData[pixelRef+2];
                if(sumVal > 0 && !img.dieObjectscontainsPixel(x, y)) {
                    //we found the beginning of the die edge,
                    DieObject object = DieObject.createDieObject(imageData, height, width, x, y, bands);
                    img.dieObjects.add(object);
                    largestBlobSize = Math.max(object.getEdgeSize(), largestBlobSize);
                }
            }
        }
        int pipCount = 0;
        int otherPipCount = 0;
        int smallObjectCount = 0;
        int mediumObjectCount = 0;
        int largeObjectCount = 0;
        int pipSizeThrehold = (int)Math.max(largestBlobSize * 0.10, 10.);
        int largeObjectThreshold = (int)(largestBlobSize * 0.707); // worse case diff between rotated dice is a factory of 1/sqrt(2)
        for ( DieObject dieObject : img.dieObjects) {
            int objSize = dieObject.getEdgeSize();
//            System.out.printf("edge with dimensions h=%d w=%d \n", objSize, dieObject.getEdgeWidth());
            if ( Math.abs(objSize - dieObject.getEdgeWidth()) <= 5 && objSize > pipSizeThrehold) {
                System.out.printf("squarish / circle shape found h=%d w=%d \n", objSize, dieObject.getEdgeWidth());
                otherPipCount++;
            }

            if (objSize >= largeObjectThreshold) {
                System.out.printf("Die Edge of size %d detected\n", objSize);
                img.colorDieObject(dieObject, ProcessedImage.colorBlue);
                largeObjectCount++;
            } else if (objSize > pipSizeThrehold) {
                System.out.printf("pip object of size %d detected\n", objSize);
                pipCount++;
                mediumObjectCount++;
                img.colorDieObject(dieObject, ProcessedImage.colorGreen);
            } else {
//                System.out.printf("trash object of size %d detected\n", objSize);
                smallObjectCount++;
                img.colorDieObject(dieObject, ProcessedImage.colorRed);
            }
        }
        System.out.printf("Apparent pip count=%d\n", pipCount);
        System.out.printf("Other pip count=%d\n", otherPipCount - 1);
        System.out.printf("Small count=%d, medium count=%d, large count=%d\n", smallObjectCount, mediumObjectCount, largeObjectCount);
        return img;
    }
}
