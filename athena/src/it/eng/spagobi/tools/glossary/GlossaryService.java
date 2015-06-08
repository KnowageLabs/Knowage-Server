package it.eng.spagobi.tools.glossary;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.glossary.dao.IGlossaryDAO;
import it.eng.spagobi.tools.glossary.metadata.SbiGlAttribute;
import it.eng.spagobi.tools.glossary.metadata.SbiGlReferences;
import it.eng.spagobi.tools.glossary.metadata.SbiGlReferencesId;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWord;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWordAttr;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWordAttrId;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/1.0/glossary")
public class GlossaryService {

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
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service: JsonChartTemplateService.getJSONChartTemplate", t);
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
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service: JsonChartTemplateService.getJSONChartTemplate", t);
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
	 * catch (Throwable t) { throw new SpagoBIServiceException(req.getPathInfo(),
	 * "An unexpected error occured while executing service: JsonChartTemplateService.getJSONChartTemplate", t); } }
	 */
	private static JSONObject fromWordLight(SbiGlWord sbiGlWord) throws JSONException {
		JSONObject jobj = new JSONObject();
		jobj.put("WORD_ID", sbiGlWord.getWordId());
		jobj.put("WORD", sbiGlWord.getWord());
		return jobj;
	}

	private static JSONObject from(SbiGlWord word) throws JSONException {
		JSONObject obj = new JSONObject();
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
		}
		return obj;
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
			JSONObject jobj = from(obj);
			System.out.println(jobj.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
