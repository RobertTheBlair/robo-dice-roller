package dicedetector;

import javax.swing.*;

public class Main {

    JMenu menu;
    JMenuItem a1,a2;
    public Main() {
        JFrame a = new JFrame("Hello world!");
        menu = new JMenu("options");
        JMenuBar m1 = new JMenuBar();
        a1 = new JMenuItem("item 1");
        a2 = new JMenuItem("item 2");
        menu.add(a1);
        menu.add(a2);
        m1.add(menu);
        a.setJMenuBar(m1);
        a.setSize(400,400);
        a.setLayout(null);
        a.setVisible(true);
    }
    public static void main(String[] args) {
        new Main();
    }

}