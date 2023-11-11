public class Main {
    public static void main(String[] args) throws Exception{
        // If we need to do more
        if (parseCommandLines(args)) {
            ILOCParser parser = new ILOCParser(new ILOCScanner(args[1]));
            Pair<IRList, Integer> res = parser.parse();
            if (res.getItem2() == -1) {
                return;
            }
            ILOCRenamer renamer = new ILOCRenamer(res);
            Pair<Integer, Integer> renamerRes = renamer.rename();
            //res.getItem1().printIR();
            //res.getItem1().printVRCodeRep();
            ILOCAllocator allocator = new ILOCAllocator(res.getItem1(), renamerRes.getItem1(), Integer.parseInt(args[0]), renamerRes.getItem2());
            allocator.allocate();
            //res.getItem1().printPRCodeRep();
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
                System.out.println("This is lab 2 for comp 412\nAvailable flags are: \n\t-h: lists available commands\n\t-x <name>: runs both scanner, parser, and renamer on file <name> and prints out the renamed code \n\tk <name>: same as -x but also runs allocator on code before printing");
                return false;
            }
            else if (args.length > 3) {
                System.err.println("ERROR: Too many command line arguments");
                System.out.println("This is lab 2 for comp 412\nAvailable flags are: \n\t-h: lists available commands\n\t-x <name>: runs both scanner, parser, and renamer on file <name> and prints out the renamed code \n\tk <name>: same as -x but also runs allocator on code before printing");
                return false;
            }
            // Check if it's k
            else if (isInteger(args[0])) {
                if (Integer.parseInt(args[0]) < 3) {
                    System.err.println("ERROR: Need at least 3 physical registers to perform allocation");
                    return false;
                }
                return true;
            }
            else {
                System.err.println("ERROR: Non-valid command line args");
                System.err.println("This is lab 2 for comp 412\nAvailable flags are: \n\t-h: lists available commands\n\t-x <name>: runs both scanner, parser, and renamer on file <name> and prints out the renamed code \n\tk <name>: same as -x but also runs allocator on code before printing");
                return false;
            }
        }
        else {
            System.out.println("ERROR: No command line arguments found for 412alloc");
            return false;
        }
    }

    /**
     * Helper function that checks if a string can be parsed to an integer
     * @param str
     * @return true if can be parsed to integer, false otherwise
     */
    private static boolean isInteger(String str) {
        try{
            Integer.parseInt(str);
            return true;
        }
        catch(NumberFormatException e) {
            return false;
        }
    }
    
}
