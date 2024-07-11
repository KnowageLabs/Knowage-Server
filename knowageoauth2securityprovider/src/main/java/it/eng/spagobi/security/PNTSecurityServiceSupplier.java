/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.jayway.jsonpath.JsonPath;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.KnowageStringUtils;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class PNTSecurityServiceSupplier extends OIDCFullIdTokenSecurityServiceSupplier {

	private static Logger logger = Logger.getLogger(PNTSecurityServiceSupplier.class);

	private static final String DEFAULT_DATASOURCE_NAME = "DWH";

	private static final String QUERY_FOR_STRUTTURE = "select DISTINCT denominazione_azienda from anagrafiche.ts_asl";
	private static final String QUERY_FOR_REGIONI = "select DISTINCT regione from anagrafiche.italian_geography";

	private static final String REGIONE_ATTRIBUTE = "REGIONE";
	private static final String STRUTTURA_ATTRIBUTE = "STRUTTURA";
	private static final String ROLE_KN_ADMIN = "KN_ADMIN";
	private static final String ROLE_KN_DEV = "KN_DEV";
	private static final String ROLE_KN_GOVERNO = "KN_GOVERNO";
	private static final String FULL_VISILIBITY_ATTRIBUTE_VALUE = "*";
	private static final String NO_VISILIBITY_ATTRIBUTE_VALUE = "";

	private static final String ORGANIZATIONS_JSON_PATH = "$.applications[?(@.name == 'Knowage')].profiles[*].organizations[*]";

	@Override
	protected Map<String, String> getUserAttributes(DecodedJWT decodedJWT) {
		try {
			String[] rolesArray = this.getUserRoles(decodedJWT);
			if (rolesArray == null || rolesArray.length == 0) {
				logger.debug("No roles detected, returning empty profile attributes.");
				return getNoVisibilityAttributes();
			}
			List<String> roles = Arrays.asList(rolesArray);
			if (roles.contains(ROLE_KN_ADMIN) || roles.contains(ROLE_KN_DEV) || roles.contains(ROLE_KN_GOVERNO)) {
				logger.debug("A role between " + ROLE_KN_ADMIN + ", " + ROLE_KN_DEV + " or " + ROLE_KN_GOVERNO
						+ "was detected, returning full visibility attributes");
				return getFullVisibilityAttributes();
			}
			return getPartialVisibilityAttributes(decodedJWT);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("An error occured while getting attributes from JWT token", e);
		}
	}

	private Map<String, String> getPartialVisibilityAttributes(DecodedJWT decodedJWT) {
		List<String> regioni = new ArrayList<>();
		List<String> strutture = new ArrayList<>();

		List<String> allRegioni = getAllRegioni();
		List<String> allStrutture = getAllStrutture();

		try {
			String payload = decodedJWT.getPayload();
			String decodedPayload = new String(Base64.getDecoder().decode(payload));

			net.minidev.json.JSONArray parsed = JsonPath.read(decodedPayload, ORGANIZATIONS_JSON_PATH);
			LogMF.debug(logger, "Got parsed organizations [{0}]", parsed);
			if (parsed == null || parsed.isEmpty()) {
				logger.debug("No organizations detected");
				return getNoVisibilityAttributes();
			}

			parsed.forEach(elem -> {
				String organization = (String) elem;
				if (allRegioni.stream().anyMatch(organization::equalsIgnoreCase)) { // if it is a valid regione (case insensitive check), put it inside the list
					regioni.add(organization);
				} else if (allStrutture.stream().anyMatch(organization::equalsIgnoreCase)) { // if it is a valid struttura (case insensitive check), put it
																								// inside the list
					strutture.add(organization);
				} else {
					logger.warn("Organization [" + organization + "] not recognized neither as a 'regione' nor as a 'struttura'");
				}
			});
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("An error occurred while parsing json", e);
		}

		Map<String, String> attributes = new HashMap<>();
		attributes.put(REGIONE_ATTRIBUTE, joinValues(regioni));
		attributes.put(STRUTTURA_ATTRIBUTE, joinValues(strutture));
		return attributes;
	}

	private Map<String, String> getFullVisibilityAttributes() {
		Map<String, String> attributes = new HashMap<>();
		attributes.put(REGIONE_ATTRIBUTE, FULL_VISILIBITY_ATTRIBUTE_VALUE);
		attributes.put(STRUTTURA_ATTRIBUTE, FULL_VISILIBITY_ATTRIBUTE_VALUE);
		return attributes;
	}

	private Map<String, String> getNoVisibilityAttributes() {
		Map<String, String> attributes = new HashMap<>();
		attributes.put(REGIONE_ATTRIBUTE, NO_VISILIBITY_ATTRIBUTE_VALUE);
		attributes.put(STRUTTURA_ATTRIBUTE, NO_VISILIBITY_ATTRIBUTE_VALUE);
		return attributes;
	}

	private List<String> getAllStrutture() {
		return getValues(QUERY_FOR_STRUTTURE);
	}

	private List<String> getAllRegioni() {
		return getValues(QUERY_FOR_REGIONI);
	}

	private String joinValues(List<String> values) {
		if (values.isEmpty()) {
			return "";
		}
		if (values.size() == 1) {
			return values.get(0);
		}
		// @formatter:off
		return  "'" +
				String.join("','",
						values
							.stream()
							.map(KnowageStringUtils::escapeSql) // SQL escape for each region
							.collect(Collectors.toList())) +
				"'";
		// @formatter:on
	}

	private List<String> getValues(String query) {
		try {
			IDataSource ds = getDataSource();
			IDataStore dataStore = ds.executeStatement(query, 0, 0);
			return dataStore.getFieldValues(0);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("An error occurred while executing statement [" + query + "]", e);
		}
	}

	private IDataSource getDataSource() {
		String datasourceName = getDataSourceName();
		LogMF.debug(logger, "Data source name is [{0}]", datasourceName);
		IDataSource ds = null;
		try {
			ds = DAOFactory.getDataSourceDAO().findDataSourceByLabel(datasourceName);
			if (ds == null) {
				throw new SpagoBIRuntimeException("Data source with name [" + datasourceName + "] not found");
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cloud not get data source with name [" + datasourceName + "]", e);
		}
		return ds;
	}

	private String getDataSourceName() {
		return Optional.ofNullable(System.getProperty("pnt_profiling_datasource", System.getenv("PNT_PROFILING_DATASOURCE"))).orElse(DEFAULT_DATASOURCE_NAME);
	}

	// @formatter:off
	/* TO BE ENABLED WITH A JNDI CONNECTION

	private static final String DEFAULT_JNDI_DATASOURCE_NAME = "java:/comp/env/jdbc/dwh";

	protected List<String> getValues(String statement) {
		try {
			final Context ctx = new InitialContext();
			return getValues(ctx, statement);
		} catch (NamingException e) {
			logger.error("Unable to initialize the context:" + e.getMessage(), e);
			throw new SpagoBIRuntimeException("Unable to initialize the context:" + e.getMessage(), e);
		}
	}

	protected List<String> getValues(Context ctx, String statement) {
		String datasourceJNDIName = getDatasourceJNDIName();
		try {
			final DataSource ds = (DataSource) ctx.lookup(datasourceJNDIName);
			return getValues(ds, statement);
		} catch (NamingException e) {
			logger.error("Unable to lookup the datasource " + datasourceJNDIName + " :" + e.getMessage(), e);
			throw new SpagoBIRuntimeException("Unable to lookup the datasource " + datasourceJNDIName + " :" + e.getMessage(), e);
		} finally {
			try {
				ctx.close();
			} catch (Exception e) {
				logger.error("Unable to close the context: " + e.getMessage(), e);
			}
		}
	}

	protected List<String> getValues(DataSource ds, String statement) {
		try (final Connection conn = ds.getConnection(); final Statement stmt = conn.createStatement()) {
			final List<String> res = new ArrayList<>();
			logger.debug("Executing statement [" + statement + "] ...");
			try (final ResultSet rs = stmt.executeQuery(statement)) {
				while (rs.next()) {
					String value = rs.getString(1);
					LogMF.debug(logger, "Retrieved value [{0}]", value);
					res.add(value);
				}
			}
			return res;
		} catch (SQLException e) {
			logger.error("Unable to execute query [" + statement + "] :" + e.getMessage(), e);
			throw new SpagoBIRuntimeException("Unable to execute query [" + statement + "] :" + e.getMessage(), e);
		}
	}

	private String getDatasourceJNDIName() {
		return Optional.ofNullable(System.getProperty("pnt_profiling_datasource", System.getenv("PNT_PROFILING_DATASOURCE")))
				.orElse(DEFAULT_JNDI_DATASOURCE_NAME);
	}
	*/
	// @formatter:on

}
