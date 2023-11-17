
public class DepGraphNode implements Comparable<DepGraphNode>{
    private int[] nodeInfo;
    private int priority; // The number of cycles this will take plus all the ancestor node's priority
    private int status; //0 = not ready, 1 = ready, 2 = active, 3 = done
    private int inEdges;
    private int outEdges;
    private int endCycle; // The cycle that this node operation should finish up

    public DepGraphNode(int[] nodeInfo) {
        this.nodeInfo = nodeInfo;
        this.priority = 0;
        this.status = 0;
        this.inEdges = 0;
        this.outEdges = 0;
        this.endCycle = -1;
    }

    // Used to order nodes by priority
    public int compareTo(DepGraphNode other){
        if(this.priority < other.getPriority()){
            return -1;
        }
        else if (this.priority > other.getPriority()){
            return 1;
        }
        return 0;
    }

    // Checks equality for purposes of knowing if a node is the same as itself
    public boolean equals(Object obj){
        if(!(obj instanceof DepGraphNode)){
            return false;
        }
        DepGraphNode node = (DepGraphNode)obj;
        if(node.getLine() == this.nodeInfo[OpInfoEnum.LINE.getValue()]){
            return true;
        }
        return false;
    }

    // Used to get the associated ILOC operation
    public int getOp(){
        return this.nodeInfo[OpInfoEnum.OP.getValue()];
    }

    public int getLine(){
        return this.nodeInfo[OpInfoEnum.LINE.getValue()];
    }

    public void setEndCycle(int currCycle) {
        if(this.getOp() == OpCode.STORE.getValue() || this.getOp() == OpCode.LOAD.getValue()){
            this.endCycle = currCycle + 5;
        }
        else if(this.getOp() == OpCode.MULT.getValue()){
            this.endCycle = currCycle + 3;
        }
        else{
            this.endCycle = currCycle + 1;
        }

    }

    public int getEndCycle(){
        return this.endCycle;
    }

    // Increments the node's in count
    public void incrementIn(){
        this.inEdges = inEdges + 1;
    }

    // Increments the node's out count
    public void incrementOut(){
        this.outEdges = outEdges + 1;
    }

    // Indicates a parent has set this node's priority
    // When it reaches zero all parents have had a shot at setting priority
    public void decrementIn(){
        this.inEdges = inEdges - 1;
    }

    // Indicates a node that is relied on has finished
    // might not be necessary?
    public void decrementOut(){
        this.outEdges = outEdges - 1;
    }

    public int getInCount(){
        return this.inEdges;
    }

    public int getOutCount(){
        return this.outEdges;
    }
    // Sets the priority of the node
    public void setPriority(int priority){
        this.priority = priority;
    }

    // Gets the priority of the node
    public int getPriority(){
        return this.priority;
    }

    // Sets the status of the node
    // 0 = not ready, 1 = ready, 2 = active, 3 = done
    public void setStatus(int status){
        this.status = status;
    }

    // Gets the status of the node
    public int getStatus(){
        return this.status;
    }

    public String getILOCString(){
        int opCode = this.nodeInfo[OpInfoEnum.OP.getValue()];
        // load or store
        if (opCode == OpCode.LOAD.getValue() || opCode == OpCode.STORE.getValue()) {
            return OpCode.getLabelFromValue(this.nodeInfo[OpInfoEnum.OP.getValue()]) + " r"+ this.nodeInfo[OpInfoEnum.VR1.getValue()] + " => r" + this.nodeInfo[OpInfoEnum.VR3.getValue()];
        }
        // loadI
        else if (opCode == OpCode.LOADI.getValue()) {
            return OpCode.getLabelFromValue(this.nodeInfo[OpInfoEnum.OP.getValue()]) + " " + this.nodeInfo[OpInfoEnum.SR1.getValue()] + " => r" + this.nodeInfo[OpInfoEnum.VR3.getValue()];
        }
        // Arithops
        else if (opCode >= 3 && opCode <= 7) {
            return OpCode.getLabelFromValue(this.nodeInfo[OpInfoEnum.OP.getValue()]) + " r" + this.nodeInfo[OpInfoEnum.VR1.getValue()] + ", r" + this.nodeInfo[OpInfoEnum.VR2.getValue()] + " => r" + this.nodeInfo[OpInfoEnum.VR3.getValue()];
        }
        // output
        else if (opCode == OpCode.OUTPUT.getValue()) {
            return OpCode.getLabelFromValue(this.nodeInfo[OpInfoEnum.OP.getValue()]) + this.nodeInfo[OpInfoEnum.SR1.getValue()];
        }
        return "";
    }

    public String toString(){
        int opCode = this.nodeInfo[OpInfoEnum.OP.getValue()];
        // load or store
        if (opCode == OpCode.LOAD.getValue() || opCode == OpCode.STORE.getValue()) {
            return this.nodeInfo[OpInfoEnum.LINE.getValue()]  + "[label=\"" + this.nodeInfo[OpInfoEnum.LINE.getValue()] + ": " + OpCode.getLabelFromValue(this.nodeInfo[OpInfoEnum.OP.getValue()]) + " r"+ this.nodeInfo[OpInfoEnum.VR1.getValue()] + " => r" + this.nodeInfo[OpInfoEnum.VR3.getValue()] + "\"];";
        }
        // loadI
        else if (opCode == OpCode.LOADI.getValue()) {
            return this.nodeInfo[OpInfoEnum.LINE.getValue()]  + "[label=\"" + this.nodeInfo[OpInfoEnum.LINE.getValue()] + ": " + OpCode.getLabelFromValue(this.nodeInfo[OpInfoEnum.OP.getValue()]) + " " + this.nodeInfo[OpInfoEnum.SR1.getValue()] + " => r" + this.nodeInfo[OpInfoEnum.VR3.getValue()] + "\"];";
        }
        // Arithops
        else if (opCode >= 3 && opCode <= 7) {
            return this.nodeInfo[OpInfoEnum.LINE.getValue()]  + "[label=\"" + this.nodeInfo[OpInfoEnum.LINE.getValue()] + ": " + OpCode.getLabelFromValue(this.nodeInfo[OpInfoEnum.OP.getValue()]) + " r" + this.nodeInfo[OpInfoEnum.VR1.getValue()] + ", r" + this.nodeInfo[OpInfoEnum.VR2.getValue()] + " => r" + this.nodeInfo[OpInfoEnum.VR3.getValue()] + "\"];";
        }
        // output
        else if (opCode == OpCode.OUTPUT.getValue()) {
            return this.nodeInfo[OpInfoEnum.LINE.getValue()]  + "[label=\"" + this.nodeInfo[OpInfoEnum.LINE.getValue()] + ": " + OpCode.getLabelFromValue(this.nodeInfo[OpInfoEnum.OP.getValue()]) + " " + this.nodeInfo[OpInfoEnum.SR1.getValue()] + "\"];";
        }
        // nop
        else {
            return "";
        }
    }
}