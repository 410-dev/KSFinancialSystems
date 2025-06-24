package org.kynesys.ksscripting.commands;

import org.kynesys.ksscripting.objects.KSScriptingNull;
import org.kynesys.foundation.v1.interfaces.KSScriptingExecutable;
import org.kynesys.foundation.v1.sharedobj.KSExecutionSession;

public class Exit implements KSScriptingExecutable {
    @Override
    public String returnType() {
        return KSScriptingNull.class.getName();
    }

    @Override
    public Object execute(Object[] args, KSExecutionSession session) throws Exception {
        session.setSessionTerminated(true);
        session.setTerminatingValue(args.length > 0 ? args[0] : new KSScriptingNull());
        return session.getTerminatingValue();
    }
}
