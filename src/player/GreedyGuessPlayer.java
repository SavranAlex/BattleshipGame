package player;

import java.util.ArrayList;
import java.util.Stack;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import world.World;
import world.World.Coordinate;
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
    Stack<Guess> previousGuesses = new Stack<>();
    Stack<Guess> hits = new Stack<>();
    Guess lastHit = new Guess();
    Guess previousHit = new Guess();
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
        Coordinate coords;
        //for each ship location, check each coordinate the ship contains
        //to see if it matches the guess.

        if(guess == null) return null; //remove in future

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
        
        Guess newGuess = new Guess();
        Random random = new Random();
        int index;
        boolean validShot = false;

        if(targetMode == true)
        {
            if(!hits.empty())
            {
                lastHit = hits.pop();
                if(!hits.empty())
                {
                    previousHit = hits.pop();
                    while(!validShot)
                    {
                        newGuess = lastHit;
                        if(previousHit.column > lastHit.column)
                        {
                            newGuess.column-=1;
                        }
                        else if(previousHit.column < lastHit.column)
                        {
                            newGuess.column+=1;
                        }
                        else if(previousHit.row > lastHit.row)
                        {
                            newGuess.row-=1;
                        }
                        else if(previousHit.row < lastHit.row)
                        {
                            newGuess.row+=1;
                        }

                        if(isValidShot(newGuess))
                        {
                            validShot = true;
                            return newGuess;
                        }
                    }
                }
                else
                {
                    newGuess = lastHit;
                    //generate random direction, check for valid shot, shoot
                    while(validShot == false)
                    {
                        int direction = random.nextInt(4);
                        
                        switch(direction)
                        {
                            case 0: 
                            {
                                newGuess.column+=1;
                                break;
                            }
                            case 1:
                            {
                                newGuess.column-=1;
                                break;
                            }
                            case 2:
                            {
                                newGuess.row+=1;
                                break;
                            }
                            case 3:
                            {
                                newGuess.row-=1;
                                break;
                            }
                        }
                        if(isValidShot(newGuess)) 
                        {
                            validShot = true;
                            return newGuess;
                        }
                    }
                }
            }
        }
        else //Random parityGuesses, hunting mode
        {
            if(parityGuesses.size() != 0)
            {
                index = random.nextInt(parityGuesses.size());
                newGuess = parityGuesses.get(index);
                if(isValidShot(newGuess))
                {
                    parityGuesses.remove(index);
                    previousGuesses.push(newGuess);
                }
            }
            return newGuess;
        }
        return null;
    } // end of makeGuess()


    @Override
    public void update(Guess guess, Answer answer) {

        previousGuesses.push(guess);

        //If ship is hit but not sunk, enter targetting mode
        if(targetMode == true)
        {
            if(answer.isHit == true && answer.shipSunk == null) //Ship hit but not sunk
            {
                hits.push(guess);
            }
            else if(answer.isHit == true && answer.shipSunk != null) //ship hit and sunk
            {
                hits.push(guess);
                targetMode = false;
            }
        }
        else
        {
            if(answer.isHit == true)
            {
                hits.push(guess);
                targetMode = true;
            }
        }
    } // end of update()


    @Override
    public boolean noRemainingShips() {
        return remainingShips.isEmpty();
    } // end of noRemainingShips()

    public boolean isValidShot(Guess guess)
    {
        Guess test = new Guess();
        Iterator<Guess> iter = previousGuesses.iterator();

        while(iter.hasNext())
        {
            test = iter.next();
            if(test.column == guess.column && test.row == guess.row)
            {
                return false;
            }
        }

        return true;
    }
} // end of class GreedyGuessPlayer
