package uk.oczadly.karl.nanopowbench;

/**
 * @author Karl Oczadly
 */
public enum MetricPrefix {
    
    PETA  (1e15, 'P'),
    TERA  (1e12, 'T'),
    GIGA  (1e9,  'G'),
    MEGA  (1e6,  'M'),
    KILO  (1e3,  'k'),
    BASE  (1d,   null),
    MILLI (1e-3, 'm'),
    MICRO (1e-6, 'μ'),
    NANO  (1e-9, 'n');
    
    
    private final double value;
    private final Character symbol;
    
    MetricPrefix(double value, Character symbol) {
        this.value = value;
        this.symbol = symbol;
    }
    
    
    public Character getSymbol() {
        return symbol;
    }
    
    public double convert(double val) {
        return val / value;
    }
    
    public String format(double val, String unit) {
        return String.format("%.3f %s%s", convert(val), getSymbol() != null ? getSymbol() : "", unit);
    }
    
    
    public static MetricPrefix getSuitable(double val, boolean useHigher) {
        for (MetricPrefix unit : values()) {
            if ((useHigher || unit.value <= 1d) && val > unit.value)
                return unit;
        }
        return NANO;
    }
    
    public static String format(double val, String unit, boolean useHigher) {
        return getSuitable(val, useHigher).format(val, unit);
    }
    
}
