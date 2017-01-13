# Wumpus-World
This is a Wumpus World Agent written in Java.
Final Project for Intelligent Systems class.

Most of the classes are just the framework for the program, and generate and manage the board and UI. Agent.java is the agent that plays the game and Brain.java is the knowledge base. Neither of those classes do anything cheaty (like check if a square has a pit in it before moving to it.)

The main decision making from the agent is a fairly simple decision tree that checks couple conditions (like whether we still have the arrow and know where the Wumpus is) and then does something based on that. Uses an A\* search to find paths around the board with a Manhattan Distance heuristic.

If there are any issues with the icons, they need to be in "/src/resource/"
