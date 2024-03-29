package za.co.entelect.challenge.entities;

import com.google.gson.annotations.SerializedName;

public class Worm {
    @SerializedName("id")
    public int id;

    @SerializedName("health")
    public int health;

    @SerializedName("position")
    public Position position;

    @SerializedName("diggingRange")
    public int diggingRange;

    @SerializedName("movementRange")
    public int movementRange;

    @SerializedName("bananaBombs")
    public Bomb bananaBomb;

    @SerializedName("snowballs")
    public Bomb snowballs;

    @SerializedName("roundsUntilUnfrozen")
    public int frozen;

    public boolean alive() {
        return health > 0;
    }
}
