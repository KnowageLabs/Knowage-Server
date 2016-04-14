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
package it.eng.spagobi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import it.eng.spagobi.tools.dataset.bo.DataSetParametersListTest;
import it.eng.spagobi.tools.dataset.bo.RESTDataSetTest;
import it.eng.spagobi.tools.dataset.common.dataproxy.RESTDataProxyTest;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReaderTest;
import it.eng.spagobi.tools.dataset.listener.DataStoreListenerOperatorTest;
import it.eng.spagobi.tools.dataset.notifier.fiware.ContextBrokerNotifierOperatorTest;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilitiesTest;
import it.eng.spagobi.utilities.json.JSONUtilsTest;
import it.eng.spagobi.utilities.rest.RestUtilitiesTest;

@RunWith(Suite.class)
@SuiteClasses({ it.eng.spagobi.security.hmacfilter.AllTests.class, DataSetParametersListTest.class, RESTDataSetTest.class, RESTDataProxyTest.class,
		JSONPathDataReaderTest.class, DataStoreListenerOperatorTest.class, ContextBrokerNotifierOperatorTest.class, DataSetUtilitiesTest.class,
		it.eng.spagobi.utilities.engines.rest.AllTests.class, JSONUtilsTest.class, RestUtilitiesTest.class })
public class AllTests {

}
