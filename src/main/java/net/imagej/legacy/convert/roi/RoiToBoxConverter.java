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
import net.imglib2.roi.geom.real.Box;

import org.scijava.Priority;
import org.scijava.convert.AbstractConverter;
import org.scijava.convert.ConversionRequest;
import org.scijava.convert.Converter;
import org.scijava.plugin.Plugin;

import ij.gui.Roi;

/**
 * Converts an ImageJ 1.x {@link Roi} of type {@link Roi#RECTANGLE} with corner
 * diameter = 0 to an ImgLib2 {@link Box}.
 *
 * @author Alison Walter
 */
@Plugin(type = Converter.class, priority = Priority.LOW)
public class RoiToBoxConverter extends AbstractConverter<Roi, Box<RealPoint>> {

	@Override
	public boolean canConvert(final ConversionRequest request) {
		if (super.canConvert(request)) {
			final Roi r = (Roi) request.sourceObject();
			return r.getType() == Roi.RECTANGLE && r.getCornerDiameter() == 0;
		}
		return false;
	}

	@Override
	public boolean canConvert(final Object src, final Type dest) {
		if (super.canConvert(src, dest)) {
			final Roi r = (Roi) src;
			return r.getType() == Roi.RECTANGLE && r.getCornerDiameter() == 0;
		}
		return false;
	}

	@Override
	public boolean canConvert(final Object src, final Class<?> dest) {
		if (super.canConvert(src, dest)) {
			final Roi r = (Roi) src;
			return r.getType() == Roi.RECTANGLE && r.getCornerDiameter() == 0;
		}
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T convert(final Object src, final Class<T> dest) {
		if (!(src instanceof Roi)) throw new IllegalArgumentException(
			"Cannot convert " + src.getClass().getSimpleName() + " to Box");
		return (T) new RoiWrapper((Roi) src);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Class<Box<RealPoint>> getOutputType() {
		return (Class) Box.class;
	}

	@Override
	public Class<Roi> getInputType() {
		return Roi.class;
	}

}
