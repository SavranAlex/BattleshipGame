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
            if(!potentialGuess.isEmpty())
            {
                do
                {
                    index = random.nextInt(potentialGuess.size());
                    newGuess = potentialGuess.get(index);
                    if(isValidShot(newGuess))
                    {
                        potentialGuess.remove(index);
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
                        return newGuess;
                    }
                } while (validShot == false);
            }
        }
        return newGuess;
    } // end of makeGuess()


    @Override
    public void update(Guess guess, Answer answer) {

        previousGuesses.push(guess);

        if(answer.isHit == true)
        {
            hits.push(guess);
            targetMode = true;
            addAdjacentGuesses(guess);
        }

        if(targetMode == true)
        {
            if(potentialGuess.isEmpty()) //In target mode but have no other potential guesses
            {
                targetMode = false;
            }
            else if(answer.isHit == false)
            {
                hits = reverseStack(hits);
            }
        }
    } // end of update()


    @Override
    public boolean noRemainingShips() {
        return remainingShips.isEmpty();
    } // end of noRemainingShips()

    public void addAdjacentGuesses(Guess guess)
    {
        Guess north = new Guess();
        north.column = guess.column;
        north.row = guess.row+1;
        
        Guess south = new Guess();
        south.column = guess.column;
        south.row = guess.row-1;
        
        Guess west = new Guess();
        west.column = guess.column-1;
        west.row = guess.row;
        
        Guess east = new Guess();
        east.column = guess.column+1;
        east.row = guess.row;
        
        
        if(isValidShot(west) && isValidPotentialGuess(west)) potentialGuess.add(west);
        if(isValidShot(south) && isValidPotentialGuess(south)) potentialGuess.add(south);
        if(isValidShot(north) && isValidPotentialGuess(north)) potentialGuess.add(north);
        if(isValidShot(east) && isValidPotentialGuess(east)) potentialGuess.add(east);
    }

    private Stack<Guess> reverseStack(Stack<Guess> stack)
    {
        Stack<Guess> temp = new Stack<Guess>();

        while(!stack.isEmpty())
        {
            temp.push(stack.pop());
        }
        return temp;
    }

    public boolean isValidPotentialGuess(Guess guess)
    {
        Guess test = new Guess();
        Iterator<Guess> iter = potentialGuess.iterator();

        while(iter.hasNext())
        {
            test = iter.next();
            if(test.row == guess.row && test.column == guess.column)
            {
                return false;
            }
        }
        return true;
    }

    public boolean isValidShot(Guess guess)
    {
        Guess test = new Guess();
        Iterator<Guess> iter = previousGuesses.iterator();

        if(guess.column < 0 || guess.column >= world.numColumn || guess.row < 0 || guess.row >= world.numRow)
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
