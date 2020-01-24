package it.eng.spagobi.utilities.cache;

public class RoleEhCache {

	public static CacheInterface getCache() {
		GenericCacheFactory genericCacheFactory = new GenericCacheFactory();
		return genericCacheFactory.getCache("roles");
	}

}
