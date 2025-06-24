package kstradermachine.objects;

import kstradermachine.subwins.SystemLogs;
import lombok.Getter;
import org.kynesys.foundation.v1.interfaces.KSJournalingService;

@Getter
public class JournalingAgent implements KSJournalingService {

    private String namespace;

    public JournalingAgent(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public void log(String status, String message) {
        SystemLogs.log(status, message);
        System.out.println("[" + status + "] " + message);
        // TODO Add file journaling
    }
}
