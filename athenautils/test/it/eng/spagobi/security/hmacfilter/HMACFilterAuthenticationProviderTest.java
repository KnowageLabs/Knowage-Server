package it.eng.spagobi.security.hmacfilter;

import java.io.ByteArrayInputStream;

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class HMACFilterAuthenticationProviderTest {

	private HMACFilterAuthenticationProvider provider;

	private ApacheHttpClientExecutor httpExecutor;

	@BeforeClass
	public static void setUpClass() throws Exception {
		// Create Server
		HMACFilterTest.startHMACFilterServer();
	}

	@AfterClass
	public static void teatDownClass() throws Exception {
		// Create Server
		HMACFilterTest.stopHMACFilterServer();
	}

	@Before
	public void setUp() throws Exception {
		DummyServlet.arrived = false;
		DummyServlet.body = null;
		provider = new HMACFilterAuthenticationProvider(HMACFilterTest.key, new SystemTimeHMACTokenValidator(HMACFilter.MAX_TIME_DELTA_DEFAULT_MS));
		httpExecutor = new ApacheHttpClientExecutor();
	}

	@Test
	public void testProvideAuthenticationGetSuccess() throws Exception {
		ClientRequest request = new ClientRequest("http://localhost:8080/hmac", httpExecutor);
		request.queryParameter("a", "b");
		request.queryParameter("c", "d");
		provider.provideAuthentication(request);
		ClientResponse<?> resp = request.get();
		Assert.assertEquals(200, resp.getStatus());
		Assert.assertTrue(DummyServlet.arrived);
	}

	@Test
	public void testProvideAuthenticationGetFailKey() throws Exception {
		ClientRequest request = new ClientRequest("http://localhost:8080/hmac", httpExecutor);
		request.queryParameter("a", "b");
		request.queryParameter("c", "d");
		provider = new HMACFilterAuthenticationProvider("cde", new SystemTimeHMACTokenValidator(HMACFilter.MAX_TIME_DELTA_DEFAULT_MS));
		provider.provideAuthentication(request);
		ClientResponse<?> resp = request.get();
		Assert.assertTrue(resp.getStatus() >= 400);
		Assert.assertFalse(DummyServlet.arrived);
	}

	@Test
	public void testProvideAuthenticationGetFailToken() throws Exception {
		ClientRequest request = new ClientRequest("http://localhost:8080/hmac", httpExecutor);
		request.queryParameter("a", "b");
		request.queryParameter("c", "d");
		provider = new HMACFilterAuthenticationProvider(HMACFilterTest.key, new HMACTokenValidator() {

			@Override
			public void validate(String token) throws HMACSecurityException {

			}

			@Override
			public String generateToken() throws HMACSecurityException {
				return "ju67";
			}
		});
		provider.provideAuthentication(request);
		ClientResponse<?> resp = request.get();
		Assert.assertTrue(resp.getStatus() >= 400);
		Assert.assertFalse(DummyServlet.arrived);
	}

	@Test
	public void testProvideAuthenticationPostSuccess() throws Exception {
		ClientRequest request = new ClientRequest("http://localhost:8080/hmac", httpExecutor);
		request.queryParameter("a", "b"); // with also params in URI
		request.queryParameter("c", "d");
		request.body(MediaType.TEXT_PLAIN, "k=t&r=f");
		provider.provideAuthentication(request);
		ClientResponse<?> resp = request.post();
		Assert.assertEquals(200, resp.getStatus());
		Assert.assertTrue(DummyServlet.arrived);
	}

	@Test
	public void testProvideAuthenticationPostFail() throws Exception {
		ClientRequest request = new ClientRequest("http://localhost:8080/hmac", httpExecutor);
		request.queryParameter("a", "b"); // with also params in URI
		request.queryParameter("c", "d");
		request.body(MediaType.TEXT_PLAIN, "abcdhjk");
		// without providing authentication
		ClientResponse<?> resp = request.post();
		Assert.assertTrue(resp.getStatus() >= 400);
		Assert.assertFalse(DummyServlet.arrived);
	}

	@Test
	public void testProvideAuthenticationPostSuccessMediaType() throws Exception {
		ClientRequest request = new ClientRequest("http://localhost:8080/hmac", httpExecutor);
		request.queryParameter("a", "b"); // with also params in URI
		request.queryParameter("c", "d");
		byte[] body = new byte[] { 12, 100, 24, 38 };
		request.body(MediaType.APPLICATION_OCTET_STREAM_TYPE, new ByteArrayInputStream(body));
		provider.provideAuthentication(request);
		ClientResponse<?> resp = request.post();
		Assert.assertEquals(200, resp.getStatus());
		Assert.assertTrue(DummyServlet.arrived);
		Assert.assertArrayEquals(body, DummyServlet.body);
	}

}
