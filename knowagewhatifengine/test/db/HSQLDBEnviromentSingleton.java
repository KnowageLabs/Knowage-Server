/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author ghedin
 *
 */
package db;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;

import org.hsqldb.Server;

public class HSQLDBEnviromentSingleton {

	private static HSQLDBEnviromentSingleton instance = null;
	Server hsqlServer = null;
	private boolean leaveOpen;

	private HSQLDBEnviromentSingleton() {
	}

	public synchronized static HSQLDBEnviromentSingleton getInstance() throws Exception {
		if (instance == null) {
			instance = new HSQLDBEnviromentSingleton();
		}

		return instance;
	}

	public synchronized void startDB() throws ClassNotFoundException, SQLException {

		// 'Server' is a class of HSQLDB representing
		// the database server

		if (hsqlServer == null) {
			hsqlServer = new Server();

			// HSQLDB prints out a lot of informations when
			// starting and closing, which we don't need now.
			// Normally you should point the setLogWriter
			// to some Writer object that could store the logs.
			hsqlServer.setLogWriter(null);
			hsqlServer.setSilent(true);

			// The actual database will be named 'xdb' and its
			// settings and data will be stored in files
			// testdb.properties and testdb.script
			hsqlServer.setDatabaseName(0, "foodmart");

			File userDir = new File("").getAbsoluteFile();

			hsqlServer.setDatabasePath(0, "file:" + userDir.getAbsolutePath() + "\\test\\db\\foodmart");

			// Start the database!
			hsqlServer.start();

			// We have here two 'try' blocks and two 'finally'
			// blocks because we have two things to close
			// after all - HSQLDB server and connection

			// Getting a connection to the newly started database
			Class.forName("org.hsqldb.jdbcDriver");
		}

	}

	public synchronized void closeDB() throws Exception {
		// Closing the server
		clean();
		if (hsqlServer != null && !isLeaveOpen()) {
			hsqlServer.stop();
			hsqlServer = null;
		}

	}

	private void clean() throws Exception {
		File foodmartLog = new File(new File("").getAbsoluteFile(), "\\test\\db\\foodmart.log");

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(foodmartLog);
			fos.write(new byte[0]);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

		foodmartLog.delete();
	}

	public boolean isLeaveOpen() {
		return leaveOpen;
	}

	public void setLeaveOpen(boolean leaveOpen) {
		this.leaveOpen = leaveOpen;
	}

}