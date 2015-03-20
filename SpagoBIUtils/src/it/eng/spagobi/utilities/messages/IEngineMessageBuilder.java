package it.eng.spagobi.utilities.messages;

import java.util.Locale;

/**
 * @author Andrea Fantappiè (andrea.fantappiè@eng.it)
 * 
 * DATE            CONTRIBUTOR/DEVELOPER                        NOTE
 * 19-04-2013   Antonella Giachino (antonella.giachino@eng.it)      
 * 				Andrea Fantappiè (andrea.fantappiè@eng.it) 		Added internationalization management
 *  														  	for highchart engine
 *
 */

public interface IEngineMessageBuilder
{
	public String getI18nMessage(Locale locale, String code);
}
