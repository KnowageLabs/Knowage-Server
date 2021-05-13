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
import it.eng.spagobi.services.security.SpagoBIUserProfile;

@Component
public class WidgetGalleryAPIimpl implements WidgetGalleryAPI {

	@Autowired
	private SbiWidgetGalleryDao sbiWidgetGalleryDao;

	private static String GALLERY_FUNCTION = "WidgetGalleryManagement";

	/**
	 * This method gets all widgets within all tenants
	 */
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

	/**
	 * This method gets all widgets related the tenant set into profile
	 */
	@Override
	public List<WidgetGalleryDTO> getWidgetsByTenant(SpagoBIUserProfile profile) throws JSONException {
		List<WidgetGalleryDTO> ret = null;
		if (this.canSeeGallery(profile)) {
			List<SbiWidgetGallery> widgets = (List<SbiWidgetGallery>) sbiWidgetGalleryDao.findAllByTenant(profile.getOrganization());

			ret = widgets.stream().map(el -> {
				try {
					return mapTo(el);
				} catch (JSONException e) {
					throw new KnowageRuntimeException(e.getMessage());
				}
			}).collect(Collectors.toList());
		}
		return ret;
	}

	/**
	 * This method gets widget with @{id} within the tenant set into profile, or null
	 */
	@Override
	public WidgetGalleryDTO getWidgetsById(String id, SpagoBIUserProfile profile) throws JSONException {
		if (this.canSeeGallery(profile)) {
			SbiWidgetGallery widget = sbiWidgetGalleryDao.findByIdTenant(id, profile.getOrganization());
			if (widget != null) {
				updateGalleryCounter(widget);
				return mapTo(widget);
			}
		}

		return null;
	}

	private WidgetGalleryDTO mapTo(SbiWidgetGallery sbiWidgetGallery) throws JSONException {

		WidgetGalleryDTO toRet = new WidgetGalleryDTO();

		toRet.setAuthor(sbiWidgetGallery.getAuthor());
		toRet.setId(sbiWidgetGallery.getUuid());
		toRet.setName(sbiWidgetGallery.getName());
		toRet.setDescription(sbiWidgetGallery.getDescription());
		toRet.setType(sbiWidgetGallery.getType());
		if (sbiWidgetGallery.getPreviewImage() != null) {
			toRet.setImage(new String(sbiWidgetGallery.getPreviewImage()));
		}
		toRet.setOrganization(sbiWidgetGallery.getOrganization());
		toRet.setUsageCounter(sbiWidgetGallery.getUsageCounter());
		List<SbiWidgetGalleryTag> tagList = sbiWidgetGallery.getSbiWidgetGalleryTags();
		if (tagList != null && tagList.size() > 0) {
			List<String> tags = new ArrayList<String>();
			for (int i = 0; i < tagList.size(); i++) {
				tags.add(tagList.get(i).getId().getTag());
			}
			toRet.setTags(tags);
		}

		JSONObject jsonBody = new JSONObject(new String(sbiWidgetGallery.getTemplate()));

		Code code = new Code();
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
		toRet.setOutputType(sbiWidgetGallery.getOutputType());

		return toRet;
	}

	@Override
	public WidgetGalleryDTO makeNewWidget(WidgetGalleryDTO widgetGalleryDTO, SpagoBIUserProfile profile, boolean create) {
		if (this.canSeeGallery(profile)) {
			if (create) {
				widgetGalleryDTO.setId(generateType1UUID().toString());
			}

			SbiWidgetGallery newSbiWidgetGallery = new SbiWidgetGallery();
			newSbiWidgetGallery.setUuid(widgetGalleryDTO.getId());
			newSbiWidgetGallery.setAuthor(profile.getUserId());
			newSbiWidgetGallery.setDescription(widgetGalleryDTO.getDescription());
			newSbiWidgetGallery.setName(widgetGalleryDTO.getName());
			newSbiWidgetGallery.setOrganization(profile.getOrganization());
			newSbiWidgetGallery.setPreviewImage(widgetGalleryDTO.getImage() != null ? widgetGalleryDTO.getImage().getBytes() : "".getBytes());
			newSbiWidgetGallery.setSbiVersionIn("");
			newSbiWidgetGallery.setTemplate(widgetGalleryDTO.getTemplate().getBytes());
			newSbiWidgetGallery.setTimeIn(Timestamp.from(Instant.now()));
			newSbiWidgetGallery.setType(widgetGalleryDTO.getType());
			newSbiWidgetGallery.setUserIn(profile.getUserId());
			newSbiWidgetGallery.setUsageCounter(1);
			newSbiWidgetGallery.setOutputType(widgetGalleryDTO.getOutputType());

			String tags = widgetGalleryDTO.getTags().toString().equals("[]") ? "" : widgetGalleryDTO.getTags().toString();
			List<SbiWidgetGalleryTag> tagList = createNewWidgetTagsByList(newSbiWidgetGallery, profile.getUserId(), tags);
			if (tagList != null) {
				newSbiWidgetGallery.getSbiWidgetGalleryTags().addAll(tagList);
			}
			sbiWidgetGalleryDao.create(newSbiWidgetGallery);
		}
		return widgetGalleryDTO;
	}

	public WidgetGalleryDTO importNewWidget(WidgetGalleryDTO widgetGalleryDTO, SpagoBIUserProfile profile) {
		return makeNewWidget(widgetGalleryDTO, profile, false);
	}

	@Override
	public WidgetGalleryDTO updateWidget(WidgetGalleryDTO widgetGalleryDTO, SpagoBIUserProfile profile) {
		if (this.canSeeGallery(profile)) {
			SbiWidgetGallery newSbiWidgetGallery = new SbiWidgetGallery();
			newSbiWidgetGallery.setUuid(widgetGalleryDTO.getId());
			newSbiWidgetGallery.setAuthor(profile.getUserId());
			newSbiWidgetGallery.setDescription(widgetGalleryDTO.getDescription());
			newSbiWidgetGallery.setName(widgetGalleryDTO.getName());
			newSbiWidgetGallery.setOrganization(profile.getOrganization());
			newSbiWidgetGallery.setPreviewImage(widgetGalleryDTO.getImage() != null ? widgetGalleryDTO.getImage().getBytes() : "".getBytes());
			newSbiWidgetGallery.setSbiVersionIn("");
			newSbiWidgetGallery.setTemplate(widgetGalleryDTO.getTemplate().getBytes());
			newSbiWidgetGallery.setTimeIn(Timestamp.from(Instant.now()));
			newSbiWidgetGallery.setType(widgetGalleryDTO.getType());
			newSbiWidgetGallery.setUserIn(profile.getUserId());
			newSbiWidgetGallery.setOutputType(widgetGalleryDTO.getOutputType());
			String tags = widgetGalleryDTO.getTags().toString().equals("[]") ? "" : widgetGalleryDTO.getTags().toString();
			List<SbiWidgetGalleryTag> tagList = createNewWidgetTagsByList(newSbiWidgetGallery, profile.getUserId(), tags);
			if (tagList != null) {
				newSbiWidgetGallery.getSbiWidgetGalleryTags().addAll(tagList);
			}
			sbiWidgetGalleryDao.update(newSbiWidgetGallery);
		}
		return widgetGalleryDTO;
	}

	@Override
	public void updateGalleryCounter(SbiWidgetGallery newSbiWidgetGallery) {
		sbiWidgetGalleryDao.updateCounter(newSbiWidgetGallery);
	}

	@Override
	public int deleteGallery(String id, SpagoBIUserProfile profile) {
		if (this.canSeeGallery(profile)) {
			return sbiWidgetGalleryDao.deleteByIdTenant(id, profile.getOrganization());
		}
		return 0;
	}

	@Override
	public List<SbiWidgetGalleryTag> createNewWidgetTagsByList(SbiWidgetGallery sbiWidgetGallery, String userid, String tags) {

		List<SbiWidgetGalleryTag> tagList = null;
		if (tags.length() > 0) {
			tags = tags.substring(1, tags.length() - 1);

			String[] tagArray = tags.split(",");

			tagList = new ArrayList<SbiWidgetGalleryTag>();
			ArrayList<String> insertedTags = new ArrayList<String>();
			for (int i = 0; i < tagArray.length; i++) {

				tagArray[i] = tagArray[i].trim().replaceAll("\"", "");
				if (insertedTags.contains(tagArray[i])) {
					throw new KnowageRuntimeException("Cannot insert duplicate tags, please select different ones");
				}
				insertedTags.add(tagArray[i]);
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

	/*
	 * Permission methods
	 */
	@Override
	public boolean canSeeGallery(SpagoBIUserProfile userProfile) {
		boolean canSee = false;
		for (String function : userProfile.getFunctions()) {
			if (function.equalsIgnoreCase(GALLERY_FUNCTION)) {
				canSee = true;
			}
		}
		return canSee;
	}

	@Override
	public List<WidgetGalleryDTO> getWidgetsByTenantType(SpagoBIUserProfile profile, String type) throws JSONException {
		List<WidgetGalleryDTO> ret = null;
		if (this.canSeeGallery(profile)) {
			List<SbiWidgetGallery> widgets = (List<SbiWidgetGallery>) sbiWidgetGalleryDao.findAllByTenantAndType(profile.getOrganization(), type);

			ret = widgets.stream().map(el -> {
				try {
					return mapTo(el);
				} catch (JSONException e) {
					throw new KnowageRuntimeException(e.getMessage());
				}
			}).collect(Collectors.toList());
		}
		return ret;
	}

	@Override
	public WidgetGalleryDTO importOrUpdateWidget(WidgetGalleryDTO widgetGalleryDTO, SpagoBIUserProfile profile) throws JSONException {
		WidgetGalleryDTO existingWidget = getWidgetsById(widgetGalleryDTO.getId(), profile);
		WidgetGalleryDTO newSbiWidgetGallery = null;
		if (existingWidget == null) {
			newSbiWidgetGallery = importNewWidget(widgetGalleryDTO, profile);
		} else {
			newSbiWidgetGallery = updateWidget(widgetGalleryDTO, profile);
		}

		return newSbiWidgetGallery;
	}

}
