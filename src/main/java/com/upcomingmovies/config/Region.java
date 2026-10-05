package com.upcomingmovies.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public enum Region {
    US("US", "United States"),
    SE("SE", "Sweden"),
    GB("GB", "United Kingdom"),
    DE("DE", "Germany"),
    FR("FR", "France"),
    JP("JP", "Japan"),
    AU("AU", "Australia"),
    CA("CA", "Canada"),
    IT("IT", "Italy"),
    ES("ES", "Spain"),
    NL("NL", "Netherlands"),
    BR("BR", "Brazil"),
    IN("IN", "India"),
    KR("KR", "South Korea"),
    CN("CN", "China");

    private final String code;
    private final String countryName;

    private static final Map<String, Region> BY_CODE;

    static {
        Map<String, Region> map = new LinkedHashMap<>();
        for (Region region : values()) {
            map.put(region.code.toUpperCase(), region);
        }
        BY_CODE = Collections.unmodifiableMap(map);
    }

    Region(String code, String countryName) {
        this.code = code;
        this.countryName = countryName;
    }

    public String getCode() {
        return code;
    }

    public String getCountryName() {
        return countryName;
    }

    public static Optional<Region> fromCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_CODE.get(code.trim().toUpperCase()));
    }

    public static Map<String, Region> all() {
        return BY_CODE;
    }
}
