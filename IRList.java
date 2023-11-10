public class IRList {
    private IRNode head;
    private IRNode tail;

    public IRList() {
        this.head = null;
        this.tail = null;
    }
   
    public IRNode getHead() {
        return head;
    }

    public IRNode getTail() {
        return tail;
    }
    public void printIR() {
        IRNode curr = head;
        System.out.println("Printing IR");
        while (curr != null) {
            curr.printIRNode();
            curr = curr.getNext();
        }
    }

    public void printDetailedIR() {
        IRNode curr = head;
        System.out.println("Printing Detailed IR");
        while (curr != null) {
            curr.printFullIRNode();
            curr = curr.getNext();
        }
    }

    public void printVRCodeRep() {
        IRNode curr = head;
        while (curr != null) {
            curr.printVRCodeRep();
            curr = curr.getNext();
        }
    }

    public void printPRCodeRep() {
        IRNode curr = head;
        while (curr != null) {
            curr.printPRCodeRep();
            curr = curr.getNext();
        }
    }

    // Inserts node at the end
    public void insertNode(int line, int opCode, int sr1, int sr2, int sr3) {
        IRNode newNode = new IRNode(line, opCode, sr1, sr2, sr3);
        if (tail == null) {
            head = newNode;
            tail = newNode;
        }
        else {
            tail.setNext(newNode);
            newNode.setPrev(tail);
            tail = newNode;
        }
    }
}
