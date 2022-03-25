package it.eng.knowage.resourcemanager;

import java.net.MalformedURLException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.factory.SecurityServiceFactory;
import it.eng.knowage.boot.factory.SecurityServiceFactoryTest;
import it.eng.knowage.knowageapi.service.FunctionCatalogAPI;
import it.eng.knowage.knowageapi.service.impl.FunctionCatalogAPIImplTest;
import it.eng.spagobi.services.security.SpagoBIUserProfile;
import it.eng.spagobi.services.security.SpagoBIUserProfile.Attributes.Entry;

@Configuration
@Profile("test")
@ComponentScan("it.eng.knowage.resourcemanager")
public class KnowageResourceManagerConfigurationTest {

	@Bean
	public BusinessRequestContext businessRequestContext(@Value("${application.version}") String version) {
		Entry entry = new Entry();

		entry.setKey("test");
		entry.setValue("test");

		SpagoBIUserProfile.Attributes attributes = new SpagoBIUserProfile.Attributes();

		attributes.getEntry().add(entry);

		SpagoBIUserProfile userProfile = new SpagoBIUserProfile();

		userProfile.setAttributes(attributes);
		userProfile.setIsSuperadmin(true);
		userProfile.setOrganization("DEFAULT_TENANT");
		userProfile.setUniqueIdentifier("biadmin");
		userProfile.setUserId("biadmin");
		userProfile.setUserName("biadmin");
		userProfile.getFunctions().add("WidgetGalleryManagement");

		BusinessRequestContext businessRequestContext = new BusinessRequestContext(version);
		businessRequestContext.setUsername("biadmin");
		businessRequestContext.setOrganization("DEFAULT_TENANT");
		businessRequestContext.setUserProfile(userProfile);
		return businessRequestContext;
	}

	@Bean
	public FunctionCatalogAPI functionCatalogAPI() {
		return new FunctionCatalogAPIImplTest();
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Lazy
	@Bean
	public SecurityServiceFactory securityService() throws NamingException, MalformedURLException {
		return new SecurityServiceFactoryTest();
	}

	@Bean(name = "multipartResolver")
	public CommonsMultipartResolver multipartResolver() {
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
		multipartResolver.setMaxUploadSize(100000);
		return multipartResolver;
	}

	@Bean
	public Context context() throws NamingException {

		Hashtable env = new Hashtable();
		env.remove("org.osjava.sj.jndi.ignoreClose");
		env.put("java.naming.factory.initial", "org.osjava.sj.SimpleJndiContextFactory");
		InitialContext initialContext = new InitialContext(env);

		initialContext.bind("java:/comp/env/hmacKey", "abc123");

		return initialContext;
	}
}
