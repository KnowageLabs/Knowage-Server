/*** Eclipse Class Decompiler plugin, copyright (c) 2012 Chao Chen (cnfree2000@hotmail.com) ***/
package it.eng.spago.base;

import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.tracing.TracerSingleton;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.xerces.parsers.SAXParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class SourceBean extends AbstractXMLObject implements CloneableObject,
		Serializable {
	private static final long serialVersionUID = 1L;
	private String _sourceBeanName;
	private List _attributes;
	private String _characters;
	private boolean _trim;
	private boolean _upperCase;
	private List _namespaceMappings;
	private String _prefix;
	private XMLSerializer _xmlSerializer;

	public SourceBean(String name, boolean trim) throws SourceBeanException {
		this(name);
		this._trim = trim;
	}

	public SourceBean(String name, boolean trim, boolean upperCase)
			throws SourceBeanException {
		this(name, trim);
		this._upperCase = upperCase;
	}

	public SourceBean(String name) throws SourceBeanException {
		this._sourceBeanName = null;

		this._attributes = null;

		this._characters = null;

		this._trim = true;

		this._upperCase = true;

		this._namespaceMappings = null;

		this._prefix = null;

		this._xmlSerializer = null;

		this._sourceBeanName = SourceBeanAttribute.validateKey(name);
		this._attributes = new ArrayList();
		this._characters = null;
		this._namespaceMappings = new ArrayList();
		this._prefix = null;
		this._xmlSerializer = null;
	}

	public SourceBean(SourceBean sourceBean) throws SourceBeanException {
		this._sourceBeanName = null;

		this._attributes = null;

		this._characters = null;

		this._trim = true;

		this._upperCase = true;

		this._namespaceMappings = null;

		this._prefix = null;

		this._xmlSerializer = null;

		if (sourceBean == null)
			throw new SourceBeanException("sourceBean non valido");
		this._sourceBeanName = sourceBean._sourceBeanName;
		this._attributes = new ArrayList(sourceBean._attributes.size());
		for (int i = 0; i < sourceBean._attributes.size(); ++i) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) (SourceBeanAttribute) sourceBean._attributes
					.get(i);

			this._attributes.add(attribute.cloneObject());
		}
		this._characters = sourceBean._characters;
		this._namespaceMappings = ((ArrayList) ((ArrayList) sourceBean
				.getNamespaceMappings()).clone());
		this._prefix = sourceBean._prefix;
		this._xmlSerializer = sourceBean._xmlSerializer;
	}

	public CloneableObject cloneObject() {
		SourceBean clonedObject = null;
		try {
			clonedObject = new SourceBean(this);
		} catch (SourceBeanException ex) {
			TracerSingleton
					.log("Spago",
							4,
							"SourceBean::cloneObject: clonedObject = new SourceBean(this)",
							ex);
		}

		return clonedObject;
	}

	public void clearBean() {
		delContainedAttributes();
		delCharacters();
	}

	public void clearBean(String key) {
		delContainedAttributes(key);
		delCharacters(key);
	}

	public void setBean(SourceBean sourceBean) {
		clearBean();
		if (sourceBean == null)
			return;
		setContainedAttributes(sourceBean.getContainedAttributes());
		setCharacters(sourceBean.getCharacters());
	}

	public void setBean(String key, SourceBean sourceBean)
			throws SourceBeanException {
		Object attribute = getAttribute(key);
		if ((attribute == null) || (!(attribute instanceof SourceBean))) {
			TracerSingleton.log("Spago", 1,
					"SourceBean::setBean: chiave errata");

			throw new SourceBeanException("Chiave errata");
		}
		((SourceBean) attribute).setBean(sourceBean);
	}

	public String getName() {
		return this._sourceBeanName;
	}

	public void setName(String sourceBeanName) {
		this._sourceBeanName = sourceBeanName;
	}

	public Object getAttributeItem(String key) {
		if (key == null) {
			TracerSingleton.log("Spago", 1,
					"SourceBean::getAttributeItem: chiave nulla");

			return null;
		}

		boolean deepSearch = key.indexOf(46) != -1;
		String searchKey = key;
		if (deepSearch) {
			searchKey = key.substring(0, key.indexOf(46));
		}

		List values = new ArrayList();
		for (int i = 0; i < this._attributes.size(); ++i) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) (SourceBeanAttribute) this._attributes
					.get(i);

			if (attribute.getKey().equalsIgnoreCase(searchKey))
				values.add(attribute);
		}
		if (values.size() == 0) {
			return null;
		}
		if (values.size() == 1) {
			if (deepSearch) {
				if (!(((SourceBeanAttribute) (SourceBeanAttribute) values
						.get(0)).getValue() instanceof SourceBean)) {
					TracerSingleton.log("Spago", 1,
							"SourceBean::getAttributeItem: attributo ["
									+ searchKey + "] non è un SourceBean");

					return null;
				}
				String childKey = key.substring(key.indexOf(46) + 1,
						key.length());

				return ((SourceBean) (SourceBean) ((SourceBeanAttribute) (SourceBeanAttribute) values
						.get(0)).getValue()).getAttributeItem(childKey);
			}

			return values.get(0);
		}
		if (deepSearch) {
			TracerSingleton
					.log("Spago",
							1,
							"SourceBean::getAttributeItem: attributo ["
									+ searchKey
									+ "] multivalore, impossibile eseguire una ricerca nidificata");

			return null;
		}
		return values;
	}

	public boolean containsAttribute(String key) {
		Object value = getAttributeItem(key);
		return (value != null);
	}

	public Object getAttribute(String key) {
		Object attributeItem = getAttributeItem(key);
		if (attributeItem == null)
			return null;
		if (attributeItem instanceof SourceBeanAttribute) {
			SourceBeanAttribute sourceBeanAttribute = (SourceBeanAttribute) attributeItem;

			return sourceBeanAttribute.getValue();
		}

		List sourceBeanAttributes = (List) attributeItem;
		List values = new ArrayList(sourceBeanAttributes.size());
		for (int i = 0; i < sourceBeanAttributes.size(); ++i) {
			SourceBeanAttribute sourceBeanAttribute = (SourceBeanAttribute) (SourceBeanAttribute) sourceBeanAttributes
					.get(i);

			values.add(sourceBeanAttribute.getValue());
		}
		return values;
	}

	public List getAttributeAsList(String key) {
		Object value = getAttribute(key);
		if (value == null)
			return new ArrayList(0);
		if (value instanceof List)
			return ((List) value);
		List values = new ArrayList(1);
		values.add(value);
		return values;
	}

	public void setAttribute(String key, Object value)
			throws SourceBeanException {
		if (key == null) {
			TracerSingleton.log("Spago", 1,
					"SourceBean::setAttribute: chiave nulla");

			throw new SourceBeanException("Chiave nulla");
		}

		if (key.indexOf(46) != -1) {
			String searchKey = key.substring(0, key.indexOf(46));
			String childKey = key.substring(key.indexOf(46) + 1, key.length());
			Object attribute = getAttribute(searchKey);
			if ((attribute != null) && (attribute instanceof List)) {
				ArrayList filteredAttribute = new ArrayList();
				for (int i = 0; i < ((ArrayList) attribute).size(); ++i) {
					Object attributeItem = ((ArrayList) attribute).get(i);
					if (attributeItem instanceof SourceBean)
						filteredAttribute.add(attributeItem);
				}
				if (filteredAttribute.size() == 0)
					attribute = null;
				else if (filteredAttribute.size() == 1)
					attribute = filteredAttribute.get(0);
				else
					attribute = filteredAttribute;
			}
			if ((attribute != null) && (!(attribute instanceof List))
					&& (!(attribute instanceof SourceBean))) {
				attribute = null;
			}
			if (attribute == null) {
				SourceBean sourceBean = new SourceBean(searchKey);
				sourceBean.setAttribute(childKey, value);
				setAttribute(sourceBean);
				return;
			}
			if (attribute instanceof SourceBean) {
				SourceBean sourceBean = (SourceBean) attribute;
				sourceBean.setAttribute(childKey, value);
				return;
			}
			TracerSingleton
					.log("Spago",
							1,
							"SourceBean::setAttribute: attributo ["
									+ searchKey
									+ "] multivalore, impossibile eseguire una ricerca nidificata");

			throw new SourceBeanException(
					"Attributo ["
							+ searchKey
							+ "] multivalore, impossibile eseguire una ricerca nidificata");
		}

		if (value instanceof SourceBean) {
			SourceBean sourceBean = new SourceBean(key);
			sourceBean.setAttribute((SourceBean) value);
			setAttribute(sourceBean);
			return;
		}
		this._attributes.add(new SourceBeanAttribute(key, value));
	}

	public void setAttribute(SourceBean value) throws SourceBeanException {
		SourceBeanAttribute.validateValue(value);
		this._attributes.add(new SourceBeanAttribute(value.getName(), value));
	}

	public void setAttribute(SourceBeanAttribute attribute)
			throws SourceBeanException {
		this._attributes.add(attribute);
	}

	public void updAttribute(String key, Object value)
			throws SourceBeanException {
		if (key == null) {
			TracerSingleton.log("Spago", 1,
					"SourceBean::updAttribute: chiave nulla");

			throw new SourceBeanException("Chiave nulla");
		}

		if (key.indexOf(46) != -1) {
			String searchKey = key.substring(0, key.indexOf(46));
			String childKey = key.substring(key.indexOf(46) + 1, key.length());
			Object attribute = getAttribute(searchKey);
			if (attribute == null) {
				SourceBean sourceBean = new SourceBean(searchKey);
				sourceBean.updAttribute(childKey, value);
				setAttribute(sourceBean);
				return;
			}
			if (attribute instanceof SourceBean) {
				SourceBean sourceBean = (SourceBean) attribute;
				sourceBean.updAttribute(childKey, value);
				return;
			}
			if (attribute instanceof List) {
				TracerSingleton
						.log("Spago",
								1,
								"SourceBean::updAttribute: attributo ["
										+ searchKey
										+ "] multivalore, impossibile eseguire una ricerca nidificata");

				throw new SourceBeanException(
						"Attributo ["
								+ searchKey
								+ "] multivalore, impossibile eseguire una ricerca nidificata");
			}

			TracerSingleton.log("Spago", 1,
					"SourceBean::updAttribute: attributo [" + searchKey
							+ "] non è un SourceBean");

			throw new SourceBeanException("Attributo [" + searchKey
					+ "] non è un SourceBean");
		}

		Object attributeItem = getAttributeItem(key);
		if (attributeItem == null) {
			setAttribute(key, value);
			return;
		}
		if (attributeItem instanceof List) {
			TracerSingleton
					.log("Spago",
							1,
							"SourceBean::updAttribute: attributo ["
									+ key
									+ "] multivalore, impossibile eseguire aggiornamento");

			throw new SourceBeanException("Attributo [" + key
					+ "] multivalore, impossibile eseguire aggiornamento");
		}

		SourceBeanAttribute sourceBeanAttribute = (SourceBeanAttribute) attributeItem;

		if (value instanceof SourceBean) {
			SourceBean sourceBean = new SourceBean(key);
			sourceBean.setAttribute((SourceBean) value);
			sourceBeanAttribute.setValue(sourceBean);
			return;
		}
		sourceBeanAttribute.setValue(value);
	}

	public void updAttribute(SourceBean value) throws SourceBeanException {
		Object attributeItem = getAttributeItem(value.getName());
		if (attributeItem == null) {
			setAttribute(value);
			return;
		}
		if (attributeItem instanceof List) {
			TracerSingleton
					.log("Spago",
							1,
							"SourceBean::updAttribute: attributo ["
									+ value.getName()
									+ "] multivalore, impossibile eseguire aggiornamento");

			throw new SourceBeanException("Attributo [" + value.getName()
					+ "] multivalore, impossibile eseguire aggiornamento");
		}

		SourceBeanAttribute sourceBeanAttribute = (SourceBeanAttribute) attributeItem;

		sourceBeanAttribute.setValue(value);
	}

	public void delAttribute(String key) throws SourceBeanException {
		if (key == null) {
			TracerSingleton.log("Spago", 1,
					"SourceBean::delAttribute: chiave nulla");

			throw new SourceBeanException("Chiave nulla");
		}

		if (key.indexOf(46) != -1) {
			String searchKey = key.substring(0, key.indexOf(46));
			ArrayList values = new ArrayList();
			for (int i = 0; i < this._attributes.size(); ++i) {
				SourceBeanAttribute attribute = (SourceBeanAttribute) (SourceBeanAttribute) this._attributes
						.get(i);

				if (attribute.getKey().equalsIgnoreCase(searchKey))
					values.add(attribute.getValue());
			}
			if (values.size() == 0) {
				return;
			}
			if (values.size() == 1) {
				if (!(values.get(0) instanceof SourceBean)) {
					TracerSingleton.log("Spago", 1,
							"SourceBean::delAttribute: attributo [" + searchKey
									+ "] non è un SourceBean");

					throw new SourceBeanException("Attributo [" + searchKey
							+ "] non è un SourceBean");
				}

				((SourceBean) (SourceBean) values.get(0)).delAttribute(key
						.substring(key.indexOf(46) + 1, key.length()));

				return;
			}
			TracerSingleton
					.log("Spago",
							1,
							"SourceBean::delAttribute: attributo ["
									+ searchKey
									+ "] multivalore, impossibile eseguire una ricerca nidificata");

			throw new SourceBeanException(
					"Attributo ["
							+ searchKey
							+ "] multivalore, impossibile eseguire una ricerca nidificata");
		}

		for (Iterator it = this._attributes.iterator(); it.hasNext();) {
			SourceBeanAttribute sba = (SourceBeanAttribute) it.next();
			if (sba.getKey().equalsIgnoreCase(key))
				it.remove();
		}
	}

	public String getCharacters() {
		return this._characters;
	}

	public String getCharacters(String key) {
		Object attribute = getAttribute(key);
		if ((attribute == null) || (!(attribute instanceof SourceBean)))
			return null;
		return ((SourceBean) attribute).getCharacters();
	}

	public void setCharacters(String characters) {
		setCharacters(characters, this._trim);
	}

	public void setCharacters(String characters, boolean trim) {
		delCharacters();
		if ((characters == null) || (characters.trim().length() == 0))
			return;
		this._characters = ((trim) ? characters.trim() : characters);
	}

	public void setCharacters(String key, String characters)
			throws SourceBeanException {
		Object attribute = getAttribute(key);
		if ((attribute == null) || (!(attribute instanceof SourceBean))) {
			TracerSingleton.log("Spago", 1,
					"SourceBean::setCharacters: chiave errata");

			throw new SourceBeanException("Chiave errata");
		}
		((SourceBean) attribute).setCharacters(characters);
	}

	public void delCharacters() {
		this._characters = null;
	}

	public void delCharacters(String key) {
		Object attribute = getAttribute(key);
		if ((attribute == null) || (!(attribute instanceof SourceBean)))
			return;
		((SourceBean) attribute).delCharacters();
	}

	public List getFilteredSourceBeanAttributeAsList(String key,
			String paramName, String paramValue) {
		List sourceBeanAttributeValues = new ArrayList();
		if (key == null) {
			TracerSingleton
					.log("Spago", 1,
							"SourceBean::getFilteredSourceBeanAttributeAsList: chiave nulla");

			return sourceBeanAttributeValues;
		}
		if (paramName == null) {
			TracerSingleton
					.log("Spago", 1,
							"SourceBean::getFilteredSourceBeanAttributeAsList: nome parametro nullo");

			return sourceBeanAttributeValues;
		}

		Object attributeValue = getAttribute(key);
		if (attributeValue == null)
			return sourceBeanAttributeValues;
		List attributeValues = null;
		if (attributeValue instanceof List) {
			attributeValues = (List) attributeValue;
		} else {
			attributeValues = new ArrayList(1);
			attributeValues.add(attributeValue);
		}
		for (int i = 0; i < attributeValues.size(); ++i) {
			attributeValue = attributeValues.get(i);
			if (!(attributeValue instanceof SourceBean))
				continue;
			Object localParamValue = ((SourceBean) attributeValue)
					.getAttribute(paramName);

			if ((localParamValue == null) && (paramValue != null))
				continue;
			if ((localParamValue != null) && (paramValue == null)) {
				continue;
			}
			if ((((localParamValue != null) || (paramValue != null)))
					&& (((!(localParamValue instanceof String)) || (!(((String) localParamValue)
							.equalsIgnoreCase(paramValue)))))) {
				continue;
			}
			sourceBeanAttributeValues.add(attributeValue);
		}
		return sourceBeanAttributeValues;
	}

	public Object getFilteredSourceBeanAttribute(String key, String paramName,
			String paramValue) {
		List sourceBeanAttributeValues = getFilteredSourceBeanAttributeAsList(
				key, paramName, paramValue);

		if (sourceBeanAttributeValues.size() == 0)
			return null;
		if (sourceBeanAttributeValues.size() == 1)
			return sourceBeanAttributeValues.get(0);
		return sourceBeanAttributeValues;
	}

	public List getContainedAttributes() {
		return ((List) ((ArrayList) this._attributes).clone());
	}

	public List getContainedAttributes(String key) {
		Object attribute = getAttribute(key);
		if ((attribute == null) || (!(attribute instanceof SourceBean)))
			return new ArrayList(0);
		return ((SourceBean) attribute).getContainedAttributes();
	}

	public void setContainedAttributes(List attributes) {
		delContainedAttributes();
		this._attributes = new ArrayList(attributes);
	}

	public void setContainedAttributes(String key, List attributes)
			throws SourceBeanException {
		Object attribute = getAttribute(key);
		if ((attribute == null) || (!(attribute instanceof SourceBean))) {
			TracerSingleton.log("Spago", 1,
					"SourceBean::setContainedAttributes: chiave errata");

			throw new SourceBeanException("Chiave errata");
		}
		((SourceBean) attribute).setContainedAttributes(attributes);
	}

	public void updContainedAttributes(List attributes)
			throws SourceBeanException {
		if (attributes == null)
			return;
		for (int i = 0; i < attributes.size(); ++i) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) attributes
					.get(i);

			String attributeKey = attribute.getKey();
			Object attributeValue = attribute.getValue();
			if (attributeValue instanceof SourceBean)
				updAttribute((SourceBean) attributeValue);
			else
				updAttribute(attributeKey, attributeValue);
		}
	}

	public void updContainedAttributes(String key, ArrayList attributes)
			throws SourceBeanException {
		Object attribute = getAttribute(key);
		if ((attribute == null) || (!(attribute instanceof SourceBean))) {
			TracerSingleton.log("Spago", 1,
					"SourceBean::updContainedAttributes: chiave errata");

			throw new SourceBeanException("Chiave errata");
		}
		((SourceBean) attribute).updContainedAttributes(attributes);
	}

	public void delContainedAttributes() {
		this._attributes.clear();
	}

	public void delContainedAttributes(String key) {
		Object attribute = getAttribute(key);
		if ((attribute == null) || (!(attribute instanceof SourceBean)))
			return;
		((SourceBean) attribute).delContainedAttributes();
	}

	public List getContainedSourceBeanAttributes() {
		List attributes = new ArrayList();
		for (int i = 0; i < this._attributes.size(); ++i) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) (SourceBeanAttribute) this._attributes
					.get(i);

			if (attribute.getValue() instanceof SourceBean)
				attributes.add(attribute);
		}
		return attributes;
	}

	public List getContainedSourceBeanAttributes(String key) {
		Object attribute = getAttribute(key);
		if ((attribute == null) || (!(attribute instanceof SourceBean)))
			return new ArrayList(0);
		return ((SourceBean) attribute).getContainedSourceBeanAttributes();
	}

	private List getContainedXMLObjectAttributes() {
		List attributes = new ArrayList();
		for (int i = 0; i < this._attributes.size(); ++i) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) (SourceBeanAttribute) this._attributes
					.get(i);

			if (attribute.getValue() instanceof XMLObject)
				attributes.add(attribute);
		}
		return attributes;
	}

	private List getContainedNotXMLObjectAttributes() {
		List attributes = new ArrayList();
		for (int i = 0; i < this._attributes.size(); ++i) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) (SourceBeanAttribute) this._attributes
					.get(i);

			if (!(attribute.getValue() instanceof XMLObject))
				attributes.add(attribute);
		}
		return attributes;
	}

	public Vector getFullKeyPaths(String key) {
		Vector fullKeyPaths = new Vector();
		getFullKeyPaths(key, "", fullKeyPaths);
		return fullKeyPaths;
	}

	private void getFullKeyPaths(String key, String path, Vector fullKeyPaths) {
		for (int i = 0; i < this._attributes.size(); ++i) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) (SourceBeanAttribute) this._attributes
					.get(i);

			if (attribute.getKey().equalsIgnoreCase(key)) {
				fullKeyPaths.addElement(path + "." + key);
				break;
			}
		}
		for (int i = 0; i < this._attributes.size(); ++i) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) (SourceBeanAttribute) this._attributes
					.get(i);

			if (attribute.getValue() instanceof SourceBean) {
				SourceBean sourceBean = (SourceBean) (SourceBean) attribute
						.getValue();
				String newPath = null;
				if (path == null)
					newPath = sourceBean.getName();
				else
					newPath = path + "." + sourceBean.getName();
				sourceBean.getFullKeyPaths(key, newPath, fullKeyPaths);
			}
		}
	}

	public static SourceBean fromXMLStream(InputSource stream,
			boolean trimCharacters, boolean upperCase)
			throws SourceBeanException {
		if (stream == null) {
			TracerSingleton.log("Spago", 1,
					"SourceBean::fromXMLStream: stream nullo");

			return null;
		}
		SourceBean sourceBean = null;
		XMLReader parser = new SAXParser();
		try {
			SourceBeanContentHandler contentHandler = new SourceBeanContentHandler(
					trimCharacters, upperCase);

			parser.setContentHandler(contentHandler);
			parser.parse(stream);
			sourceBean = contentHandler.getSourceBean();
		} catch (Exception ex) {
			TracerSingleton
					.log("Spago",
							4,
							"SourceBean::fromXMLString: impossibile elaborare lo stream XML",
							ex);

			throw new SourceBeanException("Impossibile elaborare lo stream XML");
		}
		return sourceBean;
	}

	public static SourceBean fromXMLStream(InputSource stream,
			boolean trimCharacters) throws SourceBeanException {
		return fromXMLStream(stream, trimCharacters, true);
	}

	public static SourceBean fromXMLStream(InputSource stream)
			throws SourceBeanException {
		return fromXMLStream(stream, true, true);
	}

	public static SourceBean fromXMLString(String xmlSourceBean,
			boolean trimCharacters, boolean upperCase)
			throws SourceBeanException {
		if (xmlSourceBean == null) {
			TracerSingleton.log("Spago", 4,
					"SourceBean::fromXMLString: xmlSourceBean non valido");

			throw new SourceBeanException("xmlSourceBean non valido");
		}
		xmlSourceBean = xmlSourceBean.trim();
		String uri = null;
		try {
			uri = (String) ConfigSingleton.getInstance().getAttribute(
					"COMMON.FILE_URI_PREFIX");
			
		} catch (Exception e) {
			TracerSingleton.log("Spago", 4,
					"SourceBean::fromXMLString: impossible to read from ConfigSingleton the COMMON.FILE_URI_PREFIX",e);
		}

		if (uri == null)
			uri = "";
		if (!(xmlSourceBean.startsWith("<"))) {
			xmlSourceBean = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<XMLSOURCEBEAN>\n"
					+ xmlSourceBean + "\n</XMLSOURCEBEAN>";

			SourceBean xmlRequestBean = fromXMLStream(new InputSource(
					new StringReader(xmlSourceBean)));

			xmlSourceBean = xmlRequestBean.getCharacters();
		}
		if (!(xmlSourceBean.startsWith("<?xml"))) {
			xmlSourceBean = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
					+ xmlSourceBean;
		}

		return fromXMLStream(new InputSource(new StringReader(xmlSourceBean)),
				trimCharacters, upperCase);
	}

	public static SourceBean fromXMLString(String xmlSourceBean,
			boolean trimCharacters) throws SourceBeanException {
		return fromXMLString(xmlSourceBean, trimCharacters, true);
	}

	public static SourceBean fromXMLString(String xmlSourceBean)
			throws SourceBeanException {
		return fromXMLString(xmlSourceBean, true, true);
	}

	public static SourceBean fromXMLFile(String xmlSourceBean,
			boolean trimCharacters, boolean upperCase)
			throws SourceBeanException {
		if (xmlSourceBean == null) {
			TracerSingleton.log("Spago", 4,
					"SourceBean::fromXMLFile: xmlSourceBean non valido");

			throw new SourceBeanException("XMLSourceBean non valido");
		}
		String rootPath = ConfigSingleton.getRootPath();
		if (rootPath == null) {
			TracerSingleton.log("Spago", 4,
					"SourceBean::fromXMLFile: root path non valido");

			throw new SourceBeanException("Root path non valido");
		}
		xmlSourceBean = rootPath + xmlSourceBean;
		SourceBean sourceBean = null;
		try {
			InputSource stream = new InputSource(new FileReader(xmlSourceBean));
			sourceBean = fromXMLStream(stream, trimCharacters, upperCase);
		} catch (FileNotFoundException ex) {
			TracerSingleton.log("Spago", 4,
					"SourceBean::fromXMLFile: file non trovato");

			throw new SourceBeanException("File non trovato");
		}
		return sourceBean;
	}

	public static SourceBean fromXMLFile(String xmlSourceBean,
			boolean trimCharacters) throws SourceBeanException {
		return fromXMLFile(xmlSourceBean, trimCharacters, true);
	}

	public static SourceBean fromXMLFile(String xmlSourceBean)
			throws SourceBeanException {
		return fromXMLFile(xmlSourceBean, true, true);
	}

	public Element toElement(Document document, XMLSerializer serializer) {
		if (this._upperCase)
			this._sourceBeanName = this._sourceBeanName.toUpperCase();
		Element element = document.createElement(this._sourceBeanName);

		for (Iterator it = this._namespaceMappings.iterator(); it.hasNext();) {
			NamespaceMapping mapping = (NamespaceMapping) it.next();
			String namespaceName = ":" + mapping.getPrefix();
			element.setAttribute("xmlns" + namespaceName, mapping.getUri());
		}

		notXMLObjectAttributesToElement(document, element, serializer);

		if (this._characters != null) {
			element.appendChild(document.createTextNode(this._characters));
		}

		XMLObjectAttributesToElement(document, element);

		return element;
	}

	public Element toElement(Document document) {
		return toElement(document, this._xmlSerializer);
	}

	private void notXMLObjectAttributesToElement(Document document,
			Element element, XMLSerializer serializer) {
		List notXMLObjectAttributes = getContainedNotXMLObjectAttributes();
		if (serializer == null) {
			serializer = new StringXMLSerializer();
		}
		for (int i = 0; i < notXMLObjectAttributes.size(); ++i) {
			SourceBeanAttribute sourceBeanAttribute = (SourceBeanAttribute) (SourceBeanAttribute) notXMLObjectAttributes
					.get(i);
			try {
				serializer.serialize(document, element, (sourceBeanAttribute
						.getQName() != null) ? sourceBeanAttribute.getQName()
						: sourceBeanAttribute.getKey(), sourceBeanAttribute
						.getValue());
			} catch (Exception ex) {
				element.setAttribute(sourceBeanAttribute.getKey(),
						"NOT_AVAILABLE");
				TracerSingleton
						.log("Spago",
								4,
								"SourceBean::notXMLObjectAttributesToElement: error during XML transformation",
								ex);
			}
		}
	}

	private void XMLObjectAttributesToElement(Document document, Element element) {
		List xmlObjectAttributes = getContainedXMLObjectAttributes();
		for (int i = 0; i < xmlObjectAttributes.size(); ++i) {
			SourceBeanAttribute sourceBeanAttribute = (SourceBeanAttribute) (SourceBeanAttribute) xmlObjectAttributes
					.get(i);

			XMLObject xmlObject = (XMLObject) (XMLObject) sourceBeanAttribute
					.getValue();

			if (xmlObject instanceof SourceBean) {
				element.appendChild(xmlObject.toElement(document));
			} else {
				String key = (this._upperCase) ? sourceBeanAttribute.getKey()
						.toUpperCase() : sourceBeanAttribute.getKey();
				Element keyElement = document.createElement(key);
				keyElement.appendChild(xmlObject.toElement(document));
				element.appendChild(keyElement);
			}
		}
	}

	public String toString() {
		return toXML(false);
	}

	public boolean getTrim() {
		return this._trim;
	}

	public void setTrim(boolean trim) {
		this._trim = trim;
	}

	public List getNamespaceMappings() {
		return this._namespaceMappings;
	}

	public void setNamespaceMappings(List mappings) {
		this._namespaceMappings = mappings;
	}

	public String getPrefix() {
		return this._prefix;
	}

	public void setPrefix(String prefix) {
		this._prefix = prefix;
	}

	public XMLSerializer getXMLSerializer() {
		return this._xmlSerializer;
	}

	public void setXMLSerializer(XMLSerializer serializer) {
		this._xmlSerializer = serializer;
	}
}