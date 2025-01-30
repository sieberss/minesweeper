import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MineSweeper {

    private String[][] board;
    private int totalMines;
    private int foundMines;
    private boolean solvable;
    List<CheckableCell> toCheck = new ArrayList<>();
    Set<Set<Point>> havingExactlyOneMine = new HashSet<>();

    public MineSweeper(final String s, final int nMines) {
        totalMines = nMines;
        board = Stream.of(s.split("\n"))
                .map(line -> line.split(" "))
                .toArray(String[][]::new);
        foundMines = (int) Arrays.stream(board)
                .flatMap(Arrays::stream)
                .filter(cell -> cell.equals("x"))
                .count();;
        solvable = true;
    }

    public String solve() {
        addCellsToCheck();
        System.out.println(toCheck);
        while (totalMines > foundMines && solvable) {
            solvable = foundNewData();
            System.out.println(getBoardString());
            System.out.println("found mines: " + foundMines);
            System.out.println(toCheck);
            System.out.println(havingExactlyOneMine);
        }
        if (!solvable) return "?";
        openAllFields();
        return getBoardString();
    }

    String getBoardString() {
        return Arrays.stream(board)
                .map(line -> String.join(" ", line))
                .collect(Collectors.joining("\n"));
    }

    private void addCellsToCheck() {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                char entry = board[row][col].charAt(0);
                if (Character.isDigit(entry)){
                    addSingleCellToCheck(entry - '0', row, col);
                }
            }
        }
    }

    private void addSingleCellToCheck(int mines, int row, int col) {
        int unknownMines = mines;
        Set<Point> neighborCells = getNeighbors(row, col);
        Set<Point> unknownNeighborCells = new HashSet<>();
        for (Point neighbor : neighborCells) {
            if (board[neighbor.x][neighbor.y].equals("x")) {
                unknownMines--;
            }
            if (board[neighbor.x][neighbor.y].equals("?")) {
                unknownNeighborCells.add(neighbor);
            }
        }
        if (!unknownNeighborCells.isEmpty()) {
            toCheck.add(new CheckableCell(row, col, unknownMines, unknownNeighborCells));
        }
    }

    private Set<Point> getNeighbors(int row, int col) {
        Set<Point> neighbors = new HashSet<>();
        int[][] indices = {{row-1, col-1}, {row-1, col}, {row-1, col+1}, {row, col-1}, {row, col+1},
                {row+1, col-1}, {row+1, col}, {row+1, col+1}};
        for (int[] index : indices) {
            if (index[0] >= 0 && index[0] < board.length && index[1] >= 0 && index[1] < board[0].length) {
                neighbors.add(new Point(index[0], index[1]));
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

    private boolean foundNewData() {
        // toCheck.sort(Comparator.comparing(cell -> cell.unknownMines));
        boolean foundNewSetWithExactlyOneMine = false;
        Set<Point> mines = new HashSet<>();
        Set<Point> free = new HashSet<>();
        List<CheckableCell> solved = new ArrayList<>();
        for (CheckableCell cell : toCheck) {
            if (cell.unknownMines == 0){
                free.addAll(cell.unknownNeighbors);
                solved.add(cell);
            }
            else if (cell.unknownMines == cell.unknownNeighbors.size()){
                mines.addAll(cell.unknownNeighbors);
                solved.add(cell);
            }
            else if (cell.unknownMines == 1){
                boolean isNewSet = addSetWithExactlyOneMine(cell.unknownNeighbors);
                foundNewSetWithExactlyOneMine = foundNewSetWithExactlyOneMine || isNewSet;
                free.addAll(allUnknownFreeExceptOne(cell));
            }
            else if (cell.unknownMines == cell.unknownNeighbors.size() - 1){
                mines.addAll(allUnknownMinesExceptOne(cell));
            }
        }
        if (solved.isEmpty() && mines.isEmpty() && free.isEmpty() && !foundNewSetWithExactlyOneMine) {
            //no updates could be made
            return false;
        }
        toCheck.removeAll(solved);
        updateMines(mines);
        updateFreeCells(free);
        havingExactlyOneMine.removeIf(set -> set.size() < 2);
        return true;
    }

    private boolean addSetWithExactlyOneMine(Set<Point> points) {
        for (Set<Point> set : havingExactlyOneMine) {
            if (points.containsAll(set)) {
                // old set is smaller, no new set necessary
                return false;
            }
            if (set.containsAll(points)) {
                // points is a smaller set, replace old set
                set.clear();
                set.addAll(points);
                return true;
            }
        }
        // add new set
        havingExactlyOneMine.add(points);
        return true;
    }

    private void updateFreeCells(Set<Point> free) {
        List<Set<Point>> setsToRemove = new ArrayList<>();
        for (Point point : free) {
            int mines = Game.open(point.x, point.y);
            board[point.x][point.y] = "" + mines;
            addSingleCellToCheck(mines, point.x, point.y);
            // point is no unknown neighbor any more
            for (CheckableCell cell : toCheck) {
                cell.unknownNeighbors.remove(point);
            }
            // point does not belong to sets with exactly one mine any more
            for (Set<Point> setWithOneMine : havingExactlyOneMine) {
                setWithOneMine.remove(point);
            }
        }
    }

    private void updateMines(Set<Point> mines) {
        List<Set<Point>> setsToRemove = new ArrayList<>();
        for (Point point : mines) {
            board[point.x][point.y] = "x";
            foundMines++;
            for (CheckableCell cell : toCheck) {
                if (cell.unknownNeighbors.contains(point)) {
                    cell.unknownNeighbors.remove(point);
                    cell.unknownMines--;
                }
            }
            for (Set<Point> setWithOneMine : havingExactlyOneMine) {
                if (setWithOneMine.contains(point)) {
                    setsToRemove.add(setWithOneMine);
                }
            }
        }
        setsToRemove.forEach(havingExactlyOneMine::remove);
    }

    private Set<Point> allUnknownMinesExceptOne(CheckableCell cell) {
        Set<Point> mineFields = new HashSet<>(cell.unknownNeighbors);
        for (Point neighbor : cell.unknownNeighbors) {
            for (Point otherNeighbor : cell.unknownNeighbors) {
                if (neighbor.equals(otherNeighbor)) {
                    continue;
                }
                if (oneMineOnTwoCells(neighbor, otherNeighbor)) {
                    //all other cells are mines
                    mineFields.remove(neighbor);
                    mineFields.remove(otherNeighbor);
                    return mineFields;
                }
            }
        }
        // no mines identified
        return new HashSet<>();
    }

    private boolean oneMineOnTwoCells(Point neighbor, Point otherNeighbor) {
        for (Set<Point> set : havingExactlyOneMine) {
            if (set.contains(neighbor) && set.contains(otherNeighbor)){
                return true;
            }
        }
        return false;
    }

    private Set<Point> allUnknownFreeExceptOne(CheckableCell cell) {
        for (Set<Point> set : havingExactlyOneMine) {
            if (cell.unknownNeighbors.containsAll(set)){
                Set<Point> others = new HashSet<>(cell.unknownNeighbors);
                others.removeAll(set);
                if (!others.isEmpty())
                    // the other cells are free because the mine is within set
                    return others;
            }
        }
        // no free cells found
        return new HashSet<>();
    }

    private Set<Point> allUnknownMines(CheckableCell cell) {
        for (Point neighbor : cell.unknownNeighbors) {
            board[neighbor.x][neighbor.y] = "x";
        }
        return cell.unknownNeighbors;
    }

    private Set<Point> allUnknownFree(CheckableCell cell) {
        for (Point neighbor : cell.unknownNeighbors) {
            int mines = Game.open(neighbor.x, neighbor.y);
            board[neighbor.x][neighbor.y] = "" + mines;
            addSingleCellToCheck(mines, neighbor.x, neighbor.y);
            }
        return cell.unknownNeighbors;
    }
}