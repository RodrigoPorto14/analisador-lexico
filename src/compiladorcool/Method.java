package compiladorcool;

public class Method {
    private String type;
    private String[] args;
    
    public Method(String type, String... args)
    {
        this.type=type;
        this.args=args;
    }
    
    public String getType(){ return type; }
    
    public String getMethod()
    {
        String str = "(";
        for(var t : args)
        {
            str+=t;
            str+=',';
        }
        str+=String.format(") : %s", type);
        return str;
    }
}
