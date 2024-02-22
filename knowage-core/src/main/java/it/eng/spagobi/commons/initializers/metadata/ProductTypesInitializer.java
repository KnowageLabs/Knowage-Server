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
package it.eng.spagobi.commons.initializers.metadata;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiProductType;
import it.eng.spagobi.commons.metadata.SbiProductTypeEngine;
import it.eng.spagobi.commons.metadata.SbiProductTypeEngineId;
import it.eng.spagobi.engines.config.metadata.SbiEngines;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class ProductTypesInitializer extends SpagoBIInitializer {
	private static Logger logger = Logger.getLogger(ProductTypesInitializer.class);

	public ProductTypesInitializer() {
		targetComponentName = "ProductType";
		configurationFileName = "it/eng/spagobi/commons/initializers/metadata/config/productTypes.xml";
	}

	@Override
	public void init(SourceBean config, Session hibernateSession) {
		logger.debug("IN");
		try {
			String hql = "from SbiProductType";
			Query hqlQuery = hibernateSession.createQuery(hql);
			List<SbiProductType> productTypes = hqlQuery.list();
			if (productTypes.isEmpty()) {
				logger.info("Product Type table is empty. Starting populating product type...");
				writeProductTypes(hibernateSession);
			} else {
				logger.debug("Product Type table is already populated, only missing product types will be populated");
				synchronizeProductTypes(hibernateSession, productTypes);
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Ab unexpected error occured while initializing Product Types", t);
		} finally {
			logger.debug("OUT");
		}

	}

	private void writeProductTypes(Session aSession) throws Exception {
		logger.debug("IN");
		SourceBean productTypesSB = getConfiguration();
		if (productTypesSB == null) {
			throw new Exception("Product Types configuration file not found!!!");
		}
		List productTypesList = productTypesSB.getAttributeAsList("PRODUCT_TYPE");
		if (productTypesList == null || productTypesList.isEmpty()) {
			throw new Exception("No predefined product types found!!!");
		}
		Iterator it = productTypesList.iterator();
		while (it.hasNext()) {
			SourceBean aProductTypeSB = (SourceBean) it.next();
			SbiProductType aProductType = new SbiProductType();
			String isActive = (String) aProductTypeSB.getAttribute("active");
			if (isActive != null && isActive.equalsIgnoreCase("true")) {
				aProductType.setLabel((String) aProductTypeSB.getAttribute("label"));

				logger.debug("Inserting Product Type with label = [" + aProductTypeSB.getAttribute("label") + "] ...");
				aSession.save(aProductType);

				writeEngineAssociations(aSession, aProductTypeSB);
			}
		}
		logger.debug("OUT");
	}

	private void synchronizeProductTypes(Session aSession, List<SbiProductType> productTypes) throws Exception {
		logger.debug("IN");
		SourceBean productTypesSB = getConfiguration();
		if (productTypesSB == null) {
			throw new Exception("Product Types configuration file not found!!!");
		}
		List productTypesList = productTypesSB.getAttributeAsList("PRODUCT_TYPE");
		if (productTypesList == null || productTypesList.isEmpty()) {
			throw new Exception("No predefined product types found!!!");
		}

		// remove from DB the SbiProductType deleted in configuration file
		for (SbiProductType pt : productTypes) {
			boolean isInConfigFile = false;
			String engLabelDB = pt.getLabel();
			Iterator it = productTypesList.iterator();
			while (it.hasNext()) {
				SourceBean ptSB = (SourceBean) it.next();
				String productTypeConfigFile = (String) ptSB.getAttribute("label");
				if (productTypeConfigFile.equals(engLabelDB)) {
					isInConfigFile = true;
					break;
				}
			}
			if (!isInConfigFile) {
				deleteProductType(aSession, pt);
			}
		}
		// insert missing ProductType
		List alreadyExamined = new ArrayList();
		Iterator it = productTypesList.iterator();
		while (it.hasNext()) {
			SourceBean aProductTypeSB = (SourceBean) it.next();
			if (!alreadyExamined.contains(aProductTypeSB)) {

				String label = (String) aProductTypeSB.getAttribute("label");
				if (label == null || label.equals("")) {
					logger.error("No predefined product type label found!!!");
					throw new Exception("No predefined product type label found!!!");
				}
				// Retrieving all the product types in the DB with the specified label
				logger.debug("Retrieving all the product types in the DB with the specified label");
				String hql = "from SbiProductType where label = :label";
				Query hqlQuery = aSession.createQuery(hql);
				hqlQuery.setParameter("label", label);
				List result = hqlQuery.list();

				logger.debug("Retrieving all the product types in the XML file with the specified label");
				// Retrieving all the product types in the XML file with the specified label
				List productTypesXmlList = productTypesSB.getFilteredSourceBeanAttributeAsList("PRODUCT_TYPE", "label", label);

				// Checking if the product types in the DB are less than the ones in the xml file
				if (result.size() < productTypesXmlList.size()) {
					// Less product types in the DB than in the XML file, will add new ones
					logger.debug("Less product types in the DB than in the XML file, will add new ones");
					addMissingProductTypes(aSession, result, productTypesXmlList);
				}
				// Removing form the list of XML product types the ones already checked
				logger.debug("Adding to the list of XML product types already checked");
				alreadyExamined.addAll(productTypesXmlList);
			}
		}

		// update engines of product types
		it = productTypesList.iterator();
		while (it.hasNext()) {
			SourceBean aProductTypeSB = (SourceBean) it.next();
			updateEngineAssociations(aSession, aProductTypeSB);
		}
		logger.debug("OUT");
	}

	private void addMissingProductTypes(Session aSession, List dbProductTypes, List xmlProductTypes) {
		logger.debug("IN");

		Iterator it2 = xmlProductTypes.iterator();
		while (it2.hasNext()) {
			boolean existsInDb = false;
			SourceBean aProductTypeSB = (SourceBean) it2.next();
			String labelXml = (String) aProductTypeSB.getAttribute("label");
			logger.debug("Retrieved label of XML Product Type: " + labelXml);

			String isActive = (String) aProductTypeSB.getAttribute("active");
			if (isActive != null && isActive.equalsIgnoreCase("true")) {
				Iterator it = dbProductTypes.iterator();
				while (it.hasNext()) {
					SbiProductType d = (SbiProductType) it.next();
					String label = d.getLabel();
					logger.debug("Retrieved label of DB Product Type: " + label);

					if (labelXml.equalsIgnoreCase(label)) {
						existsInDb = true;
						logger.debug("Product Type already exists in the DB");
						break;
					}
				}
				if (!existsInDb) {
					logger.debug("Product Type doesn't exist in the DB");
					SbiProductType aProductType = new SbiProductType();
					aProductType.setLabel((String) aProductTypeSB.getAttribute("label"));
					logger.debug("New Product Type ready to be inserted in the DB");
					logger.debug("Inserting Product Type with label = [" + aProductTypeSB.getAttribute("label") + "] ...");
					aSession.save(aProductType);
					logger.debug("New Product Type inserted in the DB");

					writeEngineAssociations(aSession, aProductTypeSB);
				}
			}
		}
		logger.debug("OUT");
	}

	private void writeEngineAssociations(Session aSession, SourceBean aProductTypeSB) {

		List enginesList = aProductTypeSB.getAttributeAsList("ENGINE");
		String productTypeName = (String) aProductTypeSB.getAttribute("label");
		if (enginesList == null || enginesList.isEmpty()) {
			throw new SpagoBIRuntimeException("No associated engines for " + productTypeName + " !!!");
		}
		Iterator it = enginesList.iterator();
		while (it.hasNext()) {
			SourceBean anEngineSB = (SourceBean) it.next();
			SbiEngines anEngine = findEngine(aSession, (String) anEngineSB.getAttribute("label"));
			SbiProductType aProductType = findProductType(aSession, (String) aProductTypeSB.getAttribute("label"));

			SbiProductTypeEngine association = new SbiProductTypeEngine();
			association.setSbiProductType(aProductType);
			association.setSbiEngines(anEngine);
			SbiCommonInfo commonInfo = new SbiCommonInfo();
			commonInfo.setUserIn("server");
			commonInfo.setTimeIn(new Date());

			association.setCommonInfo(commonInfo);

			SbiProductTypeEngineId id = new SbiProductTypeEngineId(anEngine.getEngineId(),aProductType.getProductTypeId());
			association.setId(id);

			aSession.save(association);

		}
	}

	private void updateEngineAssociations(Session aSession, SourceBean aProductTypeSB) {

		List enginesList = aProductTypeSB.getAttributeAsList("ENGINE");
		String productTypeName = (String) aProductTypeSB.getAttribute("label");
		if (enginesList == null || enginesList.isEmpty()) {
			throw new SpagoBIRuntimeException("No associated engines for " + productTypeName + " !!!");
		}
		Iterator it = enginesList.iterator();
		while (it.hasNext()) {
			SourceBean anEngineSB = (SourceBean) it.next();
			SbiEngines anEngine = findEngine(aSession, (String) anEngineSB.getAttribute("label"));
			SbiProductType aProductType = findProductType(aSession, (String) aProductTypeSB.getAttribute("label"));

			SbiProductTypeEngine association = findProductEngineType(aSession, (String) anEngineSB.getAttribute("label"),
					(String) aProductTypeSB.getAttribute("label"));
			if (association == null) {
				association = new SbiProductTypeEngine();
				association.setSbiProductType(aProductType);
				association.setSbiEngines(anEngine);
				SbiCommonInfo commonInfo = new SbiCommonInfo();
				commonInfo.setUserIn("server");
				commonInfo.setTimeIn(new Date());

				association.setCommonInfo(commonInfo);

				SbiProductTypeEngineId id = new SbiProductTypeEngineId(anEngine.getEngineId(),aProductType.getProductTypeId());
				association.setId(id);

				aSession.save(association);
			}

		}
	}
}
