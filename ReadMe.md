# CC Virtual Machine

The attached project is the fourth and final step of simple compiler construction, that converts three-address code into machine code of a simple custom language and then executes it in a virtual machine.

## About .cc Language

A new yet simple custom made language is taken as an example which uses constructs from some of the most popular languages in use today. The task is to develop a lexical analyzer for this language.  
A brief description of the main features of this language is as follow:

1. **Data types:** int, char
2. **Keywords:**  
    • **Decision statement:** if, elif, else  
    • **Looping statement:** while  
    • **Standart input statement:** input  
    • **Standard output statement:** print (print and remain on same line), println (print and move to next line)  
3. **Arithmetic operators:** + - * /
4. **Relational operators:** < <= > >= == ~=
5. **Comments:**  
    • **Single line comment:** // comment  
    • **Multi  line comment:** /* comment */
6. **Identifier:** a letter followed by any number of letters, digits or the underscore symbol
7. **Numeric constants:** only integers
8. **Literal constants:** only a single letter enclosed in single quotes
9. **Strings:** sequence of characters and white spaces enclosed in double quotes
10. **Parenthesis, Braces, Square Brackets**
11. **Assignment Operator:** =
12. **Input Operators:** ->
13. **Semi-Colon, Colon, Comma**

### Sample Code

``` 
int: num;
char: my_char;
// lets assign variable my_char a value
my_char = 'd';
print("my char contains: ");
println(my_char);
/*
The program here onwards is an iterative algorithm
for fibonacci numbers
*/
println("enter a number");
input -> num;
int: a = 0, b=1, c=0;
println("The fibonacci seq is: ");
println(a);
println(b);
while c <= num:
{
int: temp = a+b;
a = b;
b = temp;
println(temp);
c++;
}
```

## Getting Started

Before execution, the three-address code generated in ```tac.txt``` is converted into machine code produced in ```machine-code.txt```. Since, every instruction in three-address code has at max three operands so, the machine code will look like:
```
opCode | operand1 | operand2 | operand3
eg. 5 0 3 6
```
The opCode determines the type of instruction for which the mapping is given in ```opcodes.txt```. Here, the operands are the relative addresses of variables that can be seen in ```translator-symboltable.txt```. An invalid operand will have a value of -1 and the last valid operand is the destination variable of the instruction.  
After machine code is generated, the Virtual Machine will take it as input and start executing the code given in ```sample_code.cc```.

The following are some test cases and sample output of the code:

#### Wrong Input

sample_code.cc  | Output
-------------   | -------------
//min number entered<br>int: num, num2;<br>println("enter two numbers");<br>input -> num3,num2;<br>if num < num2:<br>{<br>print(num);<br>print(" is less than ");<br>print(num2);<br>}<br>else<br>{<br>print(num2);<br>print(" is less than ");<br>print(num);<br>}  |  Lexer execution successful! Check the generated tokens in tokens.txt<br>Syntax error at line 4<br>4&emsp;&emsp;	input -> num3,num2;<br>ID out of scope or ID not defined

#### Correct Input

sample_code.cc  | Output
-------------   | -------------
//min number entered<br>int: num, num2;<br>println("enter two numbers");<br>input -> num3,num2;<br>if num < num2:<br>{<br>print(num);<br>print(" is less than ");<br>print(num2);<br>}<br>else<br>{<br>print(num2);<br>print(" is less than ");<br>print(num);<br>}  |  Lexer execution successful! Check the generated tokens in tokens.txt<br>Parser execution successful! Check parsetree.txt, parser-symboltable.txt and translator-symboltable.txt files.<br>Three Address Code successfully generated! Check tac.txt file.<br>Code Execution in Progress<br>enter two numbers<br>10<br>20<br>10 is less than 20<br>Code Execution Successful

#### Output of Program Files

sample_code.cc  | translator-symboltable.txt | tac.txt | machine-code.txt
-------------   | ------------- | ------------- | ------------- 
//min number entered<br>int: num, num2;<br>println("enter two numbers");<br>input -> num3,num2;<br>if num < num2:<br>{<br>print(num);<br>print(" is less than ");<br>print(num2);<br>}<br>else<br>{<br>print(num2);<br>print(" is less than ");<br>print(num);<br>}  |  num&emsp;&emsp;INT&emsp;&emsp;0<br>num2&emsp;&emsp;INT&emsp;&emsp;4<br>t1&emsp;&emsp;STR&emsp;&emsp;8&emsp;&emsp;"enter two numbers"<br>t2&emsp;&emsp;STR&emsp;&emsp;25&emsp;&emsp;"\n"<br>t3&emsp;&emsp;STR&emsp;&emsp;27&emsp;&emsp;" is less than "<br>t4&emsp;&emsp;STR&emsp;&emsp;41&emsp;&emsp;" is less than " | 1&emsp;&emsp;int num<br>2&emsp;&emsp;int num2<br>3&emsp;&emsp;t1 = "enter two numbers"<br>4&emsp;&emsp;out t1<br>5&emsp;&emsp;t2 = "\n"<br>6&emsp;&emsp;out t2<br>7&emsp;&emsp;in num<br>8&emsp;&emsp;in num2<br>9&emsp;&emsp;if (num LT num2) goto 11<br>10&emsp;&emsp;goto 16<br>11&emsp;&emsp;out num<br>12&emsp;&emsp;t3 = " is less than "<br>13&emsp;&emsp;out t3<br>14&emsp;&emsp;out num2<br>15&emsp;&emsp;goto 20<br>16&emsp;&emsp;out num2<br>17&emsp;&emsp;t4 = " is less than "<br>18&emsp;&emsp;out t4<br>19&emsp;&emsp;out num | 15 0 -1 -1<br>15 4 -1 -1<br>5 8 -1 -1<br>13 8 -1 -1<br>5 25 -1 -1<br>13 25 -1 -1<br>12 0 -1 -1<br>12 4 -1 -1<br>6 0 4 11<br>14 16 -1 -1<br>13 0 -1 -1<br>5 27 -1 -1<br>13 27 -1 -1<br>13 4 -1 -1<br>14 20 -1 -1<br>13 4 -1 -1<br>5 41 -1 -1<br>13 41 -1 -1<br>13 0 -1 -1

### Before you Start

This program is made and tested in **java version 14 2020-03-17**. So, you should have **Java JDK 14** installed on your system.

## How to Build and Run

In src folder, enter the following commands to compile the code. Necessary binary files will be produced with ```.class``` extention.  
```javac lexer/Lexer.java```  
```javac parser_and_tac_generator/ParserAndTranslator.java```  
```javac virtual_machine/TacToMachineCodeConverter.java```  
```javac virtual_machine/VirtualMachine.java```  
```javac Source.java```  

To run this program, enter the following in src folder:  
```java Source sample_code.cc```