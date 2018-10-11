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

    //create ship memory
    ArrayList<World.ShipLocation> remainingShips = new ArrayList<>();
    ArrayList<Ship> oppShips = new ArrayList<>();

    //create guess memory
    ArrayList<Guess> remainingGuesses = new ArrayList<>();
    Stack<Guess> previousGuesses = new Stack<>();

    //Keep a stack of the hits
    Stack<Guess> hits = new Stack<>();
    //Keep track of the density map for potential ship locations
    int[][] densityMap;

    @Override
    public void initialisePlayer(World world) {
        this.world = world;
        densityMap = new int[world.numColumn][world.numRow];
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
                remainingGuesses.add(addGuess);
            }
        }

    } // end of initialisePlayer()

    @Override
    public Answer getAnswer(Guess guess) {
        Answer answer = new Answer();
        //for each ship location, check each coordinate the ship contains
        //to see if it matches the guess.

        for(ShipLocation possibleShip : remainingShips)
        {
            for (Coordinate coords : possibleShip.coordinates) {
                if(guess.column == coords.column && guess.row == coords.row)
                {
                    answer.isHit = true; //HIT!
                    possibleShip.coordinates.remove(coords); // remove the coord from the ship (remaining hits for this particular ship)

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
        Guess guess = new Guess();
        while(true)
        {
            guess = bestGuess();
            if(isWithinBorders(guess)&& !isGuessed(guess))
            {
                return guess;
            }
        }
    } // end of makeGuess()


    @Override
    public void update(Guess guess, Answer answer) {
        previousGuesses.push(guess);
        remainingGuesses.remove(guess);
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
                        if(!isWithinBorders(tempGuess)) // If the shot is invalid and the location has not been hit
                        {
                            canPlaceDown = false;
                        }

                        //For Across Movement
                        tempGuess.column = guess.column + i;    //this specifies that the i movement (length) is across row
                        tempGuess.row = guess.row + j;       //so that the j movement is across columns

                        if(!isWithinBorders(tempGuess))
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
                guess.row = j;
                guess.column = i;
                if(densityMap[i][j] > count)
                {
                    count = densityMap[i][j];
                    bestGuesses.clear();
                    bestGuesses.add(guess);
                }
                else if( densityMap[i][j] == count)
                {
                    bestGuesses.add(guess);
                }
            }
        }
        
        index = random.nextInt(bestGuesses.size());
        guess = bestGuesses.get(index);
        System.out.printf("Best Guess: Col: %d\tRow:%d\n", guess.column, guess.row);

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
