/**
 * Enums for the broad categories of ILOC op codes
 */
public enum OpInfoEnum {
    LINE(0, "Line"),
    OP(1, "Op"),
    SR1(2, "SR1"),
    VR1(3, "VR1"),
    PR1(4, "PR1"),
    NU1(5, "NU1"),
    SR2(6, "SR2"),
    VR2(7, "VR2"),
    PR2(8, "PR2"),
    NU2(9, "NU2"),
    SR3(10, "SR3"),
    VR3(11, "VR3"),
    PR3(12, "PR3"),
    NU3(13, "NU3");

    private final int value;
    private final String label;
    private OpInfoEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public static String getLabelFromValue(int value) {
        for(OpInfoEnum cat : values()) {
            if (cat.value == value) {
                return cat.label;
            }
        }
        return null;
    }
}

