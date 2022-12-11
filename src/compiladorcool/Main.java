package compiladorcool;
import compiladorcool.semantic.Node;
import compiladorcool.semantic.SemanticAnalyzer;
import compiladorcool.syntactic.SyntacticAnalyzer;
import compiladorcool.lexical.Token;
import compiladorcool.lexical.LexicalAnalyzer;
import compiladorcool.codegeneration.Function;
import compiladorcool.codegeneration.CodeGenerator;
import compiladorcool.codegeneration.BrilProgram;
import java.util.ArrayList;
import java.util.Queue;
import com.google.gson.Gson;
import compiladorcool.semantic.ClassDescriptor;
import java.util.HashMap;


public class Main {
    
    public static void main(String[] args)
    {
        
        Function f = new Function("main");
        ArrayList<Error> errors = new ArrayList<>();
        ArrayList<Token> tokens;
        Queue<Node> syntacticTree;
        HashMap<String,ClassDescriptor> classDescriptors;
        
        LexicalAnalyzer lexical = new LexicalAnalyzer("teste.txt",errors);
        tokens = lexical.getTokens();
        lexical.closeFile();
        
        SyntacticAnalyzer syntactic = new SyntacticAnalyzer(tokens,errors);
        syntacticTree = syntactic.analyze();

        SemanticAnalyzer semantic = new SemanticAnalyzer(tokens,syntacticTree,errors);
        classDescriptors = semantic.analyze();
        
        for(var error: errors) System.out.println(error.getMessage());  
        
        /*while(!syntacticTree.isEmpty())
        {
            Node node = syntacticTree.poll();
            //System.out.printf("%d",node.getLevel());
            for(int i=1;i<node.getLevel();i++) System.out.print(" ");
            System.out.println(node.getType().toString());
        }*/
        
        if(errors.isEmpty())
        {
            CodeGenerator generator = new CodeGenerator(classDescriptors,syntacticTree);
            BrilProgram brilProgram = generator.generateCode();
            System.out.println(new Gson().toJson(brilProgram));
        }
        
        
    }        
}
