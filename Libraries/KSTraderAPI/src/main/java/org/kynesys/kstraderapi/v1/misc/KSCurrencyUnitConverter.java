package org.kynesys.kstraderapi.v1.misc;

import java.util.function.UnaryOperator;

public interface KSCurrencyUnitConverter extends UnaryOperator<Double> {

    String getFrom();
    String getTo();

}
