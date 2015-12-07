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
