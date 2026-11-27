public class ExecutionEngine {

    public static void step(MachineState state) {
        // After a terminal status, require Reset before executing again
        if (isTerminal(state)) {
            return;
        }

        // Guard against starting at or beyond the step limit
        if (state.steps >= state.stepLimit) {
            state.status = "LIMIT REACHED";
            return;
        }

        // Check if PC fell past the final instruction without executing HALT
        if (state.pc < 0 || state.pc >= state.instructions.size()) {
            state.status = "ERROR";
            state.errorMessage = "Runtime error: Program fell past the final instruction without executing HALT.";
            return;
        }

        state.status = "RUNNING";
        Instruction instr = state.instructions.get(state.pc);
        state.steps++; // Count every executed instruction as 1 step

        switch (instr.command) {
            case "LEFT":
                if (state.head > 0) {
                    state.head--;
                }
                // If already at 0, stay at 0; still counts as 1 step
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
                // Keep PC on HALT and show halted status
                state.status = "HALTED";
                break;
        }

        // Check if we fell off the end immediately after advancing PC
        if (state.status.equals("RUNNING") && state.pc >= state.instructions.size()) {
            state.status = "ERROR";
            state.errorMessage = "Runtime error: Program fell past the final instruction without executing HALT.";
        }

        // If the last permitted step executed HALT or caused ERROR, keep HALTED/ERROR.
        // Only set LIMIT REACHED if the machine is still RUNNING at the limit.
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