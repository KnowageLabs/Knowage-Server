/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.knowage.websocket;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import it.eng.spagobi.api.v2.export.Entry;
import it.eng.spagobi.api.v2.export.ExportPathBuilder;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.resource.export.Utilities;

/**
 * @author Marco Libanori
 *
 */
class AsyncDownloadsFeed {

	private static final Logger LOGGER = Logger.getLogger(AsyncDownloadsFeed.class);

	private final Set<AsyncDownloadsListener> asyncDownloadsListeners = new HashSet<>();

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	private final Set<Path> alreadyWatchedPaths = new HashSet<>();

	private final Map<Path, Set<AsyncDownloadsListener>> path2Listeners = new HashMap<>();

	public AsyncDownloadsFeed() {
		super();
	}

	public void addListener(AsyncDownloadsListener asyncDownloadsListener) {
		synchronized (asyncDownloadsListeners) {
			asyncDownloadsListeners.add(asyncDownloadsListener);

			String tenantId = asyncDownloadsListener.subscribeForOrganization();

			Tenant tenant = new Tenant(tenantId);
			TenantManager.setTenant(tenant);

			UserProfile userProfile = asyncDownloadsListener.getUserProfile();
			String resoursePath = SpagoBIUtilities.getResourcePath();
			Path perUserExportResourcePath = ExportPathBuilder.getInstance().getPerUserExportResourcePath(resoursePath, userProfile);

			path2Listeners.computeIfAbsent(perUserExportResourcePath, p -> new HashSet<>()).add(asyncDownloadsListener);

			if (!alreadyWatchedPaths.contains(perUserExportResourcePath)) {

				Runnable runnable = () -> {
					TenantManager.setTenant(tenant);

					List<Entry> allExportedFiles = Collections.emptyList();
					try {
						allExportedFiles = Utilities.getInstance().getAllExportedFiles(userProfile, true);
					} catch (IOException e) {
						LOGGER.error("Error getting async downloads", e);
					}

					int total = Long.valueOf(allExportedFiles.stream().count()).intValue();
					int alreadyDownloaded = Long.valueOf(allExportedFiles.stream().filter(e -> e.isAlreadyDownloaded()).count()).intValue();

					path2Listeners.getOrDefault(perUserExportResourcePath, Collections.emptySet()).forEach(e -> {
						e.listenForDownload(total, alreadyDownloaded);
					});
				};

				executor.submit(runnable);
				executor.scheduleAtFixedRate(runnable, 1, 1, TimeUnit.MINUTES);
			}

		}
	}

	public void removeListener(AsyncDownloadsListener asyncDownloadsListener) {
		synchronized (asyncDownloadsListeners) {
			asyncDownloadsListeners.remove(asyncDownloadsListener);

			path2Listeners.values().forEach(e -> e.remove(asyncDownloadsListener));
		}
	}

}
