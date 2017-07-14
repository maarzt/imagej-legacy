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

import java.lang.reflect.Type;

import net.imglib2.RealPoint;
import net.imglib2.roi.geom.real.Polyline;

import org.scijava.convert.AbstractConverter;
import org.scijava.convert.ConversionRequest;
import org.scijava.convert.Converter;
import org.scijava.plugin.Plugin;

import ij.gui.PolygonRoi;
import ij.gui.Roi;

/**
 * Converts an ImageJ 1.x {@link PolygonRoi} to an ImgLib2 {@link Polyline}.
 * This is only intended to convert ImageJ 1.x {@link PolygonRoi}s with zero
 * width that are not spline fit and of type:
 * <ul>
 * <li>{@link Roi#POLYLINE}</li>
 * <li>{@link Roi#FREELINE}</li>
 * <li>{@link Roi#ANGLE}</li>
 * </ul>
 *
 * @author Alison Walter
 */
@Plugin(type = Converter.class)
public class PolylineRoiToPolylineConverter extends
	AbstractConverter<PolygonRoi, Polyline<RealPoint>>
{

	@Override
	public boolean canConvert(final ConversionRequest request) {
		if (super.canConvert(request)) return supportedType((PolygonRoi) request
			.sourceObject());
		return false;
	}

	@Override
	public boolean canConvert(final Object src, final Type dest) {
		if (super.canConvert(src, dest)) return supportedType((PolygonRoi) src);
		return false;
	}

	@Override
	public boolean canConvert(final Object src, final Class<?> dest) {
		if (super.canConvert(src, dest)) return supportedType((PolygonRoi) src);
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T convert(final Object src, final Class<T> dest) {
		if (!(src instanceof PolygonRoi)) throw new IllegalArgumentException(
			"Cannot convert " + src.getClass().getSimpleName() + " to Polyline");
		if (((PolygonRoi) src).getType() == Roi.POLYLINE)
			return (T) new PolylineRoiWrapper((PolygonRoi) src);
		return (T) new UnmodifiablePolylineRoiWrapper((PolygonRoi) src);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<Polyline<RealPoint>> getOutputType() {
		return (Class) Polyline.class;
	}

	@Override
	public Class<PolygonRoi> getInputType() {
		return PolygonRoi.class;
	}

	// -- Helper methods --

	private boolean supportedType(final PolygonRoi p) {
		final boolean supportedType = p.getType() == Roi.POLYLINE || p
			.getType() == Roi.ANGLE || p.getType() == Roi.FREELINE;
		return supportedType && p.getStrokeWidth() == 0 && !p.isSplineFit();
	}

}
