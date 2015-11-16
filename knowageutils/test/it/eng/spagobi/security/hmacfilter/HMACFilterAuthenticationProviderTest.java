package it.eng.spagobi.security.hmacfilter;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
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
		HeaderChanges.changed = false;

		provider = new HMACFilterAuthenticationProvider(HMACFilterTest.key, new SystemTimeHMACTokenValidator(HMACFilter.MAX_TIME_DELTA_DEFAULT_MS));
		httpExecutor = new ApacheHttpClientExecutor();
	}

	@Test
	public void testProvideAuthenticationGetSuccess() throws Exception {
		ClientRequest request = new ClientRequest("http://localhost:8080/hmac", httpExecutor);
		request.queryParameter("a", "b");
		request.queryParameter("c", "d");
		request.header(HMACFilter.HEADERS_SIGNED.get(0), "z");
		request.header("r", "f");
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
		request.header(HMACFilter.HEADERS_SIGNED.get(0), "z");
		request.header("r", "f");
		provider = new HMACFilterAuthenticationProvider("cde", new SystemTimeHMACTokenValidator(HMACFilter.MAX_TIME_DELTA_DEFAULT_MS));
		provider.provideAuthentication(request);
		ClientResponse<?> resp = request.get();
		Assert.assertTrue(resp.getStatus() >= 400);
		Assert.assertFalse(DummyServlet.arrived);
	}

	@Test
	public void testProvideAuthenticationGetFailUserManInTheMiddle() throws Exception {
		ClientRequest request = new ClientRequest("http://localhost:8080/hmac", httpExecutor);
		request.queryParameter("a", "b");
		request.queryParameter("c", "d");
		request.header("r", "f");
		request.header(HMACFilter.HEADERS_SIGNED.get(0), "z");
		provider.provideAuthentication(request);
		HMACFilterTest.stopHMACFilterServer();
		HMACFilterTest.startHMACFilterServer(HeaderChanges.class);

		ClientResponse<?> resp;
		try {
			resp = request.get();
		} finally {
			HMACFilterTest.stopHMACFilterServer();
			HMACFilterTest.startHMACFilterServer();
		}
		Assert.assertTrue(HeaderChanges.changed);
		Assert.assertTrue(resp.getStatus() >= 400);
		Assert.assertFalse(DummyServlet.arrived);
	}

	public static class HeaderChanges implements Filter {

		public static boolean changed;

		@Override
		public void destroy() {
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
			HttpServletRequestWrapper hsrw = new HttpServletRequestWrapper((HttpServletRequest) request) {
				@Override
				public String getHeader(String name) {
					if (name.equals(HMACFilter.HEADERS_SIGNED.get(0))) {
						changed = true;
						return "manInTheMiddle";
					}
					return super.getHeader(name);
				}
			};
			chain.doFilter(hsrw, response);
		}

		@Override
		public void init(FilterConfig filterConfig) throws ServletException {
		}

	}

	@Test
	public void testProvideAuthenticationGetFailToken() throws Exception {
		ClientRequest request = new ClientRequest("http://localhost:8080/hmac", httpExecutor);
		request.queryParameter("a", "b");
		request.queryParameter("c", "d");
		request.header(HMACFilter.HEADERS_SIGNED.get(0), "z");
		request.header("r", "f");
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
		request.header(HMACFilter.HEADERS_SIGNED.get(0), "z");
		request.header("r", "f");
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
		request.header(HMACFilter.HEADERS_SIGNED.get(0), "z");
		request.header("r", "f");
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
		request.header(HMACFilter.HEADERS_SIGNED.get(0), "z");
		request.header("r", "f");
		byte[] body = new byte[] { 12, 100, 24, 38 };
		request.body(MediaType.APPLICATION_OCTET_STREAM_TYPE, new ByteArrayInputStream(body));
		provider.provideAuthentication(request);
		ClientResponse<?> resp = request.post();
		Assert.assertEquals(200, resp.getStatus());
		Assert.assertTrue(DummyServlet.arrived);
		Assert.assertArrayEquals(body, DummyServlet.body);
	}

}
