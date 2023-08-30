package com.golfing8.kcommon.util;

import lombok.experimental.UtilityClass;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * A utility class to evaluate mathematical expressions.
 */
@UtilityClass
public final class MathExpressions {
    /**
     * Evaluates a given expression with the given placeholders.
     *
     * @param expression the expression.
     * @param placeholders the placeholders.
     * @return the value.
     */
    public static double evaluate(String expression, Object... placeholders) {
        String parsed = MS.parseSingle(expression, placeholders);
        return new ExpressionBuilder(parsed).build().evaluate();
    }
}
