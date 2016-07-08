package it.eng.knowage.engines.svgviewer.datamart.provider.configurator;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineConstants;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineRuntimeException;
import it.eng.knowage.engines.svgviewer.datamart.provider.AbstractDataMartProvider;
import it.eng.knowage.engines.svgviewer.dataset.DataSetMetaData;
import it.eng.knowage.engines.svgviewer.dataset.HierarchyMember;
import it.eng.knowage.engines.svgviewer.dataset.provider.Hierarchy;
import it.eng.knowage.engines.svgviewer.dataset.provider.Link;
import it.eng.knowage.engines.svgviewer.map.renderer.configurator.AbstractMapRendererConfigurator;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * The Class AbstractDatasetProviderConfigurator.
 *
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
			Map<String, HierarchyMember> hierarchyMembers = new HashMap<String, HierarchyMember>();

			String selectedHierarchyName = null;
			String selectedMemberName = null;

			selectedHierarchyName = getSelectedHierarchyName(confSB);
			hierarchyMembers = getHierarchyMembers(confSB);
			selectedMemberName = getDefaultMemberName(hierarchyMembers);
			List membersLst = getHierarchyMembersList(confSB);

			abstractDatasetProvider.setHierarchyMembers(hierarchyMembers);
			abstractDatasetProvider.setSelectedHierarchyName(selectedHierarchyName);
			abstractDatasetProvider.setSelectedMemberName(selectedMemberName);
			abstractDatasetProvider.setSelectedLevel("1");
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
		SourceBean hierarchySB = (SourceBean) confSB.getAttribute("HIERARCHY");
		return (String) hierarchySB.getAttribute("name");
	}

	/**
	 * Gets the default (level=1) member of the hierarchy. If it doesn't exist into the template returns the first member found.
	 *
	 * @param membersMap
	 *            the map with all members
	 *
	 * @return the default member name
	 */
	private static String getDefaultMemberName(Map<String, HierarchyMember> membersMap) {
		String toReturn = null;
		int idx = 0;
		for (Iterator iterator = membersMap.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			HierarchyMember member = membersMap.get(key);
			if (idx == 0) {
				toReturn = key;
			}
			if (member.getLevel() == 1) {
				toReturn = key;
				break;
			}
			idx++;
			logger.error("Member with level [1]  not found into the template. Returned the first member found [" + key + "]! Check the template.");
		}

		return toReturn;
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
						// nothing
					} else if (columnType.equalsIgnoreCase("measure")) {

						metaData.setColumnProperty(columnName, "func", "sum");
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
	 * Gets the selected hierarchy name.
	 *
	 * @param confSB
	 *            the conf sb
	 *
	 * @return the selected hierarchy name
	 */
	private static Map<String, HierarchyMember> getHierarchyMembers(SourceBean confSB) {

		Map<String, HierarchyMember> toReturn = new HashMap<String, HierarchyMember>();
		SourceBean hierarchySB = (SourceBean) confSB.getAttribute("HIERARCHY");
		SourceBean memberSB = null;

		try {
			List members = hierarchySB.getAttributeAsList(SvgViewerEngineConstants.MEMBER_TAG);
			// List members = getHierarchyMembersList(confSB);
			boolean foundActive = false;
			for (int i = 0; i < members.size(); i++) {
				memberSB = null;
				try {
					logger.debug("Parsing member  [" + i + "]");
					memberSB = (SourceBean) members.get(i);

					String hierarchy = (String) hierarchySB.getAttribute("name");
					logger.debug("Member [" + i + "] hierarchy [" + hierarchy + "]");
					String name = (String) memberSB.getAttribute("name");
					logger.debug("Member [" + i + "] name [" + name + "]");
					String dsMeasure = (String) memberSB.getAttribute("measure_dataset");
					logger.debug("Member [" + i + "] measure_dataset [" + dsMeasure + "]");
					String dsConfig = (String) memberSB.getAttribute("config_dataset");
					logger.debug("Member [" + i + "] config_dataset [" + dsConfig + "]");
					String level = (String) memberSB.getAttribute("level");
					logger.debug("Member [" + i + "] level [" + level + "]");
					String enableCross = (memberSB.getAttribute("enableExternalCross") == null) ? "false" : (String) memberSB
							.getAttribute("enableExternalCross");
					logger.debug("Member [" + i + "] enableExternalCross [" + enableCross + "]");

					Assert.assertNotNull(name, "Attribute [" + SvgViewerEngineConstants.MEMBER_NAME + "] of tag [" + SvgViewerEngineConstants.MEMBER_NAME
							+ "] cannot be null");

					Assert.assertNotNull(dsMeasure, "Attribute [" + SvgViewerEngineConstants.DATASET_MEASURE + "] of tag ["
							+ SvgViewerEngineConstants.DATASET_MEASURE + "] cannot be null");

					Assert.assertNotNull(dsMeasure, "Attribute [" + SvgViewerEngineConstants.DATASET_CONFIG + "] of tag ["
							+ SvgViewerEngineConstants.DATASET_CONFIG + "] cannot be null");

					HierarchyMember member = new HierarchyMember();
					member.setHierarchy(hierarchy);
					member.setName(name);
					member.setDsMeasure(dsMeasure);
					member.setLevel(Integer.valueOf(level));
					member.setEnableCross(new Boolean(enableCross));

					// get metadata informations
					DataSetMetaData dsMetadata = getMetaData(memberSB);
					member.setDsMetaData(dsMetadata);

					// get leyers informations
					SourceBean layersSB = (SourceBean) memberSB.getAttribute("LAYERS");
					Map layers = AbstractMapRendererConfigurator.getLayers(layersSB);
					member.setLayers(layers);

					// for default set active the member with level 1
					if (member.getLevel() == 1) {
						member.setActive(true);
						foundActive = true;
						logger.debug("Set [" + member.getName() + "] as the active member.");
					}

					// get measures (kpi) configurations
					SourceBean measuresSB = (SourceBean) memberSB.getAttribute("MEASURES");
					Assert.assertNotNull(measuresSB, "Tag [MEASURES] cannot be null");

					Map measures = AbstractMapRendererConfigurator.getMeasures(measuresSB);
					member.setMeasures(measures);

					logger.debug("Member  [" + i + "] parsed succesfully");
					toReturn.put(name, member);
				} catch (Throwable t) {
					throw new SvgViewerEngineException("An error occurred while parsing member [" + memberSB + "]", t);
				}
			}
			if (!foundActive) {
				// force the first member as active
				for (String key : toReturn.keySet()) {
					(toReturn.get(key)).setActive(true);
					logger.debug("Member with level 1 not found. Set [" + key + "] as the active member.");
					break;
				}
			}
		} catch (Throwable t) {
			SvgViewerEngineRuntimeException e = new SvgViewerEngineRuntimeException("An error occurred while parsing metadata [" + memberSB + "]", t);
			e.addHint("Download document template and fix the problem that have coused the syntax/semantic error");
			throw e;
		} finally {
			logger.debug("OUT");
		}

		return toReturn;

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

	private static List getHierarchyMembersList(SourceBean confSB) {
		SourceBean hierarchySB = null;
		try {
			hierarchySB = (SourceBean) confSB.getAttribute("HIERARCHY");
			return hierarchySB.getAttributeAsList(SvgViewerEngineConstants.MEMBER_TAG);
		} catch (Throwable t) {
			SvgViewerEngineRuntimeException e = new SvgViewerEngineRuntimeException("An error occurred while parsing metadata [" + hierarchySB + "]", t);
			e.addHint("Download document template and fix the problem that have coused the syntax/semantic error");
			throw e;
		} finally {
			logger.debug("OUT");
		}
	}

}
