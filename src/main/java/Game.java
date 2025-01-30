import java.util.Arrays;
import java.util.stream.Stream;

public class Game {

    static String[][] board;

    public static void newGame(String s) {
        board = Stream.of(s.split("\n"))
                .map(line -> line.split(" "))
                .toArray(String[][]::new);
    }

    public static void read(String s) {
        System.out.println("game map:");
        System.out.println(s);
    }

    public static int getMinesN() {
        int number = (int) Arrays.stream(board)
                .flatMap(Arrays::stream)
                .filter(cell -> cell.equals("x"))
                .count();
        System.out.println("Number of mines: " + number);
        return number;
    }

    public static int open(int row, int col) {
        if (board[row][col].equals("x")) {
            System.out.printf("There was a bomb at row %d and col %d\n", row, col);
            throw new BombError("Bomb");
        }
        return board[row][col].charAt(0) - '0';
    }
}
