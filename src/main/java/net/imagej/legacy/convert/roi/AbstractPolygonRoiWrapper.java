/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.legacy.convert.roi;

import net.imglib2.Localizable;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealPositionable;
import net.imglib2.roi.Mask;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.roi.geom.real.Polyline;

import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;

/**
 * Abstract base class for wrapping ImageJ 1.x objects which are
 * {@link PolygonRoi}s (i.e. POLYGON, POLYLINE, FREELINE, etc.).
 *
 * @author Alison Walter
 */
public abstract class AbstractPolygonRoiWrapper implements
	IJRealRoiWrapper<PolygonRoi>
{

	private final PolygonRoi poly;

	/**
	 * Creates an ImageJ 1.x {@link PolygonRoi} and wraps it as an ImgLib2
	 * {@link Polygon2D} or {@link Polyline}.
	 *
	 * @param xPoints x coordinates of the vertices
	 * @param yPoints y coordinates of the vertices
	 * @param nPoints number of vertices
	 * @param type denotes if PolygonRoi behaves as {@link Roi#POLYGON} or
	 *          {@link Roi#POLYLINE}
	 */
	public AbstractPolygonRoiWrapper(final int[] xPoints, final int[] yPoints,
		final int nPoints, final int type)
	{
		poly = new PolygonRoi(xPoints, yPoints, nPoints, type);
	}

	/**
	 * Creates an ImageJ 1.x {@link PolygonRoi} and wraps it as an ImgLib2
	 * {@link Polygon2D} or {@link Polyline}.
	 *
	 * @param xPoints x coordinates of the vertices
	 * @param yPoints y coordinates of the vertices
	 * @param nPoints number of vertices
	 * @param type denotes if PolygonRoi behaves as {@link Roi#POLYGON} or
	 *          {@link Roi#POLYLINE}
	 */
	public AbstractPolygonRoiWrapper(final float[] xPoints, final float[] yPoints,
		final int nPoints, final int type)
	{
		poly = new PolygonRoi(xPoints, yPoints, nPoints, type);
	}

	/**
	 * Creates an ImageJ 1.x {@link PolygonRoi} and wraps it as an ImgLib2
	 * {@link Mask}. The length of {@code xPoints} will be used to determine the
	 * number of vertices this {@code Mask} has.
	 *
	 * @param xPoints x coordinates of the vertices
	 * @param yPoints y coordinates of the vertices
	 * @param type denotes if PolygonRoi behaves as {@link Roi#POLYGON} or
	 *          {@link Roi#POLYLINE}
	 */
	public AbstractPolygonRoiWrapper(final float[] xPoints, final float[] yPoints,
		final int type)
	{
		poly = new PolygonRoi(xPoints, yPoints, type);
	}

	/**
	 * Wraps an ImageJ 1.x {@link PolygonRoi} as an ImgLib2 {@link Mask}.
	 *
	 * @param poly the {@code PolygonRoi} to be wrapped
	 */
	public AbstractPolygonRoiWrapper(final PolygonRoi poly) {
		if (poly.isSplineFit()) throw new IllegalArgumentException(
			"Cannot wrap spline fit PolygonRois");
		this.poly = poly;
	}

	@Override
	public double realMin(final int d) {
		if (d != 0 && d != 1) throw new IllegalArgumentException(
			"Invalid dimension " + d);
		// NB: bounding box doesn't update after vertex removed
		final float[] c = d == 0 ? poly.getFloatPolygon().xpoints : poly
			.getFloatPolygon().ypoints;
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < numVertices(); i++)
			if (c[i] < min) min = c[i];
		return min;
	}

	@Override
	public double realMax(final int d) {
		if (d != 0 && d != 1) throw new IllegalArgumentException(
			"Invalid dimension " + d);
		// NB: bounding box doesn't update after vertex removed
		final float[] c = d == 0 ? poly.getFloatPolygon().xpoints : poly
			.getFloatPolygon().ypoints;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < numVertices(); i++)
			if (c[i] > max) max = c[i];
		return max;
	}

	public RealPoint vertex(final int pos) {
		final float[] x = poly.getFloatPolygon().xpoints;
		final float[] y = poly.getFloatPolygon().ypoints;

		return new PolygonRoiVertex(x[pos], y[pos]);
	}

	public int numVertices() {
		return poly.getNCoordinates();
	}

	/**
	 * If the wrapped {@link PolygonRoi} is not associated with an
	 * {@link ImagePlus}, then this method will always throw an
	 * {@code UnsupportedOperationException}. Otherwise, the vertex will be
	 * removed provided the index is valid.
	 */
	public void removeVertex(final int index) {
		if (poly.getImage() != null) {
			final double x = poly.getFloatPolygon().xpoints[index];
			final double y = poly.getFloatPolygon().ypoints[index];
			poly.deleteHandle(x, y);
		}
		else throw new UnsupportedOperationException("removeVertex");
	}

	@Override
	public PolygonRoi getRoi() {
		return poly;
	}

	// -- Helper classes --

	/**
	 * This {@link RealPoint} throws {@link UnsupportedOperationException}s for
	 * all {@link RealPositionable} methods, because the vertices of the
	 * underlying {@link ij.gui.PolygonRoi polygon} cannot be modified.
	 */
	protected class PolygonRoiVertex extends RealPoint {

		public PolygonRoiVertex(final double x, final double y) {
			super(new double[] { x, y });
		}

		@Override
		public void move(final float distance, final int d) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void move(final double distance, final int d) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void move(final RealLocalizable localizable) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void move(final float[] distance) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void move(final double[] distance) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void setPosition(final RealLocalizable localizable) {
			throw new UnsupportedOperationException("setPosition");
		}

		@Override
		public void setPosition(final float[] position) {
			throw new UnsupportedOperationException("setPosition");
		}

		@Override
		public void setPosition(final double[] position) {
			throw new UnsupportedOperationException("setPosition");
		}

		@Override
		public void setPosition(final float position, final int d) {
			throw new UnsupportedOperationException("setPosition");
		}

		@Override
		public void setPosition(final double position, final int d) {
			throw new UnsupportedOperationException("setPosition");
		}

		@Override
		public void fwd(final int d) {
			throw new UnsupportedOperationException("fwd");
		}

		@Override
		public void bck(final int d) {
			throw new UnsupportedOperationException("bck");
		}

		@Override
		public void move(final int distance, final int d) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void move(final long distance, final int d) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void move(final Localizable localizable) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void move(final int[] distance) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void move(final long[] distance) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void setPosition(final Localizable localizable) {
			throw new UnsupportedOperationException("setPosition");
		}

		@Override
		public void setPosition(final int[] position) {
			throw new UnsupportedOperationException("setPosition");
		}

		@Override
		public void setPosition(final long[] position) {
			throw new UnsupportedOperationException("setPosition");
		}

		@Override
		public void setPosition(final int position, final int d) {
			throw new UnsupportedOperationException("setPosition");
		}

		@Override
		public void setPosition(final long position, final int d) {
			throw new UnsupportedOperationException("setPosition");
		}

	}
}
