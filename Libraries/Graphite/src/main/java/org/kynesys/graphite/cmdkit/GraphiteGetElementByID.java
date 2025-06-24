package org.kynesys.graphite.cmdkit;

import org.kynesys.foundation.v1.interfaces.KSScriptingExecutable;
import org.kynesys.foundation.v1.sharedobj.KSExecutionSession;
import org.kynesys.graphite.v1.GPGenericWindow;

import java.awt.*;

public class GraphiteGetElementByID implements KSScriptingExecutable {

    @Override
    public String returnType() {
        return Object.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage example: GraphiteGetElementByID {GPGenericWindow Object} username
        // Usage        : GraphiteGetElementByID {GPGenericWindow Object} <element id>
        if (args == null || args.length != 2) {
            throw new RuntimeException("GraphiteGetElement requires 2 arguments: {GPGenericWindowObject} <element id>");
        }

        if (!(args[0] instanceof GPGenericWindow window)) {
            throw new RuntimeException("First argument must be a GPGenericWindow object.");
        }

        String elementId = (String) args[1];
        Component component = window.getComponentById(elementId);
        if (component == null) {
            throw new RuntimeException("Element with ID " + elementId + " not found in loaded program.");
        } else {
            return component;
        }
    }
}
