package za.co.entelect.challenge.entities;

import com.google.gson.annotations.SerializedName;

public class Position {
    @SerializedName("x")
    public int x;

    @SerializedName("y")
    public int y;

    public Position(){
        this.x = 0;
        this.y = 0;
    }

    public Position(int x, int y){
        this.x = x;
        this.y = y;
    }

    public boolean equals(Position pos){
        if(pos == null){
            return false;
        }
        return this.x==pos.x && this.y==pos.y;
    }
}
