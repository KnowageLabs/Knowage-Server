package it.eng.spagobi.analiticalmodel.document.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.dao.ISnapshotDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.services.scheduler.service.ISchedulerServiceSupplier;
import it.eng.spagobi.services.scheduler.service.SchedulerServiceSupplierFactory;
import it.eng.spagobi.tools.scheduler.SchedulerConstants;
import it.eng.spagobi.tools.scheduler.to.JobInfo;
import it.eng.spagobi.tools.scheduler.utils.SchedulerUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/2.0/snapshotsContent")
public class GetSnapshotContentResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = Logger.getLogger(GetSnapshotContentResource.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getSnapshotContent(@Context HttpServletRequest req) {
		LOGGER.debug("IN");

		String schedulationName = req.getParameter("schedulationName");
		String objectId = req.getParameter(ObjectsTreeConstants.OBJECT_ID);
		String snapshotId = req.getParameter(SpagoBIConstants.SNAPSHOT_ID);
		String[] mergeItems = req.getParameterValues("mergeitems");

		try {
			Map<String, Object> contentMap;

			if (schedulationName != null) {
				List<Snapshot> snaps = DAOFactory.getSnapshotDAO().getLastSnapshotsBySchedulation(schedulationName, false);
				contentMap = mergeListSnap(snaps);
			} else if (mergeItems != null && mergeItems.length > 0) {
				contentMap = merge(Arrays.asList(mergeItems));
			} else if (objectId != null && snapshotId != null) {
				contentMap = getSnapshotForOneDocument(objectId, snapshotId);
			} else {
				return Response.status(Response.Status.BAD_REQUEST).entity("Missing required parameters").build();
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

			return Response.ok(content).type(contentType).header("Content-Disposition", "attachment; filename=\"export" + exportType + "\"").build();

		} catch (Exception e) {
			LOGGER.error("Error processing snapshot request", e);
			throw new SpagoBIRuntimeException("Error processing snapshot request", e);
		}
	}

	private Map<String, Object> getSnapshotForOneDocument(String objectIdStr, String idSnapStr) throws Exception {
		Integer objectId = Integer.valueOf(objectIdStr);
		Integer idSnap = Integer.valueOf(idSnapStr);
		LOGGER.debug("Required snapshot with id = " + idSnap + " of document with id = " + objectId);
		IEngUserProfile profile = getUserProfile();
		BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectById(objectId);
		// check if the user is able to see the document
		// TODO check if the user is able to execute the document (even if it does no make sense to be able to see the document but not to execute it...)
		byte[] content = null;
		String contentType = "text/html";
		if (ObjectsAccessVerifier.canSee(obj, profile)) {
			LOGGER.debug("Current user [" + ((UserProfile) profile).getUserId().toString() + "] can see snapshot with id = " + idSnap
					+ " of document with id = " + objectId);
			ISnapshotDAO snapdao = DAOFactory.getSnapshotDAO();
			Snapshot snap = snapdao.loadSnapshot(idSnap);
			content = snap.getContent();
			if (snap.getContentType() != null) {
				contentType = snap.getContentType();
			}
		} else {
			LOGGER.error("Current user [" + ((UserProfile) profile).getUserId().toString() + "] CANNOT see snapshot with id = " + idSnap
					+ " of document with id = " + objectId);
			content = "You cannot see required snapshot.".getBytes(UTF_8);
		}

		Map<String, Object> toReturn = new HashMap<>();
		toReturn.put("content", content);
		toReturn.put("contentType", contentType);
		return toReturn;
	}

	private Map<String, Object> merge(List<String> snapshotIds) {
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
			String jobDetail = schedulerService.getJobDefinition(jobName, SchedulerConstants.JOB_GROUP);
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
