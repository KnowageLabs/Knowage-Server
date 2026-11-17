package it.eng.spagobi.commons.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import javax.ws.rs.core.MediaType;

import org.eclipse.persistence.jpa.rs.resources.unversioned.PersistenceUnitResource;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.eng.spagobi.services.rest.annotations.PublicService;

public class SamlMetadataResourceTest {
	private String previousEnabled;
	private ResteasyProviderFactory previousProviderFactory;

	@Before
	public void setup() {
		previousEnabled = System.getProperty("knowage.saml.enabled");
		previousProviderFactory = ResteasyProviderFactory.getInstance();
		System.setProperty("knowage.saml.enabled", "false");
	}

	@After
	public void cleanup() {
		if (previousEnabled == null) {
			System.clearProperty("knowage.saml.enabled");
		} else {
			System.setProperty("knowage.saml.enabled", previousEnabled);
		}
		ResteasyProviderFactory.setInstance(previousProviderFactory);
	}

	@Test
	public void shouldReturnNotFoundForDisabledSamlInsteadOfSelectingJpaMetadata() throws Exception {
		for (boolean jpaFirst : new boolean[] { true, false }) {
			Dispatcher dispatcher = dispatcher(jpaFirst);
			for (String path : new String[] { "/saml/metadata", "/saml/metadata/" }) {
				Method method = matchedMethod(dispatcher, MockHttpRequest.get(path));
				assertEquals(SamlMetadataResource.class, method.getDeclaringClass());
				assertTrue(method.isAnnotationPresent(PublicService.class));

				MockHttpResponse response = new MockHttpResponse();
				dispatcher.invoke(MockHttpRequest.get(path), response);
				assertEquals(404, response.getStatus());
			}
		}
	}

	@Test
	public void shouldPreserveOtherSamlRoutesAndJpaMetadata() throws Exception {
		Dispatcher dispatcher = dispatcher(true);
		assertEquals(SamlResource.class, matchedMethod(dispatcher, MockHttpRequest.get("/saml/login")).getDeclaringClass());
		for (String path : new String[] { "/saml/acs", "/saml/slo", "/saml/exchange" }) {
			MockHttpRequest request = MockHttpRequest.post(path).contentType(
					path.endsWith("/exchange") ? MediaType.APPLICATION_JSON_TYPE : MediaType.APPLICATION_FORM_URLENCODED_TYPE);
			assertEquals(SamlResource.class, matchedMethod(dispatcher, request).getDeclaringClass());
		}
		assertEquals(PersistenceUnitResource.class,
				matchedMethod(dispatcher, MockHttpRequest.get("/other-context/metadata")).getDeclaringClass());
	}

	private Dispatcher dispatcher(boolean jpaFirst) {
		Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();
		if (jpaFirst) {
			dispatcher.getRegistry().addPerRequestResource(PersistenceUnitResource.class);
		}
		dispatcher.getRegistry().addPerRequestResource(SamlResource.class);
		dispatcher.getRegistry().addPerRequestResource(SamlMetadataResource.class);
		if (!jpaFirst) {
			dispatcher.getRegistry().addPerRequestResource(PersistenceUnitResource.class);
		}
		return dispatcher;
	}

	private Method matchedMethod(Dispatcher dispatcher, MockHttpRequest request) {
		return ((ResourceMethodInvoker) dispatcher.getRegistry().getResourceInvoker(request)).getMethod();
	}
}
