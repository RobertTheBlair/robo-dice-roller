package dicedetector;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    ProcessedImage processedImage;
    ImageIcon imageHolder;
    JLabel image;
    JFrame imageFrame;
    JTextPane logView;
    JPanel infoPane;
    JTextPane infoView;

    List<String> logData = new ArrayList<>();
    List<String> imageData = new ArrayList<>();
    FilterTools filterTools = new FilterTools();

    public Main() {
        imageFrame = new JFrame("Image viewer");

        infoPane = new JPanel();
        infoView = new JTextPane();
        logView = new JTextPane();

        infoView.setText("Information");
        infoView.setPreferredSize(new Dimension(400, 200));
        infoView.setBorder(new TitledBorder("Information"));
        infoView.setEditable(false);
        infoView.setAutoscrolls(true);

        logView.setText("Dice detector loaded");
        logView.setBorder(new TitledBorder("App logs"));
        logView.setEditable(false);
        logView.setPreferredSize(new Dimension(400, 200));
        logView.setAutoscrolls(true);

        infoPane.add(infoView);
        infoPane.add(logView);
        infoPane.setLayout(new GridLayout(0,1));

        image = new JLabel();
        imageHolder = new ImageIcon();
        imageFrame.add(infoPane);
        imageFrame.add(image);
        imageFrame.setJMenuBar(initMenu());

        imageFrame.setSize(640, 480);
        imageFrame.setLayout(new FlowLayout());
        imageFrame.setVisible(true);
    }

    public void logInfo(final String str) {
        logData.add(str);
        final String output = String.join("\n", logData);
        logView.setText(output);
        System.out.println(str);
    }

    private JMenuBar initMenu() {

        JMenu manipulationMenu = new JMenu("modify image");


        JMenuItem bwUpdate = new JMenuItem("convert to b/w");
        bwUpdate.addActionListener(this::thresholdImage);
        manipulationMenu.add(bwUpdate);

        JMenu subBlurMenu = new JMenu("blur options");

        JMenuItem blur = new JMenuItem("blur image");
        blur.addActionListener(this::blurImage);
        subBlurMenu.add(blur);

        JMenuItem blur5 = new JMenuItem("blur 5x5 image");
        blur5.addActionListener(this::blurImage5);
        subBlurMenu.add(blur5);

        JMenuItem strongBlur = new JMenuItem("strong blur");
        strongBlur.addActionListener(this::strongBlur);
        subBlurMenu.add(strongBlur);

        manipulationMenu.add(subBlurMenu);

        JMenuItem edge = new JMenuItem("edge image");
        edge.addActionListener(this::edgeImage);
        manipulationMenu.add(edge);

        JMenu subComboMenu = new JMenu("combo options");

        JMenuItem smallCombo = new JMenuItem("soft_blur/thresh/edge");
        smallCombo.addActionListener(this::smallComboImage);
        subComboMenu.add(smallCombo);

        JMenuItem largeCombo = new JMenuItem("sharp/strong_blur/thresh/edge");
        largeCombo.addActionListener(this::largeComboImage);
        subComboMenu.add(largeCombo);

        manipulationMenu.add(subComboMenu);

        JMenu sharpBlurMenu = new JMenu("sharp options");

        JMenuItem sharp = new JMenuItem("sharp");
        sharp.addActionListener( actionEvent
                 -> runFilterAction(sourceImage -> filterTools.runMatrixFilter(sourceImage, FilterTools.sharpenFilter), "sharp"));
        sharpBlurMenu.add(sharp);

        JMenuItem unsharp = new JMenuItem("unsharp");
        unsharp.addActionListener( actionEvent
                 -> runFilterAction(sourceImage -> filterTools.runMatrixFilter(sourceImage, FilterTools.unsharpMaskFilter), "unsharp"));
        sharpBlurMenu.add(unsharp);

        manipulationMenu.add(sharpBlurMenu);

        JMenuItem menuItem = new JMenuItem("detectThing");
        menuItem.addActionListener( actionEvent
                 -> runFilterAction(sourceImage -> filterTools.scanRowColImage(sourceImage), "Detect thing"));
        manipulationMenu.add(menuItem);

        menuItem = new JMenuItem("analyze");
        menuItem.addActionListener( actionEvent
                 -> runFilterAction(sourceImage -> filterTools.scanAndThreshold(sourceImage), "analyze"));
        manipulationMenu.add(menuItem);

        JMenu imageSubMenu = new JMenu("Load Image");

        JMenu imageOjectMenu = new JMenu("Image Objects");

        JMenuItem findDie = new JMenuItem("find Die");

        findDie.addActionListener(this::findDieImage);

        for (int i=1;i<=6; i++) {
            JMenuItem imageItem = imageSubMenu.add(new JMenuItem("Load Small Ideal " + i));
            imageItem.addActionListener((ActionEvent e) -> displayImage(e, false, true));
            imageItem.setActionCommand(String.valueOf(i));
        }
        for (int i=1;i<=6; i++) {
            JMenuItem imageItem = imageSubMenu.add(new JMenuItem("Load Original Unideal " + i));
            imageItem.setActionCommand(String.valueOf(i));
            imageItem.addActionListener((ActionEvent e) -> displayImage(e,true, false));
        }

        JMenuBar m1 = new JMenuBar();
        m1.add(imageSubMenu);
        m1.add(manipulationMenu);
        return m1;
    }

    void runFilterAction(FilterTools.ImageAction action, String filterName) {
        if (processedImage!=null) {
            long startTime = System.currentTimeMillis();
             action.apply(processedImage);
            long endTime = System.currentTimeMillis();
            imageData.add(filterName + " action: duration MS = " + (endTime - startTime));
            imageHolder.setImage(processedImage.image);
        }
        refreshInfoPanel();
        imageFrame.repaint();
    }
    /* unfinished */
    void findDieImage(final ActionEvent actionEvent) {
        //todo, find die object and store it in the image variable.
    }

    private void thresholdImage(final ActionEvent actionEvent) {
        runFilterAction(sourceImage -> filterTools.thresholdImage(sourceImage, 170), "convert to b/w 170");
    }

    void blurImage(final ActionEvent actionEvent) {
        runFilterAction(sourceImage -> filterTools.runMatrixFilter(sourceImage, FilterTools.blurFilter), "3x3 blur");
    }
    void blurImage5(final ActionEvent actionEvent) {
        runFilterAction(sourceImage -> filterTools.runMatrixFilter(sourceImage, FilterTools.blur5x5Filter), "5x5 blur");
    }
    void strongBlur(final ActionEvent actionEvent) {
        runFilterAction(sourceImage -> filterTools.runMatrixFilter(sourceImage, FilterTools.strongBlurFilter), "Strong blur");
    }

    void edgeImage(final ActionEvent actionEvent) {
        runFilterAction(sourceImage -> filterTools.runMatrixFilter(sourceImage, FilterTools.edgeFilter), "edge");
    }

    void smallComboImage(final ActionEvent actionEvent) {
        runFilterAction(sourceImage -> {
            filterTools.runMatrixFilter(sourceImage, FilterTools.blurFilter);
            filterTools.thresholdImage(sourceImage, 170);
            return filterTools.runMatrixFilter(sourceImage, FilterTools.edgeFilter);
        }, "combo");
    }

    void largeComboImage(final ActionEvent actionEvent) {
        runFilterAction(sourceImage -> {
            filterTools.runMatrixFilter(sourceImage, FilterTools.sharpenFilter);
            filterTools.runMatrixFilter(sourceImage, FilterTools.strongBlurFilter);
            filterTools.thresholdImage(sourceImage, 170);
            return filterTools.runMatrixFilter(sourceImage, FilterTools.edgeFilter);
        }, "combo");
    }


    String imageFileName(final int number, final boolean original, final boolean ideal) {
        String filePath = "../robo-dice-roller/images";
        String small = "";

        if(original) {
            filePath += "/original";
        }
        else {
            filePath += "/small";
            small = "_small";
        }

        switch (number) {
            case 1:
                filePath += "/one_";
                break;
            case 2:
                filePath += "/two_";
                break;
            case 3:
                filePath += "/three_";
                break;
            case 4:
                filePath += "/four_";
                break;
            case 5:
                filePath += "/five_";
                break;
            case 6:
                filePath += "/six_";
                break;
            default:
                logInfo("Unexpected face name " + number);
                break;
        }
        if(ideal)
            filePath += "ideal";
        else
            filePath += "unideal";


        filePath = filePath + small + ".jpg";
        return filePath;
    }

    BufferedImage loadImage(final String filePath) {
        logInfo("trying to load file: " + filePath);

        imageData = new ArrayList<>();
        imageData.add("File loaded = " + filePath);

        BufferedImage newImage = null;
        final File file = new File(filePath);
        if (file.exists() && file.canRead()) {
            try {
                newImage = ImageIO.read(file);
                imageData.add("Original image size: (" + newImage.getWidth() + ", " + newImage.getHeight() + ")");
            } catch (final IOException e) {
                logInfo("Cannot read image from File " + filePath + ". Stack trace\n");
                e.printStackTrace(System.out);
            }
        } else {
            logInfo("File " + filePath + " does not exist, or is unreadable");
        }
        if (newImage == null) {
            logInfo("File " + filePath + " is not a valid image");
        }
        return newImage;
    }

    void displayImage(ActionEvent actionEvent, boolean original, boolean ideal) {
        final int number = Integer.parseInt(actionEvent.getActionCommand());
        final String filePath = imageFileName(number, original, ideal);

        BufferedImage bufferedImage = loadImage(filePath);
        if (bufferedImage != null) {
            ProcessedImage processedImage = new ProcessedImage();
            processedImage.fileName = filePath;
            processedImage.image = bufferedImage;
            processedImage.originalImage = bufferedImage;
            this.processedImage = processedImage;
            filterTools.resizeImageIfBig(processedImage, 640, 480);
            imageData.add("New image size (" + processedImage.image.getWidth() + ", " + processedImage.image.getHeight() + ")");
            imageHolder.setImage(processedImage.image);
            image.setIcon(imageHolder);
            imageFrame.pack();
            imageFrame.repaint(30);
        }
        refreshInfoPanel();
    }

    void refreshInfoPanel() {
        final String output = String.join("\n", imageData);
        infoView.setText(output);
    }

    public static void main(String[] args) {
        new Main();
    }
}

/* Notes
 * 1/2/23 -> For getting consistent "straight" lines for the die, I run sharp,strongblur, convert b/w, edge.
 *           This process detected fairly good edges in all 12 example images (both unideal and ideal)
 *
 *           Something to note going forward into the "die detection" phase of our work is that for all the example die,
 *           there were AT LEAST 2 sides that were mostly unobstructed linear edges that we might want to use for our algo.
 *           Edges however are curved at the corners and so having "straight lines" will be difficult to detect.
 *
 *           Next Steps for me are to detect a dice "object" as a contiguous square block of white pixels, and detect the pips as contiguous circular blocks of white pixels.
 */