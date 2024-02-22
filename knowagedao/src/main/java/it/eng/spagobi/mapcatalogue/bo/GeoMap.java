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
package it.eng.spagobi.mapcatalogue.bo;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IBinContentDAO;
import it.eng.spagobi.commons.metadata.SbiBinContents;
import it.eng.spagobi.mapcatalogue.metadata.SbiGeoMaps;

import java.io.Serializable;

import org.apache.log4j.Logger;

/**
 * Defines a value constraint object.
 *
 * @author giachino
 *
 */

public class GeoMap implements Serializable {

	private int mapId;
	private String name;
	private String descr;
	private String url;
	private String format;
	private String hierarchyName;
	private Integer level;
	private String memberName;
	private int binId;

	private static Logger logger = Logger.getLogger(GeoMap.class);

	/**
	 * Gets the descr.
	 *
	 * @return the descr
	 */
	public String getDescr() {
		return descr;
	}

	/**
	 * Sets the descr.
	 *
	 * @param descr
	 *            the new descr
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}

	/**
	 * Gets the format.
	 *
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * Sets the format.
	 *
	 * @param format
	 *            the new format
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * Gets the map id.
	 *
	 * @return the map id
	 */
	public int getMapId() {
		return mapId;
	}

	/**
	 * Sets the map id.
	 *
	 * @param mapId
	 *            the new map id
	 */
	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the url.
	 *
	 * @param url
	 *            the new url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the hierarchyName
	 */
	public String getHierarchyName() {
		return hierarchyName;
	}

	/**
	 * @param hierarchyName
	 *            the hierarchyName to set
	 */
	public void setHierarchyName(String hierarchyName) {
		this.hierarchyName = hierarchyName;
	}

	/**
	 * @return the level
	 */
	public Integer getLevel() {
		return level;
	}

	/**
	 * @param level
	 *            the level to set
	 */
	public void setLevel(Integer level) {
		this.level = level;
	}

	/**
	 * @return the memberName
	 */
	public String getMemberName() {
		return memberName;
	}

	/**
	 * @param memberName
	 *            the memberName to set
	 */
	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	/**
	 * Gets the binary id of the map file (ie. the svg).
	 *
	 * @return the binId
	 */
	public int getBinId() {
		return binId;
	}

	/**
	 * Sets the binary id of the map file (ie. the svg)..
	 *
	 * @param binId
	 *            the binId to set
	 */
	public void setBinId(int binId) {
		this.binId = binId;
	}

	public SbiGeoMaps toSpagoBiGeoMaps() throws EMFUserError, EMFInternalError {
		logger.info("IN");
		SbiGeoMaps sbm = new SbiGeoMaps(getMapId());
		sbm.setName(getName());
		sbm.setDescr(getDescr());
		sbm.setFormat(getFormat());
		sbm.setHierarchyName(getHierarchyName());
		sbm.setLevel(getLevel());
		sbm.setMemberName(getMemberName());
		sbm.setUrl(getUrl());

		IBinContentDAO binContentDAO = DAOFactory.getBinContentDAO();
		try {
			byte[] binContentsContent = binContentDAO.getBinContent(getBinId());
			if (binContentsContent != null) {
				Integer contentId = getBinId();
				SbiBinContents sbiBinContents = new SbiBinContents(contentId);
				
				sbiBinContents.setContent(binContentsContent);
				sbm.setBinContents(sbiBinContents);
			}
		} catch (Exception e) {
			logger.error("Bin COntent not found");
		}

		logger.info("OUT");
		return sbm;
	}

}
