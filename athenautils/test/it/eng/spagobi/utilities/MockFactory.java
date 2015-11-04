package it.eng.spagobi.utilities;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

public class MockFactory implements InitialContextFactory {

	public static Context context;

	@Override
	public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
		return context;
	}

}