package compiladorcool.codegeneration;

import java.util.ArrayList;

public class BrilProgram {
    
    private ArrayList<Function> functions;
    
    public BrilProgram()
    {
        functions = new ArrayList<>();
    }
    
    public ArrayList<Function> getFunctions() { return functions; }
}
