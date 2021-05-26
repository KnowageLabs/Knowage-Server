package it.eng.spagobi.utilities;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ServiceConverter implements Converter {

	@Override
	public boolean canConvert(Class type) {
		return Service.class == type;
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object unmarshal(HierarchicalStreamReader hsr, UnmarshallingContext uc) {
		Service av = new Service();
		av.baseurl = hsr.getAttribute("baseurl");
		av.relativepath = hsr.getAttribute("relativepath");
		if (av.relativepath == null)
			av.relativepath = "";
		return av;
	}

}
