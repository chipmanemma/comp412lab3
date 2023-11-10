public class DepGraphNode {
    private String label; // the label of the type of ILOC operation
    private int ilocCode; // The actual opCode for the node
    private int priority; // The number of cycles this will take plus all the ancestor node's priority
    private List<DepGraphNode> parents; // Parent nodes
    private List<DepGraphNode> children; // Child nodes

    public DepGraphNode(String label, int ilocCode) {
        this.label = label;
        this.ilocCode = ilocCode;
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

    public String getLabel(){
        return this.label;
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
}