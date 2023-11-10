public class DepGraphNode {
    private String label; // the label of the type of ILOC operation
    private int ilocCode; // The actual opCode for the node
    private int priority; // The number of cycles this will take plus all the ancestor node's priority
    private List<DepGraphNode> parents; // Parent nodes
    private List<DepGraphNode> children; // Child nodes

    public DepGraphNode(String label, int ilocCode, int priority) {
        this.label = label;
        this.ilocCode = ilocCode;
        this.priority = priority;
        this.parents = new ArrayList<>();
        this.children = new ArrayList<>();
    }


}