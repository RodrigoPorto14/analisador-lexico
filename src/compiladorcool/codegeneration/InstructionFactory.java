package compiladorcool.codegeneration;

public class InstructionFactory {
    
    public static Instr createCall(String name,String dest,String type,String... args)
    {
        Instr instr;
        if(type.equals("ptr")) instr = new Operation("call");
        else instr = new Instruction("call");
        
        instr.setFuncs(name);
        instr.setDest(dest);
        instr.setType(type);
        instr.setArgs(args);
        return instr;
    }
    
    public static Instruction createOperation(String op, String dest, String... args)
    {
        Instruction instr = new Instruction(op);
        instr.setDest(dest);
        instr.setArgs(args);
        return instr;
    }
    
    public static Instruction createNot(String dest, String arg)
    {
        Instruction instr = new Instruction("not");
        instr.setDest(dest);
        instr.setArgs(arg);
        return instr;
    }
    
    public static Instr createBr(String arg, String... labels)
    {
        Instruction instr = new Instruction("br");
        instr.setArgs(arg);
        instr.setLabels(labels);
        return instr;
    }
    
    public static Instr createJmp(String label)
    {
        Instruction instr = new Instruction("jmp");
        instr.setLabels(label);
        return instr;
    }
    
    public static Instr createRet(String arg)
    {
        Instruction instr = new Instruction("ret");
        instr.setArgs(arg);
        return instr;
    }
    
    public static Instr createPrint(String... args)
    {
        Instruction instr = new Instruction("print");
        instr.setArgs(args);
        return instr;
    }
    
    public static Instr createId(String dest, String type, String arg)
    {
        Instruction instr = new Instruction("id");
        instr.setDest(dest);
        instr.setType(type);
        instr.setArgs(arg);
        return instr;
    }
    
    public static Instr createAlloc(String size, String dest)
    {
        Operation instr = new Operation("alloc");
        instr.setArgs(size);
        instr.setDest(dest);
        instr.setType("ptr");
        return instr;
    }
    
    public static Instr createPtrAdd(String ptr, String value, String dest)
    {
        Operation instr = new Operation("ptradd");
        instr.setArgs(ptr,value);
        instr.setDest(dest);
        instr.setType("ptr");
        return instr;
    }
    
    public static Instr createPtrStore(String ptr, String value)
    {
        Operation instr = new Operation("store");
        instr.setArgs(ptr,value);
        return instr;
    }
    
    public static Instr createPtrLoad(String ptr,String dest)
    {
        Instruction instr = new Instruction("load");
        instr.setArgs(ptr);
        instr.setDest(dest);
        instr.setType("int");
        return instr;
    }
    
    public static Instr createFree(String ptr)
    {
        Operation instr = new Operation("free");
        instr.setArgs(ptr);
        return instr;
    }
}
