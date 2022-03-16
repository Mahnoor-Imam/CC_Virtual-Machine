import lexer.Lexer;
import parser_and_tac_generator.ParserAndTranslator;
import virtual_machine.TacToMachineCodeConverter;
import virtual_machine.VirtualMachine;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Source {

    public static void main(String[] args) throws IOException {

        //Lexical Analysis
        Lexer lexer = new Lexer();
        if(!lexer.setFilename(args[0]))
        {
            return;
        }
        lexer.startLexicalAnalysis();
        if(!lexer.success)
        {
            System.exit(1);     //not to proceed further if some error was encountered previously
        }

        //Parsing and Translating
        ParserAndTranslator parserAndTranslator = new ParserAndTranslator();
        parserAndTranslator.setFilename(args[0]);
        parserAndTranslator.startParsingAndTranslating();

        //TAC to Machine Code Conversion
        TacToMachineCodeConverter converter = new TacToMachineCodeConverter();
        converter.setTempVarCount(parserAndTranslator.getTempVarCount());
        converter.setRelativeAddress(parserAndTranslator.getRelativeAddress());
        converter.startConversion();
        List<List> machineCode = new ArrayList<>();
        machineCode = converter.getMachineCode();

        //Executing Code in Virtual Machine
        VirtualMachine vm = new VirtualMachine();
        vm.setMachineCode(machineCode);
        vm.startVM();
    }
}