package dicedetector;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ProcessedImage {

    String fileName;
    Map<String, Object> metaData = new TreeMap<>();
    List<String> processHistory = new ArrayList<>();
    BufferedImage originalImage;
    BufferedImage alteredImage;

    ArrayList<DieObject> dieObjects;

    static final int[] colorRed =   { 255,   0,   0, 255};

    static final int[] colorGreen = {   0, 255,   0, 255};

    static final int[] colorBlue =  {   0,   0, 255, 255};

    public void colorDieobject(int index, int[] colorPixel) {
        DieObject die = dieObjects.get(index);
        if(die== null) {
            System.out.println("Cannot alter null die");
            return;
        }
        if(alteredImage == null) {
            System.out.println("no altered image");
            return;
        }
        WritableRaster writableImage = alteredImage.getRaster();
        die.edge_pixels.forEach((k,v) ->  {
            for(int i : v) {
                // System.out.printf("(%d,%d)\n", i,k);
                writableImage.setPixel(i, k, colorPixel);
            }
        });
    }

    public boolean dieObjectscontainsPixel(int x, int y) {
        if(dieObjects == null) {
            return false;
        }
        ArrayList<Integer> pixels;
        for(DieObject die : dieObjects) {
            pixels = die.edge_pixels.get(y);
            if(pixels != null) {
                for(int xVal : pixels) {
                    if(xVal == x) return true;
                }
            }
        }
        return false;
    }
}


