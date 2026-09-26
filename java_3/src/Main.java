import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Player player1 = new HumanPlayer("Player 1", Mark.X);
        Player player2 = new HumanPlayer("Player 2", Mark.O);
        Board board = new Board();
        Game game = new Game(board, player1, player2, scanner);

        game.startGame();
    }
}
