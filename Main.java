import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("===Tape Turing Machine===");

        ProgramParser.ParseResult parsed = null;
        while (parsed == null) {
            System.out.print("Enter program file path: ");
            String filePath = scanner.nextLine().trim();
            try {
                parsed = ProgramParser.parseFile(filePath);
                System.out.println("Program loaded successfully (" + parsed.instructions.size() + " instructions).");
            } catch (IOException e) {
                System.out.println("File Error: Could not read file '" + filePath + "'. Try again.");
            } catch (IllegalArgumentException e) {
                System.out.println("Validation Error: " + e.getMessage());
                System.out.println("Execution cannot begin until the program is valid.\n");
            }
        }

        String inputTape = null;
        while (inputTape == null) {
            System.out.print("Enter input tape (press ENTER for empty input): ");
            String rawInput = scanner.nextLine();
            try {
                ProgramParser.validateInputTape(rawInput);
                inputTape = rawInput;
            } catch (IllegalArgumentException e) {
                System.out.println("Input Error: " + e.getMessage());
            }
        }

        int stepLimit = 10000;
        while (true) {
            System.out.print("Enter step limit (Between: 1-10000; press ENTER for default 10000): ");
            String limitStr = scanner.nextLine().trim();
            if (limitStr.isEmpty()) {
                break;
            }
            try {
                int val = Integer.parseInt(limitStr);
                if (val <= 0 || val > 10000) {
                    System.out.println("Invalid step limit: value must be between 1 and 10000.");
                } else {
                    stepLimit = val;
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid step limit: '" + limitStr + "' is not a valid integer (must be 1-10000).");
            }
        }

        MachineState state = new MachineState(parsed.instructions, parsed.labels, inputTape, stepLimit);
        displayConfiguration(state);

        boolean runningCli = true;
        while (runningCli) {
            System.out.print("\nCommand [step | run | reset | quit]: ");
            String choice = scanner.nextLine().trim().toLowerCase();

            switch (choice) {
                case "step":
                case "s":
                    if (ExecutionEngine.isTerminal(state)) {
                        System.out.println("Machine is in terminal status (" + state.status + "). Type 'reset' before executing again.");
                    } else {
                        ExecutionEngine.step(state);
                        displayConfiguration(state);
                    }
                    break;

                case "run":
                case "r":
                    if (ExecutionEngine.isTerminal(state)) {
                        System.out.println("Machine is in terminal status (" + state.status + "). Type 'reset' before executing again.");
                    } else {
                        ExecutionEngine.run(state);
                        displayConfiguration(state);
                    }
                    break;

                case "reset":
                    state.reset();
                    System.out.println("Machine reset to initial state.");
                    displayConfiguration(state);
                    break;

                case "quit":
                case "exit":
                case "q":
                    runningCli = false;
                    System.out.println("Exiting simulator.");
                    break;

                default:
                    System.out.println("Unknown command. Use: step, run, reset, or quit.");
            }
        }

        scanner.close();
    }

    public static void displayConfiguration(MachineState state) {
        int maxIndex = state.head;
        for (int idx : state.tape.keySet()) {
            if (idx > maxIndex) {
                maxIndex = idx;
            }
        }

        StringBuilder indexRow = new StringBuilder("Index : ");
        StringBuilder tapeRow  = new StringBuilder("Tape  : ");
        StringBuilder headRow  = new StringBuilder("Head  : ");

        for (int i = 0; i <= maxIndex; i++) {
            String idxLabel = String.valueOf(i);
            int width = Math.max(idxLabel.length(), 1) + 2;

            indexRow.append(String.format("%-" + width + "s", idxLabel));
            tapeRow.append(String.format("%-" + width + "s", state.tape.getOrDefault(i, '_')));
            headRow.append(String.format("%-" + width + "s", (i == state.head) ? "^" : ""));
        }

        System.out.println("\n--------------------------------------------------");
        System.out.println(indexRow);
        System.out.println(tapeRow);
        System.out.println(headRow);
        System.out.println("Head Index : " + state.head);
        System.out.println("Total Steps: " + state.steps + " / " + state.stepLimit);
        System.out.println("Status     : " + state.status);

        if (state.pc >= 0 && state.pc < state.instructions.size()) {
            Instruction instr = state.instructions.get(state.pc);
            String label = state.status.equals("HALTED") ? "Terminal Instruction: " : "Next Instruction    : ";
            System.out.println(label + "[Line " + instr.lineNumber + "] " + instr);
        } else {
            System.out.println("Next Instruction    : <None / Past End>");
        }

        if (!state.errorMessage.isEmpty()) {
            System.out.println("Message    : " + state.errorMessage);
        }
        System.out.println("--------------------------------------------------");
    }
}
