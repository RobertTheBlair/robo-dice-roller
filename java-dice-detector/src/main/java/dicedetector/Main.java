package dicedetector;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

public class Main {

    private void updateImage(final ActionEvent actionEvent) {
        int threshold = 3 * 170;

        System.out.println("updating the image");
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

    void displayImage(ActionEvent actionEvent, boolean original, boolean ideal) {

        String filePath = "../robo-dice-roller/images";
        String small = "";

        if(original) {
            filePath += "/original";
        }
        else {
            filePath += "/small";
            small = "_small";
        }
        final int number = Integer.parseInt(actionEvent.getActionCommand());

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
                System.out.println("Unexpected face name " + number);
        }
        if(ideal)
            filePath += "ideal";
        else
            filePath += "unideal";


        filePath = filePath + small + ".jpg";
        System.out.println("trying to load file: " + filePath);
        File file = new File(filePath);
        try {
            bufferedImage = ImageIO.read(file);
        } catch (IOException e) {
            // auto generated :)
            e.printStackTrace();
        }
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        int targetMaxWidth = 640;
        int targetMaxHeight = 480;
        if (width > targetMaxWidth || height > targetMaxHeight) {
            double widthScale = (double)targetMaxWidth / width;
            double heightScale = (double)targetMaxHeight / height;
            double scale = Math.min(widthScale, heightScale);
            int newWidth = (int)(width * scale);
            int newHeight = (int)(height * scale);

            System.out.println("w=" + width + ", tw=" + targetMaxWidth + " h=" + height + " th=" + targetMaxHeight + " scaleW=" + widthScale + "scaleH=" + heightScale);

            Image originalImage = bufferedImage.getScaledInstance(newWidth, (int)(height * scale), Image.SCALE_SMOOTH);

            int type = ((bufferedImage.getType() == 0) ? BufferedImage.TYPE_INT_ARGB : bufferedImage.getType());
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
            bufferedImage = resizedImage;
        }
        imageHolder.setImage(bufferedImage);
        image.setIcon(imageHolder);
        imageFrame.pack();
        imageFrame.repaint(30);
    }

    BufferedImage bufferedImage;
    ImageIcon imageHolder;
    JLabel image;
    JFrame imageFrame;

    public Main() {
        imageFrame = new JFrame("Image viewer");

        image = new JLabel();
        imageHolder = new ImageIcon();

        imageFrame.add(image);

        imageFrame.setJMenuBar(initMenu());

        imageFrame.setSize(640, 480);
        imageFrame.setLayout(new FlowLayout());
        imageFrame.setVisible(true);
    }

    private JMenuBar initMenu() {

        JMenu manipulationMenu = new JMenu("options");
        JMenuItem bwUpdate = new JMenuItem("convert to b/w");
        bwUpdate.addActionListener(this::updateImage);
        bwUpdate.addActionListener(this::updateImage);
        bwUpdate.addActionListener(this::updateImage);
        bwUpdate.addActionListener(this::updateImage);
        manipulationMenu.add(bwUpdate);

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