package uk.oczadly.karl.nanopowbench;

import uk.oczadly.karl.nanopowbench.util.Util;

import java.util.Objects;

public class Difficulty implements Comparable<Difficulty> {

    private final long uval;
    private final String label;

    public Difficulty(long uval, String label) {
        this.uval = uval;
        this.label = label;
    }


    public long asLong() {
        return uval;
    }

    public String getLabel() {
        return label;
    }

    public String asHex() {
        return Util.leftPadString(Long.toHexString(uval), 16, '0');
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(asHex());
        if (label != null) {
            sb.append(" [").append(label).append(']');
        }
        return sb.toString();
    }

    @Override
    public int compareTo(Difficulty o) {
        return Long.compareUnsigned(uval, o.uval);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Difficulty that = (Difficulty) o;
        return uval == that.uval;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uval);
    }


    public static Difficulty parseHex(String hex) {
        return new Difficulty(Long.parseUnsignedLong(hex, 16), null);
    }

}
