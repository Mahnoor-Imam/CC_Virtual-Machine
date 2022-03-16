package virtual_machine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class VirtualMachine {

    static List<List> machineCode = new ArrayList<>();  //used to store machine code for each line of three-address code

    public void setMachineCode(List mc)
    {
        machineCode = mc;
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

    static String getDataType(String s) throws FileNotFoundException {
        File file = new File("translator-symboltable.txt");
        Scanner reader = new Scanner(file);
        String dataType = "";

        while(reader.hasNext())
        {
            String temp = reader.nextLine();
            String[] str = temp.split("\t");
            if(str[2].contentEquals(s))
            {
                dataType = str[1];
                break;
            }
        }

        reader.close();
        return dataType;
    }

    static String getInitialValue(String s) throws FileNotFoundException {
        File file = new File("translator-symboltable.txt");
        Scanner reader = new Scanner(file);
        String value = "";

        while(reader.hasNext())
        {
            String temp = reader.nextLine();
            String[] str = temp.split("\t");
            if(str[2].contentEquals(s))
            {
                for(int i=3; i<str.length; i++)
                    value += str[i];
            }
        }

        if(value.startsWith("\"") || value.startsWith("'"))
            value = value.substring(1, value.length()-1);

        reader.close();
        return value;
    }

    static boolean hasInitialValue(String s) throws FileNotFoundException {
        File file = new File("translator-symboltable.txt");
        Scanner reader = new Scanner(file);

        while(reader.hasNext())
        {
            String temp = reader.nextLine();
            String[] str = temp.split("\t");
            if(str[2].contentEquals(s))
            {
                if(str.length > 3)
                    return true;
            }
        }

        reader.close();
        return false;
    }

    static boolean isInStack(List<List> stack, String str)
    {
        for (List<String> temp : stack) {
            if (temp.size() > 0)
                if (temp.get(0).contentEquals(str))
                    return true;
        }
        return false;
    }

    static boolean hasValueInStack(List<List> stack, String str)
    {
        for (List<String> temp : stack) {
            if (temp.size() > 0)
                if (temp.get(0).contentEquals(str) && temp.size() > 2)
                    return true;
        }
        return false;
    }

    static void updateVariable(List<List> stack, String address, String value)
    {
        for(int i=0; i<stack.size(); i++)
        {
            List<String> temp = stack.get(i);
            if(temp.get(0).contentEquals(address))
            {
                if(temp.size() > 2)
                    temp.set(2, value);
                else
                    temp.add(value);

                stack.set(i,temp);
                break;
            }
        }
    }

    static void updateVariableDataType(List<List> stack, String address, String dataType)
    {
        for(int i=0; i<stack.size(); i++)
        {
            List<String> temp = stack.get(i);
            if(temp.get(0).contentEquals(address))
            {
                temp.set(1,dataType);
                stack.set(i,temp);
                break;
            }
        }
    }

    static String getDataTypeFromStack(List<List> stack, String address)
    {
        String dt = "";
        for (List<String> temp : stack) {
            if(temp.size() > 0)
                if(temp.get(0).contentEquals(address))
                    dt = temp.get(1);
        }
        return dt;
    }

    static String getValueFromStack(List<List> stack, String address)
    {
        String val = "";
        for (List<String> temp : stack) {
            if(temp.size() > 0)
                if(temp.get(0).contentEquals(address))
                    val = temp.get(2);
        }
        return val;
    }

    static void executeCode(List<List> stack) throws FileNotFoundException {
        int index = 0;

        while(index < machineCode.size())
        {
            List<String> varData;
            List<String> mcLine = machineCode.get(index);

            if(mcLine.get(0).contentEquals("1"))
            {
                if(!isInStack(stack, mcLine.get(1)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(1));
                    varData.add(getDataType(mcLine.get(1)));
                    if(hasInitialValue(mcLine.get(1)))
                        varData.add(getInitialValue(mcLine.get(1)));

                    stack.add(varData);
                }

                if(!isInStack(stack, mcLine.get(2)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(2));
                    varData.add(getDataType(mcLine.get(2)));
                    if(hasInitialValue(mcLine.get(2)))
                        varData.add(getInitialValue(mcLine.get(2)));

                    stack.add(varData);
                }

                int op1=0, op2=0, value;
                if(hasValueInStack(stack, mcLine.get(1)) && hasValueInStack(stack, mcLine.get(2)))
                {
                    if(getDataTypeFromStack(stack, mcLine.get(1)).equalsIgnoreCase("INT"))
                        op1 = Integer.parseInt(getValueFromStack(stack, mcLine.get(1)));

                    if(getDataTypeFromStack(stack, mcLine.get(2)).equalsIgnoreCase("INT"))
                        op2 = Integer.parseInt(getValueFromStack(stack, mcLine.get(2)));

                    value = op1 + op2;

                    if(!isInStack(stack, mcLine.get(3)))
                    {
                        varData = new ArrayList<>();
                        varData.add(mcLine.get(3));
                        varData.add(getDataType(mcLine.get(3)));
                        stack.add(varData);
                    }

                    if(getDataTypeFromStack(stack, mcLine.get(3)).equalsIgnoreCase("INT"))
                        updateVariable(stack, mcLine.get(3), Integer.toString(value));
                }
                else
                {
                    System.out.println("Variable not initialized at line "+(index+1)+" of three-address code");
                    System.exit(0);
                }
            }
            else if(mcLine.get(0).contentEquals("2"))
            {
                if (!isInStack(stack, mcLine.get(1)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(1));
                    varData.add(getDataType(mcLine.get(1)));
                    if (hasInitialValue(mcLine.get(1)))
                        varData.add(getInitialValue(mcLine.get(1)));

                    stack.add(varData);
                }

                if (!isInStack(stack, mcLine.get(2)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(2));
                    varData.add(getDataType(mcLine.get(2)));
                    if (hasInitialValue(mcLine.get(2)))
                        varData.add(getInitialValue(mcLine.get(2)));

                    stack.add(varData);
                }

                int op1 = 0, op2 = 0, value;
                if (hasValueInStack(stack, mcLine.get(1)) && hasValueInStack(stack, mcLine.get(2)))
                {
                    if (getDataTypeFromStack(stack, mcLine.get(1)).equalsIgnoreCase("INT"))
                        op1 = Integer.parseInt(getValueFromStack(stack, mcLine.get(1)));

                    if (getDataTypeFromStack(stack, mcLine.get(2)).equalsIgnoreCase("INT"))
                        op2 = Integer.parseInt(getValueFromStack(stack, mcLine.get(2)));

                    value = op1 - op2;

                    if (!isInStack(stack, mcLine.get(3)))
                    {
                        varData = new ArrayList<>();
                        varData.add(mcLine.get(3));
                        varData.add(getDataType(mcLine.get(3)));
                        stack.add(varData);
                    }

                    if (getDataTypeFromStack(stack, mcLine.get(3)).equalsIgnoreCase("INT"))
                        updateVariable(stack, mcLine.get(3), Integer.toString(value));
                }
                else
                {
                    System.out.println("Variable not initialized at line " + (index + 1) + " of three-address code");
                    System.exit(0);
                }
            }
            else if(mcLine.get(0).contentEquals("3"))
            {
                if(!isInStack(stack, mcLine.get(1)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(1));
                    varData.add(getDataType(mcLine.get(1)));
                    if(hasInitialValue(mcLine.get(1)))
                        varData.add(getInitialValue(mcLine.get(1)));

                    stack.add(varData);
                }

                if(!isInStack(stack, mcLine.get(2)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(2));
                    varData.add(getDataType(mcLine.get(2)));
                    if(hasInitialValue(mcLine.get(2)))
                        varData.add(getInitialValue(mcLine.get(2)));

                    stack.add(varData);
                }

                int op1=0, op2=0, value;
                if(hasValueInStack(stack, mcLine.get(1)) && hasValueInStack(stack, mcLine.get(2)))
                {
                    if(getDataTypeFromStack(stack, mcLine.get(1)).equalsIgnoreCase("INT"))
                        op1 = Integer.parseInt(getValueFromStack(stack, mcLine.get(1)));

                    if(getDataTypeFromStack(stack, mcLine.get(2)).equalsIgnoreCase("INT"))
                        op2 = Integer.parseInt(getValueFromStack(stack, mcLine.get(2)));

                    value = op1 * op2;

                    if(!isInStack(stack, mcLine.get(3)))
                    {
                        varData = new ArrayList<>();
                        varData.add(mcLine.get(3));
                        varData.add(getDataType(mcLine.get(3)));
                        stack.add(varData);
                    }

                    if(getDataTypeFromStack(stack, mcLine.get(3)).equalsIgnoreCase("INT"))
                        updateVariable(stack, mcLine.get(3), Integer.toString(value));
                }
                else
                {
                    System.out.println("Variable not initialized at line "+(index+1)+" of three-address code");
                    System.exit(0);
                }
            }
            else if(mcLine.get(0).contentEquals("4"))
            {
                if(!isInStack(stack, mcLine.get(1)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(1));
                    varData.add(getDataType(mcLine.get(1)));
                    if(hasInitialValue(mcLine.get(1)))
                        varData.add(getInitialValue(mcLine.get(1)));

                    stack.add(varData);
                }

                if(!isInStack(stack, mcLine.get(2)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(2));
                    varData.add(getDataType(mcLine.get(2)));
                    if(hasInitialValue(mcLine.get(2)))
                        varData.add(getInitialValue(mcLine.get(2)));

                    stack.add(varData);
                }

                int op1=0, op2=0, value;
                if(hasValueInStack(stack, mcLine.get(1)) && hasValueInStack(stack, mcLine.get(2)))
                {
                    if(getDataTypeFromStack(stack, mcLine.get(1)).equalsIgnoreCase("INT"))
                        op1 = Integer.parseInt(getValueFromStack(stack, mcLine.get(1)));

                    if(getDataTypeFromStack(stack, mcLine.get(2)).equalsIgnoreCase("INT"))
                        op2 = Integer.parseInt(getValueFromStack(stack, mcLine.get(2)));

                    value = op1 / op2;

                    if(!isInStack(stack, mcLine.get(3)))
                    {
                        varData = new ArrayList<>();
                        varData.add(mcLine.get(3));
                        varData.add(getDataType(mcLine.get(3)));
                        stack.add(varData);
                    }

                    if(getDataTypeFromStack(stack, mcLine.get(3)).equalsIgnoreCase("INT"))
                        updateVariable(stack, mcLine.get(3), Integer.toString(value));
                }
                else
                {
                    System.out.println("Variable not initialized at line "+(index+1)+" of three-address code");
                    System.exit(0);
                }
            }
            else if(mcLine.get(0).contentEquals("5"))
            {
                if(mcLine.get(2).contentEquals("-1") && mcLine.get(3).contentEquals("-1"))
                {
                    if(!isInStack(stack, mcLine.get(1)))
                    {
                        varData = new ArrayList<>();
                        varData.add(mcLine.get(1));
                        varData.add(getDataType(mcLine.get(1)));
                        if(hasInitialValue(mcLine.get(1)))
                            varData.add(getInitialValue(mcLine.get(1)));

                        stack.add(varData);
                    }
                }
                else
                {
                    if(!isInStack(stack, mcLine.get(1)))
                    {
                        varData = new ArrayList<>();
                        varData.add(mcLine.get(1));
                        varData.add(getDataType(mcLine.get(1)));
                        if(hasInitialValue(mcLine.get(1)))
                            varData.add(getInitialValue(mcLine.get(1)));

                        stack.add(varData);
                    }

                    if(hasValueInStack(stack, mcLine.get(1)))
                    {
                        String val = getValueFromStack(stack, mcLine.get(1));
                        if(getDataTypeFromStack(stack, mcLine.get(2)).equalsIgnoreCase("CHAR") && getDataTypeFromStack(stack, mcLine.get(1)).equalsIgnoreCase("INT"))
                        {
                            int tempInt = Integer.parseInt(val);
                            val = Character.toString((char) tempInt);
                        }
                        else if(getDataTypeFromStack(stack, mcLine.get(2)).equalsIgnoreCase("INT") && getDataTypeFromStack(stack, mcLine.get(1)).equalsIgnoreCase("CHAR"))
                        {
                            char tempChar = val.charAt(0);
                            val = Integer.toString(tempChar);
                        }


                        if(!isInStack(stack, mcLine.get(2)))
                        {
                            varData = new ArrayList<>();
                            varData.add(mcLine.get(2));
                            varData.add(getDataType(mcLine.get(2)));
                            if(hasInitialValue(mcLine.get(2)))
                                varData.add(getInitialValue(mcLine.get(2)));

                            stack.add(varData);
                        }


                        updateVariable(stack, mcLine.get(2), val);
                    }
                    else
                    {
                        System.out.println("Variable not initialized at line "+(index+1)+" of three-address code");
                        System.exit(0);
                    }
                }
            }
            else if(mcLine.get(0).contentEquals("6"))
            {
                if(!isInStack(stack, mcLine.get(1)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(1));
                    varData.add(getDataType(mcLine.get(1)));
                    if(hasInitialValue(mcLine.get(1)))
                        varData.add(getInitialValue(mcLine.get(1)));

                    stack.add(varData);
                }

                if(!isInStack(stack, mcLine.get(2)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(2));
                    varData.add(getDataType(mcLine.get(2)));
                    if(hasInitialValue(mcLine.get(2)))
                        varData.add(getInitialValue(mcLine.get(2)));

                    stack.add(varData);
                }

                int op1=0, op2=0;
                if(hasValueInStack(stack, mcLine.get(1)) && hasValueInStack(stack, mcLine.get(2)))
                {
                    if(getDataTypeFromStack(stack, mcLine.get(1)).equalsIgnoreCase("INT"))
                        op1 = Integer.parseInt(getValueFromStack(stack, mcLine.get(1)));

                    if(getDataTypeFromStack(stack, mcLine.get(2)).equalsIgnoreCase("INT"))
                        op2 = Integer.parseInt(getValueFromStack(stack, mcLine.get(2)));

                    if(op1 < op2)
                        index = Integer.parseInt(mcLine.get(3)) - 2;
                }
                else
                {
                    System.out.println("Variable not initialized at line "+(index+1)+" of three-address code");
                    System.exit(0);
                }
            }
            else if(mcLine.get(0).contentEquals("7"))
            {
                if(!isInStack(stack, mcLine.get(1)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(1));
                    varData.add(getDataType(mcLine.get(1)));
                    if(hasInitialValue(mcLine.get(1)))
                        varData.add(getInitialValue(mcLine.get(1)));

                    stack.add(varData);
                }

                if(!isInStack(stack, mcLine.get(2)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(2));
                    varData.add(getDataType(mcLine.get(2)));
                    if(hasInitialValue(mcLine.get(2)))
                        varData.add(getInitialValue(mcLine.get(2)));

                    stack.add(varData);
                }

                int op1=0, op2=0;
                if(hasValueInStack(stack, mcLine.get(1)) && hasValueInStack(stack, mcLine.get(2)))
                {
                    if(getDataTypeFromStack(stack, mcLine.get(1)).equalsIgnoreCase("INT"))
                        op1 = Integer.parseInt(getValueFromStack(stack, mcLine.get(1)));

                    if(getDataTypeFromStack(stack, mcLine.get(2)).equalsIgnoreCase("INT"))
                        op2 = Integer.parseInt(getValueFromStack(stack, mcLine.get(2)));

                    if(op1 <= op2)
                        index = Integer.parseInt(mcLine.get(3)) - 2;
                }
                else
                {
                    System.out.println("Variable not initialized at line "+(index+1)+" of three-address code");
                    System.exit(0);
                }
            }
            else if(mcLine.get(0).contentEquals("8"))
            {
                if(!isInStack(stack, mcLine.get(1)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(1));
                    varData.add(getDataType(mcLine.get(1)));
                    if(hasInitialValue(mcLine.get(1)))
                        varData.add(getInitialValue(mcLine.get(1)));

                    stack.add(varData);
                }

                if(!isInStack(stack, mcLine.get(2)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(2));
                    varData.add(getDataType(mcLine.get(2)));
                    if(hasInitialValue(mcLine.get(2)))
                        varData.add(getInitialValue(mcLine.get(2)));

                    stack.add(varData);
                }

                int op1=0, op2=0;
                if(hasValueInStack(stack, mcLine.get(1)) && hasValueInStack(stack, mcLine.get(2)))
                {
                    if(getDataTypeFromStack(stack, mcLine.get(1)).equalsIgnoreCase("INT"))
                        op1 = Integer.parseInt(getValueFromStack(stack, mcLine.get(1)));

                    if(getDataTypeFromStack(stack, mcLine.get(2)).equalsIgnoreCase("INT"))
                        op2 = Integer.parseInt(getValueFromStack(stack, mcLine.get(2)));

                    if(op1 > op2)
                        index = Integer.parseInt(mcLine.get(3)) - 2;
                }
                else
                {
                    System.out.println("Variable not initialized at line "+(index+1)+" of three-address code");
                    System.exit(0);
                }
            }
            else if(mcLine.get(0).contentEquals("9"))
            {
                if(!isInStack(stack, mcLine.get(1)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(1));
                    varData.add(getDataType(mcLine.get(1)));
                    if(hasInitialValue(mcLine.get(1)))
                        varData.add(getInitialValue(mcLine.get(1)));

                    stack.add(varData);
                }

                if(!isInStack(stack, mcLine.get(2)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(2));
                    varData.add(getDataType(mcLine.get(2)));
                    if(hasInitialValue(mcLine.get(2)))
                        varData.add(getInitialValue(mcLine.get(2)));

                    stack.add(varData);
                }

                int op1=0, op2=0;
                if(hasValueInStack(stack, mcLine.get(1)) && hasValueInStack(stack, mcLine.get(2)))
                {
                    if(getDataTypeFromStack(stack, mcLine.get(1)).equalsIgnoreCase("INT"))
                        op1 = Integer.parseInt(getValueFromStack(stack, mcLine.get(1)));

                    if(getDataTypeFromStack(stack, mcLine.get(2)).equalsIgnoreCase("INT"))
                        op2 = Integer.parseInt(getValueFromStack(stack, mcLine.get(2)));

                    if(op1 >= op2)
                        index = Integer.parseInt(mcLine.get(3)) - 2;
                }
                else
                {
                    System.out.println("Variable not initialized at line "+(index+1)+" of three-address code");
                    System.exit(0);
                }
            }
            else if(mcLine.get(0).contentEquals("10"))
            {
                if(!isInStack(stack, mcLine.get(1)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(1));
                    varData.add(getDataType(mcLine.get(1)));
                    if(hasInitialValue(mcLine.get(1)))
                        varData.add(getInitialValue(mcLine.get(1)));

                    stack.add(varData);
                }

                if(!isInStack(stack, mcLine.get(2)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(2));
                    varData.add(getDataType(mcLine.get(2)));
                    if(hasInitialValue(mcLine.get(2)))
                        varData.add(getInitialValue(mcLine.get(2)));

                    stack.add(varData);
                }

                int op1=0, op2=0;
                if(hasValueInStack(stack, mcLine.get(1)) && hasValueInStack(stack, mcLine.get(2)))
                {
                    if(getDataTypeFromStack(stack, mcLine.get(1)).equalsIgnoreCase("INT"))
                        op1 = Integer.parseInt(getValueFromStack(stack, mcLine.get(1)));

                    if(getDataTypeFromStack(stack, mcLine.get(2)).equalsIgnoreCase("INT"))
                        op2 = Integer.parseInt(getValueFromStack(stack, mcLine.get(2)));

                    if(op1 == op2)
                        index = Integer.parseInt(mcLine.get(3)) - 2;
                }
                else
                {
                    System.out.println("Variable not initialized at line "+(index+1)+" of three-address code");
                    System.exit(0);
                }
            }
            else if(mcLine.get(0).contentEquals("11"))
            {
                if(!isInStack(stack, mcLine.get(1)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(1));
                    varData.add(getDataType(mcLine.get(1)));
                    if(hasInitialValue(mcLine.get(1)))
                        varData.add(getInitialValue(mcLine.get(1)));

                    stack.add(varData);
                }

                if(!isInStack(stack, mcLine.get(2)))
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(2));
                    varData.add(getDataType(mcLine.get(2)));
                    if(hasInitialValue(mcLine.get(2)))
                        varData.add(getInitialValue(mcLine.get(2)));

                    stack.add(varData);
                }

                int op1=0, op2=0;
                if(hasValueInStack(stack, mcLine.get(1)) && hasValueInStack(stack, mcLine.get(2)))
                {
                    if(getDataTypeFromStack(stack, mcLine.get(1)).equalsIgnoreCase("INT"))
                        op1 = Integer.parseInt(getValueFromStack(stack, mcLine.get(1)));

                    if(getDataTypeFromStack(stack, mcLine.get(2)).equalsIgnoreCase("INT"))
                        op2 = Integer.parseInt(getValueFromStack(stack, mcLine.get(2)));

                    if(op1 != op2)
                        index = Integer.parseInt(mcLine.get(3)) - 2;
                }
                else
                {
                    System.out.println("Variable not initialized at line "+(index+1)+" of three-address code");
                    System.exit(0);
                }
            }
            else if(mcLine.get(0).contentEquals("12"))
            {
                Scanner userInput = new Scanner(System.in);
                String input = "";

                if(getDataTypeFromStack(stack, mcLine.get(1)).equalsIgnoreCase("INT"))
                {
                    int temp = userInput.nextInt();
                    input = Integer.toString(temp);
                }
                else if(getDataTypeFromStack(stack, mcLine.get(1)).equalsIgnoreCase("CHAR"))
                {
                    char temp = userInput.next().charAt(0);
                    input = Character.toString(temp);
                }

                updateVariable(stack, mcLine.get(1), input);
            }
            else if(mcLine.get(0).contentEquals("13"))
            {
                if(hasValueInStack(stack, mcLine.get(1)))
                {
                    if(getValueFromStack(stack, mcLine.get(1)).contentEquals("\\n") || mcLine.get(1).contentEquals("-1"))
                        System.out.println();
                    else
                        System.out.print(getValueFromStack(stack, mcLine.get(1)));
                }
                else if(hasInitialValue(mcLine.get(1)))
                {
                    if(getInitialValue(mcLine.get(1)).contentEquals("\\n") || mcLine.get(1).contentEquals("-1"))
                        System.out.println();
                    else
                        System.out.print(getInitialValue(mcLine.get(1)));
                }
                else
                {
                    System.out.println("Variable not initialized at line "+(index+1)+" of three-address code");
                    System.exit(0);
                }
            }
            else if(mcLine.get(0).contentEquals("14"))
            {
                index = Integer.parseInt(mcLine.get(1)) - 2;
            }
            else if(mcLine.get(0).contentEquals("15"))
            {
                if(isInStack(stack, mcLine.get(1)))
                    updateVariableDataType(stack, mcLine.get(1), "INT");
                else
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(1));
                    varData.add("INT");
                    stack.add(varData);
                }
            }
            else if(mcLine.get(0).contentEquals("16"))
            {
                if(isInStack(stack, mcLine.get(1)))
                    updateVariableDataType(stack, mcLine.get(1), "CHAR");
                else
                {
                    varData = new ArrayList<>();
                    varData.add(mcLine.get(1));
                    varData.add("CHAR");
                    stack.add(varData);
                }
            }

            index++;
        }
    }

    public void startVM() throws FileNotFoundException
    {
        System.out.println("Code Execution in Progress\n");
        List<List> stack = new ArrayList<>();
        executeCode(stack);
        System.out.println("\nCode Execution Successful");
    }
}
