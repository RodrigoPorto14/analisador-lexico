package compiladorcool;
import java.util.ArrayList;

public class Analiser {
    private ArrayList<Token> tokens;
    private int tokenId;
    
    public Analiser(ArrayList<Token> tokens)
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
    
    public void nextToken()
    {
        tokenId++;
    }
}
