package it.eng.knowage.resourcemanager.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.error.KnowageBusinessException;
import it.eng.knowage.resourcemanager.resource.dto.EngGptDataSaveDTO;
import it.eng.knowage.resourcemanager.service.ResourceManagerAPI;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

@ExtendWith(MockitoExtension.class)
class EngGptDataResourceTest {

	@Mock
	private ResourceManagerAPI resourceManagerAPIservice;

	@Mock
	private BusinessRequestContext businessContext;

	@Mock
	private SpagoBIUserProfile profile;

	@InjectMocks
	private EngGptDataResource resource;

	@Test
	void getEngGptDataReturnsJsonResponse() throws KnowageBusinessException {
		when(businessContext.getUserProfile()).thenReturn(profile);
		when(resourceManagerAPIservice.getEngGptData("model-1", profile)).thenReturn("{\"sql\":\"SELECT 1\"}");

		Response response = resource.getEngGptData("model-1");

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		assertEquals("{\"sql\":\"SELECT 1\"}", response.getEntity());
		assertEquals("application", response.getMediaType().getType());
		assertEquals("json", response.getMediaType().getSubtype());
		assertEquals("utf-8", response.getMediaType().getParameters().get("charset").toLowerCase(Locale.ROOT));
	}

	@Test
	void saveEngGptDataDelegatesToService() throws KnowageBusinessException {
		when(businessContext.getUserProfile()).thenReturn(profile);

		Response response = resource.saveEngGptData("model-1", "{\"sql\":\"SELECT 1\"}");

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		verify(resourceManagerAPIservice).saveEngGptData("model-1", "{\"sql\":\"SELECT 1\"}", profile);
	}

	@Test
	void saveEngGptDataDelegatesToServiceUsingFrontendWrapper() throws KnowageBusinessException {
		when(businessContext.getUserProfile()).thenReturn(profile);

		String requestBody = "{\"modelId\":\"1\",\"jsonContent\":\"{\\\"sql_gold\\\":[{\\\"NL\\\":\\\"fjsjgkfgjksgjksgd\\\",\\\"SQL\\\":\\\"select 'pippo' as pippo from dual\\\",\\\"tables\\\":[],\\\"columns\\\":[]}]}\"}";
		Response response = resource.saveEngGptData("1", requestBody);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		verify(resourceManagerAPIservice).saveEngGptData("1",
				"{\"sql_gold\":[{\"NL\":\"fjsjgkfgjksgjksgd\",\"SQL\":\"select 'pippo' as pippo from dual\",\"tables\":[],\"columns\":[]}]}",
				profile);
	}

	@Test
	void saveEngGptDataDelegatesToServiceUsingDto() throws KnowageBusinessException {
		when(businessContext.getUserProfile()).thenReturn(profile);

		EngGptDataSaveDTO dto = new EngGptDataSaveDTO("1",
				"{\"sql_gold\":[{\"NL\":\"fjsjgkfgjksgjksgd\",\"SQL\":\"select 'pippo' as pippo from dual\",\"tables\":[],\"columns\":[]}]}");
		Response response = resource.saveEngGptData(dto);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		verify(resourceManagerAPIservice).saveEngGptData("1",
				"{\"sql_gold\":[{\"NL\":\"fjsjgkfgjksgjksgd\",\"SQL\":\"select 'pippo' as pippo from dual\",\"tables\":[],\"columns\":[]}]}",
				profile);
	}

	@Test
	void deleteEngGptDataDelegatesToService() throws KnowageBusinessException {
		when(businessContext.getUserProfile()).thenReturn(profile);

		Response response = resource.deleteEngGptData("model-1");

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		verify(resourceManagerAPIservice).deleteEngGptData("model-1", profile);
	}

	@Test
	void getEngGptDataWrapsBusinessExceptionWithLocale() throws KnowageBusinessException {
		KnowageBusinessException serviceException = new KnowageBusinessException("not found");
		serviceException.setStatus(Response.Status.NOT_FOUND);
		serviceException.setCode("KN-RM-012");
		serviceException.setDescription("No sqlGold.json found for the specified model");

		when(businessContext.getUserProfile()).thenReturn(profile);
		when(businessContext.getLocale()).thenReturn(Locale.ITALIAN);
		when(resourceManagerAPIservice.getEngGptData("model-1", profile)).thenThrow(serviceException);

		KnowageBusinessException thrown = assertThrows(KnowageBusinessException.class, () -> resource.getEngGptData("model-1"));

		assertEquals(Response.Status.NOT_FOUND, thrown.getStatus());
		assertEquals("KN-RM-012", thrown.getCode());
		assertEquals("No sqlGold.json found for the specified model", thrown.getDescription());
		assertEquals(Locale.ITALIAN, thrown.getLocale());
	}
}
