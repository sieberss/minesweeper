import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MineSweeper {

    private final String[][] board;
    private final int totalMines;
    private final Set<Cell> foundMines = new HashSet<>();
    private final Set<Cell> uncompletedCells = new HashSet<>();
    private final Cell[][] cells;

    public MineSweeper(final String s, final int nMines) {
        totalMines = nMines;
        board = Stream.of(s.split("\n"))
                .map(line -> line.split(" "))
                .toArray(String[][]::new);
        cells = new Cell[board.length][board[0].length];
        // define cells
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                Cell created = new Cell(i, j, board[i][j]);
                if (board[i][j].equals("x"))
                    foundMines.add(created);
                else uncompletedCells.add(created);
                cells[i][j] = created;
            }
        }
        // tell cells about their neighbors
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                addNeighbours(i, j);
            }
        }
    }

    public String solve() {
        iterateCellChecking();
        if (totalMines > foundMines.size())
            return treatRemainingMines();
        openAllUncompletedFields();
        return getBoardString();
    }

    private String treatRemainingMines() {
        int remainingMines = totalMines - foundMines.size();
        List<Cell> unreachableCells = new ArrayList<>();
        List<Cell> reachableUnknowns = new ArrayList<>();
        List<Cell> uncompletedFree = new ArrayList<>();
        for (Cell cell : uncompletedCells) {
            if (cell.isFree()) uncompletedFree.add(cell);
            else {
                if (cell.getEmptyFieldsList().isEmpty()) unreachableCells.add(cell);
                else reachableUnknowns.add(cell);
            }
        }
        int minMines = remainingMines - unreachableCells.size();
        List<List<Cell>> possibleMineLists = CombinationTest.getPossibleMineLists(uncompletedFree, reachableUnknowns, minMines, remainingMines);
        System.out.println("Possible solutions:");
        possibleMineLists.forEach(list -> System.out.println(list.size() + " " + list));
        return getResult(remainingMines, possibleMineLists, reachableUnknowns, unreachableCells);
    }

    private String getResult(int remainingMines, List<List<Cell>> possibleMineLists, List<Cell> reachableUnknowns, List<Cell> unreachableCells) {
        int listCount = possibleMineLists.size();
        List<Cell> shortest = possibleMineLists.get(0);
        List<Cell> longest = possibleMineLists.get(listCount - 1);
        int maxMines = longest.size();/*
        // only 1 possible solution. Set mines from solution and retry solving
        if (listCount == 1) {
            addMines(shortest);
            return solve();
        }*/
        List<Cell> sureMines = CombinationTest.getSureMines(possibleMineLists, reachableUnknowns);
        List<Cell> sureFree = CombinationTest.getSureFree(possibleMineLists, reachableUnknowns);
        // no new information from possibleMineLists
        if (sureMines.isEmpty() && sureFree.isEmpty()) {
            if (!unreachableCells.isEmpty() && unreachableCells.size() + maxMines == remainingMines) {
                // any solution requires all unreachable cells to be mines. Set them and retry solving
                addMines(unreachableCells);
                return solve();
            }
            return "?";
        }
        // add found fields and retry solving
        addMines(sureMines);
        addEmptyFields(sureFree);
        return solve();
    }

    private void iterateCellChecking() {
        boolean updated;
        do {
            updated = didCellUpdate();
            System.out.println(getBoardString());
            System.out.println(foundMines.size() + " mines of " + totalMines + " discovered: " + foundMines);
        }
        while (totalMines > foundMines.size() && updated);
    }

    boolean didCellUpdate() {
        boolean madeUpdate = false;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j].equals("x") || board[i][j].equals("?"))
                    continue;
                Cell cell = cells[i][j];
                if (uncompletedCells.contains(cell))
                    madeUpdate = updatedSingleCell(cell, madeUpdate);
            }
        }
        return madeUpdate;
    }

    boolean updatedSingleCell(Cell cell, boolean updated) {
        System.out.println("checking cell " + cell);
        // 1+2: all unknown neighbors are free cells / mines
        if (cell.allUnknownAreFree() || cell.allUnknownAreMines()) {
            addEmptyFields(cell.getEmptyFieldsList());
            addMines(cell.getMinesList());
            uncompletedCells.remove(cell);
            return true;
        }
        // 3: some empty fields identified
        List<Cell> list = cell.foundNewFreesFromSubset();
        if (!list.isEmpty()) {
            addEmptyFields(list);
            return true;
        }
        // 4: some mines identified
        list = cell.foundNewMinesFromSubset();
        if (!list.isEmpty()) {
            addMines(list);
            return true;
        }
        // 5: no updates on this cell
        return updated;
    }

    private void addMines(List<Cell> list) {
        System.out.println("found mines: " + list);
        for (Cell found : list) {
            uncompletedCells.remove(found);
            board[found.getRow()][found.getCol()] = "x";
            found.setToMine();
            foundMines.add(found);
        }
    }

    private void addEmptyFields(List<Cell> list) {
        for (Cell found : list) {
            if (uncompletedCells.contains(found)) {
                System.out.println("opening empty field " + found);
                int mines = Game.open(found.getRow(), found.getCol());
                board[found.getRow()][found.getCol()] = "" + mines;
                found.setToFree(mines);
                System.out.println(mines + " mines around empty field " + found);
            }
        }
    }

    String getBoardString() {
        return Arrays.stream(board)
                .map(line -> String.join(" ", line))
                .collect(Collectors.joining("\n"));
    }

    private void addNeighbours(int i, int j) {
        List<Cell> neighbours = getNeighbourCellsOf(i, j);
        for (Cell neighbour : neighbours) {
            switch(board[neighbour.getRow()][neighbour.getCol()]){
                case "x": cells[i][j].addMineNeighbour(neighbour); break;
                case "?": cells[i][j].addUnknownNeighbour(neighbour); break;
                default:  cells[i][j].addFreeNeighbour(neighbour); break;
            }
        }
    }

    private List<Cell> getNeighbourCellsOf(int row, int col) {
        List<Cell> neighbors = new ArrayList<>();
        int[][] indices = {{row-1, col-1}, {row-1, col}, {row-1, col+1}, {row, col-1}, {row, col+1},
                {row+1, col-1}, {row+1, col}, {row+1, col+1}};
        for (int[] index : indices) {
            if (index[0] >= 0 && index[0] < board.length && index[1] >= 0 && index[1] < board[0].length) {
                neighbors.add(cells[index[0]][index[1]]);
            }
        }
        return neighbors;
    }

    private void openAllUncompletedFields() {
        for (Cell cell : uncompletedCells) {
            board[cell.getRow()][cell.getCol()] = "" + Game.open(cell.getRow(), cell.getCol());
        }
    }

}