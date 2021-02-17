package za.co.entelect.challenge.command;


public class ThrowBananaCommand implements Command {

    private final int x;
    private final int y;

    public ThrowBananaCommand(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String render() {
        return String.format("banana %s %s", x, y);
    }
}
