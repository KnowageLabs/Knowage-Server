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
