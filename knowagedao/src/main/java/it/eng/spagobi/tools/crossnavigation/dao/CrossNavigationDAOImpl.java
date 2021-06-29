/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.tools.crossnavigation.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.OutputParameter;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjPar;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.crossnavigation.bo.NavigationDetail;
import it.eng.spagobi.tools.crossnavigation.bo.SimpleNavigation;
import it.eng.spagobi.tools.crossnavigation.bo.SimpleParameter;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiCrossNavigation;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiCrossNavigationPar;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiOutputParameter;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class CrossNavigationDAOImpl extends AbstractHibernateDAO implements ICrossNavigationDAO {

	private static Logger logger = Logger.getLogger(CrossNavigationDAOImpl.class);

	public static final int TYPE_FIXED = 2;
	public static final int TYPE_INPUT = 1;
	public static final int TYPE_OUTPUT = 0;

	@Override
	public List<SimpleNavigation> listNavigation() {
		final List<SimpleNavigation> lst = new ArrayList<>();
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws JSONException {
				Criteria c = session.createCriteria(SbiCrossNavigation.class).addOrder(Order.asc("name"));

				for (Object o : c.list()) {
					SbiCrossNavigation cn = (SbiCrossNavigation) o;
					SimpleNavigation sn = new SimpleNavigation();
					sn.setId(cn.getId());
					sn.setName(cn.getName());
					sn.setDescription(cn.getDescription());
					sn.setBreadcrumb(cn.getBreadcrumb());
					sn.setType(cn.getType());
					sn.setFromDocId(cn.getFromDocId());
					sn.setToDocId(cn.getToDocId());
					if (cn.getSbiCrossNavigationPars() != null && !cn.getSbiCrossNavigationPars().isEmpty()) {
						SbiCrossNavigationPar cnp = cn.getSbiCrossNavigationPars().iterator().next();
						sn.setToDoc(cnp.getToKey().getSbiObject().getLabel());
						switch (cnp.getFromType()) {
						case TYPE_INPUT:
							SbiObjPar op = (SbiObjPar) session.get(SbiObjPar.class, cnp.getFromKeyId());
							if (op == null) {
								logger.error("Cross Navigation Error! id[" + cn.getId() + "] - Input type parameter not found with fromKeyId ["
										+ cnp.getFromKeyId() + "]");
								continue;
							}
							sn.setFromDoc(op.getSbiObject().getLabel());
							break;
						case TYPE_OUTPUT:
							SbiOutputParameter o2 = (SbiOutputParameter) session.get(SbiOutputParameter.class, cnp.getFromKeyId());
							if (o2 == null) {
								logger.error("Cross Navigation Error! id[" + cn.getId() + "] - Output type parameter not found with fromKeyId ["
										+ cnp.getFromKeyId() + "]");
								continue;
							}
							sn.setFromDoc(o2.getSbiObject().getLabel());
							break;
						case TYPE_FIXED:
							SbiObjects obj = (SbiObjects) session.load(SbiObjects.class, cnp.getFromKeyId());
							if (obj == null) {
								logger.error("Cross Navigation Error! id[" + cn.getId() + "] - Document not found with id [" + cnp.getFromKeyId() + "]");
								continue;
							}
							sn.setFromDoc(obj.getLabel());
							break;
						}
					}
					lst.add(sn);
				}
				return Boolean.TRUE;
			}
		});

		return lst;
	}

	@Override
	public void insert(final NavigationDetail nd) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws JSONException {
				if (nd.getSimpleNavigation().getId() != null) {
					throw new SpagoBIDAOException("Write error: record not valid");
				}
				SbiCrossNavigation cn = new SbiCrossNavigation();
				cn.setName(nd.getSimpleNavigation().getName());
				cn.setDescription(nd.getSimpleNavigation().getDescription());
				cn.setBreadcrumb(nd.getSimpleNavigation().getBreadcrumb());
				cn.setType(nd.getSimpleNavigation().getType());
				cn.setFromDocId(nd.getSimpleNavigation().getFromDocId());
				cn.setToDocId(nd.getSimpleNavigation().getToDocId());
				cn.setSbiCrossNavigationPars(new HashSet<SbiCrossNavigationPar>());
				cn.setPopupOptions(nd.getSimpleNavigation().getPopupOptions());
				if (nd.getToPars() != null) {
					for (SimpleParameter sp : nd.getToPars()) {
						if (sp.getLinks() != null && !sp.getLinks().isEmpty()) {
							SbiCrossNavigationPar cnp = from(sp, cn);
							updateSbiCommonInfo4Insert(cnp);
							cn.getSbiCrossNavigationPars().add(cnp);
						}
					}
				}
				updateSbiCommonInfo4Insert(cn);
				session.save(cn);
				return Boolean.TRUE;
			}

		});
	}

	@Override
	public void update(final NavigationDetail nd) {
		executeOnTransaction(new IExecuteOnTransaction<Boolean>() {
			@Override
			public Boolean execute(Session session) throws JSONException {
				SbiCrossNavigation cn = (SbiCrossNavigation) session.get(SbiCrossNavigation.class, nd.getSimpleNavigation().getId());
				if (cn == null) {
					throw new SpagoBIDAOException("Write error: record not found");
				}
				cn.setName(nd.getSimpleNavigation().getName());
				cn.setDescription(nd.getSimpleNavigation().getDescription());
				cn.setBreadcrumb(nd.getSimpleNavigation().getBreadcrumb());
				cn.setType(nd.getSimpleNavigation().getType());
				cn.setPopupOptions(nd.getSimpleNavigation().getPopupOptions());
				cn.setFromDocId(nd.getSimpleNavigation().getFromDocId());
				cn.setToDocId(nd.getSimpleNavigation().getToDocId());
				if (cn.getSbiCrossNavigationPars() != null) {
					cn.getSbiCrossNavigationPars().clear();
				} else {
					cn.setSbiCrossNavigationPars(new HashSet<SbiCrossNavigationPar>());
				}
				if (nd.getToPars() != null) {
					for (SimpleParameter sp : nd.getToPars()) {
						if (sp.getLinks() != null && !sp.getLinks().isEmpty()) {
							SbiCrossNavigationPar cnp = from(sp, cn);
							updateSbiCommonInfo4Update(cnp);
							cn.getSbiCrossNavigationPars().add(cnp);
						}
					}
				}
				updateSbiCommonInfo4Update(cn);
				session.update(cn);
				return Boolean.TRUE;
			}
		});
	}

	@Override
	public NavigationDetail loadNavigation(final Integer id) {

		return executeOnTransaction(new IExecuteOnTransaction<NavigationDetail>() {
			@Override
			public NavigationDetail execute(Session session) throws JSONException {
				SbiCrossNavigation cn = (SbiCrossNavigation) session.get(SbiCrossNavigation.class, id);
				if (cn == null) {
					throw new SpagoBIDAOException("Object of type SbiCrossNavigation with id[" + id + "] not found");
				}
				NavigationDetail nd = new NavigationDetail();
				if (cn.getSbiCrossNavigationPars() != null && !cn.getSbiCrossNavigationPars().isEmpty()) {
					// All parameters belong to two docs only
					SbiObjects fromDoc = null;
					SbiObjects toDoc = null;
					for (SbiCrossNavigationPar cnp : cn.getSbiCrossNavigationPars()) {
						SimpleParameter fromSp = new SimpleParameter();
						fromSp.setId(cnp.getFromKeyId());
						fromSp.setType(cnp.getFromType());
						switch (cnp.getFromType().intValue()) {
						case TYPE_INPUT:
							SbiObjPar inputParameter = (SbiObjPar) session.get(SbiObjPar.class, cnp.getFromKeyId());
							fromSp.setName(inputParameter.getLabel());
							fromSp.setParType(inputParameter.getSbiParameter().getParameterTypeCode());
							if (fromDoc == null) {
								fromDoc = inputParameter.getSbiObject();
							}
							break;
						case TYPE_OUTPUT:
							SbiOutputParameter outputParameter = (SbiOutputParameter) session.get(SbiOutputParameter.class, cnp.getFromKeyId());
							fromSp.setName(outputParameter.getLabel());
							fromSp.setParType(outputParameter.getParameterType().getValueCd());
							if (fromDoc == null) {
								fromDoc = outputParameter.getSbiObject();
							}
							break;
						case TYPE_FIXED:
							fromSp.setFixedValue(cnp.getFixedValue());
							fromSp.setName(cnp.getFixedValue());
							if (fromDoc == null) {
								fromDoc = (SbiObjects) session.get(SbiObjects.class, cnp.getFromKeyId());
							}
							break;
						}
						SimpleParameter toSp = new SimpleParameter(cnp.getToKey().getObjParId(), cnp.getToKey().getLabel(), TYPE_INPUT);
						if (toDoc == null) {
							toDoc = cnp.getToKey().getSbiObject();
						}
						for (Object o : fromDoc.getSbiObjPars()) {
							SbiObjPar op = (SbiObjPar) o;
							// nd.getFromPars().add(new SimpleParameter(op.getObjParId(), op.getLabel(), TYPE_INPUT));
							checkAndAddToList(nd.getFromPars(),
									new SimpleParameter(op.getObjParId(), op.getLabel(), TYPE_INPUT, op.getSbiParameter().getParameterTypeCode()));
						}
						List outputParameterList = session.createCriteria(SbiOutputParameter.class).add(Restrictions.eq("biobjId", fromDoc.getBiobjId()))
								.list();
						for (Object object : outputParameterList) {
							SbiOutputParameter outPar = (SbiOutputParameter) object;
							// nd.getFromPars().add(new SimpleParameter(outPar.getId(), outPar.getLabel(), TYPE_OUTPUT));
							checkAndAddToList(nd.getFromPars(),
									new SimpleParameter(outPar.getId(), outPar.getLabel(), TYPE_OUTPUT, outPar.getParameterType().getValueCd()));
						}
						for (Object o : toDoc.getSbiObjPars()) {
							SbiObjPar op = (SbiObjPar) o;
							// nd.getToPars().add(new SimpleParameter(op.getObjParId(), op.getLabel(), TYPE_INPUT));
							checkAndAddToList(nd.getToPars(),
									new SimpleParameter(op.getObjParId(), op.getLabel(), TYPE_INPUT, op.getSbiParameter().getParameterTypeCode()));
						}

						int i = nd.getToPars().indexOf(toSp);
						if (i < 0) {
							nd.getToPars().add(fromSp);
						} else {
							nd.getToPars().get(i).getLinks().clear();
							nd.getToPars().get(i).getLinks().add(fromSp);
						}
					}

					nd.setSimpleNavigation(new SimpleNavigation(cn.getId(), cn.getName(), cn.getDescription(), cn.getBreadcrumb(), cn.getType(),
							fromDoc.getLabel(), fromDoc.getBiobjId(), toDoc.getLabel(), toDoc.getBiobjId(), cn.getPopupOptions()));

				}
				return nd;
			}
		});
	}

	private void checkAndAddToList(List list, Object obj) {
		if (!list.contains(obj)) {
			list.add(obj);
		}
	}

	@Override
	public void delete(Integer id) {
		delete(SbiCrossNavigation.class, id);
	}

	@Override
	public void deleteByDocument(BIObject obj, Session session) {
		List<Integer> inputParameters = new ArrayList<>();
		if (obj.getDrivers() != null) {
			for (BIObjectParameter biObjectParameter : obj.getDrivers()) {
				inputParameters.add(biObjectParameter.getId());
			}
		}
		List<Integer> outputParameters = new ArrayList<>();
		if (obj.getOutputParameters() != null) {
			for (OutputParameter outputParameter : obj.getOutputParameters()) {
				outputParameters.add(outputParameter.getId());
			}
		}
		List<SbiCrossNavigation> cnToRemove = listNavigationsByDocumentAndParameters(obj.getId(), inputParameters, outputParameters, session);
		for (SbiCrossNavigation cn : cnToRemove) {
			session.delete(cn);
		}
	}

	@Override
	public List<SbiCrossNavigation> listNavigationsByDocumentAndParameters(Integer documentId, List<Integer> inputParameters, List<Integer> outputParameters,
			Session session) {
		// load cross navigation item
		Disjunction disj = Restrictions.disjunction();
		if (!inputParameters.isEmpty()) {
			disj.add(Restrictions.conjunction().add(Restrictions.eq("_par.fromType", 1)).add(Restrictions.in("_par.fromKeyId", inputParameters)));
			disj.add(Restrictions.in("_par.toKeyId", inputParameters));
		}
		if (!outputParameters.isEmpty()) {
			disj.add(Restrictions.conjunction().add(Restrictions.eq("_par.fromType", 0)).add(Restrictions.in("_par.fromKeyId", outputParameters)));
		}
		disj.add(Restrictions.conjunction().add(Restrictions.eq("_par.fromType", 2)).add(Restrictions.eq("_par.fromKeyId", documentId)));

		List ret = session.createCriteria(SbiCrossNavigation.class).createAlias("sbiCrossNavigationPars", "_par").add(disj).list();
		return ret;
	}

	@Override
	public JSONArray loadNavigationByDocument(final String label) {

		return executeOnTransaction(new IExecuteOnTransaction<JSONArray>() {
			@Override
			public JSONArray execute(Session session) throws JSONException, EMFUserError {
				BIObject document = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(label);
				if (document == null) {
					throw new RuntimeException("Unable to get document with label [" + label + "]");
				}

				// load Input Parameter
				Map<Integer, crossNavigationParameters> documentInputParams = new HashMap<Integer, crossNavigationParameters>();
				List docParams = document.getDrivers();
				for (Iterator iterator = docParams.iterator(); iterator.hasNext();) {
					BIObjectParameter docParam = (BIObjectParameter) iterator.next();
					crossNavigationParameters cnParams = new crossNavigationParameters(docParam.getParameterUrlName(), docParam.getParameter().getType());
					cnParams.setIsInput(true);
					documentInputParams.put(docParam.getId(), cnParams);
				}

				// Load Output Parameter
				Map<Integer, crossNavigationParameters> documentOutputParams = new HashMap<Integer, crossNavigationParameters>();
				List<OutputParameter> outParams = document.getOutputParameters();
				for (OutputParameter outParam : outParams) {
					crossNavigationParameters cnParams = new crossNavigationParameters(outParam.getName(), outParam.getType(), outParam.getFormatValue());
					cnParams.setIsInput(false);
					documentOutputParams.put(outParam.getId(), cnParams);
				}

				// load cross navigation parameters
				Disjunction disjunction = Restrictions.disjunction();
				if (!documentInputParams.isEmpty()) {
					disjunction.add(
							Restrictions.conjunction().add(Restrictions.eq("fromType", 1)).add(Restrictions.in("fromKeyId", documentInputParams.keySet())));
				}
				if (!documentOutputParams.isEmpty()) {
					disjunction.add(
							Restrictions.conjunction().add(Restrictions.eq("fromType", 0)).add(Restrictions.in("fromKeyId", documentOutputParams.keySet())));
				}
				disjunction.add(Restrictions.conjunction().add(Restrictions.eq("fromType", 2)).add(Restrictions.eq("fromKeyId", document.getId())));
				Criteria crit = session.createCriteria(SbiCrossNavigationPar.class).add(disjunction);
				List<SbiCrossNavigationPar> cnParams = crit.list();

				Map<Integer, JSONObject> validCrossNavIdToCrossNavJSON = new HashMap<Integer, JSONObject>(); // valid cross nav id --> cross nav info in JSON
				List<Integer> nonValidCrossNavIds = new ArrayList<Integer>(); // list of non valid cross nav id

				for (SbiCrossNavigationPar cnParam : cnParams) {
					// from cross navigation item get the document with input params like cross navigation toKeyId value in input params
					Integer crossId = cnParam.getSbiCrossNavigation().getId();

					if (nonValidCrossNavIds.contains(crossId)) {
						// cross navigation already examined and found to be invalid, skip it
						continue;
					}

					if (!validCrossNavIdToCrossNavJSON.containsKey(crossId)) {
						// we don't know if this cross navigation is valid or not, let's discover it...
						SbiObjects sbiObj = cnParam.getToKey().getSbiObject();
						BIObject biObject = DAOFactory.getBIObjectDAO().toBIObject(sbiObj, session);
						UserProfile userProfile = UserProfileManager.getProfile();
						boolean canExecute = false;
						try {
							canExecute = ObjectsAccessVerifier.canExec(biObject, userProfile);
						} catch (EMFInternalError e) {
							throw new SpagoBIRuntimeException("Error while trying to see if user can execute the target document [" + biObject.getLabel() + "]",
									e);
						}

						if (canExecute) {
							JSONObject jsonCnParam = new JSONObject();
							jsonCnParam.put("document", new JSONObject(JsonConverter.objectToJson(biObject, biObject.getClass())));
							jsonCnParam.put("documentId", sbiObj.getBiobjId());
							jsonCnParam.put("crossName", cnParam.getSbiCrossNavigation().getName());
							jsonCnParam.put("crossText", cnParam.getSbiCrossNavigation().getDescription());
							jsonCnParam.put("crossBreadcrumb", cnParam.getSbiCrossNavigation().getBreadcrumb());
							jsonCnParam.put("crossType", cnParam.getSbiCrossNavigation().getType());
							jsonCnParam.put("popupOptions", cnParam.getSbiCrossNavigation().getPopupOptions());
							jsonCnParam.put("crossId", crossId);
							jsonCnParam.put("navigationParams", new JSONObject());
							validCrossNavIdToCrossNavJSON.put(crossId, jsonCnParam);
						} else {
							// user cannot execute target document, we put it in a list to avoid further iterations on it
							logger.debug("User " + userProfile.getUserId() + " cannot execute document " + biObject.getLabel()
									+ ", skipping relevant cross navigation option.");
							nonValidCrossNavIds.add(crossId);
							continue;
						}

					}

					JSONObject jsonCnParam = validCrossNavIdToCrossNavJSON.get(crossId);

					JSONObject jsonNavParam = new JSONObject();

					Integer fromKeyId = cnParam.getFromKeyId();
					int type = cnParam.getFromType().intValue();
					switch (type) {
					case 0:
						jsonNavParam.put("value",
								new JSONObject(JsonConverter.objectToJson(documentOutputParams.get(fromKeyId), crossNavigationParameters.class)));
						jsonNavParam.put("fixed", false);
						break;
					case 1:
						jsonNavParam.put("value",
								new JSONObject(JsonConverter.objectToJson(documentInputParams.get(fromKeyId), crossNavigationParameters.class)));
						jsonNavParam.put("fixed", false);
						break;
					case 2:
						jsonNavParam.put("value", cnParam.getFixedValue());
						jsonNavParam.put("fixed", true);
						break;
					default:
						throw new SpagoBIRuntimeException("Unsupported cross navigation type [" + type + "]");
					}

					jsonCnParam.getJSONObject("navigationParams").put(cnParam.getToKey().getParurlNm(), jsonNavParam);

				}

				JSONArray results = new JSONArray();
				for (JSONObject jsonDoc : validCrossNavIdToCrossNavJSON.values()) {
					results.put(jsonDoc);
				}
				return results;
			}
		});
	}

	@Override
	public boolean documentIsCrossable(String docLabel) {
		if (docLabel == null || docLabel.equals("")) {
			return false;
		} else {
			try {
				JSONArray ja = loadNavigationByDocument(docLabel);
				return ja.length() > 0;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	private SbiCrossNavigationPar from(SimpleParameter sp, SbiCrossNavigation cn) {
		SbiCrossNavigationPar cnp = new SbiCrossNavigationPar();
		SimpleParameter linkedParam = sp.getLinks().get(0);
		cnp.setFromKeyId(linkedParam.getId());
		cnp.setFromType(linkedParam.getType());
		cnp.setToKeyId(sp.getId());
		if (linkedParam.getType().equals(TYPE_FIXED)) {
			cnp.setFixedValue(linkedParam.getFixedValue());
		}
		cnp.setSbiCrossNavigation(cn);
		return cnp;
	}

	@Override
	public void deleteByBIObjectParameter(BIObjectParameter biObjectParameter, Session session) {

		List<SbiCrossNavigationPar> cnParToRemove = listNavigationsByInputParameters(biObjectParameter.getId(), session);
		List<Integer> crossNavigation = new ArrayList<Integer>();
		// Delete FROM CROSS_NAVIFATION_PAR
		for (SbiCrossNavigationPar cn : cnParToRemove) {
			session.delete(cn);
			if (!crossNavigation.contains(cn.getSbiCrossNavigation().getId()))
				crossNavigation.add(cn.getSbiCrossNavigation().getId());
		}
		// Delete FROM CROSS_NAVIGATION
		for (Integer scnId : crossNavigation) {
			List<SbiCrossNavigationPar> sbiCrossPar = listNavigationsByCrossNavParId(scnId, session);
			if (sbiCrossPar == null || sbiCrossPar.size() == 0) {
				SbiCrossNavigation snc = loadSbiCrossNavigationById(scnId, session);
				session.delete(snc);
			}

		}

	}

	@Override
	public List<SbiCrossNavigationPar> listNavigationsByInputParameters(Integer paramId) {
		return listNavigationsByInputParameters(paramId, getSession());
	}

	@Override
	public List<SbiCrossNavigationPar> listNavigationsByInputParameters(Integer paramId, Session session) {
		// return session.createCriteria(SbiCrossNavigationPar.class).add(Restrictions.eq("toKeyId", paramId)).list();
		Session aSession = session;
		return aSession.createCriteria(SbiCrossNavigationPar.class).add(
				Restrictions.or(Restrictions.eq("toKeyId", paramId), Restrictions.and(Restrictions.eq("fromKeyId", paramId), Restrictions.eq("fromType", 1))))
				.list();

	}

	@Override
	public List listNavigationsByAnalyticalDriverID(Integer analyticalDriverId) {
		return listNavigationsByAnalyticalDriverID(analyticalDriverId, getSession());
	}

	@Override
	public List listNavigationsByAnalyticalDriverID(Integer analyticalDriverId, Session session) {
		// return session.createCriteria(SbiCrossNavigationPar.class).add(Restrictions.eq("toKeyId", paramId)).list();
		Session aSession = session;

		StringBuilder sb = new StringBuilder();
		sb.append(" select t1");
		sb.append(" from SbiCrossNavigation t, SbiCrossNavigationPar t1, SbiObjPar t2");
		sb.append(" where t.id=t1.sbiCrossNavigation.id");
		sb.append(" and t1.toKey=t2.objParId");
		sb.append(" and t2.sbiParameter.parId=" + analyticalDriverId);

		Query hibQuery = session.createQuery(sb.toString());
		List hibList = hibQuery.list();

		return hibList;

	}

	@Override
	public List<SbiCrossNavigationPar> listNavigationsByOutputParameters(Integer paramId) {
		return listNavigationsByOutputParameters(paramId, getSession());
	}

	@Override
	public List<SbiCrossNavigationPar> listNavigationsByOutputParameters(Integer paramId, Session session) {
		// return session.createCriteria(SbiCrossNavigationPar.class).add(Restrictions.eq("toKeyId", paramId)).list();
		Session aSession = session;
		return aSession.createCriteria(SbiCrossNavigationPar.class).add(
				Restrictions.or(Restrictions.eq("toKeyId", paramId), Restrictions.and(Restrictions.eq("fromKeyId", paramId), Restrictions.eq("fromType", 0))))
				.list();

	}

	@Override
	public List<SbiCrossNavigationPar> listNavigationsByCrossNavParId(Integer crossNavId, Session session) {
		return session.createCriteria(SbiCrossNavigationPar.class).add(Restrictions.eq("sbiCrossNavigation.id", crossNavId)).list();
	}

	@Override
	public SbiCrossNavigation loadSbiCrossNavigationById(Integer id, Session session) {
		return (SbiCrossNavigation) session.createCriteria(SbiCrossNavigation.class).add(Restrictions.eq("id", id)).uniqueResult();
	}
}

class crossNavigationParameters {
	String label;
	Domain type;
	String inputParameterType;
	String dateFormat;
	Boolean isInput;

	/**
	 * @param label
	 * @param type
	 * @param dateFormat
	 */
	public crossNavigationParameters(String label, Domain type, String dateFormat) {
		super();
		this.label = label;
		this.type = type;
		this.dateFormat = dateFormat;
	}

	/**
	 * @param label
	 * @param type
	 */
	public crossNavigationParameters(String label, Domain type) {
		super();
		this.label = label;
		this.type = type;
	}

	/**
	 * @param label
	 * @param inputParameterType
	 */
	public crossNavigationParameters(String label, String inputParameterType) {
		super();
		this.label = label;
		this.inputParameterType = inputParameterType;
	}

	/**
	 * @param label
	 * @param type
	 */
	public crossNavigationParameters(String label) {
		super();
		this.label = label;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the type
	 */
	public Domain getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Domain type) {
		this.type = type;
	}

	/**
	 * @return the dateFormat
	 */
	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 * @param dateFormat the dateFormat to set
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * @return the inputParameterType
	 */
	public String getInputParameterType() {
		return inputParameterType;
	}

	/**
	 * @param inputParameterType the inputParameterType to set
	 */
	public void setInputParameterType(String inputParameterType) {
		this.inputParameterType = inputParameterType;
	}

	public Boolean getIsInput() {
		return isInput;
	}

	public void setIsInput(Boolean isInput) {
		this.isInput = isInput;
	}

}
