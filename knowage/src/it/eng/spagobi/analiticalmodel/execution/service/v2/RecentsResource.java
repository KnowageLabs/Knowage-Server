package it.eng.spagobi.analiticalmodel.execution.service.v2;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.hotlink.constants.HotLinkConstants;
import it.eng.spagobi.monitoring.dao.AuditManager;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/2.0/recents")
@ManageAuthorization
public class RecentsResource extends AbstractSpagoBIResource {

	List recentsList = null;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List getRecentsOfUserByUserId() {
		logger.debug("IN");
		try {
			SourceBean hotlinkSB = (SourceBean) ConfigSingleton.getInstance().getAttribute(HotLinkConstants.HOTLINK);
			SourceBean myRecentlyUsed = (SourceBean) hotlinkSB.getFilteredSourceBeanAttribute("SECTION", "name", HotLinkConstants.MY_RECENTLY_USED);
			int limit = Integer.parseInt((String) myRecentlyUsed.getAttribute(HotLinkConstants.ROWS_NUMBER));
			recentsList = AuditManager.getInstance().getMyRecentlyUsed(getUserProfile(), limit);
		} catch (Exception e) {
			logger.error("Error while recovering favourites of user [" + getUserProfile() + "]", e);
		} finally {
			logger.debug("OUT");
		}
		return recentsList;
	}
}
