package minilang;

import minilang.compiler.Compiler;
import minilang.compiler.Program;
import minilang.lexer.Lexer;
import minilang.lexer.Token;
import minilang.parser.Parser;
import minilang.parser.Stmt;
import minilang.repl.Repl;
import minilang.vm.VM;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
            System.out.println("MiniLang 2.0 CLI:");
            System.out.println("  java -jar minilang.jar <script.ml>            Execute a script");
            System.out.println("  java -jar minilang.jar --web [port]           Launch Web Studio IDE (default: 8080)");
            System.out.println("  java -jar minilang.jar --repl                 Interactive REPL shell");
            System.out.println("  java -jar minilang.jar --bytecode <file.ml>   Disassemble compiled bytecode");
            System.out.println("  java -jar minilang.jar --ast <file.ml>        Dump Abstract Syntax Tree");
            System.out.println("  java -jar minilang.jar --tokens <file.ml>     Dump token stream");
            return;
        }

        if (args.length > 0 && (args[0].equals("--web") || args[0].equals("-w") || args[0].equals("--server") || args[0].equals("--studio"))) {
            int port = 8080;
            if (args.length > 1) {
                try {
                    port = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {}
            }
            new minilang.web.WebServer(port).start();
            // Keep main thread alive
            Thread.currentThread().join();
            return;
        }

        if (args.length == 0 || args[0].equals("--repl") || args[0].equals("-i")) {
            new Repl().start();
            return;
        }

        String mode = "";
        String filePath = "";

        if (args.length == 1) {
            filePath = args[0];
        } else {
            mode = args[0];
            filePath = args[1];
        }

        String source = Files.readString(Paths.get(filePath));

        if ("--tokens".equals(mode)) {
            Lexer lexer = new Lexer(source);
            for (Token t : lexer.tokenize()) {
                System.out.println(t);
            }
            return;
        }

        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer);
        List<Stmt> statements = parser.parse();

        if ("--ast".equals(mode)) {
            for (Stmt s : statements) {
                System.out.println(s);
            }
            return;
        }

        Compiler compiler = new Compiler(true);
        Program program = compiler.compile(statements);

        if ("--bytecode".equals(mode)) {
            System.out.print(program.disassemble());
            return;
        }

        VM vm = new VM(program);
        vm.run();
    }
}