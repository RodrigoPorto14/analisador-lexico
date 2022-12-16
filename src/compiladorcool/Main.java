package compiladorcool;
import compiladorcool.semantic.Node;
import compiladorcool.syntactic.SyntacticAnalyzer;
import compiladorcool.codegeneration.CodeGenerator;
import java.util.ArrayList;
import java.util.Queue;
import com.google.gson.Gson;
import compiladorcool.codegeneration.BrilProgram;
import compiladorcool.lexical.LexicalAnalyzer;
import compiladorcool.lexical.Token;
import compiladorcool.semantic.ClassDescriptor;
import compiladorcool.semantic.SemanticAnalyzer;
import java.util.HashMap;
import java.io.PrintStream;
import java.io.FileNotFoundException;


public class Main {
    
    public static void main(String[] args)
    {
        for(var arg : args)
        {
            ArrayList<Error> errors = new ArrayList<>();
            ArrayList<Token> tokens;
            Queue<Node> syntacticTree;
            HashMap<String,ClassDescriptor> classDescriptors;

            LexicalAnalyzer lexical = new LexicalAnalyzer(arg,errors);
            tokens = lexical.getTokens();
            lexical.closeFile();

            SyntacticAnalyzer syntactic = new SyntacticAnalyzer(tokens,errors);
            syntacticTree = syntactic.analyze();
            
            /*while(!syntacticTree.isEmpty())
            {
                Node node = syntacticTree.poll();
                //System.out.printf("%d",node.getLevel());
                for(int i=1;i<node.getLevel();i++) System.out.print(" ");
                System.out.println(node.getType().toString()+" "+node.getValue());  
            }*/
            
            SemanticAnalyzer semantic = new SemanticAnalyzer(tokens,syntacticTree,errors);
            classDescriptors = semantic.analyze();

            for(var error: errors) System.out.println(error.getMessage());  

            if(errors.isEmpty())
            {
                try(PrintStream ps = new PrintStream(toJson(arg)))
                {
                    CodeGenerator generator = new CodeGenerator(classDescriptors,syntacticTree);
                    BrilProgram brilProgram = generator.generateCode();
                    String json = new Gson().toJson(brilProgram);
                    ps.print(json);
                    System.out.println(json);
                    System.out.println();
                }
                catch(FileNotFoundException e){}
            }
        }  
    }      
    
    private static String toJson(String coolProgram)
    {
        return coolProgram.substring(0, coolProgram.lastIndexOf('.'))+".json";
    }
}
