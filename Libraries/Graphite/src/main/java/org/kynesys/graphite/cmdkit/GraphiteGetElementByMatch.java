package org.kynesys.graphite.cmdkit;


import org.kynesys.foundation.v1.interfaces.KSScriptingExecutable;
import org.kynesys.foundation.v1.sharedobj.KSExecutionSession;

public class GraphiteGetElementByMatch implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Object.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        // Usage example: GraphiteGetElementByMatch {GPGenericWindow Object} Type=javax.swing.JButton FilterBy=getText
        return null;
    }
}
