package it.eng.spagobi.analiticalmodel.document.handlers;

import java.util.Locale;

import org.apache.log4j.Logger;

import it.eng.spago.security.IEngUserProfile;

public class DocumentUrlManager {

	static private Logger logger = Logger.getLogger(DocumentUrlManager.class);
	private static final String TREE_INNER_LOV_TYPE = "treeinner";

	private IEngUserProfile userProfile = null;
	private Locale locale = null;

	public DocumentUrlManager(IEngUserProfile userProfile, Locale locale) {
		this.userProfile = userProfile;
		this.locale = locale;
	}














}
