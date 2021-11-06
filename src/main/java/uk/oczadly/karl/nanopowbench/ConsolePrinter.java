package uk.oczadly.karl.nanopowbench;

import uk.oczadly.karl.nanopowbench.util.Util;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ConsolePrinter {

    private final PrintStream ps;
    private final int indentSpaces, idealLength;

    public ConsolePrinter(PrintStream ps, int indentSpaces, int idealLength) {
        this.ps = ps;
        this.indentSpaces = indentSpaces;
        this.idealLength = idealLength;
    }


    public PrintStream getPrintStream() {
        return ps;
    }


    public void blankLine() {
        ps.println();
    }

    public void println(String str) {
        ps.println(str);
    }

    public void println(int indent, String str) {
        ps.println(pad(indent) + str);
    }

    public void printSeparator() {
        printSeparator(idealLength);
    }

    public void printSeparator(int width) {
        println(Util.repeatChar('=', width));
    }

    public void printTitle(String title) {
        ps.println(title + ":");
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
        int padLen = (calcWidth - text.length()) / 4;
        ps.println(pad(padLen) + text);
    }

    public void printParams(int indent, LinkedHashMap<String, String> paramMap) {
        String prepad = pad(indent);
        Set<Map.Entry<String, String>> params = paramMap.entrySet();
        int maxLen = params.stream()
                .map(Map.Entry::getKey)
                .mapToInt(String::length)
                .max().orElse(0);
        for (Map.Entry<String, String> param : params) {
            String padding = Util.repeatChar(' ', maxLen - param.getKey().length());
            ps.printf("%s%s:%s %s%n", prepad, param.getKey(), padding, param.getValue());
        }
    }

    private String pad(int indent) {
        return Util.repeatChar(' ', indent * indentSpaces);
    }

}
