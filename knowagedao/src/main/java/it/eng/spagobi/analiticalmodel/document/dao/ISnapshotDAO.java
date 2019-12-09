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
package it.eng.spagobi.analiticalmodel.document.dao;

import java.util.List;
import java.util.Map;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.bo.SnapshotMainInfo;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

public interface ISnapshotDAO extends ISpagoBIDao {

	/**
	 * Save a snapshot of the object.
	 *
	 * @param content
	 *            byte array containing the content of the snapshot
	 * @param idBIObj
	 *            the id of the biobject parent
	 * @param name
	 *            the name of the new subobject
	 * @param description
	 *            the description of the new subobject
	 * @param contentType
	 * @param schedulationStart
	 *            time when schedulation is started
	 * @param schedulationName
	 *            name of the schedulation
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void saveSnapshot(byte[] content, Integer idBIObj, String name, String description, String contentType, long schedulationStart, String schedulerName,
			String schedulationName, int sequence) throws EMFUserError;

	/**
	 * Gets the list of the snapshot details that are children of a biobject.
	 *
	 * @param idBIObj
	 *            the id of the biobject parent
	 *
	 * @return List of BIObject.BIObjectSnapshot objects
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public List getSnapshots(Integer idBIObj) throws EMFUserError;

	/**
	 * Gets the list of the snapshot main details.
	 *
	 * Every item of the return list contains only a subset of
	 *
	 * @param idBIObj
	 *            the id of the biobject parent
	 *
	 * @return List of {@link SnapshotMainInfo} objects
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public List<SnapshotMainInfo> getSnapshotMainInfos(Integer idBIObj) throws EMFUserError;

	/**
	 * Delete a snapshot.
	 *
	 * @param idSnap
	 *            the id of the snapshot
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void deleteSnapshot(Integer idSnap) throws EMFUserError;

	/**
	 * Load a snapshot.
	 *
	 * @param idSnap
	 *            the id of the snapshot
	 *
	 * @return Snapshot the snapshot loaded
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public Snapshot loadSnapshot(Integer idSnap) throws EMFUserError;

	/**
	 * Gets the last snapshot that is children of a biobject.
	 *
	 * @param idBIObj
	 *            the id of the biobject parent
	 *
	 * @return Snapshot the snapshot loaded
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public Snapshot getLastSnapshot(Integer idBIObj) throws EMFUserError;

	/**
	 * Get all snapshot linked to a skedulation
	 *
	 * @param Schedulation
	 *            name
	 *
	 * @return returns all the snapshot groupped by schedulations and execution time
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public Map<String, Map<Integer, List<Snapshot>>> getSnapshotsBySchedulation(String schedulationName, boolean collate, boolean loadContent)
			throws EMFUserError;

	/**
	 * same as getSnapshotsBySchedulation just takes last
	 *
	 * @param schedulationName
	 * @param collate
	 * @return
	 * @throws EMFUserError
	 */
	public List<Snapshot> getLastSnapshotsBySchedulation(String schedulationName, boolean collate) throws EMFUserError;

	/**
	 * Gets the list of the snapshot details that are children of a biobject and belongs to a specific schedulation.
	 *
	 * @param idBIObj
	 *            the id of the biobject parent
	 *
	 * @param schedulation
	 *            the name of the schedulation
	 *
	 * @return List of BIObject.BIObjectSnapshot objects
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public List getSnapshotsForSchedulationAndDocument(Integer idBIObj, String scheduler, boolean loadContent) throws EMFUserError;

	public String loadSnapshotSchedulation(Integer idSnap) throws EMFUserError;
}
