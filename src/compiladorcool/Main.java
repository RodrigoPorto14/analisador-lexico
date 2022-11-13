package compiladorcool;
import java.util.ArrayList;
import java.util.Queue;

public class Main {
    
    public static void main(String[] args)
    {
        ArrayList<Error> errors = new ArrayList<>();
        Queue<Node> syntacticTree;
        
        LexicalAnaliser lexical = new LexicalAnaliser("teste.txt",errors);
        
        SyntacticAnaliser syntactic = new SyntacticAnaliser(lexical,errors);
        
        syntacticTree = syntactic.analise();
        lexical.closeFile();
        
        /*while(!syntacticTree.isEmpty())
        {
            Node node = syntacticTree.poll();
            //System.out.printf("%d",node.getLevel());
            for(int i=1;i<node.getLevel();i++) System.out.print(" ");
            System.out.println(node.getType().toString());
        }*/
        
        lexical = new LexicalAnaliser("teste.txt",errors);
        SemanticAnaliser semantic = new SemanticAnaliser(lexical,syntacticTree,errors);
        
        semantic.analise();
        lexical.closeFile();
        
        for(var error: errors) System.out.println(error.getMessage());  
    }        
}
