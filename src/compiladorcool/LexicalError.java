package compiladorcool;

public class LexicalError extends Error{
    private final String msg;
    
    public LexicalError(String msg, int row)
    {
        super(row);
        this.msg=msg;
    }
    
    @Override
    public String getMessage()
    {
        return String.format("Erro lexico, linha %d: %s",row,msg);
    }
}
