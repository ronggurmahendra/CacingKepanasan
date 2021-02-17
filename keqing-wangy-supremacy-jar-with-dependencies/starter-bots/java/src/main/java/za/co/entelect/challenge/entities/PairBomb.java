package za.co.entelect.challenge.entities;

public class PairBomb {
    public Position pos;
    public int damage;

    public PairBomb(){
        this.pos = new Position();
        this.damage = 0;
    }
    public PairBomb(Position pos, int damage){
        this.pos = pos;
        this.damage = damage;
    }
}
