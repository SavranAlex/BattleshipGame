package player;

import java.awt.font.NumericShaper.Range;
import java.util.*;

import ship.Ship;
import world.World;
import world.World.Coordinate;
import world.World.ShipLocation;


/**
 * Random guess player (task A).
 * Please implement this class.
 *
 * @author Youhan Xia, Jeffrey Chan
 */
public class RandomGuessPlayer implements Player{

    World world;
    ArrayList<ShipLocation> remainingShips = new ArrayList<>();
    ArrayList<Guess> randomGuesses = new ArrayList<>();
    ArrayList<Guess> previousGuesses = new ArrayList<>();

    public RandomGuessPlayer() {
    }

    @Override
    public void initialisePlayer(World world) {
        this.world = world;

        //insert ships into the local ship locations
        for(int i = 0; i < world.shipLocations.size(); i++)
        {
            remainingShips.add(world.shipLocations.get(i));
        }

        //Generate a set of random guesses across the board

        for(int row = 0; row < world.numRow; row++)
        {
            for(int col = 0; col < world.numColumn; col++)
            {
                Guess addGuess = new Guess();
                addGuess.row = row;
                addGuess.column = col;

                randomGuesses.add(addGuess);
            }
        }
    } // end of initialisePlayer()

    @Override
    public Answer getAnswer(Guess guess) {
        Answer answer = new Answer();
        Coordinate coords;

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

        if(!randomGuesses.isEmpty())
        {
            do
            {
                index = random.nextInt(randomGuesses.size());
                newGuess = randomGuesses.get(index);
                if(isValidShot(newGuess))
                {
                    randomGuesses.remove(index);
                    return newGuess;
                }

            } while (!validShot);
        }

        return newGuess;
    } // end of makeGuess()


    @Override
    public void update(Guess guess, Answer answer) {

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
} // end of class RandomGuessPlayer
