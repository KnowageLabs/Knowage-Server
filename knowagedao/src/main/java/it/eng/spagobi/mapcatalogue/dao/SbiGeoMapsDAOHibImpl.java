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
package it.eng.spagobi.mapcatalogue.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.metadata.SbiBinContents;
import it.eng.spagobi.mapcatalogue.bo.GeoMap;
import it.eng.spagobi.mapcatalogue.metadata.SbiGeoMaps;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author giachino antonella
 *
 */
public class SbiGeoMapsDAOHibImpl extends AbstractHibernateDAO implements ISbiGeoMapsDAO {

	private static final Logger LOGGER = Logger.getLogger(SbiGeoMapsDAOHibImpl.class);

	/**
	 * Load map by id.
	 *
	 * @param mapID the map id
	 *
	 * @return the geo map
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.mapcatalogue.dao.geo.bo.dao.ISbiGeoMapsDAO#loadMapByID(integer)
	 */
	@Override
	public GeoMap loadMapByID(Integer mapID) throws EMFUserError {
		GeoMap toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			SbiGeoMaps hibMap = (SbiGeoMaps) tmpSession.load(SbiGeoMaps.class, mapID);
			toReturn = hibMap.toGeoMap();
			tx.commit();

		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
		}
		return toReturn;
	}

	/**
	 * Load map by name.
	 *
	 * @param name the name
	 *
	 * @return the geo map
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.mapcatalogue.dao.geo.bo.dao.ISbiGeoMapsDAO#loadMapByName(string)
	 */
	@Override
	public GeoMap loadMapByName(String name) throws EMFUserError {
		GeoMap biMap = null;
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion labelCriterrion = Restrictions.eq("name", name);
			Criteria criteria = tmpSession.createCriteria(SbiGeoMaps.class);
			criteria.add(labelCriterrion);
			// List tmpLst = criteria.list();
			// return first map (unique)
			// if (tmpLst != null && tmpLst.size()>0) biMap = (SbiGeoMaps)tmpLst.get(0);
			// if (tmpLst != null && tmpLst.size()>0) biMap = (SbiGeoMaps)tmpLst.get(0);
			SbiGeoMaps hibMap = (SbiGeoMaps) criteria.uniqueResult();
			if (hibMap == null)
				return null;
			biMap = hibMap.toGeoMap();

			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
		}
		return biMap;
	}

	/**
	 * Load map by name and level.
	 *
	 * @param name  the name
	 *
	 * @param level the level
	 *
	 * @return the geo map
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.mapcatalogue.dao.geo.bo.dao.ISbiGeoMapsDAO#loadMapByNameLevel(string)
	 */
	@Override
	public GeoMap loadMapByHierarchyKey(String hierarchy, String member, String level) throws EMFUserError {
		GeoMap biMap = null;
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Query hqlQuery = tmpSession.createQuery(" from SbiGeoMaps m "
					+ "where m.hierarchyName=:hierarchy and m.memberName = :member and m.level = :level");

			hqlQuery.setString("hierarchy", hierarchy);
			hqlQuery.setString("member", member);
			hqlQuery.setInteger("level", Integer.valueOf(level));

			SbiGeoMaps hibMap = (SbiGeoMaps) hqlQuery.uniqueResult();

			if (hibMap == null) {
				LOGGER.error("SVG with hierarchyName [" + hierarchy + "] and memberName[" + member + "] and level ["
						+ level + "] non found in catalogue. ");
				throw new SpagoBIRuntimeException("SVG with hierarchyName [" + hierarchy + "] and memberName[" + member
						+ "] and level [" + level + "] non found in catalogue.");
			}
			biMap = hibMap.toGeoMap();

			tx.commit();
		} catch (org.hibernate.NonUniqueResultException nhe) {
			logException(nhe);
			rollbackIfActive(tx);
			throw new SpagoBIRuntimeException("There are more than one SVG loaded with Hierarchy [" + hierarchy
					+ "] - Member [" + member + "] - Level [" + level
					+ "]. Please verify the univocity of the hierarchy keys of the SVG documents!", nhe);
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
		}
		return biMap;
	}

	/**
	 * Modify map.
	 *
	 * @param aMap    the a map
	 * @param content the content file svg
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.geo.bo.dao.IEngineDAO#modifyEngine(it.eng.spagobi.bo.Engine)
	 */
	@Override
	public void modifyMap(GeoMap aMap, byte[] content) throws EMFUserError {

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			// inserts the svg file into sbi_binary_contents
			SbiBinContents hibBinContents = null;

			Integer binId = Integer.valueOf(aMap.getBinId());
			if (binId != null && binId > new Integer("0")) {
				hibBinContents = (SbiBinContents) tmpSession.load(SbiBinContents.class, binId);
				hibBinContents.setContent(content);
				updateSbiCommonInfo4Insert(hibBinContents);
				tmpSession.save(hibBinContents);
			} else {
				hibBinContents = new SbiBinContents();
				hibBinContents.setContent(content);
				Integer idBin = (Integer) tmpSession.save(hibBinContents);
				// recover the saved binary hibernate object
				hibBinContents = (SbiBinContents) tmpSession.load(SbiBinContents.class, idBin);
			}
			SbiGeoMaps hibMap = (SbiGeoMaps) tmpSession.load(SbiGeoMaps.class, aMap.getMapId());
			hibMap.setName(aMap.getName());
			hibMap.setDescr(aMap.getDescr());
			hibMap.setUrl(aMap.getUrl());
			hibMap.setFormat(aMap.getFormat());
			hibMap.setHierarchyName(aMap.getHierarchyName());
			hibMap.setLevel(aMap.getLevel());
			hibMap.setMemberName(aMap.getMemberName());
			hibMap.setBinContents(hibBinContents);
			updateSbiCommonInfo4Update(hibMap);
			tx.commit();

		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
		}

	}

	/**
	 * Insert map.
	 *
	 * @param aMap the a map
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.geo.bo.dao.IEngineDAO#insertEngine(it.eng.spagobi.bo.Engine)
	 */
	@Override
	public void insertMap(GeoMap aMap, byte[] content) throws EMFUserError {
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			// inserts the svg file into sbi_binary_contents
			SbiBinContents hibBinContents = new SbiBinContents();
			hibBinContents.setContent(content);
			Integer idBin = (Integer) tmpSession.save(hibBinContents);
			// recover the saved binary hibernate object
			hibBinContents = (SbiBinContents) tmpSession.load(SbiBinContents.class, idBin);

			SbiGeoMaps hibMap = new SbiGeoMaps();
			hibMap.setName(aMap.getName());
			hibMap.setDescr(aMap.getDescr());
			hibMap.setUrl(aMap.getUrl());
			hibMap.setFormat(aMap.getFormat());
			hibMap.setHierarchyName(aMap.getHierarchyName());
			hibMap.setLevel(aMap.getLevel());
			hibMap.setMemberName(aMap.getMemberName());

			hibMap.setBinContents(hibBinContents);
			updateSbiCommonInfo4Insert(hibMap);
			tmpSession.save(hibMap);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
		}
	}

	/**
	 * Erase map.
	 *
	 * @param aMap the a map
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.geo.bo.dao.IEngineDAO#eraseEngine(it.eng.spagobi.bo.Engine)
	 */
	@Override
	public void eraseMap(GeoMap aMap) throws EMFUserError {

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiGeoMaps hibMap = (SbiGeoMaps) tmpSession.load(SbiGeoMaps.class, aMap.getMapId());

			tmpSession.delete(hibMap);

			// delete template from sbi_binary_contents
			SbiBinContents hibBinCont = hibMap.getBinContents();
			if (hibBinCont != null)
				tmpSession.delete(hibBinCont);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
		}
	}

	/**
	 * Load all maps.
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public List loadAllMaps() throws EMFUserError {
		Session tmpSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			Query hibQuery = tmpSession.createQuery(" from SbiGeoMaps");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiGeoMaps hibMap = (SbiGeoMaps) it.next();
				if (hibMap != null) {
					GeoMap biMap = hibMap.toGeoMap();
					realResult.add(biMap);
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
		}
		return realResult;
	}

	/**
	 * Checks for features associated.
	 *
	 * @param mapId the map id
	 *
	 * @return true, if checks for features associated
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.mapcatalogue.dao.geo.bo.dao.ISbiGeoMapsDAO#hasFeaturesAssociated(java.lang.String)
	 */
	@Override
	public boolean hasFeaturesAssociated(String mapId) throws EMFUserError {
		boolean bool = false;

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Integer mapIdInt = Integer.valueOf(mapId);

			String hql = " from SbiGeoMapFeatures s where s.id.mapId =?";
			Query aQuery = tmpSession.createQuery(hql);
			aQuery.setInteger(0, mapIdInt);

			List biFeaturesAssocitedWithMap = aQuery.list();
			if (!biFeaturesAssocitedWithMap.isEmpty())
				bool = true;
			else
				bool = false;
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(tmpSession);
		}
		return bool;

	}

	/**
	 * Gets the features (tag <g>) from the SVG File.
	 *
	 * @param content the content of svg file
	 *
	 * @return the features from svg
	 *
	 * @throws Exception raised If there are some problems
	 */
	@Override
	public List getFeaturesFromSVG(byte[] content) throws Exception {
		LOGGER.debug("IN");
		// load a svg file
		XMLInputFactory xmlIF = XMLInputFactory.newInstance();
		xmlIF.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);

		// create a temporary file for gets the features:
		String javaIoTmpDir = System.getProperty("java.io.tmpdir");
		String tmpdir = null;
		if (javaIoTmpDir.endsWith(System.getProperty("file.separator"))) {
			tmpdir = javaIoTmpDir + "temp";
		} else {
			tmpdir = javaIoTmpDir + System.getProperty("file.separator") + "temp";
		}
		LOGGER.debug("** tmpdir: " + tmpdir);
		File dir = new File(tmpdir);
		dir.mkdirs();
		LOGGER.debug("Temporary file created.");
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile("svgfile", ".svg", dir);
			try (OutputStream out = new FileOutputStream(tmpFile)) {
				out.write(content);
			}
		} catch (Exception e) {
			LOGGER.error("Error while creating outputstream: ", e);
		}

		FileInputStream fisMap = null;
		List lstFeatures = null;
		HashMap feature;

		try {
			fisMap = new FileInputStream(tmpFile);
		} catch (FileNotFoundException e) {
			LOGGER.error("file svg not found, path " + tmpFile);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "error.mapfile.notfound");
		}
		XMLStreamReader streamReader = null;
		try {
			streamReader = xmlIF.createXMLStreamReader(fisMap);
		} catch (XMLStreamException e) {
			LOGGER.error("Cannot load the stream of the file svg, path " + tmpFile);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "error.mapfile.notloaded");
		}
		if (streamReader == null) {
			LOGGER.debug("streamReader is null.");
			throw new EMFUserError(EMFErrorSeverity.ERROR, "error.mapfile.notloaded");
		}

		try {
			streamReader.next();
			int event = streamReader.getEventType();
			int nFeature = -1;
			lstFeatures = new ArrayList();
			while (true) {
				switch (event) {
				case XMLStreamConstants.START_DOCUMENT:
					break;
				case XMLStreamConstants.START_ELEMENT:
					// get the tag name
					String tagname = streamReader.getLocalName();
					if (tagname.trim().equalsIgnoreCase("g")) {
						for (int i = 0, n = streamReader.getAttributeCount(); i < n; ++i) {
							String attrName = streamReader.getAttributeName(i).toString();
							String attrValue = streamReader.getAttributeValue(i);
							// if the attribute is the id, search values and set the style
							feature = new HashMap();
							if (attrName.equalsIgnoreCase("id")) {
								nFeature++;
								feature.put("id", attrValue);
							}
							if (attrName.equalsIgnoreCase("descr")) {
								feature.put("descr", attrValue);
							}
							if (attrName.equalsIgnoreCase("type")) {
								feature.put("type", attrValue);
							}
							if (feature.size() > 0)
								lstFeatures.add(feature);
						}
					}
					break;
				case XMLStreamConstants.END_ELEMENT:
					// tagname = streamReader.getLocalName();
					// if(tagname.trim().equalsIgnoreCase("g")) {
					// }
					break;
				case XMLStreamConstants.END_DOCUMENT:
					break;
				}
				if (!streamReader.hasNext())
					break;
				event = streamReader.next();
			}
		} catch (XMLStreamException xe) {
			LOGGER.error("Error while parsign the svg file: " + xe.getMessage());
			throw new EMFUserError(EMFErrorSeverity.ERROR, "5031", "component_mapcatalogue_messages");
		} finally {
			streamReader.close();
		}

		// instant cleaning
		if (tmpFile != null) {
			Files.deleteIfExists(tmpFile.toPath());
		}
		if (dir != null) {
			Files.deleteIfExists(dir.toPath());
		}

		LOGGER.debug("OUT");
		return lstFeatures;
	}

}