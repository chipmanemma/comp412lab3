import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ILOCScanner{
// load, store, loadI, add, sub, mult, lshift, rshift, output, nop
    // Categories: MEMOP, LOADI, ARITHOP, OUTPUT, NOP, CONSTANT, REGISTER, COMMA, INTO, EOF, EOL
    BufferedReader buffReader;
    private int line;
    private int fileStatus; // The current state the scanner is in
    private int readptr;
    private int lineLen;
    private String lineBuff;
    private char lastChar;
    private boolean atEOF; // Tells wether the scanner has reached the end of the file

    public ILOCScanner(String toRead) {
        fileStatus = 0;
        try{
            buffReader = new BufferedReader(new FileReader(toRead));
        }
        catch(Exception e) {
            fileStatus = -1;
        }
        readptr = 1;
        lineLen = 0;
        lineBuff = "";
        lastChar = 0;
        line = 0;
        atEOF = false;
    }

    /**
     * Returns the token of the next word in a line
     * @return
     * @throws IOException
     */
    public CatLex nextToken() throws IOException{
        //System.out.println("Start of nextToken");
        //System.out.println("Readptr: " + readptr + " lineLen: " + lineLen);
        // Only happens if there was an issue with the file being read
        if (fileStatus == -1) {
            return new CatLex(OpCategory.FILEFAIL.getValue(), "ERROR " + line + ": trying to get token from file that can't be reached", 0);
        }
        if (atEOF) {
            System.out.println("At end of file. Nothing to read");
            return new CatLex(OpCategory.EOF.getValue(), "", line);
        }
        char character;
        // For end of line purposes
        if (readptr == lineLen) {
            //System.out.println("Hit end of line");
            readptr++;
            return new CatLex(OpCategory.EOL.getValue(), "", line);
        }
        // Check if we need to load in another line
        if (readptr > lineLen) {
            if ((lineBuff = buffReader.readLine()) != null) {
                //System.out.println("line: " + lineBuff);
                lineLen = lineBuff.length();
                line = line + 1;
                readptr = 0;
            }
            else {
                atEOF = true;
                //System.out.println("Hit end of file");
                return new CatLex(OpCategory.EOF.getValue(), "", line);
            }
        }
        // Check if there was a character left over after handling integers
        if (lastChar != 0) {
            //System.out.println("Last char was: " + lastChar);
            if (lastChar == ' ' || lastChar == '\t') {
                //System.out.println("Calling WhiteSpaceFinder");
                character = whiteSpaceFinder();
                if (character == 0) {
                    readptr++;
                    //System.out.println("White space at end of line readptr: " + readptr + " lineLen: " + lineLen);
                    return new CatLex(OpCategory.EOL.getValue(), "", line);
                }
            }
            else {
                character = lastChar;
            }
            lastChar = 0;
        }
        // If no last character then get a new one to use
        else {
            if (readptr < lineLen) {
                character = lineBuff.charAt(readptr);
                //System.out.println("character is next in line is: " + character);
                readptr++;
                if (character == ' ' || character == '\t') {
                    character = whiteSpaceFinder();
                    if (character == 0) {
                        //System.out.println("White space at the end of the line");
                        readptr++;
                        //System.out.println("White space at end of line readptr: " + readptr + " lineLen: " + lineLen);
                        return new CatLex(OpCategory.EOL.getValue(),"", line);
                    }
                }
            }
            else {
                //System.out.println("End of line");
                readptr++;
                //System.out.println("106 End of line readptr: " + readptr + " lineLen: " + lineLen);
                return new CatLex(OpCategory.EOL.getValue(), "", line);
            }
        }
        // After choosing character
        //System.out.println("Scanner looking at " + character);
        switch(character) {
            case ('s'):
                if (readptr < lineLen) {
                    character = lineBuff.charAt(readptr);
                    readptr++;
                    if (character == 'u') {
                        return getSub();
                    }
                    else if (character == 't') {
                        return getStore();
                    }
                    else {
                        readptr = lineLen + 1;
                        return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected u or t but received \"" + character + "\"", line);
                    }
                }
                else { 
                    readptr = lineLen + 1;
                    return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": after s hit end of line", line);
                }
            case ('l'):
                if (readptr < lineLen - 3) {
                    character = lineBuff.charAt(readptr);
                    readptr++;
                    if (character == 's') {
                        return  getShift(true);
                    }
                    else if (character == 'o') {
                        character = lineBuff.charAt(readptr);
                        readptr++;
                        if (character == 'a') {                              
                            character = lineBuff.charAt(readptr);
                            readptr++;
                            if (character == 'd') {
                                character = lineBuff.charAt(readptr);
                                readptr++;
                                if (character == ' ' || character == '\t') {
                                    return new CatLex(OpCategory.MEMOP.getValue(), "load", line);
                                }
                                else if (character == 'I') {
                                    if (readptr < lineLen) {
                                        character = lineBuff.charAt(readptr);
                                        readptr++;
                                        if (character == ' ' || character == '\t') {
                                            return new CatLex(OpCategory.LOADI.getValue(), "loadI", line);
                                        }
                                        else { // Corresponds to ' '
                                            readptr = lineLen + 1;
                                            return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": op LOADI must be followed by whitespace", line);
                                        }
                                    }
                                    else { // Corresponds to ending after loadI
                                        return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + (line) + " after loadI hit end of line", line);
                                    }
                                }
                                else { // Corresponds to I
                                    readptr = lineLen + 1;
                                    return new CatLex(OpCategory.ERROR.getValue(), "ERROR "+ line + ": op LOAD must be followed by whitespace", line);
                                }
                            }
                            else { // Corresponds to d
                                readptr = lineLen + 1;
                                return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + (line) + " expected d but received \"" + character + "\"", line);
                            }   
                        }
                        else { // Corresponds to a
                            readptr = lineLen + 1;
                            return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + (line) + " expected a but received \"" + character + "\"", line);
                        }     
                    }
                    else { // Corresponds to s or o
                        readptr = lineLen + 1;
                        return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected s or o but received \"" + character + "\"", line);
                    }
                }
                else { // Corresponds to not having enough letters for "load "
                    readptr = lineLen + 1;
                    return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + (line) + " after l hit end of line", line);
                }
                    
            case ('r'):
                if (readptr < lineLen) {
                    character = lineBuff.charAt(readptr);
                    readptr++;
                    if (character == 's') {
                        return getShift(false);
                    }
                    else if ((int)character >= 48 && (int)character <= 57) {
                        String lexeme = "r" + character;
                        return getRegister(lexeme);
                    }
                    else {
                        readptr = lineLen + 1;
                        return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + (line) + " expected s or integer but received \"" + character + "\"", line);
                    }
                }
                else {
                    readptr = lineLen + 1;
                    return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + (line) + " after r hit end of line", line);
                }         
            case ('m'):
                return getMult();
            case ('a'):
                return getAdd();
            case ('n'):
                return getNOP();
            case ('o'):
                return getOutput();
            case ('='):
                return getInto();
            case (','):
                return new CatLex(OpCategory.COMMA.getValue(), ",", line);
            case ('/'):
                return getComment();
            default:
                if ((int)character >= 48 && (int)character <= 57) {
                    return getConstant("" + character);
                }
                else {
                    readptr = lineLen + 1;
                    return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": " + character + " is not a valid token", line);
                }         
        }
    }

    public void nextLine() {
        this.readptr = lineLen + 1;
    }

    /**
     * Got st from nextToken. Continue to find the rest of the op code
     * @return
     * @throws IOException
     */
    public CatLex getStore() throws IOException{
        char character;
        if (readptr < lineLen - 3) {
            character = lineBuff.charAt(readptr);
            readptr++;
            if (character == 'o') {
                character = lineBuff.charAt(readptr);
                readptr++;
                if (character == 'r') {
                    character = lineBuff.charAt(readptr);
                    readptr++;
                    if (character == 'e') {
                        character = lineBuff.charAt(readptr);
                        readptr++;
                        if (character == ' ' || character == '\t') {
                            return new CatLex(OpCategory.MEMOP.getValue(), "store", line);
                        }
                        else {
                            readptr = lineLen + 1;
                            return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": op STORE must be followed by whitespace", line);
                        }
                    }
                    else {
                        readptr = lineLen + 1;
                        return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected e but received \"" + character + "\"", line);
                    }
                }
                else {
                    readptr = lineLen + 1;
                    return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected r but received \"" + character + "\"", line);
                }
            }
            else {
                readptr = lineLen + 1;
                return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected o but received \"" + character + "\"", line);
            }
        }
        else {
            readptr = lineLen + 1;
            return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": given st, reached end of line" , line);
        }
    }

    /**
     * Got su from nextToken.
     * @return
     * @throws IOException
     */
    public CatLex getSub() throws IOException{
        char character;
        if (readptr < lineLen - 1) {
            character = lineBuff.charAt(readptr);
            readptr++;
            if (character == 'b') {
                character = lineBuff.charAt(readptr);
                readptr++;
                if (character == ' ' || character == '\t') {
                    return new CatLex(OpCategory.ARITHOP.getValue(), "sub", line);
                }
                else {
                    readptr = lineLen + 1;
                    return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": op SUB must be followed by whitespace", line);
                }
            }
            else {
                readptr = lineLen + 1;
                return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected b but received \"" + character + "\"", line);
            }
        }
        else {
            readptr = lineLen + 1;
            return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": given su, reached end of file" , line);
        }
    }

    /**
     * Got ls from nextToken. 
     * @param isLeft true if it is lshift, false if it is rshift
     * @return
     * @throws IOException
     */
    public CatLex getShift(boolean isLeft) throws IOException{
        char character;
        if (readptr < lineLen - 4) {
            character = lineBuff.charAt(readptr);
            readptr++;
            if (character == 'h') {
                character = lineBuff.charAt(readptr);
                readptr++;
                if (character == 'i') {
                    character = lineBuff.charAt(readptr);
                    readptr++;
                    if (character == 'f') {
                        character = lineBuff.charAt(readptr);
                        readptr++;
                        if (character == 't') {
                            character = lineBuff.charAt(readptr);
                            readptr++;
                            if (character == ' ' || character == '\t') {
                                if (isLeft) {
                                    return new CatLex(OpCategory.ARITHOP.getValue(), "lshift", line);
                                }
                                return new CatLex(OpCategory.ARITHOP.getValue(), "rshift", line);
                            }
                            else {
                                readptr = lineLen + 1;
                                return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": op LSHIFT must be followed by whitespace", line);
                            }
                        }
                        else {
                            readptr = lineLen + 1;
                            return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected t but received \"" + character + "\"", line);
                        }
                    }
                    else {
                        readptr = lineLen + 1;
                        return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected f but received \"" + character + "\"", line);
                    }
                }
                else {
                    readptr = lineLen + 1;
                    return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected i but received \"" + character + "\"", line);
                }
            }
            else {
                readptr = lineLen + 1;
                return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected h but received \"" + character + "\"", line);
            }
        }
        else {
            readptr = lineLen + 1;
            return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": given ls or rs, reached end of line" , line);
        }
    }

    /**
     * Got m from nextToken
     * @return
     * @throws IOException
     */
    public CatLex getMult() throws IOException{
        char character;
        if (readptr < lineLen - 3) {
            character = lineBuff.charAt(readptr);
            readptr++;
            if (character == 'u') {
                character = lineBuff.charAt(readptr);
                readptr++;
                if (character == 'l') {
                    character = lineBuff.charAt(readptr);
                    readptr++;
                    if (character == 't') {
                        character = lineBuff.charAt(readptr);
                        readptr++;
                        if (character == ' ' || character == '\t') {
                            return new CatLex(OpCategory.ARITHOP.getValue(), "mult", line);
                         }
                        else {
                            readptr = lineLen + 1;
                            return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": op MULT must be followed by whitespace", line);
                        }
                    }
                    else {
                        readptr = lineLen + 1;
                        return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected t but received \"" + character + "\"", line); 
                    }
                }
                else {
                    readptr = lineLen + 1;
                    return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected l but received \"" + character + "\"", line);
                }
            }
            else {
                readptr = lineLen + 1;
                return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected u but received \"" + character + "\"", line);
            }
        }
        else {
            readptr = lineLen + 1;
            return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": given m, reached end of file" , line);
        }
    }

    /**
     * Got a from nextToken
     * @return
     * @throws IOException
     */
    public CatLex getAdd() throws IOException{
        char character;
        if (readptr < lineLen - 2) {
            character = lineBuff.charAt(readptr);
            readptr++;
            if (character == 'd') {
                character = lineBuff.charAt(readptr);
                readptr++;
                if (character == 'd') {
                    character = lineBuff.charAt(readptr);
                    readptr++;
                    if (character == ' ' || character == '\t') {
                        return new CatLex(OpCategory.ARITHOP.getValue(), "add", line);
                    }
                    else {
                        readptr = lineLen + 1;
                        return new CatLex(OpCategory.ERROR.getValue(), "ERROR " +  line + ": op ADD must be followed by whitespace", line);
                    }
                }
                else {
                    readptr = lineLen + 1;
                    return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected d but received \"" + character + "\"", line); 
                }
            }
            else {
                readptr = lineLen + 1;
                return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected d but received \"" + character + "\"", line);
            }
        }
        else {
            readptr = lineLen + 1;
            return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": given a, reached end of line" , line);
        }
    }

    /**
     * Got n from nextToken
     * @return
     * @throws IOException
     */
    public CatLex getNOP() throws IOException{
        char character;
        if (readptr < lineLen - 1) {
            character = lineBuff.charAt(readptr);
            readptr++;
            if (character == 'o') {
                character = lineBuff.charAt(readptr);
                readptr++;
                if (character == 'p') {
                    // Don't need a space after nop
                    return new CatLex(OpCategory.NOP.getValue(), "nop", line);
                }
                else {
                    readptr = lineLen + 1;
                    return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected p but received \"" + character + "\"", line); 
                }
            }
            else {
                readptr = lineLen + 1;
                return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected o but received \"" + character + "\"", line);
            }
        }
        else {
            readptr = lineLen + 1;
            return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": given n, reached end of line" , line);
        }
    }

    /**
     * Got o from nextToken
     * @return
     * @throws IOException
     */
    public CatLex getOutput() throws IOException{
        char character;
        if (readptr < lineLen - 5) {
            character = lineBuff.charAt(readptr);
            readptr++;
            if (character == 'u') {
                character = lineBuff.charAt(readptr);
                readptr++;
                if (character == 't') {
                    character = lineBuff.charAt(readptr);
                    readptr++;
                    if (character == 'p') {
                        character = lineBuff.charAt(readptr);
                        readptr++;
                        if (character == 'u') {
                            character = lineBuff.charAt(readptr);
                            readptr++;
                            if (character == 't') {
                                character = lineBuff.charAt(readptr);
                                readptr++;
                                if (character == ' ' || character == '\t') {
                                    return new CatLex(OpCategory.OUTPUT.getValue(), "output", line);
                                }
                                else {
                                    readptr = lineLen + 1;
                                    return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": op OUTPUT must be followed by whitespace", line);
                                }
                            }
                            else {
                                readptr = lineLen + 1;
                                return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected u but received \"" + character + "\"", line);
                            }
                        }
                        else {
                            readptr = lineLen + 1;
                            return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected u but received \"" + character + "\"", line);
                        }
                    }
                    else {
                        readptr = lineLen + 1;
                        return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected p but received \"" + character + "\"", line);
                    }
                }
                else {
                    readptr = lineLen + 1;
                    return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected t but received \"" + character + "\"", line);
                }
            }
            else {
                readptr = lineLen + 1;
                return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected u but received \"" + character + "\"", line);
            }
        }
        else {
            readptr = lineLen + 1;
            return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": given o, reached end of line" , line);
        }
    }

    /**
     * Got = from nextToken
     * @return
     * @throws IOException
     */
    public CatLex getInto() throws IOException{
        char character;
        if (readptr < lineLen) {
            character = lineBuff.charAt(readptr);
            readptr++;
            if (character == '>') {
                return new CatLex(OpCategory.INTO.getValue(), "=>", line);
            }
            else {
                readptr = lineLen + 1;
                return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": expected > but received \"" + character + "\"", line);
            }
        }
        else {
            readptr = lineLen + 1;
            return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": given =, reached end of file" , line);
        }
    }

    /**
     * Got r and the first integer from 
     * @return
     * @throws IOException
     */
    public CatLex getRegister(String lexeme) throws IOException{
        char character;
        while(readptr < lineLen) {
            character = lineBuff.charAt(readptr);
            readptr++;
            if ((int)character >= 48 && (int)character <= 57) {
                lexeme = lexeme + character;
            }
            else {
                lastChar = character;
                return new CatLex(OpCategory.REGISTER.getValue(), lexeme, line);
            }
        }
        //System.out.println("register readptr: " + readptr + " lineLen:" + lineLen);
        readptr = lineLen;
        return new CatLex(OpCategory.REGISTER.getValue(), lexeme, line);
    }

    /**
     * Got the first integer from nextToken
     * @return
     * @throws IOException
     */
    public CatLex getConstant(String lexeme) throws IOException{
        char character;
        while(readptr < lineLen) {
            character = lineBuff.charAt(readptr);
            readptr++;
            if ((int)character >= 48 && (int)character <= 57) {
                lexeme = lexeme + character;
            }
            else {
                lastChar = character;
                return new CatLex(OpCategory.CONSTANT.getValue(), lexeme, line);
            }
        }
        readptr = lineLen;
        return new CatLex(OpCategory.CONSTANT.getValue(), lexeme, line);
    }

    /**
     * Got first / from nextToken
     * @return
     * @throws IOException
     */
    public CatLex getComment() throws IOException{
        char character;
        if (readptr < lineLen) {
            character = lineBuff.charAt(readptr);
            readptr++;
            if (character == '/') {
                // skip the rest of the line because it's in a comment
                readptr = lineLen + 1;
                //System.out.println("Comment end of line");
                return new CatLex(OpCategory.EOL.getValue(), "//", line);
            }
            else {
                readptr = lineLen + 1;
                return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": was expecting / but received \"" + character + "\"", line);
            }
        }
        else {
            readptr = lineLen + 1;
            return new CatLex(OpCategory.ERROR.getValue(), "ERROR " + line + ": given /, reached end of line", line);
        }
    }
    
    /**
     * Moves the readptr if there is white space
     * @return returns the char you end up on after whitespace. Returns null character if the file ends
     */
    public char whiteSpaceFinder() throws IOException{
        //System.out.println("whiteSpaceFinder");
        char currCharacter = ' ';
        while(currCharacter == ' ' || currCharacter == '\t') {
            if (readptr < lineLen) {
                //System.out.println("Read from buffReader");
                currCharacter = lineBuff.charAt(readptr);
                readptr++;
                //System.out.println("curr character: " + currCharacter);
            }
            else {
                currCharacter = 0;
                break;
            }
            //System.out.println("curr character is : " + currCharacter);
        }
        return currCharacter;
    }

}
