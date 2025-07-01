package org.kynesys.kstraderapi.v1.indicator;

import org.kynesys.kstraderapi.v1.objects.Candlestick;
import org.kynesys.kstraderapi.v1.objects.Chart;

import java.util.ArrayList;
import java.util.HashMap;

public interface DiscreteValueComputer {

    double compute(Chart chart, HashMap<String, Object> parameters);

}
