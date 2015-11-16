package it.eng.spagobi;

import it.eng.spagobi.tools.dataset.bo.DataSetParametersListTest;
import it.eng.spagobi.utilities.engines.rest.SimpleRestClientTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ it.eng.spagobi.security.hmacfilter.AllTests.class, SimpleRestClientTest.class, DataSetParametersListTest.class })
public class AllTests {

}
