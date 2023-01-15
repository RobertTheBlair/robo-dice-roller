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

    ImageIcon imageHolder;
    JLabel image;
    JFrame imageFrame;
    JTextPane logView;
    JPanel infoPane;
    JTextPane infoView;
    boolean headlessMode;

    ProcessedImage processedImage;
    List<String> logData = new ArrayList<>();
    List<String> imageData = new ArrayList<>();
    FilterTools filterTools = new FilterTools();


    public Main(boolean headlessMode) {
        this.headlessMode = headlessMode;
        if (headlessMode == false) {
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
            infoPane.setLayout(new GridLayout(0, 1));

            image = new JLabel();
            imageHolder = new ImageIcon();
            imageFrame.add(infoPane);
            imageFrame.add(image);
            imageFrame.setJMenuBar(initMenu());

            imageFrame.setSize(640, 480);
            imageFrame.setLayout(new FlowLayout());
            imageFrame.setVisible(true);
        }
    }

    public void logInfo(final String str) {
        logData.add(str);
        final String output = String.join("\n", logData);
        if (!headlessMode) logView.setText(output);
        System.out.println(str);
    }

    private JMenuBar initMenu() {

        JMenu manipulationMenu = new JMenu("modify image");


        JMenuItem menuItem = new JMenuItem("convert to b/w");
        menuItem.addActionListener(this::thresholdImage);
        manipulationMenu.add(menuItem);

        JMenu subBlurMenu = new JMenu("blur options");

        menuItem= new JMenuItem("blur image");
        menuItem.addActionListener(this::blurImage);
        subBlurMenu.add(menuItem);

        menuItem = new JMenuItem("blur 5x5 image");
        menuItem.addActionListener(this::blurImage5);
        subBlurMenu.add(menuItem);

        menuItem = new JMenuItem("strong blur");
        menuItem.addActionListener(this::strongBlur);
        subBlurMenu.add(menuItem);

        menuItem = new JMenuItem("median blur 5");
        menuItem.addActionListener(actionEvent
            -> runFilterAction(sourceImage -> filterTools.runMedianBlur(sourceImage, 5), "median blur 5"));
        subBlurMenu.add(menuItem);

        menuItem = new JMenuItem("median blur 3");
        menuItem.addActionListener(actionEvent
            -> runFilterAction(sourceImage -> filterTools.runMedianBlur(sourceImage, 3), "median blur 3"));
        subBlurMenu.add(menuItem);

        manipulationMenu.add(subBlurMenu);

        menuItem = new JMenuItem("edge image");
        menuItem.addActionListener(this::edgeImage);
        manipulationMenu.add(menuItem);

        JMenu subComboMenu = new JMenu("combo options");

        menuItem = new JMenuItem("soft_blur/thresh/edge");
        menuItem.addActionListener(this::smallComboImage);
        subComboMenu.add(menuItem);

        menuItem = new JMenuItem("sharp/strong_blur/thresh/edge");
        menuItem.addActionListener(this::largeComboImage);
        subComboMenu.add(menuItem);

        manipulationMenu.add(subComboMenu);

        JMenu sharpBlurMenu = new JMenu("sharp options");

        menuItem = new JMenuItem("sharp");
        menuItem.addActionListener( actionEvent
                 -> runFilterAction(sourceImage -> filterTools.runMatrixFilter(sourceImage, FilterTools.sharpenFilter), "sharp"));
        sharpBlurMenu.add(menuItem);

        menuItem = new JMenuItem("unsharp");
        menuItem.addActionListener( actionEvent
                 -> runFilterAction(sourceImage -> filterTools.runMatrixFilter(sourceImage, FilterTools.unsharpMaskFilter), "unsharp"));
        sharpBlurMenu.add(menuItem);

        manipulationMenu.add(sharpBlurMenu);

        menuItem= new JMenuItem("detectThing");
        menuItem.addActionListener( actionEvent
                 -> runFilterAction(sourceImage -> filterTools.scanRowColImage(sourceImage), "Detect thing"));
        manipulationMenu.add(menuItem);

        menuItem = new JMenuItem("analyze");
        menuItem.addActionListener( actionEvent
                 -> runFilterAction(sourceImage -> filterTools.scanAndThreshold(sourceImage), "analyze"));
        manipulationMenu.add(menuItem);

        JMenu imageSubMenu = new JMenu("Load Image");

        JMenu imageOjectMenu = new JMenu("Image Objects");

        menuItem = new JMenuItem("find Die");

        menuItem.addActionListener(this::findDieImage);
        imageOjectMenu.add(menuItem);

        for (int i=1;i<=6; i++) {
            menuItem = imageSubMenu.add(new JMenuItem("Load Small Ideal " + i));
            menuItem.addActionListener((ActionEvent e) -> displayImage(e, false, true));
            menuItem.setActionCommand(String.valueOf(i));
        }
        for (int i=1;i<=6; i++) {
            menuItem = imageSubMenu.add(new JMenuItem("Load Original Unideal " + i));
            menuItem.setActionCommand(String.valueOf(i));
            menuItem.addActionListener((ActionEvent e) -> displayImage(e,true, false));
        }

        String[] otherFiles = {
                "images/random_junk/multiple_close_white_dice.jpg",
                "images/random_junk/multiple_close_white_dice2.jpg",
                "images/random_junk/multiple_red_dice.jpg",
                "images/random_junk/multiple_white_dice.jpg",
                "images/random_junk/multiple_white_dice2.jpg",
                "images/random_junk/multiple_white_dice3.jpg",
                "images/random_junk/multiple_white_dice4.jpg",
        };
        for (int i=0;i<otherFiles.length;i++) {
            String path = otherFiles[i];
            menuItem = imageSubMenu.add(new JMenuItem("Load File " + path.substring(path.lastIndexOf("/") + 1)));
            menuItem.addActionListener((ActionEvent e) -> displayImageByPath(path));
        }

        JMenuBar m1 = new JMenuBar();
        m1.add(imageSubMenu);
        m1.add(manipulationMenu);
        m1.add(imageOjectMenu);
        return m1;
    }

    void runFilterAction(FilterTools.ImageAction action, String filterName) {
        if (processedImage!=null) {
            long startTime = System.currentTimeMillis();
            action.apply(processedImage);
            long endTime = System.currentTimeMillis();
            imageData.add(filterName + " action: duration MS = " + (endTime - startTime));
            if (!headlessMode) imageHolder.setImage(processedImage.alteredImage);
        }
        if (!headlessMode) {
            refreshInfoPanel();
            imageFrame.repaint();
        }
    }
    /* unfinished */
    void findDieImage(final ActionEvent actionEvent) {
        //todo, find die object and store it in the image variable.
        runFilterAction( sourceImage -> filterTools.findDieInImage(sourceImage), "find die");
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

    void displayImageByPath(String filePath) {
        ProcessedImage processedImage = loadImageFromFile(filePath);
        if (processedImage != null) {
            this.processedImage = processedImage;
            imageHolder.setImage(processedImage.alteredImage);
            image.setIcon(imageHolder);
            imageFrame.pack();
            imageFrame.repaint(30);
        }
        refreshInfoPanel();
    }

    ProcessedImage loadImageFromFile(String filePath) {
        BufferedImage bufferedImage = loadImage(filePath);
        if (bufferedImage != null) {
            ProcessedImage processedImage = new ProcessedImage();
            processedImage.fileName = filePath;
            processedImage.alteredImage = bufferedImage;
            processedImage.originalImage = bufferedImage;
            processedImage.dieObjects = new ArrayList<>();
            filterTools.resizeImageIfBig(processedImage, 640, 480);
            imageData.add("New image size (" + processedImage.alteredImage.getWidth() + ", " + processedImage.alteredImage.getHeight() + ")");
            return processedImage;
        }
        return null;
    }

    void displayImage(ActionEvent actionEvent, boolean original, boolean ideal) {
        final int number = Integer.parseInt(actionEvent.getActionCommand());
        final String filePath = imageFileName(number, original, ideal);
        displayImageByPath(filePath);
    }

    void refreshInfoPanel() {
        final String output = String.join("\n", imageData);
        infoView.setText(output);
    }

    public static void main(final String[] args) {
        if (args == null || args.length == 0) {
            new Main(false);
        } else {
            final DiceDetectCommand detectCommand  = createDetectCommandFromArgs(args);
            if (detectCommand == null) {
                System.exit(1);
            } else {
                Main processor = new Main(true);
                processor.processedImage = processor.loadImageFromFile(detectCommand.file);
                processor.smallComboImage(null);
                processor.findDieImage(null);
            }
        }
    }

    public static DiceDetectCommand createDetectCommandFromArgs(final String[] args) {
        String filter = null;
        String file = null;
        String detector = null;

        int commandIndex = 0;
        while (commandIndex < args.length) {
            switch(args[commandIndex]) {
                case "-f":
                case "--file":
                    if (commandIndex+1 >= args.length) {
                        System.out.printf("File command missing file name%n");
                        return null;
                    }
                    file = args[commandIndex + 1];
                    commandIndex += 2;
                    break;
                case "-p":
                case "--process":
                    if (commandIndex+1 >= args.length) {
                        System.out.printf("Process command missing processor name%n");
                        return null;
                    }
                    filter = args[commandIndex + 1];
                    commandIndex += 2;
                    break;
                case "-d":
                case "--detector":
                    if (commandIndex+1 >= args.length) {
                        System.out.printf("Process command missing detector name%n");
                        return null;
                    }
                    detector = args[commandIndex + 1];
                    commandIndex += 2;
                    break;

                default:
                    System.out.printf("Unknown command line option %s%n", args[0]);
                    return null;
            }
        }
        if (file == null) {
            System.out.printf("No file to process provide%n");
            return null;
        }
        DiceDetectCommand detectCommand  = new DiceDetectCommand();
        detectCommand.file = file;
        detectCommand.detector = detector;
        detectCommand.processFilter = filter;
        return detectCommand;
    }

    public static class DiceDetectCommand {

        String file;
        String processFilter;
        String detector;

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