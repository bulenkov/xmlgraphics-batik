/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.batik.gvt;

import java.awt.Shape;

import org.apache.batik.gvt.GraphicsNodeRenderContext;

/**
 * Interface describing object that can be selected or have selections
 * made on it.
 *
 * @author <a href="bill.haneman@ireland.sun.com">Bill Haneman</a>
 * @version $Id$
 */
public interface Selectable {

    /**
     * Initializes the current selection to begin with the character at (x, y).
     */
    void selectAt(double x, double y, GraphicsNodeRenderContext rc);

    /**
     * Extends the current selection to the character at (x, y)..
     */
    void selectTo(double x, double y, GraphicsNodeRenderContext rc);
 
    /**
     * Selects the entire contents of the GraphicsNode at (x, y)..
     */
    void selectAll(double x, double y, GraphicsNodeRenderContext rc);
 
    /**
     * Get the current text selection.
     * @return an object containing the selected content.
     */
    Object getSelection(GraphicsNodeRenderContext rc);

    /** 
     * Return a shape in user coords which encloses the current selection.
     */
    Shape getHighlightShape(GraphicsNodeRenderContext rc);
}
