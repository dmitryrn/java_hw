import java.util.ArrayList;
import java.util.List;

public class Board {
    private static final int DEFAULT_SIZE = 3;

    private final int size;
    private final Mark[][] grid;

    public Board() {
        this(new Builder());
    }

    private Board(Builder builder) {
        this.size = builder.size;
        this.grid = new Mark[size][size];
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getSize() {
        return size;
    }

    public void displayBoard() {
        StringBuilder header = new StringBuilder("  ");
        for (int column = 1; column <= size; column++) {
            header.append(column);
            if (column < size) {
                header.append("   ");
            }
        }
        System.out.println(header);

        for (int row = 0; row < size; row++) {
            System.out.printf("%d ", row + 1);
            for (int column = 0; column < size; column++) {
                System.out.print(displayCell(row, column));
                if (column < size - 1) {
                    System.out.print(" | ");
                }
            }
            System.out.println();

            if (row < size - 1) {
                System.out.print(" ");
                for (int column = 0; column < size; column++) {
                    System.out.print("---");
                    if (column < size - 1) {
                        System.out.print("+");
                    }
                }
                System.out.println();
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
        if (mark == null) {
            return false;
        }

        for (int index = 0; index < size; index++) {
            boolean rowWin = true;
            boolean columnWin = true;
            for (int offset = 0; offset < size; offset++) {
                rowWin &= grid[index][offset] == mark;
                columnWin &= grid[offset][index] == mark;
            }
            if (rowWin || columnWin) {
                return true;
            }
        }

        boolean mainDiagonalWin = true;
        boolean secondaryDiagonalWin = true;
        for (int index = 0; index < size; index++) {
            mainDiagonalWin &= grid[index][index] == mark;
            secondaryDiagonalWin &= grid[index][size - index - 1] == mark;
        }
        return mainDiagonalWin || secondaryDiagonalWin;
    }

    public int[][] getEmptySquares() {
        List<int[]> emptySquares = new ArrayList<>();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (grid[row][col] == null) {
                    emptySquares.add(new int[]{row, col});
                }
            }
        }
        return emptySquares.toArray(new int[0][]);
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    public static class Builder {
        private int size = DEFAULT_SIZE;

        public Builder size(int size) {
            if (size < 1) {
                throw new IllegalArgumentException("Board size must be positive");
            }
            this.size = size;
            return this;
        }

        public Builder setSize(int size) {
            return size(size);
        }

        public Board build() {
            return new Board(this);
        }
    }
}
