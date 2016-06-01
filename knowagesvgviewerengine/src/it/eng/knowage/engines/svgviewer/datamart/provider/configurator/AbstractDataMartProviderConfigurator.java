package it.eng.knowage.engines.svgviewer.datamart.provider.configurator;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineConstants;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineRuntimeException;
import it.eng.knowage.engines.svgviewer.datamart.provider.AbstractDataMartProvider;
import it.eng.knowage.engines.svgviewer.dataset.DataSetMetaData;
import it.eng.knowage.engines.svgviewer.dataset.provider.Hierarchy;
import it.eng.knowage.engines.svgviewer.dataset.provider.Link;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractDatasetProviderConfigurator.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class AbstractDataMartProviderConfigurator {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(AbstractDataMartProviderConfigurator.class);

	/**
	 * Configure.
	 *
	 * @param abstractDatasetProvider
	 *            the abstract dataset provider
	 * @param conf
	 *            the conf
	 *
	 * @throws SvgViewerEngineException
	 *             the geo engine exception
	 */
	public static void configure(AbstractDataMartProvider abstractDatasetProvider, Object conf) throws SvgViewerEngineException {
		SourceBean confSB = null;

		if (conf instanceof String) {
			try {
				confSB = SourceBean.fromXMLString((String) conf);
			} catch (SourceBeanException e) {
				logger.error("Impossible to parse configuration block for DataSetProvider", e);
				throw new SvgViewerEngineException("Impossible to parse configuration block for DataSetProvider", e);
			}
		} else {
			confSB = (SourceBean) conf;
		}

		if (confSB != null) {
			DataSetMetaData mataData = null;
			Map hierarchies = null;
			String selectedHierarchyName = null;
			String selectedLevelName = null;

			selectedHierarchyName = getSelectedHierarchyName(confSB);
			selectedLevelName = getSelectedLevelName(confSB);
			mataData = getMetaData(confSB);

			String stdHierarchy = (String) abstractDatasetProvider.getEnv().get(SvgViewerEngineConstants.ENV_STD_HIERARCHY);
			SourceBean stdHierarchySB = null;
			try {
				stdHierarchySB = SourceBean.fromXMLString(stdHierarchy);
			} catch (SourceBeanException e) {
				e.printStackTrace();
			}
			hierarchies = getHierarchies(confSB, stdHierarchySB);
			setLink(confSB, hierarchies);

			abstractDatasetProvider.setMetaData(mataData);
			abstractDatasetProvider.setHierarchies(hierarchies);
			abstractDatasetProvider.setSelectedHierarchyName(selectedHierarchyName);
			abstractDatasetProvider.setSelectedLevelName(selectedLevelName);
		}
	}

	/**
	 * Sets the link.
	 *
	 * @param confSB
	 *            the conf sb
	 * @param hierarchies
	 *            the hierarchies
	 */
	private static void setLink(SourceBean confSB, Map hierarchies) {
		SourceBean corssNavConfSB = (SourceBean) confSB.getAttribute("CROSS_NAVIGATION");
		if (corssNavConfSB == null)
			return;

		List links = corssNavConfSB.getAttributeAsList("LINK");
		for (int i = 0; i < links.size(); i++) {
			SourceBean linkSB = (SourceBean) links.get(i);
			String hierarchyName = (String) linkSB.getAttribute("HIERARCHY");
			Hierarchy hierarchy = (Hierarchy) hierarchies.get(hierarchyName);
			if (hierarchy == null)
				continue;
			String levelName = (String) linkSB.getAttribute("LEVEL");
			Hierarchy.Level level = hierarchy.getLevel(levelName);
			if (level == null)
				continue;

			String measure = (String) linkSB.getAttribute("MEASURE");
			logger.debug("add link: " + hierarchyName + "->" + levelName + "->" + measure);

			Link link = new Link();
			List parameters = linkSB.getAttributeAsList("PARAM");
			for (int j = 0; j < parameters.size(); j++) {
				SourceBean parameterSB = (SourceBean) parameters.get(j);
				String type = (String) parameterSB.getAttribute("TYPE");
				String scope = (String) parameterSB.getAttribute("SCOPE");
				String name = (String) parameterSB.getAttribute("NAME");
				String value = (String) parameterSB.getAttribute("VALUE");
				link.addParameter(type, scope, name, value);
			}

			level.setLink(measure, link);
		}
	}

	/**
	 * Gets the selected level name.
	 *
	 * @param confSB
	 *            the conf sb
	 *
	 * @return the selected level name
	 */
	private static String getSelectedLevelName(SourceBean confSB) {
		return (String) confSB.getAttribute("LEVEL");
	}

	/**
	 * Gets the selected hierarchy name.
	 *
	 * @param confSB
	 *            the conf sb
	 *
	 * @return the selected hierarchy name
	 */
	private static String getSelectedHierarchyName(SourceBean confSB) {
		return (String) confSB.getAttribute("HIERARCHY");
	}

	/**
	 * Gets the meta data.
	 *
	 * @param confSB
	 *            the conf sb
	 *
	 * @return the meta data
	 */
	public static DataSetMetaData getMetaData(SourceBean confSB) {
		DataSetMetaData metaData;
		SourceBean metadataSB;
		List columns;
		SourceBean columnSB;
		String columnName;
		String columnType;

		logger.debug("IN");

		metaData = null;
		metadataSB = null;

		try {

			metadataSB = (SourceBean) confSB.getAttribute(SvgViewerEngineConstants.METADATA_TAG);
			if (metadataSB == null) {
				logger.warn("Cannot find metadata configuration settings: tag name " + SvgViewerEngineConstants.METADATA_TAG);
				logger.info("Metadata configuration settings must be injected at execution time");
				return null;
			}

			logger.debug("Metadata block has been found in configuration");

			metaData = new DataSetMetaData();

			columns = metadataSB.getAttributeAsList(SvgViewerEngineConstants.COLUMN_TAG);
			logger.debug("Metadata block contains settings for [" + columns + "] columns");

			for (int i = 0; i < columns.size(); i++) {
				columnSB = null;
				try {
					logger.debug("Parsing column  [" + i + "]");
					columnSB = (SourceBean) columns.get(i);

					columnName = (String) columnSB.getAttribute(SvgViewerEngineConstants.COLUMN_NAME_ATTRIBUTE);
					logger.debug("Column [" + i + "] name [" + columnName + "]");
					Assert.assertNotNull(columnName, "Attribute [" + SvgViewerEngineConstants.COLUMN_NAME_ATTRIBUTE + "] of tag ["
							+ SvgViewerEngineConstants.COLUMN_TAG + "] cannot be null");

					columnType = (String) columnSB.getAttribute(SvgViewerEngineConstants.COLUMN_TYPE_ATTRIBUTE);
					logger.debug("Column [" + i + "] name [" + columnType + "]");
					Assert.assertNotNull(columnName, "Attribute [" + SvgViewerEngineConstants.COLUMN_TYPE_ATTRIBUTE + "] of tag ["
							+ SvgViewerEngineConstants.COLUMN_TAG + "] cannot be null");

					metaData.addColumn(columnName);
					metaData.setColumnProperty(columnName, "column_id", columnName);
					metaData.setColumnProperty(columnName, "type", columnType);

					if (columnType.equalsIgnoreCase("geoid")) {
						String hierarchyName = (String) columnSB.getAttribute(SvgViewerEngineConstants.COLUMN_HIERARCHY_REF_ATTRIBUTE);
						logger.debug("Column [" + i + "] attribute [" + SvgViewerEngineConstants.COLUMN_HIERARCHY_REF_ATTRIBUTE + "]is equal to ["
								+ hierarchyName + "]");
						Assert.assertNotNull(hierarchyName, "Attribute [" + SvgViewerEngineConstants.COLUMN_HIERARCHY_REF_ATTRIBUTE + "] of tag ["
								+ SvgViewerEngineConstants.COLUMN_TAG + "] cannot be null");
						metaData.setColumnProperty(columnName, "hierarchy", hierarchyName);

						String levelName = (String) columnSB.getAttribute(SvgViewerEngineConstants.COLUMN_LEVEL_REF_ATTRIBUTE);
						logger.debug("Column [" + i + "] attribute [" + SvgViewerEngineConstants.COLUMN_LEVEL_REF_ATTRIBUTE + "]is equal to [" + levelName
								+ "]");
						Assert.assertNotNull(hierarchyName, "Attribute [" + SvgViewerEngineConstants.COLUMN_LEVEL_REF_ATTRIBUTE + "] of tag ["
								+ SvgViewerEngineConstants.COLUMN_TAG + "] cannot be null");
						metaData.setColumnProperty(columnName, "level", levelName);
					} else if (columnType.equalsIgnoreCase("measure")) {
						String aggFunc = (String) columnSB.getAttribute(SvgViewerEngineConstants.COLUMN_AFUNC_REF_ATTRIBUTE);
						logger.debug("Column [" + i + "] attribute [" + SvgViewerEngineConstants.COLUMN_AFUNC_REF_ATTRIBUTE + "]is equal to [" + aggFunc + "]");
						Assert.assertNotNull(aggFunc, "Attribute [" + SvgViewerEngineConstants.COLUMN_AFUNC_REF_ATTRIBUTE + "] of tag ["
								+ SvgViewerEngineConstants.COLUMN_TAG + "] cannot be null");
						metaData.setColumnProperty(columnName, "func", aggFunc);
					}
					logger.debug("Column  [" + i + "] parsed succesfully");
				} catch (Throwable t) {
					throw new SvgViewerEngineException("An error occurred while parsing column [" + columnSB + "]", t);
				}
			}

		} catch (Throwable t) {
			SvgViewerEngineRuntimeException e = new SvgViewerEngineRuntimeException("An error occurred while parsing metadata [" + metadataSB + "]", t);
			e.addHint("Download document template and fix the problem that have coused the syntax/semantic error");
			throw e;
		} finally {
			logger.debug("OUT");
		}

		return metaData;
	}

	/**
	 * Gets the hierarchies.
	 *
	 * @param confSB
	 *            the conf sb
	 * @param sdtHierarchySB
	 *            the sdt hierarchy sb
	 *
	 * @return the hierarchies
	 */
	public static Map getHierarchies(SourceBean confSB, SourceBean sdtHierarchySB) {
		Map hierarchies = null;

		SourceBean hierarchiesSB = (SourceBean) confSB.getAttribute(SvgViewerEngineConstants.HIERARCHIES_TAG);
		if (hierarchiesSB == null) {
			logger.warn("Cannot find hierachies configuration settings: tag name " + SvgViewerEngineConstants.HIERARCHIES_TAG);
			logger.info("Hierarchies configuration settings must be injected at execution time");
			return null;
		}

		hierarchies = new HashMap();

		Hierarchy hierarchy = null;
		List hierarchyList = hierarchiesSB.getAttributeAsList(SvgViewerEngineConstants.HIERARCHY_TAG);
		for (int i = 0; i < hierarchyList.size(); i++) {

			SourceBean hierarchySB = (SourceBean) hierarchyList.get(i);
			String name = (String) hierarchySB.getAttribute(SvgViewerEngineConstants.HIERARCHY_NAME_ATTRIBUTE);
			String type = (String) hierarchySB.getAttribute(SvgViewerEngineConstants.HIERARCHY_TYPE_ATTRIBUTE);
			List levelList = null;
			if (type.equalsIgnoreCase("custom")) {
				hierarchy = new Hierarchy(name);
				levelList = hierarchySB.getAttributeAsList(SvgViewerEngineConstants.HIERARCHY_LEVEL_TAG);
			} else {
				if (sdtHierarchySB != null) {
					hierarchySB = sdtHierarchySB;
					String table = (String) hierarchySB.getAttribute(SvgViewerEngineConstants.HIERARCHY_TABLE_ATRRIBUTE);
					hierarchy = new Hierarchy(name, table);
					levelList = hierarchySB.getAttributeAsList(SvgViewerEngineConstants.HIERARCHY_LEVEL_TAG);
				} else {
					logger.error("Impossible to include default hierarchy");
				}
			}

			for (int j = 0; j < levelList.size(); j++) {
				SourceBean levelSB = (SourceBean) levelList.get(j);
				String lname = (String) levelSB.getAttribute(SvgViewerEngineConstants.HIERARCHY_LEVEL_NAME_ATRRIBUTE);
				String lcolumnid = (String) levelSB.getAttribute(SvgViewerEngineConstants.HIERARCHY_LEVEL_COLUMN_ID_ATRRIBUTE);
				String lcolumndesc = (String) levelSB.getAttribute(SvgViewerEngineConstants.HIERARCHY_LEVEL_COLUMN_DESC_ATRRIBUTE);
				String lfeaturename = (String) levelSB.getAttribute(SvgViewerEngineConstants.HIERARCHY_LEVEL_FEATURE_NAME_ATRRIBUTE);
				Hierarchy.Level level = new Hierarchy.Level();
				level.setName(lname);
				level.setColumnId(lcolumnid);
				level.setColumnDesc(lcolumndesc);
				level.setFeatureName(lfeaturename);
				hierarchy.addLevel(level);
			}

			hierarchies.put(hierarchy.getName(), hierarchy);
		}

		return hierarchies;
	}
}
