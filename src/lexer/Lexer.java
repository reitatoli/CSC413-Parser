/**
 *
 * @author Bradley SomathiNg
 */
package lexer;

public class Lexer {

    private boolean atEOF = false;
    private char ch;     // next character to process
    private SourceReader source;
    // positions in line of current token
    private int startPosition, endPosition;

    public Lexer(String sourceFile) throws Exception {
        new TokenType();  // init token table
        source = new SourceReader(sourceFile);
        ch = source.read();
    }

    public static void main(String[] args) {
        Token tok;
        try {
            Lexer lex = new Lexer(args[0]);

            while (true) {
                tok = lex.nextToken();
                String tokenType = TokenType.tokens.get(tok.getKind()) + "";
                String p = "L: " + tok.getLeftPosition()
                        + " R: " + tok.getRightPosition() + "  "
                        + TokenType.tokens.get(tok.getKind()) + " ";
                if ((tok.getKind() == Tokens.Identifier)
                         || (tok.getKind() == Tokens.INTeger)
                        || (tok.getKind() == Tokens.FLOAT)) {
                    tokenType = tok.toString();
                }
                System.out.printf("%-12s%s%n", tokenType, p);
            }
        } catch (Exception e) {
        }
        String buffer =SourceReader.printCode.substring(4);//gets rid of unnecessary characters that would effect result print
        System.out.println(buffer);//prints the whole code with lines numbered

    }

    public Token newIdToken(String id, int startPosition, int endPosition) {
        return new Token(startPosition, endPosition, Symbol.symbol(id, Tokens.Identifier));
    }



    public Token newNumberToken(String number, int startPosition, int endPosition) {
        return new Token(startPosition, endPosition,
                Symbol.symbol(number, Tokens.INTeger));
    }

    public Token newFloatToken(String number, int startPosition, int endPosition) {
        return new Token(startPosition, endPosition,
                Symbol.symbol(number, Tokens.FLOAT));
    }

    public Token makeToken(String s, int startPosition, int endPosition) {
        if (s.equals("//")) {  // filter comment
            try {
                int oldLine = source.getLineno();
                do {
                    ch = source.read();
                } while (oldLine == source.getLineno());
            } catch (Exception e) {
                atEOF = true;
            }
            return nextToken();
        }
        Symbol sym = Symbol.symbol(s, Tokens.BogusToken); // be sure it's a valid token
        if (sym == null) {
            System.out.println("******** illegal character: " + s);
            atEOF = true;
            return nextToken();
        }
        return new Token(startPosition, endPosition, sym);
    }

    public Token nextToken() { // ch is always the next char to process
        if (atEOF) {
            if (source != null) {
                source.close();
                source = null;
            }
            return null;
        }
        try {
            while (Character.isWhitespace(ch)) {  // scan past whitespace
                ch = source.read();
            }
        } catch (Exception e) {
            atEOF = true;
            return nextToken();
        }
        startPosition = source.getPosition();
        endPosition = startPosition - 1;

        if (Character.isJavaIdentifierStart(ch)) {
            // return tokens for ids and reserved words
            String id = "";
            try {
                do {
                    endPosition++;
                    id += ch;
                    ch = source.read();
                } while (Character.isJavaIdentifierPart(ch));
            } catch (Exception e) {
                atEOF = true;
            }
            return newIdToken(id, startPosition, endPosition);
        }
        if (Character.isDigit(ch)) {
            int counter = 0; //counter to check to see if it ran into a . already
            // return number tokens
            String number = "";
            try {
                do {
                    number += ch;
                    ch = source.read();
                    endPosition++;
                    char temp = ch;

                    if (ch == '.' && counter == 0) {//if it runs into a . it collects the numbers behind it. This only happens if the counter is 0, to avoid making multiples float numbers one
                        number += ch;
                        counter++;
                        endPosition++;
                        ch = source.read();
                    }

                } while (Character.isDigit(ch) || ch == '+' || ch == '-');
            } catch (Exception e) {
                atEOF = true;
            }
            if (counter != 0) {//if the counter went up cause of the . checking it returns a float instead of a regular int
                return newFloatToken(number, startPosition, endPosition);
            } else {
                return newNumberToken(number, startPosition, endPosition);
            }
        }

        if (ch
                == '.') {//recognizes the . for floattoken
            String number = "";
            try {//increases size so numbers after the . are seen as floats
                do {

                    endPosition++;
                    number += ch;
                    ch = source.read();
                } while (Character.isDigit(ch));
            } catch (Exception e) {
                atEOF = true;
            }
            return newFloatToken(number, startPosition, endPosition);
        }

        // At this point the only tokens to check for are one or two
        // characters; we must also check for comments that begin with
        // 2 slashes
        String charOld = "" + ch;
        String op = charOld;
        Symbol sym;

        try {
            endPosition++;
            ch = source.read();
            op += ch;
            // check if valid 2 char operator; if it's not in the symbol
            // table then don't insert it since we really have a one char
            // token
            sym = Symbol.symbol(op, Tokens.BogusToken);
            if (sym == null) {  // it must be a one char token
                return makeToken(charOld, startPosition, endPosition);
            }
            endPosition++;
            ch = source.read();
            return makeToken(op, startPosition, endPosition);
        } catch (Exception e) {
        }
        atEOF = true;
        if (startPosition == endPosition) {
            op = charOld;
        }

        return makeToken(op, startPosition, endPosition);
    }
}
