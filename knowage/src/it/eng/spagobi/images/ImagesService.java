package it.eng.spagobi.images;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.images.dao.IImagesDAO;
import it.eng.spagobi.images.dao.IImagesDAO.Direction;
import it.eng.spagobi.images.dao.IImagesDAO.OrderBy;
import it.eng.spagobi.images.metadata.SbiImages;
import it.eng.spagobi.tools.glossary.util.Util;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("/1.0/images")
public class ImagesService {
	private static final String IMAGE_GALLERY_MAX_IMAGE_SIZE = "IMAGE_GALLERY.MAX_IMAGE_SIZE_KB";
	private static final String IMAGE_GALLERY_MAX_USER_IMAGES = "IMAGE_GALLERY.MAX_USER_IMAGES";
	private static final String IMAGE_GALLERY_MAX_TENANT_IMAGES = "IMAGE_GALLERY.MAX_TENANT_IMAGES";
	private final long defaultMaxImageSize = 1024;
	private final long defaultMaxUserImages = 10;
	private final long defaultMaxTenantImages = 100;

	private static transient Logger logger = Logger.getLogger(ImagesService.class);

	@GET
	@Path("/listImages")
	@Produces(MediaType.APPLICATION_JSON)
	public String listImages(@Context HttpServletRequest req) {
		try {
			IImagesDAO dao = DAOFactory.getImagesDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);
			String name = req.getParameter("IMAGES_NAME");
			String descr = req.getParameter("IMAGES_DESCRIPTION");
			Map<OrderBy, Direction> sort = getOrderMap(req.getParameter("sort"));
			JSONObject ret = new JSONObject();
			JSONArray images = new JSONArray();
			for (SbiImages imageDB : dao.listImages(name, descr, sort)) {
				Date lastMod = imageDB.getCommonInfo().getTimeUp() != null ? imageDB.getCommonInfo().getTimeUp() : imageDB.getCommonInfo().getTimeIn();
				String url = "/1.0/images/getImage?IMAGES_ID=" + imageDB.getImageId();
				JSONObject o = new JSONObject();
				o.put("name", imageDB.getName());
				o.put("size", imageDB.getContent().length());
				o.put("lastmod", lastMod);
				o.put("urlPreview", url + "&preview=true");
				o.put("url", url);
				images.put(o);
			}
			ret.put("data", images);
			return ret.toString();
		} catch (Throwable t) {
			logger.error("An unexpected error occured while executing service \"listImages\"", t);
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service \"listImages\"", t);
		}
	}

	@GET
	@Path("/getImage")
	@Produces(MediaType.APPLICATION_JSON)
	public void getImage(@Context HttpServletRequest req, @Context HttpServletResponse resp) {
		try {
			IImagesDAO dao = DAOFactory.getImagesDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);
			Integer id = Util.getNumberOrNull(req.getParameter("IMAGES_ID"));
			String preview = req.getParameter("preview");
			SbiImages imageDB = dao.loadImage(id);
			String contentType = "";
			Blob content;
			if (preview != null) {
				content = imageDB.getContentIco();
			} else {
				content = imageDB.getContent();
			}
			flushFileToResponse(resp, contentType, content.getBytes(1, (int) content.length()));
		} catch (Throwable t) {
			logger.error("An unexpected error occured while executing service \"getImage\"", t);
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service \"getImage\"", t);
		}
	}

	@POST
	@Path("/addImage")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN)
	public String addImage(MultipartFormDataInput input, @Context HttpServletRequest req) {
		boolean success = true;
		String msg = "sbi.cockpit.widgets.image.imageWidgetDesigner.uploadOK";
		try {
			Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
			IImagesDAO dao = DAOFactory.getImagesDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);

			List<InputPart> inputParts = uploadForm.get("uploadedImage");
			for (InputPart inputPart : inputParts) {
				MultivaluedMap<String, String> header = inputPart.getHeaders();
				String fileName = getFileName(header);
				try {
					InputStream inputStream = inputPart.getBody(InputStream.class, null);
					byte[] data = IOUtils.toByteArray(inputStream);
					if (data.length > getParamValue(IMAGE_GALLERY_MAX_IMAGE_SIZE, defaultMaxImageSize) * 1024) {
						msg = "sbi.cockpit.widgets.image.imageWidgetDesigner.tooBigImage";
						success = false;
					}
					if (success) {
						long numImagesByTenant = dao.countImages(false);
						if (numImagesByTenant >= getParamValue(IMAGE_GALLERY_MAX_TENANT_IMAGES, defaultMaxTenantImages)) {
							msg = "sbi.cockpit.widgets.image.imageWidgetDesigner.tooImageForTenant";
							success = false;
						}
					}
					if (success) {
						long numImagesByUser = dao.countImages(true);
						if (numImagesByUser >= getParamValue(IMAGE_GALLERY_MAX_USER_IMAGES, defaultMaxUserImages)) {
							msg = "sbi.cockpit.widgets.image.imageWidgetDesigner.tooImageForUser";
							success = false;
						}
					}
					if (success) {
						SbiImages image = dao.loadImage(fileName);
						if (image == null) {
							dao.insertImage(fileName, data);
						} else {
							msg = "sbi.cockpit.widgets.image.imageWidgetDesigner.alreadyExists";
							success = false;
						}
					}
				} catch (IOException e) {
					msg = "sbi.cockpit.widgets.image.imageWidgetDesigner.uploadKO";
					success = false;
				}

			}
		} catch (Throwable t) {
			logger.error("An unexpected error occured while executing service \"addImage\"", t);
			msg = "An unexpected error occured while executing service \"addImage\"";
			success = false;
		}

		try {
			JSONObject ret = new JSONObject();
			ret.put("success", success);
			ret.put("msg", msg);
			return ret.toString();
		} catch (JSONException e) {
			return "{\"success\":false,\"msg\":\"JSON Error\"}";
		}
	}

	@GET
	@Path("/deleteImage")
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteImage(@Context HttpServletRequest req) {
		String msg = "sbi.cockpit.widgets.image.imageWidgetDesigner.deleteOK";
		boolean success = true;
		try {
			// JSONObject jobj = RestUtilities.readBodyAsJSONObject(req);
			String name = req.getParameter("name");
			if (name == null || name.isEmpty()) {
				msg = "sbi.cockpit.widgets.image.imageWidgetDesigner.emptyParameter";
				success = false;
			} else {
				IImagesDAO dao = DAOFactory.getImagesDAO();
				IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
				dao.setUserProfile(profile);
				dao.deleteImage(name);
			}
		} catch (EMFUserError e) {
			success = false;
			msg = "sbi.cockpit.widgets.image.imageWidgetDesigner.deleteKO";
		} catch (Throwable t) {
			logger.error("An unexpected error occured while executing service \"deleteImage\"", t);
			msg = "An unexpected error occured while executing service \"deleteImage\"";
			success = false;
		}
		try {
			JSONObject ret = new JSONObject();
			ret.put("success", success);
			ret.put("msg", msg);
			return ret.toString();
		} catch (JSONException e) {
			return "{\"success\":false,\"msg\":\"JSON Error\"}";
		}
	}

	/**
	 * header sample { Content-Type=[image/png], Content-Disposition=[form-data; name="file"; filename="filename.extension"] }
	 **/
	// get uploaded filename, is there a easy way in RESTEasy?
	private String getFileName(MultivaluedMap<String, String> header) {

		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {

				String[] name = filename.split("=");

				String fn = name[1];

				if (fn.contains(File.separator)) {
					int beginIndex = fn.lastIndexOf(File.separator) + 1;
					if (beginIndex < fn.length()) {
						fn = fn.substring(beginIndex);
					}
				}

				String finalFileName = fn.trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		return "unknown";
	}

	private static void flushFileToResponse(HttpServletResponse response, String contentType, byte[] content) throws IOException {
		response.reset(); // Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it
							// may collide.
		response.setContentType(contentType); // Check http://www.w3schools.com/media/media_mimeref.asp for all types. Use if necessary
												// ServletContext#getMimeType() for auto-detection based on filename.
		response.setContentLength(content.length); // Set it with the file size. This header is optional. It will work if it's omitted, but the download
													// progress will be unknown.
		response.setHeader("Content-Disposition", "inline; filename=\"fileName\""); // The Save As popup magic is done here. You can give it any file name you
																					// want, this only won't work in MSIE, it will use current request URL as
																					// file name instead.
		OutputStream output = response.getOutputStream();
		output.write(content);
		response.flushBuffer();
	}

	private long getParamValue(String paramName, long defaultValue) {
		long ret = defaultValue;
		try {
			String size = SingletonConfig.getInstance().getConfigValue(paramName);
			if (size != null && size.matches("\\d+"))
				ret = Long.parseLong(size);
		} catch (NumberFormatException e) {
		}
		return ret;
	}

	private Map<OrderBy, Direction> getOrderMap(String parameter) {
		if (parameter != null) {
			try {
				Map<OrderBy, Direction> map = new HashMap<OrderBy, Direction>();
				JSONArray arr = new JSONArray(parameter);
				for (int i = 0; i < arr.length(); i++) {
					JSONObject s = arr.getJSONObject(i);
					try {
						OrderBy field = OrderBy.valueOf(s.getString("property"));
						Direction dir = Direction.valueOf(s.getString("direction"));
						map.put(field, dir);
					} catch (Exception e) {
					}
				}
				return map;
			} catch (JSONException e) {
			}
		}
		return null;
	}
}
