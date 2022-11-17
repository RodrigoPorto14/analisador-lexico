package compiladorcool;
import java.util.ArrayList;
import java.util.Queue;

public class Main {
    
    public static void main(String[] args)
    {
        ArrayList<Error> errors = new ArrayList<>();
        ArrayList<Token> tokens;
        Queue<Node> syntacticTree;
        
        LexicalAnaliser lexical = new LexicalAnaliser("teste.txt",errors);
        tokens = lexical.getTokens();
        lexical.closeFile();
        
        SyntacticAnaliser syntactic = new SyntacticAnaliser(tokens,errors);
        syntacticTree = syntactic.analise();
        
        
        /*while(!syntacticTree.isEmpty())
        {
            Node node = syntacticTree.poll();
            //System.out.printf("%d",node.getLevel());
            for(int i=1;i<node.getLevel();i++) System.out.print(" ");
            System.out.println(node.getType().toString());
        }*/
        

        SemanticAnaliser semantic = new SemanticAnaliser(tokens,syntacticTree,errors);
        semantic.analise();
        
        for(var error: errors) System.out.println(error.getMessage());  
    }        
}
