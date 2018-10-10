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
    Map<Guess,Integer> densityMap = new HashMap<>();

    @Override
    public void initialisePlayer(World wrld) {
        this.world = wrld;
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
                densityMap.put(addGuess, 0);
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
        Guess best = new Guess();
        createMap();
        best = bestGuess();
        return best;
    } // end of makeGuess()


    @Override
    public void update(Guess guess, Answer answer) {
        // To be implemented.
    } // end of update()


    @Override
    public boolean noRemainingShips() {
        return remainingShips.isEmpty();
    } // end of noRemainingShips()


    public void createMap()
    {
        //reset the map
        densityMap.clear();
        for(int column = 0; column < world.numColumn; column++)
        {
            for(int row = 0; row < world.numRow; row++)
            {
                Guess newGuess = new Guess();
                newGuess.column = column;
                newGuess.row = row;
                densityMap.put(newGuess, 0);
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
                        tempGuess = guess;
                        tempGuess.column+=j;    //this specifies that the j movement is across columns (where j is width)
                        tempGuess.row+=i;       //so that the i movement (length) is downwards.
                        
                            /*
                            *   If the shot is invalid AND has not been hit,
                            *   the boat cannot be placed.
                            *   AND must be used because a hit is still valid but will return false if
                            *   it has been previously guessed and hit.
                            */
                        if(!isValidShot(tempGuess) && !isHit(tempGuess)) // If the shot is invalid and the location has not been hit
                        {
                            canPlaceDown = false;
                        }

                        //For Across Movement
                        tempGuess = guess;
                        tempGuess.column+=i;    //this specifies that the i movement (length) is across row
                        tempGuess.row+=j;       //so that the j movement is across columns

                        if(!isValidShot(tempGuess) && !isHit(tempGuess))
                        {
                            canPlaceAcross = false;
                        }
                    }
                }
                //If it gets to here and canPlace is still true, increase counters

                if(canPlaceDown)
                {
                    for(int a = 0; a < ship.len(); a++)
                    {
                        for(int b = 0; b < ship.width(); b++)
                        {
                            tempGuess = guess;
                            tempGuess.column+=b;
                            tempGuess.row+=a;
                            //this part doesnt work- null pointer.
                            //not passing through correct reference to the guess in the map?
                            for (Guess entry : densityMap.keySet()) {
                                if(entry.column == tempGuess.column && entry.row == tempGuess.row)
                                {
                                    densityMap.put(entry, densityMap.get(entry)+1);
                                }
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
                            tempGuess = guess;
                            tempGuess.column+=c;
                            tempGuess.row+=d;
                            //this part doesnt work- null pointer.
                            //not passing through correct reference to the guess in the map?
                            for (Guess entry : densityMap.keySet()) {
                                if(entry.column == tempGuess.column && entry.row == tempGuess.row)
                                {
                                    densityMap.put(entry, densityMap.get(entry)+1);
                                }
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

        for (Map.Entry<Guess, Integer> entry : densityMap.entrySet()) {
            if(entry.getValue() > count)
            {
                count = entry.getValue();
                bestGuess = entry.getKey();
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
