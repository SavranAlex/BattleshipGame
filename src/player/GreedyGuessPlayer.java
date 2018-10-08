package player;

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

    int rowSize = 0;
    int colSize = 0;
    boolean[][] guessed;

    @Override
    public void initialisePlayer(World world) {
        rowSize = world.numRow;
        colSize = world.numColumn;
        guessed = new boolean[rowSize][colSize];
        int i = 0;
        for(World.ShipLocation localShipLocation : shipLocations)
        {
            
        }
    } // end of initialisePlayer()

    @Override
    public Answer getAnswer(Guess guess) {
        // To be implemented.

        // dummy return
        return null;
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
