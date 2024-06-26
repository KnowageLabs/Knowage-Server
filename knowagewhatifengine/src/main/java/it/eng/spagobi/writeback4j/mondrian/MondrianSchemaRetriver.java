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

package it.eng.spagobi.writeback4j.mondrian;

import it.eng.spagobi.engines.whatif.common.WhatIfConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.writeback4j.IMemberCoordinates;
import it.eng.spagobi.writeback4j.ISchemaRetriver;
import it.eng.spagobi.writeback4j.sql.TableEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mondrian.olap.MondrianDef;
import mondrian.olap.MondrianDef.CubeDimension;
import mondrian.olap.MondrianDef.Dimension;
import mondrian.olap.MondrianDef.Measure;

import org.apache.log4j.Logger;
import org.eigenbase.xom.NodeDef;
import org.eigenbase.xom.Parser;
import org.eigenbase.xom.XOMException;
import org.eigenbase.xom.XOMUtil;
import org.olap4j.OlapException;
import org.olap4j.metadata.Dimension.Type;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class MondrianSchemaRetriver implements ISchemaRetriver {

	public static transient Logger logger = Logger.getLogger(MondrianSchemaRetriver.class);

	MondrianDef.Schema schema;
	MondrianDef.Cube editCube;

	public static String ALL_MEMBER_NAME = "(All)";
	private String versionColumnName;
	private String versionTableName;

	public MondrianSchemaRetriver(MondrianDriver driver, String editCubeName) throws SpagoBIEngineException {
		String catalogUri = driver.getOlapSchema();
		File tmpFile = new File(catalogUri);
		FileInputStream fis;
		Parser xmlParser;

		logger.debug("IN");
		logger.debug("Loading the schema from the file " + catalogUri);

		try {
			fis = new FileInputStream(tmpFile);
			xmlParser = XOMUtil.createDefaultParser();
			schema = new MondrianDef.Schema(xmlParser.parse(fis));
		} catch (FileNotFoundException e) {
			logger.error("File not found Error loading the file with the schema with url " + catalogUri, e);
			throw new SpagoBIEngineException("File not found Error loading the file with the schema with url " + catalogUri, e);
		} catch (XOMException e) {
			logger.error("Error loading the file with the schema with url " + catalogUri, e);
			throw new SpagoBIEngineException("Error loading the file with the schema with url " + catalogUri, e);
		}
		logger.debug("File loaded ");

		logger.debug("Getting the cube for edit. The name of the cube is " + editCubeName);
		MondrianDef.Cube[] cubes = schema.cubes;
		for (int i = 0; i < cubes.length; i++) {
			MondrianDef.Cube aCube = cubes[i];
			if (aCube.name.equals(editCubeName)) {
				editCube = aCube;
			}
		}
		
		
		logger.debug("Cube for writing correctly loaded");
	}

	public MondrianSchemaRetriver(MondrianDriver driver) throws SpagoBIEngineException {
		String catalogUri = driver.getOlapSchema();
		File tmpFile = new File(catalogUri);
		FileInputStream fis;
		Parser xmlParser;

		logger.debug("IN");
		logger.debug("Loading the schema from the file " + catalogUri);

		try {
			fis = new FileInputStream(tmpFile);
			xmlParser = XOMUtil.createDefaultParser();
			schema = new MondrianDef.Schema(xmlParser.parse(fis));
		} catch (FileNotFoundException e) {
			logger.error("File not found Error loading the file with the schema with url " + catalogUri, e);
			throw new SpagoBIEngineException("File not found Error loading the file with the schema with url " + catalogUri, e);
		} catch (XOMException e) {
			logger.error("Error loading the file with the schema with url " + catalogUri, e);
			throw new SpagoBIEngineException("Error loading the file with the schema with url " + catalogUri, e);
		}
		logger.debug("File loaded ");
		logger.debug("Cube for writing correctly loaded");
	}

	public List<String> getAllCubes() {
		List<String> cubesList = new ArrayList<String>();
		MondrianDef.Cube[] cubes = schema.cubes;
		MondrianDef.VirtualCube[] virtualCubes = schema.virtualCubes;
		for (int i = 0; i < cubes.length; i++) {
			cubesList.add(cubes[i].name);

		}
		for (int i = 0; i < virtualCubes.length; i++) {
			cubesList.add(virtualCubes[i].name);
		}
		return cubesList;
	}
	
	public List<String> getCubes() {
		List<String> cubesList = new ArrayList<String>();
		MondrianDef.Cube[] cubes = schema.cubes;
		MondrianDef.VirtualCube[] virtualCubes = schema.virtualCubes;
		for (int i = 0; i < cubes.length; i++) {
			cubesList.add(cubes[i].name);

		}
		
		return cubesList;
	}

	public String getFirstDimension(String cubeName) {

		logger.debug("IN");
		String toReturn = null;
		MondrianDef.Cube[] cubes = schema.cubes;
		MondrianDef.VirtualCube[] virtualCubes = schema.virtualCubes;
		for (int i = 0; i < cubes.length; i++) {
			MondrianDef.Cube oldCube = cubes[i];
			if (oldCube.name.equals(cubeName)) {
				logger.debug("IN: getting first dimesion form the cube");
				MondrianDef.CubeDimension[] dimensons = oldCube.dimensions;
				MondrianDef.CubeDimension aDimension = dimensons[0];
				toReturn = aDimension.name;

			}
		}
		for (int i = 0; i < virtualCubes.length; i++) {
			MondrianDef.VirtualCube oldCube = virtualCubes[i];
			if (oldCube.name.equals(cubeName)) {
				logger.debug("IN: getting first dimesion form the cube");
				MondrianDef.VirtualCubeDimension[] dimensons = oldCube.dimensions;
				MondrianDef.VirtualCubeDimension aDimension = dimensons[0];
				toReturn = aDimension.name;

			}
		}
		logger.debug("OUT");
		return toReturn;

	}

	public String getFirstMeasure(String cubeName) {

		logger.debug("IN");
		String toReturn = null;
		MondrianDef.Cube[] cubes = schema.cubes;
		MondrianDef.VirtualCube[] virtualCubes = schema.virtualCubes;
		for (int i = 0; i < cubes.length; i++) {
			MondrianDef.Cube oldCube = cubes[i];
			if (oldCube.name.equals(cubeName)) {

				logger.debug("IN: loading the measure form the cube");
				for(int j = 0;j < oldCube.measures.length;j++){
					if (oldCube.measures[j].visible == null || oldCube.measures[j].visible == true) {
						toReturn = oldCube.measures[j].name;
						break;
					}
				}
				

			}
		}
		for (int i = 0; i < virtualCubes.length; i++) {
			MondrianDef.VirtualCube oldCube = virtualCubes[i];
			if (oldCube.name.equals(cubeName)) {

				logger.debug("IN: loading the measure form the virtual cube");
				for(int k = 0;k < oldCube.measures.length;k++){
					if (oldCube.measures[k].visible == null || oldCube.measures[k].visible == true) {
						if (oldCube.measures[0].name.startsWith("[")) {
							toReturn = oldCube.measures[k].name;
							int indexOfFirstLeftBracket = toReturn.lastIndexOf("[");
							int indexOfFirstRightBracket = toReturn.lastIndexOf("]");
							toReturn = toReturn.substring(indexOfFirstLeftBracket, indexOfFirstRightBracket + 1);
							toReturn = toReturn.substring(1, toReturn.length() - 1);
							break;
						}

					}
				}
				
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	public List<String> getAllMeasures(String cubeName) {
		List<String> measuresList = new ArrayList<String>();
		logger.debug("IN");
		String toReturn = null;
		MondrianDef.Cube[] cubes = schema.cubes;
		MondrianDef.VirtualCube[] virtualCube = schema.virtualCubes;
		for (int i = 0; i < cubes.length; i++) {
			MondrianDef.Cube oldCube = cubes[i];
			if (oldCube.name.equals(cubeName)) {
				logger.debug("IN: loading the measure form the cube");
				for (int j = 0; j < oldCube.measures.length; j++) {
					measuresList.add(oldCube.measures[j].name);
				}
			}
		}
		for (int i = 0; i < virtualCube.length; i++) {
			MondrianDef.VirtualCube oldCube = virtualCube[i];
			if (oldCube.name.equals(cubeName)) {
				logger.debug("IN: loading the measure form the cube");
				if (oldCube.name.equals(cubeName)) {
					logger.debug("IN: loading the measure form the cube");
					for (int j = 0; j < oldCube.measures.length; j++) {
						if (oldCube.measures[j].name.startsWith("[")) {
							toReturn = oldCube.measures[j].name;
							int indexOfFirstLeftBracket = toReturn.lastIndexOf("[");
							int indexOfFirstRightBracket = toReturn.lastIndexOf("]");
							toReturn = toReturn.substring(indexOfFirstLeftBracket, indexOfFirstRightBracket + 1);
							toReturn = toReturn.substring(1, toReturn.length() - 1);
						}
						measuresList.add(toReturn);
					}
				}
			}
		}
		logger.debug("OUT");
		return measuresList;
	}

	public IMemberCoordinates getMemberCordinates(Member member) {
		logger.debug("IN");

		// get the dimension for the member
		MondrianDef.CubeDimension mondrianDimension = getMondrianDimension(member.getLevel());

		// get the hierarchy of the level
		MondrianDef.Hierarchy mondrianHierarchy = getMondrianHierarchy(member.getLevel(), mondrianDimension.getDimension(schema));

		// For each level starting from the root to the Get the map between the
		// hierarchy of the member( root member, child, granchild, .... member)
		// and the level
		Map<TableEntry, Member> mapTableEntryValue = getMemberColumnMap(member, mondrianHierarchy);

		logger.debug("OUT");

		return new MondrianMemberCoordinates(mondrianDimension, mondrianHierarchy, mapTableEntryValue);

	}

	/**
	 * Get the Dimension that includes the level
	 *
	 * @param level
	 * @return the dimension that includes the level
	 */
	public MondrianDef.CubeDimension getMondrianDimension(Level level) {

		logger.debug("IN");

		String dimension = level.getDimension().getName();
		MondrianDef.CubeDimension[] dimensons = editCube.dimensions;
		for (int i = 0; i < dimensons.length; i++) {
			MondrianDef.CubeDimension aDimension = dimensons[i];
			if (aDimension.name.equals(dimension)) {
				logger.debug("OUT");
				return aDimension;
			}
		}

		logger.error("Impossible to find the dimension for the level " + level.getUniqueName());
		throw new SpagoBIEngineRuntimeException("Impossible to find the dimension for the level " + level.getUniqueName());
	}

	/**
	 * Gets the hierarchy of the level inside the passed dimension
	 *
	 * @param level
	 * @param thisDimension
	 * @return the hierarchy of the level inside the passed dimension
	 */
	public MondrianDef.Hierarchy getMondrianHierarchy(Level level, Dimension thisDimension) {
		logger.debug("IN");
		String hierarchy = level.getHierarchy().getName();
		MondrianDef.Hierarchy[] thisHierarchies = thisDimension.hierarchies;
		for (int j = 0; j < thisHierarchies.length; j++) {
			MondrianDef.Hierarchy aHierarchy = thisHierarchies[j];
			if (aHierarchy.name == null || aHierarchy.name.equals(hierarchy)) {
				logger.debug("OUT");
				return aHierarchy;
			}

		}
		logger.error("Impossible to find the hierarchy for the level " + level.getUniqueName());
		throw new SpagoBIEngineRuntimeException("Impossible to find the hierarchy for the level " + level.getUniqueName());
	}

	/**
	 * For each level starting from the root to the Get the map between the
	 * hierarchy of the member( root member, child, granchild, .... member) and
	 * the level. We need this information because we want to know table and
	 * column linked to the members
	 *
	 * @param member
	 * @param mondrianHierarchy
	 * @return the map Level-->Member of the level
	 */
	public Map<TableEntry, Member> getMemberColumnMap(Member member, MondrianDef.Hierarchy mondrianHierarchy) {

		logger.debug("IN");

		Map<TableEntry, Member> mapTableEntryValue = new HashMap<TableEntry, Member>();
		// int memberDepth = member.getDepth();
		Level memberLevel = member.getLevel();

		// get all the levels starting from the root to the one that contains
		// the passed member
		List<MondrianDef.Level> memberValues = getLevels(memberLevel, mondrianHierarchy);

		// Create a Map that links the member with the level that contains it
		Member aMember = member;
		for (int i = memberValues.size() - 1; i >= 0; i--) {
			MondrianDef.Level aLevel = memberValues.get(i);
			String table = aLevel.table;
			if (table == null) {
				table = getTableName(mondrianHierarchy);
			}
			mapTableEntryValue.put(new TableEntry(aLevel.column, table), aMember);
			aMember = aMember.getParentMember();
		}

		logger.debug("OUT");
		return mapTableEntryValue;
	}

	/**
	 * Gets the first n levels of a hierarchy
	 *
	 * @param levelsDepth
	 *            the number of levels to get
	 * @param aHierarchy
	 * @return the first "levelsDepth" levels of aHierarchy
	 */
	public List<MondrianDef.Level> getLevels(Level memberLevel, MondrianDef.Hierarchy aHierarchy) {
		logger.debug("IN");
		List<MondrianDef.Level> levelColumns = new ArrayList<MondrianDef.Level>();

		if (memberLevel.getName().equals(ALL_MEMBER_NAME)) {
			logger.debug("All member for Hierarchy " + aHierarchy.getName());
		} else {
			MondrianDef.Level[] schemaLevels = aHierarchy.levels;
			int i = 0;

			while (true) {
				MondrianDef.Level aMondrianLevel = schemaLevels[i];
				levelColumns.add(aMondrianLevel);
				if (aMondrianLevel.name.equals(memberLevel.getName())) {
					break;
				}
				i++;
			}
		}

		logger.debug("OUT");
		return levelColumns;
	}

	/**
	 * Gets the name of the column of the measure
	 *
	 * @param member
	 * @return
	 * @throws SpagoBIEngineException
	 */
	public String getMeasureColumn(Member member) throws SpagoBIEngineException {
		String measure = member.getName();
		try {
			if ((member.getDimension().getDimensionType().equals(Type.MEASURE))) {
				for (int i = 0; i < editCube.measures.length; i++) {
					if (editCube.measures[i].name.equals(measure)) {
						return editCube.measures[i].column;
					}
				}
			}
		} catch (OlapException e) {
			logger.error("Error loading the measure linked to the member " + member.getUniqueName(), e);
			throw new SpagoBIEngineException("Error loading the measure linked to the member " + member.getUniqueName(), e);
		}

		return null;
	}

	/**
	 * Gets the column name for each measure
	 *
	 * @return
	 */
	public List<String> getMeasuresColumn() {

		logger.debug("IN: loading the measure form the cube");
		List<String> measures = new ArrayList<String>();

		for (int i = 0; i < editCube.measures.length; i++) {
			measures.add(editCube.measures[i].column);
		}

		logger.debug("OUT");
		return measures;
	}

	public String getEditCubeTableName() {
		return editCube.fact.getAlias();
	}

	/**
	 * Returns physical name of all columns of edit cube
	 *
	 * @return columns names list
	 */

	public List<String> getColumnNamesList() {
		logger.debug("IN");
		List<String> toReturn = new ArrayList<String>();

		// add measures names
		MondrianDef.Measure[] measures = editCube.measures;
		for (int i = 0; i < measures.length; i++) {
			Measure measure = measures[i];
			if (measure.column != null && !measure.column.equalsIgnoreCase("")) {
				toReturn.add(measure.column);
			}
		}

		// add dimension names
		MondrianDef.CubeDimension[] dimensions = editCube.dimensions;
		for (int i = 0; i < dimensions.length; i++) {
			CubeDimension dimensione = dimensions[i];
			if (dimensione.foreignKey != null && !dimensione.foreignKey.equalsIgnoreCase("")) {
				toReturn.add(dimensione.foreignKey);
			}
		}

		logger.debug("OUT");
		return toReturn;
	}

	public String getVersionColumnName() {
		;
		logger.debug("IN");
		if (versionColumnName != null) {
			logger.debug("Version column name is in the cache");
			return versionColumnName;
		}
		logger.debug("Version column name isn't in the cache");
		String dimension = WhatIfConstants.VERSION_DIMENSION_NAME;
		Dimension thisDimension = null;
		MondrianDef.CubeDimension[] dimensons = editCube.dimensions;
		for (int i = 0; i < dimensons.length; i++) {
			MondrianDef.CubeDimension aDimension = dimensons[i];
			if (aDimension.name.equals(dimension)) {
				thisDimension = aDimension.getDimension(schema);
				break;
			}
		}
		if (thisDimension == null) {
			logger.error("Error loading the verison dimension " + WhatIfConstants.VERSION_DIMENSION_NAME);
			throw new SpagoBIEngineRuntimeException("Error loading the verison dimension " + WhatIfConstants.VERSION_DIMENSION_NAME);
		}
		MondrianDef.Hierarchy thisHierarchy = thisDimension.hierarchies[0];
		versionColumnName = thisHierarchy.levels[0].column;

		logger.debug("OUT");

		return versionColumnName;

	}

	public String getVersionTableName() {
		;
		logger.debug("IN");
		if (versionTableName != null) {
			logger.debug("Version table name is in the cache");
			return versionTableName;
		}
		logger.debug("Version table name isn't in the cache");
		String dimension = WhatIfConstants.VERSION_DIMENSION_NAME;
		Dimension thisDimension = null;
		MondrianDef.CubeDimension[] dimensons = editCube.dimensions;
		for (int i = 0; i < dimensons.length; i++) {
			MondrianDef.CubeDimension aDimension = dimensons[i];
			if (aDimension.name.equals(dimension)) {
				thisDimension = aDimension.getDimension(schema);
				break;
			}
		}
		if (thisDimension == null) {
			logger.error("Error loading the verison dimension " + WhatIfConstants.VERSION_DIMENSION_NAME);
			throw new SpagoBIEngineRuntimeException("Error loading the verison dimension " + WhatIfConstants.VERSION_DIMENSION_NAME);
		}
		MondrianDef.Hierarchy thisHierarchy = thisDimension.hierarchies[0];
		versionTableName = getTableName(thisHierarchy);

		logger.debug("OUT");

		return versionTableName;

	}

	public static String getTableName(MondrianDef.Hierarchy hierarchy) {
		String tableName = hierarchy.primaryKeyTable;
		if (tableName == null) {
			NodeDef[] children = hierarchy.getChildren();
			for (int i = 0; i < children.length; i++) {
				NodeDef node = children[i];
				if (node instanceof MondrianDef.Table) {
					tableName = ((MondrianDef.Table) node).name;
					break;
				}
			}
		}
		return tableName;
	}

}
