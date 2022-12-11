package compiladorcool.semantic;

public class Node {
    private final NodeType type;
    private int level;
    private String value;
    
    public Node(NodeType type, int level)
    {
        this.type=type;
        this.level=level;
    }
    
    public Node(NodeType type, int level, String value)
    {
        this.type=type;
        this.level=level;
        this.value=value;
    }
    
    public NodeType getType(){return type;}
    public int getLevel(){return level;}
    public String getValue(){return value;}
    public void setLevel(int level){ this.level=level; }
    
}
