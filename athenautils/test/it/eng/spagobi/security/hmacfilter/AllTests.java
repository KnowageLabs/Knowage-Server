package it.eng.spagobi.security.hmacfilter;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ HMACFilterAuthenticationProviderTest.class, HMACFilterTest.class, MultiReadHttpServletRequestTest.class })
public class AllTests {

}
