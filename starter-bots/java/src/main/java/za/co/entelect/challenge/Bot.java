package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.enums.Direction;

import java.util.*;
import java.util.stream.Collectors;
import java.io.*;
import java.lang.*;
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

    public Position GetWormPos(int ID){
        Worm worm = gameState.myPlayer.worms[ID-1];

        if(worm.health > 0){
            return worm.position;
        }else{
            return null;
        }
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
                //Position tempPos = resolveToPosition(currentWorm.position,gameState.opponents[0].worms[2].position);
                //return new MoveCommand(tempPos.x, tempPos.y);
                return digAndMoveTo(currentWorm.position, gameState.opponents[0].worms[2].position);

            }

        }else if(getCurrentWorm(gameState).id == 2){ // agent 
            //if(true){
            if(getCurrentWorm(gameState).bananaBomb.count>0){
                return new ThrowBananaCommand(currentWorm.position.x, currentWorm.position.y);
                //return new DoNothingCommand();
            }
        }else if(getCurrentWorm(gameState).id == 3){ //tech
            if(getCurrentWorm(gameState).snowballs.count>0){
                return new ThrowSnowballCommand(currentWorm.position.x, currentWorm.position.y);
                //return new DoNothingCommand();
            }
        }
        System.out.println("is Doing Nothing");
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
    // ini kayaknya line of sight
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
            toPosition.x = origin.x - 1;
        } else if (horizontalComponent > 0) {
            toPosition.x = origin.x + 1;
        }

        return toPosition;
    }

    private Cell findCell(Position nextPosition) {
        return gameState.map[nextPosition.y][nextPosition.x];
    }


    private Command digAndMoveTo(Position origin, Position destination) {
        Position nextPosition = resolveToPosition(origin,destination);

        MyWorm[] worms = gameState.myPlayer.worms;
        boolean canMove = true;

        for (int i = 0; i < worms.length; i++) {
            if (worms[i].position.equals(nextPosition)) {
                canMove = false;
            }
        }

        if (!isValidCoordinate(nextPosition.x, nextPosition.y)) {
            canMove = false;
        }

        if (canMove) {
            Cell nextCell = findCell(nextPosition);
//            Command cmd;

            if (nextCell.type == CellType.AIR) {
                System.out.println(nextCell.type);
                return new MoveCommand(nextPosition.x,nextPosition.y);
            } else if (nextCell.type == CellType.DIRT) {
                return new DigCommand(nextPosition.x,nextPosition.y);
            } else {
                return new DoNothingCommand();
            }
        }
        return new DoNothingCommand();

    }

    // Cek apakah ada dirt sepanjang jarak tembak
    private boolean isThereAnyObstacle (Position a_pos, Position b_pos) {
        int x_dif = a_pos.x - b_pos.x;
        int y_dif = a_pos.y - b_pos.y;
        double mag = Math.sqrt(Math.pow(x_dif, 2) + Math.pow(y_dif, 2));
        int x_dir;
        int y_dir;
        if (x_dif >= 0) {
            x_dir = (int) Math.ceil(x_dif/mag);
        } else {
            x_dir = (int) Math.floor(x_dif/mag);
        }

        if (y_dif >= 0) {
            y_dir = (int) Math.ceil(y_dif/mag);
        } else {
            y_dir = (int) Math.floor(y_dif/mag);
        }

        Position c_pos = new Position();
        c_pos.x = a_pos.x += x_dir;
        c_pos.y = a_pos.y += y_dir;
        boolean isThere = false;

        while ((c_pos.x != b_pos.x) && (c_pos.y != b_pos.y) && !isThere) {
            if (gameState.map[c_pos.y][c_pos.x].type == CellType.DIRT) {
                isThere = true;
            }
            c_pos.x += x_dir;
            c_pos.y += y_dir;
        }
        return isThere;
    }

    // Cek apakah posisi tersebut aman, tidak mengecek apakah sel tersebut dirt/air
    private boolean isOnEnemyLineOfSight(Position pos) {
        Worm[] listEnemyWorms = opponent.worms;
        boolean isOn = false;
        for (int i = 0; i < listEnemyWorms.length && !isOn; i++) {

            if (listEnemyWorms[i].alive()) { // Cek masih hidup saja
                Position enemyPos = listEnemyWorms[i].position;
                int distance = euclideanDistance(pos.x,pos.y,enemyPos.x,enemyPos.y);

                int x_dif = Math.abs(pos.x- enemyPos.x);
                int y_dif = Math.abs(pos.y- enemyPos.y);
                if (x_dif == y_dif) {   // Ada di diagonal?
                    isOn = true;
                } else if (x_dif == 0 || y_dif == 0) { // Ada di vertikal atau horizontal?
                    isOn = true;
                }
                int range = 4;  // Asumsi
                if (isOn && (distance > range || isThereAnyObstacle(pos,enemyPos))) { // Cek lagi bakal kena ga
                    isOn = false;
                }


            }
        }
        return isOn;
    }

    // Pastikan musuh sudah banyak didekat kita
//    private Command retreat() {
//        List<Position> vectorEnemyPos = new ArrayList<Position>();
//        Worm[] listEnemyWorms = opponent.worms;
//        Position pos = currentWorm.position;
//
//        for (int i = 0; i < listEnemyWorms.length; i++) {
//            Position enemyPos = listEnemyWorms[i].position;
//            if (listEnemyWorms[i].alive()) {
//                Position vectorPos = new Position();
//                vectorPos.x = pos.x - enemyPos.x;
//                vectorPos.y = pos.y - enemyPos.y;
//                vectorEnemyPos.add(vectorPos);
//
//            }
//        }
//
//    }

//    private Position grouping()

//    private Command Grouping()

    private List<List<Cell>> lineOfSight(Position pos) {
        int range = 4;
        List<List<Cell>> directionLines = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            List<Cell> directionLine = new ArrayList<>();
            for (int directionMultiplier = 1; directionMultiplier <= range; directionMultiplier++) {

                int coordinateX = pos.x + (directionMultiplier * direction.x);
                int coordinateY = pos.y + (directionMultiplier * direction.y);

                if (!isValidCoordinate(coordinateX, coordinateY)) {
                    break;
                }

                if (euclideanDistance(pos.x, pos.y, coordinateX, coordinateY) > range) {
                    break;
                }

                Cell cell = gameState.map[coordinateY][coordinateX];
                if (cell.type == CellType.DIRT) {
                    break;
                }

                directionLine.add(cell);
            }
            directionLines.add(directionLine);
        }

        return directionLines;
    }

    private int bombDamage(Position e3, int i, int j){
        if(e3.x == i && e3.y == j){
            return 20;
        }
        else if(Math.abs(i - e3.x) + Math.abs(j - e3.y) == 1){
            return 13;
        }
        else if(Math.abs(i - e3.x) == 1 && Math.abs(j - e3.y) == 1){
            return 11;
        }
        else if(Math.abs(i - e3.x) + Math.abs(j - e3.y) == 2){
            return 7;
        }
        else{
            return 0;
        }
    }

    private int maxDamageFromBomb(Position pos) {
        int max = 0, range = 5, tempMax, x = pos.x, y = pos.y;
        for (int i = x - 5; i <= x + 5; i++) {
            for (int j = y - 5; j <= y + 5; j++) {
                // Don't include the current position
                if (i != x && j != y && isValidCoordinate(i, j) && (euclideanDistance(pos.x, pos.y, i, j) > range)) {
                    Position e1 = GetEnemyPos(1), e2 = GetEnemyPos(2), e3 = GetEnemyPos(3);
                    tempMax = 0;
                    if(e3 != null){
                        tempMax += bombDamage(e3, i, j);
                    }
                    if(e2 != null){
                        tempMax += bombDamage(e2, i, j);
                    }
                    if(e1 != null){
                        tempMax += bombDamage(e1, i, j);
                    }
                    if (tempMax > max) {
                        max = tempMax;
                    }
                }
            }
        }
        return max;
    }

    private int maxFrozen(Position pos){
        int max = 0, range = 5, tempMax, x = pos.x, y = pos.y;
        for (int i = x - 5; i <= x + 5; i++) {
            for (int j = y - 5; j <= y + 5; j++) {
                // Don't include the current position
                if (i != x && j != y && isValidCoordinate(i, j) && (euclideanDistance(pos.x, pos.y, i, j) > range)) {
                    Position e1 = GetEnemyPos(1), e2 = GetEnemyPos(2), e3 = GetEnemyPos(3);
                    tempMax = 0;
                    if(e3 != null){
                        if(euclideanDistance(e3.x, e3.y, i, j) < 2) {
                            tempMax += 1;
                        }
                    }
                    if(e2 != null){
                        if(euclideanDistance(e2.x, e2.y, i, j) < 2) {
                            tempMax += 1;
                        }
                    }
                    if(e1 != null){
                        if(euclideanDistance(e1.x, e1.y, i, j) < 2) {
                            tempMax += 1;
                        }
                    }
                    if (tempMax > max) {
                        max = tempMax;
                    }
                }
            }
        }
        return max;
    }

    public modifiedCell[][] shortestRoute(Cell[][] GameMap, Cell source){
        //initialize
        modifiedCell[][] Result = new modifiedCell[GameMap.length][GameMap[0].length];
        for(int i = 0; i < GameMap.length;i++ ){
            for(int j = 0; j<GameMap[0].length;j++){
                Result[i][j].cell = GameMap[i][j];
                Result[i][j].prev = null;
                Result[i][j].visit = false;
                Result[i][j].distance = Integer.MAX_VALUE;//asumsikan tak hingga
            }
        }
        //initialize source nya
        Result[source.x][source.y].visit = true;
        Result[source.x][source.y].distance = 0;
        //starting dijstra
        int[][] ToBeVisited = new int[33*33][2];
        ToBeVisited = getAdjacentCell(source.x,source.y,Result); //initialize node yang akan di visit disekitar sumber
        for(int i = 0;i<ToBeVisited.length;i++) {
            int idxPrevX = ToBeVisited[i][0];
            int idxPrevY = ToBeVisited[i][1];
            Result[idxPrevX][idxPrevY].prev.x = ToBeVisited[i][0]; //initialize prev nya
            Result[idxPrevX][idxPrevY].prev.y = ToBeVisited[i][1];


            if (Result[idxPrevX][idxPrevY].cell.type == CellType.AIR) {
                Result[idxPrevX][idxPrevY].distance = Result[Result[idxPrevX][idxPrevY].prev.x][Result[idxPrevX][idxPrevY].prev.y].distance + 1;
            } else if (Result[idxPrevX][idxPrevY].cell.type == CellType.DIRT) {
                Result[idxPrevX][idxPrevY].distance = Result[Result[idxPrevX][idxPrevY].prev.x][Result[idxPrevX][idxPrevY].prev.y].distance + 2;
            }else{ //deep space
                Result[idxPrevX][idxPrevY].distance = Integer.MAX_VALUE;//asumsikan infinite
            }
        }
        while(ToBeVisited != null){
            int idx = getMinDist(ToBeVisited,Result); //ambil yang distancenya minimal
            Result[ToBeVisited[idx][0]][ToBeVisited[idx][1]].visit = true; //ini di visit
            int CurrX = ToBeVisited[idx][0];
            int CurrY = ToBeVisited[idx][1];
            if (Result[CurrX][CurrY].cell.type == CellType.AIR) {
                Result[CurrX][CurrY].distance = Result[Result[CurrX][CurrY].prev.x][Result[CurrX][CurrY].prev.y].distance + 1;
            } else if (Result[CurrX][CurrY].cell.type == CellType.DIRT) {
                Result[CurrX][CurrY].distance = Result[Result[CurrX][CurrY].prev.x][Result[CurrX][CurrY].prev.y].distance + 2;
            }else{ //deep space
                Result[CurrX][CurrY].distance = Integer.MAX_VALUE;//asumsikan infinite
            }
            ToBeVisited[idx] = null; //kalkulasi buat cell ini selesai

            int[][] TempToBeVisited = new int[33*33][2];
            TempToBeVisited = getAdjacentCell(CurrX,CurrY,Result); //ambil semua cell yg adj dg curr cell
            for(int i = 0;i<TempToBeVisited.length;i++) {
                int idxPrevX = TempToBeVisited[i][0];
                int idxPrevY = TempToBeVisited[i][1];
                Result[idxPrevX][idxPrevY].prev.x = TempToBeVisited[i][0]; //initialize prev nya untuk cell yang adj dg currCell
                Result[idxPrevX][idxPrevY].prev.y = TempToBeVisited[i][1];


                if (Result[idxPrevX][idxPrevY].cell.type == CellType.AIR) {
                    Result[idxPrevX][idxPrevY].distance = Result[Result[idxPrevX][idxPrevY].prev.x][Result[idxPrevX][idxPrevY].prev.y].distance + 1;
                } else if (Result[idxPrevX][idxPrevY].cell.type == CellType.DIRT) {
                    Result[idxPrevX][idxPrevY].distance = Result[Result[idxPrevX][idxPrevY].prev.x][Result[idxPrevX][idxPrevY].prev.y].distance + 2;
                }else{ //deep space
                    Result[idxPrevX][idxPrevY].distance = Integer.MAX_VALUE;//asumsikan infinite
                }
            }


            ToBeVisited = append(ToBeVisited,getAdjacentCell(CurrX,CurrY,Result)); //nanti buat fungsi merge 2 array somehow
        }

        return Result;
    }
    public  int[][] append(int[][] a,int[][] b){
        int[][] c = new int[a.length + b.length][2];
        for(int i = 0;i<a.length;i++){
            c[i][0] = a[i][0];
            c[i][1] = a[i][1];
        }
        for(int i = a.length;i< a.length + b.length;i++){
            c[i][0] = b[i-a.length][0];
            c[i][1] = b[i-a.length][1];
        }
        return c;
    }

    public  int[][] getAdjacentCell(int x,int y , modifiedCell[][] map) {
        //int x = src.cell.x;
        //int y = src.cell.y;
        int a[][] = {{x + 1, y + 1}, {x + 1, y}, {x + 1, y - 1}, {x, y + 1}, {x, y - 1}, {x - 1, y + 1}, {x - 1, y}, {x - 1, y - 1}};
        int[][] Result = new int[8][2];
        int j= 0;
        for (int i = 0; i < a.length; i++) {
            if(map[a[i][0]][a[i][1]].visit = false){
                Result[j][0] = map[a[i][0]][a[i][1]].cell.x;
                Result[j][1] = map[a[i][0]][a[i][1]].cell.y;
                j++;
            }

        }
        return Result;
    }
    public int getMinDist(int[][] ToBeVisited,modifiedCell[][] map){
        int min = map[ToBeVisited[0][0]][ToBeVisited[0][1]].distance;
        int idxmin = 0;
        for(int i = 1;i< ToBeVisited.length;i++){
            if(map[ToBeVisited[i][0]][ToBeVisited[i][1]].distance<min){
                min = map[ToBeVisited[i][0]][ToBeVisited[i][1]].distance;
                idxmin = i;
            }
        }
        return idxmin;
    }

}
