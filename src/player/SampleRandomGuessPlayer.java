package player;

import java.util.Random;
import ship.Ship;
import world.World;
import world.World.Coordinate;
import world.World.ShipLocation;

public class SampleRandomGuessPlayer implements Player
{
  static final int numShips = 5;
  static final int maxLen = 5;
  
  public SampleRandomGuessPlayer() {}
  
  static final int[] rowDeltas = { 1, 0, -1, 0, 1, 0, -1, 0 };
  static final int[] clnDeltas = { 0, -1, 0, 1, 1, 0, -1, 0 };
  
  int rowSize = 0;
  int clnSize = 0;
  boolean isHex = false;
  
  private class OwnShip { private OwnShip() {}
    Ship ship = null;
    int[] rowCdns = { -1, -1, -1, -1, -1 };
    int[] clnCdns = { -1, -1, -1, -1, -1 };
    boolean[] isdown = { true, true, true, true, true };
    
     }
  OwnShip[] ownShips = new OwnShip[5];
  boolean[][] isguessed;
  
  public void initialisePlayer(World paramWorld) {
    rowSize = numRow;
    clnSize = numColumn;
    isguessed = new boolean[rowSize][clnSize + (rowSize + 1) / 2];
    int i = 0;
    for (World.ShipLocation localShipLocation : shipLocations) {
      ownShips[i] = new OwnShip(null);
      ownShips[i].ship = ship;
      for (int j = 0; j < ownShips[i].ship.len(); j++) {
        ownShips[i].rowCdns[j] = coordinates.get(j).row;
        ownShips[i].clnCdns[j] = coordinates.get(j).column;
        ownShips[i].isdown[j] = false;
      }
      i++;
    }
  }
  

  public Answer getAnswer(Guess paramGuess)
  {
    Answer localAnswer = new Answer();
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < ownShips[i].ship.len(); j++) {
        if ((row == ownShips[i].rowCdns[j]) && (column == ownShips[i].clnCdns[j])) {
          isHit = true;
          ownShips[i].isdown[j] = true;
          int k = 1;
          for (int m = 0; m < ownShips[i].ship.len(); m++) {
            if (ownShips[i].isdown[m] == false) k = 0;
          }
          if (k != 0) {
            shipSunk = ownShips[i].ship;
          }
          return localAnswer;
        }
      }
    }
    return localAnswer;
  }
  



  public Guess makeGuess()
  {
    Random localRandom = new Random();
    int i;
    int j; 
    do { 
        i = localRandom.nextInt(rowSize);
        j = localRandom.nextInt(clnSize);
        if (isHex) {
          j += (i + 1) / 2;
        }
    } while (isguessed[i][j] != false);
    Guess localGuess = new Guess();
    row = i;
    column = j;
    isguessed[i][j] = 1;
    return localGuess;
  }
  

  public void update(Guess paramGuess, Answer paramAnswer) {}
  

  public boolean noRemainingShips()
  {
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < ownShips[i].ship.len(); j++) {
        if (ownShips[i].isdown[j] == false)
          return false;
      }
    }
    return true;
  }
}
