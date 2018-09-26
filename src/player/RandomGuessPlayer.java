package player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import world.World;

/**
 * Random guess player (task A).
 * Please implement this class.
 *
 * @author Youhan Xia, Jeffrey Chan
 */
public class RandomGuessPlayer implements Player{

    private int col;
    private int raw;
    private ArrayList<World.ShipLocation> ships;
    //private ArrayList<World.Coordinate> shots;
    private HashMap<Integer, Pair> shots;
    private int k;


//    public int pickShotCell() {
//
//    }

    @Override
    public void initialisePlayer(World world) {
        this.ships = world.shipLocations;
        this.col = world.numColumn;
        this.raw = world.numRow;
        this.shots = new HashMap<>();
        k = 0;
        for (int i = 0; i < col; i++) {
            for (int j = 0; j < raw; j++) {
                Pair pair = new Pair(i,j);
                shots.put(k, pair);
                k++;
            }
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
        Random rnd = new Random();
        Guess guess = new Guess();
        int key = rnd.nextInt(k);
        Pair shot = shots.get(key);
        guess.column = shot.col;
        guess.row = shot.raw;
        shots.remove(key);
        k--;
        return guess;
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

} // end of class RandomGuessPlayer
