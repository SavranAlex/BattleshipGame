package player;

import ship.Ship;
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
    //create world
    World world;
    boolean targetMode;
    final int WEIGHT = 100;

    //create ship memory
    ArrayList<World.ShipLocation> remainingShips = new ArrayList<>();
    ArrayList<Ship> oppShips = new ArrayList<>();

    //create guess memory
    ArrayList<Guess> remainingGuesses = new ArrayList<>();
    ArrayList<Guess> previousGuesses = new ArrayList<>();
    ArrayList<Guess> potentialGuess = new ArrayList<>();

    //Keep a list of the hits
    ArrayList<Guess> hits = new ArrayList<>();
    //Keep track of the density map for potential ship locations
    int[][] densityMap;
    int[][] targetMap;

    @Override
    public void initialisePlayer(World world) {
        this.world = world;
        targetMode = false;
        densityMap = new int[world.numColumn][world.numRow];
        targetMap = new int[world.numColumn][world.numRow];

        //insert ships into the local ship locations
        //Keep track of opponent ships still in play
        for (ShipLocation sLoc : world.shipLocations) {
            remainingShips.add(sLoc);
            oppShips.add(sLoc.ship);
        }

        //Generate an arraylist of each potential guess (also initialize that location in the densityMap 0)
        for(int column = 0; column < world.numColumn; column++)
        {
            for(int row = 0; row < world.numRow; row++)
            {
                Guess addGuess = new Guess();
                addGuess.row = row;
                addGuess.column = column;
                densityMap[column][row] = 0;
                targetMap[column][row] = 0;
                remainingGuesses.add(addGuess);
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
        if(targetMode)
        {
            return bestProbableGuess();
        }
        else
        {
            return bestGuess();
        }
    } // end of makeGuess()


    @Override
    public void update(Guess guess, Answer answer) {

        previousGuesses.add(guess);

        Iterator<Guess> iter = remainingGuesses.iterator();

        while(iter.hasNext())
        {
            Guess prev = iter.next();
            if(prev.column == guess.column && prev.row == guess.row)
            {
                iter.remove();
            }
        }

        if(answer.isHit == true){
            hits.add(guess);
            targetMode = true;
        }

        if(answer.shipSunk != null)
        {
            Iterator<Ship> itera = oppShips.iterator();
            Ship sh;
            while(itera.hasNext())
            {
                sh = itera.next();
                if(Objects.equals(sh.name(), answer.shipSunk.name()))
                {
                    itera.remove();
                }
            }
            
            if(hits.size() == answer.shipSunk.len()*answer.shipSunk.width())
            {
                hits.clear();
            }
        }

        if(targetMode)
        {
            createTargetMap();
            addPotentialGuesses();
            if(potentialGuess.isEmpty() || hits.isEmpty())
            {
                targetMode = false;
            }
        }

    } // end of update()


    @Override
    public boolean noRemainingShips() {
        return remainingShips.isEmpty();
    } // end of noRemainingShips()


    public void createMap()
    {
        //reset the map
        for(int column = 0; column < world.numColumn; column++)
        {
            for(int row = 0; row < world.numRow; row++)
            {
                densityMap[column][row] = 0;
            }
        }

        boolean canPlaceDown, canPlaceAcross;
        Guess tempGuess = new Guess();

        //for each remaining guess
        for (Guess guess : remainingGuesses) {
            //for each of the opponent ships
            for (Ship ship  : oppShips) {
                //check each spot that ship would take up to ensure it is a valid placement.
                //for down placement (row changes)
                canPlaceDown = true;
                canPlaceAcross = true;
                for(int i = 0; i < ship.len(); i++)
                {
                    for(int j = 0; j < ship.width(); j++)
                    {
                        //For Down Movement
                        tempGuess.column = guess.column + j;    //this specifies that the j movement is across columns (where j is width)
                        tempGuess.row = guess.row + i;       //so that the i movement (length) is downwards.
                        
                            /*
                            *   
                            */
                        if(!isWithinBorders(tempGuess) || isGuessed(tempGuess)) // If the shot is invalid and the location has not been hit
                        {
                            canPlaceDown = false;
                        }

                        //For Across Movement
                        tempGuess.column = guess.column + i;    //this specifies that the i movement (length) is across row
                        tempGuess.row = guess.row + j;       //so that the j movement is across columns

                        if(!isWithinBorders(tempGuess) || isGuessed(tempGuess))
                        {
                            canPlaceAcross = false;
                        }
                    }
                }
                //If it gets to here and canPlace is still true, increase counters

                if(canPlaceDown == true)
                {
                    for(int a = 0; a < ship.len(); a++)
                    {
                        for(int b = 0; b < ship.width(); b++)
                        {
                            densityMap[guess.column+b][guess.row+a]++;
                        }
                    }
                }
                if(canPlaceAcross == true)
                {
                    for(int c = 0; c < ship.len(); c++)
                    {
                        for(int d = 0; d < ship.width(); d++)
                        {
                            densityMap[guess.column+c][guess.row+d]++;
                        }
                    }
                }
            }
        }
        System.out.println("Density Map\n");
        printMap(densityMap);
    }

    public void createTargetMap()
    {
        //reset the targetmap
        for(int column = 0; column < world.numColumn; column++)
        {
            for(int row = 0; row < world.numRow; row++)
            {
                targetMap[column][row] = 0;
            }
        }

        //
        boolean canPlaceDown, canPlaceAcross, canPlaceUp, canPlaceLeft;
        Guess tempGuess = new Guess();

        for (Guess guess : remainingGuesses) {
            //create density map for ship locations
            for (Ship ship : oppShips) 
            {
                canPlaceAcross = true;
                canPlaceDown = true;
                canPlaceUp = true;
                canPlaceLeft = true;

                for(int i = 0; i < ship.len(); i++)
                {
                    for(int j = 0; j < ship.width(); j++)
                    {
                        //Down
                        tempGuess.column = guess.column + j;
                        tempGuess.row = guess.row + i;

                        if(!isWithinBorders(tempGuess) || (isGuessed(tempGuess) && !isHit(tempGuess)))
                        {
                            canPlaceDown = false;
                        }

                        //Across
                        tempGuess.column = guess.column + i;
                        tempGuess.row = guess.row + j;

                        if(!isWithinBorders(tempGuess) || (isGuessed(tempGuess) && !isHit(tempGuess)))
                        {
                            canPlaceAcross = false;
                        }

                        //Up
                        tempGuess.column = guess.column - j;
                        tempGuess.row = guess.row - i;

                        if (!isWithinBorders(tempGuess) || (isGuessed(tempGuess) && !isHit(tempGuess))) {
                            canPlaceUp = false;
                        }

                        //Left
                        tempGuess.column = guess.column - i;
                        tempGuess.row = guess.row - j;

                        if (!isWithinBorders(tempGuess) || (isGuessed(tempGuess) && !isHit(tempGuess))) {
                            canPlaceLeft = false;
                        }
                    }
                }

                if(canPlaceDown == true)
                {
                    for(int a = 0; a < ship.len(); a++)
                    {
                        for(int b = 0; b < ship.width(); b++)
                        {
                            tempGuess.column = guess.column+b;
                            tempGuess.row = guess.row+a;
                            if(!isHit(tempGuess))
                            {
                                targetMap[tempGuess.column][tempGuess.row]++;
                            }
                        }
                    }
                }
                if(canPlaceAcross == true)
                {
                    for(int c = 0; c < ship.len(); c++)
                    {
                        for(int d = 0; d < ship.width(); d++)
                        {
                            tempGuess.column = guess.column+c;
                            tempGuess.row = guess.row+d;
                            if(!isHit(tempGuess))
                            {
                                targetMap[tempGuess.column][tempGuess.row]++;
                            }
                        }
                    }
                }
                if (canPlaceUp) {
                    for(int a = 0; a < ship.len(); a++)
                    {
                        for(int b = 0; b < ship.width(); b++)
                        {
                            tempGuess.column = guess.column-b;
                            tempGuess.row = guess.row - a;
                            if(!isHit(tempGuess))
                            {
                                targetMap[tempGuess.column][tempGuess.row]++;
                            }
                        }
                    }
                }
                if (canPlaceLeft) {
                    for(int c = 0; c < ship.len(); c++)
                    {
                        for(int d = 0; d < ship.width(); d++)
                        {
                            tempGuess.column = guess.column-c;
                            tempGuess.row = guess.row - d;
                            if(!isHit(tempGuess))
                            {
                                targetMap[tempGuess.column][tempGuess.row]++;
                            }
                        }
                    }
                }
            }
        }

        System.out.println("TargetMap\n");
        printMap(targetMap);
    }

    public void addPotentialGuesses()
    {
        potentialGuess.clear();
        for (Guess hitGuess : hits) {
            Guess potGuess = new Guess();

            potGuess.column = hitGuess.column + 1;
            potGuess.row = hitGuess.row;
            if(isWithinBorders(potGuess) && !isGuessed(potGuess)) 
            { 
                potentialGuess.add(potGuess); 
                targetMap[hitGuess.column + 1][hitGuess.row]*= WEIGHT;
            }

            potGuess.column = hitGuess.column - 1;
            potGuess.row = hitGuess.row;
            if(isWithinBorders(potGuess) && !isGuessed(potGuess)) { 
                potentialGuess.add(potGuess);
                targetMap[hitGuess.column - 1][hitGuess.row]*= WEIGHT;

            }

            potGuess.row = hitGuess.row + 1;
            potGuess.column = hitGuess.column;
            if(isWithinBorders(potGuess) && !isGuessed(potGuess)) { 
                potentialGuess.add(potGuess); 
                targetMap[hitGuess.column][hitGuess.row + 1]*= WEIGHT;
            }
            
            potGuess.row = hitGuess.row - 1;
            potGuess.column = hitGuess.column;
            if(isWithinBorders(potGuess) && !isGuessed(potGuess)) { 
                potentialGuess.add(potGuess); 
                targetMap[hitGuess.column][hitGuess.row - 1]*= WEIGHT;
            }
        }
    }

    public Guess bestGuess()
    {
        ArrayList<Guess> bestGuesses = new ArrayList<>();
        Guess guess = new Guess();
        Random random = new Random();
        int index;
        int count = 1;
        createMap();

        for(int i = 0; i < world.numColumn; i++) 
        {
            for(int j = 0; j < world.numRow; j++)
            {
                if(densityMap[i][j] > count)
                {
                    guess.row = j;
                    guess.column = i;
                    count = densityMap[i][j];
                    bestGuesses.clear();
                    bestGuesses.add(guess);
                }
                else if( densityMap[i][j] == count)
                {
                    guess.row = j;
                    guess.column = i;
                    bestGuesses.add(guess);
                }
            }
        }
        
        while(true)
        {
            if(bestGuesses.size() == 0)
            {
                index = random.nextInt(remainingGuesses.size());
                return remainingGuesses.get(index);
            }
            index = random.nextInt(bestGuesses.size());
            guess = bestGuesses.get(index);
            System.out.printf("Best Guess: Col: %d\tRow:%d with a count of %d\n The size of array is: %d\n", guess.column, guess.row, count, bestGuesses.size());
            
            if(!isGuessed(guess))
            {
                return guess;
            }
        }
    }

    public Guess bestProbableGuess()
    {
        ArrayList<Guess> bestGuesses = new ArrayList<>();
        Guess guess = new Guess();
        Random random = new Random();
        int index;
        int count = 1;
        createTargetMap();
        addPotentialGuesses();

        for(int i = 0; i < world.numColumn; i++) 
        {
            for(int j = 0; j < world.numRow; j++)
            {
                if(targetMap[i][j] > count)
                {
                    guess.row = j;
                    guess.column = i;
                    count = targetMap[i][j];
                    bestGuesses.clear();
                    bestGuesses.add(guess);
                }
                else if( targetMap[i][j] == count)
                {
                    guess.row = j;
                    guess.column = i;
                    bestGuesses.add(guess);
                }
            }
        }

        if(bestGuesses.size() == 0) 
        {
            return bestGuess();
        }
        index = random.nextInt(bestGuesses.size());
        guess = bestGuesses.get(index);
        System.out.printf("Best Guess: Col: %d\tRow:%d with a count of %d\n The size of array is: %d\n", guess.column, guess.row, count, bestGuesses.size());

        
        return guess;
    }

    public boolean isHit(Guess guess)
    {
        for (Guess prev : hits) {
            if(prev.row == guess.row && prev.column == guess.column)
            {
                return true;
            }
        }
        return false;
    }

    public void printMap(int[][] map)
    {
        for(int i = 0; i < world.numColumn; i++)
        {
            for(int j = 0; j < world.numRow; j++)
            {
                System.out.printf("%d\t", map[i][j]);
            }
            System.out.println("\n");
        }
    }

    public boolean isWithinBorders(Guess guess)
    {
        if(guess.column < 0 || guess.column >= world.numColumn || guess.row < 0 || guess.row >= world.numRow)
        {
            return false;
        }
        return true;
    }

    public boolean isGuessed(Guess guess)
    {
        for (Guess prevGuess : previousGuesses) 
        {
            if(prevGuess.column == guess.column && prevGuess.row == guess.row)
            {
                return true;
            }
        }

        return false;
    }
} // end of class ProbabilisticGuessPlayer
