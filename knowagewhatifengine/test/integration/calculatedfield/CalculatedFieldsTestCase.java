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
package integration.calculatedfield;

import integration.agorithms.AbstractWhatIfInMemoryTestCase;

public class CalculatedFieldsTestCase extends AbstractWhatIfInMemoryTestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testInjectCalculatedIntoMdxQuery() throws Exception {

		// WhatIfEngineInstance ei =
		// getWhatifengineiEngineInstance(getCatalogue());
		//
		// CalculatedMemberManager cm = new CalculatedMemberManager(ei);
		//
		// Member parentMember =
		// CubeUtilities.getMember(ei.getPivotModel().getCube(),
		// "[Product].[Food]");
		//
		// String cc = "[Product].[Food].[Dairy]";
		// cm.injectCalculatedIntoMdxQuery("name", cc, parentMember, Axis.ROWS);
		//
		// String s = ei.getPivotModel().getCurrentMdx();
		// System.out.println(s);
	}

}