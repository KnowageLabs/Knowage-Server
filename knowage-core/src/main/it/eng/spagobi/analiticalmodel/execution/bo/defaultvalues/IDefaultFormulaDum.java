package it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentUrlManager;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;

import java.util.Locale;

public interface IDefaultFormulaDum {

	public String getName();

	public DefaultValuesList getDefaultValues(BIObjectParameter analyticalDocumentParameter, DocumentUrlManager dum, IEngUserProfile profile, BIObject object,
			Locale locale, String role);

}
