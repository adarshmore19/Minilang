package minilang.parser;

import java.util.List;

public record MapLiteral(List<Expr> keys, List<Expr> values) implements Expr {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < keys.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(keys.get(i)).append(": ").append(values.get(i));
        }
        sb.append("}");
        return sb.toString();
    }
}
