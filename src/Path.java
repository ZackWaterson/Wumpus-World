// Path.java
// Zack Waterson
//
// This class is used by the Brain class when performing an
// A* search for pathfinding. It stores the path as directions
// using a straight line distance. The path with the smallest
// total cost will be expanded until we reach our target
// The path gets you to a square that is adjacent to the goal
// then the main function can rotate as needed to move there

import java.util.ArrayList;

public class Path
{
    private final int BOARD_SIZE;
    private int startingSquare, traveledCost, straightLineCost;
    private ArrayList<Direction> path;

    //here because it demanded I initialise shortestPath in Brain
    public Path(int boardSize)
    {
        BOARD_SIZE = boardSize;
    }

    // in the constructor we take in the first direction we will travel and the first straight line heuristic
    public Path(int start, Direction startingDirection, int heuristic, int boardSize)
    {
        //initialize traveledCost to 1 because we will have only traveled one square at this point
        traveledCost = 1;
        straightLineCost = heuristic;
        startingSquare = start;
        path = new ArrayList<Direction>();
        path.add(startingDirection);
        BOARD_SIZE = boardSize;
    }

    //this constructor is used when copying a path to another path
    public Path(Path prevPath, int boardSize)
    {
        BOARD_SIZE = boardSize;
        startingSquare = prevPath.getStartingSquare();
        traveledCost = prevPath.getTraveledCost();
        straightLineCost = prevPath.getStraightLineCost();
        path = new ArrayList<Direction>();
        for(int i = 0; i < prevPath.getPathLength(); i++)
            path.add(prevPath.getNextDirection(i));
    }

    void addToPath(Direction newDirection, int heuristic)
    {
        path.add(newDirection);
        //each time we move it's just +1 on the traveled cost
        traveledCost++;
        straightLineCost = heuristic;
    }

    //while expanding path this returns the current square
    //that is being expanded from in the path
    int getCurrentSquare()
    {
        int index = startingSquare;
        for (Direction current : path)
        {
            switch(current)
            {
                case NORTH:
                    index += BOARD_SIZE;
                    break;
                case EAST:
                    index++;
                    break;
                case SOUTH:
                    index -= BOARD_SIZE;
                    break;
                case WEST:
                    index--;
            }

        }
        return index;
    }

    Direction getNextDirection(int index)
    {
        return path.get(index);
    }

    ArrayList<Direction> getPath()
    {
        return path;
    }

    int getTotalCost()
    {
        return traveledCost + straightLineCost;
    }

    int getStraightLineCost()
    {
        return straightLineCost;
    }

    int getTraveledCost()
    {
        return traveledCost;
    }

    int getStartingSquare()
    {
        return startingSquare;
    }

    int getPathLength()
    {
        return path.size();
    }

    public String toString()
    {
        String pathString = "";
        for(int i = 0; i < path.size(); i++)
        {
            pathString += path.get(i) + ", ";
        }
        return "Path: " + pathString;
    }
}