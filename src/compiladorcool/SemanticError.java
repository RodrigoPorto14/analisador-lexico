package compiladorcool;

public class SemanticError extends Error{
    private final String msg;
    
    public SemanticError(String msg, int row)
    {
        super(row);
        this.msg=msg;
    }
    
    @Override
    public String getMessage()
    {
        return String.format("Erro semantico, linha %d: %s",row,msg);
    }
}
