package it.eng.knowage.boot.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.naming.Context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.error.KnowageRuntimeException;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

@Component
public class SysDirectoriesUtils {

	private static final String DATA_PREPARATION_SUB_DIR = "dataPreparation";

	private static final SysDirectoriesUtils INSTANCE = new SysDirectoriesUtils();

	public static SysDirectoriesUtils getInstance() {
		return INSTANCE;
	}

	@Value("${jndi.lookup.hmackey}")
	private String hmacKey;

	@Value("${jndi.lookup.resourcepath}")
	private String resourcePathKey;

	@Autowired
	private Context ctx;

	private SysDirectoriesUtils() {
		super();
	}

	public String getDataPreparationPathAsString(BusinessRequestContext brc) {
		return getDataPreparationPathAsString(getOrganizationFromBusinessContext(brc));
	}

	public File getDataPreparationPathAsFile(BusinessRequestContext brc) {
		return getDataPreparationPathAsFile(getOrganizationFromBusinessContext(brc));
	}

	public Path getDataPreparationPath(BusinessRequestContext brc) {
		return getDataPreparationPath(getOrganizationFromBusinessContext(brc));
	}



	public String getDataPreparationPathAsString(SpagoBIUserProfile up) {
		return getDataPreparationPathAsString(getOrganizationFromUserProfile(up));
	}

	public File getDataPreparationPathAsFile(SpagoBIUserProfile up) {
		return getDataPreparationPathAsFile(getOrganizationFromUserProfile(up));
	}

	public Path getDataPreparationPath(SpagoBIUserProfile up) {
		return getDataPreparationPath(getOrganizationFromUserProfile(up));
	}



	public String getDataPreparationPathAsString(String tenantId) {
		return getDataPreparationPathAsFile(tenantId).toString();
	}

	public File getDataPreparationPathAsFile(String tenantId) {
		return getDataPreparationPath(tenantId).toFile();
	}

	public Path getDataPreparationPath(String tenantId) {
		return getTenantPath(tenantId).resolve(DATA_PREPARATION_SUB_DIR);
	}



	public String getTenantPathAsString(BusinessRequestContext brc) {
		return getTenantPathAsString(getOrganizationFromBusinessContext(brc));
	}

	public File getTenantPathAsFile(BusinessRequestContext brc) {
		return getTenantPathAsFile(getOrganizationFromBusinessContext(brc));
	}

	public Path getTenantPath(BusinessRequestContext brc) {
		return getTenantPath(getOrganizationFromBusinessContext(brc));
	}



	public String getTenantPathAsString(String tenantId) {
		return getTenantPathAsFile(tenantId).toString();
	}

	public File getTenantPathAsFile(String tenantId) {
		return getTenantPath(tenantId).toFile();
	}

	public Path getTenantPath(String tenantId) {
		return getResourcePath().resolve(tenantId);
	}



	public String getResourcePathAsString() {
		return getResourcePathAsFile().toString();
	}

	public File getResourcePathAsFile() {
		return getResourcePath().toFile();
	}

	public Path getResourcePath() {
		String resourcePath = null;
		try {
			resourcePath = (String) ctx.lookup(resourcePathKey);
		} catch (Exception e) {
			throw new KnowageRuntimeException("Cannot get resource path from JNDI name " + resourcePathKey, e);
		}
		return Paths.get(resourcePath);
	}

	private String getOrganizationFromBusinessContext(BusinessRequestContext brc) {
		return brc.getOrganization();
	}

	private String getOrganizationFromUserProfile(SpagoBIUserProfile up) {
		return up.getOrganization();
	}

}
