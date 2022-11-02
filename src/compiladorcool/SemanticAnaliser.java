package compiladorcool;

import java.util.ArrayList;
import java.util.Queue;
import java.util.Stack;
import java.util.HashMap;

public class SemanticAnaliser {
    private final static int MAX_BUFFER_SIZE = 10;
    private final LexicalAnaliser lexical;
    private final Queue<Node> syntacticTree;
    private final HashMap<String,ClassDescriptor> classDescriptors;
    private final Stack<HashMap<String,String>> environmentStack;
    private final ArrayList<Error> errors;
    private final ArrayList<Token> bufferTokens;
    private String currentClass;
    
    public SemanticAnaliser(LexicalAnaliser lexical, Queue<Node> syntacticTree, ArrayList<Error> errors)
    {
        this.lexical=lexical;
        this.syntacticTree=syntacticTree;
        this.errors=errors;
        this.bufferTokens = new ArrayList<>();
        this.classDescriptors = new HashMap<>();
        this.environmentStack = new Stack<>();
        createDefaultClasses();
    }
    
    private boolean hasClass(String className) { return classDescriptors.containsKey(className); }
    
    private boolean hasMethod(String className, String method) { return classDescriptors.get(className).hasMethod(method); }
    
    private boolean hasAttribute(String className, String attribute) { return classDescriptors.get(className).hasAttribute(attribute); }
    
    private void createClass(String name, String inherits) { if(!hasClass(name)) classDescriptors.put(name, new ClassDescriptor(inherits)); }
   
    private void addMethod(String className,String name, String type, String... paramsType)
    {
        if(!hasMethod(className,name)) classDescriptors.get(className).addMethod(name, type, paramsType);
    }
    
    private void addAttribute(String className,String name, String type) { if(!hasAttribute(className,name)) classDescriptors.get(className).addAttribute(name, type); }
    
    private void createDefaultClasses()
    {
        createClass("Object","");
        addMethod("Object","abort","Object");
        addMethod("Object","type_name","String");
        addMethod("Object","copy","SELF_TYPE");
        
        createClass("IO","Object");
        addMethod("IO","out_string","SELF_TYPE","String");
        addMethod("IO","out_int","SELF_TYPE","Int");
        addMethod("IO","in_string","String");
        addMethod("IO","in_int","Int");
        
        createClass("String","Object");
        addMethod("String","length","Int");
        addMethod("String","concat","String","String");
        addMethod("String","substr","String","Int","Int");
        
        createClass("Int","Object");
        createClass("Bool","Object");  
    }
    
    private void createError(String msg,int row) { errors.add(new SemanticError(msg,row)); }
    
    private String inheritsFrom(String className) { return classDescriptors.get(className).getInherits(); }
    
    private boolean belongs(String classA, String classB)
    {
        if(classA.isEmpty()) return true;
        do
        {
            if(classA.equals(classB)) return true;
            classA = inheritsFrom(classA);
        }while(!classA.isEmpty());
        
        return false;
    }
    
    private String union(String classA, String classB)
    {
        while(true)
        {
            if(classA.equals(classB)) return classA;
            if(inheritsFrom(classA).equals(classB)) return inheritsFrom(classA);
            if(inheritsFrom(classB).equals(classA)) return inheritsFrom(classB);
            classA = inheritsFrom(classA);
            classB = inheritsFrom(classB);
        }  
    }
    
    private Token nextToken()
    {
        Token tk=null;
        if(!bufferTokens.isEmpty()) tk = bufferTokens.remove(0);
        while(bufferTokens.size()<MAX_BUFFER_SIZE)
        {
            Token next = lexical.nextToken();
            if(next==null) break;
            bufferTokens.add(next);
        }
        return tk;
    }
    
    private Token lookAHead(int k)
    {
        if(k-1 > bufferTokens.size()-1) return null;
        return bufferTokens.get(k-1);
    }
    
    private boolean nextTokenIs(TokenType type) {return lookAHead(1)!=null && lookAHead(1).getType()==type;}
    
    private int nextNodeLvl() 
    { 
        if(syntacticTree.peek()!=null) return syntacticTree.peek().getLevel(); 
        return -1;
    }
    
    private String nextNode()
    {
        Node node = syntacticTree.poll();
        switch(node.getType())
        {
            case CLASS -> _class();
            case ATTRIBUTE -> attribute();
            case METHOD -> method();
            case ASSIGNMENT -> { return assignment(); }
            case IF -> { return _if(); }
            case INTEGER -> { nextToken(); return "Int"; }
            case STRING -> { nextToken(); return "String"; }
            case BOOL -> { nextToken(); return "Bool"; }
            case ID -> { return id(nextToken()); }
        }
        return "";
    }
    
    public void analise()
    {
        nextToken();
        while(!syntacticTree.isEmpty()) nextNode();
        for(var c : classDescriptors.keySet())
        {
            System.out.println(c);
            classDescriptors.get(c).show();
            System.out.println();
        }
    }
    
    private void _class()
    {
        Token tk;
        String name,inherits="Object";
        nextToken();
        tk = nextToken();
        name = tk.getDescription();
        if(hasClass(name)) createError(String.format("classe '%s' ja foi definida", name),tk.getRow());
        currentClass = name;
        if(nextTokenIs(TokenType.INHERITS)) 
        { 
            nextToken();
            tk = nextToken();
            inherits = tk.getDescription();
            if(inherits.equals("String") || inherits.equals("Int") || inherits.equals("Bool"))  {  createError(String.format("Nao eh possivel herdar de %s", inherits),tk.getRow()); inherits="Object"; }
            if(!hasClass(inherits)) {  createError(String.format("classe '%s' ainda nao foi definida", inherits),tk.getRow()); inherits="Object"; }
            nextToken();
        }
        createClass(name,inherits);
        while(nextNodeLvl()==2) {nextNode(); nextToken(); }
        nextToken();
    }
    
    private void method()
    {
        Token tk = nextToken();
        ArrayList<String> methodTypes = new ArrayList<>();
        environmentStack.push(new HashMap<>());
        String name = tk.getDescription();
        
        if(hasMethod(currentClass,name)) createError(String.format("metodo '%s' ja foi definido", name),tk.getRow());
        nextToken();
        
        while(nextTokenIs(TokenType.OBJECT_IDENTIFIER))
        {
            String varName = nextToken().getDescription();
            nextToken();
            String varType = nextToken().getDescription();
            methodTypes.add(varType);
            environmentStack.peek().put(varName, varType);
            if(nextTokenIs(TokenType.COMMA)) nextToken();
        }
        /*for(var v : environmentStack.peek().keySet())
        {
            System.out.printf("%s : %s\n",v,environmentStack.peek().get(v));
        }*/
        
        nextToken();
        nextToken();
        tk = nextToken();
        String type = tk.getDescription();
        if(!hasClass(type)) createError(String.format("tipo do metodo '%s' ainda nao foi definido", type),tk.getRow());
        addMethod(currentClass, name, type, methodTypes.toArray(String[]::new));
        nextToken();
        String out = nextNode();
        if(!belongs(out,type)) createError(String.format("metodo '%s' espera '%s' mas '%s' esta sendo retornado", name,type,out),tk.getRow());
        nextToken();
        environmentStack.pop();
    }   
    
    private void attribute()
    {
        Token tk = nextToken();
        String name = tk.getDescription();
        if(hasAttribute(currentClass,name)) createError(String.format("atributo '%s' ja foi definido", name),tk.getRow());
        nextToken();
        tk = nextToken();
        String type = tk.getDescription();
        if(!hasClass(type)) createError(String.format("tipo do atibuto '%s' ainda nao foi definido", type),tk.getRow());
        addAttribute(currentClass,name,type);
        if(nextTokenIs(TokenType.ASSIGN))
        {
            nextToken();
            String out = nextNode();
            if(!belongs(out,type)) createError(String.format("atributo '%s' espera '%s' mas '%s' esta sendo atribuido", name,type,out),tk.getRow());
        }    
    }
    
    private String id(Token tk)
    {
        String id = tk.getDescription();
        Stack<HashMap<String,String>> backup = new Stack();
        while(!environmentStack.isEmpty())
        {
            if(environmentStack.peek().containsKey(id)) return environmentStack.peek().get(id);
            backup.push(environmentStack.pop());
        }
        while(!backup.isEmpty()) environmentStack.push(backup.pop());
        
        String className = currentClass;
        
        while(!className.isEmpty())
        {
            if(hasAttribute(className,id)) return classDescriptors.get(className).typeOf(id);
            className = inheritsFrom(className);
        }
        
        createError(String.format("variavel '%s' nao declarada", id),tk.getRow());
        return "";
    }
    
    private String assignment()
    {
        Token tk = nextToken();
        String id = tk.getDescription();
        String type = id(tk);
        nextToken();
        String out = nextNode();
        if(type.isEmpty()) return "";
        if(!belongs(out,type)) { createError(String.format("variavel '%s' espera '%s' mas '%s' esta sendo atribuido", id,type,out),tk.getRow()); return ""; }
        return out;
    }
    
    private String _if()
    {
        Token tk = nextToken();
        String out = nextNode();
        if(!out.equals("Bool")) createError(String.format("condicao do if espera 'Bool' mas '%s' esta sendo retornado",out),tk.getRow());
        nextToken();
        String then = nextNode();
        nextToken();
        String _else = nextNode();
        nextToken();
        return union(then,_else);
    }
}
