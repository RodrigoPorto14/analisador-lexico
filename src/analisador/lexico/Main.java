package analisador.lexico;


public class Main {

    public static void main(String[] args)
    {
        Token token;
        AnalisadorLexico lexico = new AnalisadorLexico("teste.txt");
        
        while((token = lexico.nextToken())!=null)
        {
            System.out.println(token.getDescription());
            System.out.println(token.getType());
            System.out.println(token.getRow());
            System.out.println("======================");
        }
        
    }
        
}
