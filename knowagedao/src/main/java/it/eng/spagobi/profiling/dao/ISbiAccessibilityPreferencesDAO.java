package it.eng.spagobi.profiling.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.profiling.bean.SbiAccessibilityPreferences;

public interface ISbiAccessibilityPreferencesDAO extends ISpagoBIDao {

	SbiAccessibilityPreferences readUserAccessibilityPreferences(String userId) throws EMFUserError;

	void saveOrUpdatePreferencesControls(String userId, boolean enableUIO, boolean enableRobobrailles, boolean enableVoice, boolean enableGraphSonification)
			throws EMFUserError;

	void saveOrUpdateUserPreferences(String userId, String preferences) throws EMFUserError;

	Integer saveAccessibilityPreferences(String userId, boolean enableUIO, boolean enableRobobrailles, boolean enableVoice, boolean enableGraphSonification,
			String preferences) throws EMFUserError;

	void updateAccesibilityPreferences(SbiAccessibilityPreferences ap) throws EMFUserError;

}
