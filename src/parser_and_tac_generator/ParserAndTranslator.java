package parser_and_tac_generator;

import lexer.Lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ParserAndTranslator {

    static int line = 0;                //used to track line number when parsing
    static int tokenCounter = 0;        //used to update 'line'
    static int parsetreeDepth = 0;      //used to track depth when printing parse tree
    static String filename;             //name/path of code file given by user
    static int n = 1;                   //used to track line number for three address code
    static int t = 1;                   //used to track number for temporary variable generation
    static int relativeAddress = 0;     //used to track relative address for translator's symbol table

    public void setFilename(String file)
    {
        filename = file;
    }

    public int getRelativeAddress()
    {
        return relativeAddress;
    }

    public int getTempVarCount()
    {
        return t;
    }

    static String generateTempVar()
    {
        String tempVar = "t"+t;
        t++;
        return tempVar;
    }

    static void writeToSymbolTable(String s) throws IOException
    {
        FileWriter writer = new FileWriter("parser-symboltable.txt",true);
        writer.append(s+"\n");
        writer.close();
    }

    static void writeToTranslatorSymbolTable(String s, String type, String initVal) throws IOException
    {
        FileWriter writer = new FileWriter("translator-symboltable.txt",true);

        if(initVal.length() > 0)
        {
            writer.append(s+"\t"+type+"\t"+relativeAddress+"\t"+initVal+"\n");
        }
        else
        {
            writer.append(s+"\t"+type+"\t"+relativeAddress+"\n");
        }
        writer.close();

        //update relative address accordingly
        if(type.equals("INT"))
        {
            relativeAddress += 4;
        }
        else if(type.equals("CHAR"))
        {
            relativeAddress += 1;
        }
        else if(type.equals("STR"))
        {
            relativeAddress += initVal.length()-2;
        }
    }

    static String[] extractToken(Scanner reader) throws FileNotFoundException
    {
        String[] token = new String[2];
        String data = "";

        if (reader.hasNext()) {
            data = reader.nextLine(); //reading (token,lexeme) from file
        } else {
            SyntaxError();
        }

        //skipping code lines with no token
        while(Lexer.tokensInLine.get(line) == 0)
        {
            line++;
        }
        tokenCounter++;

        String tempToken = data.substring(1, data.length() - 1); //extracting inner part
        if (tempToken.startsWith("','")) {
            token[0] = "','";
            token[1] = "^";
        } else {
            String[] tokenList = tempToken.split(",");
            token[0] = tokenList[0];
            token[1] = tokenList[1];
        }
        //System.out.println(token[0]+"   "+token[1]);

        //updating tokenCounter and line accordingly
        if(tokenCounter > Lexer.tokensInLine.get(line))
        {
            tokenCounter = 1;
            line++;
            while(Lexer.tokensInLine.get(line) == 0)
            {
                line++;
            }
        }

        return  token;
    }

    static void SyntaxError() throws FileNotFoundException
    {
        File tempF = new File(filename);
        Scanner tempR = new Scanner(tempF);

        String tempStr="";
        for(int i=0;i<=line;i++)
        {
            tempStr = tempR.nextLine();
        }
        tempStr=tempStr.trim();
        System.out.println("Syntax error at line "+(line+1));
        System.out.println((line+1)+"\t"+tempStr);
        System.out.println("Review your code and try again");
        tempR.close();
        System.exit(0);
    }

    static void outputParser(FileWriter writer, String str) throws IOException
    {
        String outputDepthPattern = "";
        for(int i=0; i<parsetreeDepth;i++)
        {
            outputDepthPattern+="==>";
        }
        writer.write(outputDepthPattern+str+"\n");
    }

    static void outputTac(String str) throws IOException
    {
        FileWriter writer = new FileWriter("tac.txt", true);
        writer.append(n+"\t"+str+"\n");
        n++;
        writer.close();
    }

    static void backPatch(int line, int patch) throws IOException
    {
        Path path = Paths.get("tac.txt");
        List<String> data = Files.readAllLines(path);
        String str = data.get(line - 1) + patch;
        data.set(line - 1, str);
        Files.write(path, data);
    }

    static void Start(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        if(token[0].equals("IF") || token[0].equals("WHILE") || token[0].equals("PRINT") || token[0].equals("PRINTLN") || token[0].equals("INT") || token[0].equals("CHAR") || token[0].equals("ID") || token[0].equals("INPUT") || token[0].equals("S_COMMENT") || token[0].equals("M_COMMENT"))
        {
            attachStr = " Statements";
            outputParser(writer, attachStr);
            Statements(token, writer, reader);
        }
        else
        {
            SyntaxError();
        }

        parsetreeDepth--;
    }

    static void Statements(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;

        String attachStr = " Statement";
        outputParser(writer, attachStr);
        Statement(token, writer, reader);

        attachStr = " Statements";
        outputParser(writer, attachStr);

        if(reader.hasNext(Pattern.quote("(IF,^)")) || reader.hasNext(Pattern.quote("(WHILE,^)")) || reader.hasNext(Pattern.quote("(PRINT,^)")) || reader.hasNext(Pattern.quote("(PRINTLN,^)")) || reader.hasNext(Pattern.quote("(INPUT,^)")) || reader.hasNext(Pattern.quote("(INT,^)")) || reader.hasNext(Pattern.quote("(CHAR,^)")) || reader.hasNext("\\(ID.*") || reader.hasNext("\\(S_COMMENT.*") || reader.hasNext("\\(M_COMMENT.*"))
        {
            token = extractToken(reader);
            Statements(token, writer, reader);
        }
        else
        {
            parsetreeDepth++;
            attachStr = " ^";
            outputParser(writer, attachStr);
            parsetreeDepth--;
        }

        parsetreeDepth--;
    }

    static void Statement(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr, tacStr;

        //IF or WHILE case
        if(token[0].equals("IF") || token[0].equals("WHILE"))
        {
            boolean ifFlag = false;
            if(token[0].equals("IF"))
            {
                ifFlag = true;
            }
            attachStr = " "+token[0];
            outputParser(writer, attachStr);

            //tacStr = token[0].toLowerCase();

            token = extractToken(reader);
            /*if(token[0].contains("'('"))
            {
                attachStr = " (";
                outputParser(writer, attachStr);
                token = extractToken(reader);
            }*/

            attachStr = " Condition";
            outputParser(writer, attachStr);
            tacStr = "if (" + Condition(token, writer, reader) + ") goto ";
            int trackLine1 = n;
            outputTac(tacStr);
            tacStr = "goto ";
            int trackLine2 = n;
            outputTac(tacStr);

            token = extractToken(reader);
            /*if(token[0].contains("')'"))
            {
                attachStr = " )";
                outputParser(writer, attachStr);
                token = extractToken(reader);
            }*/

            if(token[0].contains("':'"))
            {
                attachStr = " :";
                outputParser(writer, attachStr);
            }
            else
            {
                SyntaxError();
            }

            backPatch(trackLine1, n);

            token = extractToken(reader);
            if(token[0].contains("'{'"))
            {
                attachStr = " {";
                outputParser(writer, attachStr);
                writeToSymbolTable("SCOPE START");
            }
            else
            {
                SyntaxError();
            }

            token = extractToken(reader);
            attachStr = " Statements";
            outputParser(writer, attachStr);
            Statements(token, writer, reader);

            tacStr = "goto ";
            int trackLine3 = n;
            outputTac(tacStr);

            token = extractToken(reader);
            if(token[0].contains("'}'"))
            {
                attachStr = " }";
                outputParser(writer, attachStr);
                writeToSymbolTable("SCOPE END");
            }
            else
            {
                SyntaxError();
            }

            backPatch(trackLine2, n);

            if(ifFlag)
            {
                if(reader.hasNext(Pattern.quote("(ELIF,^)")) || reader.hasNext(Pattern.quote("(ELSE,^)")))
                {
                    token = extractToken(reader);
                }
                attachStr = " ElifOrElse";
                outputParser(writer, attachStr);
                ElifOrElse(token, writer, reader);
                backPatch(trackLine3, n);
            }
            else
            {
                backPatch(trackLine3, trackLine1);
            }
        }
        //PRINT and PRINTLN case
        else if(token[0].equals("PRINT") || token[0].equals("PRINTLN"))
        {
            boolean lnFlag = false;
            if(token[0].equals("PRINTLN"))
            {
                lnFlag = true;
            }
            attachStr = " "+token[0];
            outputParser(writer, attachStr);

            token = extractToken(reader);
            if(token[0].contains("'('"))
            {
                attachStr = " (";
                outputParser(writer, attachStr);
            }
            else
            {
                SyntaxError();
            }

            attachStr = " OutputOptions";
            outputParser(writer, attachStr);
            String option = OutputOptions(token, writer, reader);

            if(option.length() == 0)
            {
                String tempVar = generateTempVar();
                tacStr = tempVar + " = \"\\n\"";
                outputTac(tacStr);
                tacStr = "out "+tempVar;
                outputTac(tacStr);

                writeToTranslatorSymbolTable(tempVar, "STR", "\"\\n\"");
            }
            else
            {
                if (option.startsWith("\""))
                    tacStr = "out t"+(t-1);
                else
                    tacStr = "out "+option;
                outputTac(tacStr);
            }

            if(option.length() > 0)
            {
                token = extractToken(reader);
            }
            if(token[0].contains("')'") || option.length() == 0)
            {
                attachStr = " )";
                outputParser(writer, attachStr);
            }
            else
            {
                SyntaxError();
            }

            token = extractToken(reader);
            if(token[0].contains("';'"))
            {
                attachStr = " ;";
                outputParser(writer, attachStr);
            }
            else
            {
                SyntaxError();
            }

            if(lnFlag)
            {
                String tempVar = generateTempVar();
                tacStr = tempVar + " = \"\\n\"";
                outputTac(tacStr);
                tacStr = "out "+tempVar;
                outputTac(tacStr);

                writeToTranslatorSymbolTable(tempVar, "STR", "\"\\n\"");
            }
        }
        //INPUT case
        else if(token[0].equals("INPUT"))
        {
            attachStr = " "+token[0];
            outputParser(writer, attachStr);
            token = extractToken(reader);
            if(token[0].equals("INPUT_OP"))
            {
                attachStr = " INPUT_OP";
                outputParser(writer, attachStr);
                INPUT_OP(token, writer, reader);
            }
            else
            {
                SyntaxError();
            }

            token = extractToken(reader);
            if(token[0].equals("ID"))
            {
                checkID(token[1]);
                attachStr = " ID("+token[1]+")";
                outputParser(writer, attachStr);

                tacStr = "in "+token[1];
                outputTac(tacStr);
            }
            else
            {
                SyntaxError();
            }

            token = extractToken(reader);
            attachStr = " inputDelimiter";
            outputParser(writer, attachStr);
            inputDelimiter(token, writer, reader);
        }
        //Assignment and IncOp/DecOp case
        else if(token[0].equals("ID"))
        {
            if(reader.hasNext(Pattern.quote("('+',^)")))
            {
                attachStr = " IncOp";
                outputParser(writer, attachStr);
                IncOp(token, writer, reader);
            }
            else if(reader.hasNext(Pattern.quote("('-',^)")))
            {
                attachStr = " DecOp";
                outputParser(writer, attachStr);
                DecOp(token, writer, reader);
            }
            else if(reader.hasNext(Pattern.quote("('=',^)")))
            {
                attachStr = " AssignmentStatement";
                outputParser(writer, attachStr);
                AssignmentStatement(token, writer, reader);
            }
            else
            {
                SyntaxError();
            }
        }
        //Variable case
        else if(token[0].equals("INT") || token[0].equals("CHAR"))
        {
            attachStr = " Variable";
            outputParser(writer, attachStr);
            Variable(token, writer, reader);
        }
        //Comments case
        else if(token[0].equals("S_COMMENT") || token[0].equals("M_COMMENT"))
        {
            attachStr = " "+token[0];
            outputParser(writer, attachStr);
        }
        else
        {
            SyntaxError();
        }

        parsetreeDepth--;
    }

    static void ElifOrElse(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr, tacStr;

        if(token[0].equals("ELIF"))
        {
            attachStr = " elif";
            outputParser(writer, attachStr);

            token = extractToken(reader);
            /*if(token[0].contains("'('"))
            {
                attachStr = " (";
                outputParser(writer, attachStr);
                token = extractToken(reader);
            }*/

            attachStr = " Condition";
            outputParser(writer, attachStr);
            tacStr = "if (" + Condition(token, writer, reader) + ") goto ";
            int trackLine1 = n;
            outputTac(tacStr);
            tacStr = "goto ";
            int trackLine2 = n;
            outputTac(tacStr);

            token = extractToken(reader);
            /*if(token[0].contains("')'"))
            {
                attachStr = " )";
                outputParser(writer, attachStr);
                token = extractToken(reader);
            }*/
            if(token[0].contains("':'"))
            {
                attachStr = " :";
                outputParser(writer, attachStr);
            }
            else
            {
                SyntaxError();
            }

            backPatch(trackLine1, n);

            token = extractToken(reader);
            if(token[0].contains("'{'"))
            {
                attachStr = " {";
                outputParser(writer, attachStr);
                writeToSymbolTable("SCOPE START");
            }
            else
            {
                SyntaxError();
            }

            token = extractToken(reader);
            attachStr = " Statements";
            outputParser(writer, attachStr);
            Statements(token, writer, reader);

            tacStr = "goto ";
            int trackLine3 = n;
            outputTac(tacStr);

            token = extractToken(reader);
            if(token[0].contains("'}'"))
            {
                attachStr = " }";
                outputParser(writer, attachStr);
                writeToSymbolTable("SCOPE END");
            }
            else
            {
                SyntaxError();
            }

            backPatch(trackLine2, n);

            if(reader.hasNext(Pattern.quote("(ELIF,^)")) || reader.hasNext(Pattern.quote("(ELSE,^)")))
            {
                token = extractToken(reader);
            }
            attachStr = " ElifOrElse";
            outputParser(writer, attachStr);
            ElifOrElse(token, writer, reader);

            backPatch(trackLine3, n);
        }
        else
        {
            attachStr = " Else";
            outputParser(writer, attachStr);
            Else(token, writer, reader);
        }

        parsetreeDepth--;
    }

    static void Else(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        if(token[0].equals("ELSE"))
        {
            attachStr = " else";
            outputParser(writer, attachStr);

            token = extractToken(reader);
            if(token[0].contains("'{'"))
            {
                attachStr = " {";
                outputParser(writer, attachStr);
                writeToSymbolTable("SCOPE START");
            }
            else
            {
                SyntaxError();
            }

            token = extractToken(reader);
            attachStr = " Statements";
            outputParser(writer, attachStr);
            Statements(token, writer, reader);

            token = extractToken(reader);
            if(token[0].contains("'}'"))
            {
                attachStr = " }";
                outputParser(writer, attachStr);
                writeToSymbolTable("SCOPE END");
            }
            else
            {
                SyntaxError();
            }
        }
        else
        {
            attachStr = " ^";
            outputParser(writer, attachStr);
        }

        parsetreeDepth--;
    }

    static String Expression(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String getValue;

        String attachStr = " Term";
        outputParser(writer, attachStr);

        String temp = Term(token, writer, reader);

        attachStr = " R";
        outputParser(writer, attachStr);

        if(reader.hasNext(Pattern.quote("('+',^)")) || reader.hasNext(Pattern.quote("('-',^)")))
        {
            token = extractToken(reader);
            getValue = R(token, writer, reader, temp);
        }
        else
        {
            parsetreeDepth++;
            attachStr = " ^";
            outputParser(writer, attachStr);
            parsetreeDepth--;
            getValue = temp;
        }

        parsetreeDepth--;
        return getValue;
    }

    static String Term(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String getValue;

        String attachStr = " Factor";
        outputParser(writer, attachStr);

        String temp = Factor(token, writer, reader);

        attachStr = " R_";
        outputParser(writer, attachStr);

        if(reader.hasNext(Pattern.quote("('*',^)")) || reader.hasNext(Pattern.quote("('/',^)")))
        {
            token = extractToken(reader);
            getValue = R_(token, writer, reader, temp);
        }
        else
        {
            parsetreeDepth++;
            attachStr = " ^";
            outputParser(writer, attachStr);
            parsetreeDepth--;
            getValue = temp;
        }

        parsetreeDepth--;
        return getValue;
    }

    static String R(String[] token, FileWriter writer, Scanner reader, String tacVar) throws IOException
    {
        parsetreeDepth++;
        String getValue = "";
        String attachStr, tacStr;

        String tempTac = generateTempVar();
        String type = getTempVarDataType();
        if(type.length() > 1)
        {
            writeToTranslatorSymbolTable(tempTac, type, "");
        }
        else
        {
            writeToTranslatorSymbolTable(tempTac, checkTempVarDataType(tacVar), "");
        }
        getValue = tempTac;
        tacStr = tempTac + " = " + tacVar;

        if(token[0].contains("'+'"))
        {
            attachStr = " +";
            outputParser(writer, attachStr);
            tacStr = tacStr + " + ";
        }
        else if(token[0].contains("'-'"))
        {
            attachStr = " -";
            outputParser(writer, attachStr);
            tacStr = tacStr + " - ";
        }
        else
        {
            SyntaxError();
        }

        attachStr = " Term";
        outputParser(writer, attachStr);
        token = extractToken(reader);

        String temp = Term(token, writer, reader);
        tacStr = tacStr + temp;
        outputTac(tacStr);

        attachStr = " R";
        outputParser(writer, attachStr);

        if(reader.hasNext(Pattern.quote("('+',^)")) || reader.hasNext(Pattern.quote("('-',^)")))
        {
            token = extractToken(reader);
            getValue = R(token, writer, reader, tempTac);
        }
        else
        {
            parsetreeDepth++;
            attachStr = " ^";
            outputParser(writer, attachStr);
            parsetreeDepth--;
        }

        parsetreeDepth--;
        return getValue;
    }

    static String Factor(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;
        String getValue = "";

        if(token[0].equals("ID"))
        {
            checkID(token[1]);
            attachStr = " ID("+token[1]+")";
            outputParser(writer, attachStr);
            getValue = token[1];
        }
        else if(token[0].equals("NUM"))
        {
            attachStr = " NUM("+token[1]+")";
            outputParser(writer, attachStr);
            getValue = token[1];
        }
        else if(token[0].equals("'('"))
        {
            attachStr = " (";
            outputParser(writer, attachStr);

            token = extractToken(reader);
            attachStr = " Expression";
            outputParser(writer, attachStr);
            getValue = Expression(token, writer, reader);

            token = extractToken(reader);
            if(token[0].equals("')'"))
            {
                attachStr = " )";
                outputParser(writer, attachStr);
            }
            else
            {
                SyntaxError();
            }
        }
        else
        {
            SyntaxError();
        }

        parsetreeDepth--;
        return getValue;
    }

    static String R_(String[] token, FileWriter writer, Scanner reader, String tacVar) throws IOException
    {
        parsetreeDepth++;
        String getValue = "";
        String attachStr, tacStr;

        String tempTac = generateTempVar();
        String type = getTempVarDataType();
        if(type.length() > 1)
        {
            writeToTranslatorSymbolTable(tempTac, type, "");
        }
        else
        {
            writeToTranslatorSymbolTable(tempTac, checkTempVarDataType(tacVar), "");
        }
        getValue = tempTac;
        tacStr = tempTac + " = " + tacVar;

        if(token[0].contains("'*'"))
        {
            attachStr = " *";
            outputParser(writer, attachStr);
            tacStr = tacStr + " * ";
        }
        else if(token[0].contains("'/'"))
        {
            attachStr = " /";
            outputParser(writer, attachStr);
            tacStr = tacStr + " / ";
        }
        else
        {
            SyntaxError();
        }

        attachStr = " Factor";
        outputParser(writer, attachStr);
        token = extractToken(reader);

        String temp = Factor(token, writer, reader);
        tacStr = tacStr + temp;
        outputTac(tacStr);

        attachStr = " R_";
        outputParser(writer, attachStr);

        if(reader.hasNext(Pattern.quote("('*',^)")) || reader.hasNext(Pattern.quote("('/',^)")))
        {
            token = extractToken(reader);
            getValue = R(token, writer, reader, tempTac);
        }
        else
        {
            parsetreeDepth++;
            attachStr = " ^";
            outputParser(writer, attachStr);
            parsetreeDepth--;
        }

        parsetreeDepth--;
        return getValue;
    }

    static String OutputOptions(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;
        String getValue = "";

        token = extractToken(reader);

        if(token[0].equals("STR"))
        {
            attachStr = " STR("+token[1]+")";
            outputParser(writer, attachStr);
            getValue = token[1];

            String temp = generateTempVar();
            String tacStr = temp + " = " + token[1];
            outputTac(tacStr);
            writeToTranslatorSymbolTable(temp, "STR", token[1]);
        }
        else if(token[0].equals("LIT"))
        {
            attachStr = " LIT("+token[1]+")";
            outputParser(writer, attachStr);
            getValue = token[1];
        }
        else if(token[0].equals("ID"))
        {
            if (reader.hasNext(Pattern.quote("('+',^)")) || reader.hasNext(Pattern.quote("('-',^)")) || reader.hasNext(Pattern.quote("('*',^)")) || reader.hasNext(Pattern.quote("('/',^)")))
            {
                attachStr = " Expression";
                outputParser(writer, attachStr);
                getValue = Expression(token, writer, reader);

            }
            else
            {
                checkID(token[1]);
                attachStr = " ID(" + token[1] + ")";
                outputParser(writer, attachStr);
                getValue = token[1];
            }
        }
        else if(token[0].equals("NUM"))
        {
            if (reader.hasNext(Pattern.quote("('+',^)")) || reader.hasNext(Pattern.quote("('-',^)")) || reader.hasNext(Pattern.quote("('*',^)")) || reader.hasNext(Pattern.quote("('/',^)")))
            {
                attachStr = " Expression";
                outputParser(writer, attachStr);
                getValue = Expression(token, writer, reader);

            }
            else
            {
                attachStr = " NUM(" + token[1] + ")";
                outputParser(writer, attachStr);
                getValue = token[1];
            }
        }
        else if(token[0].equals("'('"))
        {
            attachStr = " Expression";
            outputParser(writer, attachStr);
            getValue = Expression(token, writer, reader);
        }
        else
        {
            attachStr = " ^";
            outputParser(writer, attachStr);
        }

        parsetreeDepth--;
        return getValue;
    }

    static void relOp(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr = "relOp("+token[1]+")";
        outputParser(writer, attachStr);
        parsetreeDepth--;
    }

    static String Condition(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;
        String getValue = "";
        String getString;

        if(token[0].equals("ID") || token[0].equals("NUM") || token[0].equals("'('"))
        {
            attachStr = " Expression";
            outputParser(writer, attachStr);
            getValue = Expression(token, writer, reader);
        }
        else
        {
            SyntaxError();
        }

        getString = getValue;
        token = extractToken(reader);
        if(token[0].equals("REL_OP"))
        {
            attachStr = " relOp";
            outputParser(writer, attachStr);
            relOp(token, writer, reader);
            getString = getString+" "+token[1];

            token = extractToken(reader);
            if(token[0].equals("ID") || token[0].equals("NUM") || token[0].equals("'('"))
            {
                attachStr = " Expression";
                outputParser(writer, attachStr);
                getValue = Expression(token, writer, reader);
                getString = getString+" "+getValue;
            }
            else
            {
                SyntaxError();
            }
        }
        else
        {
            SyntaxError();
        }

        parsetreeDepth--;
        return getString;
    }

    static void INPUT_OP(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr = " ->";
        outputParser(writer, attachStr);
        parsetreeDepth--;
    }

    static void inputDelimiter(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        if(token[0].equals("';'"))
        {
            attachStr = " ;";
            outputParser(writer, attachStr);
        }
        else if(token[0].equals("','"))
        {
            attachStr = " ,";
            outputParser(writer, attachStr);

            token = extractToken(reader);
            attachStr = " nextInput";
            outputParser(writer, attachStr);
            nextInput(token, writer, reader);
        }
        else
        {
            SyntaxError();
        }

        parsetreeDepth--;
    }

    static void nextInput(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr, tacStr;

        if(token[0].equals("ID"))
        {
            checkID(token[1]);
            attachStr = " ID("+token[1]+")";
            outputParser(writer, attachStr);
            tacStr = "in "+token[1];
            outputTac(tacStr);
        }
        else
        {
            SyntaxError();
        }

        token = extractToken(reader);
        attachStr = " inputDelimiter";
        outputParser(writer, attachStr);
        inputDelimiter(token, writer, reader);

        parsetreeDepth--;
    }

    static void IncOp(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr, tacStr;

        if(token[0].equals("ID"))
        {
            attachStr = " ID("+token[1]+")";
            outputParser(writer, attachStr);

            String tempTac = generateTempVar();
            String type = getTempVarDataType();
            if(type.length() > 1)
            {
                writeToTranslatorSymbolTable(tempTac, type, "");
            }
            else
            {
                writeToTranslatorSymbolTable(tempTac, checkTempVarDataType(token[1]), "");
            }
            tacStr = tempTac + " = " + token[1] + " + 1";
            String temp = token[1];

            token = extractToken(reader);
            if(token[0].equals("'+'"))
            {
                attachStr = " +";
                outputParser(writer, attachStr);

                token = extractToken(reader);
                if(token[0].equals("'+'")) {
                    attachStr = " +";
                    outputParser(writer, attachStr);
                }
                else
                {
                    SyntaxError();
                }

                outputTac(tacStr);
                tacStr = temp + " = " + tempTac;
                outputTac(tacStr);

                token = extractToken(reader);
                if(token[0].equals("';'"))
                {
                    attachStr = " ;";
                    outputParser(writer, attachStr);
                }
                else
                {
                    SyntaxError();
                }
            }
            else
            {
                SyntaxError();
            }
        }

        parsetreeDepth--;
    }

    static void DecOp(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr, tacStr;

        if(token[0].equals("ID"))
        {
            attachStr = " ID("+token[1]+")";
            outputParser(writer, attachStr);

            String tempTac = generateTempVar();
            String type = getTempVarDataType();
            if(type.length() > 1)
            {
                writeToTranslatorSymbolTable(tempTac, type, "");
            }
            else
            {
                writeToTranslatorSymbolTable(tempTac, checkTempVarDataType(token[1]), "");
            }
            tacStr = tempTac + " = " + token[1] + " - 1";
            String temp = token[1];

            token = extractToken(reader);
            if(token[0].equals("'-'"))
            {
                attachStr = " -";
                outputParser(writer, attachStr);

                token = extractToken(reader);
                if(token[0].equals("'-'")) {
                    attachStr = " -";
                    outputParser(writer, attachStr);
                }
                else
                {
                    SyntaxError();
                }

                outputTac(tacStr);
                tacStr = temp + " = " + tempTac;
                outputTac(tacStr);

                token = extractToken(reader);
                if(token[0].equals("';'"))
                {
                    attachStr = " ;";
                    outputParser(writer, attachStr);
                }
                else
                {
                    SyntaxError();
                }
            }
            else
            {
                SyntaxError();
            }
        }

        parsetreeDepth--;
    }

    static void AssignmentStatement(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr, tacStr;

        if(token[0].equals("ID"))
        {
            checkID(token[1]);
            attachStr = " ID("+token[1]+")";
            outputParser(writer, attachStr);
            String tacStrTemp = token[1];

            token = extractToken(reader);
            if(token[0].equals("'='"))
            {
                attachStr = " =";
                outputParser(writer, attachStr);

                token = extractToken(reader);
                attachStr = " Value";
                outputParser(writer, attachStr);
                String val = Value(token, writer, reader);


                String tempTac = generateTempVar();
                writeToTranslatorSymbolTable(tempTac, checkTempVarDataType(token[1]), val);
                tacStr = tempTac + " = " + val;
                outputTac(tacStr);


                tacStr = tacStrTemp + " = " + tempTac;
                outputTac(tacStr);

                token = extractToken(reader);
                if(token[0].equals("';'"))
                {
                    attachStr = " ;";
                    outputParser(writer, attachStr);
                }
                else
                {
                    SyntaxError();
                }
            }
            else
            {
                SyntaxError();
            }
        }

        parsetreeDepth--;
    }

    static String Value(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;
        String getValue = "";

        if(reader.hasNext(Pattern.quote("('+',^)")) || reader.hasNext(Pattern.quote("('-',^)")) || reader.hasNext(Pattern.quote("('*',^)")) || reader.hasNext(Pattern.quote("('/',^)")) || token[0].equals("'('"))
        {
            attachStr = " Expression";
            outputParser(writer, attachStr);
            getValue = Expression(token, writer, reader);
        }
        else if(token[0].equals("ID"))
        {
            checkID(token[1]);
            attachStr = " ID(" + token[1] + ")";
            outputParser(writer, attachStr);
            getValue = token[1];
        }
        else if(token[0].equals("NUM"))
        {
            attachStr = " NUM(" + token[1] + ")";
            outputParser(writer, attachStr);
            getValue = token[1];
        }
        else if(token[0].equals("LIT"))
        {
            attachStr = " LIT(" + token[1] + ")";
            outputParser(writer, attachStr);
            getValue = token[1];
        }
        else
        {
            SyntaxError();
        }

        parsetreeDepth--;
        return getValue;
    }

    static void DT(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        if(token[0].equals("INT"))
        {
            attachStr = " int";
            outputParser(writer, attachStr);
        }
        else if(token[0].equals("CHAR"))
        {
            attachStr = " char";
            outputParser(writer, attachStr);
        }
        else
        {
            SyntaxError();
        }

        parsetreeDepth--;
    }

    static void optionAssign(String[] token, FileWriter writer, Scanner reader, String type) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        if(token[0].equals("ID"))
        {
            String tacStr;
            String tacStrTemp = token[1];

            token = extractToken(reader);
            if(token[0].equals("'='"))
            {
                attachStr = " =";
                outputParser(writer, attachStr);

                token = extractToken(reader);
                attachStr = " Value";
                outputParser(writer, attachStr);
                String temp = Value(token, writer, reader);

                if(temp.length()>0)
                {
                    writeToTranslatorSymbolTable(tacStrTemp, type, temp);
                    tacStr = type.toLowerCase() + " " + tacStrTemp;
                    outputTac(tacStr);

                    tacStr = tacStrTemp + " = " + temp;
                    outputTac(tacStr);
                }
            }
            else
            {
                SyntaxError();
            }
        }

        parsetreeDepth--;
    }

    static void Variable(String[] token, FileWriter writer, Scanner reader) throws IOException
    {
        parsetreeDepth++;
        String attachStr = " DT", tacStr;
        outputParser(writer, attachStr);
        DT(token, writer, reader);
        String type = token[0];

        token = extractToken(reader);
        if(token[0].equals("':'"))
        {
            attachStr = " :";
            outputParser(writer, attachStr);
        }
        else
        {
            SyntaxError();
        }

        token = extractToken(reader);
        if(token[0].equals("ID"))
        {
            attachStr = " ID("+token[1]+")";
            outputParser(writer, attachStr);
            ////
            writeToSymbolTable(token[1]+"\t"+type);

            /*writeToTranslatorSymbolTable(token[1], type, "");
            tacStr = type.toLowerCase() + " " + token[1];
            outputTac(tacStr);*/
        }
        else
        {
            SyntaxError();
        }

        if(reader.hasNext(Pattern.quote("('=',^)")))
        {
            optionAssign(token, writer, reader, type);
        }
        else
        {
            writeToTranslatorSymbolTable(token[1], type, "");
            tacStr = type.toLowerCase() + " " + token[1];
            outputTac(tacStr);
        }

        token = extractToken(reader);
        attachStr = " VariableDelimiter";
        outputParser(writer, attachStr);
        VariableDelimiter(token, writer, reader, type);

        parsetreeDepth--;
    }

    static void VariableDelimiter(String[] token, FileWriter writer, Scanner reader, String type) throws IOException
    {
        parsetreeDepth++;
        String attachStr;

        if(token[0].equals("';'"))
        {
            attachStr = " ;";
            outputParser(writer, attachStr);
        }
        else if(token[0].equals("','"))
        {
            attachStr = " ,";
            outputParser(writer, attachStr);
            token = extractToken(reader);
            attachStr = " nextVariable";
            outputParser(writer, attachStr);
            nextVariable(token, writer, reader, type);
        }
        else
        {
            SyntaxError();
        }

        parsetreeDepth--;
    }

    static void nextVariable(String[] token, FileWriter writer, Scanner reader, String type) throws IOException
    {
        parsetreeDepth++;
        String attachStr, tacStr;

        if(token[0].equals("ID"))
        {
            attachStr = " ID(" + token[1] + ")";
            outputParser(writer, attachStr);
            ////
            writeToSymbolTable(token[1]+"\t"+type);
            /*writeToTranslatorSymbolTable(token[1], type, "");
            tacStr = type.toLowerCase() + " " + token[1];
            outputTac(tacStr);*/

            if(reader.hasNext(Pattern.quote("('=',^)")))
            {
                optionAssign(token, writer, reader, type);
            }
            else
            {
                writeToTranslatorSymbolTable(token[1], type, "");
                tacStr = type.toLowerCase() + " " + token[1];
                outputTac(tacStr);
            }

            token = extractToken(reader);
            attachStr = " VariableDelimiter";
            outputParser(writer, attachStr);
            VariableDelimiter(token, writer, reader, type);
        }
        else
        {
            SyntaxError();
        }

        parsetreeDepth--;
    }

    static String getTempVarDataType() throws FileNotFoundException
    {
        File file = new File(filename);
        Scanner reader = new Scanner(file);
        String temp = "", dataType = "";

        for(int i=0; i<= line; i++)
        {
            temp = reader.nextLine();
        }
        temp = temp.split("(?=:=)")[0].trim();
        reader.close();

        file = new File("translator-symboltable.txt");
        reader = new Scanner(file);

        while(reader.hasNext())
        {
            String temp2 = reader.nextLine();
            String[] temp3 = temp2.split("\t");

            if(temp3[0].contains(temp))
                dataType = temp3[1];
        }

        return dataType;
    }

    static String checkTempVarDataType(String tacVar) throws FileNotFoundException
    {
        String dataType = "";
        if(tacVar.charAt(0) >= '0' && tacVar.charAt(0) <= '9')
        {
            dataType = "INT";
        }
        else if(tacVar.startsWith("'") && tacVar.endsWith("'"))
        {
            dataType = "CHAR";
        }
        else
        {
            File file = new File("translator-symboltable.txt");
            Scanner reader = new Scanner(file);

            while(reader.hasNext())
            {
                String temp = reader.nextLine();
                String[] temp2 = temp.split("\t");

                if(temp2[0].contains(tacVar))
                    dataType = temp2[1];
            }
        }

        return dataType;
    }

    static int getIDScope(String id) throws IOException
    {
        File file = new File("tokens.txt");
        Scanner reader = new Scanner(file);

        int scope = 1;
        for(int i=0; i<=line;i++)
        {
            for(int j=0; j<Lexer.tokensInLine.get(i);j++)
            {
                String data = reader.nextLine();
                if(data.contentEquals("('{',^)"))
                {
                    scope++;
                }
                else if(data.contentEquals("('}',^)"))
                {
                    scope--;
                }

                if(i == line)
                {
                    if(data.contentEquals("(ID," + id + ")"))
                        break;
                }
            }
        }

        reader.close();
        return scope;
    }

    static void checkID(String id) throws IOException
    {
        File file = new File("parser-symboltable.txt");
        Scanner reader = new Scanner(file);

        int scope = getIDScope(id);
        int scopeCount = 0;
        boolean flag = false;
        String data = null;

        while(reader.hasNext())
        {
            data = reader.nextLine();
            if(data.equals("SCOPE START"))
            {
                scopeCount++;
            }
            else if(data.equals("SCOPE END"))
            {
                scopeCount--;
            }

            String[] temp = data.split("\t");

            if((temp[0].contentEquals(id) && (scope>=scopeCount))) //<--check for ==
                flag = true;
        }

        reader.close();

        if(!flag)
        {
            file = new File(filename);
            reader = new Scanner(file);

            data = "";
            for(int i=0; i<=line; i++)
            {
                data = reader.nextLine();
            }
            System.out.println("Syntax error at line "+(line+1));
            System.out.println((line+1)+"\t"+data);
            System.out.println("ID out of scope or ID not defined");
            reader.close();
            System.exit(0);
        }
    }

    public void startParsingAndTranslating() throws IOException
    {
        File tokenFile = new File("tokens.txt"); //file for input from lexer

        //creating files, writers and readers
        Scanner tokenReader = new Scanner(tokenFile);
        FileWriter parserWriter = new FileWriter("parsetree.txt");
        FileWriter writer = new FileWriter("parser-symboltable.txt");
        writer.close();
        writer = new FileWriter("tac.txt");
        writer.close();
        writer = new FileWriter("translator-symboltable.txt");
        writer.close();

        String[] token; //Token variable

        token = extractToken(tokenReader);

        parserWriter.write("Start\n");
        writeToSymbolTable("SCOPE START");

        Start(token,parserWriter,tokenReader);

        writeToSymbolTable("SCOPE END");

        //closing readers and writers
        tokenReader.close();
        parserWriter.close();

        System.out.println("\nParser execution successful! Check parsetree.txt, parser-symboltable.txt and translator-symboltable.txt files.");
        System.out.println("\nThree Address Code successfully generated! Check tac.txt file.");
    }
}