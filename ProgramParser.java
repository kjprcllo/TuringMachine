import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ProgramParser {

    public static class ParseResult {
        public final List<Instruction> instructions;
        public final Map<String, Integer> labels;

        public ParseResult(List<Instruction> instructions, Map<String, Integer> labels) {
            this.instructions = instructions;
            this.labels = labels;
        }
    }

    public static ParseResult parseFile(String filePath) throws IOException, IllegalArgumentException {
        List<String> lines = Files.readAllLines(Path.of(filePath));
        return parseLines(lines);
    }

    public static ParseResult parseLines(List<String> lines) throws IllegalArgumentException {
        List<Instruction> instructions = new ArrayList<>();
        Map<String, Integer> labels = new HashMap<>();

        for (int i = 0; i < lines.size(); i++) {
            int lineNum = i + 1;
            String line = lines.get(i).trim();

            if (line.isEmpty()) {
                continue; 
            }

            int colonIdx = line.indexOf(':');
            if (colonIdx != -1) {
                String label = line.substring(0, colonIdx).trim();
                if (!label.matches("[A-Za-z][A-Za-z0-9_]*")) {
                    throw new IllegalArgumentException("Line " + lineNum + ": Invalid label '" + label + "'.");
                }
                if (labels.containsKey(label)) {
                    throw new IllegalArgumentException("Line " + lineNum + ": Duplicate label '" + label + "'.");
                }

                String remainder = line.substring(colonIdx + 1).trim();
                if (remainder.isEmpty()) {
                    throw new IllegalArgumentException("Line " + lineNum + ": Label-only line is invalid.");
                }

                labels.put(label, instructions.size());
                line = remainder;
            }

            String[] tokens = line.split("[ \\t]+");
            String cmd = tokens[0];

            switch (cmd) {
                case "LEFT":
                case "HALT":
                    if (tokens.length != 1) {
                        throw new IllegalArgumentException("Line " + lineNum + ": '" + cmd + "' takes no arguments.");
                    }
                    instructions.add(new Instruction(lineNum, cmd, '\0', null));
                    break;

                case "RIGHT":
                    if (tokens.length != 1) {
                        throw new IllegalArgumentException("Line " + lineNum + ": 'RIGHT' takes no arguments.");
                    }
                    instructions.add(new Instruction(lineNum, "RIGHT", '\0', null));
                    break;

                case "WRITE":
                    if (tokens.length != 2) {
                        throw new IllegalArgumentException("Line " + lineNum + ": 'WRITE' requires exactly 1 symbol argument.");
                    }
                    if (tokens[1].length() != 1) {
                        throw new IllegalArgumentException("Line " + lineNum + ": Symbol '" + tokens[1] + "' must be a single character.");
                    }
                    instructions.add(new Instruction(lineNum, "WRITE", tokens[1].charAt(0), null));
                    break;

                case "GOTO":
                    if (tokens.length != 2) {
                        throw new IllegalArgumentException("Line " + lineNum + ": 'GOTO' requires exactly 1 label argument.");
                    }
                    instructions.add(new Instruction(lineNum, "GOTO", '\0', tokens[1]));
                    break;

                case "IF":
                    if (tokens.length != 4 || !tokens[2].equals("GOTO")) {
                        throw new IllegalArgumentException("Line " + lineNum + ": Invalid 'IF' syntax. Expected: IF <symbol> GOTO <label>");
                    }
                    if (tokens[1].length() != 1) {
                        throw new IllegalArgumentException("Line " + lineNum + ": Symbol '" + tokens[1] + "' must be a single character.");
                    }
                    instructions.add(new Instruction(lineNum, "IF", tokens[1].charAt(0), tokens[3]));
                    break;

                default:
                    throw new IllegalArgumentException("Line " + lineNum + ": Unknown command '" + cmd + "'. Valid commands are LEFT, RIGHT, WRITE, GOTO, IF, and HALT.");
            }
        }

        if (instructions.isEmpty()) {
            throw new IllegalArgumentException("Program is empty: no executable instructions found.");
        }

        for (Instruction instr : instructions) {
            if (instr.targetLabel != null && !labels.containsKey(instr.targetLabel)) {
                throw new IllegalArgumentException("Line " + instr.lineNumber + ": Undefined jump target '" + instr.targetLabel + "'.");
            }
        }

        return new ParseResult(instructions, labels);
    }

    public static void validateInputTape(String input) throws IllegalArgumentException {
        for (int i = 0; i < input.length(); i++) {
            if (Character.isWhitespace(input.charAt(i))) {
                throw new IllegalArgumentException("Input tape cannot contain whitespace characters.");
            }
        }
    }
}