package uk.oczadly.karl.nanopowbench;

import uk.oczadly.karl.nanopowbench.util.Util;

import java.util.Objects;

public class Difficulty implements Comparable<Difficulty> {

    public static final long DIFF_V2_RECEIVE = 0xfffffe0000000000L;
    public static final long DIFF_V2_SEND = 0xfffffff800000000L;
    public static final long DIFF_V1 = 0xffffffc000000000L;

    private final long uval;

    public Difficulty(long uval) {
        this.uval = uval;
    }


    public long asLong() {
        return uval;
    }

    public String getLabel() {
        if (uval == DIFF_V2_RECEIVE) {
            return "receive";
        } else if (uval == DIFF_V2_SEND) {
            return "send";
        } else if (uval == DIFF_V1) {
            return "legacy";
        }
        return null;
    }

    public String asHex() {
        return Util.leftPadString(Long.toHexString(uval), 16, '0');
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(asHex());
        if (getLabel() != null) {
            sb.append(" [").append(getLabel()).append(']');
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
        return new Difficulty(Long.parseUnsignedLong(hex, 16));
    }

}
