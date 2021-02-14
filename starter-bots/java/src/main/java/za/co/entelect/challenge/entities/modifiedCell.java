package za.co.entelect.challenge.entities;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.entities.Cell;
import java.io.*;
public class modifiedCell {
    public Cell cell;
    public Cell prev;
    public boolean visit;
    public int distance;
    public void deepCopy(int xCell,int yCell, int xprev, int yprev,boolean visit,int distance,CellType type){
        this.cell = new Cell();
        this.cell.x = xCell;
        this.cell.y = yCell;
        this.cell.type = type;
        this.prev = new Cell();
        this.prev.x = xprev;
        this.prev.y = yprev;
        this.visit = visit;
        this.distance = distance;
    }
}
