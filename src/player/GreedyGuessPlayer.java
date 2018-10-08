package player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import world.World;
import world.World.ShipLocation;
import ship.*;

/**
 * Greedy guess player (task B).
 * Please implement this class.
 *
 * @author Youhan Xia, Jeffrey Chan
 */
public class GreedyGuessPlayer  implements Player{

    World world;
    ArrayList<World.ShipLocation> remainingShips = new ArrayList<>();
    ArrayList<Guess> parityGuesses = new ArrayList<>();
    ArrayList<Guess> potentialGuess = new ArrayList<>();
    ArrayList<Guess> previousGuesses = new ArrayList<>();
    ArrayList<Guess> hits = new ArrayList<>();
    Guess lastHit = new Guess();
    boolean targetMode = false;


    @Override
    public void initialisePlayer(World world) {
        this.world = world;

        //insert ships into the local ship locations
        for(int i = 0; i < world.shipLocations.size(); i++)
        {
            remainingShips.add(world.shipLocations.get(i));
        }

        // generate a set of parityGuesses that will rule out a bunch of future gesses
        // even rows/columns to rule out
        for (int row = 0; row < world.numRow; row = row + 2)
        {
            for (int col = 0; col < world.numColumn; col = col+2)
            {
                Guess addGuess = new Guess();
                addGuess.row = row;
                addGuess.column = col;

                parityGuesses.add(addGuess);
            }
        }
        // odd rows/columns to rule out
        for(int row = 1; row < world.numRow; row = row + 2)
        {
            for(int col = 1; col < world.numColumn; col = col + 2)
            {
                Guess addGuess = new Guess();
                addGuess.row = row;
                addGuess.column = col;

                parityGuesses.add(addGuess);
            }
        }
    } // end of initialisePlayer()

    @Override
    public Answer getAnswer(Guess guess) {
        Answer answer = new Answer();
        ArrayList<World.Coordinate> localShipCoords;
        World.Coordinate coords;
        Ship ship;

        //for each ship location, check each coordinate the ship contains
        //to see if it matches the guess.
        for(int i = 0; i < this.remainingShips.size(); i++)
        {
            localShipCoords = this.remainingShips.get(i).coordinates;
            ship = this.remainingShips.get(i).ship;

            for(int x = 0; x < localShipCoords.size(); x++)
            {
                coords = localShipCoords.get(x);

                if(coords.row == guess.row && coords.column == guess.column)
                {
                    answer.isHit = true; //HIT!
                    localShipCoords.remove(x); // remove the coord from the ship (remaining hits for this particular ship)

                    if(localShipCoords.isEmpty())
                    {
                        answer.shipSunk = ship; //set the shipsunk to the ship that was hit
                        this.remainingShips.remove(i); //remove this ship from our list of remaining ships
                        return answer;
                    }
                }
            }
        }

        return answer;
    } // end of getAnswer()


    @Override
    public Guess makeGuess() {
        
        Guess newGuess = new Guess();
        Random random = new Random();
        int index;

        if(targetMode == true)
        {
            
        }
        else //Random parityGuesses, hunting mode
        {
            if(parityGuesses.size() != 0)
            {
                index = random.nextInt(parityGuesses.size());
                newGuess = parityGuesses.get(index);
                parityGuesses.remove(index);
                previousGuesses.add(newGuess);
            }
            return newGuess;
        }
        return null;
    } // end of makeGuess()


    @Override
    public void update(Guess guess, Answer answer) {

        //If ship is hit but not sunk, enter targetting mode
        if(answer.isHit == true && answer.shipSunk == null)
        {
            targetMode = true;
            hits.add(guess);

            //Generate the guesses adjacent to the hit
            Guess north = guess;
            Guess east = guess;
            Guess south = guess;
            Guess west = guess;
            north.row-=1;
            east.column+=1;
            south.row+=1;
            west.column-=1;

            //If these locations have not previous been guessed, add to potential guess list
            if(!previousGuesses.contains(north)) 
                potentialGuess.add(north);
            if(!previousGuesses.contains(east))
                potentialGuess.add(east);
            if(!previousGuesses.contains(south))
                potentialGuess.add(south);
            if(!previousGuesses.contains(west))
                potentialGuess.add(west);
        }
        //If ship is hit and ship is sunk AND we have no more potential guesses, enter hunting mode
        else if (answer.isHit == true && answer.shipSunk != null)
        {
            if(potentialGuess.isEmpty())
            {
                targetMode = false;
            };
        }
        //Ship is not hit
        else
        {
            if(potentialGuess.contains(guess))
            {
                potentialGuess.remove(guess);
            }
        }
    } // end of update()


    @Override
    public boolean noRemainingShips() {
        return remainingShips.isEmpty();
    } // end of noRemainingShips()

} // end of class GreedyGuessPlayer
