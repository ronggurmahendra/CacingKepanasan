package za.co.entelect.challenge.command;

public class ThrowSnowballCommand implements Command {
    private final int x;
    private final int y;

    public ThrowSnowballCommand(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String render() {
        return String.format("snowball %s %s", x, y);
    }

}
