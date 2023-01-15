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
        int height = 1 + maxY - minY;
        if (height != edge_pixels.size()) {
            System.out.printf("unmatched height info: %d vs %d\n", height, edge_pixels.size());
        }

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
        Set<Point> vistedPixels = new HashSet<>();
        LinkedList<Point> pixelsToVisit = new LinkedList<>(); //Queue BFS
        Point startingPoint = new Point(x,y);
        HashMap<Integer,ArrayList<Integer>> edgePixels = new HashMap<>();
        ArrayList<Integer> firstElement = new ArrayList<>();
        firstElement.add(x);
        edgePixels.put(y, firstElement);
        pixelsToVisit.add(startingPoint);
        vistedPixels.add(startingPoint);
        Point refPoint;
        // we know the incoming point is a valid number for min/max
        int minX = x, maxX = x, minY = y, maxY = y;

        while (!pixelsToVisit.isEmpty()) {
            refPoint = pixelsToVisit.poll();

            for (int i = -1; i < 2; i++) {
                int curY = (int)refPoint.getY() + i;
                if (curY < 0 || curY >= height) {
                    continue;
                }
                for (int j = -1; j < 2; j++) {
                    if ( i==0 && j == 0) {
                        continue;
                    }
                    int curX = (int)refPoint.getX() + j;
                    if (curX < 0 || curX >= width) {
                        continue;
                    }

                    Point nextPoint = new Point(curX, curY);
                    if (!vistedPixels.contains(nextPoint)) {
                        vistedPixels.add(nextPoint);
                        int pixelRef = curY * bands * width + curX * bands;
                        int sumVal = imageData[pixelRef] + imageData[pixelRef + 1] + imageData[pixelRef + 2];
                        if (sumVal > 0) {
                            pixelsToVisit.add(nextPoint);

                            ArrayList<Integer> xVals = edgePixels.get(curY);
                            if(xVals == null) {
                                xVals = new ArrayList<>();
                                edgePixels.put(curY, xVals);
                            }
                            xVals.add(curX);
                            maxX = Math.max(maxX, curX);
                            minX = Math.min(minX, curX);
                            maxY = Math.max(maxY, curY);
                            minY = Math.min(minY, curY);
                        }
                    }
                }
            }
        }
        return new DieObject(edgePixels, minX, maxX, minY, maxY);
    }
    private static boolean pixelIsValid(int width,int height,Point p) {
        return p.getX() >= 0 && p.getX() < width && p.getY() >= 0 && p.getY() < height;
    }
}


