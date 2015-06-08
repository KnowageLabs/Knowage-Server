package it.eng.spagobi.tools.glossary;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.glossary.dao.IGlossaryDAO;
import it.eng.spagobi.tools.glossary.metadata.SbiGlWord;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

@Path("/1.0/glossary")
public class GlossaryService {

	@GET
	@Path("/listWords")
	@Produces(MediaType.APPLICATION_JSON)
	public String listWords(@Context HttpServletRequest req) {
		try {
			IGlossaryDAO dao = DAOFactory.getGlossaryDAO();
			// Map analyticalDrivers = getEngineInstance().getAnalyticalDrivers();
			// Map profileAttributes = UserProfileUtils.getProfileAttributes((UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE));
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);
			List<SbiGlWord> lst = dao.listWord();
			JSONArray jarr = new JSONArray();
			for (Iterator<SbiGlWord> iterator = lst.iterator(); iterator.hasNext();) {
				SbiGlWord sbiGlWord = iterator.next();
				JSONObject jobj = new JSONObject();
				jobj.put("WORD_ID", sbiGlWord.getWordId());
				jobj.put("WORD", sbiGlWord.getWord());
				jarr.put(jobj);
			}
			return jarr.toString();
			/*
			 * VelocityContext velocityContext = new VelocityContext(); velocityContext.put("node", jarr); Template velocityTemplate =
			 * ve.getTemplate("/chart/templates/highcharts414/drilldowndata.vm"); result = ChartEngineUtil.applyTemplate(velocityTemplate, velocityContext);
			 * String jsonData = ChartEngineDataUtil.loadJsonData(jsonTemplate, dataSet, analyticalDrivers, profileAttributes, getLocale()); JSONObject
			 * dataSetJSON = new JSONObject(); try { JSONDataWriter writer = new JSONDataWriter();
			 * 
			 * Object resultNumber = dataStore.getMetaData().getProperty("resultNumber"); if (resultNumber == null)
			 * dataStore.getMetaData().setProperty("resultNumber", new Integer((int) dataStore.getRecordsCount())); dataSetJSON = (JSONObject)
			 * writer.write(dataStore); jsonData = dataSetJSON.toString(); } catch (Throwable e) { throw new
			 * SpagoBIServiceException("Impossible to serialize datastore", e); }
			 * 
			 * VelocityContext velocityContext = ChartEngineUtil.loadVelocityContext(jsonTemplate, jsonData); String chartType =
			 * ChartEngineUtil.extractChartType(jsonTemplate, velocityContext); Template velocityTemplate =
			 * ve.getTemplate(ChartEngineUtil.getVelocityModelPath(chartType)); return ChartEngineUtil.applyTemplate(velocityTemplate, velocityContext);
			 */
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(),
					"An unexpected error occured while executing service: JsonChartTemplateService.getJSONChartTemplate", t);
		}
	}

	public static void main(String[] args) {
		try {
			List<SbiGlWord> lst = new ArrayList<SbiGlWord>();
			lst.add(new SbiGlWord(1, "aaa", null, null, null, null));
			lst.add(new SbiGlWord(20, "bbb", null, null, null, null));
			JSONArray jarr = new JSONArray();
			for (Iterator iterator = lst.iterator(); iterator.hasNext();) {
				SbiGlWord sbiGlWord = (SbiGlWord) iterator.next();
				JSONObject jobj = new JSONObject();
				jobj.put("WORD_ID", sbiGlWord.getWordId());
				jobj.put("WORD", sbiGlWord.getWord());
				jarr.put(jobj);
			}
			System.out.println(jarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
