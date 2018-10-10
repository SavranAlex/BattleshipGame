package player;

import java.util.Scanner;
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

    World world;
    ArrayList<World.ShipLocation> remainingShips = new ArrayList<>();
    Stack<Guess> previousGuesses = new Stack<>();
    Stack<Guess> hits = new Stack<>();

    @Override
    public void initialisePlayer(World world) {
        this.world = world;

        //insert ships into the local ship locations
        for(int i = 0; i < world.shipLocations.size(); i++)
        {
            remainingShips.add(world.shipLocations.get(i));
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
} // end of class ProbabilisticGuessPlayer
