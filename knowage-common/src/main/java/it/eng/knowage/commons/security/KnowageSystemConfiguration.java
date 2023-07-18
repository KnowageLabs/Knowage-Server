package it.eng.knowage.commons.security;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author albnale
 * @since 2020/08/26
 *
 *        This class aims to be used for "immutable" configurations. These configurations are set once and never modified.
 */

public class KnowageSystemConfiguration {

	private static final String KNOWAGE_CORE_DEFAULT_CONTEXT = "/knowage";
	private static final String KNOWAGE_CORE_CONTEXT = "knowage.core.context";

	private static final String KNOWAGE_API_DEFAULT_CONTEXT = "/knowage-api";
	private static final String KNOWAGE_API_CONTEXT = "knowage.api.context";

	private static final String KNOWAGEBIRTREPORTENGINE_DEFAULT_CONTEXT = "/knowagebirtreportengine";
	private static final String KNOWAGEBIRTREPORTENGINE_CONTEXT = "knowagebirtreportengine.context";

	private static final String KNOWAGECOCKPITENGINE_DEFAULT_CONTEXT = "/knowagecockpitengine";
	private static final String KNOWAGECOCKPITENGINE_CONTEXT = "knowagecockpitengine.context";

	private static final String KNOWAGECOMMONJENGINE_DEFAULT_CONTEXT = "/knowagecommonjengine";
	private static final String KNOWAGECOMMONJENGINE_CONTEXT = "knowagecommonjengine.context";

	private static final String KNOWAGEDATAPREPARATION_DEFAULT_CONTEXT = "/knowage-data-preparation";
	private static final String KNOWAGEDATAPREPARATION_CONTEXT = "knowage.data.preparation.context";

	private static final String KNOWAGEDOSSIERENGINE_DEFAULT_CONTEXT = "/knowagedossierengine";
	private static final String KNOWAGEDOSSIERENGINE_CONTEXT = "knowagedossierengine.context";

	private static final String KNOWAGEGEOREPORTENGINE_DEFAULT_CONTEXT = "/knowagegeoreportengine";
	private static final String KNOWAGEGEOREPORTENGINE_CONTEXT = "knowagegeoreportengine.context";

	private static final String KNOWAGEJASPERREPORTENGINE_DEFAULT_CONTEXT = "/knowagejasperreportengine";
	private static final String KNOWAGEJASPERREPORTENGINE_CONTEXT = "knowagejasperreportengine.context";

	private static final String KNOWAGEKPIENGINE_DEFAULT_CONTEXT = "/knowagekpiengine";
	private static final String KNOWAGEKPIENGINE_CONTEXT = "knowagekpiengine.context";

	private static final String KNOWAGEMETA_DEFAULT_CONTEXT = "/knowagemeta";
	private static final String KNOWAGEMETA_CONTEXT = "knowagemeta.context";

	private static final String KNOWAGEQBEENGINE_DEFAULT_CONTEXT = "/knowageqbeengine";
	private static final String KNOWAGEQBEENGINE_CONTEXT = "knowageqbeengine.context";

	private static final String KNOWAGESVGVIEWERENGINE_DEFAULT_CONTEXT = "/knowagesvgviewerengine";
	private static final String KNOWAGESVGVIEWERENGINE_CONTEXT = "knowagesvgviewerengine.context";

	private static final String KNOWAGETALENDENGINE_DEFAULT_CONTEXT = "/knowagetalendengine";
	private static final String KNOWAGETALENDENGINE_CONTEXT = "knowagetalendengine.context";

	private static final String KNOWAGE_VUE_DEFAULT_CONTEXT = "/knowage-vue";
	private static final String KNOWAGE_VUE_CONTEXT = "knowage.vue.context";

	private static final String KNOWAGEWHATIFENGINE_DEFAULT_CONTEXT = "/knowagewhatifengine";
	private static final String KNOWAGEWHATIFENGINE_CONTEXT = "knowagewhatifengine.context";

	private static final Logger LOGGER = LogManager.getLogger(KnowageSystemConfiguration.class);

	private KnowageSystemConfiguration() {
	}

	public static String getKnowageContext() {
		LOGGER.debug("IN");
		String path = "";
		try {
			path = Optional.ofNullable(System.getProperty(KNOWAGE_CORE_CONTEXT, System.getenv(KNOWAGE_CORE_CONTEXT))).orElse(KNOWAGE_CORE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowage context address", e);
		}
		LOGGER.debug("OUT:" + path);
		return path;
	}

	public static String getKnowageAPIContext() {
		LOGGER.debug("IN");
		String path = "";
		try {
			path = Optional.ofNullable(System.getProperty(KNOWAGE_API_CONTEXT, System.getenv(KNOWAGE_API_CONTEXT))).orElse(KNOWAGE_API_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowage-api context address", e);
		}
		LOGGER.debug("OUT:" + path);
		return path;
	}

	public static String getKnowageBirtReportEngineContext() {
		LOGGER.debug("IN");
		String path = "";
		try {
			path = Optional.ofNullable(System.getProperty(KNOWAGEBIRTREPORTENGINE_CONTEXT, System.getenv(KNOWAGEBIRTREPORTENGINE_CONTEXT)))
					.orElse(KNOWAGEBIRTREPORTENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagebirtreportengine context address", e);
		}
		LOGGER.debug("OUT:" + path);
		return path;
	}

	public static String getKnowageCockpitEngineContext() {
		LOGGER.debug("IN");
		String path = "";
		try {
			path = Optional.ofNullable(System.getProperty(KNOWAGECOCKPITENGINE_CONTEXT, System.getenv(KNOWAGECOCKPITENGINE_CONTEXT)))
					.orElse(KNOWAGECOCKPITENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagecockpitengine context address", e);
		}
		LOGGER.debug("OUT:" + path);
		return path;
	}

	public static String getKnowageCommonjEngineContext() {
		LOGGER.debug("IN");
		String path = "";
		try {
			path = Optional.ofNullable(System.getProperty(KNOWAGECOMMONJENGINE_CONTEXT, System.getenv(KNOWAGECOMMONJENGINE_CONTEXT)))
					.orElse(KNOWAGECOMMONJENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagecommonjengine context address", e);
		}
		LOGGER.debug("OUT:" + path);
		return path;
	}

	public static String getKnowageDataPreparationContext() {
		LOGGER.debug("IN");
		String path = "";
		try {
			path = Optional.ofNullable(System.getProperty(KNOWAGEDATAPREPARATION_CONTEXT, System.getenv(KNOWAGEDATAPREPARATION_CONTEXT)))
					.orElse(KNOWAGEDATAPREPARATION_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowage-data-preparation context address", e);
		}
		LOGGER.debug("OUT:" + path);
		return path;
	}

	public static String getKnowageDossierEngineContext() {
		LOGGER.debug("IN");
		String path = "";
		try {
			path = Optional.ofNullable(System.getProperty(KNOWAGEDOSSIERENGINE_CONTEXT, System.getenv(KNOWAGEDOSSIERENGINE_CONTEXT)))
					.orElse(KNOWAGEDOSSIERENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagedossierengine context address", e);
		}
		LOGGER.debug("OUT:" + path);
		return path;
	}

	public static String getKnowageGeoReportEngineContext() {
		LOGGER.debug("IN");
		String path = "";
		try {
			path = Optional.ofNullable(System.getProperty(KNOWAGEGEOREPORTENGINE_CONTEXT, System.getenv(KNOWAGEGEOREPORTENGINE_CONTEXT)))
					.orElse(KNOWAGEGEOREPORTENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagegeoreportengine context address", e);
		}
		LOGGER.debug("OUT:" + path);
		return path;
	}

	public static String getKnowageJasperReportEngineContext() {
		LOGGER.debug("IN");
		String path = "";
		try {
			path = Optional.ofNullable(System.getProperty(KNOWAGEJASPERREPORTENGINE_CONTEXT, System.getenv(KNOWAGEJASPERREPORTENGINE_CONTEXT)))
					.orElse(KNOWAGEJASPERREPORTENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagejasperreportengine context address", e);
		}
		LOGGER.debug("OUT:" + path);
		return path;
	}

	public static String getKnowageKpiEngineContext() {
		LOGGER.debug("IN");
		String path = "";
		try {
			path = Optional.ofNullable(System.getProperty(KNOWAGEKPIENGINE_CONTEXT, System.getenv(KNOWAGEKPIENGINE_CONTEXT)))
					.orElse(KNOWAGEKPIENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagekpiengine context address", e);
		}
		LOGGER.debug("OUT:" + path);
		return path;
	}

	public static String getKnowageMetaContext() {
		LOGGER.debug("IN");
		String path = "";
		try {
			path = Optional.ofNullable(System.getProperty(KNOWAGEMETA_CONTEXT, System.getenv(KNOWAGEMETA_CONTEXT))).orElse(KNOWAGEMETA_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagemeta context address", e);
		}
		LOGGER.debug("OUT:" + path);
		return path;
	}

	public static String getKnowageQbeEngineContext() {
		LOGGER.debug("IN");
		String path = "";
		try {
			path = Optional.ofNullable(System.getProperty(KNOWAGEQBEENGINE_CONTEXT, System.getenv(KNOWAGEQBEENGINE_CONTEXT)))
					.orElse(KNOWAGEQBEENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowageqbeengine context address", e);
		}
		LOGGER.debug("OUT:" + path);
		return path;
	}

	public static String getKnowageSvgViewerEngineContext() {
		LOGGER.debug("IN");
		String path = "";
		try {
			path = Optional.ofNullable(System.getProperty(KNOWAGESVGVIEWERENGINE_CONTEXT, System.getenv(KNOWAGESVGVIEWERENGINE_CONTEXT)))
					.orElse(KNOWAGESVGVIEWERENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagesvgviewerengine context address", e);
		}
		LOGGER.debug("OUT:" + path);
		return path;
	}

	public static String getKnowageTalendEngineContext() {
		LOGGER.debug("IN");
		String path = "";
		try {
			path = Optional.ofNullable(System.getProperty(KNOWAGETALENDENGINE_CONTEXT, System.getenv(KNOWAGETALENDENGINE_CONTEXT)))
					.orElse(KNOWAGETALENDENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagetalendengine context address", e);
		}
		LOGGER.debug("OUT:" + path);
		return path;
	}

	public static String getKnowageVueContext() {
		LOGGER.debug("IN");
		String path = "";
		try {
			path = Optional.ofNullable(System.getProperty(KNOWAGE_VUE_CONTEXT, System.getenv(KNOWAGE_VUE_CONTEXT))).orElse(KNOWAGE_VUE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowage-vue context address", e);
		}
		LOGGER.debug("OUT:" + path);
		return path;
	}

	public static String getKnowageWhatifEngineContext() {
		LOGGER.debug("IN");
		String path = "";
		try {
			path = Optional.ofNullable(System.getProperty(KNOWAGEWHATIFENGINE_CONTEXT, System.getenv(KNOWAGEWHATIFENGINE_CONTEXT)))
					.orElse(KNOWAGEWHATIFENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagewhatifengine context address", e);
		}
		LOGGER.debug("OUT:" + path);
		return path;
	}

}
