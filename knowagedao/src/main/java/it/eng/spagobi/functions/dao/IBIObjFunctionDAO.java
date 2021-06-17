package it.eng.spagobi.functions.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.Session;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;

public interface IBIObjFunctionDAO {

	public ArrayList<BIObject> getBIObjectsUsingFunction(UUID uuid, Session currSession) throws EMFUserError;

	public void eraseBIObjFunctionByObjectId(Integer biObjId) throws EMFUserError;

	public void eraseBIObjFunctionByObjectId(Integer biObjId, Session currSession) throws EMFUserError;

	public void updateObjectFunctions(BIObject biObj, List<UUID> functionUuids, Session currSession) throws EMFUserError;

	public ArrayList<BIObjFunction> getBiObjFunctions(Integer biObjId) throws EMFUserError;

	public ArrayList<BIObjFunction> getBiObjFunctions(Integer biObjId, Session currSession) throws EMFUserError;
}
