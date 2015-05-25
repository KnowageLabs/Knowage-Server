/**
 * BehaviouralServiceSoapBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.sdk.behavioural.stub;

import it.eng.spagobi.sdk.behavioural.impl.BehaviouralServiceImpl;

public class BehaviouralServiceSoapBindingImpl implements it.eng.spagobi.sdk.behavioural.stub.BehaviouralService {
	public it.eng.spagobi.sdk.behavioural.bo.SDKAttribute[] getAllAttributes(java.lang.String in0) throws java.rmi.RemoteException,
			it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {

		BehaviouralServiceImpl supplier = new BehaviouralServiceImpl();
		return supplier.getAllAttributes(in0);
	}

}
