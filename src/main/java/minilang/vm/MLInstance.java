package minilang.vm;

import minilang.compiler.FunctionInfo;
import java.util.HashMap;
import java.util.Map;

public class MLInstance implements Value {
    private final MLClass klass;
    private final Map<String, Value> fields = new HashMap<>();

    public MLInstance(MLClass klass) {
        this.klass = klass;
    }

    public MLClass getKlass() {
        return klass;
    }

    public Value get(String name) {
        if (fields.containsKey(name)) {
            return fields.get(name);
        }
        FunctionInfo method = klass.findMethod(name);
        if (method != null) {
            return new BoundMethod(this, method);
        }
        throw new RuntimeError("Undefined property '" + name + "' on instance of " + klass.name(), 0);
    }

    public void set(String name, Value value) {
        fields.put(name, value);
    }

    public Map<String, Value> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return "<" + klass.name() + " instance " + fields + ">";
    }
}
