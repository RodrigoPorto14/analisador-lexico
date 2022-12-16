package compiladorcool.codegeneration;

import java.util.ArrayList;

public abstract class Instr implements InstructionInterface{
    private String op;
    private String dest;
    private String[] args;
    private String[] funcs;
    private String[] labels;
    
    public Instr(String op) { this.op = op; }

    public void setDest(String dest) { this.dest = dest; }
    
    public abstract void setType(String type);

    public void setArgs(String... args) { this.args = args; }

    public void setFuncs(String... funcs) { this.funcs = funcs; }
    
    public void setLabels(String... labels) { this.labels = labels; }

}
