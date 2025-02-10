import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        switch (boardEntry) {
            case "x":
                isMine = true;
                break;
            case "?":
                break;
            default:
                mines = Integer.parseInt(boardEntry);
                break;
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

    public List<Cell> getMinesList() {
        return new ArrayList<>(mineNeighbours);
    }

    public List<Cell> getEmptyFieldsList() {
        return new ArrayList<>(freeNeighbours);
    }

    public List<Cell> getUnknownsList() {
        return new ArrayList<>(unknownNeighbours);
    }

    public Set<Cell> getCommonUnknowns(Cell other) {
        Set<Cell> commonUnknowns = new HashSet<>();
        for (Cell cell : other.getUnknownsList()) {
            if (unknownNeighbours.contains(cell)) commonUnknowns.add(cell);
        }
        return commonUnknowns;
    }

    public int getMaxCommonMines(Cell other) {
        return Math.min(Math.min(getUnknownMines(), other.getUnknownMines()), getCommonUnknowns(other).size());
    }

    public int getMaxCommonFree(Cell other) {
        return Math.min(Math.min(getUnknownFree(), other.getUnknownFree()), getCommonUnknowns(other).size());
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setToMine() {
        isMine = true;
        declareCellMineForNeighbors(this);
    }

    public void setToFree(int mines) {
        this.mines = mines;
        declareCellFreeForNeighbors(this);
    }

    public boolean isFree(){
        return mines != -1;
    }

    public int getUnknownMines() {
        return mines - mineNeighbours.size();
    }

    public int getUnknownFree() {
        return unknownNeighbours.size() - getUnknownMines();
    }

    public List<Cell> getCellsNotInUnknownsList(List<Cell> list) {
        List<Cell> result = new ArrayList<>(list);
        result.removeAll(unknownNeighbours);
        return result;
    }

    public void setCellMine(Cell cell) {
        if (unknownNeighbours.contains(cell)) {
            unknownNeighbours.remove(cell);
            mineNeighbours.add(cell);
            declareCellMineForNeighbors(cell);
        }
    }

    public void setCellFree(Cell cell) {
        if (unknownNeighbours.contains(cell)) {
            unknownNeighbours.remove(cell);
            freeNeighbours.add(cell);
            declareCellFreeForNeighbors(cell);
        }
    }

    public void declareCellFreeForNeighbors(Cell cell) {
        freeNeighbours.forEach(neighbor -> neighbor.setCellFree(cell));
        unknownNeighbours.forEach(neighbour -> neighbour.setCellFree(cell));
    }

    public void declareCellMineForNeighbors(Cell cell) {
        freeNeighbours.forEach(neighbor -> neighbor.setCellMine(cell));
        unknownNeighbours.forEach(neighbour -> neighbour.setCellMine(cell));
    }

    public boolean allUnknownAreFree() {
        if (mines == mineNeighbours.size()) { // all mines are identified
            freeNeighbours.addAll(unknownNeighbours);
            List<Cell> temp = new ArrayList<>(unknownNeighbours);
            unknownNeighbours.clear();
            temp.forEach(this::declareCellFreeForNeighbors);
            return true;
        }
        return false;
    }

    public boolean allUnknownAreMines() {
        if (getUnknownMines() == unknownNeighbours.size()) {
            mineNeighbours.addAll(unknownNeighbours);
            List<Cell> temp = new ArrayList<>(unknownNeighbours);
            unknownNeighbours.clear();
            temp.forEach(this::declareCellMineForNeighbors);
            return true;
        }
        return false;
    }

    public List<Cell> foundNewMinesFromSubset() {
        // build list of neighbors with common unknowns and check if one of them indicates a free cell
        List<Cell> neigboursWithCommonUnknowns = new ArrayList<>();
        for (Cell other : getFreeNeighboursOfUnknowns()) {
            List<Cell> uncommon = other.getCellsNotInUnknownsList(unknownNeighbours);
            if (!uncommon.isEmpty()) {
                neigboursWithCommonUnknowns.add(other);
                if (allUncommonMustBeMinesRespFree(uncommon, other, true)) {
                    uncommon.forEach(this::setCellMine);
                    return uncommon;
                }
            }
        }
        // check if combination of two subsets indicates a free cell
        if (neigboursWithCommonUnknowns.size() >= 2) {
            for (Cell cell1 : neigboursWithCommonUnknowns) {
                for (Cell cell2 : neigboursWithCommonUnknowns) {
                    if (unknownsAreDisjoint(cell1, cell2)) {
                        // subtract both subsets from unknownNeighbors
                        List<Cell> cellsInNoneOfSubsets = cell1.getCellsNotInUnknownsList(
                                cell2.getCellsNotInUnknownsList(unknownNeighbours));
                        int mineDifference = getUnknownMines() - getMaxCommonMines(cell1) - getMaxCommonMines(cell2);
                        if (cellsInNoneOfSubsets.size() == mineDifference) {
                            cellsInNoneOfSubsets.forEach(this::setCellMine);
                            return cellsInNoneOfSubsets;
                        }
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    public List<Cell> foundNewFreesFromSubset() {
        // build list of neighbors with common unknowns and check if one of them indicates a free cell
        List<Cell> neigboursWithCommonUnknowns = new ArrayList<>();
        for (Cell other : getFreeNeighboursOfUnknowns()) {
            List<Cell> uncommon = other.getCellsNotInUnknownsList(unknownNeighbours);
            if (!uncommon.isEmpty()) {
                neigboursWithCommonUnknowns.add(other);
                if (allUncommonMustBeMinesRespFree(uncommon, other, false)) {
                    uncommon.forEach(this::setCellFree);
                    return uncommon;
                }
            }
        }
        // check if combination of two subsets indicates a free cell
        if (neigboursWithCommonUnknowns.size() >= 2) {
            for (Cell cell1 : neigboursWithCommonUnknowns) {
                for (Cell cell2 : neigboursWithCommonUnknowns) {
                    if (unknownsAreDisjoint(cell1, cell2)) {
                        // subtract both subsets from unknownNeighbors
                        List<Cell> cellsInNoneOfSubsets = cell1.getCellsNotInUnknownsList(
                                cell2.getCellsNotInUnknownsList(unknownNeighbours));
                        int freeDifference = getUnknownFree() - getMaxCommonFree(cell1) - getMaxCommonFree(cell2);
                        if (cellsInNoneOfSubsets.size() == freeDifference) {
                            cellsInNoneOfSubsets.forEach(this::setCellFree);
                            return cellsInNoneOfSubsets;
                        }
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    private boolean allUncommonMustBeMinesRespFree(List<Cell> uncommon, Cell other, boolean searchingMine) {
        if (searchingMine) {
            int mineDifference = getUnknownMines() - getMaxCommonMines(other);
            return uncommon.size() == mineDifference;
        } else {
            int freeDifference = getUnknownFree() - getMaxCommonFree(other);
            return uncommon.size() == freeDifference;
        }
    }


    private boolean unknownsAreDisjoint(Cell cell1, Cell cell2) {
        if (cell1.equals(cell2)) return false;
        if (cell1.getUnknownsList().removeAll(cell2.getUnknownsList())) return false;
        return !cell2.getUnknownsList().removeAll(cell1.getUnknownsList());
    }

    public Set<Cell> getFreeNeighboursOfUnknowns() {
        Set<Cell> secondNeighbours = new HashSet<>();
        unknownNeighbours.forEach(unknown -> secondNeighbours.addAll(unknown.freeNeighbours));
        return secondNeighbours;
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

    private String getListString(List<Cell> list) {
        return "{"
                + list.stream()
                .map(cell -> "(" + cell.getRow() + "," + cell.getCol() + ")")
                .collect(Collectors.joining(", "))
                + "}";
    }
}
