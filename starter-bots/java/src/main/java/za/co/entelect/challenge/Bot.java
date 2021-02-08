package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.enums.Direction;

import java.util.*;
import java.util.stream.Collectors;
import java.io.*;
public class Bot {

    private Random random;
    private GameState gameState;
    private Opponent opponent;
    private MyWorm currentWorm;

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.opponent = gameState.opponents[0];
        this.currentWorm = getCurrentWorm(gameState);
    }

    private MyWorm getCurrentWorm(GameState gameState) {
        return Arrays.stream(gameState.myPlayer.worms)
                .filter(myWorm -> myWorm.id == gameState.currentWormId)
                .findFirst()
                .get();
    }

    
    public Position GetEnemyPos(int ID){
        Worm Enemyworm = gameState.opponents[0].worms[ID-1];

        if(Enemyworm.health > 0){
            //System.out.println(Enemyworm.position.x);
            //System.out.println(Enemyworm.position.y);
            return Enemyworm.position;
        }else{
            return null;
        }
    }

    public Command run(){
        if(getCurrentWorm(gameState).id == 1){ //commander
            Worm enemyWorm = getFirstWormInRange();
            if (enemyWorm != null) {
                System.out.println("recognizing enemy in sight and shooting");
                Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
                return new ShootCommand(direction);
            }
            if(GetEnemyPos(3) != null){//asumsikan 3 itu tech
                System.out.println("recognizing enemy tech and hunting");
                Position tempPos = resolveToPosition(currentWorm.position,gameState.opponents[0].worms[2].position);
                return new MoveCommand(tempPos.x, tempPos.y);
            }

        }else if(getCurrentWorm(gameState).id == 2){ // agent 
            //if(true){
            if(getCurrentWorm(gameState).bananaBomb.count>0){
                return new DoNothingCommand();
            }
        }else if(getCurrentWorm(gameState).id == 3){ //tech
            if(getCurrentWorm(gameState).snowballs.count>0){
                return new DoNothingCommand();
            }
        }
        return new DoNothingCommand();
        /**
        Worm enemyWorm = getFirstWormInRange();
        if (enemyWorm != null) {
            Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
            return new ShootCommand(direction);
        }

        List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
        int cellIdx = random.nextInt(surroundingBlocks.size());

        Cell block = surroundingBlocks.get(cellIdx);
        if (block.type == CellType.AIR) {
            return new MoveCommand(block.x, block.y);
        } else if (block.type == CellType.DIRT) {
            return new DigCommand(block.x, block.y);
        }

        return new DoNothingCommand();
        */
    }

    private Worm getFirstWormInRange() {

        Set<String> cells = constructFireDirectionLines(currentWorm.weapon.range)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        for (Worm enemyWorm : opponent.worms) {
            String enemyPosition = String.format("%d_%d", enemyWorm.position.x, enemyWorm.position.y);
            if (cells.contains(enemyPosition)) {
                return enemyWorm;
            }
        }

        return null;
    }

    private List<List<Cell>> constructFireDirectionLines(int range) {
        List<List<Cell>> directionLines = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            List<Cell> directionLine = new ArrayList<>();
            for (int directionMultiplier = 1; directionMultiplier <= range; directionMultiplier++) {

                int coordinateX = currentWorm.position.x + (directionMultiplier * direction.x);
                int coordinateY = currentWorm.position.y + (directionMultiplier * direction.y);

                if (!isValidCoordinate(coordinateX, coordinateY)) {
                    break;
                }

                if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, coordinateX, coordinateY) > range) {
                    break;
                }

                Cell cell = gameState.map[coordinateY][coordinateX];
                if (cell.type != CellType.AIR) {
                    break;
                }

                directionLine.add(cell);
            }
            directionLines.add(directionLine);
        }

        return directionLines;
    }

    private List<Cell> getSurroundingCells(int x, int y) {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // Don't include the current position
                if (i != x && j != y && isValidCoordinate(i, j)) {
                    cells.add(gameState.map[j][i]);
                }
            }
        }

        return cells;
    }

    private int euclideanDistance(int aX, int aY, int bX, int bY) {
        return (int) (Math.sqrt(Math.pow(aX - bX, 2) + Math.pow(aY - bY, 2)));
    }

    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < gameState.mapSize
                && y >= 0 && y < gameState.mapSize;
    }

    private Direction resolveDirection(Position a, Position b) {
        StringBuilder builder = new StringBuilder();

        int verticalComponent = b.y - a.y;
        int horizontalComponent = b.x - a.x;

        if (verticalComponent < 0) {
            builder.append('N');
        } else if (verticalComponent > 0) {
            builder.append('S');
        }

        if (horizontalComponent < 0) {
            builder.append('W');
        } else if (horizontalComponent > 0) {
            builder.append('E');
        }

        return Direction.valueOf(builder.toString());
    }

    private Position resolveToPosition(Position origin, Position destination) {
        Position toPosition = new Position();

        int verticalComponent = destination.y - origin.y;
        int horizontalComponent = destination.x - origin.x;

        if (verticalComponent < 0) {
            toPosition.y = origin.y - 1;
        } else if (verticalComponent > 0) {
            toPosition.y = origin.y + 1;
        }

        if (horizontalComponent < 0) {
            toPosition.x = origin.x + 1;
        } else if (horizontalComponent > 0) {
            toPosition.x = origin.x - 1;
        }

        return toPosition;
    }

    private Cell findNextCellInPath(Position nextPosition) {
        return gameState.map[nextPosition.x][nextPosition.y];
    }


    private Command digAndMoveTo(Position origin, Position destination) {
        Position nextPosition = resolveToPosition(origin,destination);

        boolean canMove = true;
        MyWorm[] worms = gameState.myPlayer.worms;
        for (int i = 0; i < worms.length; i++) {
            if (worms[i].position.equals(nextPosition)) {
                canMove = false;
            }
        }

        if (!isValidCoordinate(nextPosition.x, nextPosition.y)) {
            canMove = false;
        }

        if (canMove) {
            Cell nextCell = findNextCellInPath(nextPosition);
//            Command cmd;
            if (nextCell.type == CellType.DIRT) {
                return new DigCommand(nextPosition.x,nextPosition.y);
            } else if (nextCell.type == CellType.AIR) {
                return new MoveCommand(nextPosition.x,nextPosition.y);
            } else {
                return new DoNothingCommand();
            }
        } else {
            return new DoNothingCommand();
        }

    }

}
