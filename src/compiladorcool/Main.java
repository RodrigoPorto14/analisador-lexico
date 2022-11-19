package compiladorcool;
import java.util.ArrayList;
import java.util.Queue;

public class Main {
    
    public static void main(String[] args)
    {
        ArrayList<Error> errors = new ArrayList<>();
        ArrayList<Token> tokens;
        Queue<Node> syntacticTree;
        
        LexicalAnalyzer lexical = new LexicalAnalyzer("teste.txt",errors);
        tokens = lexical.getTokens();
        lexical.closeFile();
        
        SyntacticAnalyzer syntactic = new SyntacticAnalyzer(tokens,errors);
        syntacticTree = syntactic.analyze();
        
        /*while(!syntacticTree.isEmpty())
        {
            Node node = syntacticTree.poll();
            //System.out.printf("%d",node.getLevel());
            for(int i=1;i<node.getLevel();i++) System.out.print(" ");
            System.out.println(node.getType().toString());
        }*/

        SemanticAnalyzer semantic = new SemanticAnalyzer(tokens,syntacticTree,errors);
        semantic.analyze();
        
        for(var error: errors) System.out.println(error.getMessage());  
    }        
}
