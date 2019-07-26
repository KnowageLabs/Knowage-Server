/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

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
package it.eng.spagobi.api.v2.export;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import it.eng.spagobi.commons.bo.UserProfile;

/**
 * Delete old export of an user.
 *
 * @author Marco Libanori
 */
public class ExportDeleteOldJob implements Job {

	/**
	 * Used to made the job a "exclusive".
	 *
	 * Only one instance of this job at time can make operations on filesystem.
	 *
	 * TODO : There's obviously a better solution in Quartz to do this
	 */
	private static final Lock LOCK = new ReentrantLock();

	private static final Logger logger = Logger.getLogger(ExportDeleteOldJob.class);

	public static final String MAP_KEY_RESOURCE_PATH = "resourcePath";

	public static final String MAP_KEY_USER_PROFILE = "userProfile";

	/**
	 * Only last {@value #MAX_DOWNLOADED_FILE_IN_HISTORY} will be stored on filesystem.
	 */
	private static final int MAX_DOWNLOADED_FILE_IN_HISTORY = 10;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		JobDataMap mergedJobDataMap = context.getMergedJobDataMap();

		String resourcePathAsStr = (String) mergedJobDataMap.get(MAP_KEY_RESOURCE_PATH);
		UserProfile userProfile = (UserProfile) mergedJobDataMap.get(MAP_KEY_USER_PROFILE);

		Path perUserExportPath = ExportPathBuilder.getInstance().getPerUserExportResourcePath(resourcePathAsStr, userProfile);

		try {
			logger.debug("Acquiring lock...");
			LOCK.lock();
			logger.debug("Acquired!");

			DirectoryStream<Path> downloadedExportStream = null;
			try {
				downloadedExportStream = Files.newDirectoryStream(perUserExportPath, new DirectoryStream.Filter<Path>() {

					@Override
					public boolean accept(Path entry) throws IOException {
						Path downloadedPlaceholderPath = entry.resolve(ExportPathBuilder.DOWNLOADED_PLACEHOLDER_FILENAME);
						return Files.isDirectory(entry) && Files.isRegularFile(downloadedPlaceholderPath);
					}
				});

				List<Path> downloadedList = new ArrayList<Path>();
				for (Path path : downloadedExportStream) {
					downloadedList.add(path);
				}

				Collections.sort(downloadedList, new Comparator<Path>() {

					@Override
					public int compare(Path o1, Path o2) {
						try {
							FileTime o1LastModifiedTime = Files.getLastModifiedTime(o1);
							FileTime o2LastModifiedTime = Files.getLastModifiedTime(o2);
							return o1LastModifiedTime.compareTo(o2LastModifiedTime);
						} catch (IOException e) {
							String msg = String.format("Error comparing last modified time of %s and %s", o1, o2);
							logger.error(msg, e);
							throw new IllegalStateException(msg, e);
						}
					}
				});

				int size = downloadedList.size();
				if (size > MAX_DOWNLOADED_FILE_IN_HISTORY) {
					List<Path> subList = downloadedList.subList(10, size);

					for (Path path : subList) {
						try {
							FileUtils.deleteDirectory(path.toFile());
						} catch (IOException e) {
							String msg = String.format("Error deleting directory %s", path);
							logger.error(msg, e);
							// Yes, i'm not rethrowing original exception
						}
					}
				}

			} catch (Exception e) {
				String msg = String.format("Error deleting old exported file in directory %s", perUserExportPath);
				logger.error(msg, e);
				throw new JobExecutionException(msg, e);
			} finally {
				if (downloadedExportStream != null) {
					try {
						downloadedExportStream.close();
					} catch (IOException e) {
						// Yes, it's mute!
					}
				}
			}

		} finally {
			logger.debug("Releasing lock...");
			LOCK.unlock();
			logger.debug("Released!");
		}
	}

}
