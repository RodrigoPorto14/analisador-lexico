package compiladorcool;

public class Node {
    private final NodeType type;
    private final int level;
    
    public Node(NodeType type, int level)
    {
        this.type=type;
        this.level=level;
    }
    
    public NodeType getType(){return type;}
    public int getLevel(){return level;}
    
}
