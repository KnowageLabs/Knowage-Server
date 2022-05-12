/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.tools.dataset.resource.export;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spagobi.api.v2.export.Entry;
import it.eng.spagobi.api.v2.export.ExportMetadata;
import it.eng.spagobi.api.v2.export.ExportPathBuilder;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.user.UserProfileManager;

public class Utilities {

	private static final Logger logger = Logger.getLogger(Utilities.class);

	private static final Utilities INSTANCE = new Utilities();

	public static Utilities getInstance() {
		return INSTANCE;
	}

	private Utilities() {
		super();
	}

	public int countAllExportedFiles(boolean showAll) throws IOException {
		return getAllExportedFiles(showAll).size();

	}

	public List<Entry> getAllExportedFiles(UserProfile userProfile, boolean showAll) throws IOException {
		List<Entry> ret = new ArrayList<Entry>();

		String resoursePath = SpagoBIUtilities.getResourcePath();
		java.nio.file.Path perUserExportResourcePath = ExportPathBuilder.getInstance().getPerUserExportResourcePath(resoursePath, userProfile);

		Monitor totalTime = MonitorFactory.start("Knowage.ExportResource.gettingExportedDatasets.user:" + userProfile.getUserId());

		try {

			DirectoryStream<java.nio.file.Path> userJobDirectory = null;

			logger.info("Getting list of exported files for user " + userProfile.getUserId() + "...");

			if (Files.isDirectory(perUserExportResourcePath)) {

				try {
					userJobDirectory = Files.newDirectoryStream(perUserExportResourcePath, new DirectoryStream.Filter<java.nio.file.Path>() {

						@Override
						public boolean accept(java.nio.file.Path entry) throws IOException {
							return Files.isDirectory(entry);
						}
					});

					Iterator<java.nio.file.Path> iterator = userJobDirectory.iterator();

					while (iterator.hasNext()) {
						java.nio.file.Path curr = iterator.next();
						java.nio.file.Path downloadPlaceholderPath = curr.resolve(ExportPathBuilder.DOWNLOADED_PLACEHOLDER_FILENAME);
						java.nio.file.Path metadataPath = curr.resolve(ExportPathBuilder.METADATA_FILENAME);
						java.nio.file.Path dataPath = curr.resolve(ExportPathBuilder.DATA_FILENAME);

						boolean downloadPlaceholderExist = Files.isRegularFile(downloadPlaceholderPath);

						if (!showAll && downloadPlaceholderExist) {
							continue;
						}

						if (!Files.isRegularFile(metadataPath)) {
							continue;
						}

						if (!Files.isRegularFile(dataPath)) {
							continue;
						}

						ExportMetadata metadata = ExportMetadata.readFromJsonFile(metadataPath);

						Entry entry = new Entry(metadata.getDataSetName(), metadata.getStartDate(), metadata.getId().toString(), downloadPlaceholderExist);

						ret.add(entry);
					}
				} finally {
					if (userJobDirectory != null) {
						try {
							userJobDirectory.close();
						} catch (IOException e) {
							// Yes, it's mute!
						}
					}
				}
			}

		} finally {
			totalTime.stop();
		}
		logger.info("Got list of exported files for user " + userProfile.getUserId());
		return ret;
	}

	public List<Entry> getAllExportedFiles(boolean showAll) throws IOException {
		UserProfile profile = UserProfileManager.getProfile();
		return getAllExportedFiles(profile, showAll);
	}
}
