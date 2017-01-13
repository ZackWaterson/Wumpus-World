// Brain.java
// Zack Waterson
//
// This is where the magic happens. Brain makes the decisions
// for the agent and keeps track of a lot of information

import java.util.ArrayList;

public class Brain
{
    final int BOARD_SIZE;
    Square[] squares;

    //a simple boolean and index used to hunt the wumpus
    private boolean foundWumpus, agentDeath;
    private int wumpusIndex;

    //the stench and breeze arrays track how many adjacent breezes and stenches there are
    int[] stenchArray, breezeArray;

    //this array of booleans simply keeps track of what we know to be safe
    boolean[] safe;
    //this whole class is really driven by these arrays
    //all the decision making will be based off these arrays and the squares array

    public Brain(int boardSize, Square[] sqrs)
    {
        BOARD_SIZE = boardSize;
        squares = sqrs;
        foundWumpus = false;
        agentDeath = false;
        //fill all the arrays with 0's or false
        int k = squares.length;
        stenchArray = new int[k];
        breezeArray = new int[k];
        safe = new boolean[k];
        for(int i = 0; i < k; i++)
        {
            stenchArray[i] = 0;
            breezeArray[i] = 0;
            safe[i] = false;
        }
    }

    //checks for wumpus or pits in a square when we enter it the first time and sets it to visited, visible, and safe if safe
    //if it has a breeze or stench then it ups some counters
    void enterSquare(int newLocation)
    {
        //new location is the index of the square we enter
        if(!squares[newLocation].checkVisited()) {
            squares[newLocation].setVisited();
            if (!squares[newLocation].checkPit() && !squares[newLocation].checkWumpus())
            {
                agentDeath = false;
                safe[newLocation] = true;
                stenchArray[newLocation] = 0;
                breezeArray[newLocation] = 0;
                //if either was true then mark adjacent squares with danger counters
                if (squares[newLocation].checkBreeze() || squares[newLocation].checkStench())
                {
                    //if there is a breeze mark up adjacent unknown breeze counters
                    if (squares[newLocation].checkBreeze())
                    {
                        markSquares(newLocation, breezeArray);
                    }
                    //if there is a stench mark adjacent unknown stench counters
                    if (squares[newLocation].checkStench() && !foundWumpus)
                    {
                        markSquares(newLocation, stenchArray);
                    }
                }
                //if it is safe then mark adjacents as safe
                else
                {
                    markSafe(newLocation);
                }
                huntPits();
                huntWumpus();
                checkSpecialCases();
            }
            else
            {
                agentDeath = true;
            }
        }
    }

    //marks squares with breeze or stench counters if it is unknown and an adjacent square has a breeze or stench
    void markSquares(int newLocation, int[] array)
    {
        // makes sure that we don't try to go north of the top edge
        if (checkNorth(newLocation))
        {
            //if the square is not visible, then it's counter for potential danger is upped
            //and the number of unknown adjacent squares next to the breeze square is upped
            if (!squares[newLocation + BOARD_SIZE].checkVisible() && !safe[newLocation + BOARD_SIZE])
            {
                array[newLocation + BOARD_SIZE]++;
            }
        }
        // checks to make sure we didn't wrap around to the other side of the board
        if (checkEast(newLocation))
        {
            if (!squares[newLocation + 1].checkVisible() && !safe[newLocation + 1])
            {
                array[newLocation + 1]++;
            }
        }
        // makes sure we don't try to go below the bottom edge
        if (checkSouth(newLocation))
        {
            if (!squares[newLocation - BOARD_SIZE].checkVisible() && !safe[newLocation - BOARD_SIZE])
            {
                array[newLocation - BOARD_SIZE]++;
            }
        }
        // makes sure we don't go below 0 or wrap around to the other side
        if (checkWest(newLocation))
        {
            if (!squares[newLocation - 1].checkVisible() && !safe[newLocation - 1])
            {
                array[newLocation - 1]++;
            }
        }
    }

    //if a square has no breeze or stench, then we mark all adjacent squares as safe
    void markSafe(int location)
    {
        // makes sure that we don't try to go north of the top edge
        if (checkNorth(location))
        {
            safe[location + BOARD_SIZE] = true;
            breezeArray[location + BOARD_SIZE] = 0;
            stenchArray[location + BOARD_SIZE] = 0;
        }
        // checks to make sure we didn't wrap around to the other side of the board
        if (checkEast(location))
        {
            safe[location + 1] = true;
            breezeArray[location + 1] = 0;
            stenchArray[location + 1] = 0;
        }
        // makes sure we don't try to go below the bottom edge
        if (checkSouth(location))
        {
            safe[location - BOARD_SIZE] = true;
            breezeArray[location - BOARD_SIZE] = 0;
            stenchArray[location - BOARD_SIZE] = 0;
        }
        // makes sure we don't go below 0 or wrap around to the other side
        if (checkWest(location))
        {
            safe[location - 1] = true;
            breezeArray[location - 1] = 0;
            stenchArray[location - 1] = 0;
        }
    }

    //basically if any one square ever has more Wumpus counters than any other then it is the wumpus
    //this function looks for that and reveals the wumpus if we know where it is
    void huntWumpus()
    {
        int max = 0, counter = 0, index = 0;
        for (int i = 0; i < squares.length; i++)
        {
            if (stenchArray[i] > max)
            {
                max = stenchArray[i];
                index = i;
            }
        }
        if (max > 0)
        {
            for (int i = 0; i < squares.length; i++)
            {
                if(stenchArray[i] == max)
                    counter++;
            }
        }
        //if the counter is one then the wumpus is definitely in index
        if(counter == 1)
        {
            foundWumpus = true;
            wumpusIndex = index;
            squares[index].setVisible();
            stenchArray[index] = 100;
        }
    }

    //this function is used when there are no safe squares and the wumpus is alive and we don't know where it is exactly
    //but we've seen a stench. rather than just go into the least dangerous square, this function tries to guess
    //the most likely location of the wumpus so the agent can go try to kill it
    //we do stenchArray - breezeArray so that the square will hopefully not have a pit on it
    int guessWumpus()
    {
        int max = -5, index = -1;
        for(int i = 0; i < squares.length; i++)
        {
            if(stenchArray[i] > 0)
            {
                if (stenchArray[i] - breezeArray[i] > max)
                {
                    max = stenchArray[i] - breezeArray[i];
                    index = i;
                }
            }
        }
        return index;
    }

    //used to see if we know where the wumpus is
    boolean checkConfirmedWumpus()
    {
        return foundWumpus;
    }

    //returns true if we've seen a stench, used to see if we can try to guess where the wumpus is
    boolean checkStenchArray()
    {
        for(int i = 0; i < squares.length; i++)
        {
            if(stenchArray[i] > 0)
                return true;
        }
        return false;
    }

    //returns the index of the wumpus if it is found
    int getWumpusIndex()
    {
        if(foundWumpus)
            return wumpusIndex;
        else
            return -1;
    }

    //clears the stenchArray if the wumpus is found
    void clearStenchArray()
    {
        for (int i = 0; i < squares.length; i++)
        {
            if(stenchArray[i] > 0 && i != wumpusIndex)
            {
                stenchArray[i] = 0;
                if (breezeArray[i] == 0)
                    safe[i] = true;
            }
        }
    }

    //called when the wumpus is shot
    void shootWumpus()
    {
        safe[wumpusIndex] = true;
        clearStenchArray();
    }

    //called when a shot at the wumpus fails to mark the square as not wumpus
    void failedShot(int location)
    {
        stenchArray[location] = 0;
        if (breezeArray[location] == 0)
            safe[location] = true;
    }

    //looks for confirmed pits
    void huntPits()
    {
        //we will count how many adjacent squares are potentially dangerous
        //and save the index of any that might be
        //then if adjacentCounters is 0 (there is only 1 potential danger)
        //then index is a pit and we can reveal it
        int adjacentDangers, index = 0;
        for (int i = 0; i < squares.length; i++)
        {
            adjacentDangers = 0;
            if (squares[i].checkBreeze() && squares[i].checkVisited()) {
                if (checkNorth(i)) {
                    if (!safe[i + BOARD_SIZE]) {
                        adjacentDangers++;
                        index = i + BOARD_SIZE;
                    }
                }
                if (checkEast(i)) {
                    if (!safe[i + 1]) {
                        adjacentDangers++;
                        index = i + 1;
                    }
                }
                if (checkSouth(i)) {
                    if (!safe[i - BOARD_SIZE]) {
                        adjacentDangers++;
                        index = i - BOARD_SIZE;
                    }
                }
                if (checkWest(i)) {
                    if (!safe[i - 1]) {
                        adjacentDangers++;
                        index = i - 1;
                    }
                }
                if (adjacentDangers == 1) {
                    squares[index].setVisible();
                    //we simply make it so that the confirmed pit is the last place it will go by making the array index = 1000
                    stenchArray[index] = 0;
                    breezeArray[index] = 100;
                }
            }
        }
    }

    //there is a special case where a square will have 1 on each counter and
    //if that square has more than 1 adjacent square that we've visited, it is safe
    //this is because if one has a stench and one has a breeze, the square between
    //cannot be a wumpus or a pit, otherwise one of those would have both a breeze and a stench
    void checkSpecialCases()
    {
        //used to track adjacent squares we've been to
        int counter;
        for (int i = 0; i < squares.length; i++)
        {
            counter = 0;
            if (stenchArray[i] == 1 && breezeArray[i] == 1)
            {
                if(checkNorth(i) && squares[i + BOARD_SIZE].checkVisited())
                {
                    counter++;
                }
                if(checkEast(i) && squares[i + 1].checkVisited())
                {
                    counter++;
                }
                if(checkSouth(i) && squares[i - BOARD_SIZE].checkVisited())
                {
                    counter++;
                }
                if(checkWest(i) && squares[i - 1].checkVisited())
                {
                    counter++;
                }
                //if counter > 1 we know this square is safe
                if (counter > 1)
                {
                    safe[i] = true;
                    stenchArray[i] = 0;
                    breezeArray[i] = 100;
                }
            }
        }

        //the other, smaller special case is if we fail to shoot the wumpus
        //and there is a square next to a stench with no breeze that we know is not the wumpus
        //but does not get marked safe
        //this marks that square safe
        for (int i = 0; i < squares.length; i++)
        {
            counter = 0;
            if ((stenchArray[i] + breezeArray[i]) == 0)
            {
                if(checkNorth(i) && squares[i + BOARD_SIZE].checkVisited())
                {
                    counter++;
                }
                if(checkEast(i) && squares[i + 1].checkVisited())
                {
                    counter++;
                }
                if(checkSouth(i) && squares[i - BOARD_SIZE].checkVisited())
                {
                    counter++;
                }
                if(checkWest(i) && squares[i - 1].checkVisited())
                {
                    counter++;
                }
                //if counter > 1 we know this square is safe
                if (counter > 0)
                {
                    safe[i] = true;
                }
            }
        }
    }

    //this function finds the space that is closest to the agent that is both unvisited and safe
    //if there are no safe, unvisited spaces, it will return -1
    int findNearestSafe(int agentLocation)
    {
        int closest = 100, index = -1, temp;
        for(int i = 0; i < squares.length; i++)
        {
            if(safe[i] && !squares[i].checkVisited())
            {
                temp = getStraightLine(agentLocation, i);
                if(temp < closest)
                {
                    closest = temp;
                    index = i;
                }
            }
        }
        return index;
    }

    //finds the least dangerous square if there is no other choice and goes there
    int findLeastDangerous(int agentLocation)
    {
        //initialize at 101 because pits and wumpus are set to 100
        //that way the agent will properly suicide if it has no other choice
        int lowest = 100, closest = 100, index = -1;
        for (int i = 0; i < squares.length; i++) {
            if (!safe[i] && breezeArray[i] + stenchArray[i] > 0 && breezeArray[i] + stenchArray[i] <= lowest && getStraightLine(agentLocation, i) <= closest)
            {

                closest = getStraightLine(agentLocation, i);
                lowest = breezeArray[i] + stenchArray[i];
                index = i;
            }
        }
        return index;
    }

    //returns the straight line distance from a square to a square
    int getStraightLine(int start, int end)
    {
        //index / BOARD_SIZE = row
        //index % BOARD_SIZE = column
        //|row1 - row2| + |column1 - column2| = distance
        return Math.abs((start / BOARD_SIZE) - (end / BOARD_SIZE)) + Math.abs((start % BOARD_SIZE) - (end % BOARD_SIZE));
    }

    boolean checkSafe(int index)
    {
        return safe[index];
    }

    //returns true if it's the first move and one space is safe
    boolean checkFirstMove()
    {
        int counter = 0;
        for(int i = 0; i < squares.length; i++)
        {
            if(safe[i])
                counter++;
        }
        return counter <= 1;
    }

    //returns true if the agent died
    boolean checkDeath()
    {
        return agentDeath;
    }

    //these four functions check to make sure we are within the bounds of the board
    boolean checkNorth(int location)
    {
        // makes sure that we don't try to go north of the top edge
        return location + BOARD_SIZE < squares.length;
    }

    boolean checkEast(int location)
    {
        // checks to make sure we didn't wrap around to the other side of the board
        return (location + 1) % BOARD_SIZE != 0;
    }

    boolean checkSouth(int location)
    {
        // makes sure we don't try to go below the bottom edge
        return location - BOARD_SIZE >= 0;
    }

    boolean checkWest(int location)
    {
        // makes sure we don't go below 0 or wrap around to the other side
        return (location - 1) >= 0 && (location - 1) % BOARD_SIZE != BOARD_SIZE - 1;
    }

    boolean boundCheck(int index)
    {
        return index >= 0 && index < squares.length;
    }
}