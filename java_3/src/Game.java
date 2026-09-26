import java.util.Scanner;

public class Game {
    private final Board board;
    private final Player player1;
    private final Player player2;
    private final Scanner scanner;
    private Player currentPlayer;

    public Game(Board board, Player player1, Player player2, Scanner scanner) {
        this.board = board;
        this.player1 = player1;
        this.player2 = player2;
        this.scanner = scanner;
        this.currentPlayer = player1;
    }

    public void startGame() {
        System.out.println("Welcome to Tic-Tac-Toe!");

        while (true) {
            board.displayBoard();
            currentPlayer.makeMove(board, scanner);

            if (checkForWinOrDraw()) {
                board.displayBoard();
                return;
            }

            switchPlayer();
        }
    }

    public void switchPlayer() {
        currentPlayer = currentPlayer == player1 ? player2 : player1;
    }

    public boolean checkForWinOrDraw() {
        if (board.checkWin(currentPlayer.getMark())) {
            System.out.printf("%s (%s) wins!%n", currentPlayer.getName(), currentPlayer.getMark());
            return true;
        }

        if (board.isFull()) {
            System.out.println("It's a draw!");
            return true;
        }

        return false;
    }
}
