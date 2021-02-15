package za.co.entelect.challenge;

//import javafx.geometry.Pos;
//import javafx.util.Pair;
import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.enums.Direction;

import java.util.*;
import java.util.stream.Collectors;
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
        //modifiedCell[][] test = shortestRoute(gameState.map,currentWorm.position);
        if(getCurrentWorm(gameState).id == 1){ //commander
            /*Position enemy = basicShot(currentWorm.position);
            if (enemy != null){
                System.out.println("SHOOT");
                Direction direction = resolveDirection(currentWorm.position, enemy);
                return new ShootCommand(direction);
            }*/
            Command com = basicShot();
            if(com != null){
                return com;
            }
            if(GetEnemyPos(3) != null){
                System.out.println("recognizing enemy tech and hunting");
                return digAndMoveTo(currentWorm.position, GetEnemyPos(3));
            }
            /*
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
            }*/


        }else if(getCurrentWorm(gameState).id == 2){ // agent 
            //if(true){
            if(getCurrentWorm(gameState).bananaBomb.count>0){
                PairBomb pb = maxDamageFromBomb(currentWorm.position);
                if (pb.pos != null && pb.damage >= 20){
                    return new ThrowBananaCommand(pb.pos.x, pb.pos.y);
                }
                //return new ThrowBananaCommand(currentWorm.position.x, currentWorm.position.y);
                //return new DoNothingCommand();
            }
            /*Position enemy = basicShot(currentWorm.position);
            if (enemy != null){
                Direction direction = resolveDirection(currentWorm.position, enemy);
                return new ShootCommand(direction);
            }*/
            Command com = basicShot();
            if(com != null){
                return com;
            }
            return digAndMoveTo(currentWorm.position, GetWormPos(1));
        }else if(getCurrentWorm(gameState).id == 3){ //tech
            if(getCurrentWorm(gameState).snowballs.count>0){
                PairBomb pb = maxFrozen(currentWorm.position);
                if (pb.pos != null && pb.damage > 0) {
                    return new ThrowSnowballCommand(pb.pos.x, pb.pos.y);
                }
                //return new ThrowSnowballCommand(currentWorm.position.x, currentWorm.position.y);
                //return new DoNothingCommand();
            }
            /*Position enemy = basicShot(currentWorm.position);
            if (enemy != null){
                Direction direction = resolveDirection(currentWorm.position, enemy);
                return new ShootCommand(direction);
            }*/
            Command com = basicShot();
            if(com != null){
                return com;
            }
            return digAndMoveTo(currentWorm.position, GetWormPos(1));
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

    private boolean isNotLavaOrSpace(int x, int y) {
        return (gameState.map[y][x].type != CellType.DEEP_SPACE) &&
                (gameState.map[y][x].type != CellType.LAVA);
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
        Position toPosition = new Position(origin.x, origin.y);

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
        if(origin == null || destination == null){
            return new DoNothingCommand();
        }
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

    // Ini buat normalisasi vektor
    private Position normalizeVector(Position vectorPos) {
        Position dir = new Position();

        int x_dif = vectorPos.x;
        int y_dif = vectorPos.y;
        double mag = Math.sqrt(Math.pow(x_dif, 2) + Math.pow(y_dif, 2));
        double cdir_x = x_dif/mag;
        double cdir_y = y_dif/mag;

        // Solve yang x
        if (cdir_x > -0.5 && cdir_x < 0.5) {
            dir.x = 0;
        } else if (cdir_x >= 0.5) {
            dir.x = 1;
        } else {
            dir.x = 0;
        }
        // Solve yang y
        if (cdir_y > -0.5 && cdir_y < 0.5) {
            dir.y = 0;
        } else if (cdir_y >= 0.5) {
            dir.y = 1;
        } else {
            dir.y = 0;
        }

        return dir;
    }

    // Cek apakah ada dirt sepanjang jarak tembak
    private boolean isThereAnyObstacle (Position a_pos, Position b_pos) {
        Position dif = new Position();
        dif.x = b_pos.x - a_pos.x;
        dif.y = b_pos.y - a_pos.y;

        Position dir = normalizeVector(dif);

        Position c_pos = new Position();
        c_pos.x = a_pos.x + dir.x;
        c_pos.y = a_pos.y + dir.y;
        boolean isThere = false;

        while ((c_pos.x != b_pos.x) && (c_pos.y != b_pos.y) && !isThere) {
            if (findCell(c_pos).type == CellType.DIRT) {
                isThere = true;
            }
            c_pos.x += dir.x;
            c_pos.y += dir.y;
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

    private boolean isSaveToEscape(Position movePos) {
        return !isOnEnemyLineOfSight(movePos) && findCell(movePos).type != CellType.DIRT;
    }

    // Pastikan musuh sudah banyak didekat kita
    private Command retreat() {
        List<Position> vectorPos = new ArrayList<Position>();
        Worm[] listEnemyWorms = opponent.worms;
        Position pos = currentWorm.position;

        // Tambah Vektor Posisi musuh ke kita (biar bisa langsung dijumlahin)
        for (int i = 0; i < listEnemyWorms.length; i++) {
            Position enemyPos = listEnemyWorms[i].position;
            if (listEnemyWorms[i].alive()) {
                Position vectorPosEnemy = new Position();
                vectorPosEnemy.x = pos.x - enemyPos.x;      // Vektor Posisi musuh ke kita
                vectorPosEnemy.y = pos.y - enemyPos.y;
                vectorPos.add(vectorPosEnemy);
            }
        }

        // Tambah Vektor Posisi teman terdekat
        int distance = 999999;
        int idx = -1;
        Worm[] listPlayerWorms = gameState.myPlayer.worms;
        for (int i = 0; i < listPlayerWorms.length; i++) {
            if (currentWorm.id != listPlayerWorms[i].id) {  // Bukan worm sekarang
                if (listPlayerWorms[i].alive()) {
                    int c_distance = euclideanDistance(currentWorm.position.x,currentWorm.position.y,listPlayerWorms[i].position.x,listPlayerWorms[i].position.y);
                    if (c_distance < distance) {
                        distance = c_distance;
                        idx = i;
                    }
                }
            }
        }
        if (idx != -1) {
            Position vectorPosFriend = new Position();
            vectorPosFriend.x = listPlayerWorms[idx].position.x - pos.x;
            vectorPosFriend.y = listPlayerWorms[idx].position.y - pos.y;
            vectorPos.add(vectorPosFriend);
        }

        // Tambah Vektor Kecenderungan Bergerak Memutar
        Position vectorPosCenterMap = new Position();
        int rMultiplier = 2;
        int c_x = (pos.x - (gameState.mapSize/2))/rMultiplier;
        int c_y = (pos.y - (gameState.mapSize/2))/rMultiplier;
        // Putar 90 derajat
        vectorPosCenterMap.x = -c_y;
        vectorPosCenterMap.y = c_x;
        vectorPos.add(vectorPosCenterMap);

        Position totalVectorPos = new Position();
        totalVectorPos.x = 0;
        totalVectorPos.y = 0;

        for (int i = 0; i < vectorPos.size(); i++) {
            totalVectorPos.x += vectorPos.get(i).x;
            totalVectorPos.y += vectorPos.get(i).y;
        }

        // Normalisasi totalPosVector
        Position totalDir = normalizeVector(totalVectorPos);

        Position movePos = new Position();
        movePos.x = pos.x + totalDir.x;
        movePos.y = pos.y + totalDir.y;

        // Jika beruntung ini aman (ini kondisi yang sangat ideal dari banyak perhitungan)
        if (isSaveToEscape(movePos)) {
            return digAndMoveTo(pos,movePos);
        } else {    // Ternyata tidak beruntung
            List<Cell> surroundCell = getSurroundingCells(pos.x, pos.y);
            // Harusnya bikin PrioQueue
            // Cari posisi yang aman dulu aja
            Position alterMovePos = new Position();
            List<Cell> cellAman = new ArrayList<>();
            for (int i = 0; i < surroundCell.size(); i++) {
                alterMovePos.x = surroundCell.get(i).x;
                alterMovePos.y = surroundCell.get(i).y;
                if (isSaveToEscape(alterMovePos)) {
                    cellAman.add(surroundCell.get(i));
                }
            }
            Random rand = new Random();
            if (!cellAman.isEmpty()) { // ada yang aman, geraknya random aja kali ya
                int i = rand.nextInt(cellAman.size());
                alterMovePos.x = cellAman.get(i).x;
                alterMovePos.y = cellAman.get(i).y;
                return digAndMoveTo(pos,alterMovePos);
            } else {    // ga ada yang aman, gas aja kesana deh
                return digAndMoveTo(pos,movePos);

//                for (int i = 0; i < surroundCell.size(); i++) {
//                    alterMovePos.x = surroundCell.get(i).x;
//                    alterMovePos.y = surroundCell.get(i).y;
//                    if (!isOnEnemyLineOfSight(alterMovePos)) {
//                        cellAman.add(surroundCell.get(i));
//                    }
//                }
//                if (!cellAman.isEmpty()) { // ada yang aman aja
//                    int i = rand.nextInt(cellAman.size());
//                    alterMovePos.x = cellAman.get(i).x;
//                    alterMovePos.y = cellAman.get(i).y;
//                    return digAndMoveTo(pos,alterMovePos);
//                } else {    // ga ada yang aman samsek
//                    int i = rand.nextInt(surroundCell.size());
//                    alterMovePos.x = surroundCell.get(i).x;
//                    alterMovePos.y = surroundCell.get(i).y;
//                    return digAndMoveTo(pos,alterMovePos);
//                }
            }
        }
    }

   private Command positioning() {
       Worm[] listPlayerWorms = gameState.myPlayer.worms;
       List<Position> friendWormsPos = new ArrayList<Position>();
       for (int i = 0; i < listPlayerWorms.length; i++) {
           if (currentWorm.id != listPlayerWorms[i].id && listPlayerWorms[i].alive()) {  // Bukan worm sekarang
                friendWormsPos.add(listPlayerWorms[i].position);
           }
       }
       List<Cell> surroundCell = getSurroundingCells(currentWorm.position.x,currentWorm.position.y);
       List<Cell> possibleCell = new ArrayList<Cell>();
       // Cari yang jaraknya 2 dari setiap temen
       boolean isGood;
       for (int i = 0; i < surroundCell.size(); i++) {
           isGood = true;
           for (int j = 0; j < friendWormsPos.size(); j++) {
               int distance = euclideanDistance(surroundCell.get(i).x,surroundCell.get(i).y,friendWormsPos.get(i).x,friendWormsPos.get(i).y);
                if (distance < 2) {
                    isGood = false;
                }
           }
           if (isGood) {
               possibleCell.add(surroundCell.get(i));
           }
       }
       Position cellPos = new Position();
       if (possibleCell.isEmpty()) {    // Ternyata semua cell jaraknya 2 dari temen
           for (int i = 0; i < surroundCell.size(); i++) {  // Cari yang ada di lineOfSightMusuh
               cellPos.x = surroundCell.get(i).x;
               cellPos.y = surroundCell.get(i).y;
               if (isOnEnemyLineOfSight(cellPos)) {
                   possibleCell.add(surroundCell.get(i));
               }
           }
           if (possibleCell.isEmpty()) {        // Kalau ga ada do nothing aja deh
               return new DoNothingCommand();
           } else {
               cellPos.x = possibleCell.get(0).x;
               cellPos.y = possibleCell.get(0).y;
               return digAndMoveTo(currentWorm.position,cellPos);
           }
       } else { // Ada yang jaraknya 2 nih
           List<Cell> isGoodCell = new ArrayList<Cell>();
           for (int i = 0; i < possibleCell.size(); i++) {
               cellPos.x = possibleCell.get(i).x;
               cellPos.y = possibleCell.get(i).y;
               if (isOnEnemyLineOfSight(cellPos)) {  // Lebih ideal lagi kalau ada di lineofsightmusuh
                   isGoodCell.add(possibleCell.get(i));
               }
           }
           if (isGoodCell.isEmpty()) {  // Kalau ga ada yang ideal
               cellPos.x = possibleCell.get(0).x;
               cellPos.y = possibleCell.get(0).y;
               return digAndMoveTo(currentWorm.position,cellPos);
           } else { // Kalau ini ideal banget
               cellPos.x = isGoodCell.get(0).x;
               cellPos.y = isGoodCell.get(0).y;
               return digAndMoveTo(currentWorm.position,cellPos);
           }
       }

   }

    private List<Position> lineOfSight(Position pos) {
        int range = 4;
        List<Position> directionLine = new ArrayList<>();
        for (Direction direction : Direction.values()) {
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
                Position sight = new Position(coordinateX, coordinateY);
                directionLine.add(sight);
            }
        }
        return directionLine;
    }

    private Position shotPosition(Position pos){
        List<Position> sight = lineOfSight(pos);
        boolean w1 = false, w2 = false, w3 = false;
        Position e1 = GetEnemyPos(1), e2 = GetEnemyPos(2), e3 = GetEnemyPos(3), e;
        for(Position element : sight){
            if(element.equals(e1)){
                w1 = true;
            }
            else if(element.equals(e2)){
                w2 = true;
            }
            else if(element.equals(e3)){
                w3 = true;
            }
        }
        if(w3){
            e = e3;
        }
        else if(w2){
            e = e2;
        }
        else if(w1){
            e = e1;
        }
        else{
            e = null;
        }
        return e;
    }
    // asumsi a_pos sama b_pos udah lurus
    private boolean isFriendlyFire (Position a_pos, Position b_pos) {
        //System.out.println(String.format("a pos: %d %d", a_pos.x, a_pos.y));
        //System.out.println(String.format("b pos: %d %d", b_pos.x, b_pos.y));
        Position c_pos = resolveToPosition(a_pos, b_pos);
        boolean isThere = false;
        while (!c_pos.equals(b_pos) && !isThere) {
            //System.out.println(String.format("c pos: %d %d", c_pos.x, c_pos.y));
            if (c_pos.equals(GetWormPos(1)) || c_pos.equals(GetWormPos(2)) || c_pos.equals(GetWormPos(3))) {
                //System.out.println("found");
                isThere = true;
            }
            else{
                //System.out.println("not found");
                c_pos = resolveToPosition(c_pos, b_pos);
            }
        }
        return isThere;
    }

    private Command basicShot(){
        Position pos = currentWorm.position;
        Position target = shotPosition(pos);
        if(target != null){
            if(!isFriendlyFire(pos, target)){
                Direction dir = resolveDirection(pos, target);
                return new ShootCommand(dir);
            }
        }
        return null;
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

    private PairBomb maxDamageFromBomb(Position pos) {
        int max = 0, range = 5, tempMax, x = pos.x, y = pos.y;
        Position e = null;
        for (int i = x - 5; i <= x + 5; i++) {
            for (int j = y - 5; j <= y + 5; j++) {
                // Don't include the current position
                if (i != x && j != y && isValidCoordinate(i, j) && (euclideanDistance(pos.x, pos.y, i, j) <= range)) {
                    Position e1 = GetEnemyPos(1), e2 = GetEnemyPos(2), e3 = GetEnemyPos(3);
                    tempMax = 0;
                    for(int a = 1; a < 4; a++){
                        if(GetEnemyPos(a) != null){
                            tempMax += bombDamage(GetEnemyPos(a), i, j);
                        }
                        if(GetWormPos(a) != null){
                            tempMax -= bombDamage(GetWormPos(a), i, j);
                        }
                    }
                    if (tempMax > max) {
                        e = new Position(i, j);
                        max = tempMax;
                    }
                }
            }
        }
        return new PairBomb(e, max);
    }

    private int frozenUntil(int ID){
        return gameState.opponents[0].worms[ID-1].frozen;
    }

    private boolean frozen(Position pos,int ID, int i, int j){
        return euclideanDistance(pos.x, pos.y, i, j) < 2 && frozenUntil(3) == 0;
    }

    private PairBomb maxFrozen(Position pos){
        int max = 0, range = 5, tempMax, x = pos.x, y = pos.y;
        Position e = null;
        for (int i = x - 5; i <= x + 5; i++) {
            for (int j = y - 5; j <= y + 5; j++) {
                // Don't include the current position
                if (i != x && j != y && isValidCoordinate(i, j) && (euclideanDistance(pos.x, pos.y, i, j) <= range)) {
                    tempMax = 0;
                    for(int a = 1; a < 4; a++){
                        if(GetEnemyPos(a) != null){
                            if(euclideanDistance(GetEnemyPos(a).x, GetEnemyPos(a).y, i, j) < 2 && frozenUntil(a) == 0) {
                                tempMax += 1;
                            }
                        }
                        if(GetWormPos(a) != null){
                            if(euclideanDistance(GetWormPos(a).x, GetWormPos(a).y, i, j) < 2 && frozenUntil(a) == 0) {
                                tempMax -= 1;
                            }
                        }
                    }
                    if (tempMax > max) {
                        e = new Position(i, j);
                        max = tempMax;
                    }
                }
            }
        }
//<<<<<<< HEAD
        PairBomb pb = new PairBomb(e, max);
        return pb;
    }

//>>>>>>> d1e3a3023d4ff109786a3a41c0083c775e6a9504
//        return max;
//    }

    public modifiedCell[][] shortestRoute(Cell[][] GameMap, Position source){
        //initialize
        modifiedCell[][] Result = new modifiedCell[GameMap.length][GameMap[0].length];
        for(int i = 0; i < GameMap.length;i++ ){
            for(int j = 0; j<GameMap[0].length;j++){
                Result[i][j] = new modifiedCell();
                //System.out.println(i);
                //System.out.println(GameMap[i][j].x);
                //System.out.println(GameMap[i][j].type);
                Result[i][j].deepCopy(GameMap[j][i].x,GameMap[j][i].y, source.x,source.y,false,Integer.MAX_VALUE,GameMap[j][i].type);
            }
        }
        Result[source.x][source.y].distance = 0;
        List<Position> ToBeVisited = new ArrayList<Position>();
        List<Position> TempToBeVisited = new ArrayList<Position>();
        do{
            //initialize source nya
            Result[source.x][source.y].visit = true;
            //starting dijstra
            TempToBeVisited = getAdjacentCell(source.x,source.y,Result); //initialize node yang akan di visit disekitar sumber
            for(int i = 0;i<TempToBeVisited.size();i++){
                ToBeVisited.add(TempToBeVisited.get(i));
            }
            for(int i = 0;i<TempToBeVisited.size();i++){
                int CurridxX = TempToBeVisited.get(i).x;
                int CurridxY = TempToBeVisited.get(i).y;
                Result[CurridxX][CurridxY].prev.x = source.x; //initialize prev nya
                Result[CurridxX][CurridxY].prev.y = source.y;
                if (Result[CurridxX][CurridxY].cell.type == CellType.AIR) {
                    Result[CurridxX][CurridxY].distance = Result[Result[CurridxX][CurridxY].prev.x][Result[CurridxX][CurridxY].prev.y].distance + 1;
                } else if (Result[CurridxX][CurridxY].cell.type == CellType.DIRT) {
                    Result[CurridxX][CurridxY].distance = Result[Result[CurridxX][CurridxY].prev.x][Result[CurridxX][CurridxY].prev.y].distance + 2;
                }else{ //deep space
                    Result[CurridxX][CurridxY].distance = Integer.MAX_VALUE;//asumsikan infinite
                }
            }
            int idx = getMinDist(ToBeVisited,Result);
            source.x = ToBeVisited.get(idx).x;
            source.y = ToBeVisited.get(idx).y;
            ToBeVisited.remove(idx);
        }while(ToBeVisited.size()>0);
        /*for(int i = 0;i<ToBeVisited.size();i++) {//initialize ynag pertama
            int CurridxX = ToBeVisited.get(i).x;
            int CurridxY = ToBeVisited.get(i).y;
            Result[CurridxX][CurridxY].prev.x = source.x; //initialize prev nya
            Result[CurridxX][CurridxY].prev.y = source.y;
            if (Result[CurridxX][CurridxY].cell.type == CellType.AIR) {
                Result[CurridxX][CurridxY].distance = Result[Result[CurridxX][CurridxY].prev.x][Result[CurridxX][CurridxY].prev.y].distance + 1;
            } else if (Result[CurridxX][CurridxY].cell.type == CellType.DIRT) {
                Result[CurridxX][CurridxY].distance = Result[Result[CurridxX][CurridxY].prev.x][Result[CurridxX][CurridxY].prev.y].distance + 2;
            }else{ //deep space
                Result[CurridxX][CurridxY].distance = Integer.MAX_VALUE;//asumsikan infinite
            }
        }
        while(ToBeVisited.size() > 0){
            System.out.println(ToBeVisited.size());
            int idx = getMinDist(ToBeVisited,Result); //ambil yang distancenya minimal
            int CurrX = ToBeVisited.get(idx).x;
            int CurrY = ToBeVisited.get(idx).y;
            Result[CurrX][CurrY].visit = true; //ini di visit

            if (Result[CurrX][CurrY].cell.type == CellType.AIR) { //masukin distancenya
                Result[CurrX][CurrY].distance = Result[Result[CurrX][CurrY].prev.x][Result[CurrX][CurrY].prev.y].distance + 1;
            } else if (Result[CurrX][CurrY].cell.type == CellType.DIRT) {
                Result[CurrX][CurrY].distance = Result[Result[CurrX][CurrY].prev.x][Result[CurrX][CurrY].prev.y].distance + 2;
            }else{ //deep space
                Result[CurrX][CurrY].distance = Integer.MAX_VALUE;//asumsikan infinite
            }
            //ToBeVisited.get(idx) = null; //kalkulasi buat cell ini selesai
            ToBeVisited.remove(idx);
            List<Position> TempToBeVisited = new ArrayList<Position>();
            TempToBeVisited = getAdjacentCell(CurrX,CurrY,Result); //ambil semua cell yg adj dg curr cell
            for(int i = 0;i<TempToBeVisited.size();i++) {
                int idxNextX = TempToBeVisited.get(i).x;
                int idxNextY = TempToBeVisited.get(i).y;
                Result[idxNextX][idxNextY].prev.x = TempToBeVisited.get(i).x; //initialize prev nya untuk cell yang adj dg currCell
                Result[idxNextX][idxNextY].prev.y = TempToBeVisited.get(i).y;
            }
            for(int j = 0;j<TempToBeVisited.size();j++){
                ToBeVisited.add(TempToBeVisited.get(j));
                //Result[]
            }

            //ToBeVisited = append(ToBeVisited,getAdjacentCell(CurrX,CurrY,Result)); //nanti buat fungsi merge 2 array somehow
        }*/

        return Result;
    }

    public  List<Position> getAdjacentCell(int x,int y , modifiedCell[][] map) {
        //int x = src.cell.x;
        //int y = src.cell.y;
        //int a[][] = {{x + 1, y + 1}, {x + 1, y}, {x + 1, y - 1}, {x, y + 1}, {x, y - 1}, {x - 1, y + 1}, {x - 1, y}, {x - 1, y - 1}};
        List<Position> a = new ArrayList<Position>();
        a.add(new Position(x+1,y+1));
        a.add(new Position(x+1,y));
        a.add(new Position(x+1,y-1));
        a.add(new Position(x,y+1));
        a.add(new Position(x,y-1));
        a.add(new Position(x-1,y+1));
        a.add(new Position(x-1,y));
        a.add(new Position(x-1,y-1));
        List<Position> Result = new ArrayList<Position>();
        //int j= 0;
        for (int i = 0; i < a.size(); i++) {
            if( a.get(i).x < 33 && a.get(i).y < 33 && a.get(i).x >= 0 &&a.get(i).y >= 0){
                if(map[a.get(i).x][a.get(i).y].distance == Integer.MAX_VALUE && map[a.get(i).x][a.get(i).y].cell.type != CellType.DEEP_SPACE ){
                    Result.add(a.get(i));
                }
            }


        }
        return Result;
    }
    public int getMinDist(List<Position> ToBeVisited,modifiedCell[][] map){
        int min = map[ToBeVisited.get(0).x][ToBeVisited.get(0).y].distance;
        int idxmin = 0;
        for(int i = 1;i< ToBeVisited.size();i++){
            if(map[ToBeVisited.get(i).x][ToBeVisited.get(i).y].distance<min){
                min = map[ToBeVisited.get(i).x][ToBeVisited.get(i).y].distance;
                idxmin = i;
            }
        }
        return idxmin;
    }

    public int[][] removeElmt(int[][] ToBeVisited,int index){
        int[][] temp = new int[ToBeVisited.length-1][2];
        int j = 0;
        for(int i = 0;i<ToBeVisited.length;i++){
            if(i != index){
                temp[j][0] = ToBeVisited[i][0];
                temp[j][1] = ToBeVisited[i][1];
                j++;
            }
        }
        return temp;
    }
    public Command HuntAndKill(){
        Command command =  basicShot();
        if(command == null){
            List<Position> EnemyinRange = new ArrayList<Position>();
            for(int i = 0;i<3;i++){
                List<Position> TempEnemyinRang = lineOfSight(opponent.worms[i].position);
                for(int j = 0;j< TempEnemyinRang.size();j++){
                    EnemyinRange.add(EnemyinRange.get(j));
                }
            }
            modifiedCell[][] Map = shortestRoute(gameState.map,getCurrentWorm(gameState).position);
            Position GoTo = GetMinDistanceFromArray(Map,EnemyinRange);
            return ImprovedDigAndMoveTo(getCurrentWorm(gameState).position,GoTo);
        }else{
            return command;
        }


    }

    public Position GetMinDistanceFromArray(modifiedCell[][] Map, List<Position> EnemyinRange){
        int min = Map[getShortestFirstRoute(Map,EnemyinRange.get(0)).x][ getShortestFirstRoute(Map,EnemyinRange.get(0)).y].distance;
        Position Result = new Position(getShortestFirstRoute(Map,EnemyinRange.get(0)).x,getShortestFirstRoute(Map,EnemyinRange.get(0)).y);
        for(int i = 1;i<EnemyinRange.size();i++){
            int tempmin = Map[getShortestFirstRoute(Map,EnemyinRange.get(i)).x][ getShortestFirstRoute(Map,EnemyinRange.get(i)).y].distance;
            if(tempmin < min){
                min = tempmin;
                Result.x = getShortestFirstRoute(Map,EnemyinRange.get(i)).x;
                Result.y = getShortestFirstRoute(Map,EnemyinRange.get(i)).y;
            }
        }
        return Result;

    }


    public Position getShortestFirstRoute(modifiedCell[][] Map, Position Target){
        int XTarget = Target.x;
        int YTarget = Target.y;
        modifiedCell currCell = Map[XTarget][YTarget];
        System.out.print("Generating Best Route : ");

        System.out.print(" x:");
        System.out.print(currCell.cell.x);
        System.out.print(" y:");
        System.out.print(currCell.cell.y);
        while(Map[currCell.prev.x][currCell.prev.y].distance != 0){

            currCell = Map[currCell.prev.x][currCell.prev.y];
            System.out.print(" <- ");
            System.out.print(" x:");
            System.out.print(currCell.prev.x);
            System.out.print(" y:");
            System.out.print(currCell.prev.y);

        }
        System.out.println();
        System.out.print("So Go To");
        System.out.print(" x:");
        System.out.print(currCell.cell.x);
        System.out.print(" y: ");
        System.out.print(currCell.cell.y);
        return new Position(currCell.cell.x,currCell.cell.y);
    }

    public Command ImprovedDigAndMoveTo(Position origin, Position destination){
        modifiedCell[][] Map = shortestRoute(gameState.map, origin);
        Position goTo = getShortestFirstRoute(Map,destination);

        return digAndMoveTo(origin, goTo);
    }


}
