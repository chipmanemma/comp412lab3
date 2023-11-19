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
import java.util.Set;

public class ILOCScheduler{
    private IRList ir;
    //private int nodeNumCount; 
    private DepGraphNode[] vrToNodes; //index is vr that is defined
    // 0 = data, 1 = serial, 2 = conflict
    // For example {node 1: {(node2, serial), (node 3, conflict)}}
    private Map<DepGraphNode, HashSet<Pair<DepGraphNode, Integer>>> sourceToSink; // key is source value is sink AKA node : nodes it depends on 
    private Map<DepGraphNode, HashSet<Pair<DepGraphNode, Integer>>> sinkToSource; // key is sink value is source AKA node : nodes that depend on it
    private Queue<DepGraphNode> f0; // Can only hold load or store
    private Queue<DepGraphNode> f1; // Can only hold mult
    private Queue<DepGraphNode> both; // Can hold everything else
    private List<DepGraphNode> active; // Holds active nodes
    public ILOCScheduler(IRList ir, int maxVR){
        this.ir = ir;
        this.vrToNodes = new DepGraphNode[maxVR + 1];
        this.sourceToSink = new HashMap<>();
        this.sinkToSource = new HashMap<>();
        this.f0 = new PriorityQueue<>();
        this.f1 = new PriorityQueue<>();
        this.both = new PriorityQueue<>();
        this.active = new ArrayList<>();
    }

    /**
     * Build dependence //graph
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
        //long time = System.nanoTime();
        List<DepGraphNode> lastLoads = new ArrayList<>();
        List<DepGraphNode> lastOutputs = new ArrayList<>();
        DepGraphNode lastStore = null;
        DepGraphNode lastOutput = null;
        //StringBuilder graph = new StringBuilder();
        //graph.append("di//graph test\n{\n");
        // Algo: 
        // Create an empty map M => vrToNodes in global
        IRNode currOp = ir.getHead();
        while(currOp != null){
            // Walk block top to bottom
            // At each op
            // create node for o
            int[] opData = currOp.getData();
            DepGraphNode currNode = new DepGraphNode(opData);
            //graph.append(currNode.getGraphString() + "\n");
            // Make sure to add node to either source or sink
            sourceToSink.put(currNode, new HashSet<Pair<DepGraphNode, Integer>>());
            sinkToSource.put(currNode, new HashSet<Pair<DepGraphNode, Integer>>());
            //System.out.println("On node: " + currNode.toString());
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
                    //graph.append(currNode.getLine() + " -> " + vrToNodes[opData[OpInfoEnum.VR1.getValue()]].getLine() + "[label=\"Data, r" + opData[OpInfoEnum.VR1.getValue()] + "\"];\n");
                    //graph.append(currNode.getLine() + " -> " + vrToNodes[opData[OpInfoEnum.VR2.getValue()]].getLine() + "[label=\"Data, r" + opData[OpInfoEnum.VR2.getValue()] + "\"];\n");
                }
                // only one VR is really used
                else{
                    // currNode is the use, the value in vrToNodes is the node that defined that use
                    sourceToSink.get(currNode).add(new Pair<>(vrToNodes[opData[OpInfoEnum.VR1.getValue()]],0));
                    sinkToSource.get(vrToNodes[opData[OpInfoEnum.VR1.getValue()]]).add(new Pair<>(currNode, 0));
                    currNode.incrementOut();
                    vrToNodes[opData[OpInfoEnum.VR1.getValue()]].incrementIn();
                    //graph.append(currNode.getLine() + " -> " + vrToNodes[opData[OpInfoEnum.VR1.getValue()]].getLine() + "[label=\"Data, r" + opData[OpInfoEnum.VR1.getValue()] + "\"];\n");
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
                    //graph.append(currNode.getLine() + " -> " + vrToNodes[opData[OpInfoEnum.VR1.getValue()]].getLine() + "[label=\"Data, r" + opData[OpInfoEnum.VR1.getValue()] + "\"];\n");
                    //graph.append(currNode.getLine() + " -> " + vrToNodes[opData[OpInfoEnum.VR3.getValue()]].getLine() + "[label=\"Data, r" + opData[OpInfoEnum.VR3.getValue()] + "\"];\n");
                }
                // only one VR is really used
                else{
                    sourceToSink.get(currNode).add(new Pair<>(vrToNodes[opData[OpInfoEnum.VR1.getValue()]],0));
                    sinkToSource.get(vrToNodes[opData[OpInfoEnum.VR1.getValue()]]).add(new Pair<>(currNode, 0));
                    currNode.incrementOut();
                    vrToNodes[opData[OpInfoEnum.VR1.getValue()]].incrementIn();
                    //graph.append(currNode.getLine() + " -> " + vrToNodes[opData[OpInfoEnum.VR1.getValue()]].getLine() + "[label=\"Data, r" + opData[OpInfoEnum.VR1.getValue()] + "\"];\n");
                }
                // After load or output, use serialization edge
                if(lastLoads.size() != 0){
                    for(DepGraphNode node : lastLoads){
                        sourceToSink.get(currNode).add(new Pair<>(node, 1));
                        sinkToSource.get(node).add(new Pair<>(currNode, 1));
                        currNode.incrementOut();
                        node.incrementIn();
                        //graph.append(currNode.getLine() + " -> " + node.getLine() + "[label=\"Serial\"];\n");
                    }   
                }
                if(lastOutputs.size() != 0){
                    for(DepGraphNode node : lastOutputs){
                        sourceToSink.get(currNode).add(new Pair<>(node, 1));
                        sinkToSource.get(node).add(new Pair<>(currNode, 1));
                        currNode.incrementOut();
                        node.incrementIn();
                        //graph.append(currNode.getLine() + " -> " + node.getLine() + "[label=\"Serial\"];\n");
                    }   
                }
                // After store, use serialization edge
                if(lastStore != null){
                    sourceToSink.get(currNode).add(new Pair<>(lastStore, 1));
                    sinkToSource.get(lastStore).add(new Pair<>(currNode, 1));
                    currNode.incrementOut();
                    lastStore.incrementIn();
                    //graph.append(currNode.getLine() + " -> " + lastStore.getLine() + "[label=\"Serial\"];\n");
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
                    //graph.append(currNode.getLine() + " -> " + lastStore.getLine() + "[label=\"Conflict\"];\n");
                }
                // After output, use a serialization edge
                if(lastOutput != null){
                    sourceToSink.get(currNode).add(new Pair<>(lastOutput, 1));
                    sinkToSource.get(lastOutput).add(new Pair<>(currNode, 1));
                    currNode.incrementOut();
                    lastOutput.incrementIn();
                    //graph.append(currNode.getLine() + " -> " + lastOutput.getLine() + "[label=\"Serial\"];\n");
                }
                lastOutput = currNode;
                lastOutputs.add(currNode);
            }
            // Load
            else if(opData[OpInfoEnum.OP.getValue()] == OpCode.LOAD.getValue()){
                sourceToSink.get(currNode).add(new Pair<>(vrToNodes[opData[OpInfoEnum.VR1.getValue()]],0));
                sinkToSource.get(vrToNodes[opData[OpInfoEnum.VR1.getValue()]]).add(new Pair<>(currNode, 0));
                currNode.incrementOut();
                vrToNodes[opData[OpInfoEnum.VR1.getValue()]].incrementIn();
                //graph.append(currNode.getLine() + " -> " + vrToNodes[opData[OpInfoEnum.VR1.getValue()]].getLine() + "[label=\"Data, r" + opData[OpInfoEnum.VR1.getValue()] + "\"];\n");
                // After a store, use a conflict edge
                if(lastStore != null){
                    sourceToSink.get(currNode).add(new Pair<>(lastStore, 2));
                    sinkToSource.get(lastStore).add(new Pair<>(currNode, 2));
                    currNode.incrementOut();
                    lastStore.incrementIn();
                    //graph.append(currNode.getLine() + " -> " + lastStore.getLine() + "[label=\"Conflict\"];\n");
                }
                lastLoads.add(currNode);
            }
            currOp = currOp.getNext();
        } 
        //graph.append("}");
        //viewGraph(graph);
        // Go through all nodes and check if they're a store
        // If they're a store check to see if they have multiple edges to the same node and decrement out count accordingly
        for(DepGraphNode node : sourceToSink.keySet()){
            if(node.getOp() == OpCode.STORE.getValue()){
                checkExistingEdge(node);
            }
        }
        /* 
        for(DepGraphNode node: sourceToSink.keySet()){
            System.out.println(node.toString() +  " In: " + node.getInCount() + " Out: " + node.getOutCount());
        }
        */
        
        //System.out.println("Time to build //graph: " + (System.nanoTime() - time));
    }

    // Compute priorities
    // load and store have latency 5
    // mult has letency 3
    // Everything else latency 1
    public void computePriorities(){
        //long time = System.nanoTime();
        // Find any roots
        Queue<DepGraphNode> visit = new LinkedList<>();
        // Cycle through nodes in Sink to source and find any that have no sources (root)
        for(DepGraphNode node : sinkToSource.keySet()){
            if(sinkToSource.get(node).size() == 0){
                visit.add(node);
                if(node.getOp() == OpCode.LOAD.getValue() || node.getOp() == OpCode.STORE.getValue()){
                    node.setPriority(5);
                }
                else if(node.getOp() == OpCode.MULT.getValue()){
                    node.setPriority(3);
                }
                else{
                    node.setPriority(1);
                }
                //System.out.println(node.toString() + " Priority: " + node.getPriority());
            }
        }

        // Bredth first search from node and compute priorities
        while(visit.size() != 0){
            // Get node
            DepGraphNode currNode = visit.poll();
            // Go through children and assign them priorities based on //graph
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
                        //System.out.println("trialPriority for " + child.getItem1().toString() + ": " + trialPriority);
                        child.getItem1().setPriority(trialPriority);
                    }
                }
                child.getItem1().decrementIn();
                if(child.getItem1().getInCount() == 0){
                    visit.add(child.getItem1());
                    //System.out.println(child.getItem1().toString() + " Priority: " + child.getItem1().getPriority());
                }
            }
        }
        /* 
        for(DepGraphNode node: sourceToSink.keySet()){
            if(node.getOp() == OpCode.STORE.getValue()){
                System.out.println(node.toString() + " In: " + node.getInCount() + " Out: " + node.getOutCount());
            }
        }
        //System.out.println("Time to compute priorities: " + (System.nanoTime() - time));
        */
    }

    //store r15 => r1 relies on data load 1032 => r1, data load r17 => r15, and serial load r16 => r10
    // Schedule code
    public void schedule(){
        //long time = System.nanoTime();
        // three priority queues
        // f1 only stores things that have to be in f1
        // f2 only stores things that have to be in f2
        // both stores things that can go in either
        // only one output can happen at a time
        int cycle = 1;
        List<DepGraphNode> toRemove = new ArrayList<>();

        // Load in leaves into ready
        for(DepGraphNode node : sourceToSink.keySet()){
            if(sourceToSink.get(node).size() == 0){
                moveToReady(node);
            }
        }
        // While there's still something to schedule and something still running
        while(f0.size() != 0 || f1.size() != 0 || both.size() != 0 || active.size() != 0){
            //System.out.println("cycle: " + cycle);
            //System.out.println("f0: " + f0.size() + " f1: " + f1.size() + " both: " + both.size() + " active: " + active.size());
            // pick an operation o for each functional unit move o from ready to active
            // Should be highest priority
            // look in all of them and take the two highest priority
            moveToActive(cycle);
            
            // increment cycle
            cycle = cycle + 1;
            //System.out.println((cycle - 1) + " -> " + cycle);
            toRemove.clear();
            // find each op o in active that retires in this cycle and remove from active
            for(DepGraphNode activeNode : active){
                //System.out.println("Node: " + activeNode.toString() + " End cycle: " + activeNode.getEndCycle());
                if(activeNode.getEndCycle() == cycle){
                    //activeNode.setStatus(3);
                    handleChildren(activeNode);
                    toRemove.add(activeNode);
                }
                // For each multi-cycle operation o in Active
                else if(activeNode.getOp() == OpCode.LOAD.getValue() || activeNode.getOp() == OpCode.STORE.getValue()){
                    // Only want to do this once per op
                    if(activeNode.getEndCycle() - cycle == 4){
                        // check ops that depend on o for early releases
                        // add any early releases to Ready
                        earlyRelease(activeNode);
                    }
                }
            }
            // Remove all nodes that are done
            for(DepGraphNode node : toRemove){
                active.remove(node);
            }
        }      
        //System.out.println("Time to schedule: " + (System.nanoTime() - time))       ;
    }

    // This is to ensure the out count for a store is correct during the building of the //graph
    public void checkExistingEdge(DepGraphNode source){
        List<DepGraphNode> toDelete = new ArrayList<>();
        List<DepGraphNode> seen = new ArrayList<>();

        for(Pair<DepGraphNode, Integer> node : sourceToSink.get(source)){
            //System.out.println(dependent.getItem1().toString() + ", " + dependent.getItem2());
            // If you hit a data or conflict edge to a node with a serial edge add it to seen we need to compare later
            // only serial edges will add to toDelete, only non serial edges will add to seen
            if(node.getItem2() == 1){
                // We haven't seen this node yet
                toDelete.add(node.getItem1());
            }
            // not a serial edge
            else{
                seen.add(node.getItem1());
            }
        }

        for(DepGraphNode node : toDelete){
            if(seen.contains(node)){
                source.decrementOut();
                node.decrementIn();
                // We can remove the serial version of this node if there's a data or conflict version
                sourceToSink.get(source).remove(new Pair<>(node, 1));
                sinkToSource.get(node).remove(new Pair<>(source, 1));
            }
        }
    }

    // Does the required setup to create a file that 
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

    public void moveToReady(DepGraphNode node){
        //long time = System.nanoTime();
       //System.out.println("moving " + node.toString() + " to ready");
        if(node.getOp() == OpCode.STORE.getValue() || node.getOp() == OpCode.LOAD.getValue()){
            f0.add(node);
        }
        else if(node.getOp() == OpCode.MULT.getValue()){
            f1.add(node);
        }
        else{
            both.add(node);
        }
        //node.setStatus(1);
        //System.out.println("move to ready: " + (System.nanoTime() - time));
    }

    // Chooses a node from ready to go into active
    public void moveToActive(int cycle){
        //long time = System.nanoTime();
        DepGraphNode f0Prior = f0.peek();
        DepGraphNode f1Prior = f1.peek();
        DepGraphNode bothPrior = both.peek();
        //System.out.println("f0 " + f0Prior + "\n" + "f1 " + f1Prior + "\n");
        // nothing is ready
        // !f0 !f1 !both
        if(f0Prior == null && f1Prior == null && bothPrior == null){
            // nops for everything
            printILOCHelper(null, null);
        }
        if(f0Prior != null){
            if(f1Prior != null){
                // f0 f1 both
                if(bothPrior != null){
                    int maxTwoCheck = maxTwo(f0Prior.getPriority(), f1Prior.getPriority(), bothPrior.getPriority());
                    // f0 f1
                    if(maxTwoCheck == 0){
                        f0Prior = f0.poll();
                        f1Prior = f1.poll();
                        // Set end cycle f0
                        f0Prior.setEndCycle(cycle);
                        //f0Prior.setStatus(2);
                        active.add(f0Prior);
                        // Set end cycle f1
                        f1Prior.setEndCycle(cycle);
                        //f1Prior.setStatus(2);
                        active.add(f1Prior);
                        printILOCHelper(f0Prior, f1Prior);
                    }
                    // f0 both
                    else if(maxTwoCheck == 1) {
                        f0Prior = f0.poll();
                        bothPrior = both.poll();
                        // Set end cycle f0
                        f0Prior.setEndCycle(cycle);
                        //f0Prior.setStatus(2);
                        active.add(f0Prior);
                        // Set end cycle f1
                        bothPrior.setEndCycle(cycle);
                        //bothPrior.setStatus(2);
                        active.add(bothPrior);
                        printILOCHelper(f0Prior, bothPrior);
                    }
                    // both f1 
                    else{
                        f1Prior = f1.poll();
                        bothPrior = both.poll();
                        // Set end cycle f0
                        f1Prior.setEndCycle(cycle);
                        //f1Prior.setStatus(2);
                        active.add(f1Prior);
                        // Set end cycle f1
                        bothPrior.setEndCycle(cycle);
                        //bothPrior.setStatus(2);
                        active.add(bothPrior);
                        printILOCHelper(bothPrior, f1Prior);
                    }
                }
                // f0 f1 !both
                else{
                    f0Prior = f0.poll();
                    f1Prior = f1.poll();
                    // Set end cycle f0
                    f0Prior.setEndCycle(cycle);
                    //f0Prior.setStatus(2);
                    active.add(f0Prior);
                    // Set end cycle f1
                    f1Prior.setEndCycle(cycle);
                    //f1Prior.setStatus(2);
                    active.add(f1Prior);
                    printILOCHelper(f0Prior, f1Prior);
                }
            }
            // f0 !f1 both
            else if(bothPrior != null){
                f0Prior = f0.poll();
                bothPrior = both.poll();
                // Set end cycle f0
                f0Prior.setEndCycle(cycle);
                //f0Prior.setStatus(2);
                active.add(f0Prior);
                // Set end cycle f1
                bothPrior.setEndCycle(cycle);
                //bothPrior.setStatus(2);
                active.add(bothPrior);
                printILOCHelper(f0Prior, bothPrior);
            }
            // f0 !f1 !both
            else{
                f0Prior = f0.poll();
                f0Prior.setEndCycle(cycle);
                //f0Prior.setStatus(2);
                active.add(f0Prior);
                printILOCHelper(f0Prior, null);
            }
        }
        else if(f1Prior != null){
            // !f0 f1 both
            if(bothPrior != null){
                f1Prior = f1.poll();
                bothPrior = both.poll();
                // Set end cycle f0
                f1Prior.setEndCycle(cycle);
                //f1Prior.setStatus(2);
                active.add(f1Prior);
                // Set end cycle f1
                bothPrior.setEndCycle(cycle);
                //bothPrior.setStatus(2);
                active.add(bothPrior);
                printILOCHelper(bothPrior, f1Prior);               
            }
            // !f0 f1 !both
            else{
                f1Prior = f1.poll();
                // Set end cycle f0
                f1Prior.setEndCycle(cycle);
                //f1Prior.setStatus(2);
                active.add(f1Prior);
                // Set end cycle f1
                printILOCHelper(null, f1Prior);
            }
        }
        else if(bothPrior != null) {
            // !f0 !f1 both both
            bothPrior = both.poll();
            // Set stuff
            bothPrior.setEndCycle(cycle);
            //bothPrior.setStatus(2);
            active.add(bothPrior);
            // Check again again
            DepGraphNode bothPrior2 = both.poll();
            if(bothPrior2 != null){
                bothPrior2.setEndCycle(cycle);
                //bothPrior2.setStatus(2);
                active.add(bothPrior2);
            }
            printILOCHelper(bothPrior, bothPrior2);
        }
        //System.out.println("move to active: " + (System.nanoTime() - time));
    }

    // For sake of my mind
    // f0 f1 = 0
    // f0 both = 1
    // f1 both = 2
    public int maxTwo(int f0, int f1, int both){
        if(f0 > f1){
            if(both > f1){
                return 1;
            }
            return 0;
        }
        else{
            if(both > f1){
                return 2;
            }
            return 0;
        }
    }

    // Helper to print out what's active in a given cycle
    public void printILOCHelper(DepGraphNode f0Node, DepGraphNode f1Node){
        if(f0Node == null && f1Node == null){
            System.out.println("[ nop; nop ]");
        }
        else if(f0Node == null && f1Node != null){
            System.out.println("[ nop; "+ f1Node.toString() + " ]");
        }
        else if(f0Node != null && f1Node == null){
            System.out.println("[ " + f0Node.toString() + "; nop ]");
        }
        else{
            System.out.println("[ " + f0Node.toString() + "; " + f1Node.toString() + " ]");
        }
    }

    // When a parent is done, checks if the children become ready
    public void handleChildren(DepGraphNode parent){
        //long time = System.nanoTime();
        //System.out.println("parent: " + parent.toString());
        // For every node that depends on the parameterized node
        for (Pair<DepGraphNode, Integer> node : sinkToSource.get(parent)){
            DepGraphNode dependent = node.getItem1();
            // Avoid double counting
            dependent.decrementOut();
            /*             if(dependent.getOp() == OpCode.STORE.getValue()){
                System.out.println("Decrement store: " + dependent.toString() + " resulting in out count: " + dependent.getOutCount());
            }
            */
            // All nodes this node depends on are done 
            if(dependent.getOutCount() == 0){
                //System.out.println("Moving: " + dependent.toString());
                moveToReady(dependent);
            }
        } 
        //System.out.println("Handle children: " + (System.nanoTime() - time));
    }
        
    // Looks through a memops dependents and performs any necessary early releases
    public void earlyRelease(DepGraphNode parent){
        //long time = System.nanoTime();
        List<Pair<DepGraphNode, Integer>> toRemove = new ArrayList<>();
        //System.out.println(parent.toString() + " finished. Starting early release");
        for(Pair<DepGraphNode, Integer> dependent : sinkToSource.get(parent)){
            //System.out.println(dependent.getItem1().toString() + ", " + dependent.getItem2());
            // if we hit a node with a serial edge to parent, we can decrement it
            if(dependent.getItem2() == 1){
                // We've changed stuff now so we know this is the only edge to the node depending on this
                dependent.getItem1().decrementOut();
                toRemove.add(dependent);
                if(dependent.getItem1().getOp() == OpCode.STORE.getValue()){
                    //System.out.println("Early release: " + dependent.getItem1().toString() + " resulting in out count: " + dependent.getItem1().getOutCount());
                }
                if(dependent.getItem1().getOutCount() == 0){
                    // Decrement it so if it get's decremented later on it will = -1 and not be moved to ready again
                    moveToReady(dependent.getItem1());
                }
            }
        //System.out.println("early release: " + (System.nanoTime() - time));
        }
        //System.out.println("To remove: " + toRemove.toString());
        for(Pair<DepGraphNode, Integer> node : toRemove){
            sinkToSource.get(parent).remove(node);
        }
    }
}