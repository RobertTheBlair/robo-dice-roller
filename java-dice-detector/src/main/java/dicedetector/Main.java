package dicedetector;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {


    final double[][] blurFilter = {
        {0.0625, 0.125, 0.0625},
        {0.125, 0.25, 0.125},
        {0.0625, 0.125, 0.0625}
};

    private void updateImage(final ActionEvent actionEvent) {
        int threshold = 3 * 170;

        logInfo("updating the image");
        if (bufferedImage!=null) {
            WritableRaster writableRaster = bufferedImage.getRaster();
            int[] pixel = new int[4];
            int[] outPixel = new int[4];
            int width = writableRaster.getWidth();
            int height = writableRaster.getHeight();
            for (int y=0;y<height; y++) {
                for (int x = 0; x< width; x++) {
                    writableRaster.getPixel(x, y, pixel);
                    int pixelSum = pixel[0] + pixel[1] + pixel[2];
                    if (pixelSum > threshold) {
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
        }
        imageFrame.repaint();
    }

    void blurImage(final ActionEvent actionEvent) {
        if (bufferedImage!=null) {
            WritableRaster referenceRaster = bufferedImage.getRaster();
            WritableRaster blurredRaster = referenceRaster; // we will be updating blur a
            int width = referenceRaster.getWidth();
            int height = referenceRaster.getHeight();
            int[] tempPixel = new int[4];
            int[] outPixel = new int[4];
            outPixel[3] = 255;
            /* for each pixel, we average its RGB values with the surrounding pixels if they exist */
            for (int y=0; y < height; y++) { //jesus christ 4 4loops!!!
                for (int x = 0; x < width; x++) {
                    outPixel[0] = 0;
                    outPixel[1] = 0;
                    outPixel[2] = 0;
                    for(int i = 0; i < 3; i++) {
                        for(int j = 0; j < 3; j++) {
                            int pixRow = y+i-1;
                            int pixCol = x+j-1;

                            if(pixCol > 0 && pixRow > 0 && pixCol < width && pixRow < height) { //if this is a valid pixel location we add its value to the running sum
                                referenceRaster.getPixel(pixCol, pixRow, tempPixel);
                                outPixel[0] += (tempPixel[0]*blurFilter[i][j]);
                                outPixel[1] += (tempPixel[1]*blurFilter[i][j]);
                                outPixel[2] += (tempPixel[2]*blurFilter[i][j]);
                            }
                        }
                    }
                    blurredRaster.setPixel(x, y, outPixel);
                }
            }
            referenceRaster = blurredRaster;
        }
        imageFrame.repaint();
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

        BufferedImage newImage = null;
        final File file = new File(filePath);
        if (file.exists() && file.canRead()) {
            try {
                newImage = ImageIO.read(file);
            } catch (final IOException e) {
                logInfo("Cannot read image from File " + filePath + ". Stack trace\n");
                e.printStackTrace(System.out);
            }
        } else {
            logInfo("File " + filePath + " does not exist, or is unreadable");
        }
        if (bufferedImage == null) {
            logInfo("File " + filePath + " is not a valid image");
        }
        return newImage;
    }

    void displayImage(ActionEvent actionEvent, boolean original, boolean ideal) {

        final int number = Integer.parseInt(actionEvent.getActionCommand());
        final String filePath = imageFileName(number, original, ideal);

        logInfo("trying to load file: " + filePath);
        bufferedImage = loadImage(filePath);
        if (bufferedImage == null) {
            return;
        }
        bufferedImage = resizeImageIfBig(bufferedImage, 640, 480);
        imageHolder.setImage(bufferedImage);
        image.setIcon(imageHolder);
        imageFrame.pack();
        imageFrame.repaint(30);
    }

    BufferedImage resizeImageIfBig(BufferedImage inputImage, int targetMaxWidth, int targetMaxHeight) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        if (width > targetMaxWidth || height > targetMaxHeight) {
            double widthScale = (double)targetMaxWidth / width;
            double heightScale = (double)targetMaxHeight / height;
            double scale = Math.min(widthScale, heightScale);
            int newWidth = (int)(width * scale);
            int newHeight = (int)(height * scale);

            logInfo("w=" + width + ", tw=" + targetMaxWidth + " h=" + height + " th=" + targetMaxHeight + " scaleW=" + widthScale + "scaleH=" + heightScale);

            Image originalImage = inputImage.getScaledInstance(newWidth, (int)(height * scale), Image.SCALE_SMOOTH);

            int type = ((inputImage.getType() == 0) ? BufferedImage.TYPE_INT_ARGB : inputImage.getType());
            // create a buffer of the desired processing size
            BufferedImage resizedImage = new BufferedImage(targetMaxWidth, targetMaxHeight, type);

            // create a graphics context to write resized image into
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.setColor(Color.GRAY);
            g2d.fillRect(0, 0, targetMaxWidth, targetMaxHeight);

            // center the resized image inside the target area, and draw it
            int offsetX = (targetMaxWidth - newWidth)/2;
            int offsetY = (targetMaxHeight - newHeight)/2;
            g2d.drawImage(originalImage, offsetX, offsetY, null);
            g2d.dispose();
            return resizedImage;
        }
        return inputImage;
    }

    BufferedImage bufferedImage;
    ImageIcon imageHolder;
    JLabel image;
    JFrame imageFrame;
    JTextPane logView;
    JPanel infoPane;

    JTextPane infoView;

    List<String> logData = new ArrayList<>();

    public Main() {
        imageFrame = new JFrame("Image viewer");

        infoPane = new JPanel();
        infoView = new JTextPane();
        logView = new JTextPane();

        infoView.setText("Information");
        infoView.setPreferredSize(new Dimension(400, 200));
        infoView.setBorder(new TitledBorder("Information"));
        infoView.setEditable(false);
        infoView.setMinimumSize(new Dimension(200, 200));

        logView.setText("abbce\ndefgh");
        logView.setBorder(new TitledBorder("App logs"));
        logView.setEditable(false);
        infoView.setPreferredSize(new Dimension(400, 200));

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
        System.out.print(str);
    }

    private JMenuBar initMenu() {

        JMenu manipulationMenu = new JMenu("options");
        JMenuItem bwUpdate = new JMenuItem("convert to b/w");
        JMenuItem blur = new JMenuItem("blur image");
        blur.addActionListener(this::blurImage);
        bwUpdate.addActionListener(this::updateImage);
        manipulationMenu.add(bwUpdate);
        manipulationMenu.add(blur);

        JMenu imageSubMenu = new JMenu("Load Images");

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

    public static void main(String[] args) {
        new Main();
    }

}