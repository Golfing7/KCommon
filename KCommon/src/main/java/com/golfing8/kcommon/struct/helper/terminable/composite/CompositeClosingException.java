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

package com.golfing8.kcommon.struct.helper.terminable.composite;

import java.util.Collections;
import java.util.List;

/**
 * Exception thrown to propagate exceptions thrown by
 * {@link CompositeTerminable#close()}.
 */
public class CompositeClosingException extends Exception {
    private final List<? extends Throwable> causes;

    public CompositeClosingException(List<? extends Throwable> causes) {
        super("Exception(s) occurred whilst closing: " + causes.toString());
        if (causes.isEmpty()) {
            throw new IllegalArgumentException("No causes");
        }
        this.causes = Collections.unmodifiableList(causes);
    }

    public List<? extends Throwable> getCauses() {
        return this.causes;
    }

    public void printAllStackTraces() {
        this.printStackTrace();
        for (Throwable cause : this.causes) {
            cause.printStackTrace();
        }
    }

}
