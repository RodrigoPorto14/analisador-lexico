package analisador.lexico;

import java.io.BufferedReader;
import java.io.FileReader;

public class AnalisadorLexico {
    
   private BufferedReader codeFile;
   private int row;
   private char lastChar;
   private boolean tokenRead;
   
   public AnalisadorLexico(String fileName)
   {
       try
       {
           this.codeFile = new BufferedReader(new FileReader(fileName));
       }
       catch(Exception e){e.getStackTrace();}
       
       this.row=1;
       this.tokenRead=false;
   }
   
   public Token nextToken()
   {
       String tokenDescription="";
       char currentChar;
       int estado=0;
       try
       {
            while(codeFile.ready())
            {
                if(tokenRead) currentChar = lastChar;
                else currentChar = (char) codeFile.read();
                
                if(currentChar=='\n')row++;
                
                switch(estado)
                {
                    case 0 -> 
                    {
                        
                        if(isDigit(currentChar)) estado=1;
                        if(isLowerChar(currentChar)) estado=3;
                        if(isUpperChar(currentChar)) estado=5;
                        if(estado!=0) tokenDescription+=currentChar;
                    }
                    
                    case 1 -> 
                    {
                        if(isDigit(currentChar)) tokenDescription+=currentChar;
                        else return new Token(tokenDescription,TokenType.INTEGER,row);     
                    }
                }
            }
            return null;
       }
       catch(Exception e){e.getStackTrace();return null;}     
   }
   
   private boolean isDigit(char c){return c >= '0' && c <= '9';}
   private boolean isUpperChar(char c){return c >= 'A' && c <= 'Z';}
   private boolean isLowerChar(char c){return c >= 'a' && c <= 'z';}
   private boolean isUnderScore(char c){return c == '_';}
    
}
