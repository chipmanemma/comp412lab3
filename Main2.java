public class Main2 {
    public static void main(String[] args) throws Exception{
        // If we need to do more
        if (parseCommandLines(args)) {
            ILOCParser parser = new ILOCParser(new ILOCScanner(args[0]));
            Pair<IRList, Integer> res = parser.parse();
            if (res.getItem2() == -1) {
                return;
            }
            ILOCRenamer renamer = new ILOCRenamer(res);
            Integer maxVr = renamer.rename();
            //res.getItem1().printVRCodeRep();
            //System.out.println("asldkjfslk");
            ILOCScheduler scheduler = new ILOCScheduler(res.getItem1(), maxVr);
            scheduler.buildGraph(); // call dot -T pdf graphText.dot > graphText.pdf
            scheduler.computePriorities();
            scheduler.schedule();
        }
    }

    /**
     * Parses the command lines
     * @param args
     * @return false if nothing else needs to be done and true if reallocation needs to happen
     * @throws Exception
     */
    private static boolean parseCommandLines(String[] args) throws Exception{
        if (args.length > 0) {
            if (args[0].equals("-h")) {
                System.out.println("This is lab 3 for comp 412\nAvailable parameters are: \n\t-h: lists available commands\n\t<name>: The file to be scheduled");
                return false;
            }
            else if (args.length > 1) {
                System.err.println("ERROR: Too many command line arguments");
                System.out.println("This is lab 3 for comp 412\nAvailable parameters are: \n\t-h: lists available commands\n\t<name>: The file to be scheduled");
                return false;
            }
            else{
                return true;
            }
        }
        else {
            System.out.println("ERROR: No command line arguments found for 412alloc");
            return false;
        }
    }
}
