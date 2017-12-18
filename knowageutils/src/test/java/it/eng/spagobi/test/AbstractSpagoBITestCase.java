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
package it.eng.spagobi.test;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class AbstractSpagoBITestCase extends TestCase {

	protected boolean performTearDown;

	static protected Logger logger = Logger.getLogger(AbstractSpagoBITestCase.class);

	public AbstractSpagoBITestCase() {
		super();
	}

	@Override
	public void setUp() throws Exception {
		try {
			performTearDown = true;
		} catch (Exception t) {
			logger.error("An unespected error occurred during setUp: ");
			t.printStackTrace();
			throw t;
		}
	}

	@Override
	public void tearDown() throws Exception {
		if (performTearDown) {
			doTearDown();
		}
	}

	protected void doTearDown() {

	}
}
