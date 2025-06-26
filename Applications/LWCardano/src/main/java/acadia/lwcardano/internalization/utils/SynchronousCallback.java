package acadia.lwcardano.internalization.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class SynchronousCallback {

    private ArrayList<Object> responses;
    private double latency = 0.01;
    private long timeout = 5;
    private boolean printOnCapture = false;

    public SynchronousCallback() {
        responses = new ArrayList<>();
    }

    public void callbackCapture(Object response) {
        responses.add(response);
        if (printOnCapture) {
            System.out.println("Response captured: " + response);
        }
    }

    public SynchronousCallback sync(int minimumSize) {
        try {
            long time = 0;
            while (responses.size() < minimumSize && time < timeout * 1000L) {
                Thread.sleep((long) (latency * 1000));
                time += (long) (latency * 1000L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }

    public SynchronousCallback sync() {
        return sync(1);
    }

    public Object getLastResponse() {
        if (!responses.isEmpty()) {
            return responses.getLast();
        }
        System.out.println("Warning: No response captured.");
        return null;
    }
}
