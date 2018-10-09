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
    boolean swapped = false;


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
            lastHit = hits.pop();

            //if hits has >1 hits
            if(!hits.isEmpty())
            {
                previousHit = hits.peek();
                do
                {
                    if(previousHit.column > lastHit.column) //Last shot was west
                    {
                        newGuess.column = lastHit.column-1;
                        newGuess.row = lastHit.row;
                    }
                    else if(previousHit.column < lastHit.column) //Last shot was east
                    {
                        newGuess.column = lastHit.column+1;
                        newGuess.row = lastHit.row;
                    }
                    else if(previousHit.row > lastHit.row) //Last shot was north
                    {
                        newGuess.row = lastHit.row -1;
                        newGuess.column = lastHit.column;
                    }
                    else if(previousHit.row < lastHit.row)//Last shot was south
                    {
                        newGuess.row = lastHit.row +1;
                        newGuess.column = lastHit.column;
                    }

                    if(isValidShot(newGuess))
                    {
                        return newGuess;
                    }
                } while(validShot == false);
            }
            else
            {
                //generate random direction, check for valid shot, shoot
                do
                {
                    int direction = random.nextInt(4);
                    
                    switch(direction)
                    {
                        case 0: 
                        {
                            newGuess.column = lastHit.column + 1;
                            newGuess.row = lastHit.row;
                            break;
                        }
                        case 1:
                        {
                            newGuess.column = lastHit.column - 1;
                            newGuess.row = lastHit.row;
                            break;
                        }
                        case 2:
                        {
                            newGuess.row = lastHit.row + 1;
                            newGuess.column = lastHit.column;
                            break;
                        }
                        case 3:
                        {
                            newGuess.row = lastHit.row - 1;
                            newGuess.column = lastHit.column;
                            break;
                        }
                    }
                    if(isValidShot(newGuess)) 
                    {
                        return newGuess;
                    }
                } while(validShot == false);
            }
            
        }
        else //Random parityGuesses, hunting mode
        {
            if(parityGuesses.size() != 0)
            {
                do
                {
                    index = random.nextInt(parityGuesses.size());
                    newGuess = parityGuesses.get(index);
                    if(isValidShot(newGuess))
                    {
                        parityGuesses.remove(index);
                        previousGuesses.push(newGuess);
                        return newGuess;
                    }
                } while (validShot == false);
            }
        }
        return null;
    } // end of makeGuess()


    @Override
    public void update(Guess guess, Answer answer) {

        boolean swapped = false;

        previousGuesses.push(guess);

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
            else if(answer.isHit == false)
            {
                hits = reverseStack(hits);
                swapped = true;
            }
        }
        else
        {
            if(answer.isHit == true)
            {
                hits.push(guess);
                targetMode = true;
                swapped = false;


            }
        }
    } // end of update()


    @Override
    public boolean noRemainingShips() {
        return remainingShips.isEmpty();
    } // end of noRemainingShips()

    private Stack<Guess> reverseStack(Stack<Guess> stack)
    {
        Stack<Guess> temp = new Stack<Guess>();

        while(!stack.isEmpty())
        {
            temp.push(stack.pop());
        }
        return temp;
    }

    public boolean isValidShot(Guess guess)
    {
        Guess test = new Guess();
        Iterator<Guess> iter = previousGuesses.iterator();

        if(guess.column < 0 || guess.column > world.numColumn || guess.row < 0 || guess.row > world.numRow)
        {
            return false;
        }
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
