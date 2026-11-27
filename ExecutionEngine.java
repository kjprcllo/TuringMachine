public class ExecutionEngine {

    public static void step(MachineState state) {
        if (isTerminal(state)) {
            return;
        }

        if (state.steps >= state.stepLimit) {
            state.status = "LIMIT REACHED";
            return;
        }

        if (state.pc < 0 || state.pc >= state.instructions.size()) {
            state.status = "ERROR";
            state.errorMessage = "Runtime error: Program fell past the final instruction without executing HALT.";
            return;
        }

        state.status = "RUNNING";
        Instruction instr = state.instructions.get(state.pc);
        state.steps++; 

        switch (instr.command) {
            case "LEFT":
                if (state.head > 0) {
                    state.head--;
                }
                state.pc++;
                break;

            case "RIGHT":
                state.head++;
                state.pc++;
                break;

            case "WRITE":
                state.write(instr.symbol);
                state.pc++;
                break;

            case "IF":
                if (state.read() == instr.symbol) {
                    state.pc = state.labels.get(instr.targetLabel);
                } else {
                    state.pc++;
                }
                break;

            case "GOTO":
                state.pc = state.labels.get(instr.targetLabel);
                break;

            case "HALT":
                state.status = "HALTED";
                break;
        }

        if (state.status.equals("RUNNING") && state.pc >= state.instructions.size()) {
            state.status = "ERROR";
            state.errorMessage = "Runtime error: Program fell past the final instruction without executing HALT.";
        }

        if (state.status.equals("RUNNING") && state.steps >= state.stepLimit) {
            state.status = "LIMIT REACHED";
        }
    }

    public static void run(MachineState state) {
        while (!isTerminal(state)) {
            step(state);
        }
    }

    public static boolean isTerminal(MachineState state) {
        return state.status.equals("HALTED")
            || state.status.equals("LIMIT REACHED")
            || state.status.equals("ERROR");
    }
}