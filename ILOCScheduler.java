public class ILOCScheduler{
    private DepGraph dependenceGraph;
    private IRList ir;
    private int nodeNumCount; 
    private DepGraphNode[] vrToNodes = new ArrayList<>(); //index is vr that is defined
    private Map<DepGraphNode, HashSet<DepGraphNode>> sourceToSink; // key is source value is sink
    private List<DepGraphNode> nodes;
    private List<DepGraphEdge> edges;

    public ILOCScheduler(IRList ir, int maxVR){
        this.ir = ir;
        this.dependenceGraph = new DepGraph();
        this.nodeNumCount = 0;
        this.vrToNodes = new DepGraphNode[maxVR];
        this.sourceToSink = new HashMap<>();
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
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
        DepGraphNode lastLoad = null;
        DepGraphNode lastStore = null;
        DepGraphNode lastOutput = null;
        // Algo: 
        // Create an empty map M => vrToNodes in global
        IRNode currOp = ir.getHead();
        while(currOp != null){
            // Walk block top to bottom
            // At each op
            // create node for o
            int[] opData = currOp.getData();
            DepGraphNode currNode = new DepGraphNode(opData);
            
            //if o defines vri
                //set M(vri) to o
                // Store and output are the only ones to not define a reg (aside from nop)
            if(opData[OpInfoEnum.OP.getValue()] != OpCode.STORE.getValue() && opData[OpInfoEnum.OP.getValue()] != OpCode.OUTPUT.getValue()){
                // Set defined VR index to node that defined it
                vrToNodes[opData[OpInfoEnum.VR3.getValue()]] = currNode;
            }
            // for each vrj used in o, add an edge from o to the node in M(vrj)
            // These have two uses
            if(opData[OpInfoEnum.OP.getValue()] == OpCode.ADD.getValue() || opData[OpInfoEnum.OP.getValue()] == OpCode.SUB.getValue() ||
            opData[OpInfoEnum.OP.getValue()] == OpCode.MULT.getValue() || opData[OpInfoEnum.OP.getValue()] == OpCode.LSHIFT.getValue() || 
            opData[OpInfoEnum.OP.getValue()] == OpCode.RSHIFT.getValue()) {
                if(!sourceToSink.containsKey(currNode)){
                    sourceToSink.put(currNode, new HashSet<DepGraphNode>());
                }
                sourceToSink.get(currNode).add(vrToNodes[opData[OpInfoEnum.VR1.getValue()]]);
                sourceToSink.get(currNode).add(vrToNodes[opData[OpInfoEnum.VR2.getValue()]]);
                edges.add(new DepGraphEdge("Data, vr" + opData[OpInfoEnum.VR1.getValue()], currNode, vrToNodes[opData[OpInfoEnum.VR1.getValue()]]));
                edges.add(new DepGraphEdge("Data, vr" + opData[OpInfoEnum.VR2.getValue()], currNode, vrToNodes[opData[OpInfoEnum.VR2.getValue()]]));
            }   

            // if o is a load, store, or output
                // add serial and conflict edges to other memory ops
            else if(opData[OpInfoEnum.OP.getValue()] == OpCode.STORE.getValue()){
                if(!sourceToSink.containsKey(currNode)){
                    sourceToSink.put(currNode, new HashSet<DepGraphNode>());
                }
                // Two uses add edges
                sourceToSink.get(currNode).add(vrToNodes[opData[OpInfoEnum.VR1.getValue()]]);
                sourceToSink.get(currNode).add(vrToNodes[opData[OpInfoEnum.VR3.getValue()]]);
                edges.add(new DepGraphEdge("Data, vr" + opData[OpInfoEnum.VR1.getValue()], currNode, vrToNodes[opData[OpInfoEnum.VR1.getValue()]]));
                edges.add(new DepGraphEdge("Data, vr" + opData[OpInfoEnum.VR3.getValue()], currNode, vrToNodes[opData[OpInfoEnum.VR3.getValue()]]));
                // After load or output, use serialization edge
                if(lastLoad != null){
                    sourceToSink.get(currNode).add(lastLoad);
                    edges.add(new DepGraphEdge("Serial", currNode, lastLoad));
                }
                if(lastOutput != null){
                    sourceToSink.get(currNode).add(lastOutput);
                    edges.add(new DepGraphEdge("Serial", currNode, lastOutput));
                }
                // After store, use serialization edge
                if(lastStore != null){
                    sourceToSink.get(currNode).add(lastStore);
                    edges.add(new DepGraphEdge("Conflict", currNode, lastStore));
                }
                lastStore = currNode;
            }

            else if(opData[OpInfoEnum.OP.getValue()] == OpCode.OUTPUT.getValue()){
                if(!sourceToSink.containsKey(currNode)){
                    sourceToSink.put(currNode, new HashSet<DepGraphNode>());
                }
                // After store, use a conflict edge
                if(lastStore != null){
                    sourceToSink.get(currNode).add(lastStore);
                    edges.add(new DepGraphEdge("Conflict", currNode, lastStore));
                }
                // After output, use a serialization edge
                if(lastOutput != null){
                    sourceToSink.get(currNode).add(lastOutput);
                    edges.add(new DepGraphEdge("Serial", currNode, lastOutput));
                }
                lastOutput = currNode;
            }
            else if(opData[opInfoEnum.OP.getValue()] == OpCode.LOAD.getValue()){
                if(!sourceToSink.containsKey(currNode)){
                    sourceToSink.put(currNode, new HashSet<DepGraphNode>());
                }
                // After a store, use a conflict edge
                if(lastStore != null){
                    sourceToSink.get(currNode).add(lastStore);
                    edges.add(new DepGraphEdge("Conflict", currNode, lastStore));
                }
                lastLoad = currNode;
            }
        } 
    }

    // Compute priorities

    // Schedule code
}