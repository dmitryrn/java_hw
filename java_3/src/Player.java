import java.util.Scanner;

public abstract class Player {
    private final String name;
    private final Mark mark;

    protected Player(String name, Mark mark) {
        this.name = name;
        this.mark = mark;
    }

    public String getName() {
        return name;
    }

    public Mark getMark() {
        return mark;
    }

    public abstract void makeMove(Board board, Scanner scanner);
}
