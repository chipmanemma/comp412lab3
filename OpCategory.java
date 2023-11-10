/**
 * Enums for the broad categories of ILOC op codes
 */
public enum OpCategory {
    ERROR(-1, "ERROR"),
    MEMOP(0, "MEMOP"),
    LOADI(1, "LOADI"),
    ARITHOP(2, "ARITHOP"),
    OUTPUT(3, "OUTPUT"),
    NOP(4, "NOP"),
    CONSTANT(5, "CONSTANT"),
    REGISTER(6, "REGISTER"),
    COMMA(7, "COMMA"),
    INTO(8, "INTO"),
    EOF(9, "EOF"),
    EOL(10, "EOL"),
    FILEFAIL(11, "FILEFAIL");

    private final int value;
    private final String label;
    private OpCategory(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public static String getLabelFromValue(int value) {
        for(OpCategory cat : values()) {
            if (cat.value == value) {
                return cat.label;
            }
        }
        return null;
    }
}
