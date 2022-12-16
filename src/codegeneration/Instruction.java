package compiladorcool.codegeneration;

import java.util.ArrayList;

public class Instruction implements InstructionInterface{
    private String op;
    private String dest;
    private String type;
    private String[] args;
    private String[] funcs;
    private String[] labels;
    
    public Instruction(String op) { this.op = op; }

    public void setDest(String dest) { this.dest = dest; }

    public void setType(String type) { this.type = type; }

    public void setArgs(String... args) { this.args = args; }

    public void setFuncs(String... funcs) { this.funcs = funcs; }
    
    public void setLabels(String... labels) { this.labels = labels; }

}
