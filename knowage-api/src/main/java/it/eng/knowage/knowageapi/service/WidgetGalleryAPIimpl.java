package it.eng.knowage.knowageapi.service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.knowage.knowageapi.dao.SbiWidgetGalleryDao;
import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGallery;
import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGalleryTag;
import it.eng.knowage.knowageapi.dao.dto.SbiWidgetGalleryTagId;
import it.eng.knowage.knowageapi.error.KnowageRuntimeException;
import it.eng.knowage.knowageapi.resource.dto.Code;
import it.eng.knowage.knowageapi.resource.dto.WidgetGalleryDTO;

@Component
public class WidgetGalleryAPIimpl implements WidgetGalleryAPI {

	@Autowired
	private SbiWidgetGalleryDao sbiWidgetGalleryDao;

	@Override
	public List<WidgetGalleryDTO> getWidgets() throws JSONException {

		List<SbiWidgetGallery> widgets = (List<SbiWidgetGallery>) sbiWidgetGalleryDao.findAll();

		List<WidgetGalleryDTO> ret = widgets.stream().map(el -> {
			try {
				return mapTo(el);
			} catch (JSONException e) {
				throw new KnowageRuntimeException(e.getMessage());
			}
		}).collect(Collectors.toList());

		return ret;
	}

	@Override
	public List<WidgetGalleryDTO> getWidgetsByTenant(String tenant) throws JSONException {

		List<SbiWidgetGallery> widgets = (List<SbiWidgetGallery>) sbiWidgetGalleryDao.findAllByTenant(tenant);

		List<WidgetGalleryDTO> ret = widgets.stream().map(el -> {
			try {
				return mapTo(el);
			} catch (JSONException e) {
				throw new KnowageRuntimeException(e.getMessage());
			}
		}).collect(Collectors.toList());

		return ret;
	}

	@Override
	public WidgetGalleryDTO getWidgetsById(String id, String tenant) throws JSONException {

		SbiWidgetGallery widget = sbiWidgetGalleryDao.findByIdTenant(id, tenant);
		updateGalleryCounter(widget);
		return mapTo(widget);
	}

	private WidgetGalleryDTO mapTo(SbiWidgetGallery sbiWidgetGallery) throws JSONException {

		WidgetGalleryDTO toRet = new WidgetGalleryDTO();

		toRet.setAuthor(sbiWidgetGallery.getAuthor());
		toRet.setId(sbiWidgetGallery.getUuid());
		toRet.setName(sbiWidgetGallery.getName());
		toRet.setDescription(sbiWidgetGallery.getDescription());
		toRet.setType(sbiWidgetGallery.getType());
		toRet.setImage(sbiWidgetGallery.getPreviewImage());
		toRet.setOrganization(sbiWidgetGallery.getOrganization());
		toRet.setUsageCounter(sbiWidgetGallery.getUsageCounter());
		List<SbiWidgetGalleryTag> tagList = sbiWidgetGallery.getSbiWidgetGalleryTags();
		toRet.setLicenseText(sbiWidgetGallery.getLicenseText());
		toRet.setLicenseName(sbiWidgetGallery.getLicenseName());
		if (tagList != null && tagList.size() > 0) {
			List<String> tags = new ArrayList<String>();
			for (int i = 0; i < tagList.size(); i++) {
				tags.add(tagList.get(i).getId().getTag());
			}
			toRet.setTags(tags);
		}
		Code code = new Code();
		JSONObject jsonBody;

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

		return toRet;
	}

	@Override
	public WidgetGalleryDTO createNewGallery(String name, String type, String author, String description, String licenseText, String licenseName,
			String organization, String image, String sbiversion, String template, String userid, String tags) {

		WidgetGalleryDTO widgetGalleryDTO = createWidgetGalleryDTO(name, type, author, description, licenseText, licenseName, organization, image, sbiversion,
				template, userid, tags);

		SbiWidgetGallery newSbiWidgetGallery = new SbiWidgetGallery();
		newSbiWidgetGallery.setUuid(widgetGalleryDTO.getId());
		newSbiWidgetGallery.setAuthor(author);
		newSbiWidgetGallery.setDescription(description);
		newSbiWidgetGallery.setLicenseText(licenseText);
		newSbiWidgetGallery.setLicenseName(licenseName);
		newSbiWidgetGallery.setName(name);
		newSbiWidgetGallery.setOrganization(organization);
		newSbiWidgetGallery.setPreviewImage(widgetGalleryDTO.getImage());
		newSbiWidgetGallery.setSbiVersionIn(sbiversion);
		newSbiWidgetGallery.setTemplate(template);
		newSbiWidgetGallery.setTimeIn(Timestamp.from(Instant.now()));
		newSbiWidgetGallery.setType(type);
		newSbiWidgetGallery.setUserIn(userid);
		newSbiWidgetGallery.setUsageCounter(1);
		List<SbiWidgetGalleryTag> tagList = createNewWidgetTagsByList(newSbiWidgetGallery, userid, tags);
		newSbiWidgetGallery.getSbiWidgetGalleryTags().addAll(tagList);

		sbiWidgetGalleryDao.create(newSbiWidgetGallery);

		return widgetGalleryDTO;

	}

	@Override
	public void updateGallery(String uuid, String name, String type, String author, String description, String licenseText, String licenseName,
			String organization, String image, String sbiversion, String template, String userid, String tags) {

		SbiWidgetGallery newSbiWidgetGallery = new SbiWidgetGallery();
		newSbiWidgetGallery.setUuid(uuid);
		newSbiWidgetGallery.setAuthor(author);
		newSbiWidgetGallery.setDescription(description);
		newSbiWidgetGallery.setLicenseText(licenseText);
		newSbiWidgetGallery.setLicenseName(licenseName);
		newSbiWidgetGallery.setName(name);
		newSbiWidgetGallery.setOrganization(organization);
		newSbiWidgetGallery.setPreviewImage(image);
		newSbiWidgetGallery.setSbiVersionUp(sbiversion);
		newSbiWidgetGallery.setTemplate(template);
		newSbiWidgetGallery.setTimeUp(Timestamp.from(Instant.now()));
		newSbiWidgetGallery.setType(type);
		newSbiWidgetGallery.setUserUp(userid);
		List<SbiWidgetGalleryTag> tagList = createNewWidgetTagsByList(newSbiWidgetGallery, userid, tags);
		newSbiWidgetGallery.getSbiWidgetGalleryTags().addAll(tagList);
		sbiWidgetGalleryDao.update(newSbiWidgetGallery);
	}

	@Override
	public void updateGalleryCounter(SbiWidgetGallery newSbiWidgetGallery) {
		sbiWidgetGalleryDao.updateCounter(newSbiWidgetGallery);
	}

	@Override
	public int deleteGallery(String id, String tenant) {
		return sbiWidgetGalleryDao.deleteByIdTenant(id, tenant);
	}

	@Override
	public WidgetGalleryDTO createWidgetGalleryDTO(String name, String type, String author, String description, String licenseText, String licenseName,
			String organization, String image, String sbiversion, String template, String userid, String tags) {

		UUID uuidGenerated = generateType1UUID();
		WidgetGalleryDTO newSbiWidgetGallery = new WidgetGalleryDTO();
		newSbiWidgetGallery.setId(uuidGenerated.toString());
		newSbiWidgetGallery.setAuthor(author);
		newSbiWidgetGallery.setDescription(description);
		newSbiWidgetGallery.setLicenseText(licenseText);
		newSbiWidgetGallery.setLicenseName(licenseName);
		newSbiWidgetGallery.setName(name);
		newSbiWidgetGallery.setOrganization(organization);
		newSbiWidgetGallery.setImage(image);
		newSbiWidgetGallery.setSbiversion(sbiversion);
		newSbiWidgetGallery.setTemplate(template);
		newSbiWidgetGallery.setTimestamp(Timestamp.from(Instant.now()));
		newSbiWidgetGallery.setType(type);
		newSbiWidgetGallery.setUser(userid);
		return newSbiWidgetGallery;

	}

	@Override
	public List<SbiWidgetGalleryTag> createNewWidgetTagsByList(SbiWidgetGallery sbiWidgetGallery, String userid, String tags) {

		List<SbiWidgetGalleryTag> tagList = new ArrayList<SbiWidgetGalleryTag>();
		tags = tags.substring(1, tags.length() - 1);

		String[] tagArray = tags.split(",");

		for (int i = 0; i < tagArray.length; i++) {

			tagArray[i] = tagArray[i].trim().replaceAll("\"", "");
			SbiWidgetGalleryTag sbiWidgetGalleryTag = new SbiWidgetGalleryTag();
			SbiWidgetGalleryTagId newId = new SbiWidgetGalleryTagId(tagArray[i], sbiWidgetGallery.getUuid());
			sbiWidgetGalleryTag.setId(newId);
			sbiWidgetGalleryTag.setOrganization(sbiWidgetGallery.getOrganization());
			sbiWidgetGalleryTag.setSbiVersionIn("");
			sbiWidgetGalleryTag.setTimeIn(Timestamp.from(Instant.now()));
			sbiWidgetGalleryTag.setUserIn(userid);
			sbiWidgetGalleryTag.setSbiWidgetGallery(sbiWidgetGallery);

			tagList.add(sbiWidgetGalleryTag);
		}

		return tagList;
	}

	public static UUID generateType1UUID() {

		long most64SigBits = get64MostSignificantBitsForVersion1();
		long least64SigBits = get64LeastSignificantBitsForVersion1();

		return new UUID(most64SigBits, least64SigBits);
	}

	private static long get64LeastSignificantBitsForVersion1() {
		Random random = new Random();
		long random63BitLong = random.nextLong() & 0x3FFFFFFFFFFFFFFFL;
		long variant3BitFlag = 0x8000000000000000L;
		return random63BitLong + variant3BitFlag;
	}

	private static long get64MostSignificantBitsForVersion1() {
		LocalDateTime start = LocalDateTime.of(1582, 10, 15, 0, 0, 0);
		Duration duration = Duration.between(start, LocalDateTime.now());
		long seconds = duration.getSeconds();
		long nanos = duration.getNano();
		long timeForUuidIn100Nanos = seconds * 10000000 + nanos * 100;
		long least12SignificatBitOfTime = (timeForUuidIn100Nanos & 0x000000000000FFFFL) >> 4;
		long version = 1 << 12;
		return (timeForUuidIn100Nanos & 0xFFFFFFFFFFFF0000L) + version + least12SignificatBitOfTime;
	}
}
