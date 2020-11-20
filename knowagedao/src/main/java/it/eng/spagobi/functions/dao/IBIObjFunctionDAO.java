package it.eng.spagobi.functions.dao;

import java.util.ArrayList;

import org.hibernate.Session;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;

public interface IBIObjFunctionDAO {

	public ArrayList<BIObject> getBIObjectsUsingFunction(Integer functionId, Session currSession) throws EMFUserError;
}
