package it.eng.spagobi.api.v2;

import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/2.0/internationalization/languages")
public class LanguagesResource {

	static protected Logger logger = Logger.getLogger(LanguagesResource.class);

	@GET
	@Path("/")
	@Produces({ MediaType.APPLICATION_JSON })
	public JSONArray getAvailableLanguages() {
		JSONArray toReturn = new JSONArray();

		try {
			IConfigDAO configsDao = DAOFactory.getSbiConfigDAO();

			String defaultLanguage = configsDao.loadConfigParametersByLabel("SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGE.default").getValueCheck();
			String[] availableLanguages = configsDao.loadConfigParametersByLabel("SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGES").getValueCheck().split(",");

			for (String l : availableLanguages) {
				JSONObject languageObj = new JSONObject();
				Locale locale = Locale.forLanguageTag(l);
				languageObj.put("language", String.format("%s(%s)", locale.getDisplayLanguage(), locale.getISO3Country()));
				languageObj.put("iso3code", locale.getISO3Language());
				languageObj.put("languageTag", locale.toLanguageTag());
				if (l.equals(defaultLanguage)) {
					languageObj.put("defaultLanguage", true);
				}
				toReturn.put(languageObj);
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting available languages", e);
		}

		return toReturn;
	}
}
