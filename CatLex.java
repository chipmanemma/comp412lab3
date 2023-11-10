/*
 * A class to represent the return value of the Scanner
 */
public class CatLex {
    private int opCategory;
    private String lexeme;
    private int line;

    public CatLex(int opCategory, String lexeme, int line) {
        //System.out.println("before lexeme: " + this.lexeme);
        //System.out.println("param: " + lexeme.toCharArray());
        this.opCategory = opCategory;
        this.lexeme = String.copyValueOf(lexeme.toCharArray());
        //System.out.println("after lexeme: " + this.lexeme);
        this.line = line;
    }

    public void setLexeme(String lex) {
        this.lexeme = lex;
    }

    public int getOpCategory() {
        return this.opCategory;
    }

    public String getLexeme() {
        //System.out.println("Lexeme: " + this.lexeme);
        return this.lexeme;
    }

    public int getLine() {
        return this.line;
    }
}
