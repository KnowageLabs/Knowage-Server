package it.eng.knowage.knowageapi.provider;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.Priority;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.dao.SbiConfigDao;
import it.eng.knowage.boot.dao.dto.SbiConfig;
import it.eng.knowage.boot.error.KnowageRuntimeException;
import it.eng.spagobi.services.security.SecurityServiceService;

@Provider
@Priority(Priorities.AUTHENTICATION)
@Component
public class LocaleInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

	private static final Logger LOGGER = Logger.getLogger(LocaleInterceptor.class);

	private static String KNOWAGE_LOCALE = "kn.lang";

	private static String KNOWAGE_DEFAULT_BE_LANGUAGE_LABEL = "SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGE.default";

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		LOGGER.info("FILTER OUT");
	}

	@Autowired
	@Lazy
	SecurityServiceService securityServiceService;

	@Autowired
	private BusinessRequestContext businessRequestContext;

	@Autowired
	private SbiConfigDao sbiConfigDao;

	@Autowired
	private HttpServletRequest request;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		LOGGER.info("FILTER IN");
		try {
			if (readServletCookie(request, KNOWAGE_LOCALE).isPresent()) {
				String localeFromCookie = readServletCookie(request, KNOWAGE_LOCALE).get();
				Locale newLocale = Locale.forLanguageTag(localeFromCookie);
				businessRequestContext.setLocale(newLocale);
			} else {
				SbiConfig defaultBackendConfig = sbiConfigDao.findByLabel(KNOWAGE_DEFAULT_BE_LANGUAGE_LABEL);
				String languageTag = defaultBackendConfig.getValueCheck();
				Locale locale = Locale.forLanguageTag(languageTag);
				businessRequestContext.setLocale(locale);
			}

		} catch (Exception e) {
			throw new KnowageRuntimeException("Impossible to get Locale from service", e);
		}

	}

	public Optional<String> readServletCookie(HttpServletRequest request, String name) {
		if (request.getCookies() != null) {
			return Arrays.stream(request.getCookies()).filter(cookie -> name.equals(cookie.getName())).map(Cookie::getValue).findAny();
		} else {
			return Optional.empty();
		}
	}

}
