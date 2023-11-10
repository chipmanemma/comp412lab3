public class DepGraphEdge{
    private String label; // Label for the type of edge this is
    private DepGraphNode start;
    private DepGraphNode end;
    public DepGraphEdge(String label, DepGraphNode start, DepGraphNode end){
        this.lable = label;
        this.start = start;
        this.end = end;
    }
}