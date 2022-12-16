package compiladorcool.codegeneration;

import compiladorcool.semantic.ClassDescriptor;
import compiladorcool.semantic.Node;
import compiladorcool.semantic.Parameter;
import java.util.HashMap;
import java.util.Queue;
import java.util.ArrayList;
import java.util.Stack;

public class CodeGenerator {
    
    private final HashMap<String,ClassDescriptor> classDescriptors;
    private final Queue<Node> syntacticTree;
    private final HashMap<String,ArrayList<String>> attributes;
    private final Stack<HashMap<String,String>> environmentStack;
    private final ArrayList<String> pointers;
    private BrilProgram brilProgram; 
    private Func currentFunction;
    private Node currentNode;
    private String currentClass;
    
    
    private int intVarCounter=0;
    private int boolVarCounter=0;
    private int ptrCounter=0;
    private int labelCounter=0;
    
    public CodeGenerator(HashMap<String,ClassDescriptor> classDescriptors, Queue<Node> syntacticTree)
    {
        this.brilProgram = new BrilProgram();
        this.classDescriptors=classDescriptors;
        this.syntacticTree=syntacticTree;
        this.environmentStack = new Stack<>();
        this.attributes = new HashMap<>();
        this.pointers = new ArrayList<>();
        addIOFunction();
    }
    
    public BrilProgram generateCode()
    {
        while(!syntacticTree.isEmpty()) nextNode();
        
        for(var key : attributes.keySet())
        {
            System.out.print(key+": ");
            System.out.println(attributes.get(key).toString());
            //System.out.printf(" = %d\n",getClassSize(key));
        }
        return brilProgram;
    }
    
    private String nextNode()
    {
        //resetVars();
        currentNode = syntacticTree.poll();
        switch(currentNode.getType())
        {
            case CLASS -> _class();
            case ATTRIBUTE -> attribute();
            case METHOD -> method();
            case ASSIGNMENT -> { return id(currentNode.getValue(),nextNode()); }
            case DISPATCH -> { return method_call(true); }
            case METHOD_CALL -> { return method_call(false); }
            case IF -> { return _if(); }
            case WHILE -> { return _while(); }
            case SEQUENCE -> { return sequence(); }
            case LET -> { return let(); }
            case NEW -> {return _new(); }
            case ARITHMETIC,ARITHMETIC2 -> { return arithmetic(); }
            case COMPLEMENT -> { return complement(); }
            case COMPARE,EQUAL -> { return comparison(); }
            case NOT -> { return not(); }
            case AMONG_PARENTHESES -> { return nextNode(); }
            case ID -> { return id(currentNode.getValue(),""); }
            case INTEGER -> { return integer(); }
            case BOOL -> { return bool(); }
             
        }
        return "";
    }
    
    private void _class()
    {
        int nodeLvl = currentNode.getLevel();
        currentClass=currentNode.getValue();
        String inheritance = inheritanceOf(currentClass);
        attributes.put(currentClass, new ArrayList<>());
        putAttributes();
        
        String funcName = "new_"+currentClass;
        Func f = newFunction(funcName,"ptr");
        f.setArgs(new ArgumentPtr("this"));
        
        if(!inheritance.equals("Object") && !inheritance.equals("IO")) f.addInstruction(InstructionFactory.createCall("new_"+inheritance, "this", "ptr", "this"));
        
        while(nextNodeLvl()==nodeLvl+1){nextNode();}
        
        f.addInstruction(InstructionFactory.createRet("this"));
        
    }
    
    private void attribute()
    {
        String name = currentNode.getValue();
        Func f = currentFunction;
        
        String desloc = intDest();
        int id = attributes.get(currentClass).indexOf(name);
            
        f.addInstruction(new ConstantInt(id,desloc));
        f.addInstruction(InstructionFactory.createPtrAdd("this", desloc,"this"));
        
        if(nextNodeLvl()==currentNode.getLevel()+1)
        {
             f.addInstruction(InstructionFactory.createPtrStore("this", nextNode()));     
        }
        else
        {
            f.addInstruction(new ConstantInt(0,"zero"));
            f.addInstruction(InstructionFactory.createPtrStore("this", "zero"));
        }
        
        f.addInstruction(new ConstantInt(-id,desloc));
        f.addInstruction(InstructionFactory.createPtrAdd("this", desloc,"this"));  
    }
    
    private void method()
    {
        int nodeLvl = currentNode.getLevel();
        String funcName = currentNode.getValue();
        environmentStack.push(new HashMap<>());
        String type = toType(getMethodType(funcName,currentClass,false));
        
        if(!funcName.equals("main")) funcName+="_"+currentClass;
        
        Func f = newFunction(funcName,type);
        
        if(funcName.equals("main"))
        {
            String size = intDest();
            f.addInstruction(new ConstantInt(getClassSize(currentClass),size));
            f.addInstruction(InstructionFactory.createAlloc(size, "this"));
            f.addInstruction(InstructionFactory.createCall("new_"+currentClass, "this","ptr","this"));
        }
        else f.setArgs(toArguments(currentNode.getParams()));
        
        String returnValue="";
        while(nextNodeLvl()==nodeLvl+1) returnValue = nextNode();
        
        if(funcName.equals("main")) f.addInstruction(InstructionFactory.createFree("this"));
        freePtrs();
        f.addInstruction(InstructionFactory.createRet(returnValue));
        environmentStack.pop();
    } 
    
    private String method_call(boolean isDispatch)
    {
        Func f = currentFunction;
        int nodeLvl = currentNode.getLevel();
         Parameter func = currentNode.getParams()[0];
         
         String name = isDispatch ? currentNode.getValue() : currentClass;
         String className = getMethodType(func.getName(),name,true);
         
         String type = getMethodType(func.getName(),className,false);
         String methodName = func.getName()+"_"+className;
        
        ArrayList<String> args = new ArrayList<>();
        String ptr = isDispatch ? nextNode() : "this";
        args.add(ptr);
        
        while(nextNodeLvl()==nodeLvl+1){ args.add(nextNode()); }
  
        String dest = dest(type);
        f.addInstruction(InstructionFactory.createCall(methodName,dest,toType(type),args.toArray(String[]::new)));
        return dest;
    }
    
    private String _if()
    {
        Func f = currentFunction;
        int id = labelCounter;
        String then = "then"+id;
        String _else = "else"+id;
        String end = "end"+id;
        labelCounter++;
        
        String cond = nextNode();
        f.addInstruction(InstructionFactory.createBr(cond,then,_else));
        
        f.addInstruction(new Label(then));
        then = nextNode();
        String type = toType(then);
        String dest = dest(type);
        f.addInstruction(InstructionFactory.createId(dest, type, then));
        f.addInstruction(InstructionFactory.createJmp(end));
        
        f.addInstruction(new Label(_else));
        _else = nextNode();
        f.addInstruction(InstructionFactory.createId(dest, type, _else));
        
        f.addInstruction(new Label(end));
       
        return dest;
    }
    
    private String _while()
    {
        Func f = currentFunction;
        int id = labelCounter;
        String cond = "cond"+id;
        String loop = "loop"+id;
        String end = "end"+id;
        f.addInstruction(new Label(cond));
        f.addInstruction(InstructionFactory.createBr(nextNode(), loop,end));
        f.addInstruction(new Label(loop));
        String ret = nextNode();
        f.addInstruction(InstructionFactory.createJmp(cond));
        f.addInstruction(new Label(end));
        return ret;
    }
    
    private String sequence()
    {
        String ret="";
        int nodeLvl = currentNode.getLevel();
        while(nextNodeLvl()==nodeLvl+1) ret = nextNode();
        return ret;
    }
    
    private String let()
    {
        environmentStack.push(new HashMap<>());
        int scope = environmentStack.size();
        int assignments = Integer.parseInt(currentNode.getValue());
        Func f = currentFunction;
        for(var param: currentNode.getParams())
        {
            String name = param.getName();
            String type = toType(param.getType());
            environmentStack.peek().put(name, type);
            if(assignments>0) 
            {
                assignments--;
                String value = nextNode();
                f.addInstruction(InstructionFactory.createId(name+"_"+scope, type, value));
            }
            else f.addInstruction(new ConstantInt(0,name+"_"+scope));
        }
        String ret = nextNode();
        environmentStack.pop();
        return ret;
    }
    
    private String _new()
    {
        Func f = currentFunction;
        String className = currentNode.getValue();
        String size = intDest();
        f.addInstruction(new ConstantInt(getClassSize(className),size));
        String ptr = ptrDest();
        f.addInstruction(InstructionFactory.createAlloc(size, ptr));
        f.addInstruction(InstructionFactory.createCall("new_"+className, ptr,"ptr",ptr));
        pointers.add(ptr);
        return ptr;
    }
    
    private String arithmetic()
    {
        Func f = currentFunction;
        String op = currentNode.getValue(); 
        op = switch(op)
        {
            case "+" -> "add";
            case "-" -> "sub";
            case "*" -> "mul";
            case "/" -> "div";
            default -> "";
        };
        
        String dest = intDest();
        f.addInstruction(InstructionFactory.createOperation(op, dest, nextNode(),nextNode()));
        return dest;   
    }
    
    private String complement()
    {
        Func f = currentFunction;
        String dest = intDest();
        f.addInstruction(new ConstantInt(-1,dest));
        f.addInstruction(InstructionFactory.createOperation("mul", dest, nextNode(),dest));
        return dest;
    }
    
    private String comparison()
    {
        Func f = currentFunction;
        String op = currentNode.getValue();
    
        op = switch(op)
        {
            case "=" -> "eq";
            case "<" -> "lt";
            case "<=" -> "le";
            default -> "";
        };
        
        String dest = boolDest();
        f.addInstruction(InstructionFactory.createOperation(op, dest, nextNode(),nextNode()));
        return dest;   
    }
    
    private String not()
    {
        Func f = currentFunction;
        String dest = boolDest();
        f.addInstruction(InstructionFactory.createNot(dest, nextNode()));
        return dest;
    }
    
    private String id(String name,String assignment)
    {
        
        if(name.equals("self")) return "this";
        Func f = currentFunction;
        Stack<HashMap<String,String>> envStack = (Stack<HashMap<String,String>>)environmentStack.clone();
        
        int scope=envStack.size();
        
        
        while(!envStack.isEmpty())
        {
            if(envStack.peek().containsKey(name))
            {
                String ret = name+"_"+scope;
                
                if(!assignment.isEmpty())
                {
                    String type = envStack.peek().get(name);
                    f.addInstruction(InstructionFactory.createId(ret, toType(type), assignment));
                }
                return ret;
            }
            scope--;
            envStack.pop();
        }
        
        int id = attributes.get(currentClass).indexOf(name);
        String desloc = intDest();
        f.addInstruction(new ConstantInt(id,desloc));
        f.addInstruction(InstructionFactory.createPtrAdd("this", desloc,"this"));
        String dest = intDest();
        if(!assignment.isEmpty()) f.addInstruction(InstructionFactory.createPtrStore("this", assignment));
        f.addInstruction(InstructionFactory.createPtrLoad("this",dest));
        f.addInstruction(new ConstantInt(-id,desloc));
        f.addInstruction(InstructionFactory.createPtrAdd("this", desloc,"this"));
        
        return dest;
    }
    
    private String integer()
    {
        Func f = currentFunction;
        String value = currentNode.getValue();
        String dest = intDest();
        f.addInstruction(new ConstantInt(toInt(value),dest));
        return dest;
    }
    
    private String bool()
    {
        Func f = currentFunction;
        String value = currentNode.getValue();
        String dest = boolDest();
        f.addInstruction(new ConstantBool(toBool(value),dest));
        return dest;
    }
    
    // ================================================================================================================================================ //
    //                                                             METODOS AUXILIARES                                                                   //
    // ================================================================================================================================================ //
    
    
    private void addIOFunction()
    {
        Func f = newFunction("out_int_IO","ptr");
        f.setArgs(new ArgumentPtr("this"),new Argument("value","int"));
        f.addInstruction(InstructionFactory.createPrint("value"));
        f.addInstruction(InstructionFactory.createRet("this"));
    }
    
    private Func newFunction(String name,String type)
    {
        resetVars();
        Func f;
        if(type.equals("ptr")) f = new FunctionPtr(name,new Pointer("int"));
        else f = new Function(name,type);
        brilProgram.getFunctions().add(f);
        return currentFunction = f;
    }
    
    private Arg[] toArguments(Parameter[] params)
    {
        ArrayList<Arg> args = new ArrayList<>();
        args.add(new ArgumentPtr("this"));
        int scope = environmentStack.size();
        for(var param: params)
        {
            String name = param.getName();
            String type = toType(param.getType());
            environmentStack.peek().put(name, type);
            name+="_"+scope;
            if(type.equals("ptr")) args.add(new ArgumentPtr(name));
            else args.add(new Argument(name,type));
        }
        return args.toArray(Arg[]::new);
    }
    
    private void freePtrs()
    {
        Func f = currentFunction;
        for(var ptr: pointers) f.addInstruction(InstructionFactory.createFree(ptr));
    }
    
    private String inheritanceOf(String className){ return classDescriptors.get(className).getInherits(); }
    
    private void putAttributes()
    {
        String inherits = inheritanceOf(currentClass);
        if(attributes.containsKey(inherits)) attributes.get(currentClass).addAll(attributes.get(inherits));
        attributes.get(currentClass).addAll(classDescriptors.get(currentClass).getAttributesKey());
    }
    
    private String getAttributeType(String attribute,String className)
    {
        while(className!="Object")
        {
            ClassDescriptor descriptor = classDescriptors.get(className);
            if(descriptor.hasAttribute(attribute)) return descriptor.typeOf(attribute);
            className = inheritanceOf(className);
        }
        return "";
    }
 
    private String getMethodType(String method, String className,boolean isClass)
    {
        while(className!="Object")
        {
            ClassDescriptor descriptor = classDescriptors.get(className);
            if(descriptor.hasMethod(method)) 
            {
                return isClass ? className : descriptor.getMethod(method).getType();
            }
            className = inheritanceOf(className);
        }
        return "";
    }
           
    private int getClassSize(String className)
    {
        int size=0;
        for(var attribute : attributes.get(className))
        {
            String type = getAttributeType(attribute,className);
            if(type.equals("Int") || type.equals("String") || type.equals("Bool")) size++;
            else size+=getClassSize(type);
        }
        return Math.max(1,size);
    }
    
    private String toType(String value)
    {
        value = value.toLowerCase();
        if(value.contains("int")) return "int";
        else if(value.contains("bool")) return "bool";
        return "ptr";
    }
    
    private int toInt(String value) { return Integer.parseInt(value); }
    
    private boolean toBool(String value) { return Boolean.parseBoolean(value); }
    
    private String dest(String type)
    {
        if(type.equals("int")) return intDest();
        else if(type.equals("bool")) return boolDest();
        else return ptrDest();
    }
    
    private String intDest()
    {
        String dest = "int"+intVarCounter;
        intVarCounter++;
        return dest;
    }
    
    private String boolDest()
    {
        String dest = "bool"+boolVarCounter;
        boolVarCounter++;
        return dest;
    }
    
    private String ptrDest()
    {
        String dest = "ptr"+ptrCounter;
        ptrCounter++;
        return dest;
    }
    
    private void resetVars()
    {
        intVarCounter=0;
        boolVarCounter=0;
        ptrCounter=0;
        labelCounter=0;
    }
    
    
    
    private int nextNodeLvl() 
    { 
        if(syntacticTree.peek()!=null) return syntacticTree.peek().getLevel(); 
        return -1;
    }
}