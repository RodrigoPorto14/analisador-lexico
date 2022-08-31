package analisador.lexico;


public class Main {

    public static void main(String[] args)
    {
        Token token;
        AnalisadorLexico analisador = new AnalisadorLexico("teste.txt");
        
        while((token = analisador.nextToken())!=null)
        {
            System.out.println(token.getDescription());
            System.out.println(token.getType());
            System.out.println(token.getRow());
            System.out.println("======================");
        }
        
    }
        
}
