/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.services.scheduler.stub;

import it.eng.spagobi.services.scheduler.service.SchedulerServiceImpl;

public class SchedulerServiceSoapBindingImpl implements it.eng.spagobi.services.scheduler.stub.SchedulerService{
    public java.lang.String getJobList(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException {
            SchedulerServiceImpl service = new SchedulerServiceImpl();
	    String jobListXml = service.getJobList(in0,in1);
	    return jobListXml;
    }

    public java.lang.String getJobSchedulationList(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3) throws java.rmi.RemoteException {
    	SchedulerServiceImpl service = new SchedulerServiceImpl();
	    String schedListXml = service.getJobSchedulationList(in0, in1,in2,in3);
	    return schedListXml;
    }

    public java.lang.String deleteSchedulation(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3) throws java.rmi.RemoteException {
    	SchedulerServiceImpl service = new SchedulerServiceImpl();
	    String res = service.deleteSchedulation(in0,in1,in2,in3);
	    return res;
    }

    public java.lang.String deleteJob(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3) throws java.rmi.RemoteException {
    	SchedulerServiceImpl service = new SchedulerServiceImpl();
	    String res = service.deleteJob(in0,in1,in2,in3);
	    return res;
    }

    public java.lang.String defineJob(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException {
    	SchedulerServiceImpl service = new SchedulerServiceImpl();
	    String res = service.defineJob(in0,in1,in2);
	    return res;
    }

    public java.lang.String getJobDefinition(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3) throws java.rmi.RemoteException {
    	SchedulerServiceImpl service = new SchedulerServiceImpl();
	    String res = service.getJobDefinition(in0,in1,in2,in3);
	    return res;
    }

    public java.lang.String scheduleJob(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException {
    	SchedulerServiceImpl service = new SchedulerServiceImpl();
	    String res = service.scheduleJob(in0,in1,in2);
	    return res;
    }

    public java.lang.String getJobSchedulationDefinition(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3) throws java.rmi.RemoteException {
    	SchedulerServiceImpl service = new SchedulerServiceImpl();
	    String res = service.getJobSchedulationDefinition(in0,in1,in2,in3);
	    return res;
    }

    public java.lang.String existJobDefinition(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3) throws java.rmi.RemoteException {
    	SchedulerServiceImpl service = new SchedulerServiceImpl();
	    String res = service.existJobDefinition(in0,in1,in2,in3);
	    return res;
    }

}
