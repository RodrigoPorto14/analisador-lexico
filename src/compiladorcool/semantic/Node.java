package compiladorcool.semantic;

public class Node {
    private final NodeType type;
    private int level;
    
    public Node(NodeType type, int level)
    {
        this.type=type;
        this.level=level;
    }
    
    public NodeType getType(){return type;}
    public int getLevel(){return level;}
    public void setLevel(int level){ this.level=level; }
    
}
