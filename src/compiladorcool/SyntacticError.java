package compiladorcool;

public class SyntacticError extends Error{
    private final String lastToken;
    public TokenType[] expectedTokens; 
    
    public SyntacticError(String lastToken,int row,TokenType... expectedTokens)
    {
        super(row);
        this.lastToken=lastToken;
        this.expectedTokens=expectedTokens;
    }
    
    @Override
    public String getMessage()
    {
        String msg = String.format("Erro sintatico, linha %d: esperado ",row);
        for(var token : expectedTokens)
        {
            msg += String.format("'%s' ou ",token.toString());
        }
        msg = msg.substring(0,msg.length()-3);
        msg+= String.format("depois de '%s'",lastToken);
        return msg; 
    }
}
