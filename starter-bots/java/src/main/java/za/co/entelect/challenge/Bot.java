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
//        return Arrays.stream(gameState.myPlayer.worms)
//                .filter(myWorm -> myWorm.id == gameState.currentWormId)
//                .findFirst()
//                .get();
        return gameState.myPlayer.worms[gameState.currentWormId-1];
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
        int countEnemyAlive = countEnemyAlive();
        System.out.println("--1--");
        Worm[] listPlayerWorms = gameState.myPlayer.worms;
        if (currentWorm.health <= 20 && frozenUntil(false,currentWorm.id) == 0) {  // Sekarat?
            System.out.println("--2--");
            if (currentWorm.id == 2) { // Bomber
                if (currentWorm.bananaBomb.count > 0) {
                    PairBomb pb = maxDamageFromBomb(currentWorm.position);
                    if (pb.pos != null && pb.damage > 0) {
                        return new ThrowBananaCommand(pb.pos.x, pb.pos.y);
                    }
                }
            } else if (currentWorm.id == 3) {  // Snowballer
                if (getCurrentWorm(gameState).snowballs.count > 0) {
                    PairBomb pb = maxFrozen(currentWorm.position);
                    if (pb.pos != null && pb.damage > 0) {
                        return new ThrowSnowballCommand(pb.pos.x, pb.pos.y);
                    }
                }
            }
        } // KALAU CURRENT GA SEKARAT
        if (listPlayerWorms[1].alive() && listPlayerWorms[1].health <= 30 && frozenUntil(false,listPlayerWorms[1].id) == 0) {
            System.out.println("--3--");
            if (gameState.myPlayer.token > 0) { // Ganti orang
                if (listPlayerWorms[1].bananaBomb.count > 0) {
                    PairBomb pb = maxDamageFromBomb(listPlayerWorms[1].position);
                    if (pb.pos != null && pb.damage > 0) {
                        return new UseToken(2,new ThrowBananaCommand(pb.pos.x, pb.pos.y));
                    }
                }
            }
        }
        if (listPlayerWorms[2].alive() && listPlayerWorms[2].health <= 20 && frozenUntil(false,listPlayerWorms[2].id) == 0) {
            System.out.println("--4--");
            if (gameState.myPlayer.token > 0) { // Ganti orang
                if (listPlayerWorms[2].snowballs.count > 0) {
                    PairBomb pb = maxFrozen(listPlayerWorms[2].position);
                    if (pb.pos != null && pb.damage > 0) {
                        return new UseToken(3,new ThrowSnowballCommand(pb.pos.x, pb.pos.y));
                    }
                }
            }
        }
        if(isWar()){    // SEDANG PERANG
            if (currentWorm.id == 1) {
                System.out.println("--5--");
                Command cmd = basicShot();
                if (cmd != null) {
                    return cmd;
                } else {
                    return positioning();
                }
            } if (currentWorm.id == 2) {
                System.out.println("--6--");
                if (currentWorm.bananaBomb.count > 0) {
                    PairBomb pb = maxDamageFromBomb(currentWorm.position);
                    if (pb.pos != null && pb.damage >= 10*countEnemyAlive) {
                        return new ThrowBananaCommand(pb.pos.x, pb.pos.y);
                    }
                }
                Command cmd = basicShot();
                if (cmd != null) {
                    return cmd;
                } else {
                    return positioning();
                }

            } if (currentWorm.id == 3) {  // Snowballer
                System.out.println("--7--");
                if (getCurrentWorm(gameState).snowballs.count > 0) {
                    if (onBattle(1) || onBattle(2) || onBattle(3)) {
                        PairBomb pb = maxFrozen(currentWorm.position);
                        if (pb.pos != null && pb.damage > countEnemyAlive/2) {
                            return new ThrowSnowballCommand(pb.pos.x, pb.pos.y);
                        }
                    }
                }
                Command cmd = basicShot();
                if (cmd != null) {
                    return cmd;
                } else {
                    return positioning();
                }

            }
            System.out.println("Something error in War");
            System.out.println("Something error in War");
            System.out.println("Something error in War");
            return HuntAndKill();
        }
        else {  // NOT IN WAR
            int idEnemy = 1;
            if (getCurrentWorm(gameState).id == 2 || getCurrentWorm(gameState).id == 3) {
                // EDIT YANG INI
                if(currentWorm.id == 3){
//                    if (onBattle(2)  || euclideanDistance(currentWorm.position.x,currentWorm.position.y,opponent.worms[idEnemy-1].position.x,opponent.worms[idEnemy-1].position.y) < 6) {
//                        if (currentWorm.snowballs.count > 3) {
//                            PairBomb pb = maxFrozen(currentWorm.position);
//                            if (pb.pos != null && pb.damage > 0) {
//                                return new ThrowSnowballCommand(pb.pos.x, pb.pos.y);
//                            }
//                        }
//                    }
                }
                if (getCurrentWorm(gameState).id == 2) {
                    if (currentWorm.bananaBomb.count > 0) {
                        PairBomb pb = maxDamageFromBomb(currentWorm.position);
                        if (pb.pos != null && pb.damage >= 10 * countEnemyAlive) {
                            return new ThrowBananaCommand(pb.pos.x, pb.pos.y);
                        }
                    }
                }
                System.out.println("-2-----------");

                Command com = basicShot();
                if (com != null) {
                    System.out.println("-0.5-----------");
                    return com;
                }

                System.out.println("Gank Target");
                if (GetEnemyPos(idEnemy) != null) {
                    System.out.println("Execute improved dig and move to");
                    return ImprovedDigAndMoveTo(currentWorm.position, GetEnemyPos(idEnemy));
                }

                System.out.println("1-----------");
                if (isGroup()) { // harusnya grouping
                    System.out.println("2-----------");
                    if(currentWorm.id == 3){
                        if (onBattle(1) || onBattle(2) || onBattle(3)) {
                            if (getCurrentWorm(gameState).snowballs.count > 0 && frozenUntil(false,currentWorm.id) == 0) {
                                PairBomb pb = maxFrozen(currentWorm.position);
                                if (pb.pos != null && pb.damage > 0) {
                                    return new ThrowSnowballCommand(pb.pos.x, pb.pos.y);
                                }
                            }
                        }
                    }
                    if (currentWorm.id == 2) {
                        if (currentWorm.bananaBomb.count > 0 && frozenUntil(false,currentWorm.id) == 0) {
                            PairBomb pb = maxDamageFromBomb(currentWorm.position);
                            if (pb.pos != null && pb.damage >= 10 * countEnemyAlive) {
                                return new ThrowBananaCommand(pb.pos.x, pb.pos.y);
                            }
                        }
                    }
                    return HuntAndKill();
                }
                System.out.println("3-----------");

                return Regroup();

            } else if (getCurrentWorm(gameState).id == 1) { // Yang ketengah


                List<Position> powerPos =  getPowerUp();
                if (!powerPos.isEmpty()) {
                    for (int i = 0; i < powerPos.size(); i++) {
                        if ((euclideanDistance(currentWorm.position.x,currentWorm.position.y, powerPos.get(i).x, powerPos.get(i).y) < 2)) {
                            System.out.println("Cari power");
                            Position pwPos = new Position(powerPos.get(i).x,powerPos.get(i).y);
                            return ImprovedDigAndMoveTo(currentWorm.position, pwPos);
                        }
                    }
                }

//                if (currentWorm.bananaBomb.count > 0) {
//                    PairBomb pb = maxDamageFromBomb(currentWorm.position);
//                    if (pb.pos != null && pb.damage >= 10*countEnemyAlive) {
//                        return new ThrowBananaCommand(pb.pos.x, pb.pos.y);
//                    }
//                }
                if(isGroup()){
                    return HuntAndKill();
                }
                return Regroup();
            }
        }
        System.out.println("------------------------MASIH SALAH------------------------------");
        System.out.println("------------------------MASIH SALAH------------------------------");
        System.out.println("------------------------MASIH SALAH------------------------------");
        System.out.println("------------------------MASIH SALAH------------------------------");
        System.out.println("------------------------MASIH SALAH------------------------------");
        return new DoNothingCommand();
    }

    private int countEnemyAlive() {
        Worm[] listEnemyWorms = opponent.worms;
        int countAlive = 0;

        for (int i = 0; i < listEnemyWorms.length; i++) {
            if (listEnemyWorms[i].alive()) {
                countAlive++;
            }
        }
        return countAlive;
    }

    private boolean isThereAlliesEnemyNear() {
        Worm[] listEnemyWorms = opponent.worms;
        Worm[] listAlliesWorms = gameState.myPlayer.worms;
        boolean isThere1 = false;
        boolean isThere2 = false;
        int distance = 6;
        // Cek apakah ada musuh dekat?
        for (int i = 0; i < listEnemyWorms.length && !isThere1; i++) {
            if (listEnemyWorms[i].alive()) {
                distance = euclideanDistance(currentWorm.position.x,currentWorm.position.y,listEnemyWorms[i].position.x,listEnemyWorms[i].position.y);
                if (distance < 4) {
                    isThere1 = true;
                }
            }
        }

        // Cek apakah ada teman dekat
        for (int i = 0; i < listAlliesWorms.length && !isThere2; i++) {
            if (listAlliesWorms[i].alive() && i+1 != currentWorm.id) {
                distance = euclideanDistance(currentWorm.position.x,currentWorm.position.y,listAlliesWorms[i].position.x,listAlliesWorms[i].position.y);
                if (distance < 4) {
                    isThere2 = true;
                }
            }
        }
        return isThere1 && isThere2;

    }


    private boolean isWar() {
        //int countAlive = countEnemyAlive();
        boolean onWar = false;

        if (isThereAlliesEnemyNear()) {
            onWar = true;
        }
        return onWar;
    }

    private boolean onBattle(int ID) {
        Worm[] friendWorms = gameState.myPlayer.worms;
        boolean on = false;
        if (friendWorms[ID - 1].alive()) {
            Command cmd = basicShot(ID);
            if (cmd != null) {
                on = true;
            }
        }
        return on;
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

    private boolean isCellOccupied(Position pos) {
        boolean occupied = false;
        Worm[] listPlayerWorms = gameState.myPlayer.worms;
        Worm[] listEnemyWorms = opponent.worms;

        for (int i = 0; i < listPlayerWorms.length && !occupied; i++) {
            if (pos.x == listPlayerWorms[i].position.x && pos.y == listPlayerWorms[i].position.y) {
                occupied = true;
            }
        }
        for (int i = 0; i < listEnemyWorms.length && !occupied; i++) {
            if (pos.x == listEnemyWorms[i].position.x && pos.y == listEnemyWorms[i].position.y) {
                occupied = true;
            }
        }


        return occupied;
    }

    private Command digAndMoveTo(Position origin, Position destination) {
        if(origin == null || destination == null){
            return new DoNothingCommand();
        }
        Position nextPosition = resolveToPosition(origin,destination);

        MyWorm[] worms = gameState.myPlayer.worms;
        boolean canMove = !isCellOccupied(nextPosition);

//        for (int i = 0; i < worms.length; i++) {
//            if (worms[i].position.equals(nextPosition)) {
//                canMove = false;
//            }
//        }

        if (!isValidCoordinate(nextPosition.x, nextPosition.y)) {
            canMove = false;
        }

        if (canMove) {
            Cell nextCell = findCell(nextPosition);
//            Command cmd;
//            System.out.println("can move");

            if (nextCell.type == CellType.AIR || nextCell.type == CellType.LAVA) {
//                System.out.println(nextCell.type);
                return new MoveCommand(nextPosition.x,nextPosition.y);
            } else if (nextCell.type == CellType.DIRT) {
                return new DigCommand(nextPosition.x,nextPosition.y);
            } else {
                return new DoNothingCommand();
            }
        }
//        System.out.println("can't move");
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
        } else if (cdir_x <= -0.5) {
            dir.x = -1;
        } else {
            dir.x = 0;
        }
        // Solve yang y
        if (cdir_y > -0.5 && cdir_y < 0.5) {
            dir.y = 0;
        } else if (cdir_y >= 0.5) {
            dir.y = 1;
        } else if (cdir_y <= -0.5) {
            dir.y = -1;
        } else {
            dir.y = 0;
        }

        return dir;
    }
    
    // Cek apakah ada dirt sepanjang jarak tembak
    private boolean isThereAnyObstacle (Position a_pos, Position b_pos) {
//        Position dif = new Position();
//        dif.x = b_pos.x - a_pos.x;
//        dif.y = b_pos.y - a_pos.y;
//
//        Position dir = normalizeVector(dif);

        Position c_pos = new Position();
        c_pos = resolveToPosition(a_pos,b_pos);
//        c_pos.x = a_pos.x + dir.x;
//        c_pos.y = a_pos.y + dir.y;
        boolean isThere = false;
//        System.out.println("---");
//        System.out.println(a_pos.x);
//        System.out.println(a_pos.y);
//        System.out.println("x^y>");
//        System.out.println(b_pos.x);
//        System.out.println(b_pos.y);
//        System.out.println("x^y>");
//        System.out.println(c_pos.x);
//        System.out.println(c_pos.y);
//        System.out.println("---");

        Worm[] listFriendWorms = gameState.myPlayer.worms;
        while ((c_pos.x != b_pos.x || c_pos.y != b_pos.y) && !isThere) {
            if (findCell(c_pos).type == CellType.DIRT) {
                isThere = true;
            }
            for (int i = 0; i < listFriendWorms.length && !isThere; i++) {
                if (listFriendWorms[i].position.x == c_pos.x && listFriendWorms[i].position.y == c_pos.y) {
                    if (listFriendWorms[i].alive()) {
                        isThere = true;
                    }
                }
            }
//            System.out.println(c_pos.x);
//            System.out.println(c_pos.y);
//            System.out.println(isThere);

            c_pos = resolveToPosition(c_pos,b_pos);
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
                if (distance > range || isThereAnyObstacle(pos,enemyPos)) { // Cek lagi bakal kena ga
                    isOn = false;
                }


            }
//            System.out.println(isOn);
        }
        return isOn;
    }

    private boolean isSaveToEscape(Position movePos) {
        return !isOnEnemyLineOfSight(movePos) && findCell(movePos).type != CellType.DIRT;
    }

    // Pastikan musuh sudah banyak didekat kita
    private Command retreat() {
        System.out.println("Retreat");
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

//        // Tambah Vektor Posisi teman terdekat
//        int distance = 999999;
//        int idx = -1;
//        Worm[] listPlayerWorms = gameState.myPlayer.worms;
//        for (int i = 0; i < listPlayerWorms.length; i++) {
//            if (currentWorm.id != listPlayerWorms[i].id) {  // Bukan worm sekarang
//                if (listPlayerWorms[i].alive()) {
//                    int c_distance = euclideanDistance(currentWorm.position.x,currentWorm.position.y,listPlayerWorms[i].position.x,listPlayerWorms[i].position.y);
//                    if (c_distance < distance) {
//                        distance = c_distance;
//                        idx = i;
//                    }
//                }
//            }
//        }
//        if (idx != -1) {
//            Position vectorPosFriend = new Position();
//            vectorPosFriend.x = listPlayerWorms[idx].position.x - pos.x;
//            vectorPosFriend.y = listPlayerWorms[idx].position.y - pos.y;
//            vectorPos.add(vectorPosFriend);
//        }

        // Tambah Vektor Kecenderungan Bergerak Memutar
        Position vectorPosCenterMap = new Position();
        int rMultiplier = 1;
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
        if (isSaveToEscape(movePos) && totalDir.x != 0 && totalDir.y != 0) {
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
                if (isSaveToEscape(alterMovePos) && !isCellOccupied(alterMovePos)) {
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
//                return digAndMoveTo(pos,movePos);

                for (int i = 0; i < surroundCell.size(); i++) {
                    alterMovePos.x = surroundCell.get(i).x;
                    alterMovePos.y = surroundCell.get(i).y;
                    if (!isOnEnemyLineOfSight(alterMovePos) && !isCellOccupied(alterMovePos)) {
                        cellAman.add(surroundCell.get(i));
                    }
                }
                if (!cellAman.isEmpty()) { // ada yang aman aja
                    int i = rand.nextInt(cellAman.size());
                    alterMovePos.x = cellAman.get(i).x;
                    alterMovePos.y = cellAman.get(i).y;
                    return digAndMoveTo(pos,alterMovePos);
                } else {    // ga ada yang aman samsek
                    int i = rand.nextInt(surroundCell.size());
                    alterMovePos.x = surroundCell.get(i).x;
                    alterMovePos.y = surroundCell.get(i).y;
                    return digAndMoveTo(pos,alterMovePos);
//                    return digAndMoveTo(pos,movePos);
                }
            }
        }
    }

   private Command positioning() {
       System.out.println("Calling positioning");
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
           for (int j = 0; j < friendWormsPos.size() && isGood; j++) {
               int distance = euclideanDistance(surroundCell.get(i).x,surroundCell.get(i).y,friendWormsPos.get(j).x,friendWormsPos.get(j).y);
//                if (distance < 2 || surroundCell.get(i).type == CellType.DIRT) {
//                    isGood = false;
//                }
               if (distance < 2) {
                   isGood = false;
               }
           }
           if (isGood) {
//               System.out.println("Add possible cell");
//               System.out.println(surroundCell.get(i).x);
//               System.out.println(surroundCell.get(i).y);
               possibleCell.add(surroundCell.get(i));
           }
       }
       Position cellPos = new Position();
       Random rand = new Random();
       if (possibleCell.isEmpty()) {    // Ternyata semua cell jaraknya 2 dari temen
           System.out.println("---p1----");
           for (int i = 0; i < surroundCell.size(); i++) {  // Cari yang ada di lineOfSightMusuh
               cellPos.x = surroundCell.get(i).x;
               cellPos.y = surroundCell.get(i).y;
//               System.out.println("Check occupied");
//               System.out.println(isCellOccupied(cellPos));
               if (isOnEnemyLineOfSight(cellPos) && !isCellOccupied(cellPos)) {
                   possibleCell.add(surroundCell.get(i));
               }
           }
           if (possibleCell.isEmpty()) {        // Kalau ga gerak random aja
               System.out.println("Call DoNothing");
               return HuntAndKill();
//               return Regroup();
//
//               Worm[] enemyWorms = opponent.worms;
//               for (int i = 0; i < enemyWorms.length; i++) {
//                   if (enemyWorms[i].alive()) {
//                       return digAndMoveTo(currentWorm.position,resolveToPosition(currentWorm.position,enemyWorms[i].position));
//                   }
//               }
//               return new DoNothingCommand();

           } else {
               int i = rand.nextInt(possibleCell.size());
               cellPos.x = possibleCell.get(i).x;
               cellPos.y = possibleCell.get(i).y;
               return digAndMoveTo(currentWorm.position,cellPos);
           }
       } else { // Ada yang jaraknya 2 nih
           System.out.println("---p2----");
           List<Cell> isGoodCell = new ArrayList<Cell>();
           for (int i = 0; i < possibleCell.size(); i++) {
               cellPos.x = possibleCell.get(i).x;
               cellPos.y = possibleCell.get(i).y;
               if (isOnEnemyLineOfSight(cellPos)) {  // Lebih ideal lagi kalau ada di lineofsightmusuh
//                   System.out.println(cellPos.x);
//                   System.out.println(cellPos.y);
                   isGoodCell.add(possibleCell.get(i));
               }
           }
           if (isGoodCell.isEmpty()) {  // Kalau ga ada yang ideal
               System.out.println("Call Random");
//               int i = rand.nextInt(possibleCell.size());
//               cellPos.x = possibleCell.get(i).x;
//               cellPos.y = possibleCell.get(i).y;
//               System.out.println(cellPos.x);
//               System.out.println(cellPos.y);
//               return digAndMoveTo(currentWorm.position,cellPos);
               return HuntAndKill();
           } else { // Kalau ini ideal banget
               System.out.println("Call Ideal");
//               return Regroup();
               int i = rand.nextInt(isGoodCell.size());

               cellPos.x = isGoodCell.get(i).x;
               cellPos.y = isGoodCell.get(i).y;
               System.out.println(isOnEnemyLineOfSight(cellPos));
               System.out.println(cellPos.x);
               System.out.println(cellPos.y);
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

    private List<Position> modifiedLineOfSight(Position pos) {
        int range = 4;
        boolean obs = false;
        List<Position> directionLine = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            obs = false;
            for (int directionMultiplier = 1; directionMultiplier <= range && !obs; directionMultiplier++) {

                int coordinateX = pos.x + (directionMultiplier * direction.x);
                int coordinateY = pos.y + (directionMultiplier * direction.y);

                if (!isValidCoordinate(coordinateX, coordinateY)) {
                    break;
                }


//               Position coordinate = new Position(pos.x, pos.y);
//               if (isCellOccupied(coordinate)) {
//                   break;
//               }

                if (euclideanDistance(pos.x, pos.y, coordinateX, coordinateY) > range) {
                    break;
                }
                Cell cell = gameState.map[coordinateY][coordinateX];
                if (cell.type == CellType.DIRT) {
                    break;
                }

                Position sight = new Position(coordinateX, coordinateY);
                for(int a = 1; a < 4 && !obs; a++){
                    if(sight.equals(GetWormPos(a))){
                        obs = true;
                    }
                    if(sight.equals(GetEnemyPos(a))){
                        obs = true;
                    }
                }
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

    private Command basicShot(int ID){
        Position pos = gameState.myPlayer.worms[ID-1].position;
        Position target = shotPosition(pos);
        if(target != null){
            if(!isFriendlyFire(pos, target)){
                Direction dir = resolveDirection(pos, target);
                return new ShootCommand(dir);
            }
        }
        return null;
    }

    private List<Worm> countEnemy(Position pos) {
        int max = 0, range = 5, tempMax, x = pos.x, y = pos.y, count = 0;
        Position e = null, e1 = GetEnemyPos(1), e2 = GetEnemyPos(2), e3 = GetEnemyPos(3);
        List<Worm> worm = new ArrayList<>();
        for (int i = x - 5; i <= x + 5; i++) {
            for (int j = y - 5; j <= y + 5; j++) {
                // Don't include the current position
                if (i != x && j != y && isValidCoordinate(i, j) && (euclideanDistance(pos.x, pos.y, i, j) <= range)) {
                    if (e1 != null && e1.x == i && e1.y == j) {
                        //count += 1;
                        worm.add(gameState.opponents[0].worms[0]);
                    }
                    if (e2 != null && e2.x == i && e2.y == j) {
                        //count += 1;
                        worm.add(gameState.opponents[0].worms[1]);
                    }
                    if (e3 != null && e3.x == i && e3.y == j) {
                        //count += 1;
                        worm.add(gameState.opponents[0].worms[2]);
                    }
                }
            }
        }
        return worm;
    }

    private int bombDamage (Position e3,int i, int j){
        if (e3.x == i && e3.y == j) {
            return 20;
        } else if (Math.abs(i - e3.x) + Math.abs(j - e3.y) == 1) {
            return 13;
        } else if (Math.abs(i - e3.x) == 1 && Math.abs(j - e3.y) == 1) {
            return 11;
        } else if (Math.abs(i - e3.x) + Math.abs(j - e3.y) == 2) {
            return 7;
        } else {
            return 0;
        }
    }

    private PairBomb maxDamageFromBomb(Position pos) {
        if(frozenUntil(false, 2) > 0){
            return new PairBomb(null, 0);
        }
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

    private int frozenUntil(boolean enemy, int ID){
        if(enemy){
            return gameState.opponents[0].worms[ID-1].frozen;
        }
        else {
            return gameState.myPlayer.worms[ID-1].frozen;
        }
    }

    private boolean frozen(Position pos,int ID, int i, int j){
        return euclideanDistance(pos.x, pos.y, i, j) < 2 && frozenUntil(true, ID) == 0;
    }

    private PairBomb maxFrozen(Position pos){
        if(frozenUntil(false, 3) > 0){
            return new PairBomb(null, 0);
        }
        int max = 0, range = 5, tempMax, x = pos.x, y = pos.y, i, j;
        Position e = null;
        for(int b = 0; b<3; b++){ // prioritas lempar tepat ke posisi musuh
            i = gameState.opponents[0].worms[b].position.x;
            j = gameState.opponents[0].worms[b].position.y;
            if(gameState.opponents[0].worms[b].alive() && (euclideanDistance(pos.x, pos.y, i, j) <= range)){
                tempMax = 0;
                for(int a = 1; a < 4; a++){
                    if(GetEnemyPos(a) != null){
                        if(euclideanDistance(GetEnemyPos(a).x, GetEnemyPos(a).y, i, j) < 2 && frozenUntil(true, a) == 0) {
                            tempMax += 1;
                        }
                    }
                    if(GetWormPos(a) != null){
                        if(euclideanDistance(GetWormPos(a).x, GetWormPos(a).y, i, j) < 2 && frozenUntil(false, a) == 0) {
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
        for (i = x - 5; i <= x + 5; i++) {
            for (j = y - 5; j <= y + 5; j++) {
                // Don't include the current position
                if (i != x && j != y && isValidCoordinate(i, j) && (euclideanDistance(pos.x, pos.y, i, j) <= range)) {
                    tempMax = 0;
                    for(int a = 1; a < 4; a++){
                        if(GetEnemyPos(a) != null){
                            if(euclideanDistance(GetEnemyPos(a).x, GetEnemyPos(a).y, i, j) < 2 && frozenUntil(true, a) == 0) {
                                tempMax += 1;
                            }
                        }
                        if(GetWormPos(a) != null){
                            if(euclideanDistance(GetWormPos(a).x, GetWormPos(a).y, i, j) < 2 && frozenUntil(false, a) == 0) {
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
        PairBomb pb = new PairBomb(e, max);
        return pb;
    }



    public modifiedCell[][] shortestRoute(Cell[][] GameMap, Position Source){
        Position source = new Position(Source.x, Source.y);
//        System.out.println("Generating Map");
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

//        System.out.println("Generating Map1");
//       System.out.println("Generating Map2");

        List<Position> PosAlly = new ArrayList<Position>();
        for(int i = 0;i< gameState.myPlayer.worms.length;i++){
            if(gameState.myPlayer.worms[i].alive()){
                PosAlly.add(gameState.myPlayer.worms[i].position);
            }
        }
        //List<Position> PosEnemy = new ArrayList<Position>();
        for(int i = 0;i< gameState.opponents[0].worms.length;i++){
            if(gameState.opponents[0].worms[i].alive()){
                PosAlly.add(gameState.opponents[0].worms[i].position);
            }
        }

        Result[source.x][source.y].distance = 0;
        List<Position> ToBeVisited = new ArrayList<Position>();
        List<Position> TempToBeVisited = new ArrayList<Position>();
        do{
            //System.out.println(ToBeVisited.size());
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
                } else if (Result[CurridxX][CurridxY].cell.type == CellType.DIRT || Result[CurridxX][CurridxY].cell.type == CellType.LAVA) {
                    Result[CurridxX][CurridxY].distance = Result[Result[CurridxX][CurridxY].prev.x][Result[CurridxX][CurridxY].prev.y].distance + 2;
                }else{ //deep space
                    Result[CurridxX][CurridxY].distance = Integer.MAX_VALUE;//asumsikan infinite
                }
                for(int k = 0;k< PosAlly.size();k++){
                    if(PosAlly.get(k).x == CurridxX && PosAlly.get(k).y == CurridxY){
                        Result[CurridxX][CurridxY].distance = 999;
                        //Result[CurridxX][CurridxY].cell.type = CellType.DEEP_SPACE;
                    }
                }
            }
            int idx = getMinDist(ToBeVisited,Result);
            source.x = ToBeVisited.get(idx).x;
            source.y = ToBeVisited.get(idx).y;
            ToBeVisited.remove(idx);
        }while(ToBeVisited.size()>0);
//        System.out.println(source.x);
//        System.out.println(source.y);
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
        System.out.println("HuntAndKill");
        Command command =  basicShot();
        if(command == null){
            List<Position> EnemyinRange = new ArrayList<Position>();
            for(int i = 0;i<3;i++){
                if(GetEnemyPos(i+1) != null){
                    List<Position> TempEnemyinRang = modifiedLineOfSight(opponent.worms[i].position);
                    for(int j = 0;j< TempEnemyinRang.size();j++){
                        EnemyinRange.add(TempEnemyinRang.get(j));
                    }
                }
            }
            System.out.print("enemy size: ");
            System.out.println(EnemyinRange.size());
            modifiedCell[][] Map = shortestRoute(gameState.map,getCurrentWorm(gameState).position);
            Position GoTo = GetMinDistanceFromArray(Map,EnemyinRange);
            return ImprovedDigAndMoveTo(getCurrentWorm(gameState).position,GoTo);
        }else{
            return command;
        }


    }

    public Position GetMinDistanceFromArray(modifiedCell[][] Map, List<Position> EnemyinRange){
        System.out.println("getmindistance");
        if(EnemyinRange.size() > 0){
            System.out.println("size > 0");
            int min = Map[EnemyinRange.get(0).x][EnemyinRange.get(0).y].distance;
            Position Result = new Position(EnemyinRange.get(0).x,EnemyinRange.get(0).y);
            for(int i = 1;i<EnemyinRange.size();i++){
                int tempmin = Map[EnemyinRange.get(i).x][EnemyinRange.get(i).y].distance;
                if(tempmin < min){
                    min = tempmin;
                    Result.x = EnemyinRange.get(i).x;
                    Result.y = EnemyinRange.get(i).y;
                }
            }
//            System.out.println(Result.x);
//            System.out.println(Result.y);
            return Result;
        }
        System.out.println("Pos : -1 -1");
        return new Position(-1, -1);
    }


    public Position getShortestFirstRoute(modifiedCell[][] Map, Position Target){
        int XTarget = Target.x;
        int YTarget = Target.y;
        modifiedCell currCell = new modifiedCell();
        currCell.deepCopy(Map[XTarget][YTarget]);
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
        System.out.println();
        return new Position(currCell.cell.x,currCell.cell.y);
    }

    public Command ImprovedDigAndMoveTo(Position origin, Position destination){
        modifiedCell[][] Map = shortestRoute(gameState.map, origin);
        Position nextPosition = getShortestFirstRoute(Map,destination);
        Cell nextCell = gameState.map[nextPosition.y][nextPosition.x];

        if (nextCell.type == CellType.AIR) {
            System.out.println(nextCell.type);
            return new MoveCommand(nextPosition.x,nextPosition.y);
        } else if (nextCell.type == CellType.DIRT) {
            return new DigCommand(nextPosition.x,nextPosition.y);
        } else {
            System.out.println("DEEp SPaCe?");
            System.out.println(nextPosition.x);
            System.out.println(nextPosition.y);
            return new DoNothingCommand();
        }
        //return digAndMoveTo(origin, goTo);
    }


    public List<Position> getPowerUp(){
        List<Position> powerUp = new ArrayList<Position>();
        for (int i = 0;i<gameState.mapSize;i++){
            for(int j = 0;j<gameState.mapSize;j++){
                if(gameState.map[i][j].powerUp != null){
                    powerUp.add(new Position(j,i));
                }
            }
        }
        return powerUp;
    }

    private boolean isGroup(){
        Position f1 = GetWormPos(1), f2 = GetWormPos(2), f3 = GetWormPos(3);
        int total = 0, totalX = 0, totalY = 0;
        if(f1 != null){
            total ++;
            totalX += f1.x;
            totalY += f1.y;
        }
        if(f2 != null){
            total ++;
            totalX += f2.x;
            totalY += f2.y;
        }
        if(f3 != null){
            total ++;
            totalX += f3.x;
            totalY += f3.y;
        }
        Position center = new Position(totalX/total, totalY/total);
        boolean group = true;
        if(f1 != null && euclideanDistance(f1.x, f1.y, center.x, center.y) > 2){
            group = false;
        }
        if(f2 != null && euclideanDistance(f2.x, f2.y, center.x, center.y) > 2){
            group = false;
        }
        if(f3 != null && euclideanDistance(f3.x, f3.y, center.x, center.y) > 2){
            group = false;
        }
        return group;
    }

    public Command Regroup(){
        Position e1 = GetWormPos(1), e2 = GetWormPos(2), e3 = GetWormPos(3);
        List<modifiedCell[][]> mC = new ArrayList<>();
        if(e1 != null){
            mC.add(shortestRoute(gameState.map, e1));
        }
        if(e2 != null){
            mC.add(shortestRoute(gameState.map, e2));
        }
        if(e3 != null){
            mC.add(shortestRoute(gameState.map, e3));
        }
        if(mC.size() == 1){
            return HuntAndKill();
        }
        Position Center = new Position(0,0);
        int minDistance = 0;
        for(modifiedCell[][] mc : mC){
            if(minDistance < mc[0][0].distance){
                minDistance = mc[0][0].distance;
            }
            //minDistance += mc[0][0].distance;
        }
        for(int i = 0; i<gameState.mapSize;i++){
            for(int j = 0;j<gameState.mapSize;j++){
                int TempMinDistance = 0;
                for(modifiedCell[][] mc : mC){
                    if(TempMinDistance < mc[i][j].distance){
                        TempMinDistance = mc[i][j].distance;
                    }
                    //TempMinDistance += mc[i][j].distance;
                }
                if(TempMinDistance < minDistance){
                    Center.x = i;
                    Center.y = j;
                    minDistance = TempMinDistance;
                }
            }
        }
        return(ImprovedDigAndMoveTo(getCurrentWorm(gameState).position,Center));
    }

}
