package it.eng.spagobi.functions.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.functions.metadata.IInputVariable;
import it.eng.spagobi.functions.metadata.IOutputColumn;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;
import it.eng.spagobi.functions.metadata.SbiFunctionInputColumn;
import it.eng.spagobi.functions.metadata.SbiFunctionInputColumnId;
import it.eng.spagobi.functions.metadata.SbiFunctionInputVariable;
import it.eng.spagobi.functions.metadata.SbiFunctionInputVariableId;
import it.eng.spagobi.functions.metadata.SbiFunctionOutputColumn;
import it.eng.spagobi.functions.metadata.SbiFunctionOutputColumnId;
import it.eng.spagobi.utilities.CatalogFunction;
import it.eng.spagobi.utilities.assertion.Assert;

public class CatalogFunctionDAOImpl extends AbstractHibernateDAO implements ICatalogFunctionDAO {

	static private Logger logger = Logger.getLogger(CatalogFunctionDAOImpl.class);

	@Override
	public List<SbiCatalogFunction> loadAllCatalogFunctions() {
		logger.debug("IN");
		Session session = null;
		List<SbiCatalogFunction> sbiCatalogFunctions = null;
		try {
			session = getSession();
			Assert.assertNotNull(session, "session cannot be null");
			Query hibQuery = session.createQuery("from SbiCatalogFunction");
			sbiCatalogFunctions = hibQuery.list();
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An error occured while reading Catalog Functions from DB", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return sbiCatalogFunctions;
	}

	@Override
	public List<SbiCatalogFunction> loadAllCatalogFunctionsByBiobjId(Integer biobjId) {
		logger.debug("IN");
		Session session = null;
		List<SbiCatalogFunction> sbiCatalogFunctions = null;
		try {
			session = getSession();
			Assert.assertNotNull(session, "session cannot be null");

			StringBuilder query = new StringBuilder();
			query.append("select sbf from SbiCatalogFunction sbf ");
			query.append(", SbiObjFunction sof ");
			query.append("where sbf.functionUuid=sof.functionUuid ");
			query.append("and sof.sbiObject.biobjId = " + biobjId);

			Query hibQuery = session.createQuery(query.toString());

			sbiCatalogFunctions = hibQuery.list();
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An error occured while reading Catalog Functions from DB", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return sbiCatalogFunctions;
	}

	@Override
	public String insertCatalogFunction(CatalogFunction catalogFunction, Map<String, String> inputColumns, Map<String, ? extends IInputVariable> inputVariables,
			Map<String, ? extends IOutputColumn> outputColumns) {

		String catalogFunctionUuid;
		Session session;
		Transaction transaction;

		logger.debug("IN");

		session = null;
		transaction = null;
		try {
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}
			SbiCatalogFunction hibMap = toSbiFunctionCatalog(catalogFunction);
			updateSbiCommonInfo4Insert(hibMap);
			catalogFunctionUuid = (String) session.save(hibMap);

			List l = session.createQuery("from SbiCatalogFunction").list();

			SbiCatalogFunction hibCatFunction = (SbiCatalogFunction) session.load(SbiCatalogFunction.class, catalogFunctionUuid.toString());

			hibCatFunction.setSbiFunctionInputVariables(getSbiFunctionInputVariablesSet(inputVariables, hibCatFunction));
			hibCatFunction.setSbiFunctionOutputColumns(getSbiFunctionOutputColumnsSet(outputColumns, hibCatFunction));
			hibCatFunction.setSbiFunctionInputColumns(getSbiFunctionInputColumnsSet(inputColumns, hibCatFunction));
			session.save(hibCatFunction);

			transaction.commit();

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}

			throw new SpagoBIDAOException("An unexpected error occured while inserting catalog function", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return catalogFunctionUuid;
	}

	private Set<SbiFunctionInputVariable> getSbiFunctionInputVariablesSet(Map<String, ? extends IInputVariable> inputVariables,
			SbiCatalogFunction sbiCatalogFunction) {

		Set<SbiFunctionInputVariable> inputVarSet = new HashSet<SbiFunctionInputVariable>();
		SbiFunctionInputVariable var = null;

		for (String varName : inputVariables.keySet()) {
			String value = inputVariables.get(varName).getValue();
			String type = inputVariables.get(varName).getType();
			var = new SbiFunctionInputVariable(new SbiFunctionInputVariableId(sbiCatalogFunction.getFunctionUuid(), varName), sbiCatalogFunction, type, value);
			updateSbiCommonInfo4Insert(var);
			inputVarSet.add(var);
		}

		return inputVarSet;
	}

	private Set<SbiFunctionInputColumn> getSbiFunctionInputColumnsSet(Map<String, String> inputColumns, SbiCatalogFunction sbiCatalogFunction) {

		Set<SbiFunctionInputColumn> inputColSet = new HashSet<SbiFunctionInputColumn>();
		SbiFunctionInputColumn col = null;

		for (String colName : inputColumns.keySet()) {
			String value = inputColumns.get(colName);
			col = new SbiFunctionInputColumn(new SbiFunctionInputColumnId(sbiCatalogFunction.getFunctionUuid(), colName), sbiCatalogFunction, value);
			updateSbiCommonInfo4Insert(col);
			inputColSet.add(col);
		}

		return inputColSet;
	}

	private Set<SbiFunctionOutputColumn> getSbiFunctionOutputColumnsSet(Map<String, ? extends IOutputColumn> outputColumns,
			SbiCatalogFunction sbiCatalogFunction) {

		Set<SbiFunctionOutputColumn> outputColSet = new HashSet<SbiFunctionOutputColumn>();
		SbiFunctionOutputColumn col = null;

		for (String colName : outputColumns.keySet()) {
			String fieldType = outputColumns.get(colName).getFieldType();
			String type = outputColumns.get(colName).getType();
			col = new SbiFunctionOutputColumn(new SbiFunctionOutputColumnId(sbiCatalogFunction.getFunctionUuid(), colName), sbiCatalogFunction, fieldType,
					type);
			updateSbiCommonInfo4Insert(col);
			outputColSet.add(col);
		}

		return outputColSet;
	}

	private SbiCatalogFunction toSbiFunctionCatalog(CatalogFunction functionItem) {

		SbiCatalogFunction hibFunctionCatalogItem = new SbiCatalogFunction(UUID.randomUUID());
		hibFunctionCatalogItem.setBenchmarks(functionItem.getBenchmarks());
		hibFunctionCatalogItem.setFamily(functionItem.getFamily());
		hibFunctionCatalogItem.setLanguage(functionItem.getLanguage());
		hibFunctionCatalogItem.setName(functionItem.getName());
		hibFunctionCatalogItem.setDescription(functionItem.getDescription());
		hibFunctionCatalogItem.setOnlineScript(functionItem.getOnlineScript());
		hibFunctionCatalogItem.setOfflineScriptTrain(functionItem.getOfflineScriptTrain());
		hibFunctionCatalogItem.setOfflineScriptUse(functionItem.getOfflineScriptUse());
		hibFunctionCatalogItem.setOwner(functionItem.getOwner());
		hibFunctionCatalogItem.setLabel(functionItem.getLabel());
		hibFunctionCatalogItem.setType(functionItem.getType());

		List<String> keywords = functionItem.getKeywords();
		String keywordsString = StringUtils.join(keywords.iterator(), ",");
		hibFunctionCatalogItem.setKeywords(keywordsString);

		return hibFunctionCatalogItem;
	}

	@Override
	public String updateCatalogFunction(CatalogFunction updatedCatalogFunction, String uuid) {

		Session session;
		Transaction transaction;

		logger.debug("IN");

		session = null;
		transaction = null;
		try {
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}

			SbiCatalogFunction hibCatFunction = (SbiCatalogFunction) session.get(SbiCatalogFunction.class, uuid.toString());

			updateOutputColumns(hibCatFunction, session, updatedCatalogFunction);
			updateInputVariables(hibCatFunction, session, updatedCatalogFunction);
			updateInputColumns(hibCatFunction, session, updatedCatalogFunction);

			hibCatFunction.setLanguage(updatedCatalogFunction.getLanguage());
			hibCatFunction.setName(updatedCatalogFunction.getName());
			hibCatFunction.setDescription(updatedCatalogFunction.getDescription());
			hibCatFunction.setBenchmarks(updatedCatalogFunction.getBenchmarks());
			hibCatFunction.setFamily(updatedCatalogFunction.getFamily());
			hibCatFunction.setOnlineScript(updatedCatalogFunction.getOnlineScript());
			hibCatFunction.setOfflineScriptTrain(updatedCatalogFunction.getOfflineScriptTrain());
			hibCatFunction.setOfflineScriptUse(updatedCatalogFunction.getOfflineScriptUse());
			hibCatFunction.setOwner(updatedCatalogFunction.getOwner());
			hibCatFunction.setKeywords(StringUtils.join(updatedCatalogFunction.getKeywords().iterator(), ","));
			hibCatFunction.setLabel(updatedCatalogFunction.getLabel());
			hibCatFunction.setType(updatedCatalogFunction.getType());

			updateSbiCommonInfo4Update(hibCatFunction);
			session.update(hibCatFunction);
			transaction.commit();

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}

			throw new SpagoBIDAOException("An unexpected error occured while updating catalog function", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return uuid;

	}

	private void updateInputColumns(SbiCatalogFunction hibCatFunction, Session session, CatalogFunction updatedCatalogFunction) {

		// delete columns not reinserted and update columns that are already present (and delete them from updateColumnsList in updatedCatalogFunction)
		List<SbiFunctionInputColumn> colsToRemove = new ArrayList<SbiFunctionInputColumn>();
		for (Object o : hibCatFunction.getSbiFunctionInputColumns()) {
			SbiFunctionInputColumn ci = (SbiFunctionInputColumn) o;
			boolean delete = true;
			if (updatedCatalogFunction.getInputColumns().keySet().contains(ci.getId().getColName())) {
				delete = false;
				updateSbiCommonInfo4Update(ci);
				ci.setColType(updatedCatalogFunction.getInputColumns().get(ci.getId().getColName()));
				updatedCatalogFunction.getInputColumns().remove(ci.getId().getColName());
			}

			if (delete) {
				colsToRemove.add(ci);
			}
		}
		for (SbiFunctionInputColumn remCol : colsToRemove) {
			SbiFunctionInputColumn colToRem = (SbiFunctionInputColumn) session.get(SbiFunctionInputColumn.class, remCol.getId());
			hibCatFunction.getSbiFunctionInputColumns().remove(colToRem);
			session.delete(colToRem);
			session.flush();
		}

		// insert cols that are not presents
		Set<SbiFunctionInputColumn> hibColsSet = hibCatFunction.getSbiFunctionInputColumns();
		for (String colName : updatedCatalogFunction.getInputColumns().keySet()) {
			String colType = updatedCatalogFunction.getInputColumns().get(colName);
			SbiFunctionInputColumn col = new SbiFunctionInputColumn(new SbiFunctionInputColumnId(hibCatFunction.getFunctionUuid(), colName), hibCatFunction,
					colType);
			updateSbiCommonInfo4Insert(col);
			hibColsSet.add(col);
		}

	}

	private void updateOutputColumns(SbiCatalogFunction hibCatFunction, Session session, CatalogFunction updatedCatalogFunction) {

		// delete columns not reinserted and update columns that are already present (and delete them from updateColumnsList in updatedCatalogFunction)
		List<SbiFunctionOutputColumn> colsToRemove = new ArrayList<SbiFunctionOutputColumn>();
		for (Object o : hibCatFunction.getSbiFunctionOutputColumns()) {
			SbiFunctionOutputColumn ci = (SbiFunctionOutputColumn) o;
			boolean delete = true;
			if (updatedCatalogFunction.getOutputColumns().keySet().contains(ci.getId().getColName())) {
				delete = false;
				updateSbiCommonInfo4Update(ci);
				ci.setColFieldType(updatedCatalogFunction.getOutputColumns().get(ci.getId().getColName()).getFieldType());
				ci.setColType(updatedCatalogFunction.getOutputColumns().get(ci.getId().getColName()).getType());
				updatedCatalogFunction.getOutputColumns().remove(ci.getId().getColName());
			}

			if (delete) {
				colsToRemove.add(ci);
			}
		}
		for (SbiFunctionOutputColumn remCol : colsToRemove) {
			SbiFunctionOutputColumn colToRem = (SbiFunctionOutputColumn) session.get(SbiFunctionOutputColumn.class, remCol.getId());
			hibCatFunction.getSbiFunctionOutputColumns().remove(colToRem);
			session.delete(colToRem);
			session.flush();
		}

		// insert cols that are not present
		Set<SbiFunctionOutputColumn> hibColsSet = hibCatFunction.getSbiFunctionOutputColumns();
		for (String colName : updatedCatalogFunction.getOutputColumns().keySet()) {
			String colFieldType = updatedCatalogFunction.getOutputColumns().get(colName).getFieldType();
			String colType = updatedCatalogFunction.getOutputColumns().get(colName).getType();
			SbiFunctionOutputColumn col = new SbiFunctionOutputColumn(new SbiFunctionOutputColumnId(hibCatFunction.getFunctionUuid(), colName), hibCatFunction,
					colFieldType, colType);
			updateSbiCommonInfo4Insert(col);
			hibColsSet.add(col);
		}

	}

	private void updateInputVariables(SbiCatalogFunction hibCatFunction, Session session, CatalogFunction updatedCatalogFunction) {

		// delete vars not reinserted and update variables that are already presents (and delete them from updateVariablesList in updatedCatalogFunction)
		List<SbiFunctionInputVariable> varToRemove = new ArrayList<SbiFunctionInputVariable>();
		for (Object o : hibCatFunction.getSbiFunctionInputVariables()) {
			SbiFunctionInputVariable vi = (SbiFunctionInputVariable) o;
			boolean delete = true;
			if (updatedCatalogFunction.getInputVariables().keySet().contains(vi.getId().getVarName())) {
				delete = false;
				updateSbiCommonInfo4Update(vi);
				vi.setVarType(updatedCatalogFunction.getInputVariables().get(vi.getId().getVarName()).getType());
				vi.setVarValue(updatedCatalogFunction.getInputVariables().get(vi.getId().getVarName()).getValue());
				updatedCatalogFunction.getInputVariables().remove(vi.getId().getVarName());
			}

			if (delete) {
				varToRemove.add(vi);
			}
		}
		for (SbiFunctionInputVariable remVar : varToRemove) {
			SbiFunctionInputVariable varToRem = (SbiFunctionInputVariable) session.get(SbiFunctionInputVariable.class, remVar.getId());
			hibCatFunction.getSbiFunctionInputVariables().remove(varToRem);
			session.delete(varToRem);
			session.flush();
		}

		// insert vars that are not presents
		Set<SbiFunctionInputVariable> hibVarsSet = hibCatFunction.getSbiFunctionInputVariables();
		for (String varName : updatedCatalogFunction.getInputVariables().keySet()) {
			String varType = updatedCatalogFunction.getInputVariables().get(varName).getType();
			String varValue = updatedCatalogFunction.getInputVariables().get(varName).getValue();
			SbiFunctionInputVariable var = new SbiFunctionInputVariable(new SbiFunctionInputVariableId(hibCatFunction.getFunctionUuid(), varName),
					hibCatFunction, varType, varValue);
			updateSbiCommonInfo4Insert(var);
			hibVarsSet.add(var);
		}

	}

	@Override
	public void deleteCatalogFunction(String uuid) {

		Session session;
		Transaction transaction;

		logger.debug("IN");

		session = null;
		transaction = null;
		try {
			session = getSession();
			Assert.assertNotNull(session, "session cannot be null");
			transaction = session.beginTransaction();
			Assert.assertNotNull(transaction, "transaction cannot be null");

			// check if function is used by document by querying SBI_OBJ_FUNCTION table
			ArrayList<BIObject> objectsAssociated = DAOFactory.getBIObjFunctionDAO().getBIObjectsUsingFunction(uuid, session);
			if (!objectsAssociated.isEmpty()) {
				for (Iterator iterator = objectsAssociated.iterator(); iterator.hasNext();) {
					BIObject biObject = (BIObject) iterator.next();
					logger.debug("Function with uuid " + uuid + " is used by BiObject with label " + biObject.getLabel());
				}
				String message = "[deleteInUseFunctionError]: Function with uuid [" + uuid + "] " + "cannot be deleted because it is referenced by documents";
				FunctionInUseException fiue = new FunctionInUseException(message);
				ArrayList<String> objs = new ArrayList<String>();
				for (int i = 0; i < objectsAssociated.size(); i++) {
					BIObject obj = objectsAssociated.get(i);
					objs.add(obj.getLabel());
				}
				fiue.setObjectsLabel(objs);
				throw fiue;
			}

			SbiCatalogFunction hibCatFunction = (SbiCatalogFunction) session.get(SbiCatalogFunction.class, uuid.toString());
			session.delete(hibCatFunction);

			transaction.commit();

		} catch (FunctionInUseException fiue) {
			throw fiue;
		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}

			throw new SpagoBIDAOException("An unexpected error occured while updating catalog function", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

	}

	@Override
	public SbiCatalogFunction getCatalogFunctionByUuid(String uuid) {

		Session session;
		Transaction transaction = null;
		SbiCatalogFunction sbiCatalogFunction = null;

		logger.debug("IN");

		session = null;
		try {

			session = getSession();
			Assert.assertNotNull(session, "session cannot be null");
			transaction = session.beginTransaction();
			sbiCatalogFunction = (SbiCatalogFunction) session.get(SbiCatalogFunction.class, uuid.toString());
			transaction.commit();

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An error occured while reading Catalog Functions from DB", t);

		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return sbiCatalogFunction;

	}

	@Override
	public String getCatalogFunctionScriptByUuidAndOrganization(String uuid, String organization) {
		String scriptToReturn;
		Session session;
		Transaction transaction = null;
		SbiCatalogFunction sbiCatalogFunction = null;

		logger.debug("IN");

		session = null;
		try {
			session = getSession();
			Assert.assertNotNull(session, "session cannot be null");
			transaction = session.beginTransaction();
			Query q = session.createQuery("select s.onlineScript from SbiCatalogFunction s where s.functionUuid = ? and organization = ?");
			q.setString(0, uuid);
			q.setString(1, organization);
			scriptToReturn = (String) q.uniqueResult();
			transaction.commit();
		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An error occured while reading Catalog Functions from DB", t);

		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return scriptToReturn;

	}

	@Override
	public SbiCatalogFunction getCatalogFunctionByLabel(String label) {

		Session session;
		Transaction transaction = null;
		SbiCatalogFunction sbiCatalogFunction = null;

		logger.debug("IN");

		session = null;
		try {

			session = getSession();
			Assert.assertNotNull(session, "session cannot be null");
			transaction = session.beginTransaction();

			String hql = "FROM SbiCatalogFunction F WHERE F.label = ?";
			Query query = session.createQuery(hql);
			query.setString(0, label);
			sbiCatalogFunction = (SbiCatalogFunction) query.uniqueResult();

			transaction.commit();

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An error occured while reading Catalog Functions from DB", t);

		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return sbiCatalogFunction;

	}
}
