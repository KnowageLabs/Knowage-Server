/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.alarm.bo;

import java.io.Serializable;

import it.eng.spagobi.kpi.alarm.metadata.SbiAlarm;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarmEvent;

/**
 * @author Enrico Cesaretti
 *         e.cesaretti@xaltia.it
 */
public class AlertSendingItem implements Serializable{

    private SbiAlarm sbiAlarm;
    private SbiAlarmEvent sbiAlarmEvent;
    
    public AlertSendingItem(){}
    
    public AlertSendingItem(SbiAlarm sbiAlarm, SbiAlarmEvent sbiAlarmEvent){
	this.sbiAlarm=sbiAlarm;
	this.sbiAlarmEvent=sbiAlarmEvent;
    }
    
    public SbiAlarm getSbiAlarm() {
        return sbiAlarm;
    }
    public void setSbiAlarm(SbiAlarm sbiAlarm) {
        this.sbiAlarm = sbiAlarm;
    }
   
    public SbiAlarmEvent getSbiAlarmEvent() {
        return sbiAlarmEvent;
    }
    public void setSbiAlarmEvent(SbiAlarmEvent sbiAlarmEvent) {
        this.sbiAlarmEvent = sbiAlarmEvent;
    }
    
    
}
