package org.kynesys.ksscripting.types;

import org.kynesys.foundation.v1.interfaces.KSScriptingExecutable;
import org.kynesys.foundation.v1.sharedobj.KSExecutionSession;

public class True implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Bool.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        return true;
    }
}
