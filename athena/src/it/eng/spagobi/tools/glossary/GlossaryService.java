package it.eng.spagobi.tools.glossary;

import static it.eng.spagobi.tools.glossary.util.Util.fromContentsLight;
import static it.eng.spagobi.tools.glossary.util.Util.getNumberOrNull;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.glossary.dao.IGlossaryDAO;
import it.eng.spagobi.tools.glossary.metadata.SbiGlAttribute;
import it.eng.spagobi.tools.glossary.metadata.SbiGlContents;
import it.eng.spagobi.tools.glossary.metadata.SbiGlGlossary;
import it.eng.spagobi.tools.glossary.metadata.SbiGlReferences;
import it.eng.spagobi.tools.glossary.metadata.SbiGlReferencesId;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWord;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWordAttr;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWordAttrId;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JacksonWrapper;

@Path("/1.0/glossary")
public class GlossaryService {

	@POST
	@Path("/ModifyContentsGlossary")
	@Produces(MediaType.APPLICATION_JSON)
	public String ModifyContentsGlossary(@Context HttpServletRequest req) {
		try {
			System.out.println("ModifyContentsGlossary");
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);
			System.out.println(req.toString());
			JSONObject requestVal= RestUtilities.readBodyAsJSONObject(req);
			
			
			
			Integer contentId = (Integer) requestVal.opt("CONTENT_ID");
			Integer parentId = (Integer) requestVal.opt("PARENT_ID");
			
			
			System.out.println("-contentId="+contentId);
			System.out.println("-parentId="+parentId);
			
			
			SbiGlContents contents = dao.loadContents(contentId);
			
			System.out.println("padre="+contents.getParent().getContentId());
			
			SbiGlContents parent = dao.loadContents(parentId);
			contents.setParent(parent);
			
			System.out.println("padre="+contents.getParent().getContentId());
			
			dao.modifyContents(contents);
			
			
			return "{status:'ok'}";
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t);
		}
	}
	
	
	
	
	
	
	@GET
	@Path("/listContents")
	@Produces(MediaType.APPLICATION_JSON)
	public String listContents(@Context HttpServletRequest req) {
		try {
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);
			Integer glossaryId = getNumberOrNull(req.getParameter("GLOSSARY_ID"));
			Integer parentId = getNumberOrNull(req.getParameter("PARENT_ID"));
			List<SbiGlContents> lst = dao.listContentsByGlossaryId(glossaryId, parentId);
			JSONArray jarr = new JSONArray();
			if (lst != null) {
				for (Iterator<SbiGlContents> iterator = lst.iterator(); iterator.hasNext();) {
					SbiGlContents sbiGlContents = iterator.next();
					jarr.put(fromContentsLight(sbiGlContents));
				}
			}
			return jarr.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t);
		}
	}

	@GET
	@Path("/listGlossary")
	@Produces(MediaType.APPLICATION_JSON)
	public String listGlossary(@Context HttpServletRequest req) {
		try {
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);
			List<SbiGlGlossary> lst = dao.listGlossary();
			JSONArray jarr = new JSONArray();
			if (lst != null) {
				for (Iterator<SbiGlGlossary> iterator = lst.iterator(); iterator.hasNext();) {
					SbiGlGlossary sbiGlGlossary = iterator.next();
					jarr.put(fromGlossaryLight(sbiGlGlossary));
				}
			}
			return jarr.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t);
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
			List<SbiGlWord> lst = null;
			if (word != null && !word.trim().isEmpty()) {
				lst = dao.listWordFiltered(word);
			} else {
				lst = dao.listWord();
			}
			JSONArray jarr = new JSONArray();
			if (lst != null) {
				for (Iterator<SbiGlWord> iterator = lst.iterator(); iterator.hasNext();) {
					SbiGlWord sbiGlWord = iterator.next();
					jarr.put(fromWordLight(sbiGlWord));
				}
			}
			return jarr.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t);
		}
	}

	@GET
	@Path("/getWord")
	@Produces(MediaType.APPLICATION_JSON)
	public String getWord(@Context HttpServletRequest req) {
		String id = req.getParameter("WORD_ID");
		if (!id.matches("\\d+")) {
			throw new SpagoBIServiceException(req.getPathInfo(), "Input param Id [" + id + "] not valid");
		}
		try {
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);
			SbiGlWord word = dao.loadWord(Integer.valueOf(id));
			JSONObject jobj = from(word);
			return jobj.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t);
		}
	}

	/*
	 * @POST
	 * 
	 * @Path("/saveWord")
	 * 
	 * @Produces(MediaType.APPLICATION_JSON) public String saveWord(@Context HttpServletRequest req) { UserProfile profile = (UserProfile)
	 * req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE); //TODO check if profile is null try { JSONObject requestBodyJSON =
	 * RestUtilities.readBodyAsJSONObject(req); IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
	 * 
	 * if (profile.getIsSuperadmin()) { TenantManager.unset(); dao.setUserID(profile.getUserId().toString()); } else { dao.setUserProfile(profile); }
	 * 
	 * SbiGlWord word = from(requestBodyJSON); if(word.getWordId()==null){ dao.insertWord(word); }else{ dao.modifyWord(word); } return jsonWord.toString(); }
	 * catch (Throwable t) { throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t); } }
	 */
	private static JSONObject fromWordLight(SbiGlWord sbiGlWord) throws JSONException {
		JSONObject jobj = new JSONObject();
		jobj.put("WORD_ID", sbiGlWord.getWordId());
		jobj.put("WORD", sbiGlWord.getWord());
		return jobj;
	}

	private static JSONObject from(SbiGlWord word) throws JSONException {
		JSONObject obj = new JSONObject(word);
		
		/*
		obj.put("WORD_ID", word.getWordId());
		obj.put("WORD", word.getWord());
		obj.put("DESCR", word.getDescr());
		obj.put("FORMULA", word.getFormula());
		obj.put("STATE", word.getState());
		obj.put("CATEGORY", word.getCategory());
		JSONArray links = new JSONArray();
		if (word.getReferences() != null) {
			for (Iterator<SbiGlReferences> iterator = word.getReferences().iterator(); iterator.hasNext();) {
				SbiGlReferences refWord = iterator.next();
				links.put(fromWordLight(refWord.getRefWord()));
			}
			obj.put("LINK", links);
		}
		JSONArray attrs = new JSONArray();
		if (word.getAttributes() != null) {
			for (Iterator<SbiGlWordAttr> iterator = word.getAttributes().iterator(); iterator.hasNext();) {
				SbiGlWordAttr attr = iterator.next();
				JSONObject jsonAttr = new JSONObject();
				jsonAttr.put("ATTRIBUTE_NM", attr.getAttribute().getAttributeNm());
				jsonAttr.put("VALUE", attr.getValue());
				attrs.put(jsonAttr);
			}
			obj.put("SBI_GL_WORD_ATTR", attrs);
		}*/
		return obj;
	}

	private JSONObject fromGlossaryLight(SbiGlGlossary sbiGlGlossary) throws JSONException {
		JSONObject ret = new JSONObject();
		ret.put("GLOSSARY_ID", sbiGlGlossary.getGlossaryId());
		ret.put("GLOSSARY_NM", sbiGlGlossary.getGlossaryNm());
		return ret;
	}

	/*
	 * private static SbiGlWord from(JSONObject obj) throws JSONException { SbiGlWord word = new SbiGlWord(); int id = obj.optInt("WORD_ID");
	 * word.setWordId(id!=0?id:null); word.setWord(obj.optString("WORD")); word.setDescr(obj.optString("DESCR")); word.setState(obj.optString("STATE"));
	 * word.setFormula(obj.optString("FORMULA")); word.setCategory(obj.optString("CATEGORY")); JSONArray links = obj.optJSONArray("LINK"); if(links!=null){
	 * word.setReferences(new HashSet<SbiGlReferences>()); for (int i=0;i<links.length();i++) { JSONObject jsonRef = new JSONObject(links.get(i));
	 * SbiGlReferences ref = new SbiGlReferences(); ref.setId(new SbiGlReferencesId(word.getWordId(), jsonRef.optInt("REF_WORD_ID"))); ref.set //TODO
	 * word.getReferences().add(ref ); } } // TODO return word; }
	 */
	public static void main(String[] args) {
		try {
			SbiGlWord refw = new SbiGlWord(2, "test2", "primo test2", "fffff2", "S", "cat01");
			SbiGlWord obj = new SbiGlWord(1, "test", "primo test", "fffff", "S", "cat01");
			obj.setReferences(new HashSet<SbiGlReferences>());
			obj.getReferences().add(new SbiGlReferences(new SbiGlReferencesId(1, refw.getWordId()), obj, refw, 1));
			obj.setAttributes(new HashSet<SbiGlWordAttr>());
			SbiGlAttribute attribute = new SbiGlAttribute(1, "attributeCd", "attr name", "attributeDs", 1, "type", "domain", "format", "displayTp", "1");
			obj.getAttributes().add(new SbiGlWordAttr(new SbiGlWordAttrId(1, 1), obj, attribute, "attr 1 value", 1));
			JSONObject jobj = new JSONObject().put("aaa",obj);//from(obj);
			System.out.println(jobj.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
