/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.user;

import it.eng.spagobi.commons.bo.UserProfile;

public class UserProfileManager {

	private static final ThreadLocal<UserProfile> _profile = new ThreadLocal<UserProfile>();

	public static void setProfile(UserProfile profile) {
		_profile.set(profile);
	}

	public static UserProfile getProfile() {
		return _profile.get();
	}

	public static void unset() {
		_profile.remove();
	}
}
