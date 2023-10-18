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
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
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
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.mapcatalogue.bo.GeoLayer;
import it.eng.spagobi.mapcatalogue.metadata.SbiGeoLayers;
import it.eng.spagobi.mapcatalogue.metadata.SbiGeoLayersRoles;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class SbiGeoLayersDAOHibImpl extends AbstractHibernateDAO implements ISbiGeoLayersDAO {

	private static final Logger LOGGER = Logger.getLogger(SbiGeoLayersDAOHibImpl.class);

	/**
	 * Load layer by id.
	 *
	 * @param layerID the layer id
	 *
	 * @return the geo layer
	 *
	 * @throws EMFUserError                 the EMF user error
	 * @throws UnsupportedEncodingException
	 * @throws JSONException
	 *
	 * @see it.eng.spagobi.mapcatalogue.dao.geo.bo.dao.ISbiGeoLayersDAO#loadLayerByID(integer)
	 */
	@Override
	public GeoLayer loadLayerByID(Integer layerID) throws EMFUserError {
		GeoLayer toReturn = null;
		Session tmpSession = null;
		// Transaction tx = null;

		try {
			tmpSession = getSession();
			// tx = tmpSession.beginTransaction();
			SbiGeoLayers hibLayer = (SbiGeoLayers) tmpSession.load(SbiGeoLayers.class, layerID);
			toReturn = hibLayer.toGeoLayer();

			String resourcePath = SpagoBIUtilities.getResourcePath();
			if (toReturn.getPathFile() != null) {
				if (toReturn.getPathFile().startsWith(resourcePath)) {
					// biLayer.setPathFile(biLayer.getPathFile());
				} else {
					toReturn.setPathFile(resourcePath + File.separator + toReturn.getPathFile());
				}
			}

			// tx.commit();

		} catch (HibernateException he) {
			logException(he);

			// if (tx != null)
			// tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null && tmpSession.isOpen()) {
				tmpSession.close();
			}
		}

		return toReturn;
	}

	/**
	 * Load layer by name.
	 *
	 * @param name the name
	 *
	 * @return the geo layer
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.mapcatalogue.dao.geo.bo.dao.ISbiGeoLayersDAO#loadLayerByName(string)
	 */
	@Override
	public GeoLayer loadLayerByLabel(String label) throws EMFUserError {
		GeoLayer biLayer = null;
		Session tmpSession = null;
		// Transaction tx = null;

		try {
			tmpSession = getSession();
			// tx = tmpSession.beginTransaction();
			Criterion labelCriterrion = Restrictions.eq("label", label);
			Criteria criteria = tmpSession.createCriteria(SbiGeoLayers.class);
			criteria.add(labelCriterrion);
			SbiGeoLayers hibLayer = (SbiGeoLayers) criteria.uniqueResult();
			if (hibLayer == null)
				return null;
			biLayer = hibLayer.toGeoLayer();

			String resourcePath = SpagoBIUtilities.getResourcePath();
			if (biLayer.getPathFile() != null) {
				if (biLayer.getPathFile().startsWith(resourcePath)) {
					// biLayer.setPathFile(biLayer.getPathFile());
				} else {
					biLayer.setPathFile(resourcePath + File.separator + biLayer.getPathFile());
				}
			}

			// tx.commit();
		} catch (HibernateException he) {
			logException(he);
			// if (tx != null)
			// tx.rollback();
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
	 * @param aLayer the a layer
	 *
	 * @throws EMFUserError                 the EMF user error
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

			SbiGeoLayers hibLayer = (SbiGeoLayers) tmpSession.load(SbiGeoLayers.class, aLayer.getLayerId());
			hibLayer.setName(aLayer.getName());
			if (aLayer.getDescr() == null) {
				aLayer.setDescr("");
			}
			hibLayer.setDescr(aLayer.getDescr());
			hibLayer.setType(aLayer.getType());
			if (!hibLayer.getLabel().equals(aLayer.getLabel())) {
				// modify name of file
				updateFileIfPresent(hibLayer, aLayer.getLabel());
			}
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
					path = aLayer.getPathFile() + File.separator + "Layer" + File.separator;
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
					logException(e);
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

			hibLayer.setLayerDef(layerDef.toString().getBytes(StandardCharsets.UTF_8));
			updateSbiCommonInfo4Update(hibLayer);

			// delete all roles of layer
			// query hql
			String hql = "DELETE from SbiGeoLayersRoles a WHERE a.layer.id =:aLayerId";
			Query q = tmpSession.createQuery(hql);
			q.setParameter("aLayerId", aLayer.getLayerId());
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

			if (tmpSession != null && tmpSession.isOpen()) {
				tmpSession.close();
			}
		}

	}

	private void updateFileIfPresent(SbiGeoLayers layer, String newName) {
		String filePath = SpagoBIUtilities.getResourcePath() + File.separatorChar + "Layer" + File.separatorChar;
		String fileNewPath = SpagoBIUtilities.getResourcePath() + File.separatorChar + "Layer" + File.separatorChar;

		File originalDatasetFile = new File(filePath + layer.getLabel());
		File newDatasetFile = new File(fileNewPath + newName);
		if (originalDatasetFile.exists()) {
			/*
			 * This method copies the contents of the specified source file to the specified destination file. The directory holding the destination file is
			 * created if it does not exist. If the destination file exists, then this method will overwrite it.
			 */
			try {
				Files.copy(originalDatasetFile.toPath(), newDatasetFile.toPath());

				// Then delete old file
				Files.deleteIfExists(originalDatasetFile.toPath());
			} catch (IOException e) {
				throw new SpagoBIRuntimeException("Cannot move dataset File", e);
			}
		}
	}

	/**
	 * Insert layer.
	 *
	 * @param aLayer the a layer
	 *
	 * @throws EMFUserError  the EMF user error
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
					String name = aLayer.getLabel();
					try (OutputStreamWriter out = new FileWriter(resourcePath + File.separator + "Layer" + File.separator + name)) {
						String content = new String(aLayer.getFilebody());
						content = content.replaceAll("\t", "").replaceAll("\n", "").replaceAll("\r", "");
						out.write(content);
					}
				}
			} catch (IOException e) {
				logException(e);
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

			hibLayer.setLayerDef(layerDef.toString().getBytes(StandardCharsets.UTF_8));
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
		} catch (

		HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null && tmpSession.isOpen()) {
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
		ArrayList<String> keys = new ArrayList<>();
		try {
			tmpSession = getSession();
			tmpSession.beginTransaction();
			GeoLayer aLayer = loadLayerByID(layerId);
			JSONObject layerDef = new JSONObject(new String(aLayer.getLayerDef()));
			if (aLayer.getType().equals("Google") || aLayer.getType().equals("TMS") || aLayer.getType().equals("OSM")) {
				return new ArrayList<>();
			}
			// load properties of file
			if (aLayer.getType().equals("File")) {

				String resourcePath = SpagoBIUtilities.getResourcePath();
				if (aLayer.getPathFile().startsWith(resourcePath)) {
					// biLayer.setPathFile(biLayer.getPathFile());
				} else {
					aLayer.setPathFile(resourcePath + File.separator + aLayer.getPathFile());
				}
				File doc = new File(aLayer.getPathFile());
				URL path = doc.toURI().toURL();
				try (InputStream inputstream = path.openStream();
						InputStreamReader isr = new InputStreamReader(inputstream);
						BufferedReader br = new BufferedReader(isr)) {
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
									if (!keys.contains(key)) {
										keys.add(key);
									}
								}
							}
						}

					} while (c != null);

				}
			}

			// load properties of wfs
			else if (aLayer.getType().equals("WFS") || aLayer.getType().equals("WMS")) {
				String urlDescribeFeature = "";

				switch (aLayer.getType()) {
				case "WFS":
					urlDescribeFeature = getDescribeFeatureTypeURL(layerDef.getString("layer_url"));
					break;
				case "WMS":
					urlDescribeFeature = getWMSDescribeFeatureTypeURL(layerDef.getString("layer_url"), layerDef.getString("layerName"));
					break;
				}

				// Create a trust manager that does not validate certificate chains
				TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
					@Override
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					@Override
					public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
					}

					@Override
					public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
					}
				} };

				// Now you can access an https URL without having the certificate in the truststore
				URL url = new URL(urlDescribeFeature);
				URLConnection connection = url.openConnection();

				try (InputStream inputStream = connection.getInputStream();
						BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
					String inputLine;
					while ((inputLine = br.readLine()) != null) {
						JSONObject obj = new JSONObject(inputLine);
						JSONArray content = (JSONArray) obj.get("featureTypes");
						content.getJSONObject(0);
						for (int j = 0; j < content.length(); j++) {
							JSONArray arr = content.getJSONObject(j).getJSONArray("properties");
							for (int k = 0; k < arr.length(); k++) {

								JSONObject val = arr.getJSONObject(k);
								if (!keys.contains(val.get("name"))) {
									keys.add(val.getString("name"));
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Error during loading properties : " + e.getLocalizedMessage(), e);
		} finally {
			if (tmpSession != null && tmpSession.isOpen()) {
				tmpSession.close();
			}
		}
		return keys;
	}

	/**
	 * Erase layer.
	 *
	 * @param aLayer the a layer
	 *
	 * @throws EMFUserError the EMF user error
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

			if (hibLayer.getType().equals("File")) {
				String resourcePath = SpagoBIUtilities.getResourcePath();
				File doc = new File(resourcePath + File.separator + "Layer" + File.separator + aLayer.getLabel());
				if (doc.exists()) {
					try {
						Files.delete(doc.toPath());
					} catch (IOException x) {
						// File permission problems are caught here.
						throw new IllegalArgumentException("Delete failed : " + x.getLocalizedMessage(), x);
					}
				}
			}
			tmpSession.delete(hibLayer);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {

			if (tmpSession != null && tmpSession.isOpen()) {
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

			if (tmpSession != null && tmpSession.isOpen()) {
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
				try (InputStream inputstream = path.openStream(); BufferedReader br = new BufferedReader(new InputStreamReader(inputstream))) {
					String c;
					c = br.readLine();
					obj = new JSONObject(c);
				}

			}
			// load properties of wfs
			if (!layerDef.get("layer_url").equals("null")) {
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
				int httpResult = connection.getResponseCode();
				if (httpResult == HttpURLConnection.HTTP_OK) {

					try (InputStreamReader isr = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
							BufferedReader br = new BufferedReader(isr)) {
						String inputLine;
						while ((inputLine = br.readLine()) != null) {
							obj = new JSONObject(inputLine);

						}
					}
				} else {
					LOGGER.debug(connection.getResponseMessage());
				}

			}
		} catch (Exception e) {
			logException(e);
		} finally {

			if (tmpSession != null && tmpSession.isOpen()) {
				tmpSession.close();
			}

		}
		return obj;
	}

	@Override
	public List<SbiGeoLayersRoles> getListRolesById(Integer id) {
		Session tmpSession = null;
		List<SbiGeoLayersRoles> roles = new ArrayList<>();
		try {
			tmpSession = getSession();

			String hql = " from SbiGeoLayersRoles WHERE layer.layerId =? ";
			Query q = tmpSession.createQuery(hql);
			q.setInteger(0, id);
			roles = q.list();
			if (roles.isEmpty()) {
				return null;
			}

		} catch (HibernateException he) {
			logException(he);
		} finally {

			if (tmpSession != null && tmpSession.isOpen()) {
				tmpSession.close();
			}
		}
		return roles;
	}

	@Override
	public List<SbiGeoLayersRoles> getListRolesById(Integer id, Session session) {

		List<SbiGeoLayersRoles> roles = new ArrayList<>();
		try {

			String hql = " from SbiGeoLayersRoles WHERE layer.layerId =? ";
			Query q = session.createQuery(hql);
			q.setInteger(0, id);
			roles = q.list();
			if (roles.isEmpty()) {
				return null;
			}

		} catch (HibernateException he) {
			logException(he);
		}
		return roles;
	}

	/**
	 * Load all layers.
	 *
	 * @return the list
	 *
	 * @throws EMFUserError                 the EMF user error
	 * @throws JSONException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List<GeoLayer> loadAllLayers(String[] listLabel, IEngUserProfile profile) throws EMFUserError, JSONException, UnsupportedEncodingException {
		Session tmpSession = null;
		Transaction tx = null;
		List<GeoLayer> realResult = new ArrayList<>();
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
					// List<SbiGeoLayersRoles> roles = getListRolesById(hibLayer.getLayerId());
					List<SbiGeoLayersRoles> roles = getListRolesById(hibLayer.getLayerId(), tmpSession);
					if (!userIsAbilited(roles, profile)) {
						continue;
					}
					String str = new String(hibLayer.getLayerDef(), StandardCharsets.UTF_8);
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

			if (tmpSession != null && tmpSession.isOpen()) {
				tmpSession.close();
			}

		}
		return realResult;
	}

	private boolean userIsAbilited(List<SbiGeoLayersRoles> roles, IEngUserProfile profile) {
		if (UserUtilities.isAdministrator(profile)) {
			return true;
		}

		if (!UserUtilities.isAdministrator(profile) && roles == null) {
			return false;
		}

		for (SbiGeoLayersRoles r : roles) {
			Collection<String> rolesProfile;
			try {
				rolesProfile = profile.getRoles();

				Iterator<String> it = rolesProfile.iterator();
				while (it.hasNext()) {
					String roleName = it.next();
					if (roleName.equals(r.getRole().getName())) {
						return true;
					}
				}

			} catch (EMFInternalError e) {
				logException(e);
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
	public String getWMSDescribeFeatureTypeURL(String url, String layerName) {
		int indexOfRequest = url.indexOf("request=GetFeature");
		if (indexOfRequest > 0) {
			url = url.replaceAll("request=GetFeature", "request=DescribeFeatureType");
		}

		int pi = url.indexOf("?");
		if (pi == -1) {
			url += "?service=WFS&request=DescribeFeatureType&outputFormat=application%2Fjson&typename=" + layerName;
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

	@Override
	public Integer countCategories(Integer catId) {
		LOGGER.debug("IN");
		Integer resultNumber = 0;
		Session session = null;
		Transaction transaction = null;
		try {
			session = getSession();
			transaction = session.beginTransaction();

			String hql = "select count(*) from SbiGeoLayers s where s.category_id = ? ";
			Query aQuery = session.createQuery(hql);
			aQuery.setInteger(0, catId.intValue());
			resultNumber = ((Long) aQuery.uniqueResult()).intValue();

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("Error while getting the category with the geo layer with id " + catId, t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			LOGGER.debug("OUT");
		}
		return resultNumber;
	}

	@Override
	public List<GeoLayer> loadLayerByCategoryId(Integer catId) throws EMFUserError {
		Session tmpSession = null;
		// Transaction tx = null;
		List<GeoLayer> retLayers = new ArrayList<>();
		try {
			tmpSession = getSession();
			// tx = tmpSession.beginTransaction();
			Criterion labelCriterrion = Restrictions.eq("category.id", catId);
			Criteria criteria = tmpSession.createCriteria(SbiGeoLayers.class);
			criteria.add(labelCriterrion);
			List hibList = criteria.list();
			Iterator it = hibList.iterator();
			SbiGeoLayers hibLayer;
			while (it.hasNext()) {
				hibLayer = (SbiGeoLayers) it.next();
				retLayers.add(hibLayer.toGeoLayer());
			}

			// tx.commit();
		} catch (HibernateException he) {
			logException(he);
			// if (tx != null)
			// tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {

			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}

		}
		return retLayers;
	}
}
