package io.github.jester.api.conditions;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.github.jester.utils.QuarkusUtils;

public class DisabledOnQuarkusNativeCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (!QuarkusUtils.isNativePackageType()) {
            return ConditionEvaluationResult.enabled("It's not running the test on Native");
        }

        return ConditionEvaluationResult.disabled("It's running the test on Native");
    }
}
