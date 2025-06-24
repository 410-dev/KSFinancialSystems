package org.kynesys.graphite.cmdkit;

import org.kynesys.foundation.v1.interfaces.KSScriptingExecutable;
import org.kynesys.foundation.v1.sharedobj.KSExecutionSession;

public class GraphiteInvoke implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return String.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        return null;
    }
}
