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

package it.eng.spagobi.tools.dataset.wsconnectors.stub;

public class WSDataSetServiceSoapBindingImpl implements it.eng.spagobi.tools.dataset.wsconnectors.stub.IWsConnector{
    public java.lang.String readDataSet(java.lang.String in0, java.util.Map in1, java.lang.String in2) throws java.rmi.RemoteException {
        if(in2.equalsIgnoreCase("a"))
    	return "<ROWS><ROW name='io' value='30'/></ROWS>";
        if(in2.equalsIgnoreCase("b"))
        	return "<ROWS><ROW name='io' value='80'/></ROWS>";
        return null;
    }

}
	