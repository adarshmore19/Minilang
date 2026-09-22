package minilang;

import minilang.lexer.Lexer;
import minilang.lexer.Token;
import minilang.lexer.TokenType;
import minilang.parser.Parser;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java minilang.Main <file.ml>");
            return;
        }
        String source = Files.readString(Paths.get(args[0]));
        Lexer lexer = new Lexer(source);
        for (Token t : lexer.tokenize()) {
            System.out.println(t);
        }
    }
}