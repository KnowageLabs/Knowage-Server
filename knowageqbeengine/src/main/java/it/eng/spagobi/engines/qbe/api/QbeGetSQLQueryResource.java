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
				// KNOWAGE-9981 - START
				//String sqlQueryFormatted = formatQueryString(addAliasInSql(query, sqlQuery));
				String sqlQueryFormatted = formatQueryString(
				        preserveBooleanGroupingForDisplay(addAliasInSql(query, sqlQuery)));
				// KNOWAGE-9981 - END

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
	
	// KNOWAGE-9981 - START
	private String preserveBooleanGroupingForDisplay(String sqlQuery) {
	 
	    if (sqlQuery == null) {
	        return null;
	    }
	 
	    Matcher whereMatcher = Pattern.compile("(?i)\\bWHERE\\b").matcher(sqlQuery);
	 
	    if (!whereMatcher.find()) {
	        return sqlQuery;
	    }
	 
	    int whereStart = whereMatcher.end();
	 
	    Matcher endMatcher = Pattern.compile(
	            "(?i)\\b(GROUP\\s+BY|HAVING|ORDER\\s+BY|UNION|LIMIT|OFFSET)\\b")
	            .matcher(sqlQuery);
	 
	    int whereEnd = endMatcher.find(whereStart)
	            ? endMatcher.start()
	            : sqlQuery.length();
	 
	    String whereClause = sqlQuery.substring(whereStart, whereEnd).trim();
	    String cleanedWhereClause = removeRedundantPredicateParentheses(whereClause);
	    String groupedWhereClause = addExplicitAndGrouping(cleanedWhereClause);
	 
	    return sqlQuery.substring(0, whereStart)
	            + " "
	            + groupedWhereClause
	            + sqlQuery.substring(whereEnd);
	}
	 
	private String addExplicitAndGrouping(String expression) {
	 
	    int orPosition = findTopLevelOperator(expression, "OR");
	 
	    if (orPosition < 0) {
	        return expression;
	    }
	 
	    String left = expression.substring(0, orPosition).trim();
	    String right = expression.substring(orPosition + 2).trim();
	 
	    if (findTopLevelOperator(left, "AND") >= 0) {
	        left = "(" + left + ")";
	    }
	 
	    if (findTopLevelOperator(right, "OR") >= 0) {
	        right = addExplicitAndGrouping(right);
	    } else if (findTopLevelOperator(right, "AND") >= 0) {
	        right = "(" + right + ")";
	    }
	 
	    return left + " OR " + right;
	}
	 
	private int findTopLevelOperator(String expression, String operator) {
	 
	    int depth = 0;
	    char quote = 0;
	    boolean between = false;
	 
	    for (int i = 0; i < expression.length(); i++) {
	 
	        char current = expression.charAt(i);
	 
	        if (quote != 0) {
	            if (current == quote) {
	                quote = 0;
	            }
	            continue;
	        }
	 
	        if (current == '\'' || current == '"' || current == '`') {
	            quote = current;
	            continue;
	        }
	 
	        if (current == '(') {
	            depth++;
	            continue;
	        }
	 
	        if (current == ')') {
	            depth--;
	            continue;
	        }
	 
	        if (depth != 0) {
	            continue;
	        }
	 
	        if (isWordAt(expression, i, "BETWEEN")) {
	            between = true;
	            i += 6;
	            continue;
	        }
	 
	        if (isWordAt(expression, i, "AND")) {
	 
	            if (between) {
	                between = false;
	                i += 2;
	                continue;
	            }
	 
	            if ("AND".equals(operator)) {
	                return i;
	            }
	        }
	 
	        if ("OR".equals(operator) && isWordAt(expression, i, "OR")) {
	            return i;
	        }
	    }
	 
	    return -1;
	}
	 
	private boolean isWordAt(String text, int position, String word) {
	 
	    int end = position + word.length();
	 
	    return end <= text.length()
	&& text.regionMatches(true, position, word, 0, word.length())
	&& (position == 0 || !Character.isLetterOrDigit(text.charAt(position - 1)))
	&& (end == text.length() || !Character.isLetterOrDigit(text.charAt(end)));
	}
	 
	private String removeRedundantPredicateParentheses(String expression) {
	 
	    String value = expression.trim();
	 
	    if (isWrappedByParentheses(value)) {
	 
	        String inner = value.substring(1, value.length() - 1).trim();
	 
	        if (findTopLevelOperator(inner, "AND") < 0
	&& findTopLevelOperator(inner, "OR") < 0) {
	 
	            return removeRedundantPredicateParentheses(inner);
	        }
	 
	        return "(" + removeRedundantPredicateParentheses(inner) + ")";
	    }
	 
	    int orPosition = findTopLevelOperator(value, "OR");
	 
	    if (orPosition >= 0) {
	        return removeRedundantPredicateParentheses(
	                value.substring(0, orPosition))
	                + " OR "
	                + removeRedundantPredicateParentheses(
	                        value.substring(orPosition + 2));
	    }
	 
	    int andPosition = findTopLevelOperator(value, "AND");
	 
	    if (andPosition >= 0) {
	        return removeRedundantPredicateParentheses(
	                value.substring(0, andPosition))
	                + " AND "
	                + removeRedundantPredicateParentheses(
	                        value.substring(andPosition + 3));
	    }
	 
	    return value;
	}
	 
	private boolean isWrappedByParentheses(String expression) {
	 
	    if (expression.length() < 2
	            || expression.charAt(0) != '('
	            || expression.charAt(expression.length() - 1) != ')') {
	        return false;
	    }
	 
	    int depth = 0;
	    char quote = 0;
	 
	    for (int i = 0; i < expression.length(); i++) {
	 
	        char current = expression.charAt(i);
	 
	        if (quote != 0) {
	            if (current == quote) {
	                quote = 0;
	            }
	            continue;
	        }
	 
	        if (current == '\'' || current == '"' || current == '`') {
	            quote = current;
	            continue;
	        }
	 
	        if (current == '(') {
	            depth++;
	        } else if (current == ')') {
	            depth--;
	 
	            if (depth == 0 && i < expression.length() - 1) {
	                return false;
	            }
	        }
	    }
	 
	    return depth == 0;
	}
	// KNOWAGE-9981 - END


}