import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MineSweeper {

    private final String[][] board;
    private final int totalMines;
    private int foundMines;
    private final Cell[][] cells;

    public MineSweeper(final String s, final int nMines) {
        totalMines = nMines;
        board = Stream.of(s.split("\n"))
                .map(line -> line.split(" "))
                .toArray(String[][]::new);
        foundMines = (int) Arrays.stream(board)
                .flatMap(Arrays::stream)
                .filter(cell -> cell.equals("x"))
                .count();
        cells = new Cell[board.length][board[0].length];
        // define cells
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                cells[i][j] = new Cell(i, j, board[i][j]);
            }
        }
        // tell cells about their neighbors
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                cells[i][j].setNeighbours(getNeighbors(i, j));
            }
        }
    }

    public String solve() {
        boolean updated;
        do {
            updated = checkCells();
        }
        while (totalMines > foundMines && updated);
        if (!updated) return "?";
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
        // 1: all unknown neighbors are free cells
        List<Cell> list = cell.allUnknownFree();
        if (!list.isEmpty()) {
            addEmptyFields(list);
            return true;
        }
        // 2: all unknown neighbors are mines
        list = cell.allUnknownMines();
        if (!list.isEmpty()) {
            addMines(list);
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
        System.out.println("found mines: " + foundMines);
        for (Cell found : list) {
            board[found.getRow()][found.getCol()] = "x";
            found.setToMine();
            foundMines++;
        }
    }

    private void addEmptyFields(List<Cell> list) {
        System.out.println("found empty fields: " + foundMines);
        for (Cell found : list) {
            int mines = Game.open(found.getRow(), found.getCol());
            board[found.getRow()][found.getCol()] = "" + mines;
            found.setMines(mines);
        }
    }

    String getBoardString() {
        return Arrays.stream(board)
                .map(line -> String.join(" ", line))
                .collect(Collectors.joining("\n"));
    }



    private List<Cell> getNeighbors(int row, int col) {
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