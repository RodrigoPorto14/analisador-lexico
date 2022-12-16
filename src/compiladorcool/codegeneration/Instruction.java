package compiladorcool.codegeneration;

public class Instruction extends Instr{
    
    private String type;
    
    public Instruction(String op){ super(op); }
    
    @Override
    public void setType(String type) { this.type = type; }
    
}
