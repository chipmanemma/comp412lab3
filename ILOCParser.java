/*
 * A class that represents a parser which takes <category, lexeme> pairs 
 */

public class ILOCParser {
    private ILOCScanner scanner;
    private IRList ir;
    private int highestSR;
    public ILOCParser(ILOCScanner scanner) {
        this.scanner = scanner;
        this.ir = new IRList();
    }

    /**
     * Parse a file
     * 
        ERROR(-1, "ERROR"),
        MEMOP(0, "MEMOP"),
        LOADI(1, "LOADI"),
        ARITHOP(2, "ARITHOP"),
        OUTPUT(3, "OUTPUT"),
        NOP(4, "NOP"),
        CONSTANT(5, "CONSTANT"),
        REGISTER(6, "REGISTER"),
        COMMA(7, "COMMA"),
        INTO(8, "INTO"),
        EOF(9, "EOF"),
        EOL(10, "EOL");
     * @param extraInfo if extraInfo is true, need to print out the IR
     * @throws Exception
     */
    public Pair<IRList, Integer> parse() throws Exception{
        CatLex word = this.scanner.nextToken();
        boolean success = true;
        //System.out.println("Calling parse in Parser");
        if (word.getOpCategory() == OpCategory.FILEFAIL.getValue()) {
            System.out.println(word.getLexeme());
            return new Pair<IRList, Integer>(ir, -1);
        }
        while(word.getOpCategory() != OpCategory.EOF.getValue()) {
            int category = word.getOpCategory();
            //System.out.println("Start of parse loop: " + word.getLexeme() + " " + word.getOpCategory());
            switch(category) {
                case (0):
                    //System.out.println("MEMOP");
                    success = finish_memop(word.getLexeme()) && success;
                    //System.out.println("Returned from memop with " + success);
                    break;
                case (1):
                    //System.out.println("LOADI");
                    success = finish_loadI() && success;
                    break;
                case (2):
                    //System.out.println("ARITHOP");
                    success = finish_arithop(word.getLexeme()) && success;
                    break;
                case (3):
                    //System.out.println("OUTPUT");
                    success = finish_output() && success;
                    break;
                case (4):
                    //System.out.println("NOP");
                    success = finish_nop() && success;
                    break;
                case (10): // newline
                    //System.out.println("success parsing EOL on line " + word.getLine());
                    break;
                case (-1):
                    System.out.println(word.getLexeme() + " - Scanner");
                    success = false;
                    break;
                default:
                    System.out.println("ERROR " + word.getLine() + ": " + OpCategory.getLabelFromValue(category) + " is not a valid line start");
                    scanner.nextLine();
                    success = false;
                    break;
            }
            word = this.scanner.nextToken();
            //System.out.println("Next token is: " + word.getLexeme());
        }
        if (success) {
            System.out.println("//Successful full parse");
            return new Pair<IRList, Integer>(ir, highestSR);
        }
        else {
            System.out.println("Parse failed");
            return new Pair<IRList, Integer>(ir, -1);
        }
    }
    
    /**
     * 
     * @throws Exception
     */
    public boolean finish_memop(String lexeme) throws Exception{
        CatLex word = this.scanner.nextToken();
        int sr1, sr3;
        //System.out.println("MEMOP follow up 1 " + word.getLexeme());
        if (word.getOpCategory() != OpCategory.REGISTER.getValue()) {
            System.out.println("ERROR " + word.getLine() + ": Was expecting REGISTER but received " + OpCategory.getLabelFromValue(word.getOpCategory()));
            if (word.getOpCategory() == OpCategory.ERROR.getValue()) {
                System.out.println(word.getLexeme() + " - Scanner");
            }
            if (word.getOpCategory() != OpCategory.EOL.getValue() && word.getOpCategory() != OpCategory.ERROR.getValue()) {
                scanner.nextLine();
            }
            return false;
        }
        else {
            CatLex word2 = this.scanner.nextToken();
            //System.out.println("MEMOP follow up 2 " + word2.getLexeme());
            if (word2.getOpCategory() != OpCategory.INTO.getValue()) {
                System.out.println("ERROR " + word2.getLine() + ": Was expecting INTO but received " + OpCategory.getLabelFromValue(word2.getOpCategory()));
                if (word2.getOpCategory() == OpCategory.ERROR.getValue()) {
                    System.out.println(word2.getLexeme() + " - Scanner");
                }
                if (word2.getOpCategory() != OpCategory.EOL.getValue() && word2.getOpCategory() != OpCategory.ERROR.getValue()) {
                    scanner.nextLine();
                }
                return false;
            }
            else {
                CatLex word3 = this.scanner.nextToken();
                //System.out.println("MEMOP follow up 3 " + word3.getLexeme());
                if (word3.getOpCategory() != OpCategory.REGISTER.getValue()) {
                    System.out.println("ERROR " + word3.getLine() + ": Was expecting REGISTER but received " + OpCategory.getLabelFromValue(word3.getOpCategory()));
                    if (word3.getOpCategory() == OpCategory.ERROR.getValue()) {
                        System.out.println(word3.getLexeme() + " - Scanner");
                    }
                    if (word3.getOpCategory() != OpCategory.EOL.getValue() && word3.getOpCategory() != OpCategory.ERROR.getValue()) {
                        scanner.nextLine();
                    }
                    return false;
                }
                else {
                    CatLex word4 = this.scanner.nextToken();
                    //System.out.println("MEMOP follow up 4" + word4.getLexeme());
                    if (word4.getOpCategory() == OpCategory.EOL.getValue() || word4.getOpCategory() == OpCategory.EOF.getValue()) {
                        sr1 = Integer.parseInt(word.getLexeme().substring(1));
                        sr3 = Integer.parseInt(word3.getLexeme().substring(1));
                        if (sr1 > highestSR) {
                            highestSR = sr1;
                        }
                        if (sr3 > highestSR) {
                            highestSR = sr3;
                        }
                        //System.out.println("success parsing MEMOP on line " + word4.getLexeme());
                        ir.insertNode(word4.getLine(), OpCode.getValueFromLabel(lexeme), sr1 , -1, sr3);
                        return true;
                    }
                    else {
                        System.out.println("ERROR " + word4.getLine() + ": Was expecting EOL or EOF but received " + OpCategory.getLabelFromValue(word4.getOpCategory()));
                        if (word4.getOpCategory() == OpCategory.ERROR.getValue()) {
                            System.out.println(word4.getLexeme() + " - Scanner");
                        }
                        if (word4.getOpCategory() != OpCategory.EOL.getValue() && word4.getOpCategory() != OpCategory.ERROR.getValue()) {
                            scanner.nextLine();
                        }
                        return false;
                    }
                }
            }
        }
    }

    /**
     * 
     * @throws Exception
     */
    public boolean finish_loadI() throws Exception{
        CatLex word = this.scanner.nextToken();
        int sr3;
        if (word.getOpCategory() != OpCategory.CONSTANT.getValue()) {
            System.out.println("ERROR " + word.getLine() + ": Was expecting CONSTANT but received " + OpCategory.getLabelFromValue(word.getOpCategory()));
            if (word.getOpCategory() == OpCategory.ERROR.getValue()) {
                System.out.println(word.getLexeme() + " - Scanner");
            }
            if (word.getOpCategory() != OpCategory.EOL.getValue() && word.getOpCategory() != OpCategory.ERROR.getValue()) {
                scanner.nextLine();
            }
            return false;
        }
        else {
            CatLex word2 = this.scanner.nextToken();
            if (word2.getOpCategory() != OpCategory.INTO.getValue()) {
                System.out.println("ERROR " + word2.getLine() + ": Was expecting INTO but received " + OpCategory.getLabelFromValue(word2.getOpCategory()));
                if (word2.getOpCategory() == OpCategory.ERROR.getValue()) {
                    System.out.println(word2.getLexeme() + " - Scanner");
                }
                if (word2.getOpCategory() != OpCategory.EOL.getValue() && word2.getOpCategory() != OpCategory.ERROR.getValue()) {
                    scanner.nextLine();
                }
                return false;
            }
            else {
                CatLex word3 = this.scanner.nextToken();
                if (word3.getOpCategory() != OpCategory.REGISTER.getValue()) {
                    System.out.println("ERROR " + word3.getLine() + ": Was expecting REGISTER but received " + OpCategory.getLabelFromValue(word3.getOpCategory()));
                    if (word3.getOpCategory() == OpCategory.ERROR.getValue()) {
                        System.out.println(word3.getLexeme() + " - Scanner");
                    }
                    if (word3.getOpCategory() != OpCategory.EOL.getValue() && word3.getOpCategory() != OpCategory.ERROR.getValue()) {
                        scanner.nextLine();
                    }
                    return false;
                }
                else {
                    CatLex word4 = this.scanner.nextToken();
                    if (word4.getOpCategory() == OpCategory.EOL.getValue() || word4.getOpCategory() == OpCategory.EOF.getValue()) {
                        sr3 = Integer.parseInt(word3.getLexeme().substring(1));
                        if (sr3 > highestSR) {
                            highestSR = sr3;
                        }
                        //System.out.println("success parsing LOADI on line " + word4.getLine());
                        ir.insertNode(word4.getLine(), OpCode.LOADI.getValue(), Integer.parseInt(word.getLexeme()), -1, Integer.parseInt(word3.getLexeme().substring(1)));
                        return true;
                    }
                    else {
                        System.out.println("ERROR " + word4.getLine() + ": Was expecting EOL or EOF but received " + OpCategory.getLabelFromValue(word4.getOpCategory()));
                        if (word4.getOpCategory() == OpCategory.ERROR.getValue()) {
                            System.out.println(word4.getLexeme() + " - Scanner");
                        }   
                        if (word4.getOpCategory() != OpCategory.EOL.getValue() && word4.getOpCategory() != OpCategory.ERROR.getValue()) {
                            scanner.nextLine();
                        } 
                        return false;
                    }
                }
            }
        }
    }

    /**
     * 
     * @throws Exception
     */
    public boolean finish_arithop(String lexeme) throws Exception{
        CatLex word = this.scanner.nextToken();
        int sr1, sr2, sr3;
        if (word.getOpCategory() != OpCategory.REGISTER.getValue()) {
            System.out.println("ERROR " + word.getLine() + ": Was expecting REGISTER but received " + OpCategory.getLabelFromValue(word.getOpCategory()));
            if (word.getOpCategory() == OpCategory.ERROR.getValue()) {
                System.out.println(word.getLexeme() + " - Scanner");
            }
            if (word.getOpCategory() != OpCategory.EOL.getValue() && word.getOpCategory() != OpCategory.ERROR.getValue()) {
                scanner.nextLine();
            }
            return false;
        }
        else {
            CatLex word2 = this.scanner.nextToken();
            if (word2.getOpCategory() != OpCategory.COMMA.getValue()) {
                System.out.println("ERROR " + word2.getLine() + ": Was expecting COMMA but received " + OpCategory.getLabelFromValue(word2.getOpCategory()));
                if (word2.getOpCategory() == OpCategory.ERROR.getValue()) {
                    System.out.println(word2.getLexeme() + " - Scanner");
                }
                if (word2.getOpCategory() != OpCategory.EOL.getValue() && word2.getOpCategory() != OpCategory.ERROR.getValue()) {
                    scanner.nextLine();
                } 
                return false;
            }
            else {
                CatLex word3 = this.scanner.nextToken();
                if (word3.getOpCategory() != OpCategory.REGISTER.getValue()) {
                    System.out.println("ERROR " + word3.getLine() + ": Was expecting REGISTER but received " + OpCategory.getLabelFromValue(word3.getOpCategory()));
                    if (word3.getOpCategory() == OpCategory.ERROR.getValue()) {
                        System.out.println(word3.getLexeme() + " - Scanner");
                    }
                    if (word3.getOpCategory() != OpCategory.EOL.getValue() && word3.getOpCategory() != OpCategory.ERROR.getValue()) {
                        scanner.nextLine();
                    }
                    return false;
                }
                else {
                    CatLex word4 = this.scanner.nextToken();
                    if (word4.getOpCategory() != OpCategory.INTO.getValue()) {
                        System.out.println("ERROR " + word4.getLine() + ": Was expecting INTO but received " + OpCategory.getLabelFromValue(word4.getOpCategory()));
                        if (word4.getOpCategory() == OpCategory.ERROR.getValue()) {
                            System.out.println(word4.getLexeme() + " - Scanner");
                        }
                        if (word4.getOpCategory() != OpCategory.EOL.getValue() && word4.getOpCategory() != OpCategory.ERROR.getValue()) {
                            scanner.nextLine();
                        }
                        return false;
                    }
                    else {
                        CatLex word5 = this.scanner.nextToken();
                        if (word5.getOpCategory() != OpCategory.REGISTER.getValue()) {
                            System.out.println("ERROR " + word5.getLine() + ": Was expecting REGISTER but received " + OpCategory.getLabelFromValue(word5.getOpCategory()));
                            if (word5.getOpCategory() == OpCategory.ERROR.getValue()) {
                                    System.out.println(word5.getLexeme() + " - Scanner");
                            }
                            if (word5.getOpCategory() != OpCategory.EOL.getValue() && word5.getOpCategory() != OpCategory.ERROR.getValue()) {
                                scanner.nextLine();
                            } 
                            return false;
                        }
                        else {
                            CatLex word6 = this.scanner.nextToken();
                            if (word6.getOpCategory() == OpCategory.EOL.getValue() || word6.getOpCategory() == OpCategory.EOF.getValue()) {
                                sr1 = Integer.parseInt(word.getLexeme().substring(1));
                                sr2 = Integer.parseInt(word3.getLexeme().substring(1));
                                sr3 = Integer.parseInt(word5.getLexeme().substring(1));
                                if (sr1 > highestSR) {
                                    highestSR = sr1;
                                }
                                if (sr2 > highestSR) {
                                    highestSR = sr2;
                                }
                                if (sr3 > highestSR) {
                                    highestSR = sr3;
                                }
                                //System.out.println("success parsing ARITHOP on line " + word6.getLine());
                                ir.insertNode(word6.getLine(), OpCode.getValueFromLabel(lexeme), sr1, sr2, sr3);
                                return true;
                            }
                            else {
                                System.out.println("ERROR " + word6.getLine() + ": Was expecting EOL or EOF but received " + OpCategory.getLabelFromValue(word6.getOpCategory()));
                                if (word6.getOpCategory() == OpCategory.ERROR.getValue()) {
                                    System.out.println(word6.getLexeme() + " - Scanner");
                                }
                                if (word6.getOpCategory() != OpCategory.EOL.getValue() && word6.getOpCategory() != OpCategory.ERROR.getValue()) {
                                    scanner.nextLine();
                                }
                                return false;
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean finish_output() throws Exception{
        CatLex word = this.scanner.nextToken();
        if (word.getOpCategory() != OpCategory.CONSTANT.getValue()) {
            System.out.println("ERROR " + word.getLine() + ": Was expecting CONSTANT but received " + OpCategory.getLabelFromValue(word.getOpCategory()));
            if (word.getOpCategory() == OpCategory.ERROR.getValue()) {
                System.out.println(word.getLexeme() + " - Scanner");
            }
            if (word.getOpCategory() != OpCategory.EOL.getValue() && word.getOpCategory() != OpCategory.ERROR.getValue()) {
                scanner.nextLine();
            } 
            return false;
        }
        else {
            CatLex word2 = this.scanner.nextToken();
            if (word2.getOpCategory() == OpCategory.EOL.getValue() || word2.getOpCategory() == OpCategory.EOF.getValue()) {
                //System.out.println("success parsing OUTPUT on line " + word2.getLine());
                ir.insertNode(word2.getLine(), OpCode.OUTPUT.getValue(), Integer.parseInt(word.getLexeme()), -1, -1);     
                return true;           
            }
            else {
                System.out.println("ERROR " + word2.getLine() + ": Was expecting EOL or EOF but received " + OpCategory.getLabelFromValue(word2.getOpCategory()));
                if (word2.getOpCategory() == OpCategory.ERROR.getValue()) {
                    System.out.println(word2.getLexeme() + " - Scanner");
                }
                if (word2.getOpCategory() != OpCategory.EOL.getValue() && word2.getOpCategory() != OpCategory.ERROR.getValue()) {
                    scanner.nextLine();
                } 
                return false;
            }
        }
    }
    public boolean finish_nop()throws Exception{
        CatLex word = this.scanner.nextToken();
        if (word.getOpCategory() == OpCategory.EOL.getValue() || word.getOpCategory() == OpCategory.EOF.getValue()) {
            //System.out.println("success parsing NOP on line " + word.getLine());
            ir.insertNode(word.getLine(), OpCode.NOP.getValue(), -1, -1, -1);              
            return true;  
        }
        else {
            System.out.println("ERROR " + word.getLine() + ": Was expecting EOL or EOF but received " + OpCategory.getLabelFromValue(word.getOpCategory()));
            if (word.getOpCategory() == OpCategory.ERROR.getValue()) {
                System.out.println(word.getLexeme() + " - Scanner");
            }
            if (word.getOpCategory() != OpCategory.EOL.getValue() && word.getOpCategory() != OpCategory.ERROR.getValue()) {
                scanner.nextLine();
            } 
            return false;
        }
    }
}
