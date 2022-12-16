package compiladorcool.codegeneration;

import java.util.ArrayList;

public class FunctionPtr extends Func{
    
    private Pointer type;
    
    public FunctionPtr(String name, Pointer type)
    {
        super(name);
        this.type=type;
    }
}

