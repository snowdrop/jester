package io.github.snowdrop.jester.examples.quarkus.greetings;

import io.github.snowdrop.jester.api.OpenShiftServiceConfiguration;
import io.github.snowdrop.jester.api.RunOnOpenShift;

@RunOnOpenShift
@OpenShiftServiceConfiguration(forService = "quarkus", useRoute = false)
public class OpenShiftDefaultQuarkusIT extends DefaultQuarkusIT {
}
