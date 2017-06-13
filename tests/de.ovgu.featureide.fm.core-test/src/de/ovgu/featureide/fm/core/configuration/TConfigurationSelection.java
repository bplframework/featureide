/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2017  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 * 
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */
package de.ovgu.featureide.fm.core.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.ovgu.featureide.fm.core.FeatureProject;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.job.LongRunningWrapper;

/**
 * Tests about feature selection.
 * 
 * @author Fabian Benduhn
 */
public class TConfigurationSelection extends AbstractConfigurationTest {

	@Override
	IFeatureModel loadModel() {
		return loadGUIDSL("S : [A] [B] C :: _S; %% not B;");
	}

	private void testConfigurationValid(Configuration c, final long expectedValue) {
		final ConfigurationPropagator propagator = FeatureProject.getPropagator(c, true);
		LongRunningWrapper.runMethod(propagator.update());
		assertTrue(LongRunningWrapper.runMethod(propagator.isValid()));
		assertEquals(expectedValue, LongRunningWrapper.runMethod(propagator.number(1000)).longValue());
	}

	private void testConfigurationInvalid(Configuration c) {
		final ConfigurationPropagator propagator = FeatureProject.getPropagator(c, true);
		LongRunningWrapper.runMethod(propagator.update());
		assertFalse(LongRunningWrapper.runMethod(propagator.isValid()));
		assertEquals(0L, LongRunningWrapper.runMethod(propagator.number(1000)).longValue());
	}

	@Test
	public void testSelection1() {
		Configuration c = new Configuration(fm);
		c.setManual("C", Selection.SELECTED);
		testConfigurationValid(c, 2L);
	}

	@Test
	public void testSelection2() {
		Configuration c = new Configuration(fm);
		testConfigurationValid(c, 2L);
	}

	@Test
	public void testSelection3() {
		Configuration c = new Configuration(fm);
		c.setManual("A", Selection.SELECTED);
		c.setManual("C", Selection.SELECTED);
		testConfigurationValid(c, 1L);
	}

	@Test
	public void testSelection4() {
		Configuration c = new Configuration(fm);
		c.setManual("A", Selection.SELECTED);
		testConfigurationValid(c, 1L);
	}

	@Test
	public void testSelection5() {
		Configuration c = new Configuration(fm);
		c.setManual("B", Selection.SELECTED);
		testConfigurationInvalid(c);
	}

}
