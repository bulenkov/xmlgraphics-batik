/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.batik.refimpl.gvt.filter;

import org.apache.batik.gvt.filter.Filter;
import org.apache.batik.gvt.filter.CachableRed;
import org.apache.batik.gvt.filter.GaussianBlurRable;
import org.apache.batik.gvt.filter.PadMode;
import org.apache.batik.util.awt.image.GraphicsUtil;

import java.awt.Shape;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Rectangle;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.ColorModel;
import java.awt.image.renderable.RenderContext;

/**
 * GaussianBlurRable implementation
 *
 * @author <a href="mailto:vincent.hardy@eng.sun.com>Vincent Hardy</a>
 * @version $Id$
 */
public class ConcreteGaussianBlurRable
    extends    AbstractRable
    implements GaussianBlurRable {
    /**
     * Deviation along the x-axis
     */
    private double stdDeviationX;

    /**
     * Deviation along the y-axis
     */
    private double stdDeviationY;

    public ConcreteGaussianBlurRable(Filter src,
                                     double stdevX, double stdevY) {
        super(src, null);
        setStdDeviationX(stdevX);
        setStdDeviationY(stdevY);
    }

    /**
     * The deviation along the x axis, in user space.
     * @param stdDeviationX should be greater than zero.
     */
    public void setStdDeviationX(double stdDeviationX){
        if(stdDeviationX < 0){
            throw new IllegalArgumentException();
        }

        touch();
        this.stdDeviationX = stdDeviationX;
    }

    /**
     * The deviation along the y axis, in user space.
     * @param stdDeviationY should be greater than zero
     */
    public void setStdDeviationY(double stdDeviationY){
        if(stdDeviationY < 0){
            throw new IllegalArgumentException();
        }
        touch();
        this.stdDeviationY = stdDeviationY;
    }

    /**
     * Returns the deviation along the x-axis, in user space.
     */
    public double getStdDeviationX(){
        return stdDeviationX;
    }

    /**
     * Returns the deviation along the y-axis, in user space.
     */
    public double getStdDeviationY(){
        return stdDeviationY;
    }

    /**
     * Sets the source of the blur operation
     */
    public void setSource(Filter src){
        init(src, null);
    }

    /**
     * Grow the source's bounds
     */
    public Rectangle2D getBounds2D(){
        Rectangle2D src = getSource().getBounds2D();
        float dX = (float)(stdDeviationX*GaussianBlurOp.DSQRT2PI);
        float dY = (float)(stdDeviationY*GaussianBlurOp.DSQRT2PI);
        float radX = 3*dX/2;
        float radY = 3*dY/2;
        return new Rectangle2D.Float
            ((float)(src.getMinX()  -radX),
             (float)(src.getMinY()  -radY),
             (float)(src.getWidth() +2*radX),
             (float)(src.getHeight()+2*radY));
    }

    /**
     * Returns the source of the blur operation
     */
    public Filter getSource(){
        return (Filter)getSources().get(0);
    }

    public final static double eps = 0.0001;
    public static boolean eps_eq(double f1, double f2) {
        return ((f1 >= f2-eps) && (f1 <= f2+eps));
    }
    public static boolean eps_abs_eq(double f1, double f2) {
        if (f1 <0) f1 = -f1;
        if (f2 <0) f2 = -f2;
        return eps_eq(f1, f2);
    }

    public RenderedImage createRendering(RenderContext rc) {
        // Just copy over the rendering hints.
        RenderingHints rh = rc.getRenderingHints();
        if (rh == null) rh = new RenderingHints(null);

        // update the current affine transform
        AffineTransform at = rc.getTransform();


        // This splits out the scale and applies it
        // prior to the Gaussian.  Then after appying the gaussian
        // it applies the shear (rotation) and translation components.
        double sx = at.getScaleX();
        double sy = at.getScaleY();

        double shx = at.getShearX();
        double shy = at.getShearY();

        double tx = at.getTranslateX();
        double ty = at.getTranslateY();

        // The Scale is the "hypotonose" of the matrix vectors.
        double scaleX = Math.sqrt(sx*sx + shy*shy);
        double scaleY = Math.sqrt(sy*sy + shx*shx);

        double sdx = stdDeviationX*scaleX;
        double sdy = stdDeviationY*scaleY;

        GaussianBlurOp op = new GaussianBlurOp(sdx, sdy, rh);

        double blurRadX = op.getRadiusX();
        double blurRadY = op.getRadiusY();

        Shape aoi = rc.getAreaOfInterest();
        if(aoi == null)
            aoi = getBounds2D();

        Rectangle2D r = aoi.getBounds2D();

        // Grow the region in usr space.
        r = new Rectangle2D.Double(r.getX()-blurRadX/scaleX, 
                                   r.getY()-blurRadY/scaleY,
                                   r.getWidth() +2*blurRadX/scaleX, 
                                   r.getHeight()+2*blurRadY/scaleY);

        // This is the affine transform between our usr space and an
        // intermediate space which is scaled similarly to our device
        // space but is still axially aligned with our device space.
        AffineTransform srcAt;

        // This is the affine transform between our intermediate
        // coordinate space and the real device space, or null (if
        // we don't need an intermediate space).
        AffineTransform resAt;

        if (eps_eq(blurRadX, blurRadY) &&
            eps_abs_eq(sx/scaleX, sy/scaleY)) {
            // Ok we have a square Gaussian kernel which means it is
            // circularly symetric, further our residual matrix (after
            // removing scaling) is a rotation matrix (perhaps with
            // mirroring), thus we can generate our source directly in
            // device space and convolve there rather than going to an
            // intermediate space (axially aligned with usr space) and
            // then completing the requested rotation/shear, with an
            // AffineRed...

            srcAt = at;
            resAt = null;
        } else {
            // Scale to device coords.
            srcAt = AffineTransform.getScaleInstance(scaleX, scaleY);

            // The shear/rotation simply divides out the
            // common scale factor in the matrix.
            resAt = new AffineTransform(sx/scaleX, shy/scaleX,
                                        shx/scaleY,  sy/scaleY,
                                        tx, ty);
        }

        
        RenderedImage ri;
        ri = getSource().createRendering(new RenderContext(srcAt, r, rh));
        if (ri == null)
            return null;


        CachableRed cr;
        cr = GraphicsUtil.wrap(ri);
        // org.apache.batik.test.gvt.ImageDisplay.showImage("Wrap: ", cr);
        cr = GraphicsUtil.convertToLsRGB(cr);
        // org.apache.batik.test.gvt.ImageDisplay.showImage("Conv: ", cr);

        Shape devShape = srcAt.createTransformedShape(aoi);
        r = devShape.getBounds2D();
        r = new Rectangle2D.Double(r.getX()-blurRadX, 
                                   r.getY()-blurRadY,
                                   r.getWidth() +2*blurRadX, 
                                   r.getHeight()+2*blurRadY);
        if (!r.getBounds().equals(cr.getBounds()))
            cr = new PadRed(cr, r.getBounds(), PadMode.ZERO_PAD, rh);
        
        ColorModel cm = cr.getColorModel();
        
        Raster rr = cr.getData();
        WritableRaster wr = GraphicsUtil.makeRasterWritable(rr, 0, 0);

        BufferedImage srcBI;
        srcBI = new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), null);

        BufferedImage destBI;
        destBI = op.filter(srcBI, null);

        final int rrMinX = cr.getMinX();
        final int rrMinY = cr.getMinY();

        cr = new ConcreteBufferedImageCachableRed(destBI, rrMinX, rrMinY);

        if ((resAt != null) && (!resAt.isIdentity()))
            cr = new AffineRed(cr, resAt, rh);
        
        return cr;
    }

    /**
     * Returns the region of input data is is required to generate
     * outputRgn.
     * @param srcIndex  The source to do the dependency calculation for.
     * @param outputRgn The region of output you are interested in
     *  generating dependencies for.  The is given in the user coordiate
     *  system for this node.
     * @return The region of input required.  This is in the user
     * coordinate system for the source indicated by srcIndex.
     */
    public Shape getDependencyRegion(int srcIndex, Rectangle2D outputRgn){
        if(srcIndex != 0)
            outputRgn = null;
        else {
            // There is only one source in GaussianBlur
            float dX = (float)(stdDeviationX*GaussianBlurOp.DSQRT2PI);
            float dY = (float)(stdDeviationY*GaussianBlurOp.DSQRT2PI);
            float radX = 3*dX/2;
            float radY = 3*dY/2;
            outputRgn = new Rectangle2D.Float
                            ((float)(outputRgn.getMinX()  -radX),
                             (float)(outputRgn.getMinY()  -radY),
                             (float)(outputRgn.getWidth() +2*radX),
                             (float)(outputRgn.getHeight()+2*radY));

            Rectangle2D bounds = getBounds2D();
            if (outputRgn.intersects(bounds) == false)
                return new Rectangle2D.Float();
            // Intersect with output region
            outputRgn = outputRgn.createIntersection(bounds);
        }

        return outputRgn;
    }

    /**
     * This calculates the region of output that is affected by a change
     * in a region of input.
     * @param srcIndex The input that inputRgn reflects changes in.
     * @param inputRgn the region of input that has changed, used to
     *  calculate the returned shape.  This is given in the user
     *  coordinate system of the source indicated by srcIndex.
     * @return The region of output that would be invalid given
     *  a change to inputRgn of the source selected by srcIndex.
     *  this is in the user coordinate system of this node.
     */
    public Shape getDirtyRegion(int srcIndex, Rectangle2D inputRgn){
        Rectangle2D dirtyRegion = null;
        if(srcIndex == 0){
            float dX = (float)(stdDeviationX*GaussianBlurOp.DSQRT2PI);
            float dY = (float)(stdDeviationY*GaussianBlurOp.DSQRT2PI);
            float radX = 3*dX/2;
            float radY = 3*dY/2;
            inputRgn = new Rectangle2D.Float
                            ((float)(inputRgn.getMinX()  -radX),
                             (float)(inputRgn.getMinY()  -radY),
                             (float)(inputRgn.getWidth() +2*radX),
                             (float)(inputRgn.getHeight()+2*radY));

            Rectangle2D bounds = getBounds2D();
            if (inputRgn.intersects(bounds) == false)
                return new Rectangle2D.Float();
            // Intersect with input region
            dirtyRegion = inputRgn.createIntersection(bounds);
        }

        return dirtyRegion;
    }


}
