package it.eng.spagobi.commons.initializers.caching;

import it.eng.spago.base.SourceBean;
import it.eng.spago.init.InitializerIFace;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

import org.apache.log4j.Logger;

public class CachingInitializer implements InitializerIFace {

	static private Logger logger = Logger.getLogger(CachingInitializer.class);
	private SourceBean _config;

	@Override
	public SourceBean getConfig() {
		return _config;
	}

	@Override
	public void init(SourceBean config) {
		logger.debug("IN");
		_config = config;

		ICache cache = SpagoBICacheManager.getCache();
		cache.deleteAll();

		IDataSource dataSource = SpagoBICacheConfiguration.getInstance().getCacheDataSource();
		String prefix = SingletonConfig.getInstance().getConfigValue("SPAGOBI.CACHE.NAMEPREFIX");
		if (prefix != null && !prefix.isEmpty() && dataSource != null) {
			PersistedTableManager persistedTableManager = new PersistedTableManager();
			persistedTableManager.dropTablesWithPrefix(dataSource, prefix);
		}
		logger.debug("OUT");

	}

}
