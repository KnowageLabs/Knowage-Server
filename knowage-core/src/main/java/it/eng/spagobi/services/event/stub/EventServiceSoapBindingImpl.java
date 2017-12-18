/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.services.event.stub;

import it.eng.spagobi.services.event.service.EventServiceImpl;

public class EventServiceSoapBindingImpl implements it.eng.spagobi.services.event.stub.EventService{
    public java.lang.String fireEvent(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3, java.lang.String in4, java.lang.String in5) throws java.rmi.RemoteException {
	EventServiceImpl service=new EventServiceImpl();
	return service.fireEvent(in0, in1, in2, in3, in4, in5);
    }

}
