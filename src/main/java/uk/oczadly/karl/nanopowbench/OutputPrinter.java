package uk.oczadly.karl.nanopowbench;

import uk.oczadly.karl.nanopowbench.util.Util;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class OutputPrinter {

    private final PrintWriter pw;
    private final int indentSpaces, idealLength;

    public OutputPrinter(PrintStream ps, int indentSpaces, int idealLength) {
        this.pw = new PrintWriter(ps, true);
        this.indentSpaces = indentSpaces;
        this.idealLength = idealLength;
    }


    public void blankLine() {
        pw.println();
    }

    public void println(String str) {
        pw.println(str);
    }

    public void println(int indent, String str) {
        pw.println(indent(indent) + str);
    }

    public void printSeparator() {
        printSeparator(idealLength);
    }

    public void printSeparator(int width) {
        println(Util.repeatChar('=', width));
    }

    public void printTitle(String title) {
        pw.println(title + ":");
    }

    public void printHeader(String... lines) {
        int maxLen = Arrays.stream(lines)
                .mapToInt(String::length)
                .max().orElse(0);
        int width = Math.max(maxLen, idealLength);
        printSeparator(width);
        for (String str : lines) {
            printCentered(str, width);
        }
        printSeparator(width);
    }

    public void printCentered(String text) {
        printCentered(text, idealLength);
    }

    public void printCentered(String text, int width) {
        int calcWidth = Math.max(text.length(), Math.max(idealLength, width));
        int padLen = (calcWidth - text.length()) / 2;
        pw.println(pad(padLen) + text);
    }

    public void printParams(int indent, LinkedHashMap<String, String> paramMap) {
        String prepad = indent(indent);
        Set<Map.Entry<String, String>> params = paramMap.entrySet();
        int maxLen = params.stream()
                .map(Map.Entry::getKey)
                .mapToInt(String::length)
                .max().orElse(0);
        for (Map.Entry<String, String> param : params) {
            String padding = Util.repeatChar(' ', maxLen - param.getKey().length());
            pw.printf("%s%s:%s %s%n", prepad, param.getKey(), padding, param.getValue());
        }
    }

    private String indent(int indent) {
        return pad(indent * indentSpaces);
    }

    private String pad(int chars) {
        return Util.repeatChar(' ', chars);
    }

}
