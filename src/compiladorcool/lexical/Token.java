package compiladorcool.lexical;

public class Token {
    
    private final String description;
    private final TokenType type;
    private final int row;
    
    public Token(String description, TokenType type, int row)
    {
        this.description=description;
        this.type=type;
        this.row=row;
    }
    
    public String getDescription(){return this.description;}
    public TokenType getType(){return this.type;}
    public int getRow(){return this.row;}
}
