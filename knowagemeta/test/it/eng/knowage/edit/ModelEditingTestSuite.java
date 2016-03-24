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
package it.eng.knowage.edit;

import it.eng.knowage.common.TestConstants;
import it.eng.knowage.impl.mysql.MySqlModelEditingTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class ModelEditingTestSuite extends TestCase {
	static public Test suite() {
		TestSuite suite = new TestSuite("Editing tests");
		if (TestConstants.enableTestsOnMySql)
			suite.addTest(MySqlModelEditingTestSuite.suite());
		// add here test suites of other databases ...
		return suite;
	}
}