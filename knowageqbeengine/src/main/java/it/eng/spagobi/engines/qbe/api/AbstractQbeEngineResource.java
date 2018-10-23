package it.eng.spagobi.engines.qbe.api;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.rest.AbstractRestService;
import it.eng.spagobi.utilities.engines.rest.ExecutionSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;

public class AbstractQbeEngineResource extends AbstractRestService {
	@Context
	protected HttpServletRequest request;
	@Context
	protected HttpServletResponse response;

	public static transient Logger logger = Logger.getLogger(AbstractRestService.class);

	@Override
	public HttpServletRequest getServletRequest() {
		// TODO Auto-generated method stub
		return request;
	}

	@Override
	public QbeEngineInstance getEngineInstance() {

		ExecutionSession es = getExecutionSession();
		return (QbeEngineInstance) es.getAttributeFromSession(EngineConstants.ENGINE_INSTANCE);

	}

	public UserProfile getUserProfile() {
		return UserProfileManager.getProfile();
	}

}
