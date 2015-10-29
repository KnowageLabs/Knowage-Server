package it.eng.spagobi;

import it.eng.spagobi.utilities.engines.rest.SimpleRestClientTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ it.eng.spagobi.security.hmacfilter.AllTests.class, SimpleRestClientTest.class })
public class AllTests {

}
