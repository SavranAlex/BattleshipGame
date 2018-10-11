package player;

import ship.Ship;
import world.World;
import world.World.*;
import java.util.*;

/**
 * Probabilistic guess player (task C).
 * Please implement this class.
 *
 * @author Youhan Xia, Jeffrey Chan
 */
public class ProbabilisticGuessPlayer  implements Player{
    //create world
    World world;
    boolean targetMode;
    //create ship memory
    ArrayList<World.ShipLocation> remainingShips = new ArrayList<>();
    ArrayList<Ship> oppShips = new ArrayList<>();

    //create guess memory
    ArrayList<Guess> remainingGuesses = new ArrayList<>();
    Stack<Guess> previousGuesses = new Stack<>();
    ArrayList<Guess> potentialGuess = new ArrayList<>();

    //Keep a stack of the hits
    Stack<Guess> hits = new Stack<>();
    //Keep track of the density map for potential ship locations
    int[][] densityMap;
    int[][] targetMap;

    @Override
    public void initialisePlayer(World world) {
        this.world = world;
        targetMode = false;
        densityMap = new int[world.numColumn][world.numRow];
        targetMap = new int[world.numColumn][world.numRow];

        //insert ships into the local ship locations
        //Keep track of opponent ships still in play
        for (ShipLocation sLoc : world.shipLocations) {
            remainingShips.add(sLoc);
            oppShips.add(sLoc.ship);
        }

        //Generate an arraylist of each potential guess (also initialize that location in the densityMap 0)
        for(int column = 0; column < world.numColumn; column++)
        {
            for(int row = 0; row < world.numRow; row++)
            {
                Guess addGuess = new Guess();
                addGuess.row = row;
                addGuess.column = column;
                densityMap[column][row] = 0;
                targetMap[column][row] = 0;
                remainingGuesses.add(addGuess);
            }
        }

    } // end of initialisePlayer()

    @Override
    public Answer getAnswer(Guess guess) {
        Answer answer = new Answer();
        Coordinate coords;
        //for each ship location, check each coordinate the ship contains
        //to see if it matches the guess.

        for(ShipLocation possibleShip : remainingShips)
        {
            Iterator<Coordinate> iter = possibleShip.coordinates.iterator();
            while(iter.hasNext())
            {
                coords = iter.next();
                if(guess.column == coords.column && guess.row == coords.row)
                {
                    answer.isHit = true; //HIT!
                    iter.remove(); // remove the coord from the ship (remaining hits for this particular ship)

                    if(possibleShip.coordinates.isEmpty())
                    {
                        answer.shipSunk = possibleShip.ship; //set the shipsunk to the ship that was hit
                        this.remainingShips.remove(possibleShip); //remove this ship from our list of remaining ships
                        return answer;
                    }
                }
            }
        }

        return answer;
    } // end of getAnswer()


    @Override
    public Guess makeGuess() {
        Random random = new Random();
        int index;
        if(potentialGuess.isEmpty())
        {
            return bestGuess();
        }
        else
        {
            index = random.nextInt(potentialGuess.size());
            return potentialGuess.get(index);
        }
    } // end of makeGuess()


    @Override
    public void update(Guess guess, Answer answer) {
        previousGuesses.push(guess);
        remainingGuesses.remove(guess);
        
        if(answer.isHit == true){
            hits.push(guess);
            targetMode = true;
            addPotentialGuesses(guess);
        }

        if(targetMode)
        {
            //Target mode
            if(potentialGuess.isEmpty())
            {
                targetMode = false;
            }

        }

        
    } // end of update()


    @Override
    public boolean noRemainingShips() {
        return remainingShips.isEmpty();
    } // end of noRemainingShips()


    public void createMap()
    {
        //reset the map
        for(int column = 0; column < world.numColumn; column++)
        {
            for(int row = 0; row < world.numRow; row++)
            {
                densityMap[column][row] = 0;
            }
        }

        boolean canPlaceDown, canPlaceAcross;
        Guess tempGuess = new Guess();

        //for each remaining guess
        for (Guess guess : remainingGuesses) {
            //for each of the opponent ships
            for (Ship ship  : oppShips) {
                //check each spot that ship would take up to ensure it is a valid placement.
                //for down placement (row changes)
                canPlaceDown = true;
                canPlaceAcross = true;
                for(int i = 0; i < ship.len(); i++)
                {
                    for(int j = 0; j < ship.width(); j++)
                    {
                        //For Down Movement
                        tempGuess.column = guess.column + j;    //this specifies that the j movement is across columns (where j is width)
                        tempGuess.row = guess.row + i;       //so that the i movement (length) is downwards.
                        
                            /*
                            *   If the shot is invalid AND has not been hit,
                            *   the boat cannot be placed.
                            *   AND must be used because a hit is still valid but will return false if
                            *   it has been previously guessed and hit.
                            */
                        if(!isWithinBorders(tempGuess) || (isWithinBorders(tempGuess) && isGuessed(tempGuess) && !isHit(tempGuess))) // If the shot is invalid and the location has not been hit
                        {
                            canPlaceDown = false;
                        }

                        //For Across Movement
                        tempGuess.column = guess.column + i;    //this specifies that the i movement (length) is across row
                        tempGuess.row = guess.row + j;       //so that the j movement is across columns

                        if(!isWithinBorders(tempGuess) || (isWithinBorders(tempGuess) && isGuessed(tempGuess) && !isHit(tempGuess)))
                        {
                            canPlaceAcross = false;
                        }
                    }
                }
                //If it gets to here and canPlace is still true, increase counters

                if(canPlaceDown == true)
                {
                    for(int a = 0; a < ship.len(); a++)
                    {
                        for(int b = 0; b < ship.width(); b++)
                        {
                            densityMap[guess.column+b][guess.row+a]++;
                        }
                    }
                }
                if(canPlaceAcross == true)
                {
                    for(int c = 0; c < ship.len(); c++)
                    {
                        for(int d = 0; d < ship.width(); d++)
                        {
                            densityMap[guess.column+c][guess.row+d]++;
                        }
                    }
                }
            }
        }

        printMap();
    }

    // Creates a target map of potential surrounding boats and picks the most likely target.
    public void addPotentialGuesses(Guess guess)
    {
        Guess tempGuess = new Guess();
        boolean canPlaceAcross, canPlaceDown;
        //reset the target map
        for(int column = 0; column < world.numColumn; column++)
        {
            for(int row = 0; row < world.numRow; row++)
            {
                targetMap[column][row] = 0;
            }
        }

        //For every location
        for(int i = 0; i < world.numColumn; i++)
        {
            for(int j = 0; j < world.numRow; j++)
            {
                //For each ship
                for (Ship ship : oppShips) 
                {
                    canPlaceAcross = false;
                    canPlaceDown = false;
                    //If that location in the ship is a valid ship location
                    //AND that ship contains a square that has been hit(guess)
                    //then increment those squares to the targetmap
                    for(int length = 0; length < ship.len(); length++)
                    {
                        for(int width = 0; width < ship.width(); width++)
                        {
                            if(guess.column == i + length && guess.row == j + width)
                            { 
                                tempGuess.column = i + length;
                                tempGuess.row = j + width;
                                //If this ship location is invalid, 
                                if(isWithinBorders(tempGuess) && (!isGuessed(tempGuess) || (isGuessed(tempGuess) && isHit(tempGuess))))
                                {
                                    canPlaceAcross = true;
                                }

                                tempGuess.column = i + width;
                                tempGuess.row = j + length;
                                if(isWithinBorders(tempGuess) && (!isGuessed(tempGuess) || (isGuessed(tempGuess) && isHit(tempGuess))))
                                {
                                    canPlaceDown = true;
                                }
                            }
                        }
                    }

                    if(canPlaceAcross)
                    {
                        for(int c = 0; c < ship.len(); c++)
                        {
                            for(int d = 0; d < ship.width(); d++)
                            {
                                targetMap[i+c][i+d]++;
                            }
                        }
                    }
                    if(canPlaceDown)
                    {
                        for(int c = 0; c < ship.len(); c++)
                        {
                            for(int d = 0; d < ship.width(); d++)
                            {
                                targetMap[i+d][i+c]++;
                            }
                        }
                    }
                }
            }
        }

        //edit
        int count = 0;
        for(int i = 0; i < world.numColumn; i++) 
        {
            for(int j = 0; j < world.numRow; j++)
            {
                if(targetMap[i][j] > count)
                {
                    tempGuess.row = j;
                    tempGuess.column = i;
                    count = densityMap[i][j];
                    potentialGuess.clear();
                    potentialGuess.add(tempGuess);
                }
                else if( targetMap[i][j] == count)
                {
                    tempGuess.row = j;
                    tempGuess.column = i;
                    potentialGuess.add(tempGuess);
                }
            }
        }
    }

    public Guess bestGuess()
    {
        ArrayList<Guess> bestGuesses = new ArrayList<>();
        Guess guess = new Guess();
        Random random = new Random();
        int index;
        int count = 0;
        createMap();

        for(int i = 0; i < world.numColumn; i++) 
        {
            for(int j = 0; j < world.numRow; j++)
            {
                if(densityMap[i][j] > count)
                {
                    guess.row = j;
                    guess.column = i;
                    count = densityMap[i][j];
                    bestGuesses.clear();
                    bestGuesses.add(guess);
                }
                else if( densityMap[i][j] == count)
                {
                    guess.row = j;
                    guess.column = i;
                    bestGuesses.add(guess);
                }
            }
        }
        
        index = random.nextInt(bestGuesses.size());
        guess = bestGuesses.get(index);
        System.out.printf("Best Guess: Col: %d\tRow:%d with a count of %d\n The size of array is: %d\n", guess.column, guess.row, count, bestGuesses.size());

        return guess;
    }

    public boolean isHit(Guess guess)
    {
        for (Guess prev : hits) {
            if(prev.row == guess.row && prev.column == guess.column)
            {
                return false;
            }
        }
        return true;
    }
    public void printMap()
    {
        for(int i = 0; i < world.numColumn; i++)
        {
            for(int j = 0; j < world.numRow; j++)
            {
                System.out.printf("%d\t", densityMap[i][j]);
            }
            System.out.println("\n");
        }
    }

    public boolean isWithinBorders(Guess guess)
    {
        if(guess.column < 0 || guess.column >= world.numColumn || guess.row < 0 || guess.row >= world.numRow)
        {
            return false;
        }
        return true;
    }

    public boolean isGuessed(Guess guess)
    {
        for (Guess prevGuess : previousGuesses) 
        {
            if(prevGuess.column == guess.column && prevGuess.row == guess.row)
            {
                return true;
            }
        }

        return false;
    }
} // end of class ProbabilisticGuessPlayer
