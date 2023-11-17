import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class ILOCScheduler{
    private IRList ir;
    //private int nodeNumCount; 
    private DepGraphNode[] vrToNodes; //index is vr that is defined
    // 0 = data, 1 = serial, 2 = conflict
    // For example {node 1: {(node2, serial), (node 3, conflict)}}
    private Map<DepGraphNode, HashSet<Pair<DepGraphNode, Integer>>> sourceToSink; // key is source value is sink
    private Map<DepGraphNode, HashSet<Pair<DepGraphNode, Integer>>> sinkToSource; // key is sink value is source 
    private Queue<DepGraphNode> f0 = new PriorityQueue<>(); // Can only hold load or store
    private Queue<DepGraphNode> f1 = new PriorityQueue<>(); // Can only hold mult
    private Queue<DepGraphNode> both = new PriorityQueue<>(); // Can hold everything else

    public ILOCScheduler(IRList ir, int maxVR){
        this.ir = ir;
        this.vrToNodes = new DepGraphNode[maxVR];
        this.sourceToSink = new HashMap<>();
        this.sinkToSource = new HashMap<>();
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
        List<DepGraphNode> lastLoads = new ArrayList<>();
        List<DepGraphNode> lastOutputs = new ArrayList<>();
        DepGraphNode lastStore = null;
        DepGraphNode lastOutput = null;
        StringBuilder graph = new StringBuilder();
        graph.append("digraph test\n{");
        // Algo: 
        // Create an empty map M => vrToNodes in global
        IRNode currOp = ir.getHead();
        while(currOp != null){
            // Walk block top to bottom
            // At each op
            // create node for o
            int[] opData = currOp.getData();
            DepGraphNode currNode = new DepGraphNode(opData);
            graph.append(currNode.toString() + "\n");
            // Make sure to add node to either source or sink
            sourceToSink.put(currNode, new HashSet<Pair<DepGraphNode, Integer>>());
            sinkToSource.put(currNode, new HashSet<Pair<DepGraphNode, Integer>>());
            
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
                // two different VRs being used
                if (opData[OpInfoEnum.VR1.getValue()] != opData[OpInfoEnum.VR2.getValue()]){
                    // currNode is the use, the value in vrToNodes is the node that defined that use
                    sourceToSink.get(currNode).add(new Pair<>(vrToNodes[opData[OpInfoEnum.VR1.getValue()]],0));
                    sourceToSink.get(currNode).add(new Pair<>(vrToNodes[opData[OpInfoEnum.VR2.getValue()]], 0));
                    sinkToSource.get(vrToNodes[opData[OpInfoEnum.VR1.getValue()]]).add(new Pair<>(currNode, 0));
                    sinkToSource.get(vrToNodes[opData[OpInfoEnum.VR2.getValue()]]).add(new Pair<>(currNode, 0));
                    currNode.incrementOut();
                    currNode.incrementOut();
                    vrToNodes[opData[OpInfoEnum.VR1.getValue()]].incrementIn();
                    vrToNodes[opData[OpInfoEnum.VR2.getValue()]].incrementIn();
                    graph.append(currNode.getLine() + " -> " + vrToNodes[opData[OpInfoEnum.VR1.getValue()]].getLine() + "[label=\"Data, " + opData[OpInfoEnum.VR1.getValue()] + "\"];");
                    graph.append(currNode.getLine() + " -> " + vrToNodes[opData[OpInfoEnum.VR2.getValue()]].getLine() + "[label=\"Data, " + opData[OpInfoEnum.VR2.getValue()] + "\"];");
                }
                // only one VR is really used
                else{
                    // currNode is the use, the value in vrToNodes is the node that defined that use
                    sourceToSink.get(currNode).add(new Pair<>(vrToNodes[opData[OpInfoEnum.VR1.getValue()]],0));
                    sinkToSource.get(vrToNodes[opData[OpInfoEnum.VR1.getValue()]]).add(new Pair<>(currNode, 0));
                    currNode.incrementOut();
                    vrToNodes[opData[OpInfoEnum.VR1.getValue()]].incrementIn();
                    graph.append(currNode.getLine() + " -> " + vrToNodes[opData[OpInfoEnum.VR1.getValue()]].getLine() + "[label=\"Data, " + opData[OpInfoEnum.VR1.getValue()] + "\"];");
                }
            }   

            // if o is a load, store, or output
            // add serial and conflict edges to other memory ops
            else if(opData[OpInfoEnum.OP.getValue()] == OpCode.STORE.getValue()){
                // Two uses add edges               
                if (opData[OpInfoEnum.VR1.getValue()] != opData[OpInfoEnum.VR3.getValue()]){
                    sourceToSink.get(currNode).add(new Pair<>(vrToNodes[opData[OpInfoEnum.VR1.getValue()]],0));
                    sourceToSink.get(currNode).add(new Pair<>(vrToNodes[opData[OpInfoEnum.VR3.getValue()]], 0));
                    sinkToSource.get(vrToNodes[opData[OpInfoEnum.VR1.getValue()]]).add(new Pair<>(currNode, 0));
                    sinkToSource.get(vrToNodes[opData[OpInfoEnum.VR3.getValue()]]).add(new Pair<>(currNode, 0));
                    currNode.incrementOut();
                    currNode.incrementOut();
                    vrToNodes[opData[OpInfoEnum.VR1.getValue()]].incrementIn();
                    vrToNodes[opData[OpInfoEnum.VR3.getValue()]].incrementIn();
                    graph.append(currNode.getLine() + " -> " + vrToNodes[opData[OpInfoEnum.VR1.getValue()]].getLine() + "[label=\"Data, " + opData[OpInfoEnum.VR1.getValue()] + "\"];");
                    graph.append(currNode.getLine() + " -> " + vrToNodes[opData[OpInfoEnum.VR3.getValue()]].getLine() + "[label=\"Data, " + opData[OpInfoEnum.VR3.getValue()] + "\"];");
                }
                // only one VR is really used
                else{
                    sourceToSink.get(currNode).add(new Pair<>(vrToNodes[opData[OpInfoEnum.VR1.getValue()]],0));
                    sinkToSource.get(vrToNodes[opData[OpInfoEnum.VR1.getValue()]]).add(new Pair<>(currNode, 0));
                    currNode.incrementOut();
                    vrToNodes[opData[OpInfoEnum.VR1.getValue()]].incrementIn();
                    graph.append(currNode.getLine() + " -> " + vrToNodes[opData[OpInfoEnum.VR1.getValue()]].getLine() + "[label=\"Data, " + opData[OpInfoEnum.VR1.getValue()] + "\"];");
                }
                // After load or output, use serialization edge
                if(lastLoads.size() != 0){
                    for(DepGraphNode node : lastLoads){
                        sourceToSink.get(currNode).add(new Pair<>(node, 1));
                        sinkToSource.get(node).add(new Pair<>(currNode, 1));
                        currNode.incrementOut();
                        node.incrementIn();
                        graph.append(currNode.getLine() + " -> " + node.getLine() + "[label=\"Serial\"];");
                    }   
                }
                if(lastOutputs.size() != 0){
                    for(DepGraphNode node : lastLoads){
                        sourceToSink.get(currNode).add(new Pair<>(node, 1));
                        sinkToSource.get(node).add(new Pair<>(currNode, 1));
                        currNode.incrementOut();
                        node.incrementIn();
                        graph.append(currNode.getLine() + " -> " + node.getLine() + "[label=\"Serial\"];");
                    }   
                }
                // After store, use serialization edge
                if(lastStore != null){
                    sourceToSink.get(currNode).add(new Pair<>(lastStore, 1));
                    sinkToSource.get(lastStore).add(new Pair<>(currNode, 1));
                    currNode.incrementOut();
                    lastStore.incrementIn();
                    graph.append(currNode.getLine() + " -> " + lastStore.getLine() + "[label=\"Serial\"];");
                }
                lastStore = currNode;
                lastLoads.clear();
                lastOutputs.clear();
            }
            // Output
            else if(opData[OpInfoEnum.OP.getValue()] == OpCode.OUTPUT.getValue()){
                // After store, use a conflict edge
                if(lastStore != null){
                    sourceToSink.get(currNode).add(new Pair<>(lastStore, 2));
                    sinkToSource.get(lastStore).add(new Pair<>(currNode, 2));
                    currNode.incrementOut();
                    lastStore.incrementIn();
                    graph.append(currNode.getLine() + " -> " + lastStore.getLine() + "[label=\"Conflict\"];");
                }
                // After output, use a serialization edge
                if(lastOutput != null){
                    sourceToSink.get(currNode).add(new Pair<>(lastOutput, 1));
                    sinkToSource.get(lastOutput).add(new Pair<>(currNode, 1));
                    currNode.incrementOut();
                    lastOutput.incrementIn();
                    graph.append(currNode.getLine() + " -> " + lastOutput.getLine() + "[label=\"Serial\"];");
                }
                lastOutput = currNode;
                lastOutputs.add(currNode);
            }
            // Load
            else if(opData[OpInfoEnum.OP.getValue()] == OpCode.LOAD.getValue()){
                sourceToSink.get(currNode).add(new Pair<>(vrToNodes[opData[OpInfoEnum.VR1.getValue()]],0));
                sinkToSource.get(vrToNodes[opData[OpInfoEnum.VR1.getValue()]]).add(new Pair<>(currNode, 0));
                graph.append(currNode.getLine() + " -> " + vrToNodes[opData[OpInfoEnum.VR1.getValue()]].getLine() + "[label=\"Data, " + opData[OpInfoEnum.VR1.getValue()] + "\"];");
                // After a store, use a conflict edge
                if(lastStore != null){
                    sourceToSink.get(currNode).add(new Pair<>(lastStore, 2));
                    sinkToSource.get(lastOutput).add(new Pair<>(currNode, 2));
                    currNode.incrementOut();
                    lastStore.incrementIn();
                    graph.append(currNode.getLine() + " -> " + lastStore.getLine() + "[label=\"Conflict\"];");
                }
                lastLoads.add(currNode);
            }
            currOp = currOp.getNext();
        } 
        graph.append("}");
        viewGraph(graph);
    }

    // Compute priorities
    // load and store have latency 5
    // mult has letency 3
    // Everything else latency 1
    public void computePriorities(){
        // Find any roots
        Queue<DepGraphNode> visit = new LinkedList<>();
        // Cycle through nodes in Sink to source and find any that have no sources
        for(DepGraphNode node : sinkToSource.keySet()){
            if(sinkToSource.get(node).size() == 0){
                visit.add(node);
                if(node.getOp() == OpCode.LOAD.getValue() || node.getOp() == OpCode.LOAD.getValue()){
                    node.setPriority(5);
                }
                else if(node.getOp() == OpCode.MULT.getValue()){
                    node.setPriority(3);
                }
                else{
                    node.setPriority(1);
                }
            }
        }

        // Bredth first search from node and compute priorities
        while(visit.size() != 0){
            // Get node
            DepGraphNode currNode = visit.poll();
            // Go through children and assign them priorities based on graph
            // If serial, the edge weight will only be 1 otherwise, its the child node's normal latency
            for(Pair<DepGraphNode, Integer> child : sourceToSink.get(currNode)){
                // serial edge
                if(child.getItem2() == 1){
                    // check if current priority is bigger already
                    if(child.getItem1().getPriority() < currNode.getPriority() + 1){
                        child.getItem1().setPriority(currNode.getPriority() + 1);
                    }
                }

                // data or conflic edge
                else{
                    int trialPriority = currNode.getPriority();
                    if(child.getItem1().getOp() == OpCode.STORE.getValue() || child.getItem1().getOp() == OpCode.LOAD.getValue()){
                        trialPriority = trialPriority + 5;
                    }
                    else if(child.getItem1().getOp() == OpCode.MULT.getValue()){
                        trialPriority = trialPriority + 3;
                    }
                    else{
                        trialPriority = trialPriority + 1;
                    }
                    // check if current priority is bigger already
                    if(child.getItem1().getPriority() < trialPriority){
                        child.getItem1().setPriority(trialPriority);
                    }
                }
                child.getItem1().decrementIn();
                if(child.getItem1().getInCount() == 0){
                    visit.add(child.getItem1());
                }
            }
        }
    }

    // Schedule code
    public void schedule(){
        // three priority queues
        // f1 only stores things that have to be in f1
        // f2 only stores things that have to be in f2
        // both stores things that can go in either
        // only one output can happen at a time
        int cycle = 1;
        DepGraphNode inF0 = null;
        DepGraphNode inF1 = null;
        
        // Load in leaves into ready
        for(DepGraphNode node : sourceToSink.keySet()){
            if(sourceToSink.get(node).size() == 0){
                if(node.getOp() == OpCode.STORE.getValue() || node.getOp() == OpCode.LOAD.getValue()){
                        f0.add(node);
                    }
                    else if(node.getOp() == OpCode.MULT.getValue()){
                        f1.add(node);
                    }
                    else{
                        both.add(node);
                    }
                node.setStatus(1);
            }
        }

        while(f0.size() != 0 && f1.size() != 0 && both.size() != 0 && inF0 != null && inF1 != null){
            // pick an operation o for each functional unit move o from ready to active
            // Should be highest priority
            if(inF0 == null){
                DepGraphNode f0Node = f0.poll();
                if(f0Node == null){
                    f0Node = both.poll();
                }
                inF0 = f0Node;
            }

            if(inF1 == null){
                DepGraphNode f1Node = f1.poll();
                if(f1Node == null){
                    f1Node = both.poll();
                }
                inF1 = f1Node;
            }
            printILOCHelper(inF0, inF1);
            cycle = cycle + 1;


            // find each op o in active that retires in this cycle and remove from active
            if(inF0 != null && inF0.getEndCycle() == cycle){
                inF0.setStatus(3);
                inF0 = null;
            }
            if(inF1 != null && inF1.getEndCycle() == cycle){
                inF1.setStatus(3);
                inF1 = null;
            }
            // For each op that depends on o
                // if d is now ready, add d to the ready set
            // For each multi-cycle operation o in Active 
                // check ops that depend on o for early releases
                // add any early releases to Ready
        }
    }
    // Buffered String Writer
    // build up the labels and stuff
    // Create a file with a location
    // write the string builder into the file you create
    // flush the writer and close it
    // This should create a dot file that you can use in graphviz
    public void viewGraph(StringBuilder graphInfo){
        try {
            File graphFile = new File("graphText.dot");
            FileWriter writer = new FileWriter(graphFile);
            writer.write(graphInfo.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printILOCHelper(DepGraphNode f0Node, DepGraphNode f1Node){
        if(f0Node != null && f1Node != null){
            System.out.println("[ " + inF0.getILOCString() + "; " + inF1.getILOCString() + "]");
        }
    }
}