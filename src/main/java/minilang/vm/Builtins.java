package minilang.vm;

import java.util.HashMap;
import java.util.Map;

/**
 * Builtins registry for standard library and graphics functions.
 */
public class Builtins {

    public static final int LEN = 0;
    public static final int PUSH = 1;
    public static final int POP = 2;
    public static final int CLOCK = 3;
    public static final int INPUT = 4;
    public static final int STR = 5;
    public static final int INT = 6;
    public static final int FLOAT = 7;
    public static final int TYPE = 8;
    public static final int SQRT = 9;
    public static final int ABS = 10;
    public static final int MIN = 11;
    public static final int MAX = 12;
    public static final int FLOOR = 13;
    public static final int CEIL = 14;
    public static final int ROUND = 15;
    public static final int POW = 16;
    public static final int RANDOM = 17;
    public static final int SIN = 18;
    public static final int COS = 19;

    // Graphics & OpenGL-Style Builtins
    public static final int GL_WINDOW = 20;
    public static final int GL_IS_OPEN = 21;
    public static final int GL_UPDATE = 22;
    public static final int GL_CLEAR = 23;
    public static final int GL_COLOR = 24;
    public static final int GL_RECT = 25;
    public static final int GL_FILL_RECT = 26;
    public static final int GL_CIRCLE = 27;
    public static final int GL_FILL_CIRCLE = 28;
    public static final int GL_LINE = 29;
    public static final int GL_TEXT = 30;
    public static final int GL_BEGIN = 31;
    public static final int GL_VERTEX = 32;
    public static final int GL_END = 33;
    public static final int GL_KEY_DOWN = 34;
    public static final int GL_MOUSE_X = 35;
    public static final int GL_MOUSE_Y = 36;
    public static final int GL_MOUSE_DOWN = 37;
    public static final int GL_CLOSE = 38;

    // Algorithms & Statistics
    public static final int RANGE = 39;
    public static final int SUM = 40;
    public static final int MEAN = 41;
    public static final int MEDIAN = 42;
    public static final int SORT = 43;
    public static final int REVERSE = 44;
    public static final int BINARY_SEARCH = 45;

    // Matplotlib-style Plotting
    public static final int PLOT_LINE = 46;
    public static final int PLOT_SCATTER = 47;
    public static final int PLOT_BAR = 48;
    public static final int PLOT_TITLE = 49;
    public static final int PLOT_X_LABEL = 50;
    public static final int PLOT_Y_LABEL = 51;
    public static final int PLOT_GRID = 52;
    public static final int PLOT_SHOW = 53;
    public static final int PLOT_SAVE = 54;
    public static final int PLOT_CLEAR = 55;

    // Kid-friendly Turtle Graphics
    public static final int TURTLE_INIT = 56;
    public static final int TURTLE_FORWARD = 57;
    public static final int TURTLE_BACKWARD = 58;
    public static final int TURTLE_RIGHT = 59;
    public static final int TURTLE_LEFT = 60;
    public static final int TURTLE_PEN_UP = 61;
    public static final int TURTLE_PEN_DOWN = 62;
    public static final int TURTLE_PEN_COLOR = 63;
    public static final int TURTLE_PEN_SIZE = 64;
    public static final int TURTLE_CIRCLE = 65;
    public static final int TURTLE_CLEAR = 66;
    public static final int TURTLE_SPEED = 67;
    public static final int TURTLE_UPDATE = 68;
    public static final int TURTLE_SAVE = 69;

    // Dictionaries / HashMaps
    public static final int KEYS = 70;
    public static final int VALUES = 71;
    public static final int HAS = 72;

    // File I/O
    public static final int READ_FILE = 73;
    public static final int WRITE_FILE = 74;
    public static final int APPEND_FILE = 75;
    public static final int FILE_EXISTS = 76;
    public static final int DELETE_FILE = 77;

    private static final Map<String, Integer> NAME_TO_ID = new HashMap<>();
    private static final Map<Integer, String> ID_TO_NAME = new HashMap<>();

    static {
        register("len", LEN);
        register("push", PUSH);
        register("pop", POP);
        register("clock", CLOCK);
        register("input", INPUT);
        register("str", STR);
        register("int", INT);
        register("float", FLOAT);
        register("type", TYPE);
        register("sqrt", SQRT);
        register("abs", ABS);
        register("min", MIN);
        register("max", MAX);
        register("floor", FLOOR);
        register("ceil", CEIL);
        register("round", ROUND);
        register("pow", POW);
        register("random", RANDOM);
        register("sin", SIN);
        register("cos", COS);

        // Graphics / OpenGL
        register("glWindow", GL_WINDOW);
        register("glIsOpen", GL_IS_OPEN);
        register("glUpdate", GL_UPDATE);
        register("glClear", GL_CLEAR);
        register("glColor", GL_COLOR);
        register("glRect", GL_RECT);
        register("glFillRect", GL_FILL_RECT);
        register("glCircle", GL_CIRCLE);
        register("glFillCircle", GL_FILL_CIRCLE);
        register("glLine", GL_LINE);
        register("glText", GL_TEXT);
        register("glBegin", GL_BEGIN);
        register("glVertex", GL_VERTEX);
        register("glEnd", GL_END);
        register("glKeyDown", GL_KEY_DOWN);
        register("glMouseX", GL_MOUSE_X);
        register("glMouseY", GL_MOUSE_Y);
        register("glMouseDown", GL_MOUSE_DOWN);
        register("glClose", GL_CLOSE);

        // OpenGL aliases
        register("glInitWindow", GL_WINDOW);
        register("glWindowIsOpen", GL_IS_OPEN);
        register("glPollEvents", GL_UPDATE);
        register("glQuit", GL_CLOSE);

        // Algorithms
        register("range", RANGE);
        register("sum", SUM);
        register("mean", MEAN);
        register("median", MEDIAN);
        register("sort", SORT);
        register("reverse", REVERSE);
        register("binarySearch", BINARY_SEARCH);

        // Plotting
        register("plotLine", PLOT_LINE);
        register("plotScatter", PLOT_SCATTER);
        register("plotBar", PLOT_BAR);
        register("plotTitle", PLOT_TITLE);
        register("plotXLabel", PLOT_X_LABEL);
        register("plotYLabel", PLOT_Y_LABEL);
        register("plotGrid", PLOT_GRID);
        register("plotShow", PLOT_SHOW);
        register("plotSave", PLOT_SAVE);
        register("plotClear", PLOT_CLEAR);

        // Turtle Graphics
        register("turtleInit", TURTLE_INIT);
        register("forward", TURTLE_FORWARD);
        register("fd", TURTLE_FORWARD);
        register("backward", TURTLE_BACKWARD);
        register("bk", TURTLE_BACKWARD);
        register("turnRight", TURTLE_RIGHT);
        register("rt", TURTLE_RIGHT);
        register("turnLeft", TURTLE_LEFT);
        register("lt", TURTLE_LEFT);
        register("penUp", TURTLE_PEN_UP);
        register("pu", TURTLE_PEN_UP);
        register("penDown", TURTLE_PEN_DOWN);
        register("pd", TURTLE_PEN_DOWN);
        register("penColor", TURTLE_PEN_COLOR);
        register("penSize", TURTLE_PEN_SIZE);
        register("turtleCircle", TURTLE_CIRCLE);
        register("turtleClear", TURTLE_CLEAR);
        register("turtleSpeed", TURTLE_SPEED);
        register("turtleUpdate", TURTLE_UPDATE);
        register("turtleShow", TURTLE_UPDATE);
        register("turtleSave", TURTLE_SAVE);

        // Dictionaries / HashMaps
        register("keys", KEYS);
        register("values", VALUES);
        register("has", HAS);

        // File I/O
        register("readFile", READ_FILE);
        register("writeFile", WRITE_FILE);
        register("appendFile", APPEND_FILE);
        register("fileExists", FILE_EXISTS);
        register("deleteFile", DELETE_FILE);
    }

    private static void register(String name, int id) {
        NAME_TO_ID.put(name, id);
        ID_TO_NAME.put(id, name);
    }

    public static Integer getId(String name) {
        return NAME_TO_ID.get(name);
    }

    public static String getName(int id) {
        return ID_TO_NAME.getOrDefault(id, "builtin#" + id);
    }

    public static boolean isBuiltin(String name) {
        return NAME_TO_ID.containsKey(name);
    }
}
