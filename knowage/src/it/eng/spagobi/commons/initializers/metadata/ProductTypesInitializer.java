package it.eng.spagobi.commons.initializers.metadata;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiProductType;
import it.eng.spagobi.commons.metadata.SbiProductTypeEngine;
import it.eng.spagobi.commons.metadata.SbiProductTypeEngineId;
import it.eng.spagobi.engines.config.metadata.SbiEngines;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class ProductTypesInitializer extends SpagoBIInitializer {
	static private Logger logger = Logger.getLogger(ProductTypesInitializer.class);

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
			List productTypes = hqlQuery.list();
			if (productTypes.isEmpty()) {
				logger.info("Product Type table is empty. Starting populating product type...");
				writeProductTypes(hibernateSession);
			} else {
				logger.debug("Product Type table is already populated, only missing product types will be populated");
				writeMissingProductTypes(hibernateSession);
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
			aProductType.setLabel((String) aProductTypeSB.getAttribute("label"));

			logger.debug("Inserting Product Type with label = [" + aProductTypeSB.getAttribute("label") + "] ...");
			aSession.save(aProductType);

			writeEngineAssociations(aSession, aProductTypeSB);
		}
		logger.debug("OUT");
	}

	private void writeMissingProductTypes(Session aSession) throws Exception {
		logger.debug("IN");
		SourceBean productTypesSB = getConfiguration();
		if (productTypesSB == null) {
			throw new Exception("Product Types configuration file not found!!!");
		}
		List productTypesList = productTypesSB.getAttributeAsList("PRODUCT_TYPE");
		if (productTypesList == null || productTypesList.isEmpty()) {
			throw new Exception("No predefined product types found!!!");
		}

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
				logger.debug("Retrieving all the product types in the DB with the specified labek");
				String hql = "from SbiProductType where label = '" + label + "'";
				Query hqlQuery = aSession.createQuery(hql);
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

			SbiProductTypeEngineId id = new SbiProductTypeEngineId();
			id.setProductTypeId(aProductType.getProductTypeId());
			id.setEngineId(anEngine.getEngineId());
			association.setId(id);

			aSession.save(association);

		}
	}
}
