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
import it.eng.spagobi.engines.whatif.common.WhatIfConstants;
import it.eng.spagobi.engines.whatif.model.SpagoBICellSetWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.EqualPartitioningOnLeafsAllocationAlgorithm;
import test.DbConfigContainer;

public class LeafsNodesTestCase extends AbstractWhatIfInMemoryTestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testWithQuery() throws Exception {
		WhatIfEngineInstance ei = getWhatifengineiEngineInstance(getCatalogue());
		SpagoBIPivotModel pivotModel = (SpagoBIPivotModel) ei.getPivotModel();

		SpagoBICellSetWrapper cellSetWrapper = (SpagoBICellSetWrapper) pivotModel.getCellSet();
		SpagoBICellWrapper cellWrapper = (SpagoBICellWrapper) cellSetWrapper.getCell(0);

		cellWrapper.getMembers()[1].setProperty(WhatIfConstants.MEMBER_PROPERTY_LEAF, null);

		EqualPartitioningOnLeafsAllocationAlgorithm ae = new EqualPartitioningOnLeafsAllocationAlgorithm(ei);

		Long leafs = ae.getDataManager().getLeafs(cellWrapper.getMembers());

		System.out.println(leafs);
	}

	@Override
	public String getCatalogue() {
		return DbConfigContainer.getMySqlCatalogue();
	}

	@Override
	public String getTemplate() {
		return DbConfigContainer.getMySqlTemplate();
	}

}
