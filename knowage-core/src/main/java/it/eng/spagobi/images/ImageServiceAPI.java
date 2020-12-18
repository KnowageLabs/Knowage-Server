package it.eng.spagobi.images;

import java.util.Arrays;
import java.util.List;

import org.apache.clerezza.jaxrs.utils.form.FormFile;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.images.dao.IImagesDAO;
import it.eng.spagobi.user.UserProfileManager;

public class ImageServiceAPI {
	private static final String IMAGE_GALLERY_MAX_IMAGE_SIZE = "IMAGE_GALLERY.MAX_IMAGE_SIZE_KB";
	private static final String IMAGE_GALLERY_MAX_USER_IMAGES = "IMAGE_GALLERY.MAX_USER_IMAGES";
	private static final String IMAGE_GALLERY_MAX_TENANT_IMAGES = "IMAGE_GALLERY.MAX_TENANT_IMAGES";
	private final long defaultMaxImageSize = 1024;
	private final long defaultMaxUserImages = 10;
	private final long defaultMaxTenantImages = 100;

	protected boolean isTooBig(FormFile file) {
		byte[] data = file.getContent();
		return data.length > getParamValue(IMAGE_GALLERY_MAX_IMAGE_SIZE, defaultMaxImageSize) * 1024;
	}

	protected boolean isTooManyImageForTenant() {
		IImagesDAO dao = DAOFactory.getImagesDAO();
		IEngUserProfile profile = UserProfileManager.getProfile();
		dao.setUserProfile(profile);
		long numImagesByTenant = dao.countImages(false);
		return numImagesByTenant >= getParamValue(IMAGE_GALLERY_MAX_TENANT_IMAGES, defaultMaxTenantImages);
	}

	protected boolean isTooManyImageForUser() {
		IImagesDAO dao = DAOFactory.getImagesDAO();
		IEngUserProfile profile = UserProfileManager.getProfile();
		dao.setUserProfile(profile);
		long numImagesByUser = dao.countImages(true);
		return (numImagesByUser >= getParamValue(IMAGE_GALLERY_MAX_USER_IMAGES, defaultMaxUserImages));

	}

	protected boolean isAnImage(FormFile file) {
		return file.getMediaType().getType().equals("image");
	}

	protected boolean isValidFileExtension(FormFile file) {
		List<String> validFileExtensions = Arrays.asList("JPG", "JPEG", "PNG", "GIF", "TIFF");
		return validFileExtensions.contains(file.getMediaType().getSubtype().toUpperCase());
	}

	public static long getParamValue(String paramName, long defaultValue) {
		long ret = defaultValue;
		try {
			String size = SingletonConfig.getInstance().getConfigValue(paramName);
			if (size != null && size.matches("\\d+"))
				ret = Long.parseLong(size);
		} catch (NumberFormatException e) {
		}
		return ret;
	}
}
