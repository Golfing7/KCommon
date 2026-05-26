package com.golfing8.kcommon.dialogs;

/**
 * A common type adapter interface for converting KCommon objects to Adventure components
 */
public interface KDialogElement<T> {
    /**
     * Constructs a component based on this element
     *
     * @return the component
     */
    T toComponent();
}
