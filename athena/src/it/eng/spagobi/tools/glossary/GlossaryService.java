package it.eng.spagobi.tools.glossary;
import static it.eng.spagobi.tools.glossary.util.Util.*;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.glossary.dao.IGlossaryDAO;
import it.eng.spagobi.tools.glossary.metadata.SbiGlBnessCls;
import it.eng.spagobi.tools.glossary.metadata.SbiGlContents;
import it.eng.spagobi.tools.glossary.metadata.SbiGlDataSetWlist;
import it.eng.spagobi.tools.glossary.metadata.SbiGlDataSetWlistId;
import it.eng.spagobi.tools.glossary.metadata.SbiGlDocWlist;
import it.eng.spagobi.tools.glossary.metadata.SbiGlDocWlistId;
import it.eng.spagobi.tools.glossary.metadata.SbiGlGlossary;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWord;
import it.eng.spagobi.tools.glossary.metadata.SbiGlTable;
import it.eng.spagobi.tools.udp.bo.Udp;
import it.eng.spagobi.tools.udp.dao.IUdpDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.util.JSON;
/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */ 
@Path("/1.0/glossary")
public class GlossaryService {
final String DirectDSWordChild=".SELF";
	
	@POST
	@Path("/addDocWlist")
	@Produces(MediaType.APPLICATION_JSON)
	public String addDocWlist(@Context HttpServletRequest req) {
		JSONObject jo = new JSONObject();
		try {
			System.out.println("addDocWlist");
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession()
					.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);

			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
			Integer wordId = getNumberOrNull(requestVal.opt("WORD_ID"));
			Integer documentId = getNumberOrNull(requestVal.opt("DOCUMENT_ID"));
			
			
			SbiGlDocWlist docwl=new SbiGlDocWlist();
			SbiGlDocWlistId dwlid=new SbiGlDocWlistId(wordId, documentId);
			SbiGlDocWlist wo=dao.getDocWlistOrNull(dwlid);
			
			if(wo!=null){
				jo.put("Status", "NON OK");
				jo.put("Message", "sbi.glossary.word.new.name.duplicate");
				return jo.toString();
			}
			
			docwl.setId(dwlid);
			dao.insertDocWlist(docwl);
			
			

		} catch (Throwable t) {
			 throw new SpagoBIServiceException(req.getPathInfo(),
			 "An unexpected error occured while executing service", t);
		}
		return jo.toString();
	}
	
	@POST
	@Path("/addDataSetWlist")
	@Produces(MediaType.APPLICATION_JSON)
	public String addDataSetWlist(@Context HttpServletRequest req) {
		JSONObject jo = new JSONObject();
		try {
			System.out.println("addDataSetWlist");
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession()
					.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);

			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
			Integer wordId = getNumberOrNull(requestVal.opt("WORD_ID"));
			Integer datasetId = getNumberOrNull(requestVal.opt("DATASET_ID"));
			String column=requestVal.opt("COLUMN_NAME").toString();
			String organization=requestVal.opt("ORGANIZATION").toString();
			
			SbiGlDataSetWlist dswl=new SbiGlDataSetWlist();
			SbiGlDataSetWlistId dslid=new SbiGlDataSetWlistId(wordId, datasetId,organization,column);
			SbiGlDataSetWlist wo=dao.getDataSetWlistOrNull(dslid);
			
			if(wo!=null){
				jo.put("Status", "NON OK");
				jo.put("Message", "sbi.glossary.word.new.name.duplicate");
				return jo.toString();
			}
			
			dswl.setId(dslid);
			dao.insertDataSetWlist(dswl);
			
			

		} catch (Throwable t) {
			 throw new SpagoBIServiceException(req.getPathInfo(),
			 "An unexpected error occured while executing service", t);
		}
		return jo.toString();
	}
	
	@POST
	@Path("/deleteDocWlist")
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteDocWlist(@Context HttpServletRequest req) {
		try {
			System.out.println("deleteDocWlist");
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession()
					.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);
			Integer wordId = getNumberOrNull(req.getParameter("WORD_ID"));
			Integer documentId = getNumberOrNull(req.getParameter("DOCUMENT_ID"));
			
			
			SbiGlDocWlistId id=new SbiGlDocWlistId(wordId,  documentId);
			
			dao.deleteDocWlist(id);

			JSONObject jo = new JSONObject();
			jo.put("Status", "OK");
			return jo.toString();

		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service", t);
		}
	}
	
	@POST
	@Path("/deleteDatasetWlist")
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteDatasetWlist(@Context HttpServletRequest req) {
		try {
			System.out.println("deleteDatasetWlist");
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession()
					.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);
			Integer wordId = getNumberOrNull(req.getParameter("WORD_ID"));
			Integer datasetId = getNumberOrNull(req.getParameter("DATASET_ID"));
			String column=req.getParameter("COLUMN").toString();
			String organiz=req.getParameter("ORGANIZATION").toString();
			
			SbiGlDataSetWlistId id=new SbiGlDataSetWlistId(wordId,  datasetId,organiz,column);
			
			dao.deleteDataSetWlist(id);

			JSONObject jo = new JSONObject();
			jo.put("Status", "OK");
			return jo.toString();

		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service", t);
		}
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("/loadNavigationItem")
	@Produces(MediaType.APPLICATION_JSON)
	public String loadNavigationItem(@Context HttpServletRequest req) {
		try {
			System.out.println("loadNavigationItem");
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession()
					.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);
			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
			JSONObject jo = new JSONObject();
			Map<String, Object> fin=dao.NavigationItem(requestVal);

			if (fin.containsKey("document")) {
				List<SbiObjects> doc = (List<SbiObjects>) fin.get("document");
				JSONArray ja = new JSONArray();
				for (SbiObjects tmp : doc) {
					ja.put(fromDocumentLight(tmp));
				}
				jo.put("document", ja);
				jo.put("document_size",(Integer) fin.get("document_size"));
			}
			
			if (fin.containsKey("word")) {
				List<SbiGlWord> doc = (List<SbiGlWord>) fin.get("word");
				JSONArray ja = new JSONArray();
				for (SbiGlWord tmp : doc) {
					ja.put(fromWordLight(tmp));
				}
				jo.put("word", ja);
				jo.put("word_size",(Integer) fin.get("word_size"));
			}
			
			if (fin.containsKey("dataset")) {
				List<SbiDataSet> doc = (List<SbiDataSet>) fin.get("dataset");
				JSONArray ja = new JSONArray();
				for (SbiDataSet tmp : doc) {
					ja.put(fromDataSetLight(tmp));
				}
				jo.put("dataset", ja);
				jo.put("dataset_size",(Integer) fin.get("dataset_size"));
			}
			
			if (fin.containsKey("bness_cls")) {
				List<SbiGlBnessCls> doc = (List<SbiGlBnessCls>) fin.get("bness_cls");
				JSONArray ja = new JSONArray();
				for (SbiGlBnessCls tmp : doc) {
					ja.put(fromBnessClsLight(tmp));
				}
				jo.put("bness_cls", ja);
				jo.put("bness_cls_size",(Integer) fin.get("bness_cls_size"));
			}
			
			if (fin.containsKey("table")) {
				List<SbiGlTable> doc = (List<SbiGlTable>) fin.get("table");
				JSONArray ja = new JSONArray();
				for (SbiGlTable tmp : doc) {
					ja.put(fromTableLight(tmp));
				}
				jo.put("table", ja);
				jo.put("table_size",(Integer) fin.get("table_size"));
			}
			
			jo.put("Status", "OK");
			return jo.toString();

		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service", t);
		}
	}
	
	@GET
	@Path("/GlossaryUdpLikeLabel")
	@Produces(MediaType.APPLICATION_JSON)
	public String loadUDPGlossaryLikeLabel(@Context HttpServletRequest req) {
		try {
			IUdpDAO dao = DAOFactory.getUdpDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession()
					.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);
			String lab = req.getParameter("LABEL");
			if(lab.trim().isEmpty()){
				throw new SpagoBIServiceException(req.getPathInfo(),
						"An unexpected error occured while executing service. Empty search label");
			}
			List<Udp> lst=null;
			if (lab != null && !lab.trim().isEmpty()) {
				 lst =dao.loadByFamilyAndLikeLabel("Glossary",lab);
				} else {
				lst = dao.loadAllByFamily("Glossary");
			}
			if(lst==null){
				throw new SpagoBIServiceException(req.getPathInfo(),
						"An unexpected error occured while executing service. Null list");
			}
			
			JSONArray jarr = new JSONArray();
			for (Udp o : lst){
				jarr.put(fromUdpLight(o));
			}
			return jarr.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service", t);
		}
	}
	
	@GET
	@Path("/listContents")
	@Produces(MediaType.APPLICATION_JSON)
	public String listContents(@Context HttpServletRequest req) {
		try {
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession()
					.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);
			Integer glossaryId = getNumberOrNull(req
					.getParameter("GLOSSARY_ID"));
			Integer parentId = getNumberOrNull(req.getParameter("PARENT_ID"));

			List<SbiGlContents> lst = dao.listContentsByGlossaryIdAndParentId(
					glossaryId, parentId);

			JSONArray jarr = new JSONArray();
			if (lst != null) {
				for (Iterator<SbiGlContents> iterator = lst.iterator(); iterator
						.hasNext();) {
					SbiGlContents sbiGlContents = iterator.next(); 
					Integer wordCount=dao.CountWlistByContent(sbiGlContents.getContentId());
					Integer ContentCount=dao.CountContentChild(sbiGlContents.getContentId());
					jarr.put(fromContentsLight(sbiGlContents,(wordCount>0), (ContentCount>0)));
				}
			}

			if (parentId != null) {
				List<SbiGlWord> lstw = dao.listWlistWord(parentId);
				if (lstw != null) {
					for (SbiGlWord wl : lstw) {
						jarr.put(fromWordLight(wl));
					}
				}

			}

			return jarr.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service", t);
		}
	}

	@GET
	@Path("/listGlossary")
	@Produces(MediaType.APPLICATION_JSON)
	public String listGlossary(@Context HttpServletRequest req) {
		try {
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession()
					.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);
			List<SbiGlGlossary> lst = dao.listGlossary();
			JSONArray jarr = new JSONArray();
			if (lst != null) {
				for (Iterator<SbiGlGlossary> iterator = lst.iterator(); iterator
						.hasNext();) {
					SbiGlGlossary sbiGlGlossary = iterator.next();
					jarr.put(fromGlossaryLight(sbiGlGlossary));
				}
			}
			return jarr.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service", t);
		}
	}

	@GET
	@Path("/listDocument")
	@Produces(MediaType.APPLICATION_JSON)
	public String listDocument(@Context HttpServletRequest req) {
		try {
			IBIObjectDAO sbiObjectsDAO = DAOFactory.getBIObjectDAO();
			
			IEngUserProfile profile = (IEngUserProfile) req.getSession()
					.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			sbiObjectsDAO.setUserProfile(profile);
			String word = req.getParameter("WORD");
			Integer page = getNumberOrNull(req.getParameter("Page"));
			Integer itempp = getNumberOrNull(req.getParameter("ItemPerPage"));
			
			
			List<BIObject> lst=null;
			if ((word != null && !word.trim().isEmpty())) {
				lst= sbiObjectsDAO.searchBIObjects(word, "CONTAINS", "label", null, null, profile);
				
			}else{
				lst= sbiObjectsDAO.loadAllBIObjects();
			}
			JSONArray jarr = new JSONArray();
			if (lst != null) {
				for (Iterator<BIObject> iterator = lst.iterator(); iterator
						.hasNext();) {
					BIObject sbiob = iterator.next();
					jarr.put(fromDocumentLight(sbiob));
				}
			}
			if(page!=null && itempp!=null ){
				JSONObject fin=new JSONObject();
				fin.put("item", jarr);
//				fin.put("itemCount", dao.wordCount(word,gloss_id));
				fin.put("itemCount", 5);
				
				return fin.toString();
			}
			
			return jarr.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service", t);
		}
	}

	@GET
	@Path("/listDataSet")
	@Produces(MediaType.APPLICATION_JSON)
	public String listDataSet(@Context HttpServletRequest req) {
		try {
			IDataSetDAO DAO = DAOFactory.getDataSetDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession()
					.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			DAO.setUserProfile(profile);
			
			String word = req.getParameter("WORD");
			Integer page = getNumberOrNull(req.getParameter("Page"));
			Integer itempp = getNumberOrNull(req.getParameter("ItemPerPage"));
			List<IDataSet> lst=null;
			if ((word != null && !word.trim().isEmpty())) {
//				lst = DAO.loadFilteredDatasetList(" from SbiDataSet h where h.label like '%"+word+"%'", page, itempp);
				lst = DAO.loadDataSets();
				} else {
				lst = DAO.loadDataSets();
			}

			JSONArray jarr = new JSONArray();
			if (lst != null) {
				for (Iterator<IDataSet> iterator = lst.iterator(); iterator
						.hasNext();) {
					IDataSet ds = iterator.next();
					jarr.put(fromDataSetLight(ds));
				}
			}
			
			if(page!=null && itempp!=null ){
				JSONObject fin=new JSONObject();
				fin.put("item", jarr);
//				fin.put("itemCount", dao.wordCount(word,gloss_id));
				fin.put("itemCount", 1);
				
				return fin.toString();
			}
			
			return jarr.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service", t);
		}
	}
		
	@GET
	@Path("/listWords")
	@Produces(MediaType.APPLICATION_JSON)
	public String listWords(@Context HttpServletRequest req) {
		try {
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);
			String word = req.getParameter("WORD");
			Integer page = getNumberOrNull(req.getParameter("Page"));
			Integer itempp = getNumberOrNull(req.getParameter("ItemPerPage"));
			Integer gloss_id = getNumberOrNull(req.getParameter("GLOSSARY_ID"));
			
			List<SbiGlWord> lst = null;
			if ((word != null && !word.trim().isEmpty()) || gloss_id!=null) {
				lst = dao.listWordFiltered(word,page,itempp,gloss_id);
			} else {
				lst = dao.listWord(page,itempp);
			}
			JSONArray jarr = new JSONArray();
			if (lst != null) {
				for (Iterator<SbiGlWord> iterator = lst.iterator(); iterator.hasNext();) {
					SbiGlWord sbiGlWord = iterator.next();
					jarr.put(fromWordLight(sbiGlWord));
				}
			}
			
			if(page!=null && itempp!=null ){
				JSONObject fin=new JSONObject();
				fin.put("item", jarr);
				fin.put("itemCount", dao.wordCount(word,gloss_id));
				return fin.toString();
			}
			
			return jarr.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service", t);
		}
	}

	@GET
	@Path("/getDocumentInfo")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDocumentInfo(@Context HttpServletRequest req) {
		
		try {
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession()
					.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);
			
			Integer documentId = getNumberOrNull(req.getParameter("DOCUMENT_ID"));
			if (documentId==null) {
				throw new SpagoBIServiceException(req.getPathInfo(),
						"Input param Id [" + documentId + "] not valid or null");
			}
			
			List<SbiGlDocWlist> listDocWlist=dao.listDocWlist(documentId);
			
			JSONObject jobj =new JSONObject();
			JSONArray ja=new JSONArray();
			for(SbiGlDocWlist sb:listDocWlist){
				ja.put(fromWordLight(sb.getWord()));
			}
			jobj.put("word", ja);
			
			
			return jobj.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service", t);
		}
	}
	
	@GET
	@Path("/getDataSetInfo")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDataSetInfo(@Context HttpServletRequest req) {
		
		try {
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession()
					.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);
			
			Integer datasetId = getNumberOrNull(req.getParameter("DATASET_ID"));
			if (datasetId==null) {
				throw new SpagoBIServiceException(req.getPathInfo(),
						"Input param Id [" + datasetId + "] not valid or null");
			}
			
			String organiz = req.getParameter("ORGANIZATION").toString();
			
			List<SbiGlDataSetWlist> listDatasetWlist=dao.listDataSetWlist(datasetId,organiz);
			JSONObject jo=new JSONObject();
			JSONArray ja=new JSONArray();
			JSONArray dsWord=new JSONArray();
			Map<String, Integer> map = new HashMap<String,Integer>();
			
			SbiDataSet datas=DAOFactory.getSbiDataSetDAO().loadSbiDataSetByIdAndOrganiz(datasetId, organiz);
			MetaData m=datas.getMetadata();
			if(m!=null){
				for(int i=0;i<m.getFieldCount();i++){
					if(m.getFieldAlias(i).compareTo(DirectDSWordChild)!=0){
						//if is not direct child of dataset
						
					map.put(m.getFieldAlias(i), ja.length());
					map.put(m.getFieldAlias(i), ja.length());
					JSONObject tmp =new JSONObject();
					tmp.put("alias", m.getFieldAlias(i));
					tmp.put("organization", datas.getId().getOrganization());
					tmp.put("datasetId", datas.getId().getDsId());
					tmp.put("word",new JSONArray());
					ja.put(tmp);
					}
				}
				
		}
			
			if(!listDatasetWlist.isEmpty()){
				for(SbiGlDataSetWlist sb:listDatasetWlist){
					if(sb.getId().getColumn_name().compareTo(DirectDSWordChild)!=0){
						ja.getJSONObject(map.get(sb.getId().getColumn_name())).getJSONArray("word").put(fromWordLight(sb.getWord()));
					}else{
						dsWord.put(fromWordLight(sb.getWord()));
					}						
					}
			}
			
			
			jo.put("SbiGlDataSetWlist", ja);
			jo.put("DataSet", JSON.parse(JsonConverter.objectToJson(datas, SbiDataSet.class)));
			jo.put("Word", dsWord);
			return jo.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service", t);
		}
	}
	
	@GET
	@Path("/getWord")
	@Produces(MediaType.APPLICATION_JSON)
	public String getWord(@Context HttpServletRequest req) {
		String id = req.getParameter("WORD_ID");
		if (!id.matches("\\d+")) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"Input param Id [" + id + "] not valid");
		}
		try {
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession()
					.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);
			SbiGlWord word = dao.loadWord(Integer.valueOf(id));
			JSONObject jobj = fromWord(word);
			return jobj.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service", t);
		}
	}
	
	@GET
	@Path("/getBnessCls")
	@Produces(MediaType.APPLICATION_JSON)
	public String getBnessCls(@Context HttpServletRequest req) {
		String id = req.getParameter("BC_ID");
		if (!id.matches("\\d+")) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"Input param Id [" + id + "] not valid");
		}
		try {
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession()
					.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);
			SbiGlBnessCls BnessCls = dao.loadBnessCls(Integer.valueOf(id));
			
			return JsonConverter.objectToJson(BnessCls, SbiGlBnessCls.class);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service", t);
		}
	}

	@GET
	@Path("/getTable")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTable(@Context HttpServletRequest req) {
		String id = req.getParameter("TABLE_ID");
		if (!id.matches("\\d+")) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"Input param Id [" + id + "] not valid");
		}
		try {
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession()
					.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);
			SbiGlTable Table = dao.loadTable(Integer.valueOf(id));
			
			return JsonConverter.objectToJson(Table, SbiGlTable.class);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service", t);
		}
	}

	@GET
	@Path("/getGlossary")
	@Produces(MediaType.APPLICATION_JSON)
	public String getGlossary(@Context HttpServletRequest req) {
		String glossaryId = req.getParameter("GLOSSARY_ID");
		if (!glossaryId.matches("\\d+")) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"Input param Id [" + glossaryId + "] not valid");
		}
		try {
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession()
					.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);
//			Collection roles = profile.getRoles();
//			String role = (String)((ArrayList)roles).get(0);
//			Boolean role2 = (Boolean)((ArrayList)roles).contains("/spagobi/admin");
//			profile.
//			   profile..isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_STATE_MANAGEMENT);

			SbiGlGlossary glo = dao.loadGlossary(Integer.valueOf(glossaryId));
			JSONObject jobj = fromGlossary(glo);
			return jobj.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service", t);
		}
	}

	@GET
	@Path("/getContent")
	@Produces(MediaType.APPLICATION_JSON)
	public String getContents(@Context HttpServletRequest req) {
		String contentId = req.getParameter("CONTENT_ID");
		if (!contentId.matches("\\d+")) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"Input param Id [" + contentId + "] not valid");
		}
		try {
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession()
					.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);

			SbiGlContents cont = dao.loadContents(Integer.valueOf(contentId));
			JSONObject jobj = fromContent(cont);
			return jobj.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service", t);
		}
	}

	@GET
	@Path("/glosstreeLike")
	@Produces(MediaType.APPLICATION_JSON)
	public String glosstreeLike(@Context HttpServletRequest req) {

		try {
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession()
					.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);
			JSONObject jo = new JSONObject();
			
			String word = req.getParameter("WORD");
			String glossaryId = req.getParameter("GLOSSARY_ID");
			JSONObject fin=new JSONObject();
			
			if(word.trim().isEmpty()){
				SbiGlGlossary gl=dao.loadGlossary(Integer.parseInt(glossaryId));
				fin=fromGlossaryLight(gl);
				fin.put("SBI_GL_CONTENTS", JSON.parse(this.listContents(req)));
				}else{
					fin=dao.glosstreeLike(glossaryId,word);
				}
					
			jo.put("Status", "OK");
			jo.put("GlossSearch", fin);
			return jo.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service", t);
		}
	}
	
}