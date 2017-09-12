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

package it.eng.spagobi.services.dataset.stub;

public interface DataSetService extends java.rmi.Remote {
    public it.eng.spagobi.services.dataset.bo.SpagoBiDataSet saveDataSet(java.lang.String in0, java.lang.String in1, it.eng.spagobi.services.dataset.bo.SpagoBiDataSet in2) throws java.rmi.RemoteException;
    public it.eng.spagobi.services.dataset.bo.SpagoBiDataSet getDataSet(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException;
    public it.eng.spagobi.services.dataset.bo.SpagoBiDataSet[] getAllDataSet(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException;
    public it.eng.spagobi.services.dataset.bo.SpagoBiDataSet getDataSetByLabel(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException;
}
