package player;

import java.util.Scanner;

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
    ArrayList<Ship> oppShips = new Arraylist<>();

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

        //insert ships into the local ship locations
        for(int i = 0; i < world.shipLocations.size(); i++)
        {
            remainingShips.add(world.shipLocations.get(i));
        }

        //Keep track of opponent ships still in play
        for (ShipLocation sLoc : world.shipLocations) {
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
        // To be implemented.

        // dummy return
        return null;
    } // end of makeGuess()


    @Override
    public void update(Guess guess, Answer answer) {
        // To be implemented.
    } // end of update()


    @Override
    public boolean noRemainingShips() {
        return remainingShips.isEmpty();
    } // end of noRemainingShips()


    public int[][] createMap()
    {
        //reset the map
        for(int column = 0; column < world.numColumn; column++)
        {
            for(int row = 0; row < world.numRow; row++)
            {
                densityMap[column][row] = 0;
            }
        }

        boolean canPlace = true;
        Guess tempGuess = new Guess();
        //for each remaining guess
        for (Guess guess : remainingGuesses) {
            //for each of the opponent ships
            for (Ship ship  : oppShips) {
                //check each spot that ship would take up to ensure it is a valid placement.
                //for down placement (row changes)
                for(int i = 0; i < ship.len(); i++)
                {
                    for(int j = 0; j < ship.width(); j++)
                    {
                        //For Down Movement
                        tempGuess = guess;
                        tempGuess.column+=j;    //this specifies that the j movement is across columns (where j is width)
                        tempGuess.row+=i;       //so that the i movement (length) is downwards.
                        
                            //If the shot is valid OR if that guess has previously been hit
                            //  (because if a previous guess has been hit, we still need to do the calculation
                            //  on the other potential locations.
                            //  Validshot will be false because it is a previous guess
                            //  and we only want to increase the count if its not a previous guess)
                        if(isValidShot(tempGuess) || isHit(tempGuess))
                        {
                            if(!isHit(tempGuess))
                            {
                                densityMap[tempGuess.column][tempGuess.row]++;
                            }
                        }

                        //For Across Movement
                        tempGuess = guess;
                        tempGuess.column+=i;    //this specifies that the i movement (length) is across
                        tempGuess.row+=j;       //so that the j movement is across columns

                        if(isValidShot(tempGuess) || isHit(tempGuess))
                        {
                            if(!isHit(tempGuess))
                            {
                                densityMap[tempGuess.column][tempGuess.row]++;
                            }
                        }
                    }
                }
            }
        }
    }

    public Guess bestGuess()
    {
        Guess bestGuess = new Guess();
        int count = 0;

        for(int row = 0; row < world.numRow; row++)
        {
            for(int column = 0; column < world.numColumn; column++)
            {
                if(densityMap[row][column] > count)
                {
                    count = densityMap[row][column];
                    bestGuess.column = column;
                    bestGuess.row = row;
                }
            }
        }
        return bestGuess;
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

    public boolean isValidShot(Guess guess)
    {
        if(guess.column < 0 || guess.column >= world.numColumn || guess.row < 0 || guess.row >= world.numRow)
        {
            return false;
        }

        for (Guess prevGuess : previousGuesses) 
        {
            if(prevGuess.column == guess.column && prevGuess.row == guess.row)
            {
                return false;
            }
        }

        return true;
    }
} // end of class ProbabilisticGuessPlayer
