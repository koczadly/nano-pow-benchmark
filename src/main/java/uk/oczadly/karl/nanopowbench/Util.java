package uk.oczadly.karl.nanopowbench;

/**
 * @author Karl Oczadly
 */
public class Util {
    
    public static double ulongToDouble(long val) {
        double dval = (double)(val & 0x7fffffffffffffffL);
        if (val < 0) dval += 0x1p63;
        return dval;
    }
    
    public static String leftPadString(String str, int minLen, char padChar) {
        if (str.length() >= minLen) return str;
        StringBuilder sb = new StringBuilder(minLen);
        for (int i=0; i<(minLen-str.length()); i++)
            sb.append(padChar);
        sb.append(str);
        return sb.toString();
    }
    
}
