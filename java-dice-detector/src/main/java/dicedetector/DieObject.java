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
    public int minX;
    public int maxX;
    public int minY;
    public int maxY;

    public DieObject(HashMap<Integer,ArrayList<Integer>> edge, int minX, int maxX, int minY, int maxY) {
        this.edge_pixels = edge;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public int getEdgeSize() {
        return edge_pixels.size(); //returns the "height" of the object
    }

    public int getEdgeWidth() {
        return 1 + maxX - minX;
    }

    public HashMap<Integer,ArrayList<Integer>> getEdges() {
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
        int minX, maxX, minY, maxY;
        minX = Integer.MAX_VALUE;
        minY = Integer.MAX_VALUE;
        maxX = 0;
        maxY = 0;

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
                            if(xVals == null) {
                                xVals = new ArrayList<Integer>();
                                edgePixels.put(curY, xVals);
                            }
                            xVals.add(curX);
                            if (maxX < curX) {
                                maxX = curX;
                            }
                            if (minX > curX) {
                                minX = curX;
                            }
                            if (maxY < curY) {
                                maxY = curY;
                            }
                            if (minY > curY) {
                                minY = curY;
                            }
                        }
                    }
                }
            }
        }
        if(minX == Integer.MAX_VALUE) {
            minX = maxX;
        }
        if(minY == Integer.MAX_VALUE) {
            minY = maxY;
        }
        return new DieObject(edgePixels, minX, maxX, minY, maxY);
    }
    private static boolean pixelIsValid(int width,int height,Point p) {
        return p.getX() >= 0 && p.getX() < width && p.getY() >= 0 && p.getY() < height;
    }
}


