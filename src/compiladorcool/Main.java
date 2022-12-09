package compiladorcool;
import compiladorcool.semantic.Node;
import compiladorcool.semantic.SemanticAnalyzer;
import compiladorcool.syntactic.SyntacticAnalyzer;
import compiladorcool.lexical.Token;
import compiladorcool.lexical.LexicalAnalyzer;
import compiladorcool.codegeneration.Function;
import java.util.ArrayList;
import java.util.Queue;
import com.google.gson.Gson;

public class Main {
    
    public static void main(String[] args)
    {
        
        Function f = new Function("main");
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
        
        System.out.println(new Gson().toJson(f));
    }        
}
