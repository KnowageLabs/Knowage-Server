package it.eng.spagobi.engines.qbe.api;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.hibernate.jdbc.util.BasicFormatterImpl;
import org.json.JSONObject;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.datasource.dataset.DataSetDataSource;
import it.eng.qbe.model.accessmodality.IModelAccessModality;
import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.IStatement;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.engines.qbe.services.core.ExecuteQueryAction;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;

@Path("/GetSqlQuery")
public class QbeGetSQLQueryResource extends AbstractQbeEngineResource {


	public static final String ENGINE_NAME = "SpagoBIQbeEngine";

   	public static transient Logger logger = Logger.getLogger(QbeGetSQLQueryResource.class);

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getSQLQuery(@FormParam("queryId") String queryId
	                          , @FormParam("replaceParametersWithQuestion") Boolean replaceParametersWithQuestion
							  , @FormParam("promptableFilters") String promptableFilters ) {
                        
		Query query = null;
		IStatement statement = null;

		logger.debug("IN");

		try {
			query = getEngineInstance().getQueryCatalogue().getQuery(queryId);
			if (query == null) {
				query = getEngineInstance().getQueryCatalogue().getFirstQuery();

			}
			Assert.assertNotNull(query, "Query not found!!");
			
            UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);
			IModelAccessModality accessModality = getEngineInstance().getDataSource().getModelAccessModality();
			Query filteredQuery = accessModality.getFilteredStatement(query, this.getEngineInstance().getDataSource(), userProfile.getUserAttributes());

			getEngineInstance().setActiveQuery(filteredQuery);
			JSONObject promptFileds = null;

			if (promptableFilters != null && !promptableFilters.isEmpty()) {
			    try {
			    	if (replaceParametersWithQuestion) {
						// promptable filters values may come with request (read-only
						// user modality)
						QbeQueryResource.updatePromptableFiltersValue(filteredQuery, true, promptableFilters);
					} else {
						// promptable filters values may come with request (read-only
						// user modality)
						QbeQueryResource.updatePromptableFiltersValue(filteredQuery, promptableFilters);
					}
			    } catch (Exception e) {
			        logger.error("Errore nel parsing dei promptableFilters", e);
			        promptFileds = new JSONObject();
			    }
			}

			statement = getEngineInstance().getStatment();
			statement.setParameters(getEnv());

			JSONObject toReturn = new JSONObject();

			IDataSource dataSource = statement.getDataSource();
			if (dataSource instanceof DataSetDataSource) {
				String sqlQuery = statement.getSqlQueryString();
				String sqlQueryFormatted = formatQueryString(sqlQuery);

				logger.debug("Executable query (SQL): [" + sqlQuery + "]");

				toReturn.put("sql", sqlQuery);
				toReturn.put("sqlFormatted", sqlQueryFormatted);
			} else {
				String jpaQueryStr = statement.getQueryString();
				String sqlQuery = statement.getSqlQueryString();
				String jpaQueryStrFormatted = formatQueryString(addAliasInJqpl(query, jpaQueryStr));
				String sqlQueryFormatted = formatQueryString(addAliasInSql(query, sqlQuery));

				logger.debug("Executable query (HQL/JPQL): [" + jpaQueryStr + "]");
				logger.debug("Executable query (SQL): [" + sqlQuery + "]");

				toReturn.put("sql", sqlQuery);
				toReturn.put("jpqlFormatted", jpaQueryStrFormatted);
				toReturn.put("sqlFormatted", sqlQueryFormatted);
			}

 			return Response.status(200).entity(toReturn.toString()).build();	

		} catch (Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(this.getClass().getName(), getEngineInstance(), t);
		} finally {
			logger.debug("OUT");
		}
    }

	/**
	 * Get the query string of the passed Query. The returned query is formatted
	 *
	 * @param dataSource
	 * @param query
	 * @return
	 */
	public String formatQueryString(String queryString) {
		String formattedQuery;
		BasicFormatterImpl fromatter;

		if (queryString == null || queryString.equals("")) {
			logger.error("Impossible to get the query string because the query is null");
			return "";
		}

		fromatter = new BasicFormatterImpl();
		formattedQuery = fromatter.format(queryString);
		return StringUtilities.fromStringToHTML(formattedQuery);
	}

	public String addAliasInSql(Query query, String queryString) {
		String pattern = "col_(.*?)\\w+";
		List<ISelectField> selectFields = query.getSelectFields(true);
		if (selectFields != null) {

			for (int i = 0; i < selectFields.size(); i++) {
				Pattern r = Pattern.compile(pattern);
				Matcher m = r.matcher(queryString);

				String fieldAlias = selectFields.get(i).getAlias();
				if (fieldAlias == null) {
					fieldAlias = selectFields.get(i).getName();
					fieldAlias = fieldAlias.replace(" ", "_");
				}
				fieldAlias = "`" + fieldAlias + "`";
				queryString = m.replaceFirst(fieldAlias);

			}
		}

		return queryString;
	}

	public String addAliasInJqpl(Query query, String queryString) {
		List<ISelectField> selectFields = query.getSelectFields(true);
		if (selectFields != null) {
			String[] splitted = queryString.split(",");
			for (int i = 0; i < selectFields.size(); i++) {

				String fieldAlias = selectFields.get(i).getAlias();
				if (fieldAlias == null) {
					fieldAlias = selectFields.get(i).getName();
					fieldAlias = fieldAlias.replace(" ", "_");
				}
				fieldAlias = "`" + fieldAlias + "`";

				if (selectFields.size() - 1 == i) {
					int fromPosition = queryString.indexOf(" FROM ");
					queryString = queryString.substring(0, fromPosition) + "as " + fieldAlias + queryString.substring(fromPosition);

				} else {
					String queryPeace = splitted[i];
					int startColAlias = queryString.indexOf(queryPeace + ",");

					queryString = queryString.substring(0, startColAlias + queryPeace.length()) + "as " + fieldAlias
							+ queryString.substring(startColAlias + queryPeace.length());
				}
			}
		}
		return queryString;
	}


}