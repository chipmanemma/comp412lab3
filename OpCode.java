/**
 * Enum for the specific 
 */
public enum OpCode {
    LOAD(0, "load"),
    LOADI(1, "loadI"),
    STORE(2, "store"),
    ADD(3, "add"),
    SUB(4, "sub"),
    MULT(5, "mult"),
    LSHIFT(6, "lshift"),
    RSHIFT(7, "rshift"),
    OUTPUT(8, "output"),
    NOP(9, "nop");

    private final int value;
    private final String label;
    private OpCode(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public static String getLabelFromValue(int value) {
        for(OpCode cat : values()) {
            if (cat.value == value) {
                return cat.label;
            }
        }
        return null;
    }

    public static int getValueFromLabel(String label) {
        switch(label) {
            case("load"):
                return 0;
            case("loadI"):
                return 1;
            case("store"):
                return 2;
            case("add"):
                return 3;
            case("sub"):
                return 4;
            case("mult"):
                return 5;
            case("lshift"):
                return 6;
            case("rshift"):
                return 7;
            case("output"):
                return 8;
            case("nop"):
                return 9;
            default:
                return -1;
        }    
    }
}