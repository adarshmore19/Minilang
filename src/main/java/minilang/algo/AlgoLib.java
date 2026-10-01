package minilang.algo;

import minilang.vm.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fast algorithms and statistics library for MiniLang.
 */
public class AlgoLib {

    public static ArrayValue range(double stop) {
        return range(0, stop, 1);
    }

    public static ArrayValue range(double start, double stop) {
        return range(start, stop, 1);
    }

    public static ArrayValue range(double start, double stop, double step) {
        List<Value> list = new ArrayList<>();
        if (step == 0) return new ArrayValue(list);

        if (step > 0) {
            for (double i = start; i < stop; i += step) {
                if (i == (long) i) list.add(new IntValue((int) i));
                else list.add(new FloatValue(i));
            }
        } else {
            for (double i = start; i > stop; i += step) {
                if (i == (long) i) list.add(new IntValue((int) i));
                else list.add(new FloatValue(i));
            }
        }
        return new ArrayValue(list);
    }

    public static Value sum(ArrayValue arr) {
        double s = 0.0;
        boolean hasFloat = false;
        for (Value v : arr.elements()) {
            if (v instanceof IntValue iv) {
                s += iv.value();
            } else if (v instanceof FloatValue fv) {
                s += fv.value();
                hasFloat = true;
            }
        }
        if (hasFloat) return new FloatValue(s);
        return new IntValue((int) s);
    }

    public static FloatValue mean(ArrayValue arr) {
        if (arr.size() == 0) return new FloatValue(0.0);
        double s = 0.0;
        for (Value v : arr.elements()) {
            if (v instanceof IntValue iv) s += iv.value();
            else if (v instanceof FloatValue fv) s += fv.value();
        }
        return new FloatValue(s / arr.size());
    }

    public static Value median(ArrayValue arr) {
        if (arr.size() == 0) return new FloatValue(0.0);
        List<Double> numbers = new ArrayList<>();
        boolean anyFloat = false;
        for (Value v : arr.elements()) {
            if (v instanceof IntValue iv) numbers.add((double) iv.value());
            else if (v instanceof FloatValue fv) {
                numbers.add(fv.value());
                anyFloat = true;
            }
        }
        if (numbers.isEmpty()) return new FloatValue(0.0);
        Collections.sort(numbers);
        int mid = numbers.size() / 2;
        if (numbers.size() % 2 == 1) {
            double val = numbers.get(mid);
            if (!anyFloat && val == (long) val) return new IntValue((int) val);
            return new FloatValue(val);
        } else {
            double val = (numbers.get(mid - 1) + numbers.get(mid)) / 2.0;
            if (!anyFloat && val == (long) val) return new IntValue((int) val);
            return new FloatValue(val);
        }
    }

    public static ArrayValue sort(ArrayValue arr) {
        return sort(arr, true);
    }

    public static ArrayValue sort(ArrayValue arr, boolean ascending) {
        List<Value> copy = new ArrayList<>(arr.elements());
        copy.sort((a, b) -> {
            int cmp;
            if (a instanceof IntValue ia && b instanceof IntValue ib) {
                cmp = Integer.compare(ia.value(), ib.value());
            } else if ((a instanceof IntValue || a instanceof FloatValue) && (b instanceof IntValue || b instanceof FloatValue)) {
                double da = (a instanceof IntValue) ? ((IntValue) a).value() : ((FloatValue) a).value();
                double db = (b instanceof IntValue) ? ((IntValue) b).value() : ((FloatValue) b).value();
                cmp = Double.compare(da, db);
            } else {
                cmp = a.toString().compareTo(b.toString());
            }
            return ascending ? cmp : -cmp;
        });
        return new ArrayValue(copy);
    }

    public static ArrayValue reverse(ArrayValue arr) {
        List<Value> copy = new ArrayList<>(arr.elements());
        Collections.reverse(copy);
        return new ArrayValue(copy);
    }

    public static StringValue reverse(StringValue s) {
        return new StringValue(new StringBuilder(s.value()).reverse().toString());
    }

    public static int binarySearch(ArrayValue arr, Value target) {
        double targetNum = 0.0;
        boolean isNumeric = false;
        if (target instanceof IntValue iv) {
            targetNum = iv.value();
            isNumeric = true;
        } else if (target instanceof FloatValue fv) {
            targetNum = fv.value();
            isNumeric = true;
        }

        int low = 0;
        int high = arr.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            Value midVal = arr.get(mid);

            if (isNumeric) {
                double midNum = (midVal instanceof IntValue) ? ((IntValue) midVal).value() :
                               (midVal instanceof FloatValue) ? ((FloatValue) midVal).value() : 0.0;
                if (midNum < targetNum) low = mid + 1;
                else if (midNum > targetNum) high = mid - 1;
                else return mid;
            } else {
                int cmp = midVal.toString().compareTo(target.toString());
                if (cmp < 0) low = mid + 1;
                else if (cmp > 0) high = mid - 1;
                else return mid;
            }
        }
        return -1;
    }
}
