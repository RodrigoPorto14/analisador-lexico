package compiladorcool;

import java.util.ArrayList;
import java.util.Arrays;

public class SyntacticAnaliser {
    
    private final static int MAX_BUFFER_SIZE = 10;
    private final LexicalAnaliser lexical;
    private final ArrayList<Error> errors;
    private final ArrayList<Token> bufferTokens;
    private final ArrayList<TokenType> firstExpr,firstExpr2;
    private Token lastToken;
    
    public SyntacticAnaliser(LexicalAnaliser lexical, ArrayList<Error> errors)
    {
        this.errors=errors;
        this.lexical=lexical;
        bufferTokens = new ArrayList<>();
        firstExpr = new ArrayList<>();
        firstExpr2 = new ArrayList<>();
        addFirstExpr();
        nextToken();
    }
    
    private void nextToken()
    {
        if(!bufferTokens.isEmpty()) lastToken = bufferTokens.remove(0);
        while(bufferTokens.size()<MAX_BUFFER_SIZE)
        {
            Token next = lexical.nextToken();
            if(next==null) break;
            bufferTokens.add(next);
        }
    }
    
    private Token lookAHead(int k)
    {
        if(k-1 > bufferTokens.size()-1) return null;
        return bufferTokens.get(k-1);
    }
    
    private boolean nextTokenIs(TokenType type) {return lookAHead(1)!=null && lookAHead(1).getType()==type;}
    
    private boolean nextTokenIn(ArrayList<TokenType> typeArray) {return lookAHead(1)!=null && typeArray.contains(lookAHead(1).getType());}
    
    private void match(TokenType type) throws Exception
    {
        if(nextTokenIs(type)) nextToken();
        else
        {
            createError(type);
            throw new Exception();
        }
    }
    
     private void createError(TokenType... types)
    {
        if(lastToken!=null) errors.add(new SyntacticError(lastToken.getDescription(),lastToken.getRow(),types));
        else errors.add(new SyntacticError("inicio do arquivo",1,types));
        ignoreUntil(TokenType.SEMICOLON);       
    }
     
    private void ignoreUntil(TokenType type)
    {  
        while(lookAHead(1)!=null && lookAHead(1).getType()!=type) nextToken();
    }
    
    private void addFirstExpr()
    {
        TokenType[] firstExprArray = {TokenType.OBJECT_IDENTIFIER,TokenType.IF,TokenType.WHILE,TokenType.OPEN_BRACES,TokenType.LET,
                                      TokenType.CASE,TokenType.NEW,TokenType.ISVOID,TokenType.COMPLEMENT,TokenType.NOT,
                                      TokenType.OPEN_PARENTHESES,TokenType.INTEGER,TokenType.STRING,TokenType.TRUE,TokenType.FALSE};
        
        TokenType[] firstExprArray2 = {TokenType.ADD,TokenType.SUB,TokenType.MULT,TokenType.DIV,TokenType.LT,TokenType.LTE,TokenType.EQUAL,TokenType.AT,TokenType.DOT};
        
        firstExpr.addAll(Arrays.asList(firstExprArray));
        firstExpr2.addAll(Arrays.asList(firstExprArray2));
    }
    
    public void analise(){program();}
    
    private void program()
    {
        try
        {
            do
            {
                _class();
                match(TokenType.SEMICOLON);
            }
            while(lookAHead(1)!=null);
        }
        catch(Exception e){}
    }
    
    private void _class()
    {
        try
        {
            match(TokenType.CLASS);
            match(TokenType.TYPE_IDENTIFIER);
            if(nextTokenIs(TokenType.INHERITS))
            {
                match(TokenType.INHERITS);
                match(TokenType.TYPE_IDENTIFIER);
            }
            match(TokenType.OPEN_BRACES);
            while(nextTokenIs(TokenType.OBJECT_IDENTIFIER))
            {
                feature();
                match(TokenType.SEMICOLON);
            }
            match(TokenType.CLOSE_BRACES);
        }
        catch(Exception e){}    
    }
    
    private void feature()
    {
        try 
        {
            match(TokenType.OBJECT_IDENTIFIER);
            if(nextTokenIs(TokenType.OPEN_PARENTHESES)) method();
            else if(nextTokenIs(TokenType.COLON)) attribute();
            else createError(TokenType.OPEN_PARENTHESES,TokenType.COLON);
        }
        catch(Exception e){}
    }
    
    private void method()
    {
        
        try
        {
            match(TokenType.OPEN_PARENTHESES);
            if(nextTokenIs(TokenType.OBJECT_IDENTIFIER))
            {
                formal();
                while(nextTokenIs(TokenType.COMMA))
                {
                    match(TokenType.COMMA);
                    formal();
                }
            }
            match(TokenType.CLOSE_PARENTHESES);
            match(TokenType.COLON);
            match(TokenType.TYPE_IDENTIFIER);
            match(TokenType.OPEN_BRACES);
            expr();
            match(TokenType.CLOSE_BRACES);
        }
        catch(Exception e){}  
    }
    private void attribute()
    {
        try
        {
            match(TokenType.COLON);
            match(TokenType.TYPE_IDENTIFIER);
            if(nextTokenIs(TokenType.ASSIGN))
            {
                match(TokenType.ASSIGN);
                expr();
            }
        }
        catch(Exception e){}
    }
    
    private void formal()
    {
        try
        {
            match(TokenType.OBJECT_IDENTIFIER);
            match(TokenType.COLON);
            match(TokenType.TYPE_IDENTIFIER);
        }
        catch(Exception e){}
    }
    
    private void expr()
    {
        try
        {
            if(nextTokenIn(firstExpr))
            {
                switch(lookAHead(1).getType())
                {
                    case OBJECT_IDENTIFIER -> id();
                    case IF -> _if();
                    case WHILE -> _while();
                    case OPEN_BRACES -> block();
                    case LET -> let();
                    case CASE -> _case();
                    case NEW -> { match(TokenType.NEW); match(TokenType.TYPE_IDENTIFIER); }
                    case ISVOID -> { match(TokenType.ISVOID); expr(); }
                    case COMPLEMENT -> { match(TokenType.COMPLEMENT); expr(); }
                    case NOT -> { match(TokenType.NOT); expr(); }
                    case OPEN_PARENTHESES -> { match(TokenType.OPEN_PARENTHESES); expr(); match(TokenType.CLOSE_PARENTHESES); }
                    case INTEGER -> match(TokenType.INTEGER);
                    case STRING -> match(TokenType.STRING);
                    case TRUE -> match(TokenType.TRUE);
                    case FALSE -> match(TokenType.FALSE);
                }
            }
            else createError(TokenType.EXPR);
            while(nextTokenIn(firstExpr2)) expr2();
        }
        catch(Exception e){}
    }
    
    private void expr2()
    {
        try
        {
            if(nextTokenIs(TokenType.AT))
            {
                match(TokenType.AT);
                match(TokenType.TYPE_IDENTIFIER);
                match(TokenType.DOT);
                match(TokenType.OBJECT_IDENTIFIER);
                methodCall();
            }
            else if(nextTokenIs(TokenType.DOT))
            {
                match(TokenType.DOT);
                match(TokenType.OBJECT_IDENTIFIER);
                methodCall();
            }
            else
            {
               match(lookAHead(1).getType());
               expr(); 
            }
                 
        }
        catch(Exception e){}
    }
    
    private void id()
    {
        try
        {
            match(TokenType.OBJECT_IDENTIFIER);
            if(nextTokenIs(TokenType.OPEN_PARENTHESES)) methodCall();
            else if(nextTokenIs(TokenType.ASSIGN)) assignment();   
        }
        catch(Exception e){}
    }
    
    private void methodCall()
    {
        try
        {
            match(TokenType.OPEN_PARENTHESES);
            if(nextTokenIn(firstExpr))
            {
                expr();
                while(nextTokenIs(TokenType.COMMA))
                {
                    match(TokenType.COMMA);
                    expr();
                }
            }
            match(TokenType.CLOSE_PARENTHESES); 
        }
        catch(Exception e){}
    }
    
    private void assignment()
    {
        try
        {
            match(TokenType.ASSIGN);
            expr();
        }
        catch(Exception e){}
    }
    
    private void _if()
    {
        try
        {
            match(TokenType.IF);
            expr();
            match(TokenType.THEN);
            expr();
            match(TokenType.ELSE);
            expr();
            match(TokenType.FI);
        }
        catch(Exception e){} 
    }
    
    private void _while()
    {
        try
        {
            match(TokenType.WHILE);
            expr();
            match(TokenType.LOOP);
            expr();
            match(TokenType.POOL);
        }
        catch(Exception e){} 
    }
    
    private void block()
    {
        try
        {
            match(TokenType.OPEN_BRACES);
            do
            {
                expr();
                match(TokenType.SEMICOLON);
            }
            while(nextTokenIn(firstExpr));
            match(TokenType.CLOSE_BRACES);
        }
        catch(Exception e){} 
    }
    
    private void let()
    {
        boolean secondLoop = false;
        try
        {
            match(TokenType.LET);
            do
            {
                if(secondLoop) match(TokenType.COMMA);
                match(TokenType.OBJECT_IDENTIFIER);
                match(TokenType.COLON);
                match(TokenType.TYPE_IDENTIFIER);
                if(nextTokenIs(TokenType.ASSIGN))
                {
                    match(TokenType.ASSIGN);
                    expr();
                }
                secondLoop=true;
            }            
            while(nextTokenIs(TokenType.COMMA));
            match(TokenType.IN);
            expr();
        }
        catch(Exception e){}
    }
    
    private void _case()
    {
        try
        {
            match(TokenType.CASE);
            expr();
            match(TokenType.OF);
            do
            { 
                match(TokenType.OBJECT_IDENTIFIER);
                match(TokenType.COLON);
                match(TokenType.TYPE_IDENTIFIER);
                match(TokenType.ARROW);
                expr();
                match(TokenType.SEMICOLON);
            }while(nextTokenIs(TokenType.OBJECT_IDENTIFIER));
            match(TokenType.ESAC);   
        }
        catch(Exception e){} 
    }           
}
