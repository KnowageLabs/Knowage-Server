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
package it.eng.spagobi.mapcatalogue.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;
import it.eng.spagobi.mapcatalogue.metadata.SbiGeoLayers;
import it.eng.spagobi.mapcatalogue.metadata.SbiGeoLayersRoles;

public class SbiGeoLayersDAOHibImpl extends AbstractHibernateDAO implements ISbiGeoLayersDAO {

	/**
	 * Load layer by id.
	 *
	 * @param layerID
	 *            the layer id
	 *
	 * @return the geo layer
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 * @throws UnsupportedEncodingException
	 * @throws JSONException
	 *
	 * @see it.eng.spagobi.mapcatalogue.dao.geo.bo.dao.ISbiGeoLayersDAO#loadLayerByID(integer)
	 */
	@Override
	public GeoLayer loadLayerByID(Integer layerID) throws EMFUserError {
		GeoLayer toReturn = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			SbiGeoLayers hibLayer = (SbiGeoLayers) tmpSession.load(SbiGeoLayers.class, layerID);
			toReturn = hibLayer.toGeoLayer();

			String resourcePath = SpagoBIUtilities.getResourcePath();
			if (toReturn.getPathFile().startsWith(resourcePath)) {
				// biLayer.setPathFile(biLayer.getPathFile());
			} else {
				toReturn.setPathFile(resourcePath + File.separator + toReturn.getPathFile());
			}
			tx.commit();

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();

			}
		}

		return toReturn;
	}

	/**
	 * Load layer by name.
	 *
	 * @param name
	 *            the name
	 *
	 * @return the geo layer
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.mapcatalogue.dao.geo.bo.dao.ISbiGeoLayersDAO#loadLayerByName(string)
	 */
	@Override
	public GeoLayer loadLayerByLabel(String label) throws EMFUserError {
		GeoLayer biLayer = null;
		Session tmpSession = null;
		Transaction tx = null;

		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion labelCriterrion = Expression.eq("label", label);
			Criteria criteria = tmpSession.createCriteria(SbiGeoLayers.class);
			criteria.add(labelCriterrion);
			SbiGeoLayers hibLayer = (SbiGeoLayers) criteria.uniqueResult();
			if (hibLayer == null)
				return null;
			biLayer = hibLayer.toGeoLayer();

			String resourcePath = SpagoBIUtilities.getResourcePath();
			if (biLayer.getPathFile().startsWith(resourcePath)) {
				// biLayer.setPathFile(biLayer.getPathFile());
			} else {
				biLayer.setPathFile(resourcePath + File.separator + biLayer.getPathFile());
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
		return biLayer;
	}

	/**
	 * Modify layer.
	 *
	 * @param aLayer
	 *            the a layer
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 * @throws JSONException
	 * @throws UnsupportedEncodingException
	 *
	 * @see it.eng.spagobi.geo.bo.dao.IEngineDAO#modifyEngine(it.eng.spagobi.bo.Engine)
	 */
	@Override
	public void modifyLayer(GeoLayer aLayer, Boolean modified) throws EMFUserError, JSONException, UnsupportedEncodingException {

		Session tmpSession = null;
		Transaction tx = null;
		JSONObject layerDef = new JSONObject();
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiGeoLayers hibLayer = (SbiGeoLayers) tmpSession.load(SbiGeoLayers.class, new Integer(aLayer.getLayerId()));
			hibLayer.setName(aLayer.getName());
			if (aLayer.getDescr() == null) {
				aLayer.setDescr("");
			}
			hibLayer.setDescr(aLayer.getDescr());
			hibLayer.setType(aLayer.getType());
			hibLayer.setLabel(aLayer.getLabel());
			hibLayer.setBaseLayer(aLayer.isBaseLayer());
			if (aLayer.getCategory_id() == null) {
				hibLayer.setCategory(null);
				hibLayer.setCategory_id(null);

			} else {
				hibLayer.setCategory(aLayer.getCategory());
				hibLayer.setCategory_id(aLayer.getCategory_id());
			}
			String path = null;
			if (modified) {
				if (aLayer.getPathFile() != null) {
					String separator = "";
					if (!aLayer.getPathFile().endsWith("" + File.separatorChar)) {
						separator += File.separatorChar;
					}

					path = aLayer.getPathFile() + separator + getTenant() + File.separator + "Layer" + File.separator;
					aLayer.setPathFile(path + aLayer.getLabel());

				} else {
					aLayer.setPathFile(null);

				}

				// save file on server//

				try {
					if (aLayer.getPathFile() != null) {
						new File(path).mkdirs();
						OutputStreamWriter out;
						String name = aLayer.getLabel();
						out = new FileWriter(path + name);
						String content = new String(aLayer.getFilebody());
						content = content.replaceAll("\t", "").replaceAll("\n", "").replaceAll("\r", "");
						out.write(content);
						out.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			// insert properties in a field of layerDef
			layerDef.put("properties", aLayer.getProperties());

			// preparo il jsonObject da memorizzare in LayerDefinition
			layerDef.put("layerId", aLayer.getLayerIdentify());
			layerDef.put("layerLabel", aLayer.getLayerLabel());
			layerDef.put("layerName", aLayer.getLayerName());

			path = "Layer" + File.separator + aLayer.getLabel();
			layerDef.put("layer_file", path);
			if (aLayer.getLayerURL() != null) {
				layerDef.put("layer_url", aLayer.getLayerURL());
			} else {
				layerDef.put("layer_url", "null");
			}

			layerDef.put("layer_zoom", "null");
			layerDef.put("layer_cetral_point", "null");
			if (aLayer.getLayerParams() != null) {
				layerDef.put("layer_params", aLayer.getLayerParams());
			} else {
				layerDef.put("layer_params", "null");
			}
			if (aLayer.getLayerOptions() != null) {
				layerDef.put("layer_options", aLayer.getLayerOptions());
			} else {
				layerDef.put("layer_options", "null");
			}
			if (aLayer.getLayerOrder() != null) {
				layerDef.put("layer_order", aLayer.getLayerOrder());
			} else {
				layerDef.put("layer_order", "null");
			}

			hibLayer.setLayerDef(layerDef.toString().getBytes("utf-8"));
			updateSbiCommonInfo4Update(hibLayer);

			// delete all roles of layer
			// query hql
			String hql = "DELETE from SbiGeoLayersRoles a WHERE a.layer.id =" + aLayer.getLayerId();
			Query q = tmpSession.createQuery(hql);
			q.executeUpdate();

			// reload roles
			if (aLayer.getRoles() != null) {
				for (SbiExtRoles r : aLayer.getRoles()) {

					// add roles
					SbiGeoLayersRoles hibLayRol = new SbiGeoLayersRoles(aLayer.getLayerId(), r.getExtRoleId().intValue());
					updateSbiCommonInfo4Update(hibLayRol);
					tmpSession.save(hibLayRol);

				}
			}
			tx.commit();

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}

	}

	/**
	 * Insert layer.
	 *
	 * @param aLayer
	 *            the a layer
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 * @throws JSONException
	 * @throws IOException
	 * @see it.eng.spagobi.geo.bo.dao.IEngineDAO#insertEngine(it.eng.spagobi.bo.Engine)
	 */
	@Override
	public Integer insertLayer(GeoLayer aLayer) throws EMFUserError, JSONException, IOException {
		Session tmpSession = null;
		Transaction tx = null;
		Integer id;
		JSONObject layerDef = new JSONObject();
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			SbiGeoLayers hibLayer = new SbiGeoLayers();

			hibLayer.setName(aLayer.getName());
			if (aLayer.getDescr() == null) {
				aLayer.setDescr("");
			}
			hibLayer.setDescr(aLayer.getDescr());
			hibLayer.setType(aLayer.getType());
			hibLayer.setLabel(aLayer.getLabel());
			hibLayer.setBaseLayer(aLayer.isBaseLayer());
			hibLayer.setCategory_id(aLayer.getCategory_id());

			if (aLayer.getCategory_id() == null) {
				hibLayer.setCategory(null);
				hibLayer.setCategory_id(null);

			} else {
				hibLayer.setCategory(aLayer.getCategory());
			}
			String path = null;
			if (aLayer.getPathFile() != null) {

				String separator = "";
				if (!aLayer.getPathFile().endsWith("" + File.separatorChar)) {
					separator += File.separatorChar;
				}
				if (aLayer.getPathFile() == "") {
					path = "Layer" + File.separator;
				} else {
					path = aLayer.getPathFile() + File.separator + "Layer" + File.separator;
				}

				aLayer.setPathFile(path + aLayer.getLabel());

			} else {
				aLayer.setPathFile(null);

			}

			// save file on server//

			try {
				if (aLayer.getPathFile() != null) {
					String resourcePath = SpagoBIUtilities.getResourcePath();
					new File(resourcePath + File.separator + "Layer").mkdirs();
					OutputStreamWriter out;
					String name = aLayer.getLabel();
					out = new FileWriter(resourcePath + File.separator + "Layer" + File.separator + name);
					String content = new String(aLayer.getFilebody());
					content = content.replaceAll("\t", "").replaceAll("\n", "").replaceAll("\r", "");
					out.write(content);
					out.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			layerDef.put("layerId", aLayer.getLayerIdentify());
			layerDef.put("layerLabel", aLayer.getLayerLabel());
			layerDef.put("layerName", aLayer.getLayerName());
			layerDef.put("properties", "");

			if (aLayer.getPathFile() != null) {
				layerDef.put("layer_file", aLayer.getPathFile());

			} else {
				layerDef.put("layer_file", "null");
			}
			if (aLayer.getLayerURL() != null) {
				layerDef.put("layer_url", aLayer.getLayerURL());
			} else {
				layerDef.put("layer_url", "null");
			}

			layerDef.put("layer_zoom", "null");
			layerDef.put("layer_cetral_point", "null");
			if (aLayer.getLayerParams() != null) {
				layerDef.put("layer_params", aLayer.getLayerParams());
			} else {
				layerDef.put("layer_params", "null");
			}
			if (aLayer.getLayerOptions() != null) {
				layerDef.put("layer_options", aLayer.getLayerOptions());
			} else {
				layerDef.put("layer_options", "null");
			}
			if (aLayer.getLayerOrder() != null) {
				layerDef.put("layer_order", aLayer.getLayerOrder());
			} else {
				layerDef.put("layer_order", "null");
			}

			hibLayer.setLayerDef(layerDef.toString().getBytes("utf-8"));
			// save on db
			updateSbiCommonInfo4Insert(hibLayer);
			id = (Integer) tmpSession.save(hibLayer);

			// set roles choosen
			if (aLayer.getRoles() != null) {
				for (SbiExtRoles r : aLayer.getRoles()) {
					SbiGeoLayersRoles hibLayRol = new SbiGeoLayersRoles(id, r.getExtRoleId().intValue());
					updateSbiCommonInfo4Insert(hibLayRol);
					tmpSession.save(hibLayRol);

				}
			}

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
		return id;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<SbiExtRoles> listRolesFromId(final Object[] arr) {
		return list(new ICriterion() {
			@Override
			public Criteria evaluate(Session session) {
				Criteria c = session.createCriteria(SbiExtRoles.class);
				c.add(Restrictions.in("extRoleId", arr));
				return c;
			}
		});
	}

	@Override
	public ArrayList<String> getProperties(int layerId) {
		Session tmpSession = null;
		ArrayList<String> keys = new ArrayList<String>();
		try {
			tmpSession = getSession();
			tmpSession.beginTransaction();
			GeoLayer aLayer = loadLayerByID(layerId);
			JSONObject layerDef = new JSONObject(new String(aLayer.getLayerDef()));
			if (aLayer.getType().equals("WMS") || aLayer.getType().equals("Google") || aLayer.getType().equals("TMS") || aLayer.getType().equals("OSM")) {
				return new ArrayList<String>();
			}
			// load properties of file
			if (!layerDef.get("layer_file").equals("null")) {

				String resourcePath = SpagoBIUtilities.getResourcePath();
				if (aLayer.getPathFile().startsWith(resourcePath)) {
					// biLayer.setPathFile(biLayer.getPathFile());
				} else {
					aLayer.setPathFile(resourcePath + File.separator + aLayer.getPathFile());
				}
				File doc = new File(aLayer.getPathFile());
				URL path = doc.toURI().toURL();
				InputStream inputstream = path.openStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(inputstream));
				String c;
				JSONArray content = new JSONArray();

				do {
					c = br.readLine();
					if (c != null) {
						JSONObject obj = new JSONObject(c);
						content = obj.getJSONArray("features");
						for (int j = 0; j < content.length(); j++) {
							obj = content.getJSONObject(j).getJSONObject("properties");
							Iterator<String> it = obj.keys();
							while (it.hasNext()) {
								String key = it.next();
								if (obj.get(key).getClass().equals(key.getClass())) {
									if (!keys.contains(key)) {
										keys.add(key);
									}
								}
							}
						}
					}

				} while (c != null);
			}

			// load properties of wfs
			if (!(layerDef.get("layer_url").equals("null"))) {
				String urlDescribeFeature = getDescribeFeatureTypeURL(layerDef.getString("layer_url"));
				URL url = new URL(urlDescribeFeature);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();

				connection.setRequestMethod("GET");
				connection.setDoOutput(true);
				connection.setDoInput(true);
				connection.setRequestProperty("CetRequestProperty(ontent-Type", "application/json");
				connection.setRequestProperty("Accept", "application/json");
				connection.connect();

				int HttpResult = connection.getResponseCode();
				if (HttpResult == HttpURLConnection.HTTP_OK) {

					BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
					String inputLine;
					while ((inputLine = br.readLine()) != null) {
						JSONObject obj = new JSONObject(inputLine);
						JSONArray content = (JSONArray) obj.get("featureTypes");
						content.getJSONObject(0);
						for (int j = 0; j < content.length(); j++) {
							JSONArray arr = content.getJSONObject(j).getJSONArray("properties");
							for (int k = 0; k < arr.length(); k++) {

								JSONObject val = arr.getJSONObject(k);
								if (!keys.contains(val.get("name")) && "string".equals(val.optString("localType"))) {
									keys.add(val.getString("name"));
								}
							}
						}
					}
					br.close();
					connection.disconnect();
				} else {
					System.out.println(connection.getResponseMessage());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
		return keys;
	}

	/**
	 * Erase layer.
	 *
	 * @param aLayer
	 *            the a layer
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.geo.bo.dao.IEngineDAO#eraseEngine(it.eng.spagobi.bo.Engine)
	 */

	@Override
	public void eraseLayer(Integer layerId) throws EMFUserError, JSONException {

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			GeoLayer aLayer = loadLayerByID(layerId);
			SbiGeoLayers hibLayer = new SbiGeoLayers();
			hibLayer.setLayerId(aLayer.getLayerId());
			hibLayer.setLabel(aLayer.getLabel());
			hibLayer.setName(aLayer.getName());
			hibLayer.setDescr(aLayer.getDescr());
			hibLayer.setType(aLayer.getType());
			hibLayer.setLabel(aLayer.getLabel());
			hibLayer.setBaseLayer(aLayer.isBaseLayer());
			hibLayer.setCategory_id(aLayer.getCategory_id());

			tmpSession.delete(hibLayer);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
	}

	@Override
	public void eraseRole(Integer roleId, Integer layerId) throws EMFUserError {

		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			SbiGeoLayersRoles hibLayRol = new SbiGeoLayersRoles(layerId, roleId);

			tmpSession.delete(hibLayRol);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
	}

	@Override
	public JSONObject getContentforDownload(int layerId, String typeWFS) {
		Session tmpSession = null;

		JSONObject obj = null;
		try {
			tmpSession = getSession();

			GeoLayer aLayer = loadLayerByID(layerId);
			JSONObject layerDef = new JSONObject(new String(aLayer.getLayerDef()));

			if (!layerDef.get("layer_file").equals("null")) {
				String resourcePath = SpagoBIUtilities.getResourcePath();
				if (aLayer.getPathFile().startsWith(resourcePath)) {
					// biLayer.setPathFile(biLayer.getPathFile());
				} else {
					aLayer.setPathFile(resourcePath + File.separator + aLayer.getPathFile());
				}
				File doc = new File(aLayer.getPathFile());
				URL path = doc.toURI().toURL();
				InputStream inputstream = path.openStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(inputstream));
				String c;

				do {
					c = br.readLine();

					obj = new JSONObject(c);
				} while (c != null);
			}
			// load properties of wfs
			if (!layerDef.get("layer_url").equals(null)) {
				if (typeWFS.equals("kml")) {
					URL url = new URL(getOutputFormatKML(layerDef.getString("layer_url")));
					obj = new JSONObject();
					obj.put("url", url.toString());
					return obj;
				} else if (typeWFS.equals("shp")) {
					URL url = new URL(getOutputFormatSHP(layerDef.getString("layer_url")));
					obj = new JSONObject();
					obj.put("url", url.toString());
					return obj;
				}
				URL url = new URL(layerDef.getString("layer_url"));
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();

				connection.setRequestMethod("GET");
				connection.setDoOutput(true);
				connection.setDoInput(true);
				connection.setRequestProperty("CetRequestProperty(ontent-Type", "application/json");
				connection.setRequestProperty("Accept", "application/json");
				// connection.setReadTimeout(30 * 1000);
				connection.connect();
				int HttpResult = connection.getResponseCode();
				if (HttpResult == HttpURLConnection.HTTP_OK) {

					BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
					String inputLine;
					while ((inputLine = br.readLine()) != null) {
						obj = new JSONObject(inputLine);

					}
					br.close();
				} else {
					System.out.println(connection.getResponseMessage());
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
		return obj;
	}

	@Override
	public List<SbiGeoLayersRoles> getListRolesById(Integer id) {
		Session tmpSession = getSession();
		List<SbiGeoLayersRoles> roles = new ArrayList<>();

		String hql = " from SbiGeoLayersRoles WHERE layer.layerId =? ";
		Query q = tmpSession.createQuery(hql);
		q.setInteger(0, id);
		roles = q.list();
		if (roles.size() == 0) {
			return null;
		}
		return roles;
	}

	/**
	 * Load all layers.
	 *
	 * @return the list
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 * @throws JSONException
	 * @throws UnsupportedEncodingException
	 *
	 * @see it.eng.spagobi.geo.bo.dao.IEngineDAO#loadAllEngines()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List<GeoLayer> loadAllLayers(String[] listLabel, IEngUserProfile profile) throws EMFUserError, JSONException, UnsupportedEncodingException {
		Session tmpSession = null;
		Transaction tx = null;
		List<GeoLayer> realResult = new ArrayList<GeoLayer>();
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();

			String inList = "";
			if (listLabel != null) {
				inList += " where label in (:listLabel)";
			}
			Query hibQuery = tmpSession.createQuery(" from SbiGeoLayers" + inList);

			if (listLabel != null) {
				hibQuery.setParameterList("listLabel", listLabel);
			}
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			SbiGeoLayers hibLayer;
			while (it.hasNext()) {
				hibLayer = (SbiGeoLayers) it.next();
				if (hibLayer != null) {
					final GeoLayer bilayer = hibLayer.toGeoLayer();
					List<SbiGeoLayersRoles> roles = getListRolesById(hibLayer.getLayerId());
					if (!userIsAbilited(roles, profile)) {
						continue;
					}
					String str = new String(hibLayer.getLayerDef(), "UTF-8");
					JSONObject layerDef = new JSONObject(str);

					bilayer.setLayerIdentify(layerDef.getString("layerId"));
					bilayer.setLayerLabel(layerDef.getString("layerLabel"));
					bilayer.setLayerName(layerDef.getString("layerName"));
					if (!layerDef.getString("properties").isEmpty()) {
						List<String> prop = new ArrayList<>();
						JSONArray obj = layerDef.getJSONArray("properties");

						for (int j = 0; j < obj.length(); j++) {

							prop.add(obj.getString(j));
						}

						bilayer.setProperties(prop);
					}
					if (!layerDef.getString("layer_file").equals("null")) {

						String resourcePath = SpagoBIUtilities.getResourcePath();
						// TODO delete this after all layer are saved with new path file
						if (layerDef.getString("layer_file").startsWith(resourcePath)) {
							bilayer.setPathFile(layerDef.getString("layer_file"));
						} else {
							bilayer.setPathFile(resourcePath + File.separator + layerDef.getString("layer_file"));
						}

					}
					if (!layerDef.getString("layer_url").equals("null")) {
						bilayer.setLayerURL(layerDef.getString("layer_url"));

					}
					if (!layerDef.getString("layer_params").equals("null")) {
						bilayer.setLayerParams(layerDef.getString("layer_params"));
					}
					if (!layerDef.getString("layer_options").equals("null")) {
						bilayer.setLayerOptions(layerDef.getString("layer_options"));

					}
					if (!layerDef.getString("layer_order").equals("null")) {
						bilayer.setLayerOrder(new Integer(layerDef.getString("layer_order")));

					}
					realResult.add(bilayer);
				}
			}

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
		return realResult;
	}

	private boolean userIsAbilited(List<SbiGeoLayersRoles> roles, IEngUserProfile profile) {
		if (UserUtilities.isAdministrator(profile) || roles == null) {
			return true;
		}
		for (SbiGeoLayersRoles r : roles) {
			Collection<String> rolesProfile;
			try {
				rolesProfile = profile.getRoles();

				Iterator it = rolesProfile.iterator();
				while (it.hasNext()) {
					String roleName = (String) it.next();
					if (roleName.equals(r.getRole().getName())) {
						return true;
					}
				}

			} catch (EMFInternalError e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public String getDescribeFeatureTypeURL(String url) {
		int indexOfRequest = url.indexOf("request=GetFeature");
		if (indexOfRequest > 0) {
			url = url.replaceAll("request=GetFeature", "request=DescribeFeatureType");
		}
		return url;
	}

	@Override
	public String getOutputFormatKML(String url) {
		int indexOfRequest = url.indexOf("&outputFormat=application%2Fjson");
		if (indexOfRequest > 0) {
			url = url.replaceAll("&outputFormat=application%2Fjson", "&outputFormat=application%2Fvnd.google-earth.kml%2Bxml");
		}
		return url;
	}

	@Override
	public String getOutputFormatSHP(String url) {
		int indexOfRequest = url.indexOf("&outputFormat=application%2Fjson");
		if (indexOfRequest > 0) {
			url = url.replaceAll("&outputFormat=application%2Fjson", "&outputFormat=SHAPE-ZIP");
		}
		return url;
	}
}
