package io.github.snowdrop.jester.resources.localproject;

import io.github.snowdrop.jester.core.ServiceContext;
import io.github.snowdrop.jester.utils.Command;
import io.github.snowdrop.jester.utils.DockerUtils;

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
