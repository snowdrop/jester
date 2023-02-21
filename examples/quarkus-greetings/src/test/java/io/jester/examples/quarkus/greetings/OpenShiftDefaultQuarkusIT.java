package io.jester.examples.quarkus.greetings;

import io.jester.api.OpenShiftServiceConfiguration;
import io.jester.api.RunOnOpenShift;

@RunOnOpenShift
@OpenShiftServiceConfiguration(forService = "quarkus", useRoute = false)
public class OpenShiftDefaultQuarkusIT extends DefaultQuarkusIT {
}
