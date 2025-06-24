package org.kynesys.ksscripting.commands;

import org.kynesys.foundation.v1.interfaces.KSScriptingExecutable;
import org.kynesys.foundation.v1.sharedobj.KSExecutionSession;

public class GetItemFromMap implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return Object.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        if (args == null || args.length != 2) {
            throw new RuntimeException("GetItemFromMap requires 2 arguments");
        }
        Object map = args[0];
        Object key = args[1];
        if (map instanceof java.util.Map<?, ?>) {
            return ((java.util.Map<?, ?>) map).get(key);
        } else {
            throw new RuntimeException("GetItemFromMap requires a Map as the first argument");
        }
    }
}
