package it.eng.knowage.knowageapi.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.knowage.knowageapi.dao.SbiWidgetGalleryDao;
import it.eng.knowage.knowageapi.dao.SbiWidgetGalleryTagDao;
import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGallery;
import it.eng.knowage.knowageapi.resource.dto.Code;
import it.eng.knowage.knowageapi.resource.dto.WidgetGalleryDTO;

@Component
public class WidgetGalleryService {

	@Autowired
	private SbiWidgetGalleryDao sbiWidgetGalleryDao;

	@Autowired
	private SbiWidgetGalleryTagDao sbiWidgetGalleryTagDao;

	public List<WidgetGalleryDTO> getWidgets() {

		List<SbiWidgetGallery> widgets = (List<SbiWidgetGallery>) sbiWidgetGalleryDao.findAll();

		List<WidgetGalleryDTO> ret = widgets.stream().map(el -> mapTo(el)).collect(Collectors.toList());

		return ret;
	}

	public WidgetGalleryDTO getWidgetsById(String id) {

		SbiWidgetGallery widget = sbiWidgetGalleryDao.findById(id);

		return mapTo(widget);
	}

	private WidgetGalleryDTO mapTo(SbiWidgetGallery sbiWidgetGallery) {

		WidgetGalleryDTO toRet = new WidgetGalleryDTO();

		toRet.setAuthor(sbiWidgetGallery.getAuthor());
		toRet.setId(sbiWidgetGallery.getUuid());
		toRet.setName(sbiWidgetGallery.getName());
		toRet.setDescription(sbiWidgetGallery.getDescription());
		toRet.setType(sbiWidgetGallery.getType());
		String image = Base64.getEncoder().encodeToString(sbiWidgetGallery.getPreviewImage());
		toRet.setImage("data:image/png;base64," + image);
		toRet.setImageBase64Content(sbiWidgetGallery.getPreviewImage());
		Code code = new Code();
		JSONObject jsonBody;
		try {
			jsonBody = new JSONObject(sbiWidgetGallery.getTemplate());
			JSONObject jsonCode = jsonBody.optJSONObject("code");
			String html = jsonCode.getString("html");
			String javascript = jsonCode.getString("javascript");
			String python = jsonCode.getString("python");
			String css = jsonCode.getString("css");
			code.setCss(css);
			code.setJavascript(javascript);
			code.setPython(python);
			code.setHtml(html);
			toRet.setCode(code);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return toRet;
	}

	public void createNewGallery(SbiWidgetGallery sbiWidgetGallery) {
		sbiWidgetGalleryDao.create(sbiWidgetGallery);
	}

	public void updateGallery(String uuid, String name, String type, String author, String description, String licenseText, String licenseName,
			String organization, String image, String sbiversion, String template, String userid) {

		image = image.substring(image.indexOf(",") + 1);
		byte[] byteArrray = image.getBytes();
		SbiWidgetGallery newSbiWidgetGallery = new SbiWidgetGallery();
		newSbiWidgetGallery.setUuid(uuid);
		newSbiWidgetGallery.setAuthor(author);
		newSbiWidgetGallery.setDescription(description);
		newSbiWidgetGallery.setLicenseText(licenseText);
		newSbiWidgetGallery.setLicenseName(licenseName);
		newSbiWidgetGallery.setName(name);
		newSbiWidgetGallery.setOrganization(organization);
		newSbiWidgetGallery.setPreviewImage(byteArrray);
		newSbiWidgetGallery.setSbiVersionIn(sbiversion);
		newSbiWidgetGallery.setTemplate(template);
		newSbiWidgetGallery.setTimeIn(Timestamp.from(Instant.now()));
		newSbiWidgetGallery.setType(type);
		newSbiWidgetGallery.setUserIn(userid);
		sbiWidgetGalleryDao.update(newSbiWidgetGallery);
	}

	public int deleteGallery(String id) {
		return sbiWidgetGalleryDao.deleteById(id);
	}

	public void createNewWidgetTagsByList(SbiWidgetGallery sbiWidgetGallery, UUID uuid, String userid, String tags) {

		tags = tags.substring(1, tags.length() - 1);

		String[] tagArray = tags.split(",");

		for (int i = 0; i < tagArray.length; i++) {

//			SbiWidgetGalleryTag sbiWidgetGalleryTag = new SbiWidgetGalleryTag();
//			SbiWidgetGalleryTagId newId = new SbiWidgetGalleryTagId(tagArray[i], uuid);
//			sbiWidgetGalleryTag.setId(newId);
//			sbiWidgetGalleryTag.setOrganization("tenant");
//			sbiWidgetGalleryTag.setSbiVersionIn("sbiversionin");
//			sbiWidgetGalleryTag.setTimeIn(Timestamp.from(Instant.now()));
//			sbiWidgetGalleryTag.setUserIn(userid);
//			sbiWidgetGalleryTag.setSbiWidgetGallery(sbiWidgetGallery);
		}

	}

}
