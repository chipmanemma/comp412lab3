/**
 * A linked list node for Internal representations
 */
public class IRNode {
    private IRNode next;
    private IRNode prev;
    private int[] data; // line  opCode  SR VR PR NU SR VR PR NU SR VR PR NU 

    public IRNode(int line, int opcode, int sr1, int sr2, int sr3) {
        this.next = null;
        this.prev = null;
        this.data = new int[14];
        data[OpInfoEnum.LINE.getValue()] = line;
        data[OpInfoEnum.OP.getValue()] = opcode;
        data[OpInfoEnum.SR1.getValue()] = sr1;
        data[OpInfoEnum.VR1.getValue()] = -1;
        data[OpInfoEnum.PR1.getValue()] = -1;
        data[OpInfoEnum.NU1.getValue()] = -1;
        data[OpInfoEnum.SR2.getValue()] = sr2;
        data[OpInfoEnum.VR2.getValue()] = -1;
        data[OpInfoEnum.PR2.getValue()] = -1;
        data[OpInfoEnum.NU2.getValue()] = -1;
        data[OpInfoEnum.SR3.getValue()] = sr3;
        data[OpInfoEnum.VR3.getValue()] = -1;
        data[OpInfoEnum.PR3.getValue()] = -1;
        data[OpInfoEnum.NU3.getValue()] = -1;
    }

    public void printVRCodeRep() {
        int opCode = this.data[OpInfoEnum.OP.getValue()];
        System.out.print(OpCode.getLabelFromValue(this.data[OpInfoEnum.OP.getValue()]) + " ");
        if (opCode == 0 || opCode == 2) {
            System.out.println("r"+ this.data[OpInfoEnum.VR1.getValue()] + " => r" + this.data[OpInfoEnum.VR3.getValue()]);
        }
        // loadI
        else if (opCode == 1) {
            System.out.println(this.data[OpInfoEnum.SR1.getValue()] + " => r" + this.data[OpInfoEnum.VR3.getValue()]);
        }
        // Arithops
        else if (opCode >= 3 && opCode <= 7) {
            System.out.println("r" + this.data[OpInfoEnum.VR1.getValue()] + ", r" + this.data[OpInfoEnum.VR2.getValue()] + " => r" + this.data[OpInfoEnum.VR3.getValue()]);
        }
        // output
        else if (opCode == 8) {
            System.out.println(this.data[OpInfoEnum.SR1.getValue()]);
        }
        // nop
        else {
            System.out.print("\n");
        }
    }

    public void printPRCodeRep() {
        int opCode = this.data[OpInfoEnum.OP.getValue()];
        System.out.print(OpCode.getLabelFromValue(this.data[OpInfoEnum.OP.getValue()]) + " ");
        if (opCode == 0 || opCode == 2) {
            System.out.println("r"+ this.data[OpInfoEnum.PR1.getValue()] + " => r" + this.data[OpInfoEnum.PR3.getValue()]);
        }
        // loadI
        else if (opCode == 1) {
            System.out.println(this.data[OpInfoEnum.SR1.getValue()] + " => r" + this.data[OpInfoEnum.PR3.getValue()]);
        }
        // Arithops
        else if (opCode >= 3 && opCode <= 7) {
            System.out.println("r" + this.data[OpInfoEnum.PR1.getValue()] + ", r" + this.data[OpInfoEnum.PR2.getValue()] + " => r" + this.data[OpInfoEnum.PR3.getValue()]);
        }
        // output
        else if (opCode == 8) {
            System.out.println(this.data[OpInfoEnum.SR1.getValue()]);
        }
        // nop
        else {
            System.out.print("");
        }
    }


    public void printIRNode() {
        System.out.print("Line: " + this.data[OpInfoEnum.LINE.getValue()] + " OpCode: " + OpCode.getLabelFromValue(this.data[OpInfoEnum.OP.getValue()]) + " SR1: " + this.data[OpInfoEnum.SR1.getValue()]);
        if (this.data[OpInfoEnum.VR1.getValue()] != -1) {
            System.out.print(" VR1: " + this.data[OpInfoEnum.VR1.getValue()]);
        }
        if (this.data[OpInfoEnum.PR1.getValue()] != -1) {
            System.out.print(" PR1: " + this.data[OpInfoEnum.PR1.getValue()]);
        }
        if (this.data[OpInfoEnum.NU1.getValue()] != -1) {
            System.out.print(" NU1: " + this.data[OpInfoEnum.NU1.getValue()]);
        }
        if (this.data[OpInfoEnum.SR2.getValue()] != -1) {
            System.out.print(" SR2: " + this.data[OpInfoEnum.SR2.getValue()]);
        }
        if (this.data[OpInfoEnum.VR2.getValue()] != -1) {
            System.out.print(" VR2: " + this.data[OpInfoEnum.VR2.getValue()]);
        }
        if (this.data[OpInfoEnum.PR2.getValue()] != -1) {
            System.out.print(" PU2: " + this.data[OpInfoEnum.PR2.getValue()]);
        }
        if (this.data[OpInfoEnum.NU2.getValue()] != -1) {
            System.out.print(" NU2: " + this.data[OpInfoEnum.NU2.getValue()]);
        }
        if (this.data[OpInfoEnum.SR3.getValue()] != -1) {
            System.out.print(" SR3: " + this.data[OpInfoEnum.SR3.getValue()]);
        }
        if (this.data[OpInfoEnum.VR3.getValue()] != -1) {
            System.out.print(" VR3: " + this.data[OpInfoEnum.VR3.getValue()]);
        }
        if (this.data[OpInfoEnum.PR3.getValue()] != -1) {
            System.out.print(" PU3: " + this.data[OpInfoEnum.PR3.getValue()]);
        }
        if (this.data[OpInfoEnum.NU3.getValue()] != -1) {
            System.out.print(" NU3: " + this.data[OpInfoEnum.NU3.getValue()]);
        }
        System.out.print("");
    }

    public void printFullIRNode() {
        System.out.println("Line: " + this.data[OpInfoEnum.LINE.getValue()] + " OpCode: " + OpCode.getLabelFromValue(this.data[OpInfoEnum.OP.getValue()]) + " SR1: " + this.data[OpInfoEnum.SR1.getValue()] + 
        " VR1: " + this.data[OpInfoEnum.VR1.getValue()] + " PR1: " + this.data[OpInfoEnum.PR1.getValue()] + " NU1: " + this.data[OpInfoEnum.NU1.getValue()] + " SR2: " + this.data[OpInfoEnum.SR2.getValue()] + 
        " VR2: " + this.data[OpInfoEnum.VR2.getValue()] + " PR2: " + this.data[OpInfoEnum.PR2.getValue()] + " NU2: " + this.data[OpInfoEnum.NU2.getValue()] + " SR3: " + this.data[OpInfoEnum.SR3.getValue()] + 
        " VR3: " + this.data[OpInfoEnum.VR3.getValue()] + " PR3: " + this.data[OpInfoEnum.PR3.getValue()] + " NU3: " + this.data[OpInfoEnum.NU3.getValue()] + "\n");
    }

    public void setNext(IRNode next) {
        this.next = next;
    }
    
    public void setPrev(IRNode prev) {
        this.prev = prev;
    }

    public IRNode getNext() {
        return this.next;
    }
    public IRNode getPrev() {
        return this.prev;
    }
    public int[] getData() {
        return this.data;
    }
    public void setData(int idx, int value) {
        this.data[idx] = value;
    }

}