package compiladorcool;

import java.util.ArrayList;
import java.util.Queue;
import java.util.Stack;
import java.util.HashMap;

public class SemanticAnalyzer extends Analyzer{
    private final Queue<Node> syntacticTree;
    private final HashMap<String,ClassDescriptor> classDescriptors;
    private final Stack<HashMap<String,String>> environmentStack;
    private final ArrayList<Error> errors;
    private String currentClass;
    
    public SemanticAnalyzer(ArrayList<Token> tokens, Queue<Node> syntacticTree, ArrayList<Error> errors)
    {
        super(tokens);
        this.syntacticTree=syntacticTree;
        this.errors=errors;
        this.classDescriptors = new HashMap<>();
        this.environmentStack = new Stack<>();
        createDefaultClasses();
    }
    
    private boolean hasClass(String className) { return className.equals("SELF_TYPE") || classDescriptors.containsKey(className); }
    
    private boolean hasMethod(String className, String method) 
    { 
        if(className.isEmpty() || !hasClass(className)) return false;
        return classDescriptors.get(className).hasMethod(method); 
    }
    
    private Method getMethod(String className, String method)
    {
        while(!className.isEmpty())
        {
            if(hasMethod(className,method)) return classDescriptors.get(className).getMethod(method);
            className = inheritanceOf(className);
        }
        return null;
    }
    
    private boolean hasAttribute(String attribute) 
    { 
        if(!environmentStack.isEmpty()) return environmentStack.peek().containsKey(attribute);
        if(currentClass.isEmpty()) return false;
        return classDescriptors.get(currentClass).hasAttribute(attribute); 
    }
    
    private boolean hasAttribute(String className, String attribute) 
    { 
        if(className.isEmpty()) return false;
        return classDescriptors.get(className).hasAttribute(attribute); 
    }  
    
    private void createClass(String name, String inherits) { if(!hasClass(name)) classDescriptors.put(name, new ClassDescriptor(inherits)); }
   
    private void addMethod(String className,String name, String type, String... paramsType)
    {
        if(!hasMethod(className,name) && !className.isEmpty() && hasClass(type)) classDescriptors.get(className).addMethod(name, type, paramsType);
    }
    
    private void addAttribute(String name, String type) 
    { 
        if(!hasAttribute(name) && hasClass(type))
        {
            if(!environmentStack.isEmpty()) environmentStack.peek().put(name, type);
            else classDescriptors.get(currentClass).addAttribute(name, type);
        }  
    }
    
    private void createDefaultClasses()
    {
        createClass("Object","");
        createClass("IO","Object");
        createClass("String","Object");
        createClass("Int","Object");
        createClass("Bool","Object");
        
        addMethod("Object","abort","Object");
        addMethod("Object","type_name","String");
        addMethod("Object","copy","SELF_TYPE");
        
        addMethod("IO","out_string","SELF_TYPE","String");
        addMethod("IO","out_int","SELF_TYPE","Int");
        addMethod("IO","in_string","String");
        addMethod("IO","in_int","Int");
        
        addMethod("String","length","Int");
        addMethod("String","concat","String","String");
        addMethod("String","substr","String","Int","Int");    
    }
    
    private void createError(String msg,int row) { if(!currentClass.isEmpty()) errors.add(new SemanticError(msg,row)); }
    
    private String inheritanceOf(String className) 
    { 
        if(hasClass(className)) return classDescriptors.get(className).getInherits(); 
        return "Object";
    }
    
    private boolean belongs(String classA, String classB)
    {
        if(classB.equals("SELF_TYPE")) classB=currentClass;
        if(classA.isEmpty()) return true;
        do
        {
            if(classA.equals(classB)) return true;
            classA = inheritanceOf(classA);
        }while(!classA.isEmpty());
        return false;
    }
    
    private String union(String classA, String classB)
    {
        if(classA.isEmpty() || classB.isEmpty()) return "Object";
        
        while(!classA.equals("Object") && !classB.equals("Object"))
        {
            if(classA.equals(classB)) return classA;
            if(inheritanceOf(classA).equals(classB)) return inheritanceOf(classA);
            if(inheritanceOf(classB).equals(classA)) return inheritanceOf(classB);
            classA = inheritanceOf(classA);
            classB = inheritanceOf(classB);
        }  
        return "Object";
    }
    
    @Override
    public Token nextToken()
    {
        Token tk=null;
        if(tokenId<tokens.size()) 
        {
            tk = tokens.get(tokenId);
            tokenId++;
        }
        return tk;
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
            case ATTRIBUTE -> attribute("atributo",false);
            case METHOD -> method();
            case ASSIGNMENT -> { return assignment(); }
            case DISPATCH -> { return dispatch(node.getLevel()); }
            case METHOD_CALL -> { return methodCall(node.getLevel(),currentClass); }
            case IF -> { return _if(); }
            case WHILE -> { return _while(); }
            case SEQUENCE -> { return sequence(node.getLevel()); }
            case LET -> { return let(); }
            case CASE -> { return _case(); }
            case NEW -> { return _new(); }
            case ISVOID -> { return isvoid(); }
            case ARITHMETIC -> { return arithmetic(); }
            case COMPLEMENT -> { return complement(); }
            case COMPARE -> { return compare(); }
            case EQUAL -> { return equal(); }
            case NOT -> { return not(); }
            case AMONG_PARENTHESES -> { return amongParentheses(); }
            case INTEGER -> { nextToken(); return "Int"; }
            case STRING -> { nextToken(); return "String"; }
            case BOOL -> { nextToken(); return "Bool"; }
            case ID -> { return id(nextToken()); }
        }
        return "";
    }
    
    public void analyze()
    {
        while(!syntacticTree.isEmpty()) nextNode();
    }
    
    private void _class()
    {
        nextToken();
        Token tk = nextToken();
        String name = tk.getDescription();
        currentClass = name;
        String inherits="Object";
        if(hasClass(name)) { createError(String.format("classe '%s' ja foi definida", name),tk.getRow()); currentClass=""; }
        
        if(nextTokenIs(TokenType.INHERITS)) 
        { 
            nextToken();
            tk = nextToken();
            inherits = tk.getDescription();
            if(inherits.equals("String") || inherits.equals("Int") || inherits.equals("Bool"))  {  createError(String.format("Nao eh possivel herdar de %s", inherits),tk.getRow()); inherits="Object"; }
            if(!hasClass(inherits)) {  createError(String.format("classe '%s' ainda nao foi definida", inherits),tk.getRow()); inherits="Object"; }
        }
        nextToken();
        createClass(name,inherits);
        while(nextNodeLvl()==2) {nextNode(); nextToken(); }
        nextToken();
        nextToken();
    }
    
    private void method()
    {
        environmentStack.push(new HashMap<>());
        boolean hasError = false;
        ArrayList<String> paramsTypes = new ArrayList<>();
        Token tk = nextToken();
        String name = tk.getDescription();
        
        if(hasMethod(currentClass,name)){ createError(String.format("metodo '%s' ja foi definido", name),tk.getRow()); hasError=true; }
        nextToken();
        
        while(nextTokenIs(TokenType.OBJECT_IDENTIFIER))
        {
            String varType = attribute("parametro",false);
            paramsTypes.add(varType);
            if(nextTokenIs(TokenType.COMMA)) nextToken();
        }
               
        nextToken();
        nextToken();
        tk = nextToken();
        String type = tk.getDescription();
        if(!hasClass(type)){ createError(String.format("tipo de metodo '%s' ainda nao foi definido", type),tk.getRow()); hasError=true; }
        
        addMethod(currentClass, name, type, paramsTypes.toArray(String[]::new));
        nextToken();       
        String out = nextNode();
        if(!hasError && !belongs(out,type)) createError(String.format("metodo '%s' espera '%s' mas '%s' esta sendo retornado", name,type,out),tk.getRow());
        nextToken();
        environmentStack.pop();
    }   
    
    private String attribute(String msg, boolean isCase)
    {
        boolean hasError = false;
        Token tk = nextToken();
        String name = tk.getDescription();
        if(!isCase && hasAttribute(name)) { createError(String.format("%s '%s' ja foi definido", msg, name),tk.getRow()); hasError=true; }
        nextToken();
        tk = nextToken();
        String type = tk.getDescription();
        if(type.equals("SELF_TYPE")) type = currentClass;
        if(!hasClass(type)) { createError(String.format("tipo de %s '%s' ainda nao foi definido", msg, type),tk.getRow()); hasError=true; }
        addAttribute(name,type);
        if(nextTokenIs(TokenType.ASSIGN) || nextTokenIs(TokenType.ARROW))
        {
            nextToken();
            String out = nextNode();
            if(!hasError && !belongs(out,type)) createError(String.format("%s '%s' espera '%s' mas '%s' esta sendo atribuido", msg,name,type,out),tk.getRow());
        }   
        return type;
    }
    
    private String id(Token tk)
    {
        String id = tk.getDescription();
        if(id.equals("self")) return currentClass;
        
        Stack<HashMap<String,String>> envStack = (Stack<HashMap<String,String>>)environmentStack.clone();
        while(!envStack.isEmpty())
        {
            if(envStack.peek().containsKey(id)) 
            {
                String type = envStack.peek().get(id);
                if(type.equals("SELF_TYPE")) return currentClass;
                return type;
            }
            envStack.pop();
        }
        
        String className = currentClass;
        while(!className.isEmpty())
        {
            if(hasAttribute(className,id)) 
            {
                String type = classDescriptors.get(className).typeOf(id);
                if(type.equals("SELF_TYPE")) return currentClass;
                return type;
            }
            className = inheritanceOf(className);
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
        String expr = nextNode();
        if(type.isEmpty()) return "";
        if(!belongs(expr,type)) { createError(String.format("variavel '%s' espera '%s' mas '%s' esta sendo atribuido", id,type,expr),tk.getRow()); return ""; }
        return expr;
    }
    
    private String _if()
    {
        Token tk = nextToken();
        String condition = nextNode();
        if(!condition.equals("Bool")) createError(String.format("condicao do if espera 'Bool' mas '%s' esta sendo retornado",condition),tk.getRow());
        nextToken();
        String then = nextNode();
        nextToken();
        String _else = nextNode();
        nextToken();
        return union(then,_else);
    }
    
    private String _while()
    {
        Token tk = nextToken();
        String condition = nextNode();
        if(!condition.equals("Bool")) createError(String.format("condicao do while espera 'Bool' mas '%s' esta sendo retornado",condition),tk.getRow());
        nextToken();
        nextNode();
        nextToken();
        return "Object";
    }
    
    private String sequence(int nodeLvl)
    {
        nextToken();
        String expr="Object";
        while(nextNodeLvl() == nodeLvl+1)
        {
            expr = nextNode();
            nextToken();
        }
        nextToken();
        return expr;
    }
    
    private String _new()
    {
        nextToken();
        String type = nextToken().getDescription();
        if(type.equals("SELF_TYPE")) return currentClass;
        return type;
    }
    
    private String isvoid()
    {
        nextToken();
        nextNode();
        return "Bool";
    }
    
    private String arithmetic()
    {
        String op = nextNode();
        Token tk = nextToken();
        String op2 = nextNode();
        if(!op.equals("Int") || !op2.equals("Int")) createError(String.format("Operacoes com '%s' so permitem 'Int'",tk.getDescription()),tk.getRow());
        return "Int";
    }
    
    private String compare()
    {
        String op = nextNode();
        Token tk = nextToken();
        String op2 = nextNode();
        if(!op.equals("Int") || !op2.equals("Int")) createError(String.format("Operacoes com '%s' so permitem 'Int'",tk.getDescription()),tk.getRow());
        return "Bool";
    }
    
    private boolean isStatic(String type) { return type.equals("Int") || type.equals("String") || type.equals("Bool"); }
    
    private String equal()
    {
        String op = nextNode();
        Token tk = nextToken();
        String op2 = nextNode();
        if((isStatic(op) || isStatic(op2)) && !op.equals(op2)) createError("Tipos estaticos como 'Int', 'String' e 'Bool' so podem ser comparados com o mesmo tipo",tk.getRow());
        return "Bool";
    }
    
    private String complement()
    {
        Token tk = nextToken();
        String op = nextNode();
        if(!op.equals("Int")) createError("Expressao que segue '~' deve ser 'Int'",tk.getRow());
        return "Int";
    }
    
    private String not()
    {
        Token tk = nextToken();
        String op = nextNode();
        if(!op.equals("Bool")) createError("Expressao que segue 'not' deve ser 'Bool'",tk.getRow());
        return "Bool";
    }
    
    private String amongParentheses()
    {
        nextToken();
        String expr = nextNode();
        nextToken();
        return expr;
    }
    
    private String let()
    {
        environmentStack.push(new HashMap<>());
        nextToken();
        while(nextTokenIs(TokenType.OBJECT_IDENTIFIER))
        {
            attribute("variavel",false);
            if(nextTokenIs(TokenType.COMMA)) nextToken();
        }
        nextToken();
        String in = nextNode();
        environmentStack.pop();
        return in;
    }
    
    private String _case()
    {
        environmentStack.push(new HashMap<>());
        String out="";
        boolean firstLoop=true;
        nextToken();
        nextNode();
        nextToken();
        while(nextTokenIs(TokenType.OBJECT_IDENTIFIER))
        {
            String type = attribute("variavel",true);
            nextToken();
            if(firstLoop) out = type;
            else out = union(type,out);
            firstLoop = false;
        }
        nextToken();
        environmentStack.pop();
        return out;
    }
    
    private String methodCall(int nodeLvl,String className)
    {
        ArrayList<String> passedArgs = new ArrayList<>();
        String[] methodArgs = new String[0];
        boolean hasError = false;
        Token tk = nextToken();
        String id = tk.getDescription();
        Method metodo = getMethod(className,id);
        if(metodo!=null) methodArgs = metodo.getArgs();
        else createError(String.format("metodo '%s' nao esta definido na classe '%s'",id,className),tk.getRow());

        nextToken();
        while(nextNodeLvl()==nodeLvl+1)
        {
            String type = nextNode();
            passedArgs.add(type);
            if(type.isEmpty()) hasError = true;
            if(nextTokenIs(TokenType.COMMA)) nextToken();
        }
        nextToken();
        
        if(metodo!=null && passedArgs.size()!=methodArgs.length) createError(String.format("metodo '%s' espera %d argumentos mas %d argumentos foram passados",id,methodArgs.length,passedArgs.size()),tk.getRow());
        
        for(int i=0;i<passedArgs.size() && i<methodArgs.length; i++)
        {
            if(!hasError && !belongs(passedArgs.get(i),methodArgs[i])) createError(String.format("argumento%d do metodo '%s' espera '%s' mas '%s' esta sendo passado",i+1,id,methodArgs[i],passedArgs.get(i)),tk.getRow());
        }
        
        if(metodo!=null) 
        {
            String out = metodo.getType();
            if(out.equals("SELF_TYPE")) return className;
            return out;
        }
        return "";
    }
    
    private String dispatch(int nodeLvl)
    {
        String className = nextNode();
        if(className.equals("SELF_TYPE")) className = currentClass;
        if(nextTokenIs(TokenType.AT))
        {
            nextToken();
            Token tk = nextToken();
            String type = tk.getDescription();
            if(type.equals("SELF_TYPE")) type = currentClass;
            if(!belongs(className,type)) createError(String.format("%s nao eh igual nem herda de %s", className,type),tk.getRow());
            className=type;
        }
        nextToken();
        return methodCall(nodeLvl,className);
    }
}
