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

	private static final Logger LOGGER = LogManager.getLogger(KnowageSystemConfiguration.class);

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

	private KnowageSystemConfiguration() {
	}

	public static String getKnowageContext() {
		LOGGER.debug("Getting knowage context");
		String path = "";
		try {
			path = getValue(KNOWAGE_CORE_CONTEXT, KNOWAGE_CORE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowage context address", e);
		}
		LOGGER.debug("End getting knowage context: {}", path);
		return path;
	}

	public static String getKnowageAPIContext() {
		LOGGER.debug("Getting knowage-api context");
		String path = "";
		try {
			path = getValue(KNOWAGE_API_CONTEXT, KNOWAGE_API_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowage-api context address", e);
		}
		LOGGER.debug("End getting knowage-api context; {}", path);
		return path;
	}

	public static String getKnowageBirtReportEngineContext() {
		LOGGER.debug("Getting knowagebirtreportengine context");
		String path = "";
		try {
			path = getValue(KNOWAGEBIRTREPORTENGINE_CONTEXT, KNOWAGEBIRTREPORTENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagebirtreportengine context address", e);
		}
		LOGGER.debug("End getting knowagebirtreportengine context: {}", path);
		return path;
	}

	public static String getKnowageCockpitEngineContext() {
		LOGGER.debug("Getting knowagecockpitengine context");
		String path = "";
		try {
			path = getValue(KNOWAGECOCKPITENGINE_CONTEXT, KNOWAGECOCKPITENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagecockpitengine context address", e);
		}
		LOGGER.debug("End getting knowagecockpitengine context: {}", path);
		return path;
	}

	public static String getKnowageCommonjEngineContext() {
		LOGGER.debug("Getting knowagecommonjengine context");
		String path = "";
		try {
			path = getValue(KNOWAGECOMMONJENGINE_CONTEXT, KNOWAGECOMMONJENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagecommonjengine context address", e);
		}
		LOGGER.debug("End getting knowagecommonjengine context: {}", path);
		return path;
	}

	public static String getKnowageDataPreparationContext() {
		LOGGER.debug("Getting knowagedatapreparation context");
		String path = "";
		try {
			path = getValue(KNOWAGEDATAPREPARATION_CONTEXT, KNOWAGEDATAPREPARATION_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowage-data-preparation context address", e);
		}
		LOGGER.debug("End getting knowagedatapreparation context: {}", path);
		return path;
	}

	public static String getKnowageDossierEngineContext() {
		LOGGER.debug("Getting knowagedossierengine context");
		String path = "";
		try {
			path = getValue(KNOWAGEDOSSIERENGINE_CONTEXT, KNOWAGEDOSSIERENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagedossierengine context address", e);
		}
		LOGGER.debug("End getting knowagedossierengine context: {}", path);
		return path;
	}

	public static String getKnowageGeoReportEngineContext() {
		LOGGER.debug("Getting knowagegeoreportengine context");
		String path = "";
		try {
			path = getValue(KNOWAGEGEOREPORTENGINE_CONTEXT, KNOWAGEGEOREPORTENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagegeoreportengine context address", e);
		}
		LOGGER.debug("End getting knowagegeoreportengine context: {}", path);
		return path;
	}

	public static String getKnowageJasperReportEngineContext() {
		LOGGER.debug("Getting knowagejasperreportengine context");
		String path = "";
		try {
			path = getValue(KNOWAGEJASPERREPORTENGINE_CONTEXT, KNOWAGEJASPERREPORTENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagejasperreportengine context address", e);
		}
		LOGGER.debug("End getting knowagejasperreportengine context: {}", path);
		return path;
	}

	public static String getKnowageKpiEngineContext() {
		LOGGER.debug("Getting knowagekpiengine context");
		String path = "";
		try {
			path = getValue(KNOWAGEKPIENGINE_CONTEXT, KNOWAGEKPIENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagekpiengine context address", e);
		}
		LOGGER.debug("End getting knowagekpiengine context: {}", path);
		return path;
	}

	public static String getKnowageMetaContext() {
		LOGGER.debug("Getting knowagemeta context");
		String path = "";
		try {
			path = getValue(KNOWAGEMETA_CONTEXT, KNOWAGEMETA_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagemeta context address", e);
		}
		LOGGER.debug("End getting knowagemeta context: {}", path);
		return path;
	}

	public static String getKnowageQbeEngineContext() {
		LOGGER.debug("Getting knowageqbeengine context");
		String path = "";
		try {
			path = getValue(KNOWAGEQBEENGINE_CONTEXT, KNOWAGEQBEENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowageqbeengine context address", e);
		}
		LOGGER.debug("End getting knowageqbeengine context: {}", path);
		return path;
	}

	public static String getKnowageSvgViewerEngineContext() {
		LOGGER.debug("Getting knowagesvgviewerengine context");
		String path = "";
		try {
			path = getValue(KNOWAGESVGVIEWERENGINE_CONTEXT, KNOWAGESVGVIEWERENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagesvgviewerengine context address", e);
		}
		LOGGER.debug("End getting knowagesvgviewerengine context: {}", path);
		return path;
	}

	public static String getKnowageTalendEngineContext() {
		LOGGER.debug("Getting knowagetalendengine context");
		String path = "";
		try {
			path = getValue(KNOWAGETALENDENGINE_CONTEXT, KNOWAGETALENDENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagetalendengine context address", e);
		}
		LOGGER.debug("End getting knowagetalendengine context: {}", path);
		return path;
	}

	public static String getKnowageVueContext() {
		LOGGER.debug("Getting knowagevue context");
		String path = "";
		try {
			path = getValue(KNOWAGE_VUE_CONTEXT, KNOWAGE_VUE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowage-vue context address", e);
		}
		LOGGER.debug("End getting knowagevue context: {}", path);
		return path;
	}

	public static String getKnowageWhatifEngineContext() {
		LOGGER.debug("Getting knowagewhatifengine context");
		String path = "";
		try {
			path = getValue(KNOWAGEWHATIFENGINE_CONTEXT, KNOWAGEWHATIFENGINE_DEFAULT_CONTEXT);
		} catch (Exception e) {
			LOGGER.error("Error while recovering knowagewhatifengine context address", e);
		}
		LOGGER.debug("End getting knowagewhatifengine context: {}", path);
		return path;
	}

	private static String getValue(String key, String defaultValue) {
		return Optional.ofNullable(System.getProperty(key, System.getenv(key))).orElse(defaultValue);
	}
}
