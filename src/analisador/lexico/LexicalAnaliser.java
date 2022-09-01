package analisador.lexico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class LexicalAnaliser {
    
   private BufferedReader codeFile;
   private int row;
   private char lastChar;
   private boolean tokenRead;
   private final ArrayList<String> keywords;
   private final HashMap<String,TokenType> symbols;
   
   public LexicalAnaliser(String fileName)
   {
       try{this.codeFile = new BufferedReader(new FileReader(fileName));}
       catch(IOException e){e.getStackTrace();}
       
       this.row=1;
       this.tokenRead=false;
       this.keywords = new ArrayList<>();
       this.symbols = new HashMap<>();
       addKeywordsAndSymbols();
   }
   
   public Token nextToken()
   {
       String tokenDescription="",regex="";
       char currentChar=' ';
       int state=0,startCommentRow=0,startStringRow=0;
       
       try
       {
            while(currentChar!=Character.MAX_VALUE)
            {
                if(tokenRead) {currentChar = lastChar; tokenRead=false;}
                else currentChar = (char) codeFile.read();
                
                if(currentChar == '\n') row++;
               
                switch(state)
                {
                    case 0 -> 
                    {
                        if(contains("[0-9]",currentChar)) state=1;
                        if(contains("[a-z]",currentChar)) state=2;
                        if(contains("[A-Z]",currentChar)) state=3;
                        if(contains("[+*/~:{}@.,;)]",currentChar)) return new Token(""+currentChar,getSymbolType(""+currentChar),row);
                        if(currentChar=='-') state=4;
                        if(currentChar=='<') {regex="[=-]";state=6;}
                        if(currentChar=='=') {regex="[>]";state=6;}
                        if(currentChar=='(') state=7;
                        if(currentChar=='"') {startStringRow=row;state=10;}
                        if(state!=0) tokenDescription+=currentChar;
                    }
                    
                    case 1 -> 
                    {
                        if(contains("[0-9]",currentChar)) tokenDescription+=currentChar;
                        else 
                        {
                            saveChar(currentChar);
                            return new Token(tokenDescription,TokenType.INTEGER,row);
                        }    
                    }
                    
                    case 2 -> 
                    {
                        if(contains("[a-zA-Z_]",currentChar)) tokenDescription+=currentChar;
                        else 
                        {
                            saveChar(currentChar);
                            TokenType tokenType;
                            if(isBoolean(tokenDescription) || keywords.contains(tokenDescription.toLowerCase()))  tokenType = getKeywordType(tokenDescription); 
                            else tokenType = TokenType.OBJECT_IDENTIFIER;
                                
                            return new Token(tokenDescription,tokenType,row);
                        }    
                    }
                    
                    case 3 -> 
                    {
                        if(contains("[a-zA-Z_]",currentChar)) tokenDescription+=currentChar;
                        else 
                        {
                            saveChar(currentChar);
                            TokenType tokenType;
                            if(keywords.contains(tokenDescription.toLowerCase())) tokenType = getKeywordType(tokenDescription); 
                            else tokenType = TokenType.TYPE_IDENTIFIER;
                                
                            return new Token(tokenDescription,tokenType,row);
                        }    
                    }
                    
                    case 4 -> 
                    {
                        if(currentChar=='-') {tokenDescription="";state=5;}
                        else 
                        {
                            saveChar(currentChar);
                            return new Token(tokenDescription,getSymbolType(tokenDescription),row);
                        }    
                    }
                   
                    case 5 -> {if(currentChar=='\n') state=0;}
                    
                    case 6 -> 
                    {
                        if(contains(regex,currentChar)) tokenDescription+=currentChar;
                        else saveChar(currentChar);
                        
                        return new Token(tokenDescription,getSymbolType(tokenDescription),row);
                    }
                    
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
                    
                    case 8 -> {if(currentChar=='*') state=9;}
                    case 9 -> {if(currentChar==')') state=0;}
                    case 10 ->
                    {
                        tokenDescription+=currentChar;
                        if(currentChar=='"') return new Token(tokenDescription,TokenType.STRING,startStringRow);
                    }
                }   
            }
            
            if(state==8 || state==9) System.out.println("Row "+startCommentRow+": Unterminated Comment!");
            if(state==10) System.out.println("Row "+startStringRow+": Unterminated String!");
            return null;
       }
       catch(IOException e){e.getStackTrace();return null;}     
   }
   
   private void saveChar(char c){lastChar = c; tokenRead = true;}
   private boolean isBoolean(String str){return str.toLowerCase().equals("true") || str.toLowerCase().equals("false");}
   
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
        
}
