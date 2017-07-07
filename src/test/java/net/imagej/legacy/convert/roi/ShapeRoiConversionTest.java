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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.imglib2.RealPoint;
import net.imglib2.roi.RealMaskRealInterval;

import org.junit.Before;
import org.junit.Test;

import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;

/**
 * Tests converting an ImageJ 1.x {@link ShapeRoi} to an ImgLib2
 * {@link RealMaskRealInterval} and the corresponding {@link ShapeRoiWrapper}.
 *
 * @author Alison Walter
 */
public class ShapeRoiConversionTest {

	private ShapeRoi shape;
	private RealMaskRealInterval wrap;

	@Before
	public void setup() {
		final Roi rect = new Roi(100, 250, 12.5, 31);
		final OvalRoi oval = new OvalRoi(107, 240, 35.5, 17);
		final ShapeRoi rs = new ShapeRoi(rect);
		final ShapeRoi os = new ShapeRoi(oval);

		shape = rs.or(os);
		wrap = new ShapeRoiWrapper(shape);
	}

	@Test
	public void testShapeRoiWrapperTest() {
		final RealPoint test = new RealPoint(new double[] { 105, 280 });
		assertEquals(wrap.test(test), shape.contains(105, 280)); // in rectangle
		test.setPosition(new double[] { 130, 241 });
		assertEquals(wrap.test(test), shape.contains(130, 241)); // in oval
		test.setPosition(new double[] { 111, 253 });
		assertEquals(wrap.test(test), shape.contains(111, 253)); // in both
		test.setPosition(new double[] { 12, 16 });
		assertEquals(wrap.test(test), shape.contains(12, 16)); // in neither

		test.setPosition(new double[] { 100.25, 251.5 });
		assertTrue(wrap.test(test));
		test.setPosition(new double[] { 112, 281.125 });
		assertFalse(wrap.test(test));
	}

	@Test
	public void testShapeRoiWrapperBounds() {
		assertEquals(100, wrap.realMin(0), 0);
		assertEquals(240, wrap.realMin(1), 0);
		// computing the new bounds in ShapeRoi uses the integer space bounds
		// instead of the real bounds. So this is 143 instead of 142.5
		assertEquals(143, wrap.realMax(0), 0);
		assertEquals(281, wrap.realMax(1), 0);
	}
}
