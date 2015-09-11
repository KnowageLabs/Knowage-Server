/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.configuration.dao.fileimpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import it.eng.qbe.datasource.configuration.dao.DAOException;
import it.eng.qbe.datasource.configuration.dao.IHierarchiesDAO;
import it.eng.qbe.model.structure.HierarchicalDimensionField;
import it.eng.qbe.model.structure.Hierarchy;
import it.eng.qbe.model.structure.HierarchyLevel;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class HierarchiesDAOFileImpl implements IHierarchiesDAO {

	protected File modelJarFile;

	public static final String HIERARCHIES_FROM_META_FILE_NAME = "hierarchies.xml";

	public final static String ROOT_TAG = "Dimensions";

	public final static String DIMENSION_TAG = "Dimension";
	public final static String HIERARCHIES_TAG = "Hierarchies";
	public final static String HIERARCHY_TAG = "Hierarchy";
	public final static String LEVEL_TAG = "Level";

	public final static String FIELD_TAG = "CFIELD";
	public final static String FIELD_TAG_ENTIY_ATTR = "entity";
	public final static String FIELD_TAG_NAME_ATTR = "name";
	public final static String FIELD_TAG_TYPE_ATTR = "type";
	public final static String FIELD_TAG_NATURE_ATTR = "nature";
	public final static String FIELD_TAG_IN_LINE_ATTR = "isInLine";

	public static transient Logger logger = Logger.getLogger(HierarchiesDAOFileImpl.class);

	public HierarchiesDAOFileImpl(File modelJarFile) {
		this.modelJarFile = modelJarFile;
	}

	// =============================================================================
	// LOAD
	// =============================================================================
	public Map<String, HierarchicalDimensionField> loadHierarchicalDimensions() {
		File hierarchiesFile;
		Map<String, HierarchicalDimensionField> hierarchiesMap;

		hierarchiesMap = new HashMap<String, HierarchicalDimensionField>();

		hierarchiesFile = getMetaHierarchiesFile();
		loadHierarchicalDimensionsFromFile(hierarchiesFile, hierarchiesMap);
		return hierarchiesMap;
	}

	private void loadHierarchicalDimensionsFromFile(File hierarchicalDimensionsFile, Map<String, HierarchicalDimensionField> hierarchicalDimensionFieldsMap) {

		FileInputStream in;
		Document document;

		String entity;
		String name;

		HierarchicalDimensionField hierarchicalDimensionField;
		List<?> dimensionFieldNodes;
		Iterator<?> it;
		Node dimensionFieldNode;

		logger.debug("IN");

		in = null;

		try {

			logger.debug("Load hierarchical dimension fields from file [" + hierarchicalDimensionsFile + "]");

			document = guardedRead(hierarchicalDimensionsFile);

			if (document != null) {

				dimensionFieldNodes = document.selectNodes("//" + ROOT_TAG + "/" + DIMENSION_TAG + "");
				logger.debug("Found [" + dimensionFieldNodes.size() + "] dimension field/s");

				it = dimensionFieldNodes.iterator();
				while (it.hasNext()) {
					dimensionFieldNode = (Node) it.next();

					name = dimensionFieldNode.valueOf("@" + FIELD_TAG_NAME_ATTR);
					entity = dimensionFieldNode.valueOf("@" + FIELD_TAG_ENTIY_ATTR);

					hierarchicalDimensionField = new HierarchicalDimensionField(name, entity);

					// parse hierarchies
					List<Hierarchy> hierarchies = loadHierarchies(dimensionFieldNode);
					hierarchicalDimensionField.setHierarchies(hierarchies);

					hierarchicalDimensionFieldsMap.put(entity, hierarchicalDimensionField);

					// if(!hierarchicalDimensionFieldsMap.containsKey(entity)) {
					// hierarchicalDimensionFieldsMap.put(entity, new ArrayList<HierarchicalDimensionField>());
					// }

					// hierarchicalDimensions.add(hierarchicalDimensionField);

					logger.debug("Hierarchical dimension field [" + hierarchicalDimensionField.getName() + "] loaded succesfully");
				}
			} else {
				logger.debug("File [" + hierarchicalDimensionsFile + "] does not exist. No calculated fields have been loaded.");
			}
		} catch (Throwable t) {
			if (t instanceof DAOException)
				throw (DAOException) t;
			throw new DAOException("An unpredicted error occurred while loading calculated fields on file [" + hierarchicalDimensionsFile + "]", t);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new DAOException("Impossible to properly close stream to file file [" + hierarchicalDimensionsFile + "]", e);
				}
			}
			logger.debug("OUT");
		}
	}

	private List<Hierarchy> loadHierarchies(Node dimensionNode) {
		List<Hierarchy> hierarchies = new ArrayList<Hierarchy>();

		Node hierarchiesBlock = dimensionNode.selectSingleNode(HIERARCHIES_TAG);
		if (hierarchiesBlock != null) {
			List<?> hierarchyNodes = hierarchiesBlock.selectNodes(HIERARCHY_TAG);

			for (Object hierarchyNode : hierarchyNodes) {
				Hierarchy hierarchy = loadHierarchy((Node) hierarchyNode);
				hierarchies.add(hierarchy);
			}
		}

		return hierarchies;
	}

	private Hierarchy loadHierarchy(Node hierarchyNode) {
		String name = hierarchyNode.valueOf("@name");
		Boolean isDefault = Boolean.getBoolean(hierarchyNode.valueOf("@default"));
		Hierarchy hierarchy = new Hierarchy(name, isDefault);
		List<?> levelNodes = hierarchyNode.selectNodes(LEVEL_TAG);

		List<HierarchyLevel> levels = new LinkedList<HierarchyLevel>();
		for (Object levelObj : levelNodes) {
			Node levelNode = (Node) levelObj;
			String levelName = levelNode.valueOf("@name");
			String levelColumn = levelNode.valueOf("@column");
			String levelType = levelNode.valueOf("@type");
			HierarchyLevel level = new HierarchyLevel(levelName, levelColumn, levelType);
			levels.add(level);
		}
		hierarchy.setLevels(levels);
		return hierarchy;

	}

	private File getMetaHierarchiesFile() {
		File hierarchiesFile = null;
		hierarchiesFile = new File(modelJarFile.getParentFile(), HIERARCHIES_FROM_META_FILE_NAME);
		return hierarchiesFile;
	}

	// ------------------------------------------------------------------------------------------------------
	// Guarded actions. see -> http://java.sun.com/docs/books/tutorial/essential/concurrency/guardmeth.html
	// ------------------------------------------------------------------------------------------------------

	private boolean locked = false;

	private synchronized void getLock() {
		while (locked) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		locked = true;
	}

	private synchronized void releaseLock() {
		locked = false;
		notifyAll();
	}

	private Document guardedRead(File file) {
		InputStream in;
		SAXReader reader;
		Document document = null;

		logger.debug("IN");

		in = null;
		reader = null;
		ZipEntry zipEntry = null;
		JarFile jarFile = null;
		try {

			logger.debug("acquiring lock...");
			getLock();
			logger.debug("Lock acquired");

			if (file.exists()) {
				in = new FileInputStream(file);
			} else {

				zipEntry = null;
				jarFile = new JarFile(modelJarFile);
				zipEntry = jarFile.getEntry(file.getName());

				if (zipEntry != null) {
					in = jarFile.getInputStream(zipEntry);
					// jarFile.close();
				} else {
					jarFile.close();
					return null;
				}

				Assert.assertNotNull(in, "Input stream cannot be null");

				reader = new SAXReader();
				document = reader.read(in);

				Assert.assertNotNull(document, "Document cannot be null");

			}
		} catch (DocumentException de) {
			DAOException e = new DAOException("Impossible to parse file [" + file.getName() + "]", de);
			e.addHint("Check if [" + file + "] is a well formed XML file");
			throw e;
		} catch (FileNotFoundException fnfe) {
			DAOException e = new DAOException("Impossible to load calculated fields from file [" + file.getName() + "]", fnfe);
			e.addHint("Check if [" + file.getPath() + "] folder exist on your server filesystem. If not create it.");
			throw e;
		} catch (IOException ioe) {
			throw new SpagoBIRuntimeException("Impossible to load properties from file [" + zipEntry + "]");
		} catch (Throwable t) {
			if (t instanceof DAOException)
				throw (DAOException) t;
			throw new DAOException("An unpredicetd error occurred while writing on file [" + file + "]");
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new DAOException("Impossible to properly close stream to file [" + file + "]", e);
				}
			}
			if (jarFile != null) {
				try {
					jarFile.close();
				} catch (IOException e) {
					throw new DAOException("Impossible to properly close stream to file [" + jarFile + "]", e);
				}
			}
			logger.debug("releasing lock...");
			releaseLock();
			logger.debug("lock released");

			logger.debug("OUT");
		}

		return document;
	}

}
