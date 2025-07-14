package com.golfing8.kcommon.util;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A utility class to evaluate mathematical expressions.
 */
@UtilityClass
public final class MathExpressions {
    private static final Function RAND_0 = new Function("rand0") {
        @Override
        public double apply(double... doubles) {
            return Math.random();
        }
    };
    private static final Function RAND_1 = new Function("rand1") {
        @Override
        public double apply(double... doubles) {
            return ThreadLocalRandom.current().nextDouble(doubles[0]);
        }
    };
    private static final Function RAND_2 = new Function("rand2") {
        @Override
        public double apply(double... doubles) {
            return ThreadLocalRandom.current().nextDouble(doubles[0], doubles[1]);
        }
    };
    private static final List<Function> ADDED_FUNCTIONS = Lists.newArrayList(RAND_0, RAND_1, RAND_2);

    /**
     * Evaluates a given expression with the given placeholders.
     *
     * @param expression   the expression.
     * @param placeholders the placeholders.
     * @return the value.
     */
    public static double evaluate(String expression, Object... placeholders) {
        String parsed = MS.parseSingle(expression, placeholders);
        return new ExpressionBuilder(parsed).functions(ADDED_FUNCTIONS).build().evaluate();
    }
}
