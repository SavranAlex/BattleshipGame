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
    //private HashMap<Integer, TargetedCell> shots;
    private TreeMap<Integer, TargetedCell> shots;
    private int k;
    private Coordinate coor;
    private ShipLocation targetedShipLocation;
    //private Answer answer;
    //private Guess guess;
    private  ArrayList<Coordinate> hittedCoors;
    private boolean shootStatus;
    //private int count;
    private ShipLocation removingShip;
    private boolean end;


//    public TargetedCell pickShotCell() {
//        Random rnd = new Random();
//        TargetedCell shootCell = shots.get(rnd.nextInt(k));
//        if (shootCell.shootStatus) {
//            return pickShotCell();
//        }
//        return shootCell;
//    }


    public RandomGuessPlayer() {
    }

    @Override
    public void initialisePlayer(World world) {
        System.out.println("initialize mehtod");
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
                shootStatus = false;
                TargetedCell cell = new TargetedCell(i,j, shootStatus);
                shots.put(k, cell);
                k++;
            }
        }
        //count = k;
        //System.out.println("map size: " + shots.size() + " ks " + k + " count " + count);

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
      for (ShipLocation possibleShip : remainingShips) {
          if (possibleShip.coordinates.contains(coor)) {
              answer.isHit = true;
              //hittedCoors.add(coor);
              possibleShip.coordinates.remove(coor);
          }
          if (possibleShip.coordinates.isEmpty()) {
          //if (hittedCoors.containsAll(possibleShip.coordinates)) {
              answer.shipSunk = possibleShip.ship;
              //remainingShips.remove(possibleShip);
              removingShip = possibleShip;
              //System.out.println("shots size " + shots.size() + " ship name: " + possibleShip.ship.name() + " removed? " + remainingShips.remove(possibleShip));
              System.out.println("ship destroyed" + removingShip.ship.name());
              System.out.println("counts: " + k + " ships remained: " + remainingShips.size());
          }
      }
        remainingShips.remove(removingShip);
        return answer;
    } // end of getAnswer()


    @Override
    public Guess makeGuess() {
        Guess guess = new Guess();
        Random rnd = new Random();
        int key = rnd.nextInt(100);
        System.out.println(key + " random key");
        TargetedCell shot = shots.get(key);
        while (shot.shootStatus) {
            shot = shots.get(rnd.nextInt(100));
            System.out.println(shot.shootStatus + " - shoot status " + key + " - key");
        }
        guess.column = shot.col;
        guess.row = shot.raw;
        shot.shootStatus = true;

        System.out.println("shot cell: " + shot.col + " " + shot.raw);

        //shots.remove(key);
        //k--;
        System.out.println(guess.toString());
        return guess;
    } // end of makeGuess()


    @Override
    public void update(Guess guess, Answer answer) {
//        guess = this.makeGuess();
//        answer = this.getAnswer();
        System.out.println("update method " + remainingShips.size());
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
