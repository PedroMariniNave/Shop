package com.zpedroo.voltzshop.utils.formatter;

import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;

public class NumberFormatter {

    private static NumberFormatter instance;
    public static NumberFormatter getInstance() { return instance; }

    private BigInteger THOUSAND = BigInteger.valueOf(1000);
    private NavigableMap<BigInteger, String> FORMATS = new TreeMap<>();
    private List<String> NAMES = new LinkedList<>();

    public NumberFormatter(FileConfiguration file) {
        instance = this;
        NAMES.addAll(file.getStringList("NumberFormatter"));

        for (int i = 0; i < NAMES.size(); i++) {
            FORMATS.put(THOUSAND.pow(i+1), NAMES.get(i));
        }
    }

    public String format(BigInteger value) {
        Map.Entry<BigInteger, String> entry = FORMATS.floorEntry(value);
        if (entry == null) return value.toString();

        BigInteger key = entry.getKey();
        BigInteger divide = key.divide(THOUSAND);
        BigInteger divide1 = value.divide(divide);
        float f = divide1.floatValue() / 1000f;
        float rounded = ((int)(f * 100))/100f;

        if (rounded % 1 == 0) return ((int) rounded) + "" + entry.getValue();

        return rounded + "" + entry.getValue();
    }

    public String formatDecimal(Double number) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        formatter.setDecimalFormatSymbols(symbols);
        return formatter.format(number);
    }
}