package za.co.entelect.challenge.command;

public class UseToken implements Command {
    private int ID;
    private Command cmd;

    public UseToken(int ID,Command cmd) {
        this.ID = ID;
        this.cmd = cmd;
    }

    @Override
    public String render() {
        return String.format("select %d;%s", ID,cmd.render());
    }

}


