// WumpusWorldMain.java
// Zack Waterson
//
// A basic main that sets up the game's window

import javax.swing.*;

public class WumpusWorldMain {
    public static void main(String[] args)
    {
        JFrame frame = new JFrame ("Wumpus World!");
        frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        Board board = new Board ();
        frame.getContentPane().add(board);
        frame.pack();
        frame.setVisible(true);
    }
}