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

    DieObject die;

    public void turnEdgeRed() {
        if(die == null) {
            System.out.println("Cannot alter null die");
            return;
        }
        if(alteredImage == null) {
            System.out.println("no altered image");
            return;
        }
        System.out.println("turning pixels red");
        WritableRaster writableImage = alteredImage.getRaster();
        int[] redPixels = { 255, 0, 0, 255};
        die.edge_pixels.forEach((k,v) ->  {
            for(int i : v) {
                // System.out.printf("(%d,%d)\n", i,k);
                writableImage.setPixel(i, k, redPixels);
            }
        });
    }
}


