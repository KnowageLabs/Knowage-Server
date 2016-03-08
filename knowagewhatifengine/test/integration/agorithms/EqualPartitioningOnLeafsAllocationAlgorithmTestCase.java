/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author ghedin
 *
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