import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MineSweeper {

    private final String[][] board;
    private final int totalMines;
    private final Set<Cell> foundMines = new HashSet<>();
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
                cells[i][j] = created;
            }
        }
        // tell cells about their neighbors
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                addNeighbours(i, j);
                System.out.println(cells[i][j]);
            }
        }
    }

    private void addNeighbours(int i, int j) {
        List<Cell> neighbours = getNeighbourCells(i, j);
        System.out.println(neighbours);
        for (Cell neighbour : neighbours) {
            switch(board[neighbour.getRow()][neighbour.getCol()]){
                case "x": cells[i][j].addMineNeighbour(neighbour); break;
                case "?": cells[i][j].addUnknownNeighbour(neighbour); break;
                default:  cells[i][j].addFreeNeighbour(neighbour); break;
            }
        }
    }

    public String solve() {
        boolean updated;
        do {
            updated = checkCells();
            System.out.println(getBoardString());
            System.out.println(foundMines.size() + " mines discovered: " + foundMines);
        }
        while (totalMines > foundMines.size() && updated);
        if (totalMines > foundMines.size()) return "?";
        openAllFields();
        return getBoardString();
    }

    boolean checkCells() {
        boolean updated = false;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j].equals("x") || board[i][j].equals("?"))
                    continue;
                Cell cell = cells[i][j];
                if (cell.isSolved()) {
                    continue;
                }
                updated = checkSingleCell(cell, updated);
            }
        }
        return updated;
    }

    boolean checkSingleCell(Cell cell, boolean updated) {
        System.out.println("checking cell " + cell);
        // 1: all unknown neighbors are free cells
        List<Cell> list = cell.allUnknownFree();
        if (!list.isEmpty()) {
            addEmptyFields(list);
            addMines(cell.getMinesList());
            return true;
        }
        // 2: all unknown neighbors are mines
        list = cell.allUnknownMines();
        if (!list.isEmpty()) {
            addMines(list);
            addEmptyFields(cell.getEmptyFieldsList());
            return true;
        }
        // 3: some empty fields identified
        list = cell.foundNewFrees();
        if (!list.isEmpty()) {
            addEmptyFields(list);
            return true;
        }
        // 4: some mines identified
        list = cell.foundNewMines();
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
            board[found.getRow()][found.getCol()] = "x";
            found.setToMine();
            foundMines.add(found);
        }
    }

    private void addEmptyFields(List<Cell> list) {
        for (Cell found : list) {
            System.out.println("opening empty field " + found);
            int mines = Game.open(found.getRow(), found.getCol());
            board[found.getRow()][found.getCol()] = "" + mines;
            found.setMines(mines);
            System.out.println(mines + " mines around empty field " + found);
        }
    }

    String getBoardString() {
        return Arrays.stream(board)
                .map(line -> String.join(" ", line))
                .collect(Collectors.joining("\n"));
    }



    private List<Cell> getNeighbourCells(int row, int col) {
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

    private void openAllFields() {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col].equals("?")) {
                    board[row][col] = "" + Game.open(row, col);
                }
            }
        }
    }
}