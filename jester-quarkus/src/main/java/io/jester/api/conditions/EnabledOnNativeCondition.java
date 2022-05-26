package io.jester.api.conditions;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.jester.utils.QuarkusUtils;

public class EnabledOnNativeCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (QuarkusUtils.isNativePackageType()) {
            return ConditionEvaluationResult.enabled("Running test as it's running on Native");
        }

        return ConditionEvaluationResult.disabled("Skipping test as it's not running on Native");
    }
}
