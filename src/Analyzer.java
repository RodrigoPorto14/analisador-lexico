package compiladorcool;
import compiladorcool.lexical.Token;
import java.util.ArrayList;

public abstract class Analyzer {
    protected ArrayList<Token> tokens;
    protected int tokenId;
    
    public Analyzer(ArrayList<Token> tokens)
    {
        this.tokens=tokens;
        this.tokenId=0;
    }
    
    public Token lookAHead(int k)
    {
        int id = tokenId+(k-1);
        if(id>=tokens.size()) return null;
        return tokens.get(id);
    }
    
    public abstract Token nextToken();
    
}
