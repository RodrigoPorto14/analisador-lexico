package compiladorcool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class LexicalAnalyzer {
    
   private BufferedReader codeFile;
   private int row;
   private char lastChar;
   private boolean savedChar;
   private final ArrayList<String> keywords;
   private final ArrayList<Error> errors;
   private final HashMap<String,TokenType> symbols;
   
   public LexicalAnalyzer(String fileName, ArrayList<Error> errors)
   {
       try{codeFile = new BufferedReader(new FileReader(fileName));}
       catch(IOException e){e.getStackTrace();}
       
       row=1;
       savedChar=false;
       this.errors=errors;
       keywords = new ArrayList<>();
       symbols = new HashMap<>();
       addKeywordsAndSymbols();
   }
   
   
   public ArrayList<Token> getTokens()
   {
       ArrayList<Token> tokens = new ArrayList<>();
       Token tk;
       while((tk = nextToken())!=null) tokens.add(tk);
       return tokens;
   }
   
   private Token nextToken()
   {
       String tokenDescription="",regex="";
       char currentChar=' ';
       int state=0,startCommentRow=0,startStringRow=0;
       
       try
       {
            while(currentChar!=EOF())
            {
                if(savedChar) {currentChar = lastChar; savedChar=false;}
                else currentChar = (char) codeFile.read();
                
                if(currentChar == '\n') row++;
               
                switch(state)
                {
                    //PRIMEIRO CARACTER
                    case 0 -> 
                    {
                        if(contains("[0-9]",currentChar)) state=1;
                        if(contains("[a-z]",currentChar)) state=2;
                        if(contains("[A-Z]",currentChar)) state=3;
                        if(contains("[+*/~:{}@.,;)]",currentChar)) return new Token(toStr(currentChar),getSymbolType(toStr(currentChar)),row);
                        if(currentChar=='-') state=4;
                        if(currentChar=='<') {regex="[=-]";state=6;}
                        if(currentChar=='=') {regex="[>]";state=6;}
                        if(currentChar=='(') state=7;
                        if(currentChar=='"') {startStringRow=row;state=10;}
                        
                        if(state!=0) tokenDescription+=currentChar;                       
                        else if(!contains("[\n\r\f\t ]",currentChar) && currentChar!=EOF()) errors.add(new LexicalError(String.format("Token '%c' nao reconhecido",currentChar),row));                      
                    }
                    
                    // INTEIRO
                    case 1 -> 
                    {
                        if(contains("[0-9]",currentChar)) tokenDescription+=currentChar;
                        else 
                        {
                            saveChar(currentChar);
                            return new Token(tokenDescription,TokenType.INTEGER,row);
                        }                           
                    }
                    
                    // ID DE OBJETO ou PALAVRA-CHAVE
                    case 2 -> 
                    {
                        if(contains("[a-zA-Z0-9_]",currentChar)) tokenDescription+=currentChar;
                        else 
                        {
                            TokenType tokenType;
                            if(isBoolean(tokenDescription) || keywords.contains(tokenDescription.toLowerCase()))  tokenType = getKeywordType(tokenDescription); 
                            else tokenType = TokenType.OBJECT_IDENTIFIER;
                            
                            saveChar(currentChar);
                            return new Token(tokenDescription,tokenType,row);
                        }    
                    }
                    
                    // ID DE TIPO ou PALAVRA-CHAVE
                    case 3 -> 
                    {
                        if(contains("[a-zA-Z0-9_]",currentChar)) tokenDescription+=currentChar;
                        else 
                        {                           
                            TokenType tokenType;
                            if(keywords.contains(tokenDescription.toLowerCase())) tokenType = getKeywordType(tokenDescription); 
                            else tokenType = TokenType.TYPE_IDENTIFIER;
                                
                            saveChar(currentChar);
                            return new Token(tokenDescription,tokenType,row);
                        }    
                    }
                    
                    // SUBTRACAO ou COMENTARIO EM LINHA
                    case 4 -> 
                    {
                        if(currentChar=='-') {tokenDescription="";state=5;}
                        else 
                        {
                            saveChar(currentChar);
                            return new Token(tokenDescription,getSymbolType(tokenDescription),row);
                        }
                    }
                    
                    // COMENTARIO EM LINHA
                    case 5 -> {if(currentChar=='\n') state=0;}
                    
                    // SIMBOLOS DE 2 CARACTERES
                    case 6 -> 
                    {
                        if(contains(regex,currentChar)) tokenDescription+=currentChar;
                        else saveChar(currentChar);
                        
                        return new Token(tokenDescription,getSymbolType(tokenDescription),row);
                    }
                    
                    // ABRE-PARENTESES ou COMENTARIO EM BLOCO
                    case 7 -> 
                    {
                        if(currentChar=='*') 
                        {
                            startCommentRow=row;
                            tokenDescription="";
                            state=8;
                        }
                        else
                        {
                            saveChar(currentChar);
                            return new Token(tokenDescription,getSymbolType(tokenDescription),row);
                        }
                    }
                    
                    // COMENTARIO EM BLOCO
                    case 8 -> {if(currentChar=='*') state=9;}
                    case 9 -> 
                    {
                        if(currentChar==')') state=0;
                        else state=8;
                    }
                    
                    // STRING
                    case 10 ->
                    {
                        tokenDescription+=currentChar;
                        if(currentChar=='"') return new Token(tokenDescription,TokenType.STRING,startStringRow);
                    }
                }   
            }
            
            if(state==8 || state==9) errors.add(new LexicalError("Comentario nao terminado",startCommentRow));
            if(state==10) errors.add(new LexicalError("String nao terminada",startStringRow));
            return null;
       }
       catch(IOException e){e.getStackTrace();return null;}     
   }
   
   public void closeFile()
   {
       try{codeFile.close();}
       catch(IOException e){e.getStackTrace();}
   }
   
   private void saveChar(char c){lastChar = c; savedChar = true;}
   private boolean isBoolean(String str){return str.toLowerCase().equals("true") || str.toLowerCase().equals("false");}
   private String toStr(char c) {return ""+c;}
   
   
   private boolean contains(String regex,char c)
   {
       String str = ""+c;
       Pattern p = Pattern.compile(regex);
       Matcher m = p.matcher(str);
       return m.find();
   }
   
   private void addKeywordsAndSymbols()
   {
       try(Scanner keywordSc = new Scanner(new File("keywords.txt"));
           Scanner symbolSc = new Scanner(new File("symbols.txt"));)
       {  
           while(keywordSc.hasNext()) keywords.add(keywordSc.next());
           while(symbolSc.hasNext()) symbols.put(symbolSc.next(), TokenType.valueOf(symbolSc.next()));
       }
       catch(IOException e){e.getStackTrace();}
   }

   private TokenType getKeywordType(String str) {return TokenType.valueOf(str.toUpperCase());}
   private TokenType getSymbolType(String str) {return symbols.get(str);}     
   private char EOF(){return Character.MAX_VALUE;}
   
}