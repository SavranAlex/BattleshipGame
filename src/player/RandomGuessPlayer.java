package player;

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

    private int col;
    private int raw;
    private ArrayList<World.ShipLocation> shipsLocations;
    private ArrayList<ShipLocation> remainingShips;
    //private ArrayList<World.Coordinate> shots;
    //private HashMap<Integer, Pair> shots;
    private TreeMap<Integer, Pair> shots;
    private int k;
    private Coordinate coor;
    private ShipLocation targetedShipLocation;
    //private Answer answer;
    //private Guess guess;
    private  ArrayList<Coordinate> hittedCoors;

//    public int pickShotCell() {
//
//    }


    public RandomGuessPlayer() {
    }

    @Override
    public void initialisePlayer(World world) {
        this.shipsLocations = world.shipLocations;
        this.remainingShips = world.shipLocations;
        hittedCoors = new ArrayList<>();
        this.col = world.numColumn;
        this.raw = world.numRow;
        //this.shots = new HashMap<>();
        this.shots = new TreeMap<>();
        k = 0;
        for (int i = 0; i < col; i++) {
            for (int j = 0; j < raw; j++) {
                Pair pair = new Pair(i,j);
                shots.put(k, pair);
                k++;
            }
        }
        System.out.println("map size: " + shots.size() + "ks" + k);

    } // end of initialisePlayer()

    @Override
    public Answer getAnswer(Guess guess) {
        //System.out.println(guess.toString());
        Answer answer = new Answer();
        coor = new Coordinate();
        coor.column = guess.column;
        coor.row = guess.row;
        answer.isHit = false;
        //targetedShipLocation.coordinates.add(coor);
      for (ShipLocation possibleShip : shipsLocations) {
          if (possibleShip.coordinates.contains(coor)) {
              answer.isHit = true;
              hittedCoors.add(coor);
          }
          if (possibleShip.coordinates.containsAll(hittedCoors)) {
              answer.shipSunk = possibleShip.ship;
              remainingShips.remove(possibleShip.ship);
          }
      }
        return answer;
    } // end of getAnswer()


    @Override
    public Guess makeGuess() {
        Guess guess = new Guess();
        Random rnd = new Random();
        int key = rnd.nextInt(k);
        Pair shot = shots.get(key);
        guess.column =
                shot.col;
        guess.row =
                shot.raw;
        shots.remove(key);
        k--;
        System.out.println(guess.toString());
        return guess;
    } // end of makeGuess()


    @Override
    public void update(Guess guess, Answer answer) {
//        guess = this.makeGuess();
//        answer = this.getAnswer();
    } // end of update()


    @Override
    public boolean noRemainingShips() {
        if (remainingShips.isEmpty()) {
            return true;
        } else {
            return false;
        }
    } // end of noRemainingShips()

} // end of class RandomGuessPlayer
