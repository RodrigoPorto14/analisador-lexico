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
   private boolean savedChar;
   private final ArrayList<String> keywords;
   private final HashMap<String,TokenType> symbols;
   
   public LexicalAnaliser(String fileName)
   {
       try{this.codeFile = new BufferedReader(new FileReader(fileName));}
       catch(IOException e){e.getStackTrace();}
       
       this.row=1;
       this.savedChar=false;
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
                if(savedChar) {currentChar = lastChar; savedChar=false;}
                else currentChar = (char) codeFile.read();
                
                if(currentChar == '\n') row++;
               
                switch(state)
                {
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
                    }
                    
                    case 1 -> 
                    {
                        if(contains("[0-9]",currentChar)) tokenDescription+=currentChar;
                        else return createToken(tokenDescription,TokenType.INTEGER,currentChar);                           
                    }
                    
                    case 2 -> 
                    {
                        if(contains("[a-zA-Z_]",currentChar)) tokenDescription+=currentChar;
                        else 
                        {
                            TokenType tokenType;
                            if(isBoolean(tokenDescription) || keywords.contains(tokenDescription.toLowerCase()))  tokenType = getKeywordType(tokenDescription); 
                            else tokenType = TokenType.OBJECT_IDENTIFIER;
                            
                            return createToken(tokenDescription,tokenType,currentChar);
                        }    
                    }
                    
                    case 3 -> 
                    {
                        if(contains("[a-zA-Z_]",currentChar)) tokenDescription+=currentChar;
                        else 
                        {                           
                            TokenType tokenType;
                            if(keywords.contains(tokenDescription.toLowerCase())) tokenType = getKeywordType(tokenDescription); 
                            else tokenType = TokenType.TYPE_IDENTIFIER;
                                
                            return createToken(tokenDescription,tokenType,currentChar);
                        }    
                    }
                    
                    case 4 -> 
                    {
                        if(currentChar=='-') {tokenDescription="";state=5;}
                        else return createToken(tokenDescription,getSymbolType(tokenDescription),currentChar);                            
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
                        else return createToken(tokenDescription,getSymbolType(tokenDescription),currentChar);                          
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
   
   private Token createToken(String description, TokenType type,char c)
   {
       saveChar(c);
       return new Token(description,type,row);
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
}