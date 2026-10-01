package minilang.vm;

import minilang.compiler.FunctionInfo;

public record BoundMethod(MLInstance instance, FunctionInfo method) implements Value {
    @Override
    public String toString() {
        return "<bound method " + method.name() + " of " + instance + ">";
    }
}
