package io.github.snowdrop.jester.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.client.utils.Serialization;

public final class SerializationUtils {

    private static final String DOCUMENT_DELIMITER = "---";

    private SerializationUtils() {

    }

    public static KubernetesList unmarshalAsList(String content) {
        String[] parts = splitDocument(content);
        List<HasMetadata> items = new ArrayList<>();
        for (String part : parts) {
            if (part.trim().isEmpty()) {
                continue;
            }
            Object resource = Serialization.unmarshal(part);
            if (resource instanceof KubernetesList) {
                items.addAll(((KubernetesList) resource).getItems());
            } else if (resource instanceof HasMetadata) {
                items.add((HasMetadata) resource);
            } else if (resource instanceof HasMetadata[]) {
                Arrays.stream((HasMetadata[]) resource).forEach(r -> items.add(r));
            }
        }
        return new KubernetesListBuilder().withItems(items).build();
    }

    private static String[] splitDocument(String aSpecFile) {
        List<String> documents = new ArrayList<>();
        String[] lines = aSpecFile.split("\\r?\\n");
        int nLine = 0;
        StringBuilder builder = new StringBuilder();

        while (nLine < lines.length) {
            if ((lines[nLine].length() >= DOCUMENT_DELIMITER.length()
                    && !lines[nLine].substring(0, DOCUMENT_DELIMITER.length()).equals(DOCUMENT_DELIMITER))
                    || (lines[nLine].length() < DOCUMENT_DELIMITER.length())) {
                builder.append(lines[nLine] + System.lineSeparator());
            } else {
                documents.add(builder.toString());
                builder.setLength(0);
                // To have meaningful line numbers, in jackson error messages, we need each resource
                // to retain its original position in the document.
                for (int i = 0; i <= nLine; i++) {
                    builder.append(System.lineSeparator());
                }
            }
            nLine++;
        }

        if (!builder.toString().isEmpty()) {
            documents.add(builder.toString());
        }

        return documents.toArray(new String[documents.size()]);
    }
}
