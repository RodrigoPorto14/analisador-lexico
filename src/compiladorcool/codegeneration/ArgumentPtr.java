package compiladorcool.codegeneration;

public class ArgumentPtr extends Arg{
   
    private Pointer type;
    
    public ArgumentPtr(String name)
    {
        super(name);
        this.type= new Pointer("int");
    }
}
