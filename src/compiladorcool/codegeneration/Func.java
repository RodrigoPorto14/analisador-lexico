package compiladorcool.codegeneration;

import java.util.ArrayList;

public class Func {
    
    private String name;
    private Arg[] args;
    private ArrayList<InstructionInterface> instrs;
    
    public Func(String name)
    {
        this.name=name;
        instrs = new ArrayList<>();
    }

    public void setArgs(Arg... args) { this.args = args; }

    public void addInstruction(InstructionInterface instr) { instrs.add(instr); }
    
    public ArrayList<InstructionInterface> getInstructions() { return instrs; }
}