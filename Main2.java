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
            System.out.println(maxVr);
            res.getItem1().printVRCodeRep();
            ILOCScheduler scheduler = new ILOCScheduler(res.getItem1(), maxVr);
            scheduler.buildGraph();
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
            else if (args.length > 2) {
                System.err.println("ERROR: Too many command line arguments");
                System.out.println("This is lab 3 for comp 412\nAvailable parameters are: \n\t-h: lists available commands\n\t<name>: The file to be scheduled");
                return false;
            }
            else{
                System.out.println("running");
                return true;
            }
        }
        else {
            System.out.println("ERROR: No command line arguments found for 412alloc");
            return false;
        }
    }
}
