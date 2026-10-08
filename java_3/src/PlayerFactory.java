import java.util.Locale;

public final class PlayerFactory {
    private PlayerFactory() {
    }

    public static Player createPlayer(String type, String name, Mark mark) {
        if (type == null) {
            throw new IllegalArgumentException("Player type cannot be null");
        }

        switch (type.trim().toLowerCase(Locale.ROOT)) {
            case "human":
                return new HumanPlayer(name, mark);
            case "ai":
                return new AIPlayer(name, mark);
            default:
                throw new IllegalArgumentException("Unknown player type: " + type);
        }
    }
}
