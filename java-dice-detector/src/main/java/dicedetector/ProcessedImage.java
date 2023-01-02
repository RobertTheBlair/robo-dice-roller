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

}
