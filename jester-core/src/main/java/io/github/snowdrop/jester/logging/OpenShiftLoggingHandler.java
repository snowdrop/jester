package io.github.snowdrop.jester.logging;

import java.util.Map;
import java.util.Map.Entry;

import io.github.snowdrop.jester.api.Service;
import io.github.snowdrop.jester.api.clients.OpenshiftClient;
import io.github.snowdrop.jester.core.ServiceContext;
import io.github.snowdrop.jester.core.extensions.OpenShiftExtensionBootstrap;

public class OpenShiftLoggingHandler extends ServiceLoggingHandler {

    private final OpenshiftClient client;
    private final Service service;

    private Map<String, String> oldLogs;

    public OpenShiftLoggingHandler(ServiceContext context) {
        super(context.getOwner());

        service = context.getOwner();
        client = context.get(OpenShiftExtensionBootstrap.CLIENT);
    }

    @Override
    protected synchronized void handle() {
        Map<String, String> newLogs = client.logs(service);
        for (Entry<String, String> entry : newLogs.entrySet()) {
            onMapDifference(entry);
        }

        oldLogs = newLogs;
    }

    private void onMapDifference(Entry<String, String> entry) {
        String newPodLogs = formatPodLogs(entry.getKey(), entry.getValue());

        if (oldLogs != null && oldLogs.containsKey(entry.getKey())) {
            String oldPodLogs = formatPodLogs(entry.getKey(), oldLogs.get(entry.getKey()));

            onStringDifference(newPodLogs, oldPodLogs);
        } else {
            onLines(newPodLogs);
        }
    }

    private String formatPodLogs(String podName, String log) {
        return String.format("[%s] %s", podName, log);
    }

}
