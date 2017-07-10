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
import static org.junit.Assert.assertTrue;

import net.imglib2.RealPoint;
import net.imglib2.roi.geom.real.DefaultLine;
import net.imglib2.roi.geom.real.Line;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.scijava.Context;
import org.scijava.convert.ConvertService;

import ij.gui.Arrow;

/**
 * Tests converting {@link ij.gui.Line Line} to {@link Line} and the
 * corresponding {@link LineWrapper}.
 *
 * @author Alison Walter
 */
public class LineConversionTest {

	private static ij.gui.Line ijLine;
	private static Line<RealPoint> ilLine;
	private static Line<RealPoint> wrap;
	private ConvertService convertService;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void initialize() {
		ijLine = new ij.gui.Line(10.5, 20, 120.5, 150);
		ilLine = new DefaultLine(new double[] { 10.5, 20 }, new double[] { 120.5,
			150 }, false);
		wrap = new LineWrapper(ijLine);
	}

	@Before
	public void setup() {
		final Context context = new Context(ConvertService.class);
		convertService = context.service(ConvertService.class);
		ij.gui.Line.setWidth(1);
	}

	@After
	public void tearDown() {
		convertService.context().dispose();
	}

	@Test
	public void testLineWrapperGetter() {
		// Wrapped Line equals ImageJ 1.x Line
		assertEquals(wrap.endpointOne().getDoublePosition(0), ijLine.x1d, 0);
		assertEquals(wrap.endpointOne().getDoublePosition(1), ijLine.y1d, 0);
		assertEquals(wrap.endpointTwo().getDoublePosition(0), ijLine.x2d, 0);
		assertEquals(wrap.endpointTwo().getDoublePosition(1), ijLine.y2d, 0);

		// Wrapped Line equals equivalent ImgLib2 Line
		assertTrue(equalsRealPoint(wrap.endpointOne(), ilLine.endpointOne()));
		assertTrue(equalsRealPoint(wrap.endpointTwo(), ilLine.endpointTwo()));
	}

	@Test
	public void testLineWrapperSetEndPointOne() {
		exception.expect(UnsupportedOperationException.class);
		wrap.endpointOne().move(new double[] { 1, 1 });
	}

	@Test
	public void testLineWrapperSetEndPointTwo() {
		exception.expect(UnsupportedOperationException.class);
		wrap.endpointTwo().setPosition(new double[] { 1, 1 });
	}

	@Test
	public void testLineWrapperTest() {
		// Test that wrapped line and Imglib2 line have same test behavior
		final RealPoint pt1 = new RealPoint(ilLine.endpointOne());
		final RealPoint pt2 = new RealPoint(ilLine.endpointTwo());
		final RealPoint pt3 = new RealPoint(new double[] { 17, 126 }); // on line
		// on line but beyond endpoint
		final RealPoint pt4 = new RealPoint(new double[] { 4, 115 });
		// off line
		final RealPoint pt5 = new RealPoint(new double[] { 20.25, 40.125 });

		assertEquals(ilLine.test(pt1), wrap.test(pt1));
		assertEquals(ilLine.test(pt2), wrap.test(pt2));
		assertEquals(ilLine.test(pt3), wrap.test(pt3));
		assertEquals(ilLine.test(pt4), wrap.test(pt4));
		assertEquals(ilLine.test(pt5), wrap.test(pt5));
	}

	@Test
	public void testLineWrapperBounds() {
		assertEquals(10.5, wrap.realMin(0), 0);
		assertEquals(20, wrap.realMin(1), 0);
		assertEquals(120.5, wrap.realMax(0), 0);
		assertEquals(150, wrap.realMax(1), 0);
	}

	@Test
	public void testLineToLineConverter() {
		final Line<?> converted = convertService.convert(ijLine, Line.class);
		assertTrue(converted instanceof LineWrapper);

		assertEquals(wrap.endpointOne().getDoublePosition(0), converted
			.endpointOne().getDoublePosition(0), 0);
		assertEquals(wrap.endpointOne().getDoublePosition(1), converted
			.endpointOne().getDoublePosition(1), 0);
		assertEquals(wrap.endpointTwo().getDoublePosition(0), converted
			.endpointTwo().getDoublePosition(0), 0);
		assertEquals(wrap.endpointTwo().getDoublePosition(1), converted
			.endpointTwo().getDoublePosition(1), 0);
	}

	@Test
	public void testLineToLineConverterLineWithWidth() {
		ij.gui.Line.setWidth(10);
		final Line<?> converted = convertService.convert(ijLine, Line.class);
		assertTrue(converted == null); // converter does not work on ij.gui.Line
		// with width > 1
	}

	@Test
	public void testLineToLineConverterArrow() {
		final Arrow a = new Arrow(10, 10, 100, 100);
		final Line<?> converted = convertService.convert(a, Line.class);
		assertTrue(converted == null); // Arrow is an ij.gui.Line but its contains
		// method functions differently from ij.gui.Line, and an Arrow and a Line
		// which have the same end coordinates have different measurements. So,
		// Arrows cannot be converted to imglib2 Line
	}

	// -- Helper methods --

	public boolean equalsRealPoint(final RealPoint pointOne,
		final RealPoint pointTwo)
	{
		if (pointOne.numDimensions() != pointTwo.numDimensions()) return false;
		for (int d = 0; d < pointOne.numDimensions(); d++) {
			if (pointOne.getDoublePosition(d) != pointTwo.getDoublePosition(d))
				return false;
		}
		return true;
	}

}
