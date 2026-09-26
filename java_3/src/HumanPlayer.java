import java.util.Scanner;

public class HumanPlayer extends Player {
    public HumanPlayer(String name, Mark mark) {
        super(name, mark);
    }

    @Override
    public void makeMove(Board board, Scanner scanner) {
        while (true) {
            System.out.printf("%s (%s), enter row and column (1-3), e.g. 2 3: ",
                    getName(), getMark());

            if (!scanner.hasNextInt()) {
                String invalidInput = scanner.next();
                System.out.printf("'%s' is not a valid row. Enter two numbers from 1 to 3.%n",
                        invalidInput);
                continue;
            }
            int row = scanner.nextInt();

            if (!scanner.hasNextInt()) {
                String invalidInput = scanner.next();
                System.out.printf("'%s' is not a valid column. Enter two numbers from 1 to 3.%n",
                        invalidInput);
                continue;
            }
            int col = scanner.nextInt();

            if (board.placeMark(row - 1, col - 1, getMark())) {
                return;
            }
            System.out.println("That square is outside the board or already occupied. Try again.");
        }
    }
}
