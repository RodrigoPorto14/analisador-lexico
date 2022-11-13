package compiladorcool;
import java.util.HashMap;

public class ClassDescriptor {
    private final String inherits;
    private final HashMap<String,Method> methods = new HashMap<>();
    private final HashMap<String,String> attributes = new HashMap<>();
    
    public ClassDescriptor(String inherits)
    {
        this.inherits=inherits;
    }
    
    public String getInherits() { return inherits; }
    public Method getMethod(String method) { return methods.get(method); }
    public String typeOf(String attribute) { return attributes.get(attribute); }
    
    public boolean hasMethod(String method){ return methods.containsKey(method); }
    public boolean hasAttribute(String attribute) { return attributes.containsKey(attribute); }
    //public boolean MethodHasType(String method, String type) { return methods.get(method).getType().equals(type); }
    public void addMethod(String name, String type, String... args) { methods.put(name, new Method(type,args)); }
    public void addAttribute(String name, String type) { attributes.put(name, type); }
    
    public void show()
    {
        System.out.printf("Inherits: %s\n",inherits);
        for(var m: methods.keySet()) System.out.printf("%s%s\n", m,methods.get(m).getMethod()); 
    }
}
