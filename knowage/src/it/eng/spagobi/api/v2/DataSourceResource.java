package it.eng.spagobi.api.v2;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.bo.DataSourceModel;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONObject;

@Path("/2.0/datasources")
@ManageAuthorization
public class DataSourceResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(DataSourceResource.class);

	IDataSourceDAO dataSourceDAO;
	DataSource dataSource;
	List<DataSource> dataSourceList;

	
	@SuppressWarnings("unchecked")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_READ })
	public List<DataSource> getAllDataSources() {

		logger.debug("IN");

		try {

			dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());
			dataSourceList = dataSourceDAO.loadAllDataSources();

			return dataSourceList;

		} catch (Exception exception) {

			logger.error("Error while getting the list of DS", exception);
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.params.error", buildLocaleFromSession(), exception);

		} finally {

			logger.debug("OUT");

		}
	}

	@GET
	@Path("/{dsId}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_READ })
	public String getDataSourceById(@PathParam("dsId") Integer dsId) {

		logger.debug("IN");

		try {

			dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());
			dataSource = dataSourceDAO.loadDataSourceByID(dsId);

			return JsonConverter.objectToJson(dataSource, null);

		} catch (Exception e) {

			logger.error("Error while loading a single data set", e);
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.params.error", buildLocaleFromSession(), e);

		} finally {

			logger.debug("OUT");

		}
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public String postDataSource(@Valid DataSource dataSource) {

		logger.debug("IN");

		try {

			dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());
			dataSourceDAO.insertDataSource(dataSource, getUserProfile().getOrganization());

			DataSource newLabel = (DataSource) dataSourceDAO.loadDataSourceByLabel(dataSource.getLabel());
			int newId = newLabel.getDsId();

			return Integer.toString(newId);

		} catch (Exception exception) {

			logger.error("Error while posting DS", exception);
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.params.error", buildLocaleFromSession(), exception);

		} finally {

			logger.debug("OUT");

		}
	}

	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public List<DataSource> putDataSource(DataSourceModel dataSource) {

		logger.debug("IN");

		try {

			dataSourceDAO = DAOFactory.getDataSourceDAO();
			
			dataSourceDAO.setUserProfile(getUserProfile());
			IDataSource oldDataSource = dataSourceDAO.loadDataSourceWriteDefault();

			if(oldDataSource != null && dataSource.getWriteDefault() && oldDataSource.getDsId() != dataSource.getDsId())
			{
				// unset the cache
				//SpagoBICacheManager.removeCache();
				oldDataSource.setWriteDefault(false);
				dataSourceDAO.modifyDataSource(oldDataSource);
			}
			dataSourceDAO.modifyDataSource(dataSource);
			return DAOFactory.getDataSourceDAO().loadAllDataSources();

		} catch (Exception e) {

			logger.error("Error while updating url of the new resource", e);
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.params.error", buildLocaleFromSession(), e);

		} finally {

			logger.debug("OUT");

		}
	}

	@DELETE
	@Path("/{dsId}")
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public List<DataSource> deleteDataSourceById(@PathParam("dsId") Integer dsId) throws EMFUserError {

		logger.debug("IN");

		try {

			DataSource dataSource = new DataSource();
			dataSource.setDsId(dsId);
			dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());
			dataSourceDAO.eraseDataSource(dataSource);

			return DAOFactory.getDataSourceDAO().loadAllDataSources();

		} catch (Exception e) {

			logger.error("Error while updating url of the new resource", e);
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.params.error", buildLocaleFromSession(), e);

		} finally {

			logger.debug("OUT");

		}
	}

	@DELETE
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public List<DataSource> deleteMultiple(@QueryParam("id") int[] ids) {

		logger.debug("IN");

		try {

			dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());

			for (int i = 0; i < ids.length; i++) {
				DataSource ds = new DataSource();
				ds.setDsId(ids[i]);
				dataSourceDAO.eraseDataSource(ds);
			}

			return dataSourceDAO.loadAllDataSources();

		} catch (Exception e) {

			logger.error("Error while deleting url of the new resource", e);
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.params.error", buildLocaleFromSession(), e);

		} finally {

			logger.debug("OUT");

		}
	}

	@GET
	@Path("/structure/{dsId}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_READ })
	public String getDataSourceStruct(@PathParam("dsId") Integer dsId) {

		logger.debug("IN");
		JSONObject tableContent = new JSONObject();
		try {

			dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());
			dataSource = dataSourceDAO.loadDataSourceByID(dsId);

			Connection conn = dataSource.getConnection();
			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getTables(null, null, "%", null);
			while (rs.next()) {
				JSONObject column = new JSONObject();
				ResultSet tabCol = md.getColumns(rs.getString(1), rs.getString(2), rs.getString(3), "%");
				while (tabCol.next()) {
					column.put(tabCol.getString(4), "null");
				}
				tableContent.put(rs.getString(3), column);
			}

		} catch (Exception e) {

			logger.error("Error while loading a single data set", e);
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.params.error", buildLocaleFromSession(), e);

		} finally {

			logger.debug("OUT");

		}

		return tableContent.toString();
	}

}
