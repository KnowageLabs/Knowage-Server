/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package integration.agorithms;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.EqualPartitioningOnLeafsAllocationAlgorithm;

public class EqualPartitioningOnLeafsAllocationAlgorithmTestCase extends AbstractWhatIfInMemoryTestCase {

	public void testPersistTransofrmationsEqualPartitioningOnLeafsAllocationAlgorithmTestCase() throws Exception {

		long dateA = System.currentTimeMillis();

		WhatIfEngineInstance ei = getWhatifengineiEngineInstance(getCatalogue());
		EqualPartitioningOnLeafsAllocationAlgorithm al = new EqualPartitioningOnLeafsAllocationAlgorithm(ei);
		al.initAlgorithm();
		Double ration = persistTransformations(ei, al);

		long dateB = System.currentTimeMillis();

		System.out.println("Time taken from EqualPartitioningOnLeafsAllocationAlgorithmTestCase is " + (dateB - dateA));
		System.out.println("Ratio is " + ration);

		assertTrue(ration < accurancy);

	}

}