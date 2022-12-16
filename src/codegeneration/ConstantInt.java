package compiladorcool.codegeneration;

public class ConstantInt extends Instruction{
    
    private int value;
    
    public ConstantInt(int value,String dest)
    {
        super("const");
        super.setType("int");
        super.setDest(dest);
        this.value=value;
        
    }
}
