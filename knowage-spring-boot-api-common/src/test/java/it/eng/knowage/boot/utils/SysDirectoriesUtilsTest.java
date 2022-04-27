package it.eng.knowage.boot.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import it.eng.knowage.boot.configuration.MainConfigurationTest;

@SpringBootTest
@SpringBootConfiguration
@ContextConfiguration(classes = { MainConfigurationTest.class })
@ActiveProfiles("test")
class SysDirectoriesUtilsTest {

	private static final String RESOURCE_PATH = "D:\\tmp\\resources";
	private static final String TENANT_NAME = "DEFAULT_TENANT";
	private static final String USER_ID = "biadmin";

	@Autowired
	private SysDirectoriesUtils sysDirectoriesUtils;

	@Test
	void mainTest() {

		assertEquals(RESOURCE_PATH, sysDirectoriesUtils.getResourcePath().toString());
		assertEquals(RESOURCE_PATH + File.separator + TENANT_NAME, sysDirectoriesUtils.getTenantPath(TENANT_NAME).toString());
		assertEquals(RESOURCE_PATH + File.separator + TENANT_NAME + File.separator + "dataPreparation",
				sysDirectoriesUtils.getDataPreparationPath(TENANT_NAME, USER_ID).toString());

	}
}
