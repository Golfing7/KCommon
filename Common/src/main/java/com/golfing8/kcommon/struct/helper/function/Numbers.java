/*
 * This file is part of helper, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.golfing8.kcommon.struct.helper.function;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

/**
 * Utility methods for parsing {@link Number}s, {@link Integer}s, {@link Long}s,
 * {@link Float}s and {@link Double}s from {@link String}s.
 */
@UtilityClass
public final class Numbers {

    // number

    /**
     * Parses an arbitrary number from the given string
     *
     * @param s the string
     * @return the number
     */
    @Nullable
    public static Number parseNullable(@Nonnull String s) {
        Objects.requireNonNull(s);
        try {
            return NumberFormat.getInstance().parse(s);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Parses an optional number from the given string
     *
     * @param s the string
     * @return the optional number
     */
    @Nonnull
    public static Optional<Number> parse(@Nonnull String s) {
        return Optional.ofNullable(parseNullable(s));
    }

    // integer

    /**
     * Parses a nullable integer from the given string
     *
     * @param s the string
     * @return the integer
     */
    @Nullable
    public static Integer parseIntegerNullable(@Nonnull String s) {
        Objects.requireNonNull(s);
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses an optional integer from the given string
     *
     * @param s the string
     * @return the optional integer
     */
    @Nonnull
    public static Optional<Integer> parseIntegerOpt(@Nonnull String s) {
        return Optional.ofNullable(parseIntegerNullable(s));
    }

    /**
     * Parses an optional integer from the given string
     *
     * @param s the string
     * @return the optional integer
     */
    @Nonnull
    public static OptionalInt parseInteger(@Nonnull String s) {
        try {
            return OptionalInt.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    // long

    /**
     * Parses a nullable long from the given string
     *
     * @param s the string
     * @return the long
     */
    @Nullable
    public static Long parseLongNullable(@Nonnull String s) {
        Objects.requireNonNull(s);
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses an optional long from the given string
     *
     * @param s the string
     * @return the optional long
     */
    @Nonnull
    public static Optional<Long> parseLongOpt(@Nonnull String s) {
        return Optional.ofNullable(parseLongNullable(s));
    }

    /**
     * Parses an optional long from the given string
     *
     * @param s the string
     * @return the optional long
     */
    @Nonnull
    public static OptionalLong parseLong(@Nonnull String s) {
        try {
            return OptionalLong.of(Long.parseLong(s));
        } catch (NumberFormatException e) {
            return OptionalLong.empty();
        }
    }

    // float

    /**
     * Parses a nullable flot from the given string
     *
     * @param s the string
     * @return the float
     */
    @Nullable
    public static Float parseFloatNullable(@Nonnull String s) {
        Objects.requireNonNull(s);
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses an optional float from the given string
     *
     * @param s the string
     * @return the optional float
     */
    @Nonnull
    public static Optional<Float> parseFloatOpt(@Nonnull String s) {
        return Optional.ofNullable(parseFloatNullable(s));
    }

    /**
     * Parses an optional float from the given string
     *
     * @param s the string
     * @return the optional float
     */
    @Nonnull
    public static OptionalDouble parseFloat(@Nonnull String s) {
        try {
            return OptionalDouble.of(Float.parseFloat(s));
        } catch (NumberFormatException e) {
            return OptionalDouble.empty();
        }
    }

    // double

    /**
     * Parses a nullable double from the given string
     *
     * @param s the string
     * @return the double
     */
    @Nullable
    public static Double parseDoubleNullable(@Nonnull String s) {
        Objects.requireNonNull(s);
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses an optional double from the given string
     *
     * @param s the string
     * @return the optional double
     */
    @Nonnull
    public static Optional<Double> parseDoubleOpt(@Nonnull String s) {
        return Optional.ofNullable(parseDoubleNullable(s));
    }

    /**
     * Parses an optional double from the given string
     *
     * @param s the string
     * @return the optional double
     */
    @Nonnull
    public static OptionalDouble parseDouble(@Nonnull String s) {
        try {
            return OptionalDouble.of(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            return OptionalDouble.empty();
        }
    }

    // byte

    /**
     * Parses a byte from the given string
     *
     * @param s the string
     * @return the byte
     */
    @Nullable
    public static Byte parseByteNullable(@Nonnull String s) {
        Objects.requireNonNull(s);
        try {
            return Byte.parseByte(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses an optional byte from the given string
     *
     * @param s the string
     * @return the optional byte
     */
    @Nonnull
    public static Optional<Byte> parseByteOpt(@Nonnull String s) {
        return Optional.ofNullable(parseByteNullable(s));
    }
}
