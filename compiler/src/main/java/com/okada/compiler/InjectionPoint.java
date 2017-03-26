package com.okada.compiler;


public class InjectionPoint {

    private static final String INJECTION = "    activity.%s = (%s) activity.findViewById(%s);";

    private final String variableName;
    private final String type;
    private final int value;

    InjectionPoint(String variableName, String type, int value) {
        this.variableName = variableName;
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format(INJECTION, variableName, type, value);
    }
}
