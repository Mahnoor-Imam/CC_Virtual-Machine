package virtual_machine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TacToMachineCodeConverter {

    static int t = 1;                                   //used to track number for temporary variable generation
    static int relativeAddress = 0;                     //used to track relative address for translator's symbol table
    static List<List> machineCode = new ArrayList<>();  //used to store machine code for each line of three-address code

    public void setTempVarCount(int count)
    {
        t = count;
    }

    public void setRelativeAddress(int addr)
    {
        relativeAddress = addr;
    }

    public List getMachineCode()
    {
        return machineCode;
    }

    static String generateTempVar()
    {
        String tempVar = "t"+t;
        t++;
        return tempVar;
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

    static void addMachineCode(String opCode, String op1, String op2, String op3)
    {
        List<String> mc = new ArrayList<>();
        mc.add(opCode);
        mc.add(op1);
        mc.add(op2);
        mc.add(op3);

        machineCode.add(mc);
    }

    static String getRelativeAddress(String var) throws FileNotFoundException {
        File file = new File("translator-symboltable.txt");
        Scanner reader = new Scanner(file);
        String address = "";

        while(reader.hasNext())
        {
            String temp = reader.nextLine();
            String[] str = temp.split("\t");
            if(str[0].contentEquals(var))
            {
                address = str[2];
                break;
            }
        }

        reader.close();
        return address;
    }

    static boolean isNumericConstant(String s)
    {
        if(s.charAt(0) >= '0' && s.charAt(0) <= '9')
            return true;
        return false;
    }

    static boolean isLiteralConstant(String s)
    {
        if(s.startsWith("'") && s.endsWith("'"))
            return true;
        return false;
    }

    static String getOperandAddress(String var) throws IOException {
        String address = "";

        if(isNumericConstant(var))
        {
            String tempVar = generateTempVar();
            writeToTranslatorSymbolTable(tempVar, "INT", var);
            address = getRelativeAddress(tempVar);
        }
        else if(isLiteralConstant(var))
        {
            String tempVar = generateTempVar();
            writeToTranslatorSymbolTable(tempVar, "CHAR", var);
            address = getRelativeAddress(tempVar);
        }
        else
            address = getRelativeAddress(var);

        return address;
    }

    static String getOpCodeForArithmeticOperator(String s)
    {
        if(s.contentEquals("+"))
            return "1";
        else if(s.contentEquals("-"))
            return "2";
        else if(s.contentEquals("*"))
            return "3";
        else if(s.contentEquals("/"))
            return "4";
        else
            return "";
    }

    static String getOpCodeForRelationalOperator(String s)
    {
        if(s.contentEquals("LT"))
            return "6";
        else if(s.contentEquals("LE"))
            return "7";
        else if(s.contentEquals("GT"))
            return "8";
        else if(s.contentEquals("GE"))
            return "9";
        else if(s.contentEquals("EQ"))
            return "10";
        else if(s.contentEquals("NE"))
            return "11";
        else
            return "";
    }

    static void tacToMachineCodeConversion() throws IOException {
        File file = new File("tac.txt");
        Scanner reader = new Scanner(file);
        FileWriter mcWriter = new FileWriter("machine-code.txt");

        int counter = 1;    //to track line numbers of tac instructions
        while(reader.hasNext())
        {
            String opCode = "-1", op1 = "-1", op2 = "-1", op3 = "-1";   //default values

            String temp = reader.nextLine();
            temp = temp.substring(Integer.toString(counter).length(), temp.length());
            temp = temp.trim();

            String[] str = temp.split(" ");

            //variable declaration statements
            if(str[0].equalsIgnoreCase("INT") || str[0].equalsIgnoreCase("CHAR"))
            {
                if(str[0].equalsIgnoreCase("INT"))
                    opCode = "15";
                else
                    opCode = "16";

                op1 = getRelativeAddress(str[1]);
                mcWriter.write(opCode+" "+op1+" "+op2+" "+op3+"\n");
                addMachineCode(opCode,op1,op2,op3);
            }
            //goto statements
            else if(str[0].equalsIgnoreCase("goto"))
            {
                opCode = "14";
                op1 = str[1];
                mcWriter.write(opCode+" "+op1+" "+op2+" "+op3+"\n");
                addMachineCode(opCode,op1,op2,op3);
            }
            //input statements
            else if(str[0].equalsIgnoreCase("in"))
            {
                opCode = "12";
                op1 = getRelativeAddress(str[1]);
                mcWriter.write(opCode+" "+op1+" "+op2+" "+op3+"\n");
                addMachineCode(opCode,op1,op2,op3);
            }
            //output statements
            else if(str[0].equalsIgnoreCase("out"))
            {
                opCode = "13";
                if(isNumericConstant(str[1]) || isLiteralConstant(str[1]))
                    op1 = getOperandAddress(str[1]);
                else
                    op1 = getRelativeAddress(str[1]);
                mcWriter.write(opCode+" "+op1+" "+op2+" "+op3+"\n");
                addMachineCode(opCode,op1,op2,op3);
            }
            //if statements
            else if(str[0].equalsIgnoreCase("if"))
            {
                opCode = getOpCodeForRelationalOperator(str[2]);
                op1 = getOperandAddress(str[1].substring(1,str[1].length())); //ignoring the ( in condition
                op2 = getOperandAddress(str[3].substring(0,str[3].length()-1)); //ignoring the ) in condition
                op3 = str[str.length - 1];

                mcWriter.write(opCode+" "+op1+" "+op2+" "+op3+"\n");
                addMachineCode(opCode,op1,op2,op3);
            }
            //Assignment statements and arithmetic operations
            else if(str.length >= 3)
            {
                if(str[1].contentEquals("="))
                {
                    if(str[2].startsWith("\"") || str[2].startsWith("'"))
                    {
                        opCode = "5";
                        op1 = getRelativeAddress(str[0]);
                    }
                    else
                    {
                        if(str.length > 3)
                        {
                            if(str[3].contentEquals("+") || str[3].contentEquals("-") || str[3].contentEquals("*") || str[3].contentEquals("/"))
                            {
                                opCode = getOpCodeForArithmeticOperator(str[3]);
                                op1 = getOperandAddress(str[2]);
                                op2 = getOperandAddress(str[4]);
                                op3 = getRelativeAddress(str[0]);
                            }
                        }
                        else
                        {
                            opCode = "5";
                            op1 = getOperandAddress(str[2]);
                            op2 = getRelativeAddress(str[0]);
                        }
                    }

                    mcWriter.write(opCode+" "+op1+" "+op2+" "+op3+"\n");
                    addMachineCode(opCode,op1,op2,op3);
                }
            }
            counter++;
        }
        mcWriter.close();
    }

    public void startConversion() throws IOException
    {
        tacToMachineCodeConversion();
    }
}
