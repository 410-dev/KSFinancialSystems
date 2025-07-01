package org.kynesys.kstraderapi.v1.indicator;

import org.kynesys.kstraderapi.v1.objects.Chart;

import java.util.HashMap;

public interface KSComplexIndicatorComputer {
    HashMap<String, Object> computeComplex(Chart chart, HashMap<String, Object> parameters);
}
