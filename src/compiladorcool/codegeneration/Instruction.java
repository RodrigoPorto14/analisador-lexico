package compiladorcool.codegeneration;

import java.util.ArrayList;

public class Instruction implements InstructionInterface{
    private String op;
    private String dest;
    private String type;
    private ArrayList<String> args;
    private ArrayList<String> funcs;
    private ArrayList<String> labels;
}
