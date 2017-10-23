package it.eng.spagobi.tools.catalogue.dao;

import it.eng.spagobi.commons.dao.SpagoBIDAOException;

public class SpagoBIDAOMetaModelNameExistingException extends SpagoBIDAOException{
	
	private static final long serialVersionUID = 1L;
	
	public SpagoBIDAOMetaModelNameExistingException(String message) {
		super(message);
	
	}
	
	public SpagoBIDAOMetaModelNameExistingException(Throwable ex) {
		super(ex);
	}

	
	public SpagoBIDAOMetaModelNameExistingException(String message, Throwable ex) {
		super(message, ex);
	}

	
}
