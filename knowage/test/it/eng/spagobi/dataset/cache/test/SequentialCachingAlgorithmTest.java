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
package it.eng.spagobi.dataset.cache.test;

import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author portosa
 *
 */
public class SequentialCachingAlgorithmTest {

	private static Connection connection = null;
	private static Map<String, Set<String>> associationValues = null;
	private static Map<String, Map<String, String>> datasets = null;
	private static Map<String, Set<String>> associations = null;
	private static Statement stmt = null;

	private static final String D1 = "D1";
	private static final String D2 = "D2";
	private static final String D3 = "D3";

	private static final String FIRST_DATASET = D2;
	private static final String FIRST_FILTER = "gender = 'F'";

	ResultSet rs_d1 = null;
	ResultSet rs_d2 = null;
	ResultSet rs_d3 = null;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://localhost/foodmart_key", "root", "lancer");
		assertNotNull("Connection correctly established", connection);
		stmt = connection.createStatement();
		assertNotNull("Statement correctly created", stmt);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		connection.close();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		associationValues = new HashMap<String, Set<String>>();

		datasets = new HashMap<String, Map<String, String>>();
		Map<String, String> d1 = new HashMap<String, String>();
		d1.put("GENDER", "gender");
		d1.put("EDUCATION", "education");
		d1.put("STATE", "state_province");
		datasets.put(D1, d1);
		Map<String, String> d2 = new HashMap<String, String>();
		d2.put("GENDER", "gender");
		datasets.put(D2, d2);
		Map<String, String> d3 = new HashMap<String, String>();
		d3.put("EDUCATION", "education");
		d3.put("STATE", "state");
		datasets.put(D3, d3);

		associations = new HashMap<String, Set<String>>();
		Set<String> a1 = new HashSet<String>();
		a1.add(D1);
		a1.add(D2);
		associations.put("GENDER", a1);
		Set<String> a2 = new HashSet<String>();
		a2.add(D1);
		a2.add(D3);
		associations.put("EDUCATION", a2);
		Set<String> a3 = new HashSet<String>();
		a3.add(D1);
		a3.add(D3);
		associations.put("STATE", a3);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		associationValues = null;
		datasets = null;
		associations = null;

		if (rs_d1 != null) {
			rs_d1.close();
		}
		if (rs_d2 != null) {
			rs_d2.close();
		}
		if (rs_d3 != null) {
			rs_d3.close();
		}
		if (stmt != null) {
			stmt.close();
		}
	}

	@Test
	public void test() throws Exception {

		// (0) generate the starting set of values for each associations
		createStartingSets();

		// (1) user click on widget built with dataset D2 -> it clicks on gender 'F'
		calculateDatasets(FIRST_DATASET, "", FIRST_FILTER);

		String inGenderValues = "'" + StringUtils.join(associationValues.get("GENDER").iterator(), "','") + "'";
		String inEducationValues = "'" + StringUtils.join(associationValues.get("EDUCATION").iterator(), "','") + "'";
		String inStateValues = "'" + StringUtils.join(associationValues.get("STATE").iterator(), "','") + "'";

		System.out.println("GENDER [" + inGenderValues + "]");
		System.out.println("EDUCATION [" + inEducationValues + "]");
		System.out.println("STATE [" + inStateValues + "]");

	}

	@SuppressWarnings("unchecked")
	private void calculateDatasets(String dataset, String fromAssociation, String filter) throws Exception {

		Map<String, String> datasetAssociation = new HashMap<String, String>(datasets.get(dataset));
		// no need to iterate over the incoming association
		datasetAssociation.remove(fromAssociation);
		// iterate over all the associations
		for (String association : datasetAssociation.keySet()) {
			String columnName = datasetAssociation.get(association);
			String query = "SELECT DISTINCT " + columnName + " FROM " + dataset + " WHERE " + filter;
			Set<String> distinctValues = new HashSet<String>();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				distinctValues.add(rs.getString(columnName));
			}
			Set<String> baseSet = associationValues.get(association);
			Set<String> intersection = new HashSet<String>(CollectionUtils.intersection(baseSet, distinctValues));
			if (!intersection.equals(baseSet)) {
				associationValues.put(association, intersection);
				String inClauseValues = "'" + StringUtils.join(associationValues.get(association).iterator(), "','") + "'";
				String f = columnName + " IN (" + inClauseValues + ")";
				for (String datasetInvolved : associations.get(association)) {
					if (!datasetInvolved.equals(dataset)) {
						// it will skip the current dataset, from which the filter is fired
						calculateDatasets(datasetInvolved, association, f);
					}
				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void createStartingSets() throws Exception {

		// -------------------------------------------------
		// Fill the set of values for the GENDER associations
		Set<String> gender_d1 = new HashSet<String>();
		rs_d1 = stmt.executeQuery("SELECT DISTINCT gender FROM " + D1);
		while (rs_d1.next()) {
			gender_d1.add(rs_d1.getString("gender"));
		}
		Set<String> gender_d3 = new HashSet<String>();
		rs_d2 = stmt.executeQuery("SELECT DISTINCT gender FROM " + D2);
		while (rs_d2.next()) {
			gender_d3.add(rs_d2.getString("gender"));
		}
		Set<String> gender_intersection = new HashSet<String>(CollectionUtils.intersection(gender_d1, gender_d3));
		associationValues.put("GENDER", gender_intersection);
		// -------------------------------------------------

		// -------------------------------------------------
		// Fill the set of values for the EDUCATION associations
		Set<String> education_d1 = new HashSet<String>();
		rs_d1 = stmt.executeQuery("SELECT DISTINCT education FROM " + D1);
		while (rs_d1.next()) {
			education_d1.add(rs_d1.getString("education"));
		}
		Set<String> education_d3 = new HashSet<String>();
		rs_d3 = stmt.executeQuery("SELECT DISTINCT education FROM " + D3);
		while (rs_d3.next()) {
			education_d3.add(rs_d3.getString("education"));
		}
		Set<String> education_intersection = new HashSet<String>(CollectionUtils.intersection(education_d1, education_d3));
		associationValues.put("EDUCATION", education_intersection);
		// -------------------------------------------------

		// -------------------------------------------------
		// Fill the set of values for the STATE associations
		Set<String> state_d1 = new HashSet<String>();
		rs_d1 = stmt.executeQuery("SELECT DISTINCT state_province FROM " + D1);
		while (rs_d1.next()) {
			state_d1.add(rs_d1.getString("state_province"));
		}
		Set<String> state_d3 = new HashSet<String>();
		rs_d3 = stmt.executeQuery("SELECT DISTINCT state_province FROM " + D3);
		while (rs_d3.next()) {
			state_d3.add(rs_d3.getString("state_province"));
		}
		Set<String> state_intersection = new HashSet<String>(CollectionUtils.intersection(state_d1, state_d3));
		associationValues.put("STATE", state_intersection);
		// -------------------------------------------------
	}
}