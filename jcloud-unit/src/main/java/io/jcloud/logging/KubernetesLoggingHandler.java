package io.jcloud.logging;

import java.util.Map;
import java.util.Map.Entry;

import io.jcloud.api.Service;
import io.jcloud.api.clients.KubectlClient;
import io.jcloud.core.ServiceContext;
import io.jcloud.core.extensions.KubernetesExtensionBootstrap;

public class KubernetesLoggingHandler extends ServiceLoggingHandler {

    private final KubectlClient client;
    private final Service service;

    private Map<String, String> oldLogs;

    public KubernetesLoggingHandler(ServiceContext context) {
        super(context.getOwner());

        service = context.getOwner();
        client = context.get(KubernetesExtensionBootstrap.CLIENT);
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
