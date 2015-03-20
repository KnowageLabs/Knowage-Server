/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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


public class GeoMap  implements Serializable   {

	private int mapId;
	private String name;
	private String descr;
	private String url;	
	private String format;	
	private int binId;

	static private Logger logger = Logger.getLogger(GeoMap.class);


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
	 * @param descr the new descr
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
	 * @param format the new format
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
	 * @param mapId the new map id
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
	 * @param name the new name
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
	 * @param url the new url
	 */
	public void setUrl(String url) {
		this.url = url;
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
	 * Sets the  binary id of the map file (ie. the svg)..
	 * 
	 * @param binId the binId to set
	 */
	public void setBinId(int binId) {
		this.binId = binId;
	}


	public SbiGeoMaps toSpagoBiGeoMaps() throws EMFUserError, EMFInternalError {
		logger.info("IN");
		SbiGeoMaps sbm = new SbiGeoMaps();
		sbm.setMapId(getMapId());
		sbm.setName(getName());
		sbm.setDescr(getDescr());
		sbm.setFormat(getFormat());	
		sbm.setUrl(getUrl());

		IBinContentDAO binContentDAO=DAOFactory.getBinContentDAO();
		try{
			byte[] binContentsContent=binContentDAO.getBinContent(getBinId());
			if(binContentsContent!=null){
				Integer contentId=getBinId();
				SbiBinContents sbiBinContents=new SbiBinContents();
				sbiBinContents.setId(contentId);
				sbiBinContents.setContent(binContentsContent);
				sbm.setBinContents(sbiBinContents);
			}
		}
		catch (Exception e) {
			logger.error("Bin COntent not found");
		}


		logger.info("OUT");
		return sbm;
	}


}
