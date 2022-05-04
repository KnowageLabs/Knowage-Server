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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.news.dao.ISbiNewsDAO;
import it.eng.spagobi.tools.news.metadata.SbiNews;

/**
 * @author Marco Libanori
 */
class NewsFeed {

	private final Set<NewsListener> newsListeners = new HashSet<>();

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	private Set<String> alreadyScheduledTenant = new HashSet<>();

	public NewsFeed() {
		super();
	}

	public void addListener(NewsListener newsListener) {
		synchronized (newsListeners) {
			newsListeners.add(newsListener);
		}
	}

	public void removeListener(NewsListener newsListener) {
		synchronized (newsListeners) {
			newsListeners.remove(newsListener);
		}
	}

	public void refresh(String tenantId) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				Tenant tenant = new Tenant(tenantId);
				TenantManager.setTenant(tenant);

				ISbiNewsDAO sbiNewsDAO = DAOFactory.getSbiNewsDAO();

				List<SbiNews> allNews = sbiNewsDAO.getAllSbiNews().stream()
						.filter(e -> e.isActive())
						.filter(e -> e.getExpirationDate().after(new Date()))
						.collect(Collectors.toList());

				Map<NewsListener, Integer> perListenerTotal = new HashMap<>();
				Map<NewsListener, Integer> perListenerUnread = new HashMap<>();

				synchronized (newsListeners) {
					List<NewsListener> listenersForCurrentTenant = newsListeners.stream().filter(e -> tenantId.equals(e.subscribeForOrganization())).collect(Collectors.toList());

					for (SbiNews sbiNews : allNews) {
						Set<String> roles = sbiNews.getSbiNewsRoles().stream().map(e -> e.getName()).collect(Collectors.toSet());
						Set<String> readByUsers = sbiNews.getRead().stream().map(e -> e.getUser()).collect(Collectors.toSet());

						for (NewsListener newsListener : listenersForCurrentTenant) {
							Set<String> listenerRoles = newsListener.subscribeForRoles();
							String listenerUser = newsListener.subscribeForUser();

							Set<String> altRoles = new HashSet<>(listenerRoles);
							altRoles.retainAll(roles);

							perListenerTotal.computeIfAbsent(newsListener, e -> 0);
							perListenerUnread.computeIfAbsent(newsListener, e -> 0);

							if (!altRoles.isEmpty()) {
								perListenerTotal.computeIfPresent(newsListener, (x, y) -> y+1);

								if (!readByUsers.contains(listenerUser)) {
									perListenerUnread.computeIfPresent(newsListener, (x, y) -> y+1);
								}
							}
						}
					}

					for (NewsListener newsListener : listenersForCurrentTenant) {
						Integer total = perListenerTotal.get(newsListener);
						Integer unread = perListenerUnread.get(newsListener);

						newsListener.listenForNews(total, unread);
					}

				}}
			};

		removeListenerWithClosedSession();

		executor.submit(runnable);

		if (!alreadyScheduledTenant.contains(tenantId)) {
			executor.scheduleAtFixedRate(runnable, 1, 1, TimeUnit.MINUTES);
		}
	}

	private void removeListenerWithClosedSession() {
		synchronized (newsListeners) {
			Iterator<NewsListener> iterator = newsListeners.iterator();
			while (iterator.hasNext()) {
				NewsListener next = iterator.next();
				if (!next.getSession().isOpen()) {
					iterator.remove();
				}
			}
		}
	}
}
