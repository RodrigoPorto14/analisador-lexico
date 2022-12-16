package compiladorcool.codegeneration;

import compiladorcool.semantic.ClassDescriptor;
import compiladorcool.semantic.Node;
import java.util.HashMap;
import java.util.Queue;
import java.util.ArrayList;

public class CodeGenerator {
    
    private HashMap<String,ClassDescriptor> classDescriptors;
    private Queue<Node> syntacticTree;
    private HashMap<String,ArrayList<String>> attributes;
    private BrilProgram brilProgram; 
    private Node currentNode;
    private String currentClass;
    private int currentFunctionPos;
    private int intVarCounter=0;
    
    public CodeGenerator(HashMap<String,ClassDescriptor> classDescriptors, Queue<Node> syntacticTree)
    {
        this.brilProgram = new BrilProgram();
        this.classDescriptors=classDescriptors;
        this.syntacticTree=syntacticTree;
        this.attributes = new HashMap<>();
        addIOFunction();
        
    }
    
    public BrilProgram generateCode()
    {
        while(!syntacticTree.isEmpty()) nextNode();
        
        /*for(var key : attributes.keySet())
        {
            System.out.print(key+": ");
            System.out.print(attributes.get(key).toString());
            System.out.printf(" = %d\n",getClassSize(key));
        }*/
        return brilProgram;
    }
    
   
    private String inheritanceOf(String className) 
    { 
        return classDescriptors.get(className).getInherits(); 
    }
    
    private void putInheritsAttributes()
    {
        String className = inheritanceOf(currentClass);
        if(attributes.containsKey(className))
        {
            attributes.get(currentClass).addAll(attributes.get(className));
        }
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
    
    private String getMethodType(String method,String className)
    {
        while(className!="Object")
        {
            ClassDescriptor descriptor = classDescriptors.get(className);
            if(descriptor.hasMethod(method)) return className;
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
        return size;
    }
    
    private String toType(String value)
    {
        if(value.contains("int")) return "int";
        else return "bool";
    }
    
    private int toInt(String value) { return Integer.parseInt(value); }
    
    private String intDest()
    {
        String dest = "int"+intVarCounter;
        intVarCounter++;
        return dest;
    }
    
    private void resetVars()
    {
        intVarCounter=0;
    }
    
    private Function getCurrentFunction()
    {
        return brilProgram.getFunctions().get(currentFunctionPos);
    }
    
    private void addIOFunction()
    {
        currentFunctionPos=0;
        Function f = new Function("out_int_IO");
        f.setArgs(new Argument("value","int"));
        
        Instruction instr = new Instruction("print");
        instr.setArgs("value");
        f.addInstruction(instr);
        
        brilProgram.getFunctions().add(f);
    }
    
    
    private int nextNodeLvl() 
    { 
        if(syntacticTree.peek()!=null) return syntacticTree.peek().getLevel(); 
        return -1;
    }
    
    private String nextNode()
    {
        currentNode = syntacticTree.poll();
        switch(currentNode.getType())
        {
            case CLASS -> _class();
            case ATTRIBUTE -> attribute();
            case METHOD -> method();
            case METHOD_CALL -> { return method_call(); }
            case ARITHMETIC -> { return arithmetic(); }
            case INTEGER -> { return integer(); }
        }
        return "";
    }
    
    private void _class()
    {
        currentClass=currentNode.getValue();
        attributes.put(currentClass, new ArrayList<>());
        putInheritsAttributes();
    }
    
    private void attribute()
    {
        attributes.get(currentClass).add(currentNode.getValue());
    }
    
    private void method()
    {
        resetVars();
  
        String funcName = currentNode.getValue();
        if(!currentClass.equals("Main")) funcName+="_"+currentClass;
        
        Function newFunction = new Function(funcName);
        brilProgram.getFunctions().add(newFunction);
        currentFunctionPos++;
        String returnValue="";
        while(nextNodeLvl()==currentNode.getLevel()+1)
        {
            returnValue = nextNode();
        }
        
        newFunction.setType(toType(returnValue)); 
    } 
    
    private String arithmetic()
    {
        Function f = getCurrentFunction();
        String op = currentNode.getValue();
    
        if(op.equals("+")) op = "add";
        else if(op.equals("-")) op = "sub";
        else if(op.equals("*")) op = "mul";
        else op = "div";
        
        Instruction instr = new Instruction(op);
        instr.setType("int");
        instr.setArgs(nextNode(),nextNode());
        String dest = intDest();
        instr.setDest(dest);
        f.addInstruction(instr);
        return dest;   
    }
    
    private String integer()
    {
        Function f = getCurrentFunction();
        String value = currentNode.getValue();
        String dest = intDest();
        f.addInstruction(new ConstantInt(toInt(value),dest));
        return dest;
    }
    
    private String method_call()
    {
        Function f = getCurrentFunction();
        String value = currentNode.getValue();
        String methodName = value+"_"+getMethodType(value,currentClass);
        Instruction instr = new Instruction("call");
        
        ArrayList<String> args = new ArrayList<>();
        while(nextNodeLvl()==currentNode.getLevel()+1){ args.add(nextNode()); }
        
        instr.setArgs(args.toArray(String[]::new));
        instr.setFuncs(methodName);
        f.addInstruction(instr);
        return methodName;
    }
}
