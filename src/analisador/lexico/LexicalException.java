package analisador.lexico;

public class LexicalException extends Exception{
    
    private final String errorMsg;
    private final int row;
    
    public LexicalException(String errorMsg,int row)
    {
        this.errorMsg = errorMsg;
        this.row = row;
    }
    
    @Override
    public String getMessage()
    {
        return String.format("Erro lexico, linha %d: %s",row,errorMsg);
    }
    
}
