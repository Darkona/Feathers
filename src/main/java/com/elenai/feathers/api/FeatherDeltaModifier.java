package com.elenai.feathers.api;

import java.util.function.Function;

public class FeatherDeltaModifier {

    private int ordinal;

    private String name;
    private Function<Integer, Integer> fun = (i) -> i + 1;

}
