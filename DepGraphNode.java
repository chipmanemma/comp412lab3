public class DepGraphNode {
    private int[] nodeInfo;
    private int priority; // The number of cycles this will take plus all the ancestor node's priority
    private List<DepGraphNode> parents; // Parent nodes
    private List<DepGraphNode> children; // Child nodes

    public DepGraphNode(int[] nodeInfo) {
        this.nodeInfo = nodeInfo;
        this.priority = 0;
        this.parents = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public List<DepGraphNode> getChildren(){
        return this.children;
    }

    public List<DepGraphNode> getParents(){
        return this.parents;
    }

    public int getCode(){
        return this.ilocCode;
    }

    public void setPriority(int priority){
        this.priority = priority;
    }

    public int getPriority(){
        return this.priority;
    }

    public String toString(){
        // load or store
        if (opCode == OpCode.LOAD.getValue() || opCode == OpCode.Store.getValue()) {
            System.out.println(this.nodeInfo[OpInfoEnum.LINE.getValue()] + ": " + this.nodeInfo[OpInfoEnum.OP.getValue()] + " r"+ this.nodeInfo[OpInfoEnum.VR1.getValue()] + " => r" + this.nodeInfo[OpInfoEnum.VR3.getValue()]);
        }
        // loadI
        else if (opCode == OpCode.LOADI.getValue()) {
            System.out.println(this.nodeInfo[OpInfoEnum.LINE.getValue()] + ": " + this.nodeInfo[OpInfoEnum.OP.getValue()] + this.nodeInfo[OpInfoEnum.SR1.getValue()] + " => r" + this.nodeInfo[OpInfoEnum.VR3.getValue()]);
        }
        // Arithops
        else if (opCode >= 3 && opCode <= 7) {
            System.out.println(this.nodeInfo[OpInfoEnum.LINE.getValue()] + ": " + this.nodeInfo[OpInfoEnum.OP.getValue()] + "r" + this.nodeInfo[OpInfoEnum.VR1.getValue()] + ", r" + this.nodeInfo[OpInfoEnum.VR2.getValue()] + " => r" + this.nodeInfo[OpInfoEnum.VR3.getValue()]);
        }
        // output
        else if (opCode == OpCode.OUTPUT.getValue()) {
            System.out.println(this.nodeInfo[OpInfoEnum.LINE.getValue()] + ": " + this.nodeInfo[OpInfoEnum.OP.getValue()] + this.nodeInfo[OpInfoEnum.SR1.getValue()]);
        }
        // nop
        else {
            System.out.print("\n");
        }
    }
}