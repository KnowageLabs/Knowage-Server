/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.engines.georeport.features.provider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;

import it.eng.spagobi.engines.georeport.AbstractSpagoBIGeoreportTest;
import it.eng.spagobi.engines.georeport.TestCostants;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class FeaturesProviderDAOFileImplTest extends AbstractSpagoBIGeoreportTest {
	
	private IFeaturesProviderDAO featuresProvider;
	
	public void setUp() throws Exception {
		try {
			super.setUp();
			featuresProvider = new FeaturesProviderDAOFileImpl( new File(TestCostants.inputFolder, "resources"));
		} catch(Exception t) {
			System.err.println("An unespected error occurred during setUp: ");
			t.printStackTrace();
			throw t;
		}
	}

	
	protected void doTearDown() {
		super.doTearDown();
		featuresProvider = null;
	}
	
	public void testGetFeatureById() {
		Map<String, String> parameters = new HashMap();
		parameters.put(FeaturesProviderDAOFileImpl.GEOID_PNAME, "NAME_3");
		parameters.put(FeaturesProviderDAOFileImpl.GEOID_PVALUE, "Bolzano");
		
		
		SimpleFeature feature = null;
		try {
			feature = featuresProvider.getFeatureById("sudtirol_comuni.json", null, parameters);
		} catch(Throwable t) {
			t.printStackTrace();
			fail();
		}
		
		assertNotNull("Impossible to load feature", feature);
	}
	
	public void testGetAllFeatures() {
		Map<String, String> parameters = new HashMap();
		parameters.put(FeaturesProviderDAOFileImpl.GEOID_PNAME, "NAME_3");
		parameters.put(FeaturesProviderDAOFileImpl.GEOID_PVALUE, "Bolzano");
		
		
		FeatureCollection features = null;
		try {
			features = featuresProvider.getAllFeatures("sudtirol_comuni.json", null);
		} catch(Throwable t) {
			t.printStackTrace();
			fail();
		}
		
		assertNotNull("Impossible to load features", features);
		assertEquals(116, features.size());
	}
}
