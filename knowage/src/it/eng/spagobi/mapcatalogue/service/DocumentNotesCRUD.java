package it.eng.spagobi.mapcatalogue.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.collections.map.HashedMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;
import org.xml.sax.InputSource;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjNote;
import it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.rest.RestUtilities;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import sun.misc.BASE64Encoder;

@Path("/documentnotes")
@ManageAuthorization
public class DocumentNotesCRUD {

	public static final String SERVICE_NAME = "PRINT_NOTES_ACTION";

	public static final String SBI_OUTPUT_TYPE = "SBI_OUTPUT_TYPE";

	private static final String TEMPLATE_NAME = "notesPrintedTemplate.jrxml";
	private static final String TEMPLATE_PATH = "it/eng/spagobi/analiticalmodel/document/resources/";
	InputSource inputSource;

	@SuppressWarnings("unchecked")
	@POST
	@Path("/getListNotes")
	public String getListNotes(@Context HttpServletRequest req) {
		JSONObject requestVal;
		UserProfile profile = (UserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		try {
			requestVal = RestUtilities.readBodyAsJSONObject(req);
			int idObj = requestVal.getInt("id");
			BIObject doc = DAOFactory.getBIObjectDAO().loadBIObjectById(idObj);
			String execId = getExecutionIdentifier(doc);
			IObjNoteDAO dao = DAOFactory.getObjNoteDAO();
			// List<ObjNote> notes = dao.getVisibleNotes(idObj, profile.getUserId().toString());
			List<ObjNote> notes = DAOFactory.getObjNoteDAO().getListExecutionNotes(idObj, execId);

			JSONArray notesObject = new JSONArray();
			for (int i = 0; i < notes.size(); i++) {
				ObjNote nota = notes.get(i);
				JSONObject obj = new JSONObject();

				obj.put("content", new String(nota.getContent()));
				obj.put("id", nota.getId());
				obj.put("lastChangeDate", nota.getLastChangeDate());
				obj.put("creationDate", nota.getCreationDate());
				obj.put("biobjId", nota.getBiobjId());
				obj.put("execReq", nota.getExecReq());
				obj.put("owner", nota.getOwner());
				obj.put("profileLogged", profile.getUserId().toString());
				notesObject.put(obj);
			}

			// return Response.ok(notesObject).build();
			return notesObject.toString();
			// return notes;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// return Response.serverError().build();
		return null;
	}

	@POST
	@Path("/getNote")
	public String getNote(@Context HttpServletRequest req) {
		// calculateBIObjectRating
		JSONObject requestVal;
		UserProfile profile = (UserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		try {
			requestVal = RestUtilities.readBodyAsJSONObject(req);
			int idObj = requestVal.getInt("id");

			IObjNoteDAO dao = DAOFactory.getObjNoteDAO();
			// ObjNote notes = dao.getNote(idObj, profile.getUserId().toString());
			BIObject doc = DAOFactory.getBIObjectDAO().loadBIObjectById(idObj);
			String execId = getExecutionIdentifier(doc);
			ObjNote notes = dao.getExecutionNotesByOwner(idObj, execId, profile.getUserId().toString());
			JSONObject object = new JSONObject();
			if (notes != null) {
				object.put("owner", notes.getOwner());
				object.put("creationDate", notes.getCreationDate());
				object.put("lastChangeDate", notes.getLastChangeDate());
				object.put("nota", notes.getNotes());
				object.put("profile", profile.getUserId().toString());
				return object.toString();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";

	}

	@POST
	@Path("/saveNote")
	public void saveNote(@Context HttpServletRequest req) {
		JSONObject requestVal;
		UserProfile profile = (UserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		try {
			requestVal = RestUtilities.readBodyAsJSONObject(req);
			int idObj = requestVal.getInt("idObj");
			String nota = requestVal.getString("nota");
			String type = requestVal.getString("type");

			String owner = (String) profile.getUserId();
			BIObject doc = DAOFactory.getBIObjectDAO().loadBIObjectById(idObj);
			String execString = getExecutionIdentifier(doc);
			ObjNote objnote = DAOFactory.getObjNoteDAO().getExecutionNotesByOwner(idObj, execString, owner);
			String currentNotes = "";
			ObjNote saveNote = new ObjNote();
			boolean flag = false;
			if (objnote != null) {
				/*
				 * byte[] content = objnote.getContent(); currentNotes = new String(content);
				 */
				flag = true;
				// saveNote.setBinId(objnote.getBinId());
				// saveNote.setBiobjId(objnote.getBiobjId());
				// saveNote.setId(objnote.getId());
				objnote.setContent(nota.getBytes());
				objnote.setIsPublic((type.equalsIgnoreCase("PUBLIC") ? true : false));

			}
			// I decide to calculate the field EXEC_REQ as the dijest of the concatenation of biobj_id+

			saveNote.setExecReq(execString);
			saveNote.setContent(nota.getBytes());
			saveNote.setIsPublic((type.equalsIgnoreCase("PUBLIC") ? true : false));
			saveNote.setOwner(owner);
			// DAOFactory.getObjNoteDAO().saveNote(idObj, saveNote, flag);

			if (flag) {
				DAOFactory.getObjNoteDAO().modifyExecutionNotes(objnote);
			} else {
				DAOFactory.getObjNoteDAO().saveExecutionNotes(idObj, saveNote);
			}

		} catch (IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@POST
	@Path("/deleteNote")
	public void deleteNote(@Context HttpServletRequest req) {
		JSONObject requestVal;

		try {
			requestVal = RestUtilities.readBodyAsJSONObject(req);
			int idObj = requestVal.getInt("id");
			String execReq = requestVal.getString("execReq");
			String owner = requestVal.getString("owner");

			DAOFactory.getObjNoteDAO().eraseNotesByOwner(idObj, execReq, owner);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EMFUserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@POST
	@Path("/getDownalNote")
	@Produces("application/octet-stream")
	public String getDownalNote(@Context HttpServletRequest req) {
		// calculateBIObjectRating
		JSONObject requestVal;
		List globalObjNoteList = null;
		int idObj;
		String owner;
		String outputType;
		BIObject doc;
		OutputStream out = null;

		UserProfile profile = (UserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		try {
			requestVal = RestUtilities.readBodyAsJSONObject(req);
			idObj = requestVal.getInt("idObj");
			owner = profile.getUserId().toString();
			outputType = requestVal.getString("type");

			doc = DAOFactory.getBIObjectDAO().loadBIObjectById(idObj);
			globalObjNoteList = DAOFactory.getObjNoteDAO().getListExecutionNotes(idObj, getExecutionIdentifier(doc));

			List objNoteList = new ArrayList();
			for (int i = 0, l = globalObjNoteList.size(); i < l; i++) {
				ObjNote objNote = (ObjNote) globalObjNoteList.get(i);
				if (objNote.getIsPublic()) {
					objNoteList.add(objNote);
				} else if (objNote.getOwner().equalsIgnoreCase(owner)) {
					objNoteList.add(objNote);
				}
			}

			String templateStr = getTemplateTemplate();
			JRBeanCollectionDataSource datasource = new JRBeanCollectionDataSource(objNoteList);

			HashedMap parameters = new HashedMap();
			parameters.put("PARAM_OUTPUT_FORMAT", outputType);
			parameters.put("TITLE", doc.getLabel());

			UUIDGenerator uuidGen = UUIDGenerator.getInstance();
			UUID uuid_local = uuidGen.generateTimeBasedUUID();
			String executionId = uuid_local.toString();
			executionId = executionId.replaceAll("-", "");
			// Creta etemp file
			String dirS = System.getProperty("java.io.tmpdir");
			File dir = new File(dirS);
			dir.mkdirs();
			String fileName = "notes" + executionId;

			File tmpFile = null;

			tmpFile = File.createTempFile(fileName, "." + outputType, dir);
			out = new FileOutputStream(tmpFile);
			StringBufferInputStream sbis = new StringBufferInputStream(templateStr);
			JasperReport report = JasperCompileManager.compileReport(sbis);
			// report.setProperty("", )
			JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, datasource);
			JRExporter exporter = null;
			if (outputType.equalsIgnoreCase("PDF")) {
				exporter = (JRExporter) Class.forName("net.sf.jasperreports.engine.export.JRPdfExporter").newInstance();
				if (exporter == null)
					exporter = new JRPdfExporter();
			} else {
				exporter = (JRExporter) Class.forName("net.sf.jasperreports.engine.export.JRRtfExporter").newInstance();
				if (exporter == null)
					exporter = new JRRtfExporter();
			}

			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
			exporter.exportReport();

			String mimeType;
			if (outputType.equalsIgnoreCase("RTF")) {
				mimeType = "application/rtf";
			} else {
				mimeType = "application/pdf";
			}

			BufferedReader reader = new BufferedReader(new FileReader(tmpFile));
			String line = null;
			StringBuilder stringBuilder = new StringBuilder();
			String ls = System.getProperty("line.separator");
			FileInputStream fileInputStream = null;
			byte[] bFile = new byte[(int) tmpFile.length()];
			fileInputStream = new FileInputStream(tmpFile);
			fileInputStream.read(bFile);
			fileInputStream.close();
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}

			JSONObject object = new JSONObject();

			object.put("file", bFile);

			return object.toString();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				out.flush();
				out.close();
			} catch (IOException e) {
			}
		}

		return null;

	}

	@POST
	@Path("/downloadnote")
	public Response downloadNote(@Context HttpServletRequest req, @Context HttpServletResponse resp) {
		JSONObject requestVal;
		List globalObjNoteList = null;
		int idObj;
		String owner;
		String outputType;
		BIObject doc;
		UserProfile profile = (UserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		try {
			requestVal = RestUtilities.readBodyAsJSONObject(req);
			idObj = requestVal.getInt("idObj");
			owner = profile.getUserId().toString();
			outputType = requestVal.getString("type");

			doc = DAOFactory.getBIObjectDAO().loadBIObjectById(idObj);
			globalObjNoteList = DAOFactory.getObjNoteDAO().getListExecutionNotes(idObj, getExecutionIdentifier(doc));
		} catch (EMFUserError e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		// mantains only the personal notes and others one only if they have PUBLIC status
		List objNoteList = new ArrayList();
		for (int i = 0, l = globalObjNoteList.size(); i < l; i++) {
			ObjNote objNote = (ObjNote) globalObjNoteList.get(i);
			if (objNote.getIsPublic()) {
				objNoteList.add(objNote);
			} else if (objNote.getOwner().equalsIgnoreCase(owner)) {
				objNoteList.add(objNote);
			}
		}

		String templateStr = getTemplateTemplate();

		// JREmptyDataSource conn=new JREmptyDataSource(1);
		// Connection conn = getConnection("SpagoBI",getHttpSession(),profile,obj.getId().toString());
		JRBeanCollectionDataSource datasource = new JRBeanCollectionDataSource(objNoteList);

		HashedMap parameters = new HashedMap();
		parameters.put("PARAM_OUTPUT_FORMAT", outputType);
		parameters.put("TITLE", doc.getLabel());

		UUIDGenerator uuidGen = UUIDGenerator.getInstance();
		UUID uuid_local = uuidGen.generateTimeBasedUUID();
		String executionId = uuid_local.toString();
		executionId = executionId.replaceAll("-", "");
		// Creta etemp file
		String dirS = System.getProperty("java.io.tmpdir");
		File dir = new File(dirS);
		dir.mkdirs();
		String fileName = "notes" + executionId;
		OutputStream out = null;
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile(fileName, "." + outputType, dir);
			out = new FileOutputStream(tmpFile);
			StringBufferInputStream sbis = new StringBufferInputStream(templateStr);
			JasperReport report = JasperCompileManager.compileReport(sbis);
			// report.setProperty("", )
			JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, datasource);
			JRExporter exporter = null;
			if (outputType.equalsIgnoreCase("PDF")) {
				exporter = (JRExporter) Class.forName("net.sf.jasperreports.engine.export.JRPdfExporter").newInstance();
				if (exporter == null)
					exporter = new JRPdfExporter();
			} else {
				exporter = (JRExporter) Class.forName("net.sf.jasperreports.engine.export.JRRtfExporter").newInstance();
				if (exporter == null)
					exporter = new JRRtfExporter();
			}

			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
			exporter.exportReport();

		} catch (Throwable e) {
			return null;
		} finally {
			try {
				out.flush();
				out.close();
			} catch (IOException e) {
			}
		}

		String mimeType;
		if (outputType.equalsIgnoreCase("RTF")) {
			mimeType = "application/rtf";
		} else {
			mimeType = "application/pdf";
		}

		HttpServletResponse response = resp;
		response.setContentType(mimeType);
		response.setHeader("Content-Disposition", "filename=\"report." + outputType + "\";");
		response.setContentLength((int) tmpFile.length());
		try {
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(tmpFile));
			JSONObject obj = new JSONObject();

			/*
			 * int b = -1; while ((b = in.read()) != -1) { response.getOutputStream().write(b); } response.getOutputStream().flush();
			 */
			in.close();
			obj.put("prova", "test");
			return Response.ok(obj.toString()).build();
		} catch (Exception e) {
		} finally {
			tmpFile.delete();
		}
		return null;
	}

	private String getTemplateTemplate() {
		StringBuffer buffer = new StringBuffer();
		try {

			// String rootPath=ConfigSingleton.getRootPath();
			// logger.debug("rootPath: "+rootPath!=null ? rootPath : "");
			String templateDirPath = TEMPLATE_PATH;
			// logger.debug("templateDirPath: "+templateDirPath!=null ? templateDirPath : "");
			templateDirPath += TEMPLATE_NAME;
			if (templateDirPath != null) {
				InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(templateDirPath);

				if (fis != null) {
				} else {
				}
				inputSource = new InputSource(fis);
				if (inputSource != null) {
				}
				BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
				if (reader != null) {
				}
				String line = null;
				try {
					while ((line = reader.readLine()) != null) {
						buffer.append(line + "\n");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return buffer.toString();
	}

	private String getExecutionIdentifier(BIObject biobj) {
		List biparvalues = null;
		BIObjectParameter biobjpar = null;
		String parUrlName = null;
		Iterator iterBiparValues = null;
		String identif = null;
		List biobjpars = null;
		Iterator iterBiobjPars = null;
		String parValueString = null;

		identif = "biobject=" + biobj.getLabel() + "&";
		biobjpars = biobj.getBiObjectParameters();
		iterBiobjPars = biobjpars.iterator();
		while (iterBiobjPars.hasNext()) {
			biobjpar = (BIObjectParameter) iterBiobjPars.next();
			Parameter par = biobjpar.getParameter();

			if ((par == null) || (!par.isFunctional())) {
				continue;
			}
			parUrlName = biobjpar.getParameterUrlName();
			biparvalues = biobjpar.getParameterValues();
			if (biparvalues == null)
				continue;
			iterBiparValues = biparvalues.iterator();
			parValueString = "";
			while (iterBiparValues.hasNext()) {
				String value = iterBiparValues.next().toString();
				parValueString = parValueString + value;
				if (iterBiparValues.hasNext()) {
					parValueString = parValueString + ",";
				}
			}
			identif = identif + parUrlName + "=" + parValueString;
			if (iterBiobjPars.hasNext()) {
				identif = identif + "&";
			}
		}
		BASE64Encoder encoder = new BASE64Encoder();

		String ecodedIdentif = "";
		int index = 0;
		while (index < identif.length()) {
			String tmpStr = "";
			try {
				tmpStr = identif.substring(index, index + 10);
			} catch (Exception e) {
				tmpStr = identif.substring(index, identif.length());
			}
			String tmpEncoded = encoder.encode(tmpStr.getBytes());
			ecodedIdentif = ecodedIdentif + tmpEncoded;
			index = index + 10;
		}

		return ecodedIdentif;
	}
}
