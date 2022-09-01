package analisador.lexico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.IOException;

public class AnalisadorLexico {
    
   private BufferedReader codeFile;
   private int row;
   private char lastChar;
   private boolean tokenRead;
   private ArrayList<String> keywords;
   
   public AnalisadorLexico(String fileName)
   {
       try{this.codeFile = new BufferedReader(new FileReader(fileName));}
       catch(IOException e){e.getStackTrace();}
       
       this.row=1;
       this.tokenRead=false;
       this.keywords = new ArrayList<>();
       addKeywords();
   }
   
   public Token nextToken()
   {
       String tokenDescription="";
        
       char currentChar=' ';
       int state=0;
       
       try
       {
            while(currentChar!=Character.MAX_VALUE)
            {
                if(tokenRead) {currentChar = lastChar; tokenRead=false;}
                else currentChar = (char) codeFile.read();
               
                switch(state)
                {
                    case 0 -> 
                    {
                        if(isDigit(currentChar)) state=1;
                        if(isLowerChar(currentChar)) state=2;
                        if(isUpperChar(currentChar)) state=3;
                        if(isWhiteSpace(currentChar)) state=4;
                        if(state!=0) tokenDescription+=currentChar;
                    }
                    
                    case 1 -> 
                    {
                        if(isDigit(currentChar)) tokenDescription+=currentChar;
                        else 
                        {
                            saveChar(currentChar);
                            return new Token(tokenDescription,TokenType.INTEGER,row);
                        }    
                    }
                    
                    case 2 -> 
                    {
                        if(validCharID(currentChar)) tokenDescription+=currentChar;
                        else 
                        {
                            saveChar(currentChar);
                            TokenType tokenType;
                            if(isBoolean(tokenDescription) || keywords.contains(tokenDescription))  tokenType = TokenType.KEYWORD;
                            else tokenType = TokenType.OBJECT_IDENTIFIER;
                                
                            return new Token(tokenDescription,tokenType,row);
                        }    
                    }
                    
                    case 3 -> 
                    {
                        if(validCharID(currentChar)) tokenDescription+=currentChar;
                        else 
                        {
                            saveChar(currentChar);
                            TokenType tokenType;
                            if(keywords.contains(tokenDescription)) tokenType = TokenType.KEYWORD; 
                            else tokenType = TokenType.TYPE_IDENTIFIER;
                                
                            return new Token(tokenDescription,tokenType,row);
                        }    
                    }
                    
                    case 4 -> 
                    {
                        if(isWhiteSpace(currentChar)) tokenDescription+=currentChar;
                        else 
                        {
                            saveChar(currentChar);
                            Token token = new Token(tokenDescription,TokenType.WHITE_SPACE,row);
                            if(tokenDescription.contains("\n"))row++;
                            return token;
                        }    
                    }
                }   
            }
            return null;
       }
       catch(IOException e){e.getStackTrace();return null;}     
   }
   
   private void saveChar(char c){lastChar = c; tokenRead = true;}
   private boolean isDigit(char c){return c >= '0' && c <= '9';}
   private boolean isUpperChar(char c){return c >= 'A' && c <= 'Z';}
   private boolean isLowerChar(char c){return c >= 'a' && c <= 'z';}
   private boolean isUnderScore(char c){return c == '_';}
   private boolean isWhiteSpace(char c) {return c == 32 || (c >= 9 && c <= 13);}
   private boolean validCharID(char c){return isDigit(c) || isUpperChar(c) || isLowerChar(c) || isUnderScore(c);}
   private boolean isBoolean(String str){return str.toLowerCase().equals("true") || str.toLowerCase().equals("false");}
   
   private void addKeywords()
   {
       try
       {
           Scanner sc = new Scanner(new File("keywords.txt"));
           while(sc.hasNext()) keywords.add(sc.next());
       }
       catch(IOException e){e.getStackTrace();}
   }   
}
