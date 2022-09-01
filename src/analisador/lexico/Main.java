package analisador.lexico;

public class Main {

    public static void main(String[] args)
    {
        Token token;
        LexicalAnaliser lexico = new LexicalAnaliser("teste.txt");
        
        while((token = lexico.nextToken())!=null)
        {
            System.out.println(token.getDescription());
            System.out.println(token.getType());
            System.out.println(token.getRow());
            System.out.println("======================");
        }    
    }        
}
