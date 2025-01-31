import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class Cell {
    private final int row;
    private final int col;
    private boolean solved;
    private boolean isMine = false;
    private int mines = -1;
    private final List<Cell> freeNeighbours = new ArrayList<>();
    private final List<Cell> mineNeighbours = new ArrayList<>();
    private final List<Cell> unknownNeighbours = new ArrayList<>();

    public Cell(int row, int col, String boardEntry) {
        this.row = row;
        this.col = col;
        switch (boardEntry){
            case "x": isMine = true; solved = true; break;
            case "?": break;
            default : mines = Integer.parseInt(boardEntry); break;
        }
    }

    public void addUnknownNeighbour(Cell cell) {
        unknownNeighbours.add(cell);
    }
    public void addMineNeighbour(Cell cell) {
        mineNeighbours.add(cell);
    }
    public void addFreeNeighbour(Cell cell) {
        freeNeighbours.add(cell);
    }
    public List<Cell> getMinesList(){
        return mineNeighbours;
    }
    public List<Cell> getEmptyFieldsList(){
        return freeNeighbours;
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
        declareCellMineForNeighbors(this);
    }
    public boolean isSolved(){
        return solved;
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
            declareCellMineForNeighbors(cell);
        }
    }
    public void setCellFree(Cell cell){
        if(unknownNeighbours.contains(cell)){
            unknownNeighbours.remove(cell);
            freeNeighbours.add(cell);
            declareCellFreeForNeighbors(cell);
        }
    }

    public void declareCellFreeForNeighbors(Cell cell){
        freeNeighbours.forEach(neighbor -> neighbor.setCellFree(cell));
        unknownNeighbours.forEach(neighbour -> neighbour.setCellFree(cell));
    }
    public void declareCellMineForNeighbors(Cell cell){
        freeNeighbours.forEach(neighbor -> neighbor.setCellMine(cell));
        unknownNeighbours.forEach(neighbour -> neighbour.setCellMine(cell));
    }

    public List<Cell> allUnknownFree(){
        List<Cell> result = new ArrayList<>();
        if (mines == mineNeighbours.size()){ // all mines are identified
            unknownNeighbours.forEach(this::setCellFree);
            solved = true;
        }
        return result;
    }

    public List<Cell> allUnknownMines(){
        List<Cell> result = new ArrayList<>();
        if (getUnknownMines() == unknownNeighbours.size()){
            unknownNeighbours.forEach(this::setCellMine);
            solved = true;
        }
        return result;
    }

    public List<Cell> foundNewMines(){
        for (Cell other : freeNeighbours){
            if (other.unknownNeighboursSubsetOf(unknownNeighbours)){
                List<Cell> remaining = other.getAdditionalCells(unknownNeighbours);
                int mineDifference = getUnknownMines() - other.getUnknownMines();
                if (!remaining.isEmpty() && remaining.size() == mineDifference){
                    remaining.forEach(this::setCellMine);
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
                    remaining.forEach(this::setCellFree);
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
                ", freeNeighbours=" + getListString(freeNeighbours) +
                ", mineNeighbours=" + getListString(mineNeighbours) +
                ", unknownNeighbours=" + getListString(unknownNeighbours) +
                '}';
    }

    private String getListString(List<Cell> list){
        return "{"
                + list.stream()
                    .map(cell -> "(" + cell.getRow() + "," + cell.getCol() + ")")
                    .collect(Collectors.joining(", "))
                + "}";
    }
}
