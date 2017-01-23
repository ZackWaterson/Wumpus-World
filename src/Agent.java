// Agent.java
// Zack Waterson
//
// This class has many of the functions used by the agent
// and will call brain for decision making

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Agent
{
    final int BOARD_SIZE;
    //used to track how many boards have been played
    private int boardsPlayed;
    final Board board;
    private int score, currentSquare;
    //a list of booleans to track a few things
    /*private*/ boolean arrow, end;
    private Direction currentDirection;
    //some icons used depending on which way the agent is facing
    private ImageIcon agentNorth, agentEast, agentSouth, agentWest;
    private Square[] squares;
    DecimalFormat fmt;
    JTextArea actionLog;
    Brain brain;

    // requires the board size constant and squares array (by reference)
    public Agent(int boardSize, Square[] sqrs, JTextArea log, Board b)
    {
        BOARD_SIZE = boardSize;
        board = b;
        squares = sqrs;
        actionLog = log;
        score = 0;
        fmt = new DecimalFormat("##.00");
        boardsPlayed = 0;
        newBoard();
        agentNorth = new ImageIcon(getClass().getResource("/resource/AGENT_NORTH.jpg"));
        agentEast = new ImageIcon(getClass().getResource("/resource/AGENT_EAST.jpg"));
        agentSouth = new ImageIcon(getClass().getResource("/resource/AGENT_SOUTH.jpg"));
        agentWest = new ImageIcon(getClass().getResource("/resource/AGENT_WEST.jpg"));
    }

    // returns the icon that matches the direction the agent is facing
    ImageIcon getAgentImage()
    {
        ImageIcon image = agentNorth;
        switch(currentDirection)
        {
            case EAST:
                image = agentEast;
                break;
            case SOUTH:
                image = agentSouth;
                break;
            case WEST:
                image = agentWest;
        }
        return image;
    }

    //this is the function that drives the program
    //it basically is a bunch of if statements and function calls that form a decision tree
    void makeDecision()
    {
        int temp;
        Path path;
        Direction direction;

        if(!end) {
            //first, if it the first turn and the squares are dangerous we just go forward
            if (brain.checkFirstMove())
            {
                if (squares[0].checkStench() && arrow)
                    shoot();
                else
                    moveForward();
            }
            //if there is gold in the square pick it up and win
            else {
                if (squares[currentSquare].checkGold()) {
                    grab();
                }
                //then if the square in front is safe, just go forward
                else {
                    temp = getAdjacent();
                    if (brain.boundCheck(temp) && brain.checkSafe(temp) && !squares[temp].checkVisited()) {
                        appendText("I'm moving forward.");
                        moveForward();
                    }
                    //otherwise we find the nearest safe unvisited space and go there
                    else {
                        temp = brain.findNearestSafe(currentSquare);
                        if (temp != -1) {
                            appendText("I'm looking for the nearest safe square.");
                            if (brain.getManhattanDistance(currentSquare, temp) > 1)
                            {
                                path = findPath(temp);
                                followPath(path);
                            }
                            direction = pointAtSquare(temp);
                            smartRotate(direction);
                            moveForward();
                        }
                        //next we kill the wumpus if we know where it is
                        else {
                            if (brain.checkConfirmedWumpus() && arrow) {
                                appendText("I'm hunting a confirmed wumpus!");
                                if (brain.getManhattanDistance(currentSquare, brain.getWumpusIndex()) > 1)
                                {
                                    path = findPath(brain.getWumpusIndex());
                                    followPath(path);
                                }
                                direction = pointAtSquare(brain.getWumpusIndex());
                                smartRotate(direction);
                                shoot();
                            }
                            //next we guess at where the wumpus most likely is and try to shoot it
                            else {
                                if (brain.checkStenchArray() && arrow) {
                                    appendText("I'm hunting a possible wumpus!");
                                    temp = brain.guessWumpus();
                                    if (brain.getManhattanDistance(currentSquare, temp) > 1) {
                                        path = findPath(temp);
                                        followPath(path);
                                    }
                                    appendText("Shooting at index: " + temp);
                                    direction = pointAtSquare(temp);
                                    smartRotate(direction);
                                    shoot();
                                }
                                //in the worst case we go to the least dangerous square and go there, hoping we don't die
                                //this is the risky part if all else failed
                                else {
                                    temp = brain.findLeastDangerous(currentSquare);
                                    appendText("I need to take a risk!");
                                    if (brain.getManhattanDistance(currentSquare, temp) > 1) {
                                        path = findPath(temp);
                                        followPath(path);
                                    }
                                    appendText("Least dangerous index: " + temp);
                                    direction = pointAtSquare(temp);
                                    smartRotate(direction);
                                    moveForward();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //moves the agent forward a space
    void moveForward()
    {
        int adjacentIndex;
        adjacentIndex = getAdjacent();
        //a -1 means we bumped into a wall (tried to go outside the board)
        if (adjacentIndex != -1 && brain.boundCheck(adjacentIndex))
        {
            currentSquare = adjacentIndex;
            score -= 1;
            appendText("I moved forward into (" + currentSquare / BOARD_SIZE + ", " + currentSquare % BOARD_SIZE + ").\n Current score: " + score + "\n");
            brain.enterSquare(currentSquare);
            if (brain.checkDeath())
                die();
            paintAndWait();
        }
        else
        {
            appendText("I bumped into a wall!");
            rotateRight();
        }
    }

    //this function performs an A* search to find the path to the square adjacent to
    //either the nearest safest(lowest danger counts if not 0) square or the wumpusIndex
    Path findPath(int endIndex)
    {
        appendText("I'm looking for a path!");
        ArrayList<Path> paths = new ArrayList<Path>();
        Path shortestPath = new Path(BOARD_SIZE);
        boolean found = false;
        //first we see which ways we can go and create the beginning of each path
        if (brain.checkNorth(currentSquare) && squares[currentSquare + BOARD_SIZE].checkVisited())
        {
            paths.add(new Path(currentSquare, Direction.NORTH, brain.getManhattanDistance((currentSquare + BOARD_SIZE), endIndex), BOARD_SIZE));
        }

        if (brain.checkEast(currentSquare) && squares[currentSquare + 1].checkVisited())
        {
            paths.add(new Path(currentSquare, Direction.EAST, brain.getManhattanDistance((currentSquare + 1), endIndex), BOARD_SIZE));
        }

        if (brain.checkSouth(currentSquare) && squares[currentSquare - BOARD_SIZE].checkVisited())
        {
            paths.add(new Path(currentSquare, Direction.SOUTH, brain.getManhattanDistance((currentSquare - BOARD_SIZE), endIndex), BOARD_SIZE));
        }

        if (brain.checkWest(currentSquare) && squares[currentSquare - 1].checkVisited())
        {
            paths.add(new Path(currentSquare, Direction.WEST, brain.getManhattanDistance((currentSquare - 1), endIndex), BOARD_SIZE));
        }

        //these booleans are used when deciding where to expand
        boolean[] checks;
        int lowestCost, index = 0;
        while(!found)
        {
            System.out.println("Back up here! " + paths.size());
            System.out.println("End index = " + endIndex);
            lowestCost = 100;
            index = 0;
            checks = new boolean[]{false, false, false, false};
            //first find the path with the lowest cost
            for (int i = 0; i < paths.size(); i++)
            {
                if (paths.get(i).getTotalCost() < lowestCost)
                {
                    lowestCost = paths.get(i).getTotalCost();
                    index = i;
                    System.out.println(paths.get(i));
                }
            }
            //checks to see if the lowest cost path has reached our goal
            if (paths.get(index).getStraightLineCost() == 1)
            {
                found = true;
                shortestPath = paths.get(index);
            }
            //otherwise we need to expand that lowest cost path
            else
            {
                //we need to look at how many adjacent visited squares there are to expand
                if(brain.checkNorth(paths.get(index).getCurrentSquare()) && squares[paths.get(index).getCurrentSquare() + BOARD_SIZE].checkVisited())
                {
                    checks[0] = true;
                }
                if(brain.checkEast(paths.get(index).getCurrentSquare()) && squares[paths.get(index).getCurrentSquare() + 1].checkVisited())
                {
                    checks[1] = true;
                }
                if(brain.checkSouth(paths.get(index).getCurrentSquare()) && squares[paths.get(index).getCurrentSquare() - BOARD_SIZE].checkVisited())
                {
                    checks[2] = true;
                }
                if(brain.checkWest(paths.get(index).getCurrentSquare()) && squares[paths.get(index).getCurrentSquare() - 1].checkVisited())
                {
                    checks[3] = true;
                }

                boolean multiplePaths = false;
                Path expandingPath = new Path(paths.get(index), BOARD_SIZE);
                Path tempPath;
                //now we need to expand each path
                //so inside this for loop it looks at each boolean to see if there is an adjacent square that has been visited
                //if there is it adds it to the paths array for further expansion
                //use tempPath because if we need to add multiple paths (almost every time) then we need the original path
                //again before it was added to. Then tempPath gets added to paths[] and then the next direction is added to it
                for (int i = 0; i < checks.length; i++)
                {
                    /*System.out.println("I am adding to the path!");
                    System.out.println("tempPath: " + expandingPath);
                    System.out.println("tempPath length: " + tempPath.getPathLength());
                    System.out.println("total cost: " + tempPath.getTotalCost());
                    System.out.println("heuristic: " + tempPath.getStraightLineCost());
                    System.out.println("index: " + index);*/
                    if(checks[i])
                    {
                        switch(i)
                        {
                            case 0:
                                paths.get(index).addToPath(Direction.NORTH, brain.getManhattanDistance(paths.get(index).getCurrentSquare() + BOARD_SIZE, endIndex));
                                break;
                            case 1:
                                if(!multiplePaths)
                                {
                                    paths.get(index).addToPath(Direction.EAST, brain.getManhattanDistance(paths.get(index).getCurrentSquare() + 1, endIndex));
                                }
                                else
                                {
                                    tempPath = new Path(expandingPath, BOARD_SIZE);
                                    paths.add(tempPath);
                                    paths.get(paths.size() - 1).addToPath(Direction.EAST, brain.getManhattanDistance(paths.get(index).getCurrentSquare() + 1, endIndex));
                                }
                                break;
                            case 2:
                                if(!multiplePaths)
                                {
                                    paths.get(index).addToPath(Direction.SOUTH, brain.getManhattanDistance(paths.get(index).getCurrentSquare() - BOARD_SIZE, endIndex));
                                }
                                else
                                {
                                    tempPath = new Path(expandingPath, BOARD_SIZE);
                                    paths.add(tempPath);
                                    paths.get(paths.size() - 1).addToPath(Direction.SOUTH, brain.getManhattanDistance(paths.get(index).getCurrentSquare() - BOARD_SIZE, endIndex));
                                }
                                break;
                            case 3:
                                if(!multiplePaths)
                                {
                                    paths.get(index).addToPath(Direction.WEST, brain.getManhattanDistance(paths.get(index).getCurrentSquare() - 1, endIndex));
                                }
                                else
                                {
                                    tempPath = new Path(expandingPath, BOARD_SIZE);
                                    paths.add(tempPath);
                                    paths.get(paths.size() - 1).addToPath(Direction.WEST, brain.getManhattanDistance(paths.get(index).getCurrentSquare() - 1, endIndex));
                                }
                        }
                        multiplePaths = true;
                    }
                }
            }
        }
        for(int i = 0; i < paths.size(); i++)
        {
            appendText(paths.get(i).toString());
        }
        appendText("I am trying to move to (" + endIndex / BOARD_SIZE + ", " + endIndex % BOARD_SIZE + ").");
        appendText("I am taking: " + shortestPath);
        return shortestPath;
    }

    //so there's a story here
    //findPath() would always go into an infinite loop if it needed a path > 1 in length
    //I was setting tempPath to the first element in paths and then changing it and adding it back to paths
    //without realizing this was changing the first element as well, and so everything ballooned and
    //the function would go into an infinite loop
    //I thought of that while in the shower (reddit.com/r/showerthoughts) and wrote this function
    //right away to see if I was right (I was)
    /*void testTheory()
    {
        ArrayList<Path> paths = new ArrayList<Path>();
        Path tempPath = new Path(BOARD_SIZE);
        paths.add(new Path(currentSquare, Direction.NORTH, brain.getStraightLine((currentSquare + BOARD_SIZE), 9), BOARD_SIZE));
        tempPath = paths.get(0);
        System.out.println("paths.get(0) before change: " + paths.get(0));
        System.out.println("tempPath before change: " + tempPath);
        tempPath.addToPath(Direction.EAST, brain.getStraightLine((currentSquare + 1), 9));
        paths.add(tempPath);
        System.out.println("paths.get(0) after change: " + paths.get(0));
        System.out.println("tempPath after change: " + tempPath);
        System.out.println("paths.get(1): " + paths.get(1));
        tempPath = new Path(currentSquare, Direction.WEST, brain.getStraightLine((currentSquare + 1), 9), BOARD_SIZE);
        System.out.println("paths.get(0) after another change: " + paths.get(0));
        System.out.println("tempPath after change: " + tempPath);
        System.out.println("paths.get(1): " + paths.get(1));
    }*/

    //follows the path found by the pathfinding function in brain
    void followPath(Path path)
    {
        for(int i = 0; i < path.getPathLength(); i++)
        {
            if(!checkEnd())
            {
                if (currentDirection != path.getNextDirection(i))
                    smartRotate(path.getNextDirection(i));
                moveForward();
                paintAndWait();
            }
        }
    }

    // because the board is in an array where [0] is the bottom left
    // we can find the square to the north with currentSquare + BOARD_SIZE
    // the square to the south with currentSquare - BOARD_SIZE
    // and the eastern and western square with currentSquare + 1 and currentSquare - 1 respectively'
    // if we get a -1 back from this function it means we bumped into a wall (the edge of the board)
    int getAdjacent()
    {
        switch(currentDirection)
        {
            case NORTH:
                // makes sure that we don't try to go north of the top edge
                if (brain.checkNorth(currentSquare))
                    return currentSquare + BOARD_SIZE;
                else
                    return -1;
            case EAST:
                // checks to make sure we didn't wrap around to the other side of the board
                if (brain.checkEast(currentSquare))
                    return currentSquare + 1;
                else
                    return -1;
            case SOUTH:
                // makes sure we don't try to go below the bottom edge
                if (brain.checkSouth(currentSquare))
                    return currentSquare - BOARD_SIZE;
                else
                    return -1;
            case WEST:
                // makes sure we don't go below 0 or wrap around to the other side
                if (brain.checkWest(currentSquare))
                    return currentSquare - 1;
                else
                    return -1;
        }
        // need this because otherwise it says we aren't returning anything
        return -1;
    }

    //returns the current square the agent is in
    int getCurrentSquare()
    {
        return currentSquare;
    }

    //returns the direction so the agent can face it
    //used when shooting the wumpus
    //this is used when the agent is already in the adjacent square
    Direction pointAtSquare(int squareToFace)
    {
        //had to initialize to stop the IDE's whining
        Direction direction = Direction.NORTH;
        if(brain.checkNorth(currentSquare) && currentSquare + BOARD_SIZE == squareToFace)
            direction = Direction.NORTH;
        else if(brain.checkEast(currentSquare) && currentSquare + 1 == squareToFace)
            direction = Direction.EAST;
        else if(brain.checkSouth(currentSquare) && currentSquare - BOARD_SIZE == squareToFace)
            direction = Direction.SOUTH;
        else if (brain.checkWest(currentSquare))
            direction = Direction.WEST;
        return direction;
    }

    //rotates the agent to the correct direction, smartly
    //it will turn right every time, except when we only need one left turn(so it doesnt do 3 rights)
    void smartRotate(Direction nextDirection)
    {
        //use the ordinal positions to find how far we need to rotate
        int current = currentDirection.ordinal();
        int next = nextDirection.ordinal();

        //North to West (0 -> 3 in ordinal) is a special case that requires a special check
        //because we do current - next. If that equals 1 then it requires one rotation left
        //except North to west which equals -3
        if(current - next == -3 || current - next == 1)
        {
            rotateLeft();
            paintAndWait();
        }
        else
        {
            //if we don't do one left rotation, then we rotate right until we are facing the right way
            while(currentDirection != nextDirection)
            {
                rotateRight();
                paintAndWait();
            }
        }
    }

    //turns the agent right
    void rotateRight()
    {
        switch(currentDirection)
        {
            case NORTH:
                currentDirection = Direction.EAST;
                break;
            case EAST:
                currentDirection = Direction.SOUTH;
                break;
            case SOUTH:
                currentDirection = Direction.WEST;
                break;
            case WEST:
                currentDirection = Direction.NORTH;
        }
        appendText("I turned right.");
        board.repaint();
        //makeWait();
    }

    //turns the agent left
    void rotateLeft()
    {
        switch(currentDirection)
        {
            case NORTH:
                currentDirection = Direction.WEST;
                break;
            case EAST:
                currentDirection = Direction.NORTH;
                break;
            case SOUTH:
                currentDirection = Direction.EAST;
                break;
            case WEST:
                currentDirection = Direction.SOUTH;
                break;
        }
        appendText("I turned left.");
        board.repaint();
        //makeWait();
    }

    //grabs the gold if it is in the correct square
    void grab()
    {
        if(squares[currentSquare].checkGold())
        {
            squares[currentSquare].toggleGold();
            end = true;
            score += 1000;
            appendText("I got the gold!\nCurrent score: " + score  + "\nBoards played: " + boardsPlayed
                    + "\nAverage score (per board): " + fmt.format((double)score / boardsPlayed) + "\n");
            appendText("Hit Next Move to create a new board.");
            board.repaint();
        }
    }

    //attempts to shoot the wumpus
    void shoot()
    {
        arrow = false;
        score -= 10;
        appendText("I am shooting at (" + getAdjacent() / BOARD_SIZE + ", " + getAdjacent() % BOARD_SIZE + ").");
        if(squares[getAdjacent()].checkWumpus())
        {
            squares[getAdjacent()].toggleWumpus();
            clearStenches();
            brain.shootWumpus();
            appendText("I shot the Wumpus!\nCurrent score: " + score + "\n");
            board.repaint();
        }
        else
        {
            brain.failedShot(getAdjacent());
            appendText("I tried to shoot the Wumpus and failed!\nCurrent score: " + score + "\n");
        }
    }

    //gets rid of all stenches when the wumpus is shot
    void clearStenches()
    {
        for(Square square : squares)
        {
            square.removeStench();
        }
    }

    //checks if we won the game
    boolean checkEnd()
    {
        return end;
    }

    //called if the agent walks into the wumpus or a pit
    //this will hopefully never be called :)
    //of course sometimes the agent has no choice but to die(or at least risk death)
    void die()
    {
        score -= 1000;
        end = true;
        appendText("I died!\n Current score: " + score  + "\nBoards played: " + boardsPlayed
                + "\nAverage score (per board): " + fmt.format((double)score / boardsPlayed) + "\n");
        board.paintImmediately(new Rectangle(530, 700));
    }

    //appends text to the action log
    void appendText(String text)
    {
        actionLog.append(text + "\n");
        actionLog.setCaretPosition(actionLog.getDocument().getLength());
    }

    //returns score
    int getScore()
    {
        return score;
    }

    //moves the agent to the starting square when it begins a new game board
    void newBoard()
    {
        boardsPlayed++;
        arrow = true;
        end = false;
        currentSquare = 0;
        brain = new Brain(BOARD_SIZE, squares);
        brain.enterSquare(currentSquare);
        currentDirection = Direction.NORTH;
    }

    //pauses the game so that the agent moves
    void paintAndWait()
    {
        board.paintImmediately(new Rectangle(530, 700));
        try {
            Thread.sleep(300);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}