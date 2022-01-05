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

@Component
public class SysDirectoriesUtils {

	private static final String DATA_PREPARATION_SUB_DIR = "data-preparation";

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
		return getTenantPathAsString(brc.getOrganization());
	}

	public File getTenantPathAsFile(BusinessRequestContext brc) {
		return getTenantPathAsFile(brc.getOrganization());
	}

	public Path getTenantPath(BusinessRequestContext brc) {
		return getTenantPath(brc.getOrganization());
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

}
