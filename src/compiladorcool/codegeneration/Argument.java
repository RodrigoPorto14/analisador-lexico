package compiladorcool.codegeneration;

public class Argument extends Arg{
   
    private String type;
    
    public Argument(String name,String type)
    {
        super(name);
        this.type=type;
    }
}
