package com.kleen.selector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.jsoup.helper.StringUtil;
import org.jsoup.helper.Validate;
/**
 * parse json
 * @author kleen888@gmail.com
 * @since 0.1.0
 */
public class XTokenQueue {
    private String queue;
    private int pos = 0;
    private static final char ESC = '\\';
    private static final String[] quotes = new String[]{"\"", "\'"};
    private static final char singleQuote = '\'';
    private static final char doubleQuote = '\"';

    public XTokenQueue(String data) {
        Validate.notNull(data);
        this.queue = data;
    }

    public boolean isEmpty() {
        return this.remainingLength() == 0;
    }

    private int remainingLength() {
        return this.queue.length() - this.pos;
    }

    public char peek() {
        return this.isEmpty()?'\u0000':this.queue.charAt(this.pos);
    }

    public void addFirst(Character c) {
        this.addFirst(c.toString());
    }

    public void addFirst(String seq) {
        this.queue = seq + this.queue.substring(this.pos);
        this.pos = 0;
    }

    public boolean matches(String seq) {
        return this.queue.regionMatches(true, this.pos, seq, 0, seq.length());
    }

    public boolean matchesRegex(String seq) {
        return Pattern.matches(seq, this.queue.substring(this.pos));
    }

    public boolean matchesCS(String seq) {
        return this.queue.startsWith(seq, this.pos);
    }

    public boolean matchesAny(String... seq) {
        String[] arr$ = seq;
        int len$ = seq.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String s = arr$[i$];
            if(this.matches(s)) {
                return true;
            }
        }

        return false;
    }

    public boolean matchesAny(char... seq) {
        if(this.isEmpty()) {
            return false;
        } else {
            char[] arr$ = seq;
            int len$ = seq.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                char c = arr$[i$];
                if(this.queue.charAt(this.pos) == c) {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean matchesStartTag() {
        return this.remainingLength() >= 2 && this.queue.charAt(this.pos) == 60 && Character.isLetter(this.queue.charAt(this.pos + 1));
    }

    public boolean matchChomp(String seq) {
        if(this.matches(seq)) {
            this.pos += seq.length();
            return true;
        } else {
            return false;
        }
    }

    public boolean matchesWhitespace() {
        return !this.isEmpty() && StringUtil.isWhitespace(this.queue.charAt(this.pos));
    }

    public boolean matchesWord() {
        return !this.isEmpty() && Character.isLetterOrDigit(this.queue.charAt(this.pos));
    }

    public void advance() {
        if(!this.isEmpty()) {
            ++this.pos;
        }

    }

    public char consume() {
        return this.queue.charAt(this.pos++);
    }

    public void consume(String seq) {
        if(!this.matches(seq)) {
            throw new IllegalStateException("Queue did not match expected sequence");
        } else {
            int len = seq.length();
            if(len > this.remainingLength()) {
                throw new IllegalStateException("Queue not long enough to consume sequence");
            } else {
                this.pos += len;
            }
        }
    }

    public String consumeTo(String seq) {
        int offset = this.queue.indexOf(seq, this.pos);
        if(offset != -1) {
            String consumed = this.queue.substring(this.pos, offset);
            this.pos += consumed.length();
            return consumed;
        } else {
            return this.remainder();
        }
    }

    public String consumeToIgnoreCase(String seq) {
        int start = this.pos;
        String first = seq.substring(0, 1);
        boolean canScan = first.toLowerCase().equals(first.toUpperCase());

        while(!this.isEmpty() && !this.matches(seq)) {
            if(canScan) {
                int data = this.queue.indexOf(first, this.pos) - this.pos;
                if(data == 0) {
                    ++this.pos;
                } else if(data < 0) {
                    this.pos = this.queue.length();
                } else {
                    this.pos += data;
                }
            } else {
                ++this.pos;
            }
        }

        String var6 = this.queue.substring(start, this.pos);
        return var6;
    }

    public String consumeToAny(String... seq) {
        int start;
        for(start = this.pos; !this.isEmpty() && !this.matchesAny(seq); ++this.pos) {
            ;
        }

        String data = this.queue.substring(start, this.pos);
        return data;
    }

    public String consumeAny(String... seq) {
        String[] arr$ = seq;
        int len$ = seq.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String s = arr$[i$];
            if(this.matches(s)) {
                this.pos += s.length();
                return s;
            }
        }

        return "";
    }

    public String chompTo(String seq) {
        String data = this.consumeTo(seq);
        this.matchChomp(seq);
        return data;
    }

    public String chompToIgnoreCase(String seq) {
        String data = this.consumeToIgnoreCase(seq);
        this.matchChomp(seq);
        return data;
    }

    public String chompBalancedQuotes() {
        String quote = this.consumeAny(quotes);
        if(quote.length() == 0) {
            return "";
        } else {
            StringBuilder accum = new StringBuilder(quote);
            accum.append(this.consumeToUnescaped(quote));
            accum.append(this.consume());
            return accum.toString();
        }
    }

    public String chompBalancedNotInQuotes(char open, char close) {
        StringBuilder accum = new StringBuilder();
        int depth = 0;
        char last = 0;
        boolean inQuotes = false;
        Character quote = null;

        while(!this.isEmpty()) {
            Character c = Character.valueOf(this.consume());
            if(last == 0 || last != 92) {
                if(!inQuotes) {
                    if(!c.equals(Character.valueOf('\'')) && !c.equals(Character.valueOf('\"'))) {
                        if(c.equals(Character.valueOf(open))) {
                            ++depth;
                        } else if(c.equals(Character.valueOf(close))) {
                            --depth;
                        }
                    } else {
                        inQuotes = true;
                        quote = c;
                    }
                } else if(c.equals(quote)) {
                    inQuotes = false;
                }
            }

            if(depth > 0 && last != 0) {
                accum.append(c);
            }

            last = c.charValue();
            if(depth <= 0) {
                break;
            }
        }

        return accum.toString();
    }

    public String chompBalanced(char open, char close) {
        StringBuilder accum = new StringBuilder();
        int depth = 0;
        char last = 0;

        while(!this.isEmpty()) {
            Character c = Character.valueOf(this.consume());
            if(last == 0 || last != 92) {
                if(c.equals(Character.valueOf(open))) {
                    ++depth;
                } else if(c.equals(Character.valueOf(close))) {
                    --depth;
                }
            }

            if(depth > 0 && last != 0) {
                accum.append(c);
            }

            last = c.charValue();
            if(depth <= 0) {
                break;
            }
        }

        return accum.toString();
    }

    public static String unescape(String in) {
        StringBuilder out = new StringBuilder();
        char last = 0;
        char[] arr$ = in.toCharArray();
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            char c = arr$[i$];
            if(c == 92) {
                if(last != 0 && last == 92) {
                    out.append(c);
                }
            } else {
                out.append(c);
            }

            last = c;
        }

        return out.toString();
    }

    public boolean consumeWhitespace() {
        boolean seen;
        for(seen = false; this.matchesWhitespace(); seen = true) {
            ++this.pos;
        }

        return seen;
    }

    public String consumeWord() {
        int start;
        for(start = this.pos; this.matchesWord(); ++this.pos) {
            ;
        }

        return this.queue.substring(start, this.pos);
    }

    public String consumeTagName() {
        int start;
        for(start = this.pos; !this.isEmpty() && (this.matchesWord() || this.matchesAny(new char[]{':', '_', '-'})); ++this.pos) {
            ;
        }

        return this.queue.substring(start, this.pos);
    }

    public String consumeElementSelector() {
        int start;
        for(start = this.pos; !this.isEmpty() && (this.matchesWord() || this.matchesAny(new char[]{'|', '_', '-'})); ++this.pos) {
            ;
        }

        return this.queue.substring(start, this.pos);
    }

    public void unConsume(int length) {
        Validate.isTrue(length <= this.pos, "length " + length + " is larger than consumed chars " + this.pos);
        this.pos -= length;
    }

    public void unConsume(String word) {
        this.unConsume(word.length());
    }

    public String consumeCssIdentifier() {
        int start;
        for(start = this.pos; !this.isEmpty() && (this.matchesWord() || this.matchesAny(new char[]{'-', '_'})); ++this.pos) {
            ;
        }

        return this.queue.substring(start, this.pos);
    }

    public String consumeAttributeKey() {
        int start;
        for(start = this.pos; !this.isEmpty() && (this.matchesWord() || this.matchesAny(new char[]{'-', '_', ':'})); ++this.pos) {
            ;
        }

        return this.queue.substring(start, this.pos);
    }

    public String remainder() {
        StringBuilder accum = new StringBuilder();

        while(!this.isEmpty()) {
            accum.append(this.consume());
        }

        return accum.toString();
    }

    public String toString() {
        return this.queue.substring(this.pos);
    }

    public boolean containsAny(String... seq) {
        String[] arr$ = seq;
        int len$ = seq.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String s = arr$[i$];
            if(this.queue.contains(s)) {
                return true;
            }
        }

        return false;
    }

    public static String trimQuotes(String str) {
        Validate.isTrue(str != null && str.length() > 0);
        String quote = str.substring(0, 1);
        if(StringUtil.in(quote, new String[]{"\"", "\'"})) {
            Validate.isTrue(str.endsWith(quote), "Quote for " + str + " is incomplete!");
            str = str.substring(1, str.length() - 1);
        }

        return str;
    }

    public static List<String> trimQuotes(List<String> strs) {
        Validate.isTrue(strs != null);
        ArrayList list = new ArrayList();
        Iterator i$ = strs.iterator();

        while(i$.hasNext()) {
            String str = (String)i$.next();
            list.add(trimQuotes(str));
        }

        return list;
    }

    public String consumeToUnescaped(String str) {
        String s = this.consumeToAny(new String[]{str});
        if(s.length() > 0 && s.charAt(s.length() - 1) == 92) {
            s = s + this.consume();
            s = s + this.consumeToUnescaped(str);
        }

        Validate.isTrue(this.pos < this.queue.length(), "Unclosed quotes! " + this.queue);
        return s;
    }

    public List<String> parseFuncionParams() {
        ArrayList params = new ArrayList();
        StringBuilder accum = new StringBuilder();

        while(!this.isEmpty()) {
            this.consumeWhitespace();
            if(this.matchChomp(",")) {
                params.add(accum.toString());
                accum = new StringBuilder();
            } else if(this.matchesAny(quotes)) {
                String quoteUsed = this.consumeAny(quotes);
                accum.append(quoteUsed);
                accum.append(this.consumeToUnescaped(quoteUsed));
                accum.append(this.consume());
            } else {
                accum.append(this.consumeToAny(new String[]{"\"", "\'", ","}));
            }
        }

        if(accum.length() > 0) {
            params.add(accum.toString());
        }

        return params;
    }

    public static List<String> parseFuncionParams(String paramStr) {
        XTokenQueue tq = new XTokenQueue(paramStr);
        return tq.parseFuncionParams();
    }
}
