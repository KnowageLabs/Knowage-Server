package it.eng.spagobi.tools.glossary;

import static it.eng.spagobi.tools.glossary.util.Util.getNumberOrNull;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.glossary.dao.IGlossaryDAO;
import it.eng.spagobi.tools.glossary.metadata.SbiGlContents;
import it.eng.spagobi.tools.glossary.metadata.SbiGlGlossary;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWlist;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWlistId;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWord;
import it.eng.spagobi.tools.udp.dao.IUdpDAO;
import it.eng.spagobi.tools.udp.metadata.SbiUdp;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 */
@Path("/1.0/glossary/business")
@ManageAuthorization
public class GlossaryBusinessService {

	@POST
	@Path("/modifyContentsGlossary")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.MANAGE_GLOSSARY_BUSINESS })
	public String modifyContentsGlossary(@Context HttpServletRequest req) {
		try {

			System.out.println("ModifyContentsGlossary");
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);
			System.out.println(req.toString());
			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
			JSONObject jo = new JSONObject();

			if (requestVal.has("CONTENT_ID")) {
				// modify content (logical node)
				Integer contentId = getNumberOrNull(requestVal.opt("CONTENT_ID"));
				Integer parentId = getNumberOrNull(requestVal.opt("PARENT_ID"));
				Integer glossaryId = getNumberOrNull(requestVal.opt("GLOSSARY_ID"));
				boolean status = dao.modifyContentPosition(contentId, parentId, glossaryId);
				if (!status) {
					jo.put("Status", "NON OK");
					jo.put("Message", "sbi.glossary.content.duplicate.content");
					return jo.toString();
				}
			} else {
				// modify word
				Integer wordId = getNumberOrNull(requestVal.opt("WORD_ID"));
				Integer parentId = getNumberOrNull(requestVal.opt("PARENT_ID"));
				Integer oldparentId = getNumberOrNull(requestVal.opt("OLD_PARENT_ID"));

				SbiGlWlist contw = new SbiGlWlist();
				contw.setId(new SbiGlWlistId(wordId, parentId));

				SbiGlWlistId id = dao.insertWlist(contw);
				if (id == null) {
					jo.put("Status", "NON OK");
					jo.put("Message", "sbi.glossary.content.duplicate.node");
					return jo.toString();

				}

				dao.deleteWlist((new SbiGlWlistId(wordId, oldparentId)));

			}

			jo.put("Status", "OK");
			return jo.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t);
		}
	}

	@POST
	@Path("/addGlossary")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.MANAGE_GLOSSARY_BUSINESS })
	public String addGlossary(@Context HttpServletRequest req) {
		JSONObject jo = new JSONObject();
		try {
			System.out.println("addGlossary");
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);
			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

			if (((String) requestVal.opt("GLOSSARY_NM")).trim().isEmpty()) {
				jo.put("Status", "NON OK");
				jo.put("Message", "sbi.glossary.empty.glossary.name");
				return jo.toString();
			}

			jo.put("Status", "OK");
			// check if name is present
			String gn = (String) requestVal.opt("GLOSSARY_NM");
			if (gn.trim().isEmpty()) {
				// throw new SpagoBIServiceException(req.getPathInfo(),
				// "Glossary name is request");
				jo.put("Status", "NON OK");
				jo.put("Message", "sbi.glossary.new.name.request");
				return jo.toString();

			}

			SbiGlGlossary gloss;
			List<SbiGlGlossary> lg = dao.loadGlossaryByName((String) requestVal.opt("GLOSSARY_NM"));

			if (((String) requestVal.opt("SaveOrUpdate")).compareTo("Save") == 0) {
				// check if there is another glossary with the same name
				if (!lg.isEmpty()) {
					// throw new SpagoBIServiceException(req.getPathInfo(),
					// "Glossary Name already defined");
					jo.put("Status", "NON OK");
					jo.put("Message", "sbi.glossary.new.name.duplicate");
					return jo.toString();
				}

				gloss = new SbiGlGlossary();
			} else {
				// >1 because currently there is only the gloss that I want to
				// change
				if (lg.size() > 1) {
					jo.put("Status", "NON OK");
					jo.put("Message", "sbi.glossary.new.name.duplicate");
					return jo.toString();
				}
				gloss = dao.loadGlossary((Integer) requestVal.opt("GLOSSARY_ID"));
			}

			gloss.setGlossaryNm((String) requestVal.opt("GLOSSARY_NM"));
			gloss.setGlossaryCd((String) requestVal.opt("GLOSSARY_CD"));
			gloss.setGlossaryDs((String) requestVal.opt("GLOSSARY_DS"));

			if (((String) requestVal.opt("SaveOrUpdate")).compareTo("Save") == 0) {
				Integer id = dao.insertGlossary(gloss);
				jo.put("id", id);
			} else {
				dao.modifyGlossary(gloss);
			}

			return jo.toString();

		} catch (Throwable t) {
			// throw new SpagoBIServiceException(req.getPathInfo(),
			// "An unexpected error occured while executing service", t);
			try {
				jo.put("Status", "NON OK");
				jo.put("Message", "sbi.glossary.error.save");
				jo.put("Error_text", t.getCause().getCause().getMessage());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return jo.toString();
		}
	}

	@POST
	@Path("/addContents")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.MANAGE_GLOSSARY_BUSINESS })
	public String addContents(@Context HttpServletRequest req) {
		JSONObject jo = new JSONObject();

		try {
			System.out.println("addContents");
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);
			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

			Integer id;
			List<SbiGlContents> lg = dao.loadContentsByName((String) requestVal.opt("CONTENT_NM"));

			if (requestVal.has("SaveOrUpdate")) {
				if (((String) requestVal.opt("SaveOrUpdate")).compareTo("Update") == 0) {
					// update contents (logical node)
					if (lg.size() > 1) {
						// throw new SpagoBIServiceException(req.getPathInfo(),
						// "content Name already defined");
						jo.put("Status", "NON OK");
						jo.put("Message", "sbi.glossary.content.duplicate.content");
						return jo.toString();
					}
					Integer contentId = getNumberOrNull(requestVal.opt("CONTENT_ID"));
					SbiGlContents cont = dao.loadContents(contentId);
					cont.setContentNm((String) requestVal.opt("CONTENT_NM"));
					cont.setContentCd((String) requestVal.opt("CONTENT_CD"));
					cont.setContentDs((String) requestVal.opt("CONTENT_DS"));
					dao.modifyContents(cont);
					jo.put("Status", "OK");
					return jo.toString();
				} else {
					if (!lg.isEmpty()) {
						// throw new SpagoBIServiceException(req.getPathInfo(),
						// "content Name already defined");
						jo.put("Status", "NON OK");
						jo.put("Message", "sbi.glossary.content.duplicate.content");
						return jo.toString();
					}
				}

			}

			if (requestVal.has("CONTENT_NM")) {
				// add contents to contents_parent
				if (((String) requestVal.opt("CONTENT_NM")).trim().isEmpty()) {
					jo.put("Status", "NON OK");
					jo.put("Message", "sbi.glossary.empty.content.name");
					return jo.toString();
				}
				Integer parentId = getNumberOrNull(requestVal.opt("PARENT_ID"));
				Integer glossaryId = getNumberOrNull(requestVal.opt("GLOSSARY_ID"));

				SbiGlContents cont = new SbiGlContents();
				cont.setContentNm((String) requestVal.opt("CONTENT_NM"));
				cont.setContentCd((String) requestVal.opt("CONTENT_CD"));
				cont.setContentDs((String) requestVal.opt("CONTENT_DS"));
				cont.setGlossaryId(glossaryId);
				cont.setParentId(parentId);
				id = dao.insertContents(cont);
				jo.put("Status", "OK");
				jo.put("id", id);
			} else {
				// add word to contents parent
				Integer parentId = getNumberOrNull(requestVal.opt("PARENT_ID"));
				Integer wordId = getNumberOrNull(requestVal.opt("WORD_ID"));
				Integer glossaryId = getNumberOrNull(requestVal.opt("GLOSSARY_ID"));

				List<SbiGlWlist> presWordInGloss = dao.listWlistByGlossaryIdAndWordId(glossaryId, wordId);
				if (!presWordInGloss.isEmpty()) {
					jo.put("Status", "NON OK");
					jo.put("Message", "sbi.glossary.content.duplicate.node");
				} else {

					SbiGlWlist contw = new SbiGlWlist();
					contw.setId(new SbiGlWlistId(wordId, parentId));
					// contw.setWordId(wordId);
					// contw.setContentId(parentId);

					SbiGlWlistId idw = dao.insertWlist(contw);
					if (idw == null) {
						jo.put("Status", "NON OK");
						jo.put("Message", "sbi.glossary.content.duplicate.node");
					} else {
						jo.put("Status", "OK");
					}
				}
			}

			return jo.toString();

		} catch (Throwable t) {
			// throw new SpagoBIServiceException(req.getPathInfo(),
			// "An unexpected error occured while executing service", t);
			try {
				jo.put("Status", "NON OK");
				jo.put("Message", "sbi.glossary.content.load.error");

				if (t.getMessage() == null) {
					jo.put("Error_text", "ERRORE SCONOSCIUTO");
				} else {
					if (t.getCause().getMessage() != null) {
						if (t.getCause().getCause().getMessage() != null) {
							jo.put("Error_text", t.getCause().getCause().getMessage());
						} else {
							jo.put("Error_text", t.getCause().getMessage());
						}
					} else {
						jo.put("Error_text", t.getMessage());
					}

				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return jo.toString();
		}
	}

	@POST
	@Path("/addWord")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.MANAGE_GLOSSARY_BUSINESS })
	public String addWord(@Context HttpServletRequest req) {
		JSONObject jo = new JSONObject();
		try {
			System.out.println("newWord");
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IUdpDAO daoUdp = DAOFactory.getUdpDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);
			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

			SbiGlWord word;

			if (((String) requestVal.opt("WORD")).trim().isEmpty()) {
				jo.put("Status", "NON OK");
				jo.put("Message", "sbi.glossary.empty.word.name");
				return jo.toString();
			}

			boolean update = false;
			// check uniqueness of name
			List<SbiGlWord> lg = dao.loadWordByName((String) requestVal.opt("WORD"));
			if (((String) requestVal.opt("SaveOrUpdate")).compareTo("Save") == 0) {

				if (!lg.isEmpty()) {
					// throw new SpagoBIServiceException(req.getPathInfo(),
					// "Word Name already defined");
					jo.put("Status", "NON OK");
					jo.put("Message", "sbi.glossary.word.new.name.duplicate");
					return jo.toString();
				}
				word = new SbiGlWord();
			} else {
				// >1 because currently there is only the word that I want to
				// change
				if (lg.size() > 1) {
					// throw new SpagoBIServiceException(req.getPathInfo(),
					// "Word Name already defined");
					jo.put("Status", "NON OK");
					jo.put("Message", "sbi.glossary.word.new.name.duplicate");
					return jo.toString();
				}
				update = true;
				word = dao.loadWord(getNumberOrNull(requestVal.opt("WORD_ID")));
			}

			word.setWord((String) requestVal.opt("WORD"));
			word.setState_id(getNumberOrNull(requestVal.opt("STATE")));
			word.setCategory_id(getNumberOrNull(requestVal.opt("CATEGORY")));
			word.setDescr((String) requestVal.opt("DESCR"));
			word.setFormula((String) requestVal.opt("FORMULA"));
			JSONArray refe = (JSONArray) requestVal.opt("LINK");
			JSONArray attr = (JSONArray) requestVal.opt("SBI_GL_WORD_ATTR");

			Map<Integer, JSONObject> MapLink = new HashMap<Integer, JSONObject>();
			List<SbiGlWord> objLink = null;
			if (refe.length() != 0) {
				Object[] link = new Object[refe.length()];
				for (int i = 0; i < refe.length(); i++) {
					link[i] = refe.getJSONObject(i).getInt("WORD_ID");
					MapLink.put(refe.getJSONObject(i).getInt("WORD_ID"), refe.getJSONObject(i));
				}
				objLink = dao.listWordFromArray(link);
			}

			Map<Integer, JSONObject> MapAttr = new HashMap<Integer, JSONObject>();
			List<SbiUdp> objUdp = null;
			if (attr.length() != 0) {
				Object[] att = new Object[attr.length()];
				for (int i = 0; i < attr.length(); i++) {

					if (attr.getJSONObject(i).getString("VALUE").trim().isEmpty()) {
						jo.put("Status", "NON OK");
						jo.put("Message", "sbi.glossary.empty.attribute.name");
						return jo.toString();
					}

					att[i] = attr.getJSONObject(i).getInt("ATTRIBUTE_ID");
					MapAttr.put(attr.getJSONObject(i).getInt("ATTRIBUTE_ID"), attr.getJSONObject(i));
				}
				// objAttr = dao.listAttrFromArray(att);
				objUdp = daoUdp.listUdpFromArray(att);
			}

			// Integer id = dao.insertWord(word, objLink, objAttr, MapAttr,
			// MapLink, update);

			Integer id = dao.insertWord(word, objLink, objUdp, MapAttr, MapLink, update);

			jo.put("Status", "OK");
			jo.put("id", id);
			return jo.toString();

		} catch (Throwable t) {
			// throw new SpagoBIServiceException(req.getPathInfo(),
			// "An unexpected error occured while executing service", t);

			try {
				jo.put("Status", "NON OK");
				jo.put("Message", "sbi.glossary.word.save.error");
				jo.put("Error_text", t.getMessage());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return jo.toString();
		}
	}

	@POST
	@Path("/deleteWord")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.MANAGE_GLOSSARY_BUSINESS })
	public String deleteWord(@Context HttpServletRequest req) {
		try {
			System.out.println("deleteWord");
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);
			Integer wordId = getNumberOrNull(req.getParameter("WORD_ID"));

			// dao.deleteWordReferences(wordId);
			dao.deleteWord(wordId);

			JSONObject jo = new JSONObject();
			jo.put("Status", "OK");
			return jo.toString();

		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t);
		}
	}

	@POST
	@Path("/deleteGlossary")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.MANAGE_GLOSSARY_BUSINESS })
	public String deleteGlossary(@Context HttpServletRequest req) {
		try {
			System.out.println("deleteGlossary");
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);
			Integer glossaryId = getNumberOrNull(req.getParameter("GLOSSARY_ID"));

			dao.deleteGlossary(glossaryId);

			JSONObject jo = new JSONObject();
			jo.put("Status", "OK");
			return jo.toString();

		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t);
		}
	}

	@POST
	@Path("/deleteContents")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.MANAGE_GLOSSARY_BUSINESS })
	public String deleteContents(@Context HttpServletRequest req) {
		try {
			System.out.println("deleteContents");
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);

			Integer contentId = getNumberOrNull(req.getParameter("CONTENTS_ID"));
			if (contentId != null) {
				dao.deleteContents(contentId);
			} else {
				Integer parentId = getNumberOrNull(req.getParameter("PARENT_ID"));
				Integer wordId = getNumberOrNull(req.getParameter("WORD_ID"));
				dao.deleteWlist(new SbiGlWlistId(wordId, parentId));

			}

			JSONObject jo = new JSONObject();
			jo.put("Status", "OK");
			return jo.toString();

		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t);
		}
	}

	@POST
	@Path("/cloneGlossary")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.MANAGE_GLOSSARY_BUSINESS })
	public String cloneGlossary(@Context HttpServletRequest req) {
		try {
			System.out.println("cloneGlossary");
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			// TODO check if profile is null
			dao.setUserProfile(profile);

			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

			Integer glossaryId = (Integer) requestVal.opt("GLOSSARY_ID");

			if (glossaryId == null) {
				throw new SpagoBIServiceException(req.getPathInfo(), "ID non puo essere null");
			}

			// check if name is present
			String gn = (String) requestVal.opt("GLOSSARY_NM");
			if (gn.trim().isEmpty()) {
				throw new SpagoBIServiceException(req.getPathInfo(), "Glossary name is request");
			}
			// get glossary
			SbiGlGlossary glo = dao.loadGlossary(glossaryId);
			if (glo == null) {
				throw new SpagoBIServiceException(req.getPathInfo(), "Glossary not present");
			}

			// check if there is another glossary with the same name
			List<SbiGlGlossary> lg = dao.loadGlossaryByName((String) requestVal.opt("GLOSSARY_NM"));
			if (!lg.isEmpty()) {
				throw new SpagoBIServiceException(req.getPathInfo(), "Glossary Name already defined");
			}

			glo.setGlossaryNm((String) requestVal.opt("GLOSSARY_NM"));
			glo.setGlossaryCd((String) requestVal.opt("GLOSSARY_CD"));
			glo.setGlossaryDs((String) requestVal.opt("GLOSSARY_DS"));
			Integer newGlossId = dao.insertGlossary(glo);

			dao.cloneGlossary(glossaryId, newGlossId);

			JSONObject jo = new JSONObject();
			jo.put("Status", "OK");
			jo.put("id", newGlossId);
			return jo.toString();

		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", t);
		}
	}

}
