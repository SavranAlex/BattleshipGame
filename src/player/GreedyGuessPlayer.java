package player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import world.World;
import world.World.ShipLocation;

/**
 * Greedy guess player (task B).
 * Please implement this class.
 *
 * @author Youhan Xia, Jeffrey Chan
 */
public class GreedyGuessPlayer  implements Player{

    World world;
    ArrayList<World.ShipLocation> remainingShips = new ArrayList<>();
    HashSet<Guess> guesses = new HashSet<>();
    ArrayList<Guess> hits = new ArrayList<>();
    boolean targetMode = false;


    @Override
    public void initialisePlayer(World world) {
        this.world = world;

        //insert ships into the local ship locations
        for(int i = 0; i < world.shipLocations.size(); i++)
        {
            remainingShips.add(world.shipLocations.get(i));
        }

        // generate a set of guesses that will rule out a bunch of future gesses
        // even rows/columns to rule out
        for (int row = 0; row < world.numRow; row = row + 2)
        {
            for (int col = 0; col < world.numColumn; col = col+2)
            {
                Guess addGuess = new Guess();
                addGuess.row = row;
                addGuess.column = col;

                guesses.add(addGuess);
            }
        }
        // odd rows/columns to rule out
        for(row = 1; row < world.numRow; row = row + 2)
        {
            for(col = 1; col < world.numColumn; col = col + 2)
            {
                Guess addGuess = new Guess();
                addGuess.row = row;
                addGuess.column = col;

                guesses.add(addGuess);
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

            for(int x = 0; x < coords.size(); x++)
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
        // To be implemented.

        // dummy return
        return true;
    } // end of noRemainingShips()

} // end of class GreedyGuessPlayer
