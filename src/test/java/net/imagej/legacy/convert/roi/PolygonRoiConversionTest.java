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

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.geom.real.DefaultPolygon2D;
import net.imglib2.roi.geom.real.Polygon2D;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.scijava.Context;
import org.scijava.convert.ConvertService;

import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;

/**
 * Tests converting {@link PolygonRoi} to {@link Polygon2D} and the
 * corresponding {@link PolygonRoiWrapper}.
 *
 * @author Alison Walter
 */
public class PolygonRoiConversionTest {

	private PolygonRoi poly;
	private Polygon2D<RealPoint> wrap;
	private Polygon2D<RealPoint> dp;
	private RealLocalizable inside;
	private RealLocalizable outside;
	private ConvertService convertService;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Before
	public void setup() {
		poly = new PolygonRoi(new float[] { 100.5f, 100.5f, 150, 199, 199 },
			new float[] { 100, 200, 250.25f, 200, 100 }, Roi.POLYGON);
		wrap = new PolygonRoiWrapper(poly);
		dp = new DefaultPolygon2D(new double[] { 100.5, 100.5, 150, 199, 199 },
			new double[] { 100, 200, 250.25, 200, 100 });

		inside = new RealPoint(new double[] { 151, 225 });
		outside = new RealPoint(new double[] { 100, 100 });

		final Context context = new Context(ConvertService.class);
		convertService = context.service(ConvertService.class);
	}

	@After
	public void tearDown() {
		convertService.context().dispose();
	}

	@Test
	public void testPolygonRoiWrapperGetters() {
		assertEquals(5, wrap.numVertices());
		final float[] x = poly.getFloatPolygon().xpoints;
		final float[] y = poly.getFloatPolygon().ypoints;

		for (int i = 0; i < 5; i++) {
			// compare ImageJ 1.x to Wrapper
			assertEquals(x[i], wrap.vertex(i).getDoublePosition(0), 0);
			assertEquals(y[i], wrap.vertex(i).getDoublePosition(1), 0);

			// compare ImgLib2 to Wrapper
			assertEquals(dp.vertex(i).getDoublePosition(0), wrap.vertex(i)
				.getDoublePosition(0), 0);
			assertEquals(dp.vertex(i).getDoublePosition(1), wrap.vertex(i)
				.getDoublePosition(1), 0);
		}
	}

	@Test
	public void testPolygonRoiWrapperSetVertex() {
		exception.expect(UnsupportedOperationException.class);
		wrap.vertex(2).setPosition(new double[] { 1, -3 });
	}

	@Test
	public void testPolygonRoiWrapperAddVertex() {
		exception.expect(UnsupportedOperationException.class);
		wrap.addVertex(3, new double[] { 0, 0 });
	}

	@Test
	public void testPolygonRoiWrapperRemoveVertexNoImagePlus() {
		exception.expect(UnsupportedOperationException.class);
		wrap.removeVertex(0);
	}

	@Test
	public void testPolygonRoiWrapperRemoveVertexWithImagePlus() {
		final ImagePlus i = new ImagePlus("http://imagej.net/images/blobs.gif");
		i.setRoi(poly);
		poly.setImage(i);

		wrap.removeVertex(2);
		assertEquals(4, wrap.numVertices());

		// Check that backing PolygonRoi was updated
		assertEquals(4, poly.getNCoordinates());
	}

	@Test
	public void testPolygonRoiWrapperTest() {
		assertTrue(wrap.test(inside));
		assertFalse(wrap.test(outside));
	}

	@Test
	public void testPolygonRoiWrapperBounds() {
		assertEquals(100.5, wrap.realMin(0), 0);
		assertEquals(100, wrap.realMin(1), 0);
		assertEquals(199, wrap.realMax(0), 0);
		assertEquals(250.25, wrap.realMax(1), 0);
	}

	@Test
	public void testUpdatedAfterPolygonRoiWrapperModified() {
		final ImagePlus i = new ImagePlus("http://imagej.net/images/blobs.gif");
		i.setRoi(poly);
		poly.setImage(i);

		assertTrue(wrap.test(inside));
		assertFalse(wrap.test(outside));

		wrap.removeVertex(2);
		assertFalse(wrap.test(inside));
		assertFalse(wrap.test(outside));

		// Check bounds updated
		assertEquals(100.5, wrap.realMin(0), 0);
		assertEquals(100, wrap.realMin(1), 0);
		assertEquals(199, wrap.realMax(0), 0);
		assertEquals(200, wrap.realMax(1), 0);
	}

	@Test
	public void testPolygonRoiToPolygon2DConverter() {
		final Polygon2D<?> converted = convertService.convert(poly,
			Polygon2D.class);

		assertTrue(converted instanceof PolygonRoiWrapper);

		assertEquals(5, converted.numVertices());
		assertEquals(wrap.vertex(0).getDoublePosition(0), converted.vertex(0)
			.getDoublePosition(0), 0);
		assertEquals(wrap.vertex(0).getDoublePosition(1), converted.vertex(0)
			.getDoublePosition(1), 0);
		assertEquals(wrap.vertex(1).getDoublePosition(0), converted.vertex(1)
			.getDoublePosition(0), 0);
		assertEquals(wrap.vertex(1).getDoublePosition(1), converted.vertex(1)
			.getDoublePosition(1), 0);
		assertEquals(wrap.vertex(2).getDoublePosition(0), converted.vertex(2)
			.getDoublePosition(0), 0);
		assertEquals(wrap.vertex(2).getDoublePosition(1), converted.vertex(2)
			.getDoublePosition(1), 0);
		assertEquals(wrap.vertex(3).getDoublePosition(0), converted.vertex(3)
			.getDoublePosition(0), 0);
		assertEquals(wrap.vertex(3).getDoublePosition(1), converted.vertex(3)
			.getDoublePosition(1), 0);
		assertEquals(wrap.vertex(4).getDoublePosition(0), converted.vertex(4)
			.getDoublePosition(0), 0);
		assertEquals(wrap.vertex(4).getDoublePosition(1), converted.vertex(4)
			.getDoublePosition(1), 0);
	}

	@Test
	public void testPolygonRoiToPolygon2DConverterFreeRoi() {
		final PolygonRoi free = new PolygonRoi(new float[] { 1, 1.2f, 1, 1.2f, 1.3f,
			1.5f, 1, 2, 3.5f, 4, 6.1f, 7, 7, 7, 7.5f, 7.1f }, new float[] { 1, 5, 6,
				6.5f, 7, 7.25f, 8, 8.5f, 8.4f, 8, 8, 8.25f, 7, 6, 5, 4 }, Roi.FREEROI);
		final Polygon2D<?> converted = convertService.convert(free,
			Polygon2D.class);

		assertTrue(converted == null);
	}

	@Test
	public void testPolygonRoiToPolygon2DConverterWithSpline() {
		poly.fitSpline();
		final Polygon2D<?> converted = convertService.convert(poly,
			Polygon2D.class);

		assertTrue(converted == null);
	}
}
