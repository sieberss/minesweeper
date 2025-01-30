import java.awt.*;
import java.util.Set;

public class CheckableCell {
    int row;
    int col;
    int unknownMines;
    Set<Point> unknownNeighbors;

    public CheckableCell(int row, int col, int unknownMines, Set<Point> unknownNeighbors) {
        this.row = row;
        this.col = col;
        this.unknownMines = unknownMines;
        this.unknownNeighbors = unknownNeighbors;
    }
}
