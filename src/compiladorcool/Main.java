package compiladorcool;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args)
    {
        ArrayList<Error> errors = new ArrayList<>();
        LexicalAnaliser lexical = new LexicalAnaliser("teste.txt",errors);
        SyntacticAnaliser syntactic = new SyntacticAnaliser(lexical,errors);
        
        syntactic.analise();

        lexical.closeFile();
        
        for(var error: errors) System.out.println(error.getMessage());
    }        
}
