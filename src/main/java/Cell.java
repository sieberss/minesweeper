import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Cell {
    private final int row;
    private final int col;
    private boolean isMine = false;
    private int mines = -1;
    private final List<Cell> freeNeighbours = new ArrayList<>();
    private final List<Cell> mineNeighbours = new ArrayList<>();
    private final List<Cell> unknownNeighbours = new ArrayList<>();

    public Cell(int row, int col, String boardEntry) {
        this.row = row;
        this.col = col;
        switch (boardEntry){
            case "x": isMine = true; break;
            case "?": break;
            default : mines = Integer.parseInt(boardEntry); break;
        }
    }

    public void setNeighbours(List<Cell> neighbours) {
        for (Cell neighbour : neighbours) {
            if (neighbour.isMine)
                mineNeighbours.add(neighbour);
            else if (neighbour.getUnknowns() < 0)
                unknownNeighbours.add(neighbour);
            else
                freeNeighbours.add(neighbour);
        }
    }

    public int getRow() {
        return row;
    }
    public int getCol() {
        return col;
    }
    public void setMines(int mines){
        this.mines = mines;
    }
    public void setToMine(){
        isMine = true;
    }
    public boolean isSolved(){
        return isMine || unknownNeighbours.isEmpty();
    }
    public int getUnknowns() {
        return unknownNeighbours.size();
    }
    public int getUnknownMines(){
        return mines - mineNeighbours.size();
    }
    public int getUnknownFree(){
        return unknownNeighbours.size() - getUnknownMines();
    }
    public List<Cell> getAdditionalCells(List <Cell> list){
        List<Cell> result = new ArrayList<>(list);
        result.removeAll(unknownNeighbours);
        return result;
    }
    public boolean unknownNeighboursSubsetOf(List<Cell> list){
        return new HashSet<>(list).containsAll(unknownNeighbours);
    }
    public void setCellMine(Cell cell){
        if (unknownNeighbours.contains(cell)){
            unknownNeighbours.remove(cell);
            mineNeighbours.add(cell);
        }
    }
    public void setCellFree(Cell cell){
        if(unknownNeighbours.contains(cell)){
            unknownNeighbours.remove(cell);
            freeNeighbours.add(cell);
        }
    }

    public void declareCellFreeForNeighbors(Cell cell){
        freeNeighbours.forEach(neighbor -> neighbor.setCellFree(cell));
    }
    public void declareCellMineForNeighbors(Cell cell){
        freeNeighbours.forEach(neighbor -> neighbor.setCellMine(cell));
    }

    public List<Cell> allUnknownFree(){
        List<Cell> result = new ArrayList<>();
        if (mines == mineNeighbours.size()){ // all mines are identified
            for (Cell cell : unknownNeighbours){
                freeNeighbours.add(cell);
                result.add(cell);
                declareCellFreeForNeighbors(cell);
            }
            unknownNeighbours.clear();
        }
        return result;
    }

    public List<Cell> allUnknownMines(){
        List<Cell> result = new ArrayList<>();
        if (getUnknownMines() == unknownNeighbours.size()){
            for (Cell cell : unknownNeighbours){
                mineNeighbours.add(cell);
                result.add(cell);
                declareCellMineForNeighbors(cell);
            }
            unknownNeighbours.clear();
        }
        return result;
    }

    public List<Cell> foundNewMines(){
        for (Cell other : freeNeighbours){
            if (other.unknownNeighboursSubsetOf(unknownNeighbours)){
                List<Cell> remaining = other.getAdditionalCells(unknownNeighbours);
                int mineDifference = getUnknownMines() - other.getUnknownMines();
                if (!remaining.isEmpty() && remaining.size() == mineDifference){
                    for (Cell cell : remaining){
                        mineNeighbours.add(cell);
                        unknownNeighbours.remove(cell);
                        declareCellMineForNeighbors(cell);
                        // cell.setToMine();
                    }
                    return remaining;
                }
            }
        }
        return new ArrayList<>();
    }

    public List<Cell> foundNewFrees(){
        for (Cell other : freeNeighbours){
            if (other.unknownNeighboursSubsetOf(unknownNeighbours)){
                List<Cell> remaining = other.getAdditionalCells(unknownNeighbours);
                int freeDifference = getUnknownFree() - other.getUnknownFree();
                if (!remaining.isEmpty() && remaining.size() == freeDifference){
                    for (Cell cell : remaining){
                        freeNeighbours.add(cell);
                        unknownNeighbours.remove(cell);
                        declareCellFreeForNeighbors(cell);
                    }
                    return remaining;
                }
            }
        }
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Cell{" +
                "row=" + row +
                ", col=" + col +
                ", isMine=" + isMine +
                ", mines=" + mines +
                ", freeNeighbours=" + freeNeighbours +
                ", mineNeighbours=" + mineNeighbours +
                ", unknownNeighbours=" + unknownNeighbours +
                '}';
    }
}
