package compiladorcool.codegeneration;

public class Operation extends Instr{
    
    private Pointer type;
    
    public Operation(String op){ super(op); }
    
    @Override
    public void setType(String type){ this.type = new Pointer("int"); }
    
}