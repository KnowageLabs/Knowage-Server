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
package it.eng.spagobi.analiticalmodel.document.bo;

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;

public class Snapshot extends SnapshotMainInfo {

	static private Logger logger = Logger.getLogger(Snapshot.class);

	private String time = null;
	private Integer binId = null;
	private byte[] content = null;
	private String contentType = null;
	private String schedulation;
	private String scheduler;
	private Integer schedulationStartDate;
	private Integer sequence;

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Gets the bin id.
	 *
	 * @return the bin id
	 */
	public Integer getBinId() {
		return binId;
	}

	/**
	 * Sets the bin id.
	 *
	 * @param binId
	 *            the new bin id
	 */
	public void setBinId(Integer binId) {
		this.binId = binId;
	}

	/**
	 * Tries to load binary content from database for this Snapshot instance, given its binary content identifier, if content field is null.
	 *
	 * @return The binary content of this instance; if it is null, it tries to load it from database if binary content identifier is available
	 *
	 * @throws EMFUserError
	 *             if some errors while reading from db occurs
	 * @throws EMFInternalError
	 *             if some errors while reading from db occurs
	 */
	public byte[] getContent() throws EMFUserError, EMFInternalError {
		if (content == null) {
			if (binId != null) {
				content = DAOFactory.getBinContentDAO().getBinContent(binId);
			} else {
				logger.warn("Both content field of this istance and binary identifier are null. Cannot load content from database.");
			}
		}
		return content;
	}

	/**
	 * Sets the content.
	 * <p>
	 * <b> Note: </b> getContent() method checking if content field is null. If it is, getContent() method will try to load it from database if binary content
	 * identifier is available. <b> Pay attention when using this method! </b>
	 * </p>
	 *
	 * @param content
	 *            the new content
	 */
	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getSchedulation() {
		return schedulation;
	}

	public void setSchedulation(String schedulation) {
		this.schedulation = schedulation;
	}

	public String getScheduler() {
		return scheduler;
	}

	public void setScheduler(String scheduler) {
		this.scheduler = scheduler;
	}

	public Integer getSchedulationStartDate() {
		return schedulationStartDate;
	}

	public void setSchedulationStartDate(Integer schedulationStartDate) {
		this.schedulationStartDate = schedulationStartDate;
	}

	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

}
