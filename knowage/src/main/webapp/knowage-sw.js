/* Knowage, Open Source Business Intelligence suite
Copyright (C) 2019 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>. */

//Current knowage version present as a url parameter
var KNOWAGE_VERSION = new URL(location.href).searchParams.get('version');

/**
 * @desc this listener will be set on the service worker activation and will
 * try to delete old cache elements using the current knowage version as a key.
 * @author Davide Vernassa <davide.vernassa@eng.it>
*/
self.addEventListener('activate', event => {
	 function clearCacheIfDifferent(event) {
		 return caches.keys().then(cacheKeys => {
			 var oldCacheKeys = cacheKeys.filter(key => key.indexOf(KNOWAGE_VERSION) !== 0);
			 var deletePromises = oldCacheKeys.map(oldKey => caches.delete(oldKey));
			 return Promise.all(deletePromises);
		 });
	 }
	 event.waitUntil(
		 clearCacheIfDifferent(event)
		 .then( () => self.clients.claim() )
	 );
});

/**
 * @desc this listener will be thrown when a fetch service will be called.
 * If the current cache version of the css is present that file will be used, otherwise will be stored.
 * Works just with css files.
*/
self.addEventListener('fetch', function(event) {
	if(event.request.headers.get('Accept').indexOf('text/css') !== -1){
		event.respondWith(
			caches.open('knowage-cache-' + KNOWAGE_VERSION).then(function(cache) {
				return cache.match(event.request).then(function (response) {
					return response || fetch(event.request).then(function(response) {
						cache.put(event.request, response.clone());
				        return response;
				    });
				});
			})
		);
	}
});