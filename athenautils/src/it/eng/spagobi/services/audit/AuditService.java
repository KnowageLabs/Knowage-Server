/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.audit;

/**
 * Interface of audit Service
 */
public interface AuditService {
    /**
     * 
     * @param token  String
     * @param user String
     * @param id String
     * @param start String
     * @param end String
     * @param state String
     * @param message String
     * @param errorCode String
     * @return String
     */
    String log(String token,String user,String id,String start,String end,String state,String message,String errorCode);
}
