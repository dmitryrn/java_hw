import java.util.Random;
import java.util.Scanner;

public class AIPlayer extends Player {
    private final Random random = new Random();

    public AIPlayer(String name, Mark mark) {
        super(name, mark);
    }

    @Override
    public void makeMove(Board board, Scanner scanner) {
        int[][] emptySquares = board.getEmptySquares();
        int[] move = emptySquares[random.nextInt(emptySquares.length)];
        board.placeMark(move[0], move[1], getMark());
        System.out.printf("%s (%s) chose row %d, column %d.%n",
                getName(), getMark(), move[0] + 1, move[1] + 1);
    }
}
