import java.util.HashSet;

public class ILOCRenamer {
    IRList ir;
    int highestSR;
    public ILOCRenamer(Pair<IRList, Integer> res) {
        this.ir = res.getItem1();
        this.highestSR = res.getItem2();
    }
    
    /**
     * Rename IR registers to virtual register names
     * Returns the max live and max VR of all 
     */
    public Pair<Integer, Integer> rename() {
        int vrName = 0;
        int maxlive = 0; // Keep in mind if incrementing at use and decrementing at def this underestimates. Block might increment use that's never defined this overestimates. op might use the same value twice this will overestimate.
        int currlive = 0;
        int[] srToVr = new int[highestSR + 1];
        int[] LU = new int[highestSR + 1]; // Source Registration name to operation number. Most recent use
        HashSet<Integer> seenVR = new HashSet<>();

        // Set to invalid
        for(int i = 0; i < highestSR + 1; i++) {
            srToVr[i] = -1;
            LU[i] = -1;
        }
        IRNode currOp = ir.getTail();
        // For all op in the block bottom to top
        while(currOp != null) {
            int[] opData = currOp.getData();
            // If output or nop we can ignore because no registers are involved
            if (opData[OpInfoEnum.OP.getValue()] == 8 || opData[OpInfoEnum.OP.getValue()] == 9) {
                currOp = currOp.getPrev();
                continue;
            }
            // line  opCode  SR1 = 2 VR1 = 3 PR1 = 4 NU1 = 5 SR2 = 6 VR2 = 7 PR2 = 8 NU2 = 9 SR3 = 10 VR3 = 11 PR3 = 12 NU3 = 13
            //Store special case
            if (opData[OpInfoEnum.OP.getValue()] == 2) {
                // SR1 Used
                if (srToVr[opData[OpInfoEnum.SR1.getValue()]] == -1) { // If this sr hasn't been assigned a vr
                    srToVr[opData[OpInfoEnum.SR1.getValue()]] = vrName; // Give the sr a NEW vr
                    //System.out.println("sr1 to vr1 at " + opData[OpInfoEnum.SR1.getValue()]+ " set to " + vrName);
                    
                    // If this vr is a last use and and hasn't been seen before add to curr live

                    vrName++;
                }
                // Set VR1
                currOp.setData(OpInfoEnum.VR1.getValue(), srToVr[opData[OpInfoEnum.SR1.getValue()]]);
                // Set NU1
                currOp.setData(OpInfoEnum.NU1.getValue(), LU[opData[OpInfoEnum.SR1.getValue()]]);
                if (opData[OpInfoEnum.NU1.getValue()] == -1) {
                    currlive++;
                    if (currlive > maxlive) {
                        maxlive = currlive;
                    }
                    seenVR.add(opData[OpInfoEnum.VR1.getValue()]);
                }
                //System.out.println(seenVR.toString());
                //System.out.println("currlive: " + currlive + " at op " + OpCode.getLabelFromValue(opData[OpInfoEnum.OP.getValue()]) + " vr2 " + opData[OpInfoEnum.VR1.getValue()]);

                // SR3 Used
                if (srToVr[opData[OpInfoEnum.SR3.getValue()]] == -1) { // If this sr hasn't been assigned a vr
                    srToVr[opData[OpInfoEnum.SR3.getValue()]] = vrName;
                    //System.out.println("sr3 to vr3 at " + opData[OpInfoEnum.SR3.getValue()]+ " set to " + vrName);


                    // If this vr is a last use and and hasn't been seen before add to curr live

                    vrName++;
                }
                // Set VR3
                currOp.setData(OpInfoEnum.VR3.getValue(), srToVr[opData[OpInfoEnum.SR3.getValue()]]);
                // Set NU3
                currOp.setData(OpInfoEnum.NU3.getValue(), LU[opData[OpInfoEnum.SR3.getValue()]]);
                if (opData[OpInfoEnum.NU3.getValue()] == -1) {
                    currlive++;
                    if (currlive > maxlive) {
                        maxlive = currlive;
                    }
                    seenVR.add(opData[OpInfoEnum.VR3.getValue()]);
                }
                //System.out.println("currlive: " + currlive + " at op " +  OpCode.getLabelFromValue(opData[OpInfoEnum.OP.getValue()]) + " vr3 " + opData[OpInfoEnum.VR3.getValue()]);
                // Set equal to lines
                LU[opData[OpInfoEnum.SR1.getValue()]] = opData[0];
                LU[opData[OpInfoEnum.SR3.getValue()]] = opData[0];
                currOp = currOp.getPrev();
            }

            else {
                // DEFINITIONS
                // SR3 Defined
                if (srToVr[opData[OpInfoEnum.SR3.getValue()]] == -1) { // If this sr doesn't have an assigned vr
                    srToVr[opData[OpInfoEnum.SR3.getValue()]] = vrName;
                    //System.out.println("sr3 to vr3 at " + opData[OpInfoEnum.SR3.getValue()]+ " set to " + vrName);

                    // subtract one from curr live and set seen to false but ONLY IF SEEN IS TRUE. Otherwise need to add to max live
                    
                    vrName++;
                }
                // Set VR3
                currOp.setData(OpInfoEnum.VR3.getValue(), srToVr[opData[OpInfoEnum.SR3.getValue()]]);
                // Set NU3 to this line
                currOp.setData(OpInfoEnum.NU3.getValue(), LU[opData[OpInfoEnum.SR3.getValue()]]);

                //System.out.println(opData[OpInfoEnum.VR3.getValue()]);
                //System.out.println(seenVR.toString());
                if (seenVR.contains(opData[OpInfoEnum.VR3.getValue()])) {
                    currlive--;
                    seenVR.remove(opData[OpInfoEnum.VR3.getValue()]);
                }
                else {
                    maxlive++; // We need to take into account that everything after this should have had this definition as a held reg
                }
                srToVr[opData[OpInfoEnum.SR3.getValue()]] = -1;
                LU[opData[OpInfoEnum.SR3.getValue()]] = -1;
                //System.out.println("currlive: " + currlive + " at op " +  OpCode.getLabelFromValue(opData[OpInfoEnum.OP.getValue()]) + " vr3 " + opData[OpInfoEnum.VR3.getValue()]);

                // USES
                // Check that SR1 is not a constant (loadI)
                if (opData[OpInfoEnum.OP.getValue()] != 1) {
                    // SR1 Use
                    if (srToVr[opData[OpInfoEnum.SR1.getValue()]] == -1) { // If sr has not been assigned a VR
                        srToVr[opData[OpInfoEnum.SR1.getValue()]] = vrName;
                        //System.out.println("sr1 to vr1 at " + opData[OpInfoEnum.SR1.getValue()]+ " set to " + vrName);
                        vrName++;
                    }
                    // Set VR1
                    currOp.setData(OpInfoEnum.VR1.getValue(), srToVr[opData[OpInfoEnum.SR1.getValue()]]);
                    // Set NU1
                    currOp.setData(OpInfoEnum.NU1.getValue(), LU[opData[OpInfoEnum.SR1.getValue()]]);
                    // Check if this is a last use
                    // If a last use then increment curr live
                    if (opData[OpInfoEnum.NU1.getValue()] == -1) {
                        currlive++;
                        if (currlive > maxlive) {
                            maxlive = currlive;
                        }
                        seenVR.add(opData[OpInfoEnum.VR1.getValue()]);
                    }
                    LU[opData[OpInfoEnum.SR1.getValue()]] = opData[0];
                }
                //System.out.println("currlive: " + currlive + " at op " +  OpCode.getLabelFromValue(opData[OpInfoEnum.OP.getValue()]) + " vr1 " + opData[OpInfoEnum.VR1.getValue()]);
                
                // SR2 Use
                // Check that SR2 is used
                if (opData[OpInfoEnum.SR2.getValue()] != -1) {
                    if (srToVr[opData[OpInfoEnum.SR2.getValue()]] == -1) { // If sr has not been assigned a VR
                        srToVr[opData[OpInfoEnum.SR2.getValue()]] = vrName;
                        //System.out.println("sr2 to vr2 at " + opData[OpInfoEnum.SR2.getValue()]+ " set to " + vrName);
                        vrName++;
                    }
                    // Set VR2
                    currOp.setData(OpInfoEnum.VR2.getValue(), srToVr[opData[OpInfoEnum.SR2.getValue()]]);
                    // Set NU2
                    // If the same vr as VR1 then make sure the next uses are the same
                    if (opData[OpInfoEnum.VR2.getValue()] == opData[OpInfoEnum.VR1.getValue()]) {
                        currOp.setData(OpInfoEnum.NU2.getValue(), opData[OpInfoEnum.NU1.getValue()]);
                    }
                    // Otherwise do the normal thing
                    else {
                        currOp.setData(OpInfoEnum.NU2.getValue(), LU[opData[OpInfoEnum.SR2.getValue()]]);
                    }
                    // Check if this is a last use
                    // If a last use then increment curr live
                    if (opData[OpInfoEnum.NU2.getValue()] == -1) {
                        currlive++;
                        if (currlive > maxlive) {
                            maxlive = currlive;
                        }
                        seenVR.add(opData[OpInfoEnum.VR2.getValue()]);
                    }
                    LU[opData[OpInfoEnum.SR2.getValue()]] = opData[0];
                }  
                //System.out.println("currlive: " + currlive + " at op " +  OpCode.getLabelFromValue(opData[OpInfoEnum.OP.getValue()]) + " vr2 " + opData[OpInfoEnum.VR2.getValue()]);
                currOp = currOp.getPrev();  
            } 
        } 
        //System.out.println("maxlive: " + maxlive + " maxVR: " + (vrName-1));
        return new Pair<Integer, Integer>(maxlive, vrName-1);     
    }
}
