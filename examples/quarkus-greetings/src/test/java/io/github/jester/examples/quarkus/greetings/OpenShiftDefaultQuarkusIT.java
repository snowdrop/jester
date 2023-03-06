package io.github.jester.examples.quarkus.greetings;

import io.github.jester.api.OpenShiftServiceConfiguration;
import io.github.jester.api.RunOnOpenShift;

@RunOnOpenShift
@OpenShiftServiceConfiguration(forService = "quarkus", useRoute = false)
public class OpenShiftDefaultQuarkusIT extends DefaultQuarkusIT {
}
