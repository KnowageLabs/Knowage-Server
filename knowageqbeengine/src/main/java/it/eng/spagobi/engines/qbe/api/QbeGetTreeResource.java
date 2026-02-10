package it.eng.spagobi.engines.qbe.api;

import java.util.Iterator;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.qbe.model.structure.filter.IQbeTreeEntityFilter;
import it.eng.qbe.model.structure.filter.IQbeTreeFieldFilter;
import it.eng.qbe.model.structure.filter.QbeTreeAccessModalityEntityFilter;
import it.eng.qbe.model.structure.filter.QbeTreeAccessModalityFieldFilter;
import it.eng.qbe.model.structure.filter.QbeTreeFilter;
import it.eng.qbe.model.structure.filter.QbeTreeOrderEntityFilter;
import it.eng.qbe.model.structure.filter.QbeTreeOrderFieldFilter;
import it.eng.qbe.model.structure.filter.QbeTreeQueryEntityFilter;
import it.eng.qbe.query.Query;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.qbe.tree.ExtJsQbeTreeBuilder;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;

@Path("/GetTree")
public class QbeGetTreeResource extends AbstractQbeEngineResource {

	// INPUT PARAMETERS
	public static final String QUERY_ID = "parentQueryId";
	public static final String DATAMART_NAME = "datamartName";

	public static final String ENTITIES = "entities";

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(QbeGetTreeResource.class);

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getTree(@QueryParam("datamartName") String datamartName, @QueryParam("openDatasetInQbe") @DefaultValue("false")Boolean openDatasetInQbe) {
		String queryId = null;
		Query query = null;

		IQbeTreeEntityFilter entityFilter = null;
		IQbeTreeFieldFilter fieldFilter = null;
		QbeTreeFilter treeFilter = null;

		ExtJsQbeTreeBuilder qbeBuilder = null;
		JSONObject node = new JSONObject();
		JSONArray nodes = null;
		logger.debug("IN");

		UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);

		// queryId = getAttributeAsString(QUERY_ID);
		// logger.debug("Parameter [" + QUERY_ID + "] is equals to [" + queryId + "]");

		Assert.assertNotNull(getEngineInstance(),
				"It's not possible to execute GetTree service before having properly created an instance of EngineInstance class");

		try {
			logger.debug("Filtering entities list ...");
			entityFilter = new QbeTreeAccessModalityEntityFilter();
			logger.debug("Apply entity filter [" + entityFilter.getClass().getName() + "]");
			if (queryId != null) {
				logger.debug("Filtering on query [" + queryId + "] selectd entities");
				query = getEngineInstance().getQueryCatalogue().getQuery(queryId);
				if (query != null) {
					entityFilter = new QbeTreeQueryEntityFilter(entityFilter, query);
				}
			}
			entityFilter = new QbeTreeOrderEntityFilter(entityFilter);
			logger.debug("Apply field filter [" + entityFilter.getClass().getName() + "]");

			logger.debug("Filtering fields list ...");
			fieldFilter = new QbeTreeAccessModalityFieldFilter();
			logger.debug("Apply field filter [" + fieldFilter.getClass().getName() + "]");
			fieldFilter = new QbeTreeOrderFieldFilter(fieldFilter);
			logger.debug("Apply field filter [" + fieldFilter.getClass().getName() + "]");

			treeFilter = new QbeTreeFilter(entityFilter, fieldFilter);

			qbeBuilder = new ExtJsQbeTreeBuilder(treeFilter);

			if (!openDatasetInQbe && datamartName != null && !datamartName.equals("null")) {
				// if(datamartName.equals("null"))
				// datamartName="";
				nodes = qbeBuilder.getQbeTree(getEngineInstance().getDataSource(), getLocale(), datamartName, userProfile);

			} else {
				nodes = new JSONArray();
				Iterator<String> it = getEngineInstance().getDataSource().getModelStructure(userProfile).getModelNames().iterator();
				while (it.hasNext()) {
					String modelName = it.next();
					JSONArray temp = qbeBuilder.getQbeTree(getEngineInstance().getDataSource(), getLocale(), modelName, userProfile);
					for (int i = 0; i < temp.length(); i++) {
						Object object = temp.get(i);
						nodes.put(object);
					}
				}

			}

			node.put(ENTITIES, nodes);
			// writeBackToClient(new JSONSuccess(node));
			return Response.status(200).entity(node.toString()).build();

		} catch (Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("GetTree", getEngineInstance(), t);
		} finally {
			logger.debug("OUT");
		}

	}
}
