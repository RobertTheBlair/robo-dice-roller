package dicedetector;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ProcessedImage {

    String fileName;
    Map<String, Object> metaData = new TreeMap<>();
    List<String> processHistory = new ArrayList<>();
    BufferedImage originalImage;
    BufferedImage image;

    private class DieObject {
        /*  A 2D array of pixels where edge_pixels[i][0] is the x and edge_pixels[i][1] is the y value of a pixel on the edge of the object;
         *  (theorhetically) the first index in edge_pixels should be the top left corner 
         *  example: { {1,2}, {2,2} {3,2}, {3,3}, {4,3}, {4,2}, {4,1}, {3,1}, {2,1} {1,1} }
         */
        private int[][] edge_pixels; 
        /*  A 3D array of pip_objects within the die's edges. A pip is defined as a circular object representing the value of a die
         * example: 
         * { 
         *      { {1,2}, {2,2} {3,2}, {3,3}, {4,3}, {4,2}, {4,1}, {3,1}, {2,1} {1,1} },
         *      { {5,2}, {6,2} {7,2}, {7,3}, {8,3}, {8,2}, {8,1}, {7,1}, {6,1} {5,1} },
         *      ...
         * }
         */
        private int[][][] pips; 

        public DieObject(int[][] edge) {
            this.edge_pixels = edge;
            this.pips = null;
        }

        public DieObject(int[][] edge, int[][][] pips) {
            this.edge_pixels = edge;
            this.pips = pips;
        }
    }

    DieObject die;
}


