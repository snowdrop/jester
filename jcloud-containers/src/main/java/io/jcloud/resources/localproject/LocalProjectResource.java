package io.jcloud.resources.localproject;

import io.jcloud.core.ServiceContext;
import io.jcloud.utils.Command;
import io.jcloud.utils.DockerUtils;

public class LocalProjectResource {

    private String generatedImage;

    public LocalProjectResource(ServiceContext context, String location, String[] buildCommands, String dockerfile) {
        if (buildCommands.length > 0) {
            try {
                new Command(buildCommands).onDirectory(location).runAndWait();
            } catch (Exception ex) {
                throw new RuntimeException("Error running build commands for service " + context.getName(), ex);
            }
        }

        // generate image
        generatedImage = DockerUtils.build(context, dockerfile, location);
    }

    public String getGeneratedImage() {
        return generatedImage;
    }
}
