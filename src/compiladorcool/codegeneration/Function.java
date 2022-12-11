package compiladorcool.codegeneration;

import java.util.ArrayList;

public class Function {
    
    private String name;
    private Argument[] args;
    private String type;
    private ArrayList<InstructionInterface> instrs;
    
    public Function(String name)
    {
        this.name=name;
        instrs = new ArrayList<>();
    }

    public void setArgs(Argument... args) { this.args = args; }

    public void setType(String type) { this.type = type; }

    public void addInstruction(InstructionInterface instr) { instrs.add(instr); }
}
