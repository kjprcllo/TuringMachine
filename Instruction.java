public class Instruction {
    public final int lineNumber;
    public final String command;
    public final char symbol;
    public final String targetLabel;

    public Instruction(int lineNumber, String command, char symbol, String targetLabel) {
        this.lineNumber = lineNumber;
        this.command = command;
        this.symbol = symbol;
        this.targetLabel = targetLabel;
    }

    @Override 
    public String toString() {
        switch (command) {
            case "WRITE": return "WRITE " + symbol;
            case "IF": return "IF " + symbol + " GOTO " + targetLabel;
            case "GOTO": return "GOTO " + targetLabel;
            default: return command; 
        }
    }
}
