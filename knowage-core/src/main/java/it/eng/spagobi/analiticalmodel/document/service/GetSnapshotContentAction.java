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
package it.eng.spagobi.analiticalmodel.document.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.dao.ISnapshotDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.services.scheduler.service.ISchedulerServiceSupplier;
import it.eng.spagobi.services.scheduler.service.SchedulerServiceSupplierFactory;
import it.eng.spagobi.tools.scheduler.services.JobManagementModule;
import it.eng.spagobi.tools.scheduler.to.JobInfo;
import it.eng.spagobi.tools.scheduler.utils.SchedulerUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class GetSnapshotContentAction extends AbstractHttpAction {

	public static int SUCCESS = 200;

	private static final Logger LOGGER = Logger.getLogger(GetSnapshotContentAction.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.dispatching.service.ServiceIFace#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	@Override
	public void service(SourceBean request, SourceBean response) throws Exception {
		LOGGER.debug("IN");
		freezeHttpResponse();
		HttpServletResponse httpResp = getHttpResponse();
		Map<String, Object> contentMap;

		String schedulationName = (String) request.getAttribute("schedulationName");
		if (schedulationName != null) {
			boolean collate = false;
			List<Snapshot> snaps = DAOFactory.getSnapshotDAO().getLastSnapshotsBySchedulation(schedulationName,
					collate);
			contentMap = mergeListSnap(snaps);
		} else {
			List<String> objectIdStr = request.getAttributeAsList("mergeitems");
			if (objectIdStr == null || objectIdStr.isEmpty()) {
				contentMap = getSnapshotForOneDocument(request);
			} else {
				contentMap = merge(objectIdStr);
			}

		}

		byte[] content = (byte[]) contentMap.get("content");
		String contentType = (String) contentMap.get("contentType");
		String exportType = "";

		if (contentType.contains("excel")) {
			exportType = ".xls";
		} else if (contentType.contains("openxmlformats-officedocument")) {
			exportType = ".xlsx";
		} else if (contentType.contains("pdf")) {
			exportType = ".pdf";
		}

		LOGGER.debug("Type of export" + exportType);
		LOGGER.debug("Content-Disposition " + "filename=\"export" + exportType + "\";");
		httpResp.setHeader("Content-Disposition", "filename=\"export" + exportType + "\";");
		httpResp.setContentType(contentType);
		httpResp.setContentLength(content.length);
		httpResp.getOutputStream().write(content);
		httpResp.setStatus(SUCCESS);

		httpResp.getOutputStream().flush();
		LOGGER.debug("OUT");
	}

	public Map<String, Object> getSnapshotForOneDocument(SourceBean request) throws Exception {
		String objectIdStr = (String) request.getAttribute(ObjectsTreeConstants.OBJECT_ID);

		Integer objectId = new Integer(objectIdStr);
		String idSnapStr = (String) request.getAttribute(SpagoBIConstants.SNAPSHOT_ID);
		Integer idSnap = new Integer(idSnapStr);
		LOGGER.debug("Required snapshot with id = " + idSnap + " of document with id = " + objectId);
		IEngUserProfile profile = (IEngUserProfile) this.getRequestContainer().getSessionContainer()
				.getPermanentContainer().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectById(objectId);
		// check if the user is able to see the document
		// TODO check if the user is able to execute the document (even if it does no make sense to be able to see the document but not to execute it...)
		byte[] content = null;
		String contentType = "text/html";
		if (ObjectsAccessVerifier.canSee(obj, profile)) {
			LOGGER.debug("Current user [" + ((UserProfile) profile).getUserId().toString()
					+ "] can see snapshot with id = " + idSnap + " of document with id = " + objectId);
			ISnapshotDAO snapdao = DAOFactory.getSnapshotDAO();
			Snapshot snap = snapdao.loadSnapshot(idSnap);
			content = snap.getContent();
			if (snap.getContentType() != null) {
				contentType = snap.getContentType();
			}
		} else {
			LOGGER.error("Current user [" + ((UserProfile) profile).getUserId().toString()
					+ "] CANNOT see snapshot with id = " + idSnap + " of document with id = " + objectId);
			content = "You cannot see required snapshot.".getBytes(UTF_8);
		}

		Map<String, Object> toReturn = new HashMap<>();
		toReturn.put("content", content);
		toReturn.put("contentType", contentType);
		return toReturn;
	}

	public Map<String, Object> merge(List<String> snapshotIds) {
		LOGGER.debug("IN");
		ISnapshotDAO snapDao = null;
		List<Snapshot> snapList = new ArrayList<>();
		try {
			// load the snapshots
			snapDao = DAOFactory.getSnapshotDAO();
			for (int i = 0; i < snapshotIds.size(); i++) {
				Integer id = new Integer(snapshotIds.get(i));
				snapList.add(snapDao.loadSnapshot(id));

			}
			return mergeListSnap(snapList);
		} catch (Exception e) {
			LOGGER.error(" Error while crating input stream for the content of a snapshot", e);
			throw new SpagoBIRuntimeException(" Error while crating input stream for the content of a snapshot", e);
		}
	}

	private Map<String, Object> mergeListSnap(List<Snapshot> snapList) {
		LOGGER.debug("IN");

		try {

			ISnapshotDAO snapDao = DAOFactory.getSnapshotDAO();
			String jobName = snapDao.loadSnapshotSchedulation(snapList.get(0).getId());
			PDFMergerUtility mergePdf = new PDFMergerUtility();
			List<Snapshot> sortedSnapList = new ArrayList<>();

			// sort snapshot of documents respecting the order of document in the schedulation
			ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
			String jobDetail = schedulerService.getJobDefinition(jobName, JobManagementModule.JOB_GROUP);
			SourceBean jobDetailSB = SchedulerUtilities.getSBFromWebServiceResponse(jobDetail);
			JobInfo jobInfo = SchedulerUtilities.getJobInfoFromJobSourceBean(jobDetailSB);

			if (!jobInfo.isJobCollateSnapshots()) {
				int min = -1;
				Snapshot minSnap = null;
				int snapPos = -1;
				for (int i = 0; i < snapList.size(); i++) {
					min = -1;
					minSnap = null;
					snapPos = -1;
					for (int j = 0; j < snapList.size(); j++) {
						Snapshot thisSnao = snapList.get(j);
						if (thisSnao != null) {
							Integer thisSeq = thisSnao.getSequence();

							if (min < 0 || min > thisSeq) {
								min = thisSeq;
								minSnap = thisSnao;
								snapPos = j;
							}
						}
					}

					sortedSnapList.add(minSnap);
					snapList.set(snapPos, null);
				}
			} else {
				sortedSnapList = snapList;
			}
			for (int i = 0; i < sortedSnapList.size(); i++) {
				Snapshot snap = sortedSnapList.get(i);
				InputStream is = new ByteArrayInputStream(snap.getContent());
				mergePdf.addSource(is);
			}
			// download merged file
			ByteArrayOutputStream pdfDownload = new ByteArrayOutputStream();
			mergePdf.setDestinationStream(pdfDownload);
			mergePdf.mergeDocuments(null);

			Map<String, Object> toReturn = new HashMap<>();
			toReturn.put("content", pdfDownload.toByteArray());
			toReturn.put("contentType", "application/pdf");

			return toReturn;

		} catch (EMFUserError e) {
			LOGGER.error("Error with getting snapshpots", e);
			throw new SpagoBIRuntimeException("Error with getting snapshpots", e);
		} catch (IOException e) {
			LOGGER.error("I/O Error with getting snapshpot ids from request", e);
			throw new SpagoBIRuntimeException("I/O Error with getting snapshpot ids from request", e);
		} catch (EMFInternalError e) {
			LOGGER.error(" Error while crating input stream for the content of a snapshot", e);
			throw new SpagoBIRuntimeException(" Error while crating input stream for the content of a snapshot", e);
		}
	}

}
