package dicedetector;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {

    void test() {
        System.out.println("Method 3");
    }

    void displayImage(JFrame frame, int number, boolean original, boolean ideal) {

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
        imageHolder.setImage(bufferedImage);
        image.setIcon(imageHolder);
        frame.pack();
    }

    JMenu menu, imageSubMenu;
    JMenuItem a1, imageSixSmall, imageOneOriginal;
    BufferedImage bufferedImage;
    ImageIcon imageHolder;
    JLabel image;

    public Main() {
        JFrame frame = new JFrame("Hello world!");
        menu = new JMenu("options");
        JMenuBar m1 = new JMenuBar();
        a1 = new JMenuItem("Lambda Tester");
        menu.add(a1);
        m1.add(menu);
        /* Three different examples of using lambda functions for JFrame listeners */
        a1.addActionListener((ActionEvent e) -> test());
        a1.addActionListener(e -> System.out.println("Method 2"));
        a1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                System.out.println("Method 1");
            }
        });

        image = new JLabel();
        imageHolder = new ImageIcon();

        frame.add(image);

        imageSixSmall = new JMenuItem("Load Small Ideal 6");
        imageOneOriginal = new JMenuItem("Load Original Unideal 1");

        imageOneOriginal.addActionListener((ActionEvent e) -> displayImage(frame, 1, true, false));
        imageSixSmall.addActionListener((ActionEvent e) -> displayImage(frame, 6, false, true));
        // a2.addActionListener((ActionEvent e) -> displayImage(a));

        imageSubMenu = new JMenu("Load Images");
        imageSubMenu.add(imageOneOriginal);
        imageSubMenu.add(imageSixSmall);

        m1.add(imageSubMenu);

        frame.setJMenuBar(m1);
        frame.setSize(400, 400);
        frame.setLayout(new FlowLayout());
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new Main();
    }

}