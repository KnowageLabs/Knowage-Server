package it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues;

import java.util.Locale;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.handlers.AbstractBIResourceRuntime;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractDriver;
import it.eng.spagobi.tools.catalogue.metadata.IDrivableBIResource;

public interface IDefaultFormulaDum {

	public String getName();

	public DefaultValuesList getDefaultValues(AbstractDriver analyticalDocumentParameter, AbstractBIResourceRuntime dum, IEngUserProfile profile, IDrivableBIResource object,
			Locale locale, String role);

}
