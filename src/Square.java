// Square.java
// Zack Waterson
//
// This class keeps track of many booleans and the location of the square on the board

public class Square
{
    //a list of booleans to track the board state
    private boolean wumpus, pit, stench, breeze, gold, visited, visible;
    //two ints to track location on the board
    private final int x, y;
    //ints that keep track of where icons should be put on the square
    private final int agentIconLoc, mainIconLoc, stenchIconLoc, glitterIconLoc;

    public Square(int xCoord, int yCoord)
    {
        x = xCoord;
        y = yCoord;
        //this is how far off the side these should be
        //(with a margin of 1 added to the breeze, glitter, and stench icons)
        // breeze doesn't need one because it will just be: (x + 1, y + 1)
        agentIconLoc = 35;  //(x + agentIconLoc, y + agentIconLoc)
        mainIconLoc = 17;   //(x + mainIconLoc, y + mainIconLoc)
        stenchIconLoc = 79; //(x + stenchIconLoc, y + 1)
        glitterIconLoc = 71; //(x + 1, y + glitterIconLoc)

        //all the booleans start false
        wumpus = false;
        pit = false;
        stench = false;
        breeze = false;
        gold = false;
        visited = false;
        visible = false;
    }

    //returns the coordinates
    int getX()
    {
        return x;
    }

    int getY()
    {
        return y;
    }

    //returns the icon placement constants
    int getAgentIconLoc()
    {
        return agentIconLoc;
    }

    int getMainIconLoc()
    {
        return mainIconLoc;
    }

    int getStenchIconLoc()
    {
        return stenchIconLoc;
    }

    int getGlitterIconLoc()
    {
        return glitterIconLoc;
    }

    //these functions simply return true or false for each boolean
    boolean checkWumpus()
    {
        return wumpus;
    }

    boolean checkPit()
    {
        return pit;
    }

    boolean checkStench()
    {
        return stench;
    }

    boolean checkBreeze()
    {
        return breeze;
    }

    boolean checkGold()
    {
        return gold;
    }

    boolean checkVisited()
    {
        return visited;
    }

    boolean checkVisible()
    {
        return visible;
    }

    //all these functions simply toggle or set the booleans
    //the ones that toggle mean we might change that later
    //the ones that don't won't change once set
    //until we have a new board
    void toggleWumpus()
    {
        wumpus = !wumpus;
    }

    void toggleStench()
    {
        stench = !stench;
    }

    void toggleGold()
    {
        gold = !gold;
    }

    void setPit()
    {
        pit = true;
    }

    void setBreeze()
    {
        breeze = true;
    }

    void setVisited()
    {
        visited = true;
        setVisible();
    }

    void setVisible()
    {
        visible = true;
    }

    //used when generating pits to remove the stench from the square if the wumpus is adjacent and same with breezes and adjacent pits
    //removeStench is also used if the wumpus is shot
    void removeStench()
    {
        stench = false;
    }

    void removeBreeze()
    {
        breeze = false;
    }
}