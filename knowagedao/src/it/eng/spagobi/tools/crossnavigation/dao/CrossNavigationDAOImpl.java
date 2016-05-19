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

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.OutputParameter;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjPar;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.crossnavigation.bo.NavigationDetail;
import it.eng.spagobi.tools.crossnavigation.bo.SimpleNavigation;
import it.eng.spagobi.tools.crossnavigation.bo.SimpleParameter;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiCrossNavigation;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiCrossNavigationPar;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiOutputParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
					if (cn.getSbiCrossNavigationPars() != null && !cn.getSbiCrossNavigationPars().isEmpty()) {
						SbiCrossNavigationPar cnp = cn.getSbiCrossNavigationPars().iterator().next();
						sn.setToDoc(cnp.getToKey().getLabel());
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
					throw new SpagoBIDOAException("Write error: record not valid");
				}
				SbiCrossNavigation cn = new SbiCrossNavigation();
				cn.setName(nd.getSimpleNavigation().getName());
				cn.setSbiCrossNavigationPars(new HashSet<SbiCrossNavigationPar>());
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
					throw new SpagoBIDOAException("Write error: record not found");
				}
				cn.setName(nd.getSimpleNavigation().getName());
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
					throw new SpagoBIDOAException("Object of type SbiCrossNavigation with id[" + id + "] not found");
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
							if (fromDoc == null) {
								fromDoc = inputParameter.getSbiObject();
							}
							break;
						case TYPE_OUTPUT:
							SbiOutputParameter outputParameter = (SbiOutputParameter) session.get(SbiOutputParameter.class, cnp.getFromKeyId());
							fromSp.setName(outputParameter.getLabel());
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
//							nd.getFromPars().add(new SimpleParameter(op.getObjParId(), op.getLabel(), TYPE_INPUT));
							checkAndAddToList(nd.getFromPars(), new SimpleParameter(op.getObjParId(), op.getLabel(), TYPE_INPUT));
						}
						List outputParameterList = session.createCriteria(SbiOutputParameter.class).add(Restrictions.eq("biobjId", fromDoc.getBiobjId()))
								.list();
						for (Object object : outputParameterList) {
							SbiOutputParameter outPar = (SbiOutputParameter) object;
//							nd.getFromPars().add(new SimpleParameter(outPar.getId(), outPar.getLabel(), TYPE_OUTPUT));
							checkAndAddToList(nd.getFromPars(), new SimpleParameter(outPar.getId(), outPar.getLabel(), TYPE_OUTPUT));
						}
						for (Object o : toDoc.getSbiObjPars()) {
							SbiObjPar op = (SbiObjPar) o;
//							nd.getToPars().add(new SimpleParameter(op.getObjParId(), op.getLabel(), TYPE_INPUT));
							checkAndAddToList(nd.getToPars(), new SimpleParameter(op.getObjParId(), op.getLabel(), TYPE_INPUT));
						}

						int i = nd.getToPars().indexOf(toSp);
						if (i < 0) {
							nd.getToPars().add(fromSp);
						} else {
							nd.getToPars().get(i).getLinks().clear();
							nd.getToPars().get(i).getLinks().add(fromSp);
						}
					}

					nd.setSimpleNavigation(new SimpleNavigation(cn.getId(), cn.getName(), fromDoc.getLabel(), fromDoc.getBiobjId(), toDoc.getLabel()));

				}
				return nd;
			}
		});
	}
	
	private void checkAndAddToList(List list, Object obj){
		if(!list.contains(obj)) {
			list.add(obj);
		}
	}

	@Override
	public void delete(Integer id) {
		delete(SbiCrossNavigation.class, id);
	}

	@Override
	public JSONArray loadNavigationByDocument(final String label) {

		return executeOnTransaction(new IExecuteOnTransaction<JSONArray>() {
			@Override
			public JSONArray execute(Session session) throws JSONException, EMFUserError {
				JSONArray ret = new JSONArray();
				// JSONArray inputParametersList = new JSONArray();
				// JSONArray outputParametersList = new JSONArray();

				Map<Integer, crossNavigationParameters> documentIOParams = new HashMap<Integer, crossNavigationParameters>();

				List<Integer> inputId = new ArrayList<>();
				List<Integer> outputId = new ArrayList<>();

				BIObject document = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(label);
				if (document == null) {
					throw new RuntimeException("Impossible to get document [" + label + "] from SpagoBI Server");
				}

				// load Input Parameter
				List objParams = document.getBiObjectParameters();
				for (Iterator iterator = objParams.iterator(); iterator.hasNext();) {
					JSONObject paramJSON = new JSONObject();
					BIObjectParameter param = (BIObjectParameter) iterator.next();
					// paramJSON.put("label", param.getLabel());
					// paramJSON.put("url", param.getParameterUrlName());
					// inputParametersList.put(paramJSON);
					inputId.add(param.getId());
					documentIOParams.put(param.getId(), new crossNavigationParameters(param.getParameterUrlName(), param.getParameter().getType()));
				}

				// Load Output Parameter

				List<OutputParameter> lst = document.getOutputParameters();
				for (OutputParameter upitem : lst) {
					// inputParametersList.put(JsonConverter.objectToJson(upitem, upitem.getClass()));
					outputId.add(upitem.getId());
					documentIOParams.put(upitem.getId(), new crossNavigationParameters(upitem.getName(), upitem.getType(), upitem.getFormatValue()));
				}

				// if (!inputId.isEmpty() || !outputId.isEmpty()) {
				// load cross navigation item
				Disjunction disj = Restrictions.disjunction();
				if (!inputId.isEmpty()) {
					disj.add(Restrictions.conjunction().add(Restrictions.eq("fromType", 1)).add(Restrictions.in("fromKeyId", inputId)));
				}
				if (!outputId.isEmpty()) {
					disj.add(Restrictions.conjunction().add(Restrictions.eq("fromType", 0)).add(Restrictions.in("fromKeyId", outputId)));
				}
				disj.add(Restrictions.conjunction().add(Restrictions.eq("fromType", 2)).add(Restrictions.eq("fromKeyId", document.getId())));

				Criteria crit = session.createCriteria(SbiCrossNavigationPar.class).add(disj);

				List<SbiCrossNavigationPar> scn = crit.list();

				// from cross navigation item get the document whith input params like cross navigation toKeyId value in imput params
				Map<Integer, JSONObject> mappa = new HashMap<Integer, JSONObject>();
				for (SbiCrossNavigationPar cnItem : scn) {
					SbiObjects obj = cnItem.getToKey().getSbiObject();
					if (!mappa.containsKey(obj.getBiobjId())) {
						JSONObject tmpJO = new JSONObject();
						BIObject bio = DAOFactory.getBIObjectDAO().toBIObject(obj, session);
						tmpJO.put("document", new JSONObject(JsonConverter.objectToJson(bio, bio.getClass())));
						tmpJO.put("documentId", obj.getBiobjId());
						tmpJO.put("navigationParams", new JSONObject());

						mappa.put(obj.getBiobjId(), tmpJO);
					}

					JSONObject jo = mappa.get(obj.getBiobjId());
					JSONObject fromItem = new JSONObject();

					if (documentIOParams.get(cnItem.getFromKeyId()) == null) {
						fromItem.put("value", cnItem.getFixedValue());
						fromItem.put("fixed", true);
					} else {
						fromItem.put("value",
								new JSONObject(JsonConverter.objectToJson(documentIOParams.get(cnItem.getFromKeyId()), crossNavigationParameters.class)));
						fromItem.put("fixed", false);
					}

					jo.getJSONObject("navigationParams").put(cnItem.getToKey().getParurlNm(), fromItem);

				}

				Iterator it = mappa.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pair = (Map.Entry) it.next();
					ret.put(pair.getValue());
				}

				// }
				return ret;
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
}

class crossNavigationParameters {
	String label;
	Domain type;
	String inputParameterType;
	String dateFormat;

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
	 * @param label
	 *            the label to set
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
	 * @param type
	 *            the type to set
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
	 * @param dateFormat
	 *            the dateFormat to set
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
	 * @param inputParameterType
	 *            the inputParameterType to set
	 */
	public void setInputParameterType(String inputParameterType) {
		this.inputParameterType = inputParameterType;
	}

}
