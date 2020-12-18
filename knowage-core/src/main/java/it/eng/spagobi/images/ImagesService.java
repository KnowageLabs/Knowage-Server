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
package it.eng.spagobi.images;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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

import org.apache.clerezza.jaxrs.utils.form.FormFile;
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.images.dao.IImagesDAO;
import it.eng.spagobi.images.dao.IImagesDAO.Direction;
import it.eng.spagobi.images.dao.IImagesDAO.OrderBy;
import it.eng.spagobi.images.metadata.SbiImages;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.glossary.util.Util;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("/1.0/images")
public class ImagesService {

	private static transient Logger logger = Logger.getLogger(ImagesService.class);

	@GET
	@Path("/listImages")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.IMAGES_MANAGEMENT })
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
				o.put("imgId", imageDB.getImageId());
				o.put("size", imageDB.getContent().length);
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
	@UserConstraint(functionalities = { SpagoBIConstants.IMAGES_MANAGEMENT })
	public void getImage(@Context HttpServletRequest req, @Context HttpServletResponse resp) {
		try {
			IImagesDAO dao = DAOFactory.getImagesDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);
			Integer id = Util.getNumberOrNull(req.getParameter("IMAGES_ID"));
			String preview = req.getParameter("preview");
			SbiImages imageDB = dao.loadImage(id);
			String contentType = "";
			byte[] content;
			if (preview != null) {
				content = imageDB.getContentIco();
			} else {
				content = imageDB.getContent();
			}
			flushFileToResponse(resp, contentType, content);
		} catch (Throwable t) {
			logger.error("An unexpected error occured while executing service \"getImage\"", t);
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service \"getImage\"", t);
		}
	}

	@POST
	@Path("/addImage")
	@Consumes("multipart/form-data")
	@Produces(MediaType.TEXT_PLAIN)
	@UserConstraint(functionalities = { SpagoBIConstants.IMAGES_MANAGEMENT })
	public String addImage(MultiPartBody input, @Context HttpServletRequest req) {
		String msg = "sbi.cockpit.widgets.image.imageWidgetDesigner.uploadOK";
		String fileName = null;
		try {

			IImagesDAO dao = DAOFactory.getImagesDAO();
			IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			dao.setUserProfile(profile);

			final FormFile file = input.getFormFileParameterValues("uploadedImage")[0];

			if (file != null) {

				fileName = file.getFileName();
				ImageServiceAPI imageServiceAPI = new ImageServiceAPI();

				if (imageServiceAPI.isTooBig(file)) {
					msg = "sbi.cockpit.widgets.image.imageWidgetDesigner.tooBigImage";
				} else if (imageServiceAPI.isTooManyImageForTenant()) {
					msg = "sbi.cockpit.widgets.image.imageWidgetDesigner.tooImageForTenant";
				} else if (imageServiceAPI.isTooManyImageForUser()) {
					msg = "sbi.cockpit.widgets.image.imageWidgetDesigner.tooImageForUser";
				} else if (!imageServiceAPI.isAnImage(file)) {
					msg = "sbi.cockpit.widgets.image.imageWidgetDesigner.fileIsNotAnImage";
				} else if (!imageServiceAPI.isValidFileExtension(file)) {
					msg = "sbi.cockpit.widgets.image.imageWidgetDesigner.fileTypeIsNotAllowed";
				} else {
					SbiImages image = dao.loadImage(fileName);
					if (image == null) {
						byte[] data = file.getContent();
						dao.insertImage(fileName, data);

						JSONObject ret = new JSONObject();
						ret.put("success", true);
						ret.put("msg", msg);
						ret.put("fileName", fileName);
						return ret.toString();

					} else {
						msg = "sbi.cockpit.widgets.image.imageWidgetDesigner.alreadyExists";
					}
				}
			}
		} catch (Throwable t) {
			logger.error("An unexpected error occured while executing service \"addImage\"", t);
			msg = "An unexpected error occured while executing service \"addImage\"";
			throw new SpagoBIRuntimeException(msg);
		}

		try {
			JSONObject ret = new JSONObject();
			ret.put("success", false);
			ret.put("msg", msg);
			ret.put("fileName", fileName);
			return ret.toString();
		} catch (JSONException e) {
			return "{\"success\":false,\"msg\":\"JSON Error\"}";
		}
	}

//	public boolean isValidJSON(final String json) {
//		boolean valid = false;
//		try {
//			final JsonParser parser = new ObjectMapper().getJsonFactory().createJsonParser(json);
//			while (parser.nextToken() != null) {
//			}
//			valid = true;
//		} catch (JsonParseException jpe) {
//			logger.debug("Parsed String is not a valid JSON: " + json);
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//		}
//
//		return valid;
//	}

	private String checkIfImageIsInUse(Integer imageId) {
		logger.debug("IN");

		String toReturn = null;

		try {
			List biObjects = DAOFactory.getBIObjectDAO().loadBIObjects(SpagoBIConstants.DOCUMENT_COMPOSITE_TYPE, null, null);

			// check with string, is enough
			for (Iterator iterator = biObjects.iterator(); iterator.hasNext();) {
				BIObject object = (BIObject) iterator.next();
				boolean foundInObj = false;
				ObjTemplate template = object.getActiveTemplate();
				if (template != null) {
					byte[] templateContentBytes = template.getContent();
					if (templateContentBytes != null) {
						String templateContent = new String(templateContentBytes);
						if (templateContent.indexOf("\"imgId\":" + imageId) != -1) {
							if (toReturn == null) {
								toReturn = object.getName();
								foundInObj = true;
							} else {
								foundInObj = true;
								toReturn += ", " + object.getName();
							}
						}
					}
				}
			}

			// check with json, too complicated
			// for (Iterator iterator = biObjects.iterator(); iterator.hasNext();) {
			// BIObject object = (BIObject) iterator.next();
			// boolean foundInObj = false;
			// ObjTemplate template = object.getActiveTemplate();
			// byte[] templateContentBytes = template.getContent();
			// if (templateContentBytes != null) {
			// String templateContent = new String(templateContentBytes);
			// // to check if is a XML or JSON (other map templates are XML)
			// if (!templateContent.trim().startsWith("<")) {
			// if (isValidJSON(templateContent)) {
			// JSONObject templateJSONObject = JSONUtils.toJSONObject(templateContent);
			// if (templateJSONObject.has("widgets")) {
			// JSONArray widgets = templateJSONObject.getJSONArray("widgets");
			// for (int index = 0; index < widgets.length() && foundInObj == false; index++) {
			// Object object2 = widgets.get(index);
			// if (object2 instanceof JSONObject) {
			// JSONObject widget = (JSONObject) object2;
			// if (widget.has("type") && widget.getString("type").equals("image")) {
			// if (widget.has("content")) {
			// JSONObject content = widget.getJSONObject("content");
			// if (content.has("imgId")) {
			// String idS = content.getString("imgId");
			// Integer id = Integer.valueOf(idS);
			// if (id.equals(imageId)) {
			// if (toReturn == null) {
			// toReturn = object.getLabel();
			// foundInObj = true;
			// } else {
			// foundInObj = true;
			// toReturn += ", " + object.getLabel();
			// }
			// }
			// }
			// }
			// }
			// }
			// }
			// }
			// }
			// }
			// }
			// }

		} catch (Exception e) {
			logger.error("Error in checking if image is used in other cockpits template", e);

		}

		logger.debug("OUT");
		return toReturn;
	}

	@GET
	@Path("/deleteImage")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.IMAGES_MANAGEMENT })
	public String deleteImage(@Context HttpServletRequest req) {
		logger.debug("IN");
		String msg = "sbi.cockpit.widgets.image.imageWidgetDesigner.deleteOK";
		boolean success = true;
		try {
			Object idObj = req.getParameter("imageId");
			Integer id = null;
			if (idObj == null) {
				msg = "sbi.cockpit.widgets.image.imageWidgetDesigner.emptyParameter";
				success = false;
			} else {
				id = Integer.valueOf(idObj.toString());

				String labelInUSe = checkIfImageIsInUse(id);
				if (labelInUSe != null) {
					logger.error("Cannot delete image because it is in use in " + labelInUSe);
					msg = "Cannot delete image because it is in use in documents " + labelInUSe;
					success = false;
				} else {
					IImagesDAO dao = DAOFactory.getImagesDAO();
					IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
					dao.setUserProfile(profile);
					dao.deleteImage(id);
				}
			}
		} catch (Throwable t) {
			logger.error("An unexpected error occured while executing service \"deleteImage\"", t);
			msg = "An unexpected error occured while executing service \"deleteImage\"";
			success = false;
		}
		try {
			JSONObject ret = new JSONObject();
			ret.put("success", success);
			ret.put("msg", msg);
			logger.debug("OUT");
			return ret.toString();
		} catch (JSONException e) {
			logger.debug("OUT");
			return "{\"success\":false,\"msg\":\"JSON Error\"}";
		}
	}

	/**
	 * header sample { Content-Type=[image/png], Content-Disposition=[form-data; name="file"; filename="filename.extension"] }
	 **/
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
