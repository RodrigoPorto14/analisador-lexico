package compiladorcool.codegeneration;

import java.util.ArrayList;

public class Function {
    
    private String name;
    private ArrayList<Argument> args;
    private String type;
    private ArrayList<Instruction> instrs;
    
    public Function(String name)
    {
        this.name=name;
        //this.type=type;
    }
    
    //public String getName(){return name;}
    //public String getType(){return type;}
   // public void setName(String name){this.name=name;}
    //public void setType(String type){this.type=type;}
    
}
