import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MachineState {
    public final List<Instruction> instructions;
    public final Map<String, Integer> labels;
    public final String initialInput;
    public final int stepLimit;

    public Map<Integer, Character> tape;
    public int head;
    public int pc;
    public int steps;
    public String status;
    public String errorMessage;

    public MachineState(List<Instruction> instructions, Map<String, Integer> labels, String inputTape, int stepLimit) {
        this.instructions = instructions;
        this.labels = labels;
        this.initialInput = inputTape;
        this.stepLimit = stepLimit;
        reset();
    }

    public void reset() {
        this.tape = new HashMap<>();
        for (int i = 0; i < initialInput.length(); i++) {
            char c = initialInput.charAt(i);
            if (c != '_') {
                tape.put(i, c);
            }
        }
        this.head = 0;
        this.pc = 0;
        this.steps = 0;
        this.status = "READY";
        this.errorMessage = "";
    }

    public char read() {
        return tape.getOrDefault(head, '_');
    }

    public void write(char symbol) {
        if (symbol == '_') {
            tape.remove(head); 
        } else {
            tape.put(head, symbol);
        }
    }

}

