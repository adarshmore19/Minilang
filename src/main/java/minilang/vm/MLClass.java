package minilang.vm;

import minilang.compiler.FunctionInfo;
import java.util.Map;

public record MLClass(String name, Map<String, FunctionInfo> methods) implements Value {
    public FunctionInfo findMethod(String methodName) {
        return methods.get(methodName);
    }

    @Override
    public String toString() {
        return "<class " + name + ">";
    }
}
