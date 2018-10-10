package player;

import ship.Ship;
import world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;

public class ProbabilisticGuessPlayerV3 implements Player {
    ArrayList<World.Coordinate> allCoors;
    ArrayList<World.Coordinate> hittedCoors = new ArrayList<>();
    ArrayList<World.Coordinate> airCrafterPossibleCoors;
    ArrayList<World.Coordinate> patrolCraftPossibleCoors;
    ArrayList<World.Coordinate> cruiserPossibleCoors;
    ArrayList<World.Coordinate> frigatePossibleCoors;
    ArrayList<World.Coordinate> submarinePossibleCoors;
    ArrayList<World.ShipLocation> remainingShips = new ArrayList<>();
    ArrayList<String> remainingShipsNames = new ArrayList<>();
    World world;
    int type;
    boolean targetMode;
    ArrayList<Guess> potentialGuess = new ArrayList<>();
    Stack<Guess> previousGuesses = new Stack<>();
    Stack<Guess> hits = new Stack<>();

    @Override
    public void initialisePlayer(World world) {
        type = 5;
        this.world = world;
        allCoors = new ArrayList<>();
        //insert ships into the local ship locations
        for(int i = 0; i < world.shipLocations.size(); i++) {
            remainingShips.add(world.shipLocations.get(i));
            remainingShipsNames.add(world.shipLocations.get(i).ship.name());
            System.out.println(remainingShipsNames.get(i));
        }
        for (int i = 0; i < world.numRow; i++) {
            for (int j = 0; j < world.numColumn; j++) {
                World.Coordinate coord = new World.Coordinate();
                coord.row = i;
                coord.column = j;
                allCoors.add(coord);
            }
        }
    }

    @Override
    public Answer getAnswer(Guess guess) {
        Answer answer = new Answer();
        World.Coordinate coords;
        //for each ship location, check each coordinate the ship contains
        //to see if it matches the guess.

        for(World.ShipLocation possibleShip : remainingShips)
        {
            Iterator<World.Coordinate> iter = possibleShip.coordinates.iterator();
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
                        //this.remainingShipsNames.remove(possibleShip.ship.name()); //remove ship name from the list of remaining ships names
                        return answer;
                    }
                }
            }
        }

        return answer;
    }

    @Override
    public Guess makeGuess() {
        Guess guess = new Guess();
        Random random = new Random();
        int index;
        boolean validShot = false;
        if (targetMode) {
            //targeting implementation, same as for greedy guess player
            if(!potentialGuess.isEmpty())
            {
                do
                {
                    index = random.nextInt(potentialGuess.size());
                    guess = potentialGuess.get(index);
                    if(isValidShot(guess))
                    {
                        potentialGuess.remove(index);
                        return guess;
                    }
                } while(validShot == false);
            }
        }
        if (!targetMode) {
            guess = possibleShipsLocations(type);
        }

        System.out.println(guess.toString());
        return guess;
    }

    @Override
    public void update(Guess guess, Answer answer) { //needs to be fixed to correctly trace types of remaining ships
        previousGuesses.push(guess);
        System.out.println(type + " update method");
        World.Coordinate hittedCoordinate = new World.Coordinate();
        hittedCoordinate.row = guess.row;
        hittedCoordinate.column = guess.column;
        hittedCoors.add(hittedCoordinate);
        System.out.println(hittedCoordinate.row + " " + hittedCoordinate.column + " hitted coordinate");
        if(answer.isHit == true)
        {
            hits.push(guess);
            targetMode = true; // target mode needs to be fixed so it stops when ship is sunk, currently it hits every cells around the ship even if it sunk
            addAdjacentGuesses(guess);
            if (answer.shipSunk != null) {
                //targetMode = false;
                remainingShipsNames.remove(answer.shipSunk.name());
                //hits.clear();
            }
        }
//        if (answer.shipSunk != null) {
//            remainingShipsNames.remove(answer.shipSunk);
//        }
//        if (remainingShipsNames.contains("AircraftCarrier")) {
//            type = 5;
//        }
//        if (remainingShipsNames.contains("Frigate") && !remainingShipsNames.contains("AircraftCarrier")) {
//            type = 4;
//        }
//        if (remainingShipsNames.contains("Cruiser") && !remainingShipsNames.contains("AircraftCarrier") && !remainingShipsNames.contains("Frigate")) {
//            type = 3;
//        }
//        if (remainingShipsNames.contains("Submarine") && !remainingShipsNames.contains("AircraftCarrier") && !remainingShipsNames.contains("Frigate") && !remainingShipsNames.contains("Cruiser")) {
//            type = 2;
//        }
//        if (remainingShipsNames.contains("PatrolCraft") && !remainingShipsNames.contains("AircraftCarrier") &&
//                !remainingShipsNames.contains("Frigate") && !remainingShipsNames.contains("Cruiser") && !remainingShipsNames.contains("Submarine")) {
//            type = 1;
//        }
        System.out.println(remainingShipsNames.size() + " size of remaining ships names");

           if (remainingShipsNames.contains("PatrolCraft")) {
               type = 1;
           }
           if (remainingShipsNames.contains("Submarine") && !remainingShipsNames.contains("PatrolCraft")) {
               type = 2;
           }
           if (remainingShipsNames.contains("Cruiser") && !remainingShipsNames.contains("Submarine") && !remainingShipsNames.contains("PatrolCraft")) {
               type = 3;
           }
           if (remainingShipsNames.contains("Frigate") && !remainingShipsNames.contains("Cruiser") && !remainingShipsNames.contains("Submarine") && !remainingShipsNames.contains("PatrolCraft")) {
               type = 4;
           }
           if (remainingShipsNames.contains("AircraftCarrier") && !remainingShipsNames.contains("Frigate") && !remainingShipsNames.contains("Cruiser")
                   && !remainingShipsNames.contains("Submarine") && !remainingShipsNames.contains("PatrolCraft")) {
               type = 5;
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
    }

    @Override
    public boolean noRemainingShips() {
        return remainingShips.isEmpty();
    }

    public Guess possibleShipsLocations(int type) {
        //ArrayList<Guess> possibleGuesses = new ArrayList<>();
        Guess possibleGuess = new Guess();
        ArrayList<World.Coordinate> pickedCoors = new ArrayList<>();
        switch (type) {
            case 1: //patrol craft 1x2 dimension
                for (World.Coordinate coor : allCoors) {
                    if (coor.column + 1 <= world.numColumn) { //to be sure we don't pick cell outside of the field, horizontal positioning
                        World.Coordinate nextCoor = new World.Coordinate();
                        nextCoor.column = coor.column + 1;
                        nextCoor.row = coor.row;
                        if (!hittedCoors.contains(coor) && !hittedCoors.contains(nextCoor)) {
                            possibleGuess.row = coor.row;
                            possibleGuess.column = coor.column;
                            //hittedCoors.add(coor);
                            pickedCoors.add(coor);
                            pickedCoors.add(nextCoor);
                            System.out.println("case 1 horizon");
                        }
                    }
                    if (coor.row - 1 >= 0) { //to be sure we don't pick cell outside of the field, vertical positioning
                        World.Coordinate bottomCoor = new World.Coordinate();
                        bottomCoor.column = coor.column;
                        bottomCoor.row = coor.row - 1;
                        World.Coordinate nextCoor = new World.Coordinate();
                        nextCoor.column = coor.column + 1;
                        nextCoor.row = coor.row;
                        if (!hittedCoors.contains(coor) && hittedCoors.contains(nextCoor) && !hittedCoors.contains(bottomCoor)) {
                            possibleGuess.row = coor.row;
                            possibleGuess.column = coor.column;
                            //hittedCoors.add(coor);
                            System.out.println("case 1 vertical");
                        }
                    }

                }

            case 2: //submarine 1x3 dimension
                for (World.Coordinate coor : allCoors) {
                    if (coor.column + 2 <= world.numColumn) { //to be sure we don't pick cell outside of the field, horizontal positioning
                        World.Coordinate nextCoor = new World.Coordinate();
                        nextCoor.column = coor.column + 1;
                        nextCoor.row = coor.row;
                        World.Coordinate secondNextCoor = new World.Coordinate();
                        secondNextCoor.column = nextCoor.column + 1;
                        secondNextCoor.row = nextCoor.row;
                        if (!hittedCoors.contains(coor) && !hittedCoors.contains(nextCoor) && !hittedCoors.contains(secondNextCoor)) { //horizontal positioning
                            possibleGuess.row = coor.row;
                            possibleGuess.column = coor.column;
                            //hittedCoors.add(coor);
                            pickedCoors.add(coor);
                            pickedCoors.add(nextCoor);
                            pickedCoors.add(secondNextCoor);
                            System.out.println("case 2 horizon");
                        }
                    }
                    if (coor.row - 2 >= 0) { //to be sure we don't pick cell outside of the field, vertical positioning
                        World.Coordinate nextCoor = new World.Coordinate();
                        nextCoor.column = coor.column + 1;
                        nextCoor.row = coor.row;
                        World.Coordinate secondNextCoor = new World.Coordinate();
                        secondNextCoor.column = nextCoor.column + 1;
                        secondNextCoor.row = nextCoor.row;
                        World.Coordinate bottomCoor = new World.Coordinate();
                        bottomCoor.column = coor.column;
                        bottomCoor.row = coor.row - 1;
                        World.Coordinate secondBottomCoor = new World.Coordinate();
                        secondBottomCoor.column = coor.column;
                        secondBottomCoor.row = coor.row - 2;
                        boolean check = hittedCoors.contains(nextCoor) || hittedCoors.contains(secondNextCoor); //checking whether one of the two next cells were hit
                        if (!hittedCoors.contains(coor) && check && !hittedCoors.contains(bottomCoor) && !hittedCoors.contains(secondBottomCoor)) { //vertical positioning
                            possibleGuess.row = coor.row;
                            possibleGuess.column = coor.column;
                            //hittedCoors.add(coor);
                            System.out.println("case 2 vertical");
                        }
                    }
                }
            case 3: //cruiser 2x2 dimension
                for (World.Coordinate coor : allCoors) {
                    if (coor.column + 1 <= world.numColumn && coor.row - 1 >= 0) { //to be sure we don't pick cell outside of the field
                        World.Coordinate nextCoor = new World.Coordinate();
                        nextCoor.row = coor.row;
                        nextCoor.column = coor.column;
                        World.Coordinate bottomCoor = new World.Coordinate();
                        bottomCoor.column = coor.column;
                        bottomCoor.row = coor.row - 1;
                        World.Coordinate nextBottomCoor = new World.Coordinate();
                        nextBottomCoor.row = bottomCoor.row;
                        nextBottomCoor.column = bottomCoor.column + 1;
                        if (!hittedCoors.contains(coor) && !hittedCoors.contains(nextCoor) && !hittedCoors.contains(bottomCoor) && !hittedCoors.contains(nextBottomCoor)) {
                            possibleGuess.row = coor.row;
                            possibleGuess.column = coor.column;
                            //hittedCoors.add(coor);
                            System.out.println("case 3 square");
                        }
                    }
                }
            case 4: //frigate 1x4 dimension
                for (World.Coordinate coor : allCoors) {
                    if (coor.column + 3 <= world.numColumn) { //to be sure we don't pick cell outside of the field, horizontal positioning
                        World.Coordinate nextCoor = new World.Coordinate();
                        nextCoor.column = coor.column + 1;
                        nextCoor.row = coor.row;
                        World.Coordinate secondNextCoor = new World.Coordinate();
                        secondNextCoor.column = nextCoor.column + 1;
                        secondNextCoor.row = nextCoor.row;
                        World.Coordinate thirdNextCoor = new World.Coordinate();
                        thirdNextCoor.column = secondNextCoor.column + 1;
                        thirdNextCoor.row = secondNextCoor.row;
                        if (!hittedCoors.contains(coor) && !hittedCoors.contains(nextCoor) && !hittedCoors.contains(secondNextCoor)) { //horizontal positioning
                            possibleGuess.row = coor.row;
                            possibleGuess.column = coor.column;
                            //hittedCoors.add(coor);
                            pickedCoors.add(coor);
                            pickedCoors.add(nextCoor);
                            pickedCoors.add(secondNextCoor);
                            pickedCoors.add(thirdNextCoor);
                            System.out.println("case 4 horizon");
                        }

                    }
                    if (coor.row - 3 >= 0) { //to be sure we don't pick cell outside of the field, vertical positioning
                        World.Coordinate nextCoor = new World.Coordinate();
                        nextCoor.column = coor.column + 1;
                        nextCoor.row = coor.row;
                        World.Coordinate secondNextCoor = new World.Coordinate();
                        secondNextCoor.column = nextCoor.column + 1;
                        secondNextCoor.row = nextCoor.row;
                        World.Coordinate thirdNextCoor = new World.Coordinate();
                        thirdNextCoor.column = secondNextCoor.column + 1;
                        thirdNextCoor.row = secondNextCoor.row;
                        World.Coordinate bottomCoor = new World.Coordinate();
                        bottomCoor.column = coor.column;
                        bottomCoor.row = coor.row - 1;
                        World.Coordinate secondBottomCoor = new World.Coordinate();
                        secondBottomCoor.column = coor.column;
                        secondBottomCoor.row = coor.row - 2;
                        World.Coordinate thirdBottomCoor = new World.Coordinate();
                        thirdBottomCoor.column = coor.column;
                        thirdBottomCoor.row = coor.row - 2;
                        boolean check = hittedCoors.contains(nextCoor) || hittedCoors.contains(secondNextCoor) || hittedCoors.contains(thirdNextCoor); //checking whether one of the three next cells were hit
                        if (!hittedCoors.contains(coor) && check && !hittedCoors.contains(bottomCoor) && !hittedCoors.contains(secondBottomCoor)) { //vertical positioning
                            possibleGuess.row = coor.row;
                            possibleGuess.column = coor.column;
                            //hittedCoors.add(coor);
                            System.out.println("case 1 vertical");
                        }
                    }
                }
            case 5: //aircraft carrier 2x3 dimension
                for (World.Coordinate coor : allCoors) {
                    if (coor.column + 2 <= world.numColumn && coor.row - 1 >= 0) { //to be sure we don't pick cell outside of the field, horizontal positioning
                        World.Coordinate nextCoor = new World.Coordinate();
                        nextCoor.column = coor.column + 1;
                        nextCoor.row = coor.row;
                        World.Coordinate secondNextCoor = new World.Coordinate();
                        secondNextCoor.column = nextCoor.column + 1;
                        secondNextCoor.row = nextCoor.row;
                        World.Coordinate bottomCoor = new World.Coordinate();
                        bottomCoor.column = coor.column;
                        bottomCoor.row = coor.row-1;
                        World.Coordinate secondBottomCoor = new World.Coordinate();
                        secondBottomCoor.column = nextCoor.column + 1;
                        secondBottomCoor.row = nextCoor.row - 1;
                        World.Coordinate thirdBottomCoor = new World.Coordinate();
                        thirdBottomCoor.column = secondNextCoor.column + 1;
                        thirdBottomCoor.row = secondNextCoor.row - 1;
                        if (!hittedCoors.contains(coor) && !hittedCoors.contains(nextCoor) && !hittedCoors.contains(secondNextCoor)
                                && !hittedCoors.contains(bottomCoor) && !hittedCoors.contains(secondBottomCoor) && !hittedCoors.contains(thirdBottomCoor)) { //horizontal positioning
                            possibleGuess.row = coor.row;
                            possibleGuess.column = coor.column;
                            //hittedCoors.add(coor);
                            pickedCoors.add(coor);
                            pickedCoors.add(nextCoor);
                            pickedCoors.add(secondNextCoor);
                            pickedCoors.add(bottomCoor);
                            pickedCoors.add(secondBottomCoor);
                            pickedCoors.add(thirdBottomCoor);
                            System.out.println("case 5 horizon");
                        }

                    }
                    if (coor.row - 2 >= 0 && coor.column + 1 < world.numColumn) { //to be sure we don't pick cell outside of the field, vertical positioning
                        World.Coordinate nextCoor = new World.Coordinate();
                        nextCoor.column = coor.column + 1;
                        nextCoor.row = coor.row;
                        World.Coordinate bottomCoor = new World.Coordinate();
                        bottomCoor.column = coor.column;
                        bottomCoor.row = coor.row-1;
                        World.Coordinate secondBottomCoor = new World.Coordinate();
                        secondBottomCoor.column = nextCoor.column;
                        secondBottomCoor.row = nextCoor.row - 1;
                        World.Coordinate lowerBottomCoor = new World.Coordinate();
                        lowerBottomCoor.column = bottomCoor.column;
                        lowerBottomCoor.row = bottomCoor.row - 1;
                        World.Coordinate secondLowerBottomCoor = new World.Coordinate();
                        secondLowerBottomCoor.column = secondBottomCoor.column;
                        secondLowerBottomCoor.row = secondBottomCoor.row - 1;
                        World.Coordinate secondNextCoor = new World.Coordinate();
                        secondNextCoor.column = nextCoor.column + 1;
                        secondNextCoor.row = nextCoor.row;
                        World.Coordinate thirdBottomCoor = new World.Coordinate();
                        thirdBottomCoor.column = secondNextCoor.column + 1;
                        thirdBottomCoor.row = secondNextCoor.row - 1;
                        boolean check = hittedCoors.contains(secondNextCoor) || hittedCoors.contains(thirdBottomCoor); //checking whether one of the three next cells were hit
                        if (!hittedCoors.contains(coor) && check && !hittedCoors.contains(nextCoor) && !hittedCoors.contains(bottomCoor) && !hittedCoors.contains(secondBottomCoor) &&
                                !hittedCoors.contains(lowerBottomCoor) && !hittedCoors.contains(secondLowerBottomCoor)) { //vertical positioning
                            possibleGuess.row = coor.row;
                            possibleGuess.column = coor.column;
                            //hittedCoors.add(coor);
                            System.out.println("case 5 vertical");
                        }
                    }
                }
        }
        return possibleGuess;
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

    private Stack<Guess> reverseStack(Stack<Guess> stack)
    {
        Stack<Guess> temp = new Stack<Guess>();

        while(!stack.isEmpty())
        {
            temp.push(stack.pop());
        }
        return temp;
    }
}
