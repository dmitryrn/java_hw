import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Player player1 = PlayerFactory.createPlayer("human", "Player 1", Mark.X);
        Player player2 = PlayerFactory.createPlayer("human", "Player 2", Mark.O);
        Board board = Board.builder()
                .size(3)
                .build();
        Game game = Game.getInstance(board, player1, player2, scanner);

        game.startGame();
    }
}
