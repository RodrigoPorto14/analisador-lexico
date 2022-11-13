package compiladorcool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;

public class SyntacticAnaliser {
    
    private final static int MAX_BUFFER_SIZE = 10;
    private final LexicalAnaliser lexical;
    private final ArrayList<Error> errors;
    private final ArrayList<Token> bufferTokens;
    private final ArrayList<TokenType> firstExpr,firstExpr2;
    private final Queue<Node> syntacticTree;
    private final ArrayList<Node> nodesBuffer;
    private Token lastToken;
    private boolean chainStarted=false;
    private int chainPos;
    private final ArrayList<Integer> nodesId = new ArrayList<>();
    
    public SyntacticAnaliser(LexicalAnaliser lexical, ArrayList<Error> errors)
    {
        this.errors=errors;
        this.lexical=lexical;
        bufferTokens = new ArrayList<>();
        firstExpr = new ArrayList<>();
        firstExpr2 = new ArrayList<>();
        syntacticTree = new LinkedList<>();
        nodesBuffer = new ArrayList<>();
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
    
    private boolean nextTokenIn(ArrayList<TokenType> typeArray,int k) {return lookAHead(k)!=null && typeArray.contains(lookAHead(k).getType());}
    
    private void match(TokenType type) throws SyntacticException
    {
        if(nextTokenIs(type)) nextToken();
        else
        {
            createError(type);
            throw new SyntacticException();
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
    
    private void addNodesId(int pos, int lvl)
    {
        nodesId.add(pos);
        pos++;
        lvl++;
        while(nodesBuffer.size()>pos && nodesBuffer.get(pos).getLevel()>=lvl)
        {
            if(nodesBuffer.get(pos).getLevel()==lvl) addNodesId(pos,lvl);
            pos++;
        }  
    }
    
    private void upNodes(int add)
    {
        for(var id : nodesId) nodesBuffer.get(id).setLevel(nodesBuffer.get(id).getLevel()+add);
        nodesId.clear();
    }
    
    private int nextNodePos(int pos, int lvl)
    {
        while(nodesBuffer.size()>pos)
        {
            if(nodesBuffer.get(pos).getLevel()==lvl+1) return pos;
            pos++;
        }
        return 0;
    }
    
    private void fixPrecedence(NodeType type,int nodeLvl,int pos)
    {
        HashMap<NodeType,Integer> precedence = new HashMap<>();
        precedence.put(NodeType.EQUAL,1); precedence.put(NodeType.COMPARE,1);
        precedence.put(NodeType.ARITHMETIC,2);
        
        for(int i=pos-1;i>=chainPos;i--)
        {
            Node node = nodesBuffer.get(i);
            
            if(node.getLevel()==nodeLvl-1 && precedence.containsKey(node.getType()) && precedence.get(type) < precedence.get(node.getType()))
            {
                addNodesId(i+1,node.getLevel()+1);
                upNodes(1);
                node.setLevel(node.getLevel()+1);
                addNodesId(nextNodePos(pos+2,nodeLvl),nodeLvl+1);
                upNodes(-1);
                saveNodeAt(type,nodeLvl-1,i);
                nodesBuffer.remove(++pos);
                pos=i;
                nodeLvl--;
            }  
        }  
    }
         
    private void saveNode(NodeType nodeType, int nodeLvl) { nodesBuffer.add(new Node(nodeType,nodeLvl)); }   
    private void saveNodeAt(NodeType nodeType, int nodeLvl, int pos){ nodesBuffer.add(pos, new Node(nodeType,nodeLvl)); }
    private void upBuffer(int lvl,int pos)
    {
        for(int i=pos;i<nodesBuffer.size();i++) nodesBuffer.get(i).setLevel(nodesBuffer.get(i).getLevel()+lvl);
    }
    private void dumpBuffer()
    {
        syntacticTree.addAll(nodesBuffer);
        nodesBuffer.clear();
    }
    
    public Queue<Node> analise()
    {
        program(0);
        dumpBuffer();
        return syntacticTree;
    }
    
    private void program(int nodeLvl)
    {
        try
        {
            do
            {
                _class(nodeLvl+1);
                match(TokenType.SEMICOLON);
            }
            while(lookAHead(1)!=null);
        }
        catch(SyntacticException e){}
    }
    
    private void _class(int nodeLvl)
    {
        saveNode(NodeType.CLASS,nodeLvl);
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
                feature(nodeLvl+1);
                match(TokenType.SEMICOLON);
            }
            match(TokenType.CLOSE_BRACES);
        }
        catch(SyntacticException e){}    
    }
    
    private void feature(int nodeLvl)
    {
        try 
        {
            match(TokenType.OBJECT_IDENTIFIER);
            if(nextTokenIs(TokenType.OPEN_PARENTHESES)) method(nodeLvl);
            else if(nextTokenIs(TokenType.COLON)) attribute(nodeLvl);
            else createError(TokenType.OPEN_PARENTHESES,TokenType.COLON);
        }
        catch(SyntacticException e){}
    }
    
    private void method(int nodeLvl)
    {
        saveNode(NodeType.METHOD,nodeLvl);
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
            expr(nodeLvl+1);
            match(TokenType.CLOSE_BRACES);
        }
        catch(SyntacticException e){}  
    }
    
    private void attribute(int nodeLvl)
    {
        saveNode(NodeType.ATTRIBUTE,nodeLvl);
        try
        {
            match(TokenType.COLON);
            match(TokenType.TYPE_IDENTIFIER);
            if(nextTokenIs(TokenType.ASSIGN))
            {
                match(TokenType.ASSIGN);
                expr(nodeLvl+1);
            }
        }
        catch(SyntacticException e){}
    }
    
    private void formal()
    {
        try
        {
            match(TokenType.OBJECT_IDENTIFIER);
            match(TokenType.COLON);
            match(TokenType.TYPE_IDENTIFIER);
        }
        catch(SyntacticException e){}
    }
    
    private void expr(int nodeLvl)
    {
        //System.out.println("EXPR"+" "+lookAHead(1).getDescription());
        //boolean bufferIsEmpty = nodesBuffer.isEmpty();
        try
        {
            int bufferPos = nodesBuffer.size();
            //if(!nextTokenIn(firstExpr2,1)) dumpBuffer();
            if(nextTokenIn(firstExpr,1))
            {
                switch(lookAHead(1).getType())
                {
                    case OBJECT_IDENTIFIER -> id(nodeLvl);
                    case IF -> _if(nodeLvl);
                    case WHILE -> _while(nodeLvl);
                    case OPEN_BRACES -> block(nodeLvl);
                    case LET -> let(nodeLvl);
                    case CASE -> _case(nodeLvl);
                    case NEW -> { saveNode(NodeType.NEW,nodeLvl); match(TokenType.NEW); match(TokenType.TYPE_IDENTIFIER); }
                    case ISVOID -> { saveNode(NodeType.ISVOID,nodeLvl);match(TokenType.ISVOID); expr(nodeLvl+1); }
                    case COMPLEMENT -> { saveNode(NodeType.COMPLEMENT,nodeLvl); match(TokenType.COMPLEMENT); expr(nodeLvl+1); }
                    case NOT -> { saveNode(NodeType.NOT,nodeLvl); match(TokenType.NOT); expr(nodeLvl+1); }
                    case OPEN_PARENTHESES -> { saveNode(NodeType.AMONG_PARENTHESES,nodeLvl); match(TokenType.OPEN_PARENTHESES); expr(nodeLvl+1); match(TokenType.CLOSE_PARENTHESES); }
                    case INTEGER -> { saveNode(NodeType.INTEGER,nodeLvl); match(TokenType.INTEGER); }
                    case STRING -> { saveNode(NodeType.STRING,nodeLvl); match(TokenType.STRING); }
                    case TRUE -> { saveNode(NodeType.BOOL,nodeLvl); match(TokenType.TRUE); }
                    case FALSE -> { saveNode(NodeType.BOOL,nodeLvl); match(TokenType.FALSE); }

                }
                
            }
            else createError(TokenType.EXPR);       
            while(nextTokenIn(firstExpr2,1)) expr2(nodeLvl,bufferPos);
        }
        catch(SyntacticException e){}
    }
    
    private void expr2(int nodeLvl,int bufferPos)
    {
        //System.out.println("EXPR2"+" "+lookAHead(1).getDescription());
        try
        {
            if(nextTokenIs(TokenType.AT))
            {
                match(TokenType.AT);
                match(TokenType.TYPE_IDENTIFIER);
                match(TokenType.DOT);
                match(TokenType.OBJECT_IDENTIFIER);
                upBuffer(1,bufferPos);
                saveNodeAt(NodeType.DISPATCH,nodeLvl,bufferPos);
                methodCall(nodeLvl);          
                //fixPrecedence(NodeType.DISPATCH,nodeLvl,bufferPos);
                //if(comparePrecedence(TokenType.AT)) {System.out.println("ENTROUU"); dumpBuffer();}
            }
            else if(nextTokenIs(TokenType.DOT))
            {
                match(TokenType.DOT);
                match(TokenType.OBJECT_IDENTIFIER);
                upBuffer(1,bufferPos);
                saveNodeAt(NodeType.DISPATCH,nodeLvl,bufferPos);
                methodCall(nodeLvl);
                //fixPrecedence(NodeType.DISPATCH,nodeLvl,bufferPos);
                //if(comparePrecedence(TokenType.DOT)) {System.out.println("ENTROUU"); dumpBuffer();}
                
            }
            else
            {
               boolean enter = false;
               TokenType tk = lookAHead(1).getType();
               NodeType type;
               match(tk);
               if(tk==TokenType.ADD || tk==TokenType.SUB || tk==TokenType.MULT || tk==TokenType.DIV) type = NodeType.ARITHMETIC;
               else if(tk==TokenType.LT || tk==TokenType.LTE) type = NodeType.COMPARE;
               else type = NodeType.EQUAL;
               upBuffer(1,bufferPos);
               saveNodeAt(type,nodeLvl,bufferPos);
               if(!chainStarted) {chainPos=bufferPos; enter=true; chainStarted=true; }
               expr(nodeLvl+1);
               if(enter && chainStarted) chainStarted=false;
               fixPrecedence(type,nodeLvl,bufferPos);
               //if(comparePrecedence(tk)) {System.out.println("ENTROUU"); dumpBuffer();}
            }           
        }
        catch(SyntacticException e){}
    }
    
    private void id(int nodeLvl)
    {
        try
        {
            match(TokenType.OBJECT_IDENTIFIER);
            if(nextTokenIs(TokenType.OPEN_PARENTHESES)) { saveNode(NodeType.METHOD_CALL,nodeLvl); methodCall(nodeLvl); }
            else if(nextTokenIs(TokenType.ASSIGN)) { saveNode(NodeType.ASSIGNMENT,nodeLvl); assignment(nodeLvl); }
            else saveNode(NodeType.ID,nodeLvl);
        }
        catch(SyntacticException e){}
    }
    
    private void methodCall(int nodeLvl)
    {
        try
        {
            match(TokenType.OPEN_PARENTHESES);
            if(nextTokenIn(firstExpr,1))
            {
                expr(nodeLvl+1);
                while(nextTokenIs(TokenType.COMMA))
                {
                    match(TokenType.COMMA);
                    expr(nodeLvl+1);
                }
            }
            match(TokenType.CLOSE_PARENTHESES); 
        }
        catch(SyntacticException e){}
    }
    
    private void assignment(int nodeLvl)
    {
        try
        {
            match(TokenType.ASSIGN);
            expr(nodeLvl+1);
        }
        catch(SyntacticException e){}
    }
    
    private void _if(int nodeLvl)
    {
        saveNode(NodeType.IF,nodeLvl);
        try
        {
            match(TokenType.IF);
            expr(nodeLvl+1);
            match(TokenType.THEN);
            expr(nodeLvl+1);
            match(TokenType.ELSE);
            expr(nodeLvl+1);
            match(TokenType.FI);
        }
        catch(SyntacticException e){} 
    }
    
    private void _while(int nodeLvl)
    {
        saveNode(NodeType.WHILE,nodeLvl);
        try
        {
            match(TokenType.WHILE);
            expr(nodeLvl+1);
            match(TokenType.LOOP);
            expr(nodeLvl+1);
            match(TokenType.POOL);
        }
        catch(SyntacticException e){} 
    }
    
    private void block(int nodeLvl)
    {
        saveNode(NodeType.SEQUENCE,nodeLvl);
        try
        {
            match(TokenType.OPEN_BRACES);
            do
            {
                expr(nodeLvl+1);
                match(TokenType.SEMICOLON);
            }
            while(nextTokenIn(firstExpr,1));
            match(TokenType.CLOSE_BRACES);
        }
        catch(SyntacticException e){} 
    }
    
    private void let(int nodeLvl)
    {
        saveNode(NodeType.LET,nodeLvl);
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
                    expr(nodeLvl+1);
                }
                secondLoop=true;
            }            
            while(nextTokenIs(TokenType.COMMA));
            match(TokenType.IN);
            expr(nodeLvl+1);
        }
        catch(SyntacticException e){}
    }
    
    private void _case(int nodeLvl)
    {
        saveNode(NodeType.CASE,nodeLvl);
        try
        {
            match(TokenType.CASE);
            expr(nodeLvl+1);
            match(TokenType.OF);
            do
            { 
                match(TokenType.OBJECT_IDENTIFIER);
                match(TokenType.COLON);
                match(TokenType.TYPE_IDENTIFIER);
                match(TokenType.ARROW);
                expr(nodeLvl+1);
                match(TokenType.SEMICOLON);
            }while(nextTokenIs(TokenType.OBJECT_IDENTIFIER));
            match(TokenType.ESAC);   
        }
        catch(SyntacticException e){} 
    }           
}
