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

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.geom.GeomMaths;
import net.imglib2.roi.geom.real.Polygon2D;

import gnu.trove.list.array.TDoubleArrayList;
import ij.gui.PolygonRoi;
import ij.gui.Roi;

/**
 * Wraps an ImageJ 1.x {@link PolygonRoi} as an ImgLib2 {@link Polygon2D}.
 *
 * @author Alison Walter
 */
public class PolygonRoiWrapper extends AbstractPolygonRoiWrapper implements
	Polygon2D<RealPoint>
{

	/**
	 * Creates an ImageJ 1.x {@link PolygonRoi} and wraps it as an ImgLib2
	 * {@link Polygon2D}.
	 *
	 * @param xPoints x coordinates of the vertices
	 * @param yPoints y coordinates of the vertices
	 * @param nPoints number of vertices
	 */
	public PolygonRoiWrapper(final int[] xPoints, final int[] yPoints,
		final int nPoints)
	{
		super(xPoints, yPoints, nPoints, Roi.POLYGON);
	}

	/**
	 * Creates an ImageJ 1.x {@link PolygonRoi} and wraps it as an ImgLib2
	 * {@link Polygon2D}.
	 *
	 * @param xPoints x coordinates of the vertices
	 * @param yPoints y coordinates of the vertices
	 * @param nPoints number of vertices
	 */
	public PolygonRoiWrapper(final float[] xPoints, final float[] yPoints,
		final int nPoints)
	{
		super(xPoints, yPoints, nPoints, Roi.POLYGON);
	}

	/**
	 * Creates an ImageJ 1.x {@link PolygonRoi} and wraps it as an ImgLib2
	 * {@link Polygon2D}. The length of {@code xPoints} will be used to determine
	 * the number of vertices this {@code Polygon2D} has.
	 *
	 * @param xPoints x coordinates of the vertices
	 * @param yPoints y coordinates of the vertices
	 */
	public PolygonRoiWrapper(final float[] xPoints, final float[] yPoints) {
		super(xPoints, yPoints, Roi.POLYGON);
	}

	/**
	 * Wraps an ImageJ 1.x {@link PolygonRoi} as an ImgLib2 {@link Polygon2D}.
	 *
	 * @param poly the {@code PolygonRoi} to be wrapped
	 */
	public PolygonRoiWrapper(final PolygonRoi poly) {
		super(poly);
		if (poly.getType() != Roi.POLYGON && poly.getType() != Roi.FREEROI)
			throw new IllegalArgumentException("Cannot wrap " + poly
				.getTypeAsString() + " as Polygon2D");
		if (poly.isSplineFit()) throw new IllegalArgumentException("Cannot wrap " +
			"spline fitted polygons");
	}

	/**
	 * This will <strong>always</strong> throw an
	 * {@code UnsupportedOperationException}.
	 *
	 * @throws UnsupportedOperationException cannot add a new vertex to the
	 *           underlying {@link PolygonRoi}
	 */
	@Override
	public void addVertex(final int index, final double[] vertex) {
		throw new UnsupportedOperationException("addVertex");
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Since {@link PolygonRoi#contains(int, int)} uses the "pnpoly" algorithm for
	 * real space polygons, so does this implementation. Thus resulting in a
	 * {@code Polygon2D} with {@link net.imglib2.roi.BoundaryType#UNSPECIFIED
	 * unspecified} boundary behavior.
	 * </p>
	 */
	@Override
	public boolean test(final RealLocalizable t) {
		final float[] xf = getRoi().getFloatPolygon().xpoints;
		final float[] yf = getRoi().getFloatPolygon().ypoints;
		final TDoubleArrayList x = new TDoubleArrayList(getRoi().getNCoordinates());
		final TDoubleArrayList y = new TDoubleArrayList(getRoi().getNCoordinates());

		for (int i = 0; i < getRoi().getNCoordinates(); i++) {
			x.add(xf[i]);
			y.add(yf[i]);
		}

		return GeomMaths.pnpoly(x, y, t);
	}
}
