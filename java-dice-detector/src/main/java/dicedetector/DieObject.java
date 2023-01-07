package dicedetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.awt.Point;
 

public class DieObject {
    /*
     * A Hashmap of edgepixels where the key is the y component, and the x component(s) is the value.
     */
    public HashMap<Integer,ArrayList<Integer>> edge_pixels;
    /*
     * A 3D array of pip_objects within the die's edges. A pip is defined as a
     * circular object representing the value of a die
     * example:
     * {
     * { {1,2}, {2,2} {3,2}, {3,3}, {4,3}, {4,2}, {4,1}, {3,1}, {2,1} {1,1} },
     * { {5,2}, {6,2} {7,2}, {7,3}, {8,3}, {8,2}, {8,1}, {7,1}, {6,1} {5,1} },
     * ...
     * }
     */
    private int[][][] pips;

    public DieObject(HashMap<Integer,ArrayList<Integer>> edge) {
        this.edge_pixels = edge;
        this.pips = null;
    }

    public DieObject(HashMap<Integer,ArrayList<Integer>> edge, int[][][] pips) {
        this.edge_pixels = edge;
        this.pips = pips;
    }

    public int getEdgeSize() {
        return edge_pixels.size(); //returns the "height" of the object
    }

    public HashMap<Integer,ArrayList<Integer>> getEdge() {
        return edge_pixels;
    }

    static DieObject createDieObject(int[] imageData, int height, int width, int x, int y, int bands) {

        /* find all connected pixels :) */
        Set<Point> vistedPixels = new HashSet<Point>();
        LinkedList<Point> pixelsToVisit = new LinkedList<Point>(); //Queue BFS
        Point startingPoint = new Point(x,y);
        HashMap<Integer,ArrayList<Integer>> edgePixels = new HashMap<Integer,ArrayList<Integer>>();
        ArrayList<Integer> firstElement = new ArrayList<Integer>();
        firstElement.add(x);
        edgePixels.put(y, firstElement);
        pixelsToVisit.add(startingPoint);
        vistedPixels.add(startingPoint);
        Point refPoint; 
        
        while (!pixelsToVisit.isEmpty()) {
            refPoint = pixelsToVisit.poll();
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    int curX = (int)refPoint.getX() + j;
                    int curY = (int)refPoint.getY() + i;
                    Point nextPoint = new Point( curX, curY);
                    if (pixelIsValid(width,height,nextPoint) && !vistedPixels.contains(nextPoint)) {
                        vistedPixels.add(nextPoint);
                        int pixelRef = curY * bands * width + curX * bands;
                        int sumVal = imageData[pixelRef] + imageData[pixelRef + 1] + imageData[pixelRef + 2];
                        if (sumVal > 0) {
                            pixelsToVisit.add(nextPoint);

                            ArrayList<Integer> xVals = edgePixels.get(curY);
                            if(xVals ==  null) {
                                xVals = new ArrayList<Integer>();
                                edgePixels.put(curY, xVals);
                            }
                            xVals.add(curX);                     
                        } 
                    }
                }
            }
        }
        return new DieObject(edgePixels);
    }
    private static boolean pixelIsValid(int width,int height,Point p) {
        return p.getX() >= 0 && p.getX() < width && p.getY() >= 0 && p.getY() < height;
    }
}


