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
import it.eng.spagobi.json.Xml;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

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
			String datasetPlaceholder = null;

			// Monitor getSelectedHierarchyNameMonitor = MonitorFactory.start("GeoEngine.AbstractDataMartProviderConfigurator.getSelectedHierarchyName");
			selectedHierarchyName = getSelectedHierarchyName(confSB);
			// getSelectedHierarchyNameMonitor.stop();
			// Monitor getHierarchyMembersMonitor = MonitorFactory.start("GeoEngine.AbstractDataMartProviderConfigurator.getHierarchyMembers");
			hierarchyMembers = getHierarchyMembers(confSB, abstractDatasetProvider.getEnv());
			// getHierarchyMembersMonitor.stop();
			// Monitor getDefaultMemberNameMonitor = MonitorFactory.start("GeoEngine.AbstractDataMartProviderConfigurator.getDefaultMemberName");
			selectedMemberName = getDefaultMemberName(hierarchyMembers);
			// getDefaultMemberNameMonitor.stop();
			// Monitor getHierarchyMembersListMonitor = MonitorFactory.start("GeoEngine.AbstractDataMartProviderConfigurator.getHierarchyMembersList");
			// List membersLst = getHierarchyMembersList(confSB);
			// getHierarchyMembersListMonitor.stop();

			// datasetPlaceholder = getDataSetPlaceholder(confSB);

			abstractDatasetProvider.setHierarchyMembers(hierarchyMembers);
			abstractDatasetProvider.setSelectedHierarchyName(selectedHierarchyName);
			abstractDatasetProvider.setSelectedMemberName(selectedMemberName);
			abstractDatasetProvider.setDsPlaceHolder(datasetPlaceholder);
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
	 * Gets the selected level name.
	 *
	 * @param confSB
	 *            the conf sb
	 *
	 * @return the selected level name
	 */
	// private static String getDataSetPlaceholder(SourceBean confSB) {
	// return (String) confSB.getAttribute("placeholder_dataset");
	// }

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
			logger.debug("Member with level [1]  not found into the template. Returned the first member found [" + key + "]! Check the template.");
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
					logger.error("An error occurred while parsing column [" + columnSB + "]", t);
					throw new SvgViewerEngineException("An error occurred while parsing column [" + columnSB + "]", t);
				}
			}

		} catch (Throwable t) {
			logger.error("An error occurred while parsing metadata [" + metadataSB + "]", t);
			SvgViewerEngineRuntimeException e = new SvgViewerEngineRuntimeException("An error occurred while parsing metadata [" + metadataSB + "]", t);
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
	private static Map<String, HierarchyMember> getHierarchyMembers(SourceBean confSB, Map env) {

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

					String hierarchy = getMemberProperty("name", hierarchySB, env);
					logger.debug("Member [" + i + "] hierarchy [" + hierarchy + "]");
					String name = getMemberProperty("name", memberSB, env);
					logger.debug("Member [" + i + "] name [" + name + "]");
					String dsMeasure = getMemberProperty("measure_dataset", memberSB, env);
					logger.debug("Member [" + i + "] measure_dataset [" + dsMeasure + "]");
					String dsPlaceholder = getMemberProperty("placeholder_dataset", memberSB, env);
					logger.debug("Member [" + i + "] placeholder_dataset [" + dsPlaceholder + "]");
					String level = getMemberProperty("level", memberSB, env);
					logger.debug("Member [" + i + "] level [" + level + "]");
					String enableCross = getMemberProperty("enableExternalCross", memberSB, env);
					enableCross = (enableCross == null) ? "false" : enableCross;
					logger.debug("Member [" + i + "] enableExternalCross [" + enableCross + "]");
					JSONArray labelsCross = getMemberPropertyAsList("lablesExternalCross", memberSB, env);
					labelsCross = (labelsCross == null) ? new JSONArray() : labelsCross;
					logger.debug("Member [" + i + "] lablesExternalCross [" + enableCross + "]");
					String isCustomized = getMemberProperty("isCustomizedSVG", memberSB, env);
					isCustomized = (isCustomized == null) ? "false" : isCustomized;
					logger.debug("Member [" + i + "] isCustomizedSVG [" + isCustomized + "]");

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
					member.setDsPlaceholder(dsPlaceholder);
					member.setLevel(Integer.valueOf(level));
					member.setEnableCross(new Boolean(enableCross));
					member.setLabelsCross(labelsCross);
					member.setIsCustomized(new Boolean(isCustomized));

					// get metadata informations
					DataSetMetaData dsMetadata = getMetaData(memberSB);
					member.setDsMetaData(dsMetadata);

					// get layers informations
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
					// Assert.assertNotNull(measuresSB, "Tag [MEASURES] cannot be null");
					if (measuresSB != null) {
						Map measures = AbstractMapRendererConfigurator.getMeasures(measuresSB);
						member.setMeasures(measures);
					}

					if (member.getIsCustomized()) {
						// get customizations (ie. external charts...)
						SourceBean customizationSB = (SourceBean) memberSB.getAttribute("CUSTOMIZE_SETTINGS");
						if (customizationSB != null) {
							JSONObject customizationJSON = new JSONObject(Xml.xml2json(customizationSB.toXML()));
							member.setCustomizationSettings(customizationJSON);
						} else {
							logger.debug("Member with name ["
									+ name
									+ "] is configurated as a customized SVG but the customization settings aren't defined. Please check the template. Customizations will not applied!");
						}
					}
					logger.debug("Member  [" + i + "] parsed succesfully");
					toReturn.put(name, member);
				} catch (Throwable t) {
					logger.error("An error occurred while parsing member [" + memberSB + "]", t);
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
			logger.error("Error while parsing hierarchy members", t);
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
			logger.error("An error occurred while parsing metadata [" + hierarchySB + "]", t);
			SvgViewerEngineRuntimeException e = new SvgViewerEngineRuntimeException("An error occurred while parsing metadata [" + hierarchySB + "]", t);
			throw e;
		} finally {
			logger.debug("OUT");
		}
	}

	private static String getMemberProperty(String prop, SourceBean memberSB, Map env) {
		String toReturn = null;
		toReturn = (String) memberSB.getAttribute(prop);
		// replace the member name with analytical driver value if it's required
		if (toReturn != null && toReturn.indexOf("$P{") >= 0) {
			int startPos = toReturn.indexOf("$P{") + 3;
			int endPos = toReturn.indexOf("}", startPos);
			String placeholder = toReturn.substring(startPos, endPos);
			toReturn = (String) env.get(placeholder);
			logger.debug("Member name value getted from analytical driver [" + placeholder + "] is [" + toReturn + "]");
		}
		return toReturn;
	}

	private static JSONArray getMemberPropertyAsList(String prop, SourceBean memberSB, Map env) {
		JSONArray toReturn = new JSONArray();
		String propStr = (String) memberSB.getAttribute(prop);

		if (propStr == null)
			return toReturn;

		String[] propVal = propStr.split(",");
		for (int p = 0; p < propVal.length; p++) {
			if (propVal[p].trim().equals(""))
				continue;

			// replace the member name with analytical driver value if it's required
			if (propVal[p] != null && propVal[p].indexOf("$P{") >= 0) {
				int startPos = propVal[p].indexOf("$P{") + 3;
				int endPos = propVal[p].indexOf("}", startPos);
				String placeholder = propVal[p].substring(startPos, endPos);
				propVal[p] = (String) env.get(placeholder);
				logger.debug("Member name value getted from analytical driver [" + placeholder + "] is [" + toReturn + "]");
			}
			toReturn.put(propVal[p]);
		}
		return toReturn;
	}

}
