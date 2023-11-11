public class ILOCScheduler{
    DepGraph dependenceGraph;
    IRList ir;
    List<Integer> lineToNode;
    int nodeNumCount; 
    List<DepGraphNode> nodes = new ArrayList<>(); // index is the 
    public ILOCScheduler(IRList ir){
        this.ir = ir;
        this.dependenceGraph = new DepGraph();
        this.lineToNode = new ArrayList<>(); // index is the node number and the value is the line number
        this.nodeNumCount = 0;
        this.nodes = new ArrayList<>(); // index doesn't really mean anything whoops
    }

    /**
     * Build dependence graph
     * Traverse renamed code from top to bottom
     * If a load comes before a store in the original code there must be a line FROM store to EVERY previous load
     *      That is a serialization edge
     *      One cycle latency is enough
     * If a store comes before a load, the store must write its value to memory before the load reads from memory
     *      This includes a delay if they access the same location
     *      This is a conflict edge
     *      Has a latency equal to the first operation (usually a load or store)
     * 
     */
    public void buildGraph(){
        // Algo: 
        // Create an empty map M
        
        IRNode currOp = ir.getHead();
        while(currOp != null){
            // Walk block top to bottom
            // At each op
            // create node for o
            nodes.add(new DepGraphNode())
            //if o defines vri
                //set M(vri) to o
            // for each vrj used in o, add an edge from o to the node in M(vrj)
            // if o is a load, store, or output
                // add serial and conflict edges to other memory ops
        }
        
    }
    





    // Compute priorities

    // Schedule code
}