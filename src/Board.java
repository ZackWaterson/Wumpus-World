// Board.java
// Zack Waterson
//
// This class creates and manages the different elements on the board

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Random;

public class Board extends JPanel
{
    //used to create the board and also is set this way so that functions can scale to bigger boards
    final int BOARD_SIZE = 4;
    //these are the rows of the game board, they are a special class Square that keeps track of
    //many boolean
    private Square[] squares; //a single list of all the arrays for easier iteration
    private JButton nextButton, resetButton;
    private JTextArea actionLog;
    JScrollPane scrollPane;
    // a bunch of icons used in the game
    private ImageIcon wumpusIcon, pitIcon, breezeIcon, stenchIcon, glitterIcon;
    Random random = new Random();
    Agent agent;

    public Board()
    {
        setPreferredSize(new Dimension(530, 700));
        setLayout(null);

        nextButton = new JButton("Next Move");
        nextButton.setBounds(65, 420, 180, 25);
        nextButton.setMnemonic(KeyEvent.VK_SPACE);
        nextButton.setMnemonic(KeyEvent.VK_SPACE);
        nextButton.addActionListener(new NextMoveListener());
        add(nextButton);

        resetButton = new JButton("Reset");
        resetButton.setBounds(285, 420, 180, 25);
        resetButton.addActionListener(new ResetListener());
        add(resetButton);

        actionLog = new JTextArea();
        actionLog.setBackground(Color.LIGHT_GRAY);
        actionLog.setMargin(new Insets(10, 10, 10, 10));
        actionLog.setLineWrap(true);
        actionLog.setWrapStyleWord(true);
        actionLog.setEditable(false);
        scrollPane = new JScrollPane(actionLog);
        scrollPane.setBounds(30, 460, 470, 230);
        add(scrollPane);
        actionLog.setText("Welcome to Wumpus World!\n");

        //these images must be in a folder called resource inside src
        wumpusIcon = new ImageIcon(getClass().getResource("/resource/WUMPUS_ICON.jpg"));
        pitIcon = new ImageIcon(getClass().getResource("/resource/PIT_ICON.jpg"));
        breezeIcon = new ImageIcon(getClass().getResource("/resource/BREEZE_ICON.jpg"));
        stenchIcon = new ImageIcon(getClass().getResource("/resource/STENCH_ICON.jpg"));
        glitterIcon = new ImageIcon(getClass().getResource("/resource/GLITTER_ICON.jpg"));

        //sets up the squares and adds them to squares[]
        squares = new Square[BOARD_SIZE * BOARD_SIZE];
        generateSquares();
        newAgent();
    }

    //paints each square along with whatever icons should be on it
    public void paintComponent (Graphics page)
    {
        super.paintComponent(page);

        int counter = 0;
        for (Square square : squares) {
            if (square.checkVisible()) {
                page.setColor(Color.WHITE);
            } else {
                page.setColor(Color.BLACK);
            }
            page.fillRect(square.getX(), square.getY(), 100, 100);

            //checks various booleans from the square class to see what should be painted
            //then uses constants from the square class to place the icons in the correct locations
            if (square.checkVisible())
            {
                if (square.checkWumpus())
                {
                    wumpusIcon.paintIcon(this, page, square.getX() + square.getMainIconLoc(), square.getY() + square.getMainIconLoc());
                }
                else if (square.checkPit())
                {
                            pitIcon.paintIcon(this, page, square.getX() + square.getMainIconLoc(), square.getY() + square.getMainIconLoc());
                }
                else if (counter == agent.getCurrentSquare())
                {
                    //figures out which way agent is rotated and paints the appropriate direction
                    agent.getAgentImage().paintIcon(this, page, square.getX() + square.getAgentIconLoc(), square.getY() + square.getAgentIconLoc());
                }
                if (square.checkVisited() && !square.checkPit())
                {
                    if (square.checkBreeze())
                    {
                        breezeIcon.paintIcon(this, page, square.getX() + 2, square.getY() + 2);
                    }
                    if (square.checkStench())
                    {
                        stenchIcon.paintIcon(this, page, square.getX() + square.getStenchIconLoc(), square.getY() + 2);
                    }
                    if (square.checkGold())
                    {
                        glitterIcon.paintIcon(this, page, square.getX() + 2, square.getY() + square.getGlitterIconLoc());
                    }
                }
            }

            //paints borders last so they are on top of the edge of the icons
            page.setColor(Color.GRAY);
            page.drawRect(square.getX(), square.getY(), 100, 100);
            counter++;
        }
    }
    //creates the squares, also used for resetting the squares on board reset
    void generateSquares()
    {
        //sets up the board with the first square being in the bottom left
        //that is why the loops are nested this way
        //coords for 1 (on a 4x4 board) needs to be (65 * 0, 65 * 3)
        //coords for 2 should be (65 * 1, 65 * 3), etc
        int i = 0, xPad, yPad;
        for (int y = BOARD_SIZE - 1; y >= 0; y--)
        {
            //sets a padding of 10 for the top row
            yPad = 10;
            for (int x = 0; x < BOARD_SIZE; x++)
            {
                xPad = 65;
                squares[i] = new Square(xPad + (100 * x), yPad + (100 * y));
                i++;
            }
        }
        generateGame();
        repaint();
    }

    //function that places the agent, pits, wumpus, and gold and marks the breezes and stenches
    void generateGame()
    {
        int index;
        //places the gold on a random square
        squares[getRandInt()].toggleGold();
        //places the wumpus on a random square(can be with the gold)
        index = getRandInt();
        squares[index].toggleWumpus();
        markSquares(index);
        //sets up a number of pits that is BOARD_SIZE - 1
        //so for a 4x4 it would be 3 pits
        for (int i = 1; i < BOARD_SIZE; i++)
        {
            index = getRandInt();
            //checks to make sure the space isn't already occupied
            if (!occupiedCheck(index))
            {
                squares[index].setPit();
                markSquares(index);
            }
            //if it is occupied we run through again until it isn't
            else
            {
                i--;
            }
        }
    }

    //returns true if the square already has a pit, gold, or wumpus on it
    //which means it cannot have a (possibly second) pit
    boolean occupiedCheck(int index)
    {
        return squares[index].checkWumpus() || squares[index].checkGold() || squares[index].checkPit();
    }

    //marks squares next to a square with stench or breeze
    void markSquares(int index)
    {
        //if there's a wumpus in the square, mark adjacent with stenches
        if(squares[index].checkWumpus())
        {
            // makes sure that we don't try to go north of the top edge
            if (index + BOARD_SIZE < squares.length)
                squares[index+BOARD_SIZE].toggleStench();

            // checks to make sure we didn't wrap around to the other side of the board
            if ((index + 1) % BOARD_SIZE != 0)
                squares[index + 1].toggleStench();

            // makes sure we don't try to go below the bottom edge
            if (index - BOARD_SIZE >= 0)
                squares[index - BOARD_SIZE].toggleStench();

            // makes sure we don't go below 0 or wrap around to the other side
            if ((index - 1) >= 0 && (index - 1) % BOARD_SIZE != BOARD_SIZE - 1)
                squares[index - 1].toggleStench();
        }
        else if (squares[index].checkPit())
        {
            // makes sure that we don't try to go north of the top edge
            if (index + BOARD_SIZE < squares.length && !squares[index + BOARD_SIZE].checkPit())
                squares[index+BOARD_SIZE].setBreeze();

            // checks to make sure we didn't wrap around to the other side of the board
            if ((index + 1) % BOARD_SIZE != 0 && !squares[index + 1].checkPit())
                squares[index + 1].setBreeze();

            // makes sure we don't try to go below the bottom edge
            if (index - BOARD_SIZE >= 0 && !squares[index - BOARD_SIZE].checkPit())
                squares[index - BOARD_SIZE].setBreeze();

            // makes sure we don't go below 0 or wrap around to the other side
            if ((index - 1) >= 0 && (index - 1) % BOARD_SIZE != BOARD_SIZE - 1 && !squares[index - 1].checkPit())
                squares[index - 1].setBreeze();
        }
    }

    //creates a new agent on startup and reset
    void newAgent()
    {
        agent = new Agent(BOARD_SIZE, squares, actionLog, this);
    }

    //returns a number from 1 to BOARD_SIZE - 1 so that we don't place something on the starting square
    //for a 4x4 board: BOARD_SIZE * BOARD_SIZE(squares.length) == 16
    //then we would get 0 to 15 so minus 1 gives us 0 to 14 then plus 1 give us 1 to 15
    //without the minus 1 it would give us 1 to 16 which would be a potential array out of bounds error
    int getRandInt()
    {
        return random.nextInt((squares.length) - 1) + 1;
    }

    //makes the next move when nextButton is clicked
    private class NextMoveListener implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            if(!agent.checkEnd())
            {
                agent.makeDecision();

                //I used all these printouts for debugging
                //they go to the console, not actionLog
                System.out.println("Stench Array: ");
                for (int i = 0; i < squares.length; i++)
                {
                    System.out.print(Integer.toString(agent.brain.stenchArray[i]));
                    if(i%4 < 3)
                        System.out.print(", ");
                    else
                        System.out.println();
                }
                System.out.println("\n\nBreeze Array:");
                for (int i = 0; i < squares.length; i++)
                {
                    System.out.print(Integer.toString(agent.brain.breezeArray[i]));
                    if(i%4 < 3)
                        System.out.print(", ");
                    else
                        System.out.println();
                }
                System.out.println("\n\nSafe Array: ");
                for (int i = 0; i < squares.length; i++)
                {
                    System.out.print(Boolean.toString(agent.brain.safe[i]));
                    if(i%4 < 3)
                        System.out.print(", ");
                    else
                        System.out.println();
                }
                System.out.println("\n\nVisited Array: ");
                for (int i = 0; i < squares.length; i++)
                {
                    System.out.print(Boolean.toString(squares[i].checkVisited()));
                    if(i%4 < 3)
                        System.out.print(", ");
                    else
                        System.out.println();
                }
                System.out.println("\n\nVisible Array: ");
                for (int i = 0; i < squares.length; i++)
                {
                    System.out.print(Boolean.toString(squares[i].checkVisible()));
                    if(i%4 < 3)
                        System.out.print(", ");
                    else
                        System.out.println();
                }
                System.out.println("\narrow ==" + agent.arrow);
                System.out.println("\nknownWumpus == " + agent.brain.checkConfirmedWumpus());
                if (agent.brain.checkConfirmedWumpus())
                    System.out.println("\nwumpusIndex == " + "(" + agent.brain.getWumpusIndex() / BOARD_SIZE + ", " + agent.brain.getWumpusIndex() % BOARD_SIZE + ").");
                System.out.println("\n\n");
                //repaint();
            }
            else
            {
                agent.appendText("\nNew Board!\nCurrent score: " + agent.getScore() + "\n");
                generateSquares();
                agent.newBoard();
            }
        }
    }

    //resets everything as if the program has just started
    private class ResetListener implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            generateSquares();
            newAgent();
            actionLog.setText("Welcome to Wumpus World!\n");
            //repaint();
        }
    }
}