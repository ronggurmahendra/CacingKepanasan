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

    /* Mengembalikan worm yang akan beraksi */
    private MyWorm getCurrentWorm(GameState gameState) {
        return gameState.myPlayer.worms[gameState.currentWormId-1];
    }

    /* Mengembalikan posisi worm pemain berdasarkan ID nya */
    public Position GetWormPos(int ID){
        Worm worm = gameState.myPlayer.worms[ID-1];
        if(worm.health > 0){
            return worm.position;
        }else{
            return null;
        }
    }

    /* Mengembalikan posisi worm musuh berdasarkan ID nya */
    public Position GetEnemyPos(int ID){
        Worm Enemyworm = gameState.opponents[0].worms[ID-1];
        if(Enemyworm.health > 0){
            return Enemyworm.position;
        }else{
            return null;
        }
    }

    /*
     Bagian utama pemainan, segala aksi dilakukan disini
     Merupakan fungsi solusi
     */
    public Command run(){
        int countEnemyAlive = countEnemyAlive();            // Hitung banyaknya musuh yang masih hidup
        Worm[] listPlayerWorms = gameState.myPlayer.worms;  // List semua worm pemain

        /* Daftar fungsi seleksi */

        /* Fungsi seleksi, pilih jika worm sekarang sedang sekarat dan tidak beku */
        if (currentWorm.health <= 20 && frozenUntil(false,currentWorm.id) == 0) {

            /* Fungsi kelayakan, jika yang sekarat adalah agent dan masih memiliki bananaBomb */
            if (currentWorm.id == 2) {
                if (currentWorm.bananaBomb.count > 0) {
                    PairBomb pb = maxDamageFromBomb(currentWorm.position);
                    if (pb.pos != null && pb.damage > 0) {
                        return new ThrowBananaCommand(pb.pos.x, pb.pos.y);
                    }
                }
            }
            /* Fungsi kelayakan, jika yang sekarat adalah technologist dan masih memiliki snowballs */
            else if (currentWorm.id == 3) {
                if (getCurrentWorm(gameState).snowballs.count > 0) {
                    PairBomb pb = maxFrozen(currentWorm.position);
                    if (pb.pos != null && pb.damage > 0) {
                        return new ThrowSnowballCommand(pb.pos.x, pb.pos.y);
                    }
                }
            }
        }

        /* Fungsi seleksi, pilih jika worm pemain bertipe agent sedang sekarat dan tidak beku */
        if (listPlayerWorms[1].alive() && listPlayerWorms[1].health <= 30 && frozenUntil(false,listPlayerWorms[1].id) == 0) {
            /* Fungsi kelayakan, jika masih memiliki token seleksi dan masih memiliki bananaBomb*/
            if (gameState.myPlayer.token > 0) {
                if (listPlayerWorms[1].bananaBomb.count > 0) {
                    PairBomb pb = maxDamageFromBomb(listPlayerWorms[1].position);
                    if (pb.pos != null && pb.damage > 0) {
                        return new UseToken(2,new ThrowBananaCommand(pb.pos.x, pb.pos.y));
                    }
                }
            }
        }

        /* Fungsi seleksi, pilih jika worm pemain bertipe agent sedang sekarat dan tidak beku */
        if (listPlayerWorms[2].alive() && listPlayerWorms[2].health <= 20 && frozenUntil(false,listPlayerWorms[2].id) == 0) {
            /* Fungsi kelayakan, jika masih memiliki token seleksi dan masih memiliki snowballs */
            if (gameState.myPlayer.token > 0) {
                if (listPlayerWorms[2].snowballs.count > 0) {
                    PairBomb pb = maxFrozen(listPlayerWorms[2].position);
                    if (pb.pos != null && pb.damage > 0) {
                        return new UseToken(3,new ThrowSnowballCommand(pb.pos.x, pb.pos.y));
                    }
                }
            }
        }

        /* Fungsi seleksi, pilih jika keadaan pemain sedang perang */
        if(isWar()){
            /* Fungsi kelayakan, pilih jika worm sekarang commander */
            if (currentWorm.id == 1) {
                Command cmd = basicShot();
                if (cmd != null) {
                    return cmd;
                } else {
                    return positioning();
                }
            }
            /* Fungsi kelayakan, pilih jika worm sekarang agent */
            if (currentWorm.id == 2) {
                /* Fungsi obyektif, masih terdapat bananaBomb */
                if (currentWorm.bananaBomb.count > 0) {
                    PairBomb pb = maxDamageFromBomb(currentWorm.position);
                    if (pb.pos != null && pb.damage >= 10*countEnemyAlive) {
                        return new ThrowBananaCommand(pb.pos.x, pb.pos.y);
                    }
                }
                /* Fungsi obyektif, tidak tersisa terdapat bananaBomb maka tembak biasa */
                Command cmd = basicShot();
                if (cmd != null) {
                    return cmd;
                } else {
                    return positioning();
                }
            }
            /* Fungsi kelayakan, pilih jika worm sekarang technologist */
            if (currentWorm.id == 3) {
                /* Fungsi obyektif, masih terdapat snowballs */
                if (getCurrentWorm(gameState).snowballs.count > 0) {
                    if (onBattle(1) || onBattle(2) || onBattle(3)) {
                        PairBomb pb = maxFrozen(currentWorm.position);
                        if (pb.pos != null && pb.damage > countEnemyAlive/2) {
                            return new ThrowSnowballCommand(pb.pos.x, pb.pos.y);
                        }
                    }
                }
                /* Fungsi obyektif, tidak tersisa terdapat bananaBomb maka tembak biasa */
                Command cmd = basicShot();
                if (cmd != null) {
                    return cmd;
                } else {
                    return positioning();
                }
            }

            System.out.println("---Something error in War---");
            System.out.println("---Something error in War---");
            System.out.println("---Something error in War---");
            /* Default jika terjadi error */
            return HuntAndKill();
        }

        /* Fungsi seleksi, pilih jika keadaan pemain sedang tidak berperang */
        else {
            /* Fungsi kelayakan, ambil powerup jika ada di dekat worm pemain sekarang */
            List<Position> powerPos =  getPowerUp();
            if (!powerPos.isEmpty()) {
                for (int i = 0; i < powerPos.size(); i++) {
                    /* Fungsi objektif, ambil powerup terdekat (berjarak 1 blok) */
                    if ((euclideanDistance(currentWorm.position.x,currentWorm.position.y, powerPos.get(i).x, powerPos.get(i).y) <= 1)) {
                        Position pwPos = new Position(powerPos.get(i).x,powerPos.get(i).y);
                        return ImprovedDigAndMoveTo(currentWorm.position, pwPos);
                    }
                }
            }


            int idEnemy = 1;    // Id worm musuh yang akan di gank (commander)
            /* Fungsi kelayakan, kejar musuh dengan id = idEnemy dengan worm agent dan technologist */
            if (getCurrentWorm(gameState).id == 2 || getCurrentWorm(gameState).id == 3) {

                /* Fungsi objektif, jika worm sekarang techologist maka lempar snowball pada saat keadaan bertarung
                atau mengejar musuh yang tinggal sendiri */
                if(currentWorm.id == 3){
                   if (onBattle(1) || onBattle(2) || onBattle(3) ) {
                       if (countEnemyAlive() == 1 && currentWorm.snowballs.count > 0) {
                           PairBomb pb = maxFrozen(currentWorm.position);
                           if (pb.pos != null && pb.damage > 0) {
                               return new ThrowSnowballCommand(pb.pos.x, pb.pos.y);
                           }
                       }
                   }
                }

                /* Fungsi objektif, jika worm sekarang agent maka lempar bananaBomb pada saat memberikan
                damage maksimum, yaitu 10*jumlah musuh yang masih hidup */
                if (currentWorm.id == 2) {
                    if (currentWorm.bananaBomb.count > 0) {
                        PairBomb pb = maxDamageFromBomb(currentWorm.position);
                        if (pb.pos != null && pb.damage >= 10 * countEnemyAlive) {
                            return new ThrowBananaCommand(pb.pos.x, pb.pos.y);
                        }
                    }
                }
//

                /* Fungsi objektif, jika ada musuh yang bisa ditembak, maka langsung tembak */
                Command com = basicShot();
                if (com != null) {
                    return com;
                }

                /* Fungsi objektif, jika tidak ada musuh didekat kita, maka kejar musuh dengan id = idEnemy */
                if (GetEnemyPos(idEnemy) != null) {
                    return ImprovedDigAndMoveTo(currentWorm.position, GetEnemyPos(idEnemy));
                }

                /* Fungsi objektif, keadaan worm sudah berkumpul */
                if (isGroup()) {
                    /* Jika worm sekarang technologist, masih memiliki snowball, dan tidak beku, lempar saat bertarung */
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
                    /* Jika worm sekarang agent, masih memiliki bananaBomb, dan tidak beku, lempar diposisi damage terbesar */
                    if (currentWorm.id == 2) {
                        if (currentWorm.bananaBomb.count > 0 && frozenUntil(false,currentWorm.id) == 0) {
                            PairBomb pb = maxDamageFromBomb(currentWorm.position);
                            if (pb.pos != null && pb.damage >= 10 * countEnemyAlive) {
                                return new ThrowBananaCommand(pb.pos.x, pb.pos.y);
                            }
                        }
                    }
                    /* Default jika tidak ada yang dapat dibomb atau snowball (bakal cari musuh dan menyerang)*/
                    return HuntAndKill();
                }

                /* Fungsi objektif, keadaan worm belum berkumpul */
                return Regroup();

            }
            /* Fungsi kelayakan, worm commander bergerak mendekati teman untuk berkumpul */
            else if (getCurrentWorm(gameState).id == 1) {
                /* Fungsi objektif jika sudah berkumpul*/
                if(isGroup()){
                    return HuntAndKill();
                }
                return Regroup();
            }
        }

        System.out.println("---Something error---");
        System.out.println("---Something error---");
        System.out.println("---Something error---");
        /* Default jika ada error */
        return new DoNothingCommand();
    }

    /* Menghitung banyaknya worm musuh yang masih hidup */
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

    /* Mencari tau apakah ada worm pemain atau lawan didekat worm sekarang */
    private boolean isThereAlliesEnemyNear() {
        Worm[] listEnemyWorms = opponent.worms;
        Worm[] listAlliesWorms = gameState.myPlayer.worms;
        boolean isThere1 = false;
        boolean isThere2 = false;
        int distance = 6; // inisialisasi
        // Cek apakah ada musuh dekat di dekat kita
        for (int i = 0; i < listEnemyWorms.length && !isThere1; i++) {
            if (listEnemyWorms[i].alive()) {
                distance = euclideanDistance(currentWorm.position.x,currentWorm.position.y,listEnemyWorms[i].position.x,listEnemyWorms[i].position.y);
                if (distance < 4) {
                    isThere1 = true;
                }
            }
        }
        // Cek apakah ada teman di dekat kita
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

    /* Mengembalikan apakah kondisi pemainan sedang berperang*/
    private boolean isWar() {
        //int countAlive = countEnemyAlive();
        boolean onWar = false;

        if (isThereAlliesEnemyNear()) {
            onWar = true;
        }
        return onWar;
    }

    /* Mengembalikan apakah worm dengan ID tersebut sedang bertarung */
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

    /* Mengembalikan daftar cell yang berada di samping posisi tertentu */
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

    /* Menghitung jarak eclidean dari suatu titik ke titik lain */
    private int euclideanDistance(int aX, int aY, int bX, int bY) {
        return (int) (Math.sqrt(Math.pow(aX - bX, 2) + Math.pow(aY - bY, 2)));
    }

    /* Mengembalikan apakah suatu koordinat valid */
    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < gameState.mapSize
                && y >= 0 && y < gameState.mapSize;
    }

    /* Mengembalikan apakah suatu koordinat merupakan deep space atau lava */
    private boolean isNotLavaOrSpace(int x, int y) {
        return (gameState.map[y][x].type != CellType.DEEP_SPACE) &&
                (gameState.map[y][x].type != CellType.LAVA);
    }

    /* Mengembalikan arah mata angin (tujuan) dari posisi a ke b */
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

    /* Mengembalikan posisi yang harus dituju jika ingin bergerak dari posisi a ke b */
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

    /* Mengembalikan cell pada posisi tertentu */
    private Cell findCell(Position nextPosition) {
        return gameState.map[nextPosition.y][nextPosition.x];
    }

    /* Mengembalikan apakah cell pada posisi tertentu ada yang mengisi */
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

    /* Mengembalikan perintah untuk bergerak ke suatu tempat atau menggali suatu dirt */
    private Command digAndMoveTo(Position origin, Position destination) {
        if(origin == null || destination == null){
            return new DoNothingCommand();
        }

        Position nextPosition = resolveToPosition(origin,destination);
        boolean canMove = !isCellOccupied(nextPosition);

        if (!isValidCoordinate(nextPosition.x, nextPosition.y)) {
            canMove = false;
        }

        if (canMove) {
            Cell nextCell = findCell(nextPosition);
            if (nextCell.type == CellType.AIR || nextCell.type == CellType.LAVA) {
                return new MoveCommand(nextPosition.x,nextPosition.y);
            } else if (nextCell.type == CellType.DIRT) {
                return new DigCommand(nextPosition.x,nextPosition.y);
            } else {
                return new DoNothingCommand();
            }
        }
        System.out.println("Can't move");
        return new DoNothingCommand();

    }

    /* Menormalisasi vektor posisi (tidak digunakan) */
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
    
    /* Mencek apakah pada arah dari satu posisi ke posisi lain terdapat penghalang */
    private boolean isThereAnyObstacle (Position a_pos, Position b_pos) {

        Position c_pos = resolveToPosition(a_pos,b_pos);
        Worm[] listFriendWorms = gameState.myPlayer.worms;
        boolean isThere = false;

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
            c_pos = resolveToPosition(c_pos,b_pos);
        }
        return isThere;
    }

    /* Cek apakah posisi tersebut berada dalam lineOfSight musuh, tidak mengecek apakah sel tersebut dirt/air */
    private boolean isOnEnemyLineOfSight(Position pos) {
        Worm[] listEnemyWorms = opponent.worms;
        boolean isOn = false;
        for (int i = 0; i < listEnemyWorms.length && !isOn; i++) {
            if (listEnemyWorms[i].alive()) { // Cek yang masih hidup saja
                Position enemyPos = listEnemyWorms[i].position;
                int distance = euclideanDistance(pos.x,pos.y,enemyPos.x,enemyPos.y);

                int x_dif = Math.abs(pos.x- enemyPos.x);
                int y_dif = Math.abs(pos.y- enemyPos.y);
                if (x_dif == y_dif) {   // Ada di diagonal?
                    isOn = true;
                } else if (x_dif == 0 || y_dif == 0) { // Ada di vertikal atau horizontal?
                    isOn = true;
                }
                int range = 4;  // Batas tembak musuh
                if (distance > range || isThereAnyObstacle(pos,enemyPos)) { // Cek lagi bakal kena ga
                    isOn = false;
                }
            }
        }
        return isOn;
    }

    /* Mengembalikan apakah suatu posisi aman dari serangan musuh */
    private boolean isSaveToEscape(Position movePos) {
        return !isOnEnemyLineOfSight(movePos) && findCell(movePos).type != CellType.DIRT;
    }

    /* Mengembalikan perintah untuk mundur (tidak digunakan)*/
    private Command retreat() {
        List<Position> vectorPos = new ArrayList<Position>();
        Worm[] listEnemyWorms = opponent.worms;
        Position pos = currentWorm.position;

        // Tambah Vektor Posisi musuh ke kita
        for (int i = 0; i < listEnemyWorms.length; i++) {
            Position enemyPos = listEnemyWorms[i].position;
            if (listEnemyWorms[i].alive()) {
                Position vectorPosEnemy = new Position();
                vectorPosEnemy.x = pos.x - enemyPos.x;      // Vektor Posisi musuh ke kita
                vectorPosEnemy.y = pos.y - enemyPos.y;
                vectorPos.add(vectorPosEnemy);
            }
        }

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
                }
            }
        }
    }

    /* Mengembalikan perintah untuk positioning, yaitu mencari posisi yang bisa menembak musuh */
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
               possibleCell.add(surroundCell.get(i));
           }
       }
       Position cellPos = new Position();
       Random rand = new Random();
       if (possibleCell.isEmpty()) {    // Ternyata semua cell jaraknya 2 dari temen
           for (int i = 0; i < surroundCell.size(); i++) {  // Cari yang ada di lineOfSightMusuh
               cellPos.x = surroundCell.get(i).x;
               cellPos.y = surroundCell.get(i).y;
               if (isOnEnemyLineOfSight(cellPos) && !isCellOccupied(cellPos)) {
                   possibleCell.add(surroundCell.get(i));
               }
           }
           if (possibleCell.isEmpty()) {        // Kalau ga gerak random aja
               return HuntAndKill();
           } else {
               int i = rand.nextInt(possibleCell.size());
               cellPos.x = possibleCell.get(i).x;
               cellPos.y = possibleCell.get(i).y;
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
               return HuntAndKill();
           } else { // Kalau ini ideal banget
               int i = rand.nextInt(isGoodCell.size());
               cellPos.x = isGoodCell.get(i).x;
               cellPos.y = isGoodCell.get(i).y;
               return digAndMoveTo(currentWorm.position,cellPos);
           }
       }

   }

   /* Mengembalikan list berisi lineOfSight*/
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

    /* Mengembalikan list berisi lineOfSight yang lebih baik*/
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

    /* Mengembalikan posisi musuh untuk ditembak */
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

    /* Cek apakah akan terjadi friendly fire*/
    private boolean isFriendlyFire (Position a_pos, Position b_pos) {
        Position c_pos = resolveToPosition(a_pos, b_pos);
        boolean isThere = false;
        while (!c_pos.equals(b_pos) && !isThere) {
            if (c_pos.equals(GetWormPos(1)) || c_pos.equals(GetWormPos(2)) || c_pos.equals(GetWormPos(3))) {
                isThere = true;
            }
            else{
                c_pos = resolveToPosition(c_pos, b_pos);
            }
        }
        return isThere;
    }

    /* Mengembalikan perintah untuk menembak musuh */
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

    /* Mengembalikan perintah untuk menembak musuh oleh worm dengan id tertentu */
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

    /* Menghitung damage yang diberikan bananaBomb */
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

    /* Menghitung damage total yang diberikan bananaBomb */
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

    /* Mengembalikan banyaknya ronde hingga worm berhenti membeku */
    private int frozenUntil(boolean enemy, int ID){
        if(enemy){
            return gameState.opponents[0].worms[ID-1].frozen;
        }
        else {
            return gameState.myPlayer.worms[ID-1].frozen;
        }
    }

    /* Mengembalikan waktu beku yang bisa diberikan oleh snowball */
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


    /* Mengembalikan suatu map array 2 dimensi moddified cell(moddified cell adalah sel yang mengandung atribut sel tersebut, posisi ssel sebelumnya, jarak ke sel tersebut, dan suatu boolean apakah sel tersebut sudah diiterasikan) */
    public modifiedCell[][] shortestRoute(Cell[][] GameMap, Position Source){
        Position source = new Position(Source.x, Source.y);
        //initialize
        modifiedCell[][] Result = new modifiedCell[GameMap.length][GameMap[0].length];
        for(int i = 0; i < GameMap.length;i++ ){
            for(int j = 0; j<GameMap[0].length;j++){
                Result[i][j] = new modifiedCell();
                Result[i][j].deepCopy(GameMap[j][i].x,GameMap[j][i].y, source.x,source.y,false,Integer.MAX_VALUE,GameMap[j][i].type);
            }
        }

        List<Position> PosAlly = new ArrayList<Position>();
        for(int i = 0;i< gameState.myPlayer.worms.length;i++){
            if(gameState.myPlayer.worms[i].alive()){
                PosAlly.add(gameState.myPlayer.worms[i].position);
            }
        }

        for(int i = 0;i< gameState.opponents[0].worms.length;i++){
            if(gameState.opponents[0].worms[i].alive()){
                PosAlly.add(gameState.opponents[0].worms[i].position);
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
        } while(ToBeVisited.size()>0);
        return Result;
    }
    /*mengembalikan list yang adjacent, bukan deepspace,dan tidak terdapat worm (digunakna oleh fungsi shortestRoute)*/
    public  List<Position> getAdjacentCell(int x,int y , modifiedCell[][] map) {
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
    /*Mengembalikan jarak minimum dari suatu list posisi dari suatu map modified map() (digunakna oleh fungsi shortestRoute)*/
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
    /*mengembalikan suatu perintah jika bisa menembak maka akan menembak jika tidak bisa akan pergi menuju sel dimana sel tersebut bisa menembak musuh*/
    public Command HuntAndKill(){
        //System.out.println("HuntAndKill");
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
            //System.out.print("enemy size: ");
            //System.out.println(EnemyinRange.size());
            modifiedCell[][] Map = shortestRoute(gameState.map,getCurrentWorm(gameState).position);
            Position GoTo = GetMinDistanceFromArray(Map,EnemyinRange);
            return ImprovedDigAndMoveTo(getCurrentWorm(gameState).position,GoTo);
        }else{
            return command;
        }


    }
    //mengembalikan suatu posisi ynag terdekat dari array posisi berdasarkan modified map()(digunakna oleh fungsi hunt and kill)
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
        // error
        //System.out.println("Pos : -1 -1");
        return new Position(-1, -1);
    }

    //mengembalikan posisi akan di tuju berdasarkan modified map
    public Position getShortestFirstRoute(modifiedCell[][] Map, Position Target){
        int XTarget = Target.x;
        int YTarget = Target.y;
        modifiedCell currCell = new modifiedCell();
        currCell.deepCopy(Map[XTarget][YTarget]);
//        System.out.print("Generating Best Route : ");

//        System.out.print(" x:");
//        System.out.print(currCell.cell.x);
//        System.out.print(" y:");
//        System.out.print(currCell.cell.y);
        while(Map[currCell.prev.x][currCell.prev.y].distance != 0){

            currCell = Map[currCell.prev.x][currCell.prev.y];
//            System.out.print(" <- ");
//            System.out.print(" x:");
//            System.out.print(currCell.prev.x);
//            System.out.print(" y:");
//            System.out.print(currCell.prev.y);

        }
//        System.out.println();
//        System.out.print("So Go To");
//        System.out.print(" x:");
//        System.out.print(currCell.cell.x);
//        System.out.print(" y: ");
//        System.out.print(currCell.cell.y);
//        System.out.println();
        return new Position(currCell.cell.x,currCell.cell.y);
    }
    //mengembalikan perintah untuk mendig ke destination dalam garis lurus
    public Command ImprovedDigAndMoveTo(Position origin, Position destination){
        modifiedCell[][] Map = shortestRoute(gameState.map, origin);
        Position nextPosition = getShortestFirstRoute(Map,destination);
        Cell nextCell = gameState.map[nextPosition.y][nextPosition.x];

        if (nextCell.type == CellType.AIR || nextCell.type == CellType.LAVA) {
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

    //mengembalikan lisy posisi yang terdapat power up
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
    //mengembalikan true jika worm kami sudah berkumpul dan false jika belum berkumpul
    private boolean isGroup(){
        int radius = 3;
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
        if(f1 != null && euclideanDistance(f1.x, f1.y, center.x, center.y) > radius){
            group = false;
        }
        if(f2 != null && euclideanDistance(f2.x, f2.y, center.x, center.y) > radius){
            group = false;
        }
        if(f3 != null && euclideanDistance(f3.x, f3.y, center.x, center.y) > radius){
            group = false;
        }
        return group;
    }
// mengembalikan perintah bagi worm untuk berkumpul berdasarkan jarak terjauh minimal yang semua worrm harus lalui
    public Command Regroup(){
        System.out.println("Regroup!");
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
