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

package it.eng.spagobi.services.content.stub;

public interface ContentService extends java.rmi.Remote {
    public it.eng.spagobi.services.content.bo.Content readTemplate(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.util.HashMap in3) throws java.rmi.RemoteException;
    public it.eng.spagobi.services.content.bo.Content readTemplateByLabel(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.util.HashMap in3) throws java.rmi.RemoteException;
    public it.eng.spagobi.services.content.bo.Content readSubObjectContent(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.Integer in3) throws java.rmi.RemoteException;
    public it.eng.spagobi.services.content.bo.Content readSubObjectContent(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException;
    public java.lang.String saveSubObject(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3, java.lang.String in4, java.lang.String in5, java.lang.String in6) throws java.rmi.RemoteException;
    public java.lang.String saveObjectTemplate(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3, java.lang.String in4) throws java.rmi.RemoteException;
    public it.eng.spagobi.services.content.bo.Content downloadAll(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3) throws java.rmi.RemoteException;
    public java.lang.String publishTemplate(java.lang.String in0, java.lang.String in1, java.util.HashMap in2) throws java.rmi.RemoteException;
    public java.lang.String mapCatalogue(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3, java.lang.String in4, java.lang.String in5) throws java.rmi.RemoteException;
    public it.eng.spagobi.services.content.bo.Content readMap(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException;
}
