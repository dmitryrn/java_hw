import java.util.ArrayList;
import java.util.List;

public class Board {
    private static final int SIZE = 3;
    private final Mark[][] grid = new Mark[SIZE][SIZE];

    public void displayBoard() {
        System.out.println("  1   2   3");
        for (int row = 0; row < SIZE; row++) {
            System.out.printf("%d %c | %c | %c%n", row + 1,
                    displayCell(row, 0), displayCell(row, 1), displayCell(row, 2));
            if (row < SIZE - 1) {
                System.out.println(" ---+---+---");
            }
        }
    }

    private char displayCell(int row, int col) {
        return grid[row][col] == null ? ' ' : grid[row][col].getSymbol();
    }

    public boolean placeMark(int row, int col, Mark mark) {
        if (!isInBounds(row, col) || grid[row][col] != null || mark == null) {
            return false;
        }
        grid[row][col] = mark;
        return true;
    }

    public boolean isFull() {
        for (Mark[] row : grid) {
            for (Mark cell : row) {
                if (cell == null) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkWin(Mark mark) {
        for (int index = 0; index < SIZE; index++) {
            if ((grid[index][0] == mark && grid[index][1] == mark && grid[index][2] == mark)
                    || (grid[0][index] == mark && grid[1][index] == mark && grid[2][index] == mark)) {
                return true;
            }
        }

        return (grid[0][0] == mark && grid[1][1] == mark && grid[2][2] == mark)
                || (grid[0][2] == mark && grid[1][1] == mark && grid[2][0] == mark);
    }

    public int[][] getEmptySquares() {
        List<int[]> emptySquares = new ArrayList<>();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (grid[row][col] == null) {
                    emptySquares.add(new int[]{row, col});
                }
            }
        }
        return emptySquares.toArray(new int[0][]);
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }
}
