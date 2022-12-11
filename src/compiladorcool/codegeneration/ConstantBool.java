package compiladorcool.codegeneration;

public class ConstantBool extends Instruction{
    
    private boolean value;
    
    public ConstantBool(boolean value,String dest)
    {
        super("const");
        super.setType("bool");
        super.setDest(dest);
        this.value=value;
    }
}
