package io.jcloud.api.conditions;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

import io.jcloud.api.DisabledOnQuarkusSnapshot;
import io.jcloud.utils.QuarkusUtils;

public class DisabledOnQuarkusSnapshotCondition implements ExecutionCondition {
    private static final String QUARKUS_SNAPSHOT_VERSION = "999-SNAPSHOT";
    private static final ConditionEvaluationResult ENABLED_BY_DEFAULT = ConditionEvaluationResult
            .enabled("@DisabledOnQuarkusSnapshot is not present");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Optional<AnnotatedElement> element = context.getElement();
        return AnnotationUtils.findAnnotation(element, DisabledOnQuarkusSnapshot.class).stream()
                .filter(annotation -> DisabledOnQuarkusSnapshotCondition.isQuarkusSnapshotVersion()).findAny()
                .map(this::testIsDisabled).orElse(ENABLED_BY_DEFAULT);
    }

    private ConditionEvaluationResult testIsDisabled(DisabledOnQuarkusSnapshot disabledOnQuarkus) {
        return ConditionEvaluationResult
                .disabled("Disabled on Quarkus snapshot (reason: " + disabledOnQuarkus.reason() + ")");
    }

    public static boolean isQuarkusSnapshotVersion() {
        return QUARKUS_SNAPSHOT_VERSION.equals(QuarkusUtils.getVersion());
    }
}
