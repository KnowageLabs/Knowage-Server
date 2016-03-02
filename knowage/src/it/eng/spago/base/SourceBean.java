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
package it.eng.spago.base;

import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.tracing.TracerSingleton;
import it.eng.spago.util.XMLUtil;

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

/**
 * DATE            CONTRIBUTOR/DEVELOPER    NOTE
 * 25-11-2004		  Butano           Eliminati i synchronized
 *                                          Sostituito il Vector con l'arraylist
 *
 **/
/**
 * La classe <code>SourceBean</code> implementa un contenitore di oggetti. Ogni oggetto memorizzato nel contenitore &egrave; associato ad una chiave che ne
 * consente il recupero. Pi&ugrave; oggetti possono essere memorizzati con la stessa chiave. Al contenitore &egrave; associato un nome.
 * <p>
 * Il contenitore &egrave; in grado di ritornare una sua rappresentazione XML e di costruirsi a partire da uno stream XML (File, String, ...).
 * <p>
 * Un esempio d'uso &egrave; il seguente:
 * <p>
 * <blockquote>
 *
 * <pre>
 * SourceBean inner = new SourceBean(&quot;inner&quot;);
 * inner.setAttribute(&quot;param1&quot;, 1);
 * inner.setAttribute(&quot;param2&quot;, &quot;value2&quot;);
 * SourceBean outer = new SourceBean(&quot;outer&quot;);
 * outer.setAttribute(&quot;param3&quot;, &quot;value3&quot;);
 * outer.setAttribute(inner);
 * </pre>
 *
 * </blockquote>
 * <p>
 * e la relativa rappresentazione XML &egrave;:
 * <p>
 * <blockquote>
 *
 * <pre>
 * &lt;OUTER param3="value3"&gt;
 *     &lt;INNER param1="1" param2="value2"&gt;
 *     &lt;/INNER&gt;
 * &lt;/OUTER&gt;
 * </pre>
 *
 * </blockquote>
 * <p>
 * Per recuperare il/i valori di un attributo il servizio da invocare &egrave; del tipo: <blockquote>
 *
 * <pre>
 * String value2 = (String) outer.getAttribute(&quot;inner.param2&quot;);
 * </pre>
 *
 * </blockquote>
 * <p>
 * La chiave di un attributo pu&ograve; sempre essere espressa con una dot-notation: <blockquote>
 *
 * <pre>
 * String key = &quot;key1.key2.key3&quot;;
 * </pre>
 *
 * </blockquote>
 *
 * @version 1.0, 06/03/2002
 * @author Luigi Bellio
 * @see SourceBeanAttribute
 */
public class SourceBean extends AbstractXMLObject implements CloneableObject, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Nome del <code>SourceBean</code>.
	 */
	private String _sourceBeanName = null;

	/**
	 * Vettore di oggetti di tipo <code>SourceBeanAttribute</code>.
	 */
	private List _attributes = null;

	/**
	 * Testo contenuto nel <code>SourceBean</code>.
	 */
	private String _characters = null;

	// Flag that specify if the text sections have to be trimmed before storing in the SourceBean.
	// The default is to trim
	private boolean _trim = true;

	// Flag that specify if apply to elements the function toUpperCase().
	// The default is to apply
	private boolean _upperCase = true;

	// XML namespace mappings.
	private List _namespaceMappings = null;

	// Owner namespace, if any.
	private String _prefix = null;

	// XML serializer, if any. If not provided use standard String serializer
	private XMLSerializer _xmlSerializer = null;

	/**
	 * Costruisce un <code>SourceBean</code> vuoto con nome <em>name</em>.
	 * <p>
	 *
	 * @param name
	 *            nome del <code>SourceBean</code>
	 * @param trim
	 *            Indica se le sezioni di testo devono essere trimmate
	 * @exception SourceBeanException
	 *                viene lanciata se il parametro di input <em>name</em> non &egrave; una nome valido
	 * @see SourceBean#SourceBean(SourceBean)
	 */
	public SourceBean(String name, boolean trim) throws SourceBeanException {
		this(name);
		_trim = trim;
	} // public SourceBean(String name, boolean trim) throws SourceBeanException

	public SourceBean(String name, boolean trim, boolean upperCase) throws SourceBeanException {
		this(name, trim);
		_upperCase = upperCase;
	} // public SourceBean(String name, boolean trim) throws SourceBeanException

	/**
	 * Costruisce un <code>SourceBean</code> vuoto con nome <em>name</em>.
	 * <p>
	 *
	 * @param name
	 *            nome del <code>SourceBean</code>
	 * @exception SourceBeanException
	 *                viene lanciata se il parametro di input <em>name</em> non &egrave; una nome valido
	 * @see SourceBean#SourceBean(SourceBean)
	 */
	public SourceBean(String name) throws SourceBeanException {
		// TracerSingleton.log(Constants.NOME_MODULO,
		// TracerSingleton.DEBUG, "SourceBean::SourceBean: name [" + name + "]");
		_sourceBeanName = SourceBeanAttribute.validateKey(name);
		_attributes = new ArrayList();
		_characters = null;
		_namespaceMappings = new ArrayList();
		_prefix = null;
		_xmlSerializer = null;
	} // public SourceBean(String name) throws SourceBeanException

	/**
	 * Costruisce un <code>SourceBean</code> copia di <em>sourceBean</em>.
	 * <p>
	 *
	 * @param sourceBean
	 *            <code>SourceBean</code> da copiare
	 * @exception SourceBeanException
	 *                viene lanciata se il parametro di input <em>sourceBean</em> &egrave; nullo
	 * @see SourceBean#SourceBean(String)
	 */
	public SourceBean(SourceBean sourceBean) throws SourceBeanException {
		// TracerSingleton.log(Constants.NOME_MODULO,
		// TracerSingleton.DEBUG, "SourceBean::SourceBean: invocato");
		if (sourceBean == null)
			throw new SourceBeanException("sourceBean non valido");
		_sourceBeanName = sourceBean._sourceBeanName;
		_attributes = new ArrayList(sourceBean._attributes.size());
		for (int i = 0; i < sourceBean._attributes.size(); i++) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) (sourceBean._attributes.get(i));
			_attributes.add(attribute.cloneObject());
		} // for (int i = 0; i < sourceBean._attributes.size(); i++)
		_characters = sourceBean._characters;
		_namespaceMappings = (ArrayList) ((ArrayList) sourceBean.getNamespaceMappings()).clone();
		_prefix = sourceBean._prefix;
		_xmlSerializer = sourceBean._xmlSerializer;
		;
	} // public SourceBean(SourceBean sourceBean) throws SourceBeanException

	/**
	 * Ritorna un <code>CloneableObject</code> copia <em>non profonda</em> dell'oggetto stesso.
	 * <p>
	 *
	 * @return una copia <em>non profonda</em> del <code>SourceBean</code> stesso
	 */
	@Override
	public CloneableObject cloneObject() {
		SourceBean clonedObject = null;
		try {
			clonedObject = new SourceBean(this);
		} // try
		catch (SourceBeanException ex) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "SourceBean::cloneObject: clonedObject = new SourceBean(this)", ex);
		} // catch (SourceBeanException ex) try
		return clonedObject;
	} // public CloneableObject cloneObject()

	/**
	 * Elimina tutto il conenuto del <code>SourceBean</code>.
	 * <p>
	 *
	 * @see SourceBean#clearBean(String)
	 */
	public void clearBean() {
		delContainedAttributes();
		delCharacters();
	} // public void clearBean()

	/**
	 * Elimina tutto il contenuto del <code>SourceBean</code> corrispondente all'attributo di chiave <em>key</em>.
	 * <p>
	 *
	 * @param key
	 *            chiave dell'attributo in dot-notation
	 * @see SourceBean#clearBean()
	 */
	public void clearBean(String key) {
		delContainedAttributes(key);
		delCharacters(key);
	} // public void clearBean(String key)

	/**
	 * Copia tutto il contenuto del parametro <code>SourceBean</code> nel proprio stato.
	 * <p>
	 *
	 * @param sourceBean
	 *            <code>SourceBean</code> di riferimento.
	 */
	public void setBean(SourceBean sourceBean) {
		clearBean();
		if (sourceBean == null)
			return;
		setContainedAttributes(sourceBean.getContainedAttributes());
		setCharacters(sourceBean.getCharacters());
	} // public void setBean(SourceBean sourceBean)

	/**
	 * Copia il contenuto del parametro <code>SourceBean</code> nel proprio stato a partire dall'attributo con chiave <em>key</em>.
	 * <p>
	 *
	 * @param key
	 *            <code>String</code> key che identifica un elemento del sourceBean.
	 * @param sourceBean
	 *            <code>SourceBean</code> di riferimento.
	 * @exception SourceBeanException
	 *                viene lanciata se la chiave non fa riferimento a nessun elemento.
	 */
	public void setBean(String key, SourceBean sourceBean) throws SourceBeanException {
		Object attribute = getAttribute(key);
		if ((attribute == null) || (!(attribute instanceof SourceBean))) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::setBean: chiave errata");
			throw new SourceBeanException("Chiave errata");
		} // if ((attribute == null) || (!(attribute instanceof SourceBean)))
		((SourceBean) attribute).setBean(sourceBean);
	} // public void setBean(String key, SourceBean sourceBean) throws SourceBeanException

	/**
	 * Ritorna il nome del <code>SourceBean</code>.
	 * <p>
	 *
	 * @return il nome del <code>SourceBean</code>
	 * @see SourceBean#SourceBean(String)
	 */
	public String getName() {
		return _sourceBeanName;
	} // public String getName()

	/**
	 * Imposta il nome del <code>SourceBean</code>.
	 */
	public void setName(String sourceBeanName) {
		_sourceBeanName = sourceBeanName;
	} // public void setName(String sourceBeanName)

	/**
	 * Ritorna tutti gli oggetti di tipo <code>SourceBeanAttribute</code> il cui campo chiave vale <em>key</em>.
	 * <p>
	 *
	 * @param key
	 *            chiave dell'attributo in dot-notation
	 * @return <ul>
	 *         <li> <em>null</em> se l'attributo non esiste <li> il <code>SourceBeanAttribute</code> corrispondente alla chiave se l'attributo &egrave;
	 *         single-value <li> <em>List</em> arraylist di <code>SourceBeanAttribute</code> corrispondenti alla chiave se l'attributo &egrave; multi-value
	 *         </ul>
	 * @see SourceBean#getAttribute(String)
	 */
	public Object getAttributeItem(String key) {
		if (key == null) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::getAttributeItem: chiave nulla");
			return null;
		} // if (key == null)
			// TracerSingleton.log(Constants.NOME_MODULO,
			// TracerSingleton.DEBUG, "SourceBean::getAttributeItem: key [" + key + "]");
		boolean deepSearch = (key.indexOf('.') != -1);
		String searchKey = key;
		if (deepSearch)
			searchKey = key.substring(0, key.indexOf('.'));
		// TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG,
		// "SourceBean::getAttributeItem: searchKey [" + searchKey + "]");
		List values = new ArrayList();
		for (int i = 0; i < _attributes.size(); i++) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) (_attributes.get(i));
			if (attribute.getKey().equalsIgnoreCase(searchKey))
				values.add(attribute);
		} // for (int i = 0; i < _attributes.size(); i++)
		if (values.size() == 0) {
			// TracerSingleton.log(Constants.NOME_MODULO,
			// TracerSingleton.DEBUG,
			// "SourceBean::getAttributeItem: attributo ["
			// + searchKey + "] non trovato");
			return null;
		} // if (values.size() == 0)
		if (values.size() == 1) {
			if (deepSearch) {
				if (!((((SourceBeanAttribute) (values.get(0))).getValue()) instanceof SourceBean)) {
					TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::getAttributeItem: attributo [" + searchKey
							+ "] non 衵n SourceBean");
					return null;
				} // if (!(value instanceof SourceBean))
				String childKey = key.substring(key.indexOf('.') + 1, key.length());
				return ((SourceBean) (((SourceBeanAttribute) (values.get(0))).getValue())).getAttributeItem(childKey);
			} // if (deepSearch)
			return values.get(0);
		} // if (values.size() == 1)
		if (deepSearch) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::getAttributeItem: attributo [" + searchKey
					+ "] multivalore, impossibile eseguire una ricerca nidificata");
			return null;
		} // if (deepSearch)
		return values;
	} // public Object getAttributeItem(String key)

	/**
	 * Ritorna true se l'oggetto sourceBean contiene almento un elemento con chiave <em>key</em>.
	 * <p>
	 *
	 * @param key
	 *            chiave dell'attributo in dot-notation
	 * @return esito della ricerca dell'elemento.
	 */
	public boolean containsAttribute(String key) {
		Object value = getAttributeItem(key);
		return (value != null) ? true : false;
	} // public boolean containsAttribute(String key)

	/**
	 * Ritorna tutti i valori dell'attributo con chiave <em>key</em>.
	 * <p>
	 *
	 * @param key
	 *            chiave dell'attributo in dot-notation
	 * @return <ul>
	 *         <li> <em>null</em> se l'attributo non esiste <li> l'oggetto corrispondente alla chiave se l'attributo &egrave; single-value <li> <em>List</em>
	 *         arraylist degli oggetti corrispondenti alla chiave se l'attributo &egrave; multi-value
	 *         </ul>
	 * @see SourceBean#setAttribute(String, Object)
	 * @see SourceBean#setAttribute(SourceBean)
	 * @see SourceBean#updAttribute(String, Object)
	 * @see SourceBean#delAttribute(String)
	 */
	public Object getAttribute(String key) {
		// TracerSingleton.log(Constants.NOME_MODULO,
		// TracerSingleton.DEBUG, "SourceBean::getAttribute: key [" + key + "]");
		Object attributeItem = getAttributeItem(key);
		if (attributeItem == null)
			return null;
		if (attributeItem instanceof SourceBeanAttribute) {
			// TracerSingleton.log(Constants.NOME_MODULO,
			// TracerSingleton.DEBUG,
			// "SourceBean::getAttributeItem: attributo ["
			// + key + "] trovato");
			SourceBeanAttribute sourceBeanAttribute = (SourceBeanAttribute) attributeItem;
			return sourceBeanAttribute.getValue();
		} // if (attributeItem instanceof SourceBeanAttribute)
			// TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG,
			// "SourceBean::getAttributeItem: attributo [" + key +
			// "] multivalore");
		List sourceBeanAttributes = (List) attributeItem;
		List values = new ArrayList(sourceBeanAttributes.size());
		for (int i = 0; i < sourceBeanAttributes.size(); i++) {
			SourceBeanAttribute sourceBeanAttribute = (SourceBeanAttribute) (sourceBeanAttributes.get(i));
			values.add(sourceBeanAttribute.getValue());
		} // for (int i = 0; i < sourceBeanAttributes.size(); i++)
		return values;
	} // public Object getAttribute(String key)

	/**
	 * Ritorna tutti i valori dell'attributo con chiave <em>key</em>.
	 * <p>
	 *
	 * @param key
	 *            chiave dell'attributo in dot-notation
	 * @return <em>List</em> arraylist degli oggetti corrispondenti alla chiave, di dimensione nulla se nessun attributo viene trovato
	 * @see SourceBean#setAttribute(String, Object)
	 * @see SourceBean#setAttribute(SourceBean)
	 * @see SourceBean#updAttribute(String, Object)
	 * @see SourceBean#delAttribute(String)
	 */
	public List getAttributeAsList(String key) {
		Object value = getAttribute(key);
		if (value == null)
			return new ArrayList(0);
		if (value instanceof List)
			return (List) value;
		List values = new ArrayList(1);
		values.add(value);
		return values;
	} // public Vector getAttributeAsVector(String key)

	/**
	 * Aggiunge al <code>SourceBean</code> un nuovo attributo con chiave <em>key</em> e valore <em>value</em>. Se il valore dell'attributo &egrave; un
	 * <code>SourceBean</code> il servizio &egrave; equivalente a: <blockquote>
	 *
	 * <pre>
	 * SourceBean keySourceBean = new SourceBean(key);
	 * keySourceBean.setAttribute(value);
	 * this.setAttribute(keySourceBean);
	 * </pre>
	 *
	 * </blockquote>
	 *
	 * @param key
	 *            chiave dell'attributo in dot-notation
	 * @param value
	 *            valore dell'attributo
	 * @exception SourceBeanException
	 *                viene lanciata in tutti i casi in cui la chiave espressa in dot-notation non sia corretta
	 * @see SourceBean#getAttribute(String)
	 * @see SourceBean#setAttribute(SourceBean)
	 * @see SourceBean#updAttribute(String, Object)
	 * @see SourceBean#delAttribute(String)
	 */
	public void setAttribute(String key, Object value) throws SourceBeanException {
		if (key == null) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::setAttribute: chiave nulla");
			throw new SourceBeanException("Chiave nulla");
		} // if (key == null)
			// TracerSingleton.log(Constants.NOME_MODULO,
			// TracerSingleton.DEBUG, "SourceBean::setAttribute: key [" + key + "]");
		if (key.indexOf('.') != -1) {
			String searchKey = key.substring(0, key.indexOf('.'));
			String childKey = key.substring(key.indexOf('.') + 1, key.length());
			Object attribute = getAttribute(searchKey);
			if ((attribute != null) && (attribute instanceof List)) {
				ArrayList filteredAttribute = new ArrayList();
				for (int i = 0; i < ((ArrayList) attribute).size(); i++) {
					Object attributeItem = ((ArrayList) attribute).get(i);
					if (attributeItem instanceof SourceBean)
						filteredAttribute.add(attributeItem);
				} // for (int i = 0; i < ((ArrayList)attribute).size(); i++)
				if (filteredAttribute.size() == 0)
					attribute = null;
				else if (filteredAttribute.size() == 1)
					attribute = filteredAttribute.get(0);
				else
					attribute = filteredAttribute;
			} // if (attribute instanceof List)
			if ((attribute != null) && !(attribute instanceof List) && !(attribute instanceof SourceBean))
				attribute = null;
			if (attribute == null) {
				SourceBean sourceBean = new SourceBean(searchKey);
				sourceBean.setAttribute(childKey, value);
				setAttribute(sourceBean);
				return;
			} // if (attribute == null)
			if (attribute instanceof SourceBean) {
				SourceBean sourceBean = (SourceBean) attribute;
				sourceBean.setAttribute(childKey, value);
				return;
			} // if (attribute instanceof SourceBean)
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::setAttribute: attributo [" + searchKey
					+ "] multivalore, impossibile eseguire una ricerca nidificata");
			throw new SourceBeanException("Attributo [" + searchKey + "] multivalore, impossibile eseguire una ricerca nidificata");
		} // if (key.indexOf('.') != -1)
		if (value instanceof SourceBean) {
			SourceBean sourceBean = new SourceBean(key);
			sourceBean.setAttribute((SourceBean) value);
			setAttribute(sourceBean);
			return;
		} // if (value instanceof SourceBean)
		_attributes.add(new SourceBeanAttribute(key, value));
	} // public void setAttribute(Object key, Object value) throws SourceBeanException

	/**
	 * Aggiunge al <code>SourceBean</code> un nuovo attributo il cui valore &egrave; un <code>SourceBean</code>. La chiave con cui il secondo
	 * <code>SourceBean</code> viene aggiunto &egrave; pari al nome del contenitore stesso.
	 * <p>
	 *
	 * @param value
	 *            <code>SourceBean</code> da aggiungere
	 * @exception SourceBeanException
	 *                viene lanciata se <em>value</em> &egrave; <em>null</em>
	 * @see SourceBean#getAttribute(String)
	 * @see SourceBean#setAttribute(String, Object)
	 * @see SourceBean#updAttribute(String, Object)
	 * @see SourceBean#delAttribute(String)
	 */
	public void setAttribute(SourceBean value) throws SourceBeanException {
		// TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG,
		// "SourceBean::setAttribute: value [" + value.getName() + "]");
		SourceBeanAttribute.validateValue(value);
		_attributes.add(new SourceBeanAttribute(value.getName(), value));
	} // public void setAttribute(SourceBean value) throws SourceBeanException

	public void setAttribute(SourceBeanAttribute attribute) throws SourceBeanException {
		_attributes.add(attribute);
	} // public void setAttribute(SourceBean value) throws SourceBeanException

	/**
	 * Sostituisce il valore dell'attributo con chiave <em>key</em> con il nuovo valore <em>value</em>. Se l'attributo non esiste viene aggiunto. Nel caso in
	 * cui alla chiave corrisponda un attributo multi-value viene lanciata l'eccezione <code>SourceBeanException</code>.
	 * <p>
	 *
	 * @param key
	 *            chiave dell'attributo in dot-notation
	 * @param value
	 *            valore dell'attributo
	 * @exception SourceBeanException
	 *                viene lanciata in tutti i casi in cui la chiave espressa in dot-notation non sia corretta
	 * @see SourceBean#getAttribute(String)
	 * @see SourceBean#setAttribute(String, Object)
	 * @see SourceBean#setAttribute(SourceBean)
	 * @see SourceBean#delAttribute(String)
	 */
	public void updAttribute(String key, Object value) throws SourceBeanException {
		if (key == null) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::updAttribute: chiave nulla");
			throw new SourceBeanException("Chiave nulla");
		} // if (key == null)
			// TracerSingleton.log(Constants.NOME_MODULO,
			// TracerSingleton.DEBUG, "SourceBean::updAttribute: key [" + key + "]");
		if (key.indexOf('.') != -1) {
			String searchKey = key.substring(0, key.indexOf('.'));
			String childKey = key.substring(key.indexOf('.') + 1, key.length());
			Object attribute = getAttribute(searchKey);
			if (attribute == null) {
				SourceBean sourceBean = new SourceBean(searchKey);
				sourceBean.updAttribute(childKey, value);
				setAttribute(sourceBean);
				return;
			} // if (attribute == null)
			if (attribute instanceof SourceBean) {
				SourceBean sourceBean = (SourceBean) attribute;
				sourceBean.updAttribute(childKey, value);
				return;
			} // if (attribute instanceof SourceBean)
			if (attribute instanceof List) {
				TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::updAttribute: attributo [" + searchKey
						+ "] multivalore, impossibile eseguire una ricerca nidificata");
				throw new SourceBeanException("Attributo [" + searchKey + "] multivalore, impossibile eseguire una ricerca nidificata");
			} // if (attribute instanceof List)
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::updAttribute: attributo [" + searchKey + "] non 衵n SourceBean");
			throw new SourceBeanException("Attributo [" + searchKey + "] non 衵n SourceBean");
		} // if (key.indexOf('.') != -1)
		Object attributeItem = getAttributeItem(key);
		if (attributeItem == null) {
			setAttribute(key, value);
			return;
		} // if (attributeItem == null)
		if (attributeItem instanceof List) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::updAttribute: attributo [" + key
					+ "] multivalore, impossibile eseguire aggiornamento");
			throw new SourceBeanException("Attributo [" + key + "] multivalore, impossibile eseguire aggiornamento");
		} // if (attributeItem instanceof List)
		SourceBeanAttribute sourceBeanAttribute = (SourceBeanAttribute) attributeItem;
		if (value instanceof SourceBean) {
			SourceBean sourceBean = new SourceBean(key);
			sourceBean.setAttribute((SourceBean) value);
			sourceBeanAttribute.setValue(sourceBean);
			return;
		} // if (value instanceof SourceBean)
		sourceBeanAttribute.setValue(value);
	} // public void updAttribute(Object key, Object value) throws SourceBeanException

	public void updAttribute(SourceBean value) throws SourceBeanException {
		Object attributeItem = getAttributeItem(value.getName());
		if (attributeItem == null) {
			setAttribute(value);
			return;
		} // if (attributeItem == null)
		if (attributeItem instanceof List) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::updAttribute: attributo [" + value.getName()
					+ "] multivalore, impossibile eseguire aggiornamento");
			throw new SourceBeanException("Attributo [" + value.getName() + "] multivalore, impossibile eseguire aggiornamento");
		} // if (attributeItem instanceof List)
		SourceBeanAttribute sourceBeanAttribute = (SourceBeanAttribute) attributeItem;
		// sourceBeanAttribute.setKey(value.getName());
		sourceBeanAttribute.setValue(value);
	} // public void updAttribute(SourceBean value) throws SourceBeanException

	/**
	 * Elimina tutti i valori dell'attributo con chiave <em>key</em>.
	 * <p>
	 *
	 * @param key
	 *            chiave dell'attributo in dot-notation
	 * @exception SourceBeanException
	 *                viene lanciata in tutti i casi in cui la chiave espressa in dot-notation non sia corretta
	 * @see SourceBean#getAttribute(String)
	 * @see SourceBean#setAttribute(String, Object)
	 * @see SourceBean#setAttribute(SourceBean)
	 * @see SourceBean#updAttribute(String, Object)
	 */
	public void delAttribute(String key) throws SourceBeanException {
		if (key == null) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::delAttribute: chiave nulla");
			throw new SourceBeanException("Chiave nulla");
		} // if (key == null)
			// TracerSingleton.log(Constants.NOME_MODULO,
			// TracerSingleton.DEBUG, "SourceBean::delAttribute: key [" + key + "]");
		if (key.indexOf('.') != -1) {
			String searchKey = key.substring(0, key.indexOf('.'));
			ArrayList values = new ArrayList();
			for (int i = 0; i < _attributes.size(); i++) {
				SourceBeanAttribute attribute = (SourceBeanAttribute) (_attributes.get(i));
				if (attribute.getKey().equalsIgnoreCase(searchKey))
					values.add(attribute.getValue());
			} // for (int i = 0; i < _attributes.size(); i++)
			if (values.size() == 0) {
				// TracerSingleton.log(Constants.NOME_MODULO,
				// TracerSingleton.DEBUG,
				// "SourceBean::delAttribute:
				// attributo [" + searchKey + "] non trovato");
				return;
			} // if (values.size() == 0)
			if (values.size() == 1) {
				if (!((values.get(0)) instanceof SourceBean)) {
					TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::delAttribute: attributo [" + searchKey
							+ "] non 衵n SourceBean");
					throw new SourceBeanException("Attributo [" + searchKey + "] non 衵n SourceBean");
				} // if (!(value instanceof SourceBean))
				((SourceBean) (values.get(0))).delAttribute(key.substring(key.indexOf('.') + 1, key.length()));
				return;
			} // if (values.size() == 1)
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::delAttribute: attributo [" + searchKey
					+ "] multivalore, impossibile eseguire una ricerca nidificata");
			throw new SourceBeanException("Attributo [" + searchKey + "] multivalore, impossibile eseguire una ricerca nidificata");
		} // if (key.indexOf('.') != -1)

		for (Iterator it = _attributes.iterator(); it.hasNext();) {
			SourceBeanAttribute sba = (SourceBeanAttribute) it.next();
			if (sba.getKey().equalsIgnoreCase(key)) {
				it.remove();
			}
		}
	} // public void delAttribute(String key) throws SourceBeanException

	/**
	 * Ritorna il testo contenuto nel <code>SourceBean</code>.
	 * <p>
	 *
	 * @return il testo contenuto
	 * @see SourceBean#getCharacters(String)
	 * @see SourceBean#setCharacters(String)
	 * @see SourceBean#setCharacters(String, String)
	 * @see SourceBean#delCharacters()
	 * @see SourceBean#delCharacters(String)
	 */
	public String getCharacters() {
		return _characters;
	} // public String getCharacters()

	/**
	 * Ritorna il testo contenuto nel <code>SourceBean</code> corrispondente all'attributo di chiave <em>key</em>.
	 * <p>
	 *
	 * @param key
	 *            chiave dell'attributo in dot-notation
	 * @return il testo contenuto
	 * @see SourceBean#getCharacters()
	 * @see SourceBean#setCharacters(String)
	 * @see SourceBean#setCharacters(String, String)
	 * @see SourceBean#delCharacters()
	 * @see SourceBean#delCharacters(String)
	 */
	public String getCharacters(String key) {
		// TracerSingleton.log(Constants.NOME_MODULO,
		// TracerSingleton.DEBUG, "SourceBean::getCharacters: key [" + key + "]");
		Object attribute = getAttribute(key);
		if ((attribute == null) || (!(attribute instanceof SourceBean)))
			return null;
		return ((SourceBean) attribute).getCharacters();
	} // public String getCharacters(String key)

	/**
	 * Sostituisce il testo contenuto con quello del parametro <em>characters</em>.
	 * <p>
	 *
	 * @param characters
	 *            il nuovo testo
	 * @see SourceBean#getCharacters(String)
	 * @see SourceBean#getCharacters(String, String)
	 * @see SourceBean#setCharacters(String, String)
	 * @see SourceBean#delCharacters()
	 * @see SourceBean#delCharacters(String)
	 */
	public void setCharacters(String characters) {
		// Di default il testo viene trimmato
		setCharacters(characters, _trim);
	} // public void setCharacters(String characters)

	/**
	 * Sostituisce il testo contenuto con quello del parametro <em>characters</em>.
	 * <p>
	 *
	 * @param characters
	 *            il nuovo testo
	 * @param trim
	 *            true se il testo deve essere trimmato
	 * @see SourceBean#getCharacters(String)
	 * @see SourceBean#getCharacters(String, String)
	 * @see SourceBean#setCharacters(String, String)
	 * @see SourceBean#delCharacters()
	 * @see SourceBean#delCharacters(String)
	 */
	public void setCharacters(String characters, boolean trim) {
		delCharacters();
		if ((characters == null) || (characters.trim().length() == 0))
			return;
		_characters = trim ? characters.trim() : characters;
	} // public void setCharacters(String characters)

	/**
	 * Sostituisce il testo contenuto nel <code>SourceBean</code> corrispondente all'attributo di chiave <em>key</em> con quello del parametro
	 * <em>characters</em>.
	 * <p>
	 *
	 * @param key
	 *            chiave dell'attributo in dot-notation
	 * @param characters
	 *            il nuovo testo
	 * @see SourceBean#getCharacters(String)
	 * @see SourceBean#getCharacters(String, String)
	 * @see SourceBean#setCharacters(String)
	 * @see SourceBean#delCharacters()
	 * @see SourceBean#delCharacters(String)
	 */
	public void setCharacters(String key, String characters) throws SourceBeanException {
		Object attribute = getAttribute(key);
		if ((attribute == null) || (!(attribute instanceof SourceBean))) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::setCharacters: chiave errata");
			throw new SourceBeanException("Chiave errata");
		} // if ((attribute == null) || (!(attribute instanceof SourceBean)))
		((SourceBean) attribute).setCharacters(characters);
	} // public void setCharacters(String key, String characters) throws SourceBeanException

	/**
	 * Elimina il testo contenuto nel <code>SourceBean</code>.
	 * <p>
	 *
	 * @see SourceBean#getCharacters()
	 * @see SourceBean#getCharacters(String)
	 * @see SourceBean#setCharacters(String)
	 * @see SourceBean#setCharacters(String, String)
	 * @see SourceBean#delCharacters(String)
	 */
	public void delCharacters() {
		_characters = null;
	} // public void delCharacters()

	/**
	 * Elimina il testo contenuto nel <code>SourceBean</code> corrispondente all'attributo di chiave <em>key</em>.
	 * <p>
	 *
	 * @param key
	 *            chiave dell'attributo in dot-notation
	 * @see SourceBean#getCharacters()
	 * @see SourceBean#getCharacters(String)
	 * @see SourceBean#setCharacters(String)
	 * @see SourceBean#setCharacters(String, String)
	 * @see SourceBean#delCharacters()
	 */
	public void delCharacters(String key) {
		// TracerSingleton.log(Constants.NOME_MODULO,
		// TracerSingleton.DEBUG, "SourceBean::getCharacters: key [" + key + "]");
		Object attribute = getAttribute(key);
		if ((attribute == null) || (!(attribute instanceof SourceBean)))
			return;
		((SourceBean) attribute).delCharacters();
	} // public void delCharacters(String key)

	/**
	 * Ritorna un List contenente tutti i valori dell'attributo con chiave <em>key</em> che sono di tipo <code>SourceBean</code> e contengono un attributo
	 * single-value con chiave <em>paramName</em> e valore <em>paramValue</em>.
	 * <p>
	 *
	 * @param key
	 *            chiave dell'attributo in dot-notation
	 * @param paramName
	 *            nome del parametro di filtro
	 * @param paramValue
	 *            valore del parametro di filtro
	 * @return <em>List</em> arraylist di <code>SourceBean</code> se pi&ugrave; valori vengono trovati </ul>
	 * @see SourceBean#getAttribute(String)
	 */
	public List getFilteredSourceBeanAttributeAsList(String key, String paramName, String paramValue) {
		List sourceBeanAttributeValues = new ArrayList();
		if (key == null) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::getFilteredSourceBeanAttributeAsList: chiave nulla");
			return sourceBeanAttributeValues;
		} // if (key == null)
		if (paramName == null) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::getFilteredSourceBeanAttributeAsList: nome parametro nullo");
			return sourceBeanAttributeValues;
		} // if ((paramName == null)
			// TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG,
			// "SourceBean::getFilteredSourceBeanAttribute: key
			// [" + key + "], paramName [" + paramName +
			// "], paramValue [" + paramValue + "]");
		Object attributeValue = getAttribute(key);
		if (attributeValue == null)
			return sourceBeanAttributeValues;
		List attributeValues = null;
		if (attributeValue instanceof List)
			attributeValues = (List) attributeValue;
		else {
			attributeValues = new ArrayList(1);
			attributeValues.add(attributeValue);
		} // if (attributeValue instanceof List)
		for (int i = 0; i < attributeValues.size(); i++) {
			attributeValue = attributeValues.get(i);
			if (!(attributeValue instanceof SourceBean))
				continue;
			Object localParamValue = ((SourceBean) attributeValue).getAttribute(paramName);
			if (((localParamValue == null) && (paramValue != null)) || ((localParamValue != null) && (paramValue == null)))
				continue;
			if (((localParamValue == null) && (paramValue == null))
					|| ((localParamValue instanceof String) && (((String) localParamValue).equalsIgnoreCase(paramValue))))
				sourceBeanAttributeValues.add(attributeValue);
		} // for (int i = 0; i < attributeValues.size(); i++)
		return sourceBeanAttributeValues;
	} // public List
		// getFilteredSourceBeanAttributeAsList(String key, String
		// paramName, String paramValue)

	/**
	 * Ritorna tutti i valori dell'attributo con chiave <em>key</em> che sono di tipo <code>SourceBean</code> e contengono un attributo single-value con chiave
	 * <em>paramName</em> e valore <em>paramValue</em>.
	 * <p>
	 *
	 * @param key
	 *            chiave dell'attributo in dot-notation
	 * @param paramName
	 *            nome del parametro di filtro
	 * @param paramValue
	 *            valore del parametro di filtro
	 * @return <ul>
	 *         <li> <em>null</em> se nessun valore viene trovato <li> <code>SourceBean</code> se un solo valore viene trovato <li> <em>List</em> arraylist di
	 *         <code>SourceBean</code> se pi&ugrave; valori vengono trovati
	 *         </ul>
	 * @see SourceBean#getAttribute(String)
	 */
	public Object getFilteredSourceBeanAttribute(String key, String paramName, String paramValue) {
		List sourceBeanAttributeValues = getFilteredSourceBeanAttributeAsList(key, paramName, paramValue);
		if (sourceBeanAttributeValues.size() == 0)
			return null;
		if (sourceBeanAttributeValues.size() == 1)
			return sourceBeanAttributeValues.get(0);
		return sourceBeanAttributeValues;
	} // public Object getFilteredSourceBeanAttribute(String
		// key, String paramName, String paramValue)

	/**
	 * Ritorna tutti gli oggetti di tipo <code>SourceBeanAttribute</code> contenuti.
	 * <p>
	 *
	 * @return <code>List</code> arraylist di <code>SourceBeanAttribute</code> contenuti
	 * @see SourceBean#getContainedAttributes(String)
	 * @see SourceBean#setContainedAttributes(List)
	 * @see SourceBean#setContainedAttributes(String, List)
	 * @see SourceBean#delContainedAttributes()
	 * @see SourceBean#delContainedAttributes(String)
	 */
	public List getContainedAttributes() {
		return (List) ((ArrayList) _attributes).clone();
	} // public List getContainedAttributes()

	/**
	 * Ritorna tutti gli oggetti di tipo <code>SourceBeanAttribute</code> contenuti nel <code>SourceBean</code> corrispondente all'attributo di chiave
	 * <em>key</em>.
	 * <p>
	 *
	 * @param key
	 *            chiave dell'attributo in dot-notation
	 * @return <code>List</code> list di <code>SourceBeanAttribute</code> contenuti
	 * @see SourceBean#getContainedAttributes()
	 * @see SourceBean#setContainedAttributes(List)
	 * @see SourceBean#setContainedAttributes(String, List)
	 * @see SourceBean#delContainedAttributes()
	 * @see SourceBean#delContainedAttributes(String)
	 */
	public List getContainedAttributes(String key) {
		// TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG,
		// "SourceBean::getContainedAttributes: key [" + key + "]");
		Object attribute = getAttribute(key);
		if ((attribute == null) || (!(attribute instanceof SourceBean)))
			return new ArrayList(0);
		return ((SourceBean) attribute).getContainedAttributes();
	} // public List getContainedAttributes(String key)

	/**
	 * Sostituisce tutti gli oggetti di tipo <code>SourceBeanAttribute</code> contenuti con quelli del vettore <em>attributes</em>.
	 * <p>
	 *
	 * @param attributes
	 *            <code>List</code> list <code>SourceBeanAttribute</code> dei nuovi attributi
	 * @see SourceBean#getContainedAttributes()
	 * @see SourceBean#getContainedAttributes(String)
	 * @see SourceBean#setContainedAttributes(String, List)
	 * @see SourceBean#delContainedAttributes()
	 * @see SourceBean#delContainedAttributes(String)
	 */
	public void setContainedAttributes(List attributes) {
		delContainedAttributes();
		_attributes = new ArrayList(attributes);
	} // public void setContainedAttributes(List attributes)

	/**
	 * Sostituisce tutti gli oggetti di tipo <code>SourceBeanAttribute</code> contenuti nel <code>SourceBean</code> corrispondente all'attributo di chiave
	 * <em>key</em> con quelli del vettore <em>attributes</em>.
	 * <p>
	 *
	 * @param attributes
	 *            <code>List</code> list di <code>SourceBeanAttribute</code> dei nuovi attributi
	 * @see SourceBean#getContainedAttributes()
	 * @see SourceBean#getContainedAttributes(String)
	 * @see SourceBean#setContainedAttributes(List)
	 * @see SourceBean#delContainedAttributes()
	 * @see SourceBean#delContainedAttributes(String)
	 */
	public void setContainedAttributes(String key, List attributes) throws SourceBeanException {
		Object attribute = getAttribute(key);
		if ((attribute == null) || (!(attribute instanceof SourceBean))) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::setContainedAttributes: chiave errata");
			throw new SourceBeanException("Chiave errata");
		} // if ((attribute == null) || (!(attribute instanceof SourceBean)))
		((SourceBean) attribute).setContainedAttributes(attributes);
	} // public List getContainedAttributes()

	public void updContainedAttributes(List attributes) throws SourceBeanException {
		if (attributes == null)
			return;
		for (int i = 0; i < attributes.size(); i++) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) attributes.get(i);
			String attributeKey = attribute.getKey();
			Object attributeValue = attribute.getValue();
			if (attributeValue instanceof SourceBean)
				updAttribute((SourceBean) attributeValue);
			else
				updAttribute(attributeKey, attributeValue);
		} // for (int i = 0; i < attributes.size(); i++)
	} // public void updContainedAttributes(List attributes) throws SourceBeanException

	public void updContainedAttributes(String key, ArrayList attributes) throws SourceBeanException {
		Object attribute = getAttribute(key);
		if ((attribute == null) || (!(attribute instanceof SourceBean))) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::updContainedAttributes: chiave errata");
			throw new SourceBeanException("Chiave errata");
		} // if ((attribute == null) || (!(attribute instanceof SourceBean)))
		((SourceBean) attribute).updContainedAttributes(attributes);
	} // public List getContainedAttributes()

	/**
	 * Elimina tutti gli attributi contenuti nel <code>SourceBean</code>.
	 * <p>
	 *
	 * @see SourceBean#getContainedAttributes()
	 * @see SourceBean#getContainedAttributes(String)
	 * @see SourceBean#setContainedAttributes(List)
	 * @see SourceBean#setContainedAttributes(String, List)
	 * @see SourceBean#delContainedAttributes(String)
	 */
	public void delContainedAttributes() {
		_attributes.clear();
	} // public void delContainedAttributes()

	/**
	 * Elimina tutti gli attributi contenuti nel <code>SourceBean</code> corrispondente all'attributo di chiave <em>key</em>.
	 * <p>
	 *
	 * @param key
	 *            chiave dell'attributo in dot-notation
	 * @see SourceBean#getContainedAttributes()
	 * @see SourceBean#getContainedAttributes(String)
	 * @see SourceBean#setContainedAttributes(List)
	 * @see SourceBean#setContainedAttributes(String, List)
	 * @see SourceBean#delContainedAttributes()
	 */
	public void delContainedAttributes(String key) {
		// TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG,
		// "SourceBean::delContainedAttributes: key [" + key + "]");
		Object attribute = getAttribute(key);
		if ((attribute == null) || (!(attribute instanceof SourceBean)))
			return;
		((SourceBean) attribute).delContainedAttributes();
	} // public void delContainedAttributes(String key)

	/**
	 * Ritorna gli oggetti di tipo <code>SourceBeanAttribute</code> contenuti il cui valore associato &egrave; di tipo <code>SourceBean</code>.
	 * <p>
	 *
	 * @return <code>List</code> arrayList di <code>SourceBeanAttribute</code>
	 * @see SourceBean#getContainedSourceBeanAttributes(String)
	 * @see SourceBean#getContainedAttributes()
	 * @see SourceBean#getContainedAttributes(String)
	 */
	public List getContainedSourceBeanAttributes() {
		List attributes = new ArrayList();
		for (int i = 0; i < _attributes.size(); i++) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) (_attributes.get(i));
			if (attribute.getValue() instanceof SourceBean)
				attributes.add(attribute);
		} // for (int i = 0; i < _attributes.size(); i++)
		return attributes;
	} // public void Object getContainedSourceBeanAttributes()

	/**
	 * Ritorna gli oggetti di tipo <code>SourceBeanAttribute</code> contenuti nel <code>SourceBean</code> corrispondente all'attributo di chiave <em>key</em> ed
	 * il cui valore associato &egrave; di tipo <code>SourceBean</code>.
	 * <p>
	 *
	 * @param key
	 *            chiave dell'attributo in dot-notation
	 * @return <code>List</code> il vettore di <code>SourceBeanAttribute</code>
	 * @see SourceBean#getContainedSourceBeanAttributes()
	 * @see SourceBean#getContainedAttributes()
	 * @see SourceBean#getContainedAttributes(String)
	 */
	public List getContainedSourceBeanAttributes(String key) {
		Object attribute = getAttribute(key);
		if ((attribute == null) || (!(attribute instanceof SourceBean)))
			return new ArrayList(0);
		return ((SourceBean) attribute).getContainedSourceBeanAttributes();
	} // public List getContainedSourceBeanAttributes(String key)

	/**
	 * Ritorna gli oggetti di tipo <code>SourceBeanAttribute</code> contenuti il cui valore associato &egrave; di tipo <code>XMLObject</code>.
	 * <p>
	 *
	 * @return <code>List</code> arraylist di <code>SourceBeanAttribute</code>
	 * @see SourceBean#getContainedNotXMLObjectAttributes()
	 * @see SourceBean#getContainedAttributes()
	 * @see SourceBean#getContainedAttributes(String)
	 */
	private List getContainedXMLObjectAttributes() {
		List attributes = new ArrayList();
		for (int i = 0; i < _attributes.size(); i++) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) (_attributes.get(i));
			if (attribute.getValue() instanceof XMLObject)
				attributes.add(attribute);
		} // for (int i = 0; i < _attributes.size(); i++)
		return attributes;
	} // private List getContainedXMLObjectAttributes()

	/**
	 * Ritorna gli oggetti di tipo <code>SourceBeanAttribute</code> contenuti il cui valore associato non &egrave; di tipo <code>XMLObject</code>.
	 * <p>
	 *
	 * @return <code>List</code> arraylist di <code>SourceBeanAttribute</code>
	 * @see SourceBean#getContainedXMLObjectAttributes()
	 * @see SourceBean#getContainedAttributes()
	 * @see SourceBean#getContainedAttributes(String)
	 */
	private List getContainedNotXMLObjectAttributes() {
		List attributes = new ArrayList();
		for (int i = 0; i < _attributes.size(); i++) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) (_attributes.get(i));
			if (!(attribute.getValue() instanceof XMLObject))
				attributes.add(attribute);
		} // for (int i = 0; i < _attributes.size(); i++)
		return attributes;
	} // private Vector getContainedNotXMLObjectAttributes()

	/**
	 * Ritorna il vettore di chiavi in dot-notation degli attributi a cui &egrave; associata la chiave <em>key</em>.
	 * <p>
	 *
	 * @param key
	 *            chiave dell'attributo <em>non</em> in dot-notation
	 * @return <code>List</code> il vettore di chiavi in dot-notation
	 */
	public Vector getFullKeyPaths(String key) {
		Vector fullKeyPaths = new Vector();
		getFullKeyPaths(key, "", fullKeyPaths);
		return fullKeyPaths;
	} // public Vector getFullKeyPaths(String key)

	private void getFullKeyPaths(String key, String path, Vector fullKeyPaths) {
		for (int i = 0; i < _attributes.size(); i++) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) (_attributes.get(i));
			if (attribute.getKey().equalsIgnoreCase(key)) {
				fullKeyPaths.addElement(path + "." + key);
				break;
			} // if (attribute.getKey().equalsIgnoreCase(key))
		} // for (int i = 0; i < _attributes.size(); i++)
		for (int i = 0; i < _attributes.size(); i++) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) (_attributes.get(i));
			if (attribute.getValue() instanceof SourceBean) {
				SourceBean sourceBean = (SourceBean) (attribute.getValue());
				String newPath = null;
				if (path == null)
					newPath = sourceBean.getName();
				else
					newPath = path + "." + sourceBean.getName();
				sourceBean.getFullKeyPaths(key, newPath, fullKeyPaths);
			} // if (attribute.getValue() instanceof SourceBean)
		} // for (int i = 0; i < _attributes.size(); i++)
	} // private void getFullKeyPaths(String key, String
		// path, Vector fullKeyPaths)

	public static SourceBean fromXMLStream(InputSource stream, boolean trimCharacters, boolean upperCase) throws SourceBeanException {
		if (stream == null) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.WARNING, "SourceBean::fromXMLStream: stream nullo");
			return null;
		} // if (stream == null)
		SourceBean sourceBean = null;
		XMLReader parser = new SAXParser();
		try {
			SourceBeanContentHandler contentHandler = new SourceBeanContentHandler(trimCharacters, upperCase);
			parser.setContentHandler(contentHandler);
			parser.parse(stream);
			sourceBean = contentHandler.getSourceBean();
		} // try
		catch (Exception ex) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "SourceBean::fromXMLString: impossibile elaborare lo stream XML", ex);
			throw new SourceBeanException("Impossibile elaborare lo stream XML");
		} // catch (Exception ex) try
		return sourceBean;
	}

	public static SourceBean fromXMLStream(InputSource stream, boolean trimCharacters) throws SourceBeanException {
		return fromXMLStream(stream, trimCharacters, true);
	}

	/**
	 * Ritorna il <code>SourceBean</code> ottenuto dal parsing dell'<code>InputSource</code> <em>stream</em>.
	 * <p>
	 *
	 * @param stream
	 *            rappresentazione XML del <code>SourceBean</code>
	 * @return il <code>SourceBean</code> corrispondente allo stream XML
	 * @see SourceBean#fromXMLString(String)
	 * @see SourceBean#fromXMLFile(String)
	 */
	public static SourceBean fromXMLStream(InputSource stream) throws SourceBeanException {
		return fromXMLStream(stream, true, true);
	} // public static SourceBean fromXMLStream(InputSource stream) throws SourceBeanException

	/**
	 * Ritorna il <code>SourceBean</code> ottenuto dal parsing della stringa <em>xmlSourceBean</em>.
	 * <p>
	 *
	 * @param xmlSourceBean
	 *            rappresentazione XML del <code>SourceBean</code>
	 * @return il <code>SourceBean</code> corrispondente allo stream XML
	 * @see SourceBean#fromXMLStream(InputSource)
	 * @see SourceBean#fromXMLFile(String)
	 */
	public static SourceBean fromXMLString(String xmlSourceBean, boolean trimCharacters, boolean upperCase) throws SourceBeanException {
		// TracerSingleton.log(Constants.NOME_MODULO,
		// TracerSingleton.DEBUG, "SourceBean::fromXMLString: invocato");
		if (xmlSourceBean == null) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "SourceBean::fromXMLString: xmlSourceBean non valido");
			throw new SourceBeanException("xmlSourceBean non valido");
		} // if (xmlSourceBean == null)
		xmlSourceBean = xmlSourceBean.trim();
		String uri = null;
		try {
			uri = (String) ConfigSingleton.getInstance().getAttribute("COMMON.FILE_URI_PREFIX");

		} catch (Exception e) {
			TracerSingleton.log("Spago", 4, "SourceBean::fromXMLString: impossible to read from ConfigSingleton the COMMON.FILE_URI_PREFIX", e);
		}
		if (uri == null)
			uri = "";
		if (!xmlSourceBean.startsWith("<")) {
			xmlSourceBean = XMLUtil.XML_HEADER_PREFIX + " version=\"" + XMLUtil.XML_HEADER_DEFAULT_VERSION + "\" " + "encoding=\""
					+ XMLUtil.XML_HEADER_DEFAULT_ENCODING + "\"?>\n"
					// + "<!DOCTYPE SOURCEBEAN SYSTEM \""
					// + uri
					// + XMLUtil.getDoctypeFilename()
					// + "\">\n"
					+ "<XMLSOURCEBEAN>\n" + xmlSourceBean + "\n</XMLSOURCEBEAN>";
			SourceBean xmlRequestBean = fromXMLStream(new InputSource(new StringReader(xmlSourceBean)));
			xmlSourceBean = xmlRequestBean.getCharacters();
		} // if (!xmlSourceBean.startsWith("<"))
		if (!xmlSourceBean.startsWith(XMLUtil.XML_HEADER_PREFIX))
			xmlSourceBean = XMLUtil.XML_HEADER_PREFIX + " version=\"" + XMLUtil.XML_HEADER_DEFAULT_VERSION + "\" " + "encoding=\""
					+ XMLUtil.XML_HEADER_DEFAULT_ENCODING + "\"?>\n"
					// + "<!DOCTYPE SOURCEBEAN SYSTEM \""
					// + uri
					// + XMLUtil.getDoctypeFilename()
					// + "\">\n"
					+ xmlSourceBean;
		return fromXMLStream(new InputSource(new StringReader(xmlSourceBean)), trimCharacters, upperCase);
	}

	public static SourceBean fromXMLString(String xmlSourceBean, boolean trimCharacters) throws SourceBeanException {
		return fromXMLString(xmlSourceBean, trimCharacters, true);
	}

	/**
	 * Ritorna il <code>SourceBean</code> ottenuto dal parsing della stringa <em>xmlSourceBean</em>.
	 * <p>
	 *
	 * @param xmlSourceBean
	 *            rappresentazione XML del <code>SourceBean</code>
	 * @return il <code>SourceBean</code> corrispondente allo stream XML
	 * @see SourceBean#fromXMLStream(InputSource)
	 * @see SourceBean#fromXMLFile(String)
	 */
	public static SourceBean fromXMLString(String xmlSourceBean) throws SourceBeanException {
		return fromXMLString(xmlSourceBean, true, true);
	} // public static SourceBean fromXMLString(String xmlSourceBean) throws SourceBeanException

	/**
	 * Ritorna il <code>SourceBean</code> ottenuto dal parsing del file <em>xmlSourceBean</em>.
	 * <p>
	 *
	 * @param xmlSourceBean
	 *            nome del file che contiene la rappresentazione XML del <code>SourceBean</code>
	 * @return il <code>SourceBean</code> corrispondente allo stream XML
	 * @see SourceBean#fromXMLStream(InputSource)
	 * @see SourceBean#fromXMLString(String)
	 */
	public static SourceBean fromXMLFile(String xmlSourceBean, boolean trimCharacters, boolean upperCase) throws SourceBeanException {
		if (xmlSourceBean == null) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "SourceBean::fromXMLFile: xmlSourceBean non valido");
			throw new SourceBeanException("XMLSourceBean non valido");
		} // if (xmlSourceBean == null)
		String rootPath = ConfigSingleton.getRootPath();
		if (rootPath == null) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "SourceBean::fromXMLFile: root path non valido");
			throw new SourceBeanException("Root path non valido");
		} // if (rootPath == null)
		xmlSourceBean = rootPath + xmlSourceBean;
		SourceBean sourceBean = null;
		try {
			InputSource stream = new InputSource(new FileReader(xmlSourceBean));
			sourceBean = fromXMLStream(stream, trimCharacters, upperCase);
		} // try
		catch (FileNotFoundException ex) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "SourceBean::fromXMLFile: file non trovato");
			throw new SourceBeanException("File non trovato");
		} // catch (FileNotFoundException ex)
		return sourceBean;
	} // public static SourceBean fromXMLFile(String xmlSourceBean) throws SourceBeanException

	public static SourceBean fromXMLFile(String xmlSourceBean, boolean trimCharacters) throws SourceBeanException {
		return fromXMLFile(xmlSourceBean, trimCharacters, true);
	}

	/**
	 * Ritorna il <code>SourceBean</code> ottenuto dal parsing del file <em>xmlSourceBean</em>.
	 * <p>
	 *
	 * @param xmlSourceBean
	 *            nome del file che contiene la rappresentazione XML del <code>SourceBean</code>
	 * @return il <code>SourceBean</code> corrispondente allo stream XML
	 * @see SourceBean#fromXMLStream(InputSource)
	 * @see SourceBean#fromXMLString(String)
	 */
	public static SourceBean fromXMLFile(String xmlSourceBean) throws SourceBeanException {
		return fromXMLFile(xmlSourceBean, true, true);
	}

	/**
	 * Ritorna un oggetto di tipo Element che verrࡵtilizzato nella rappresentazione in XML dell'oggetto.
	 *
	 * @return <code>Document<code> un oggetto di tipo Document.
	 */
	@Override
	public Element toElement(Document document, XMLSerializer serializer) {
		if (_upperCase)
			_sourceBeanName = _sourceBeanName.toUpperCase();
		Element element = document.createElement(_sourceBeanName);

		for (Iterator it = _namespaceMappings.iterator(); it.hasNext();) {
			NamespaceMapping mapping = (NamespaceMapping) it.next();
			String namespaceName = ((mapping.getPrefix() == null) || (mapping.getPrefix().length() == 0)) ? "" : (":" + mapping.getPrefix());
			element.setAttribute("xmlns" + namespaceName, mapping.getUri());
		}

		notXMLObjectAttributesToElement(document, element, serializer);

		if (_characters != null) {
			element.appendChild(document.createTextNode(_characters));
			// element.appendChild(document.createCDATASection(_characters));
		}

		// The occurence of characters is not alternative to the occurence of
		// attributes, so this statement hasnt to be in an ELSE branch of the
		// previous if
		XMLObjectAttributesToElement(document, element);

		return element;
	} // public Element toElement(Document document)

	/**
	 * Ritorna un oggetto di tipo Element che verrࡵtilizzato nella rappresentazione in XML dell'oggetto.
	 *
	 * @return <code>Document<code> un oggetto di tipo Document.
	 */
	@Override
	public Element toElement(Document document) {
		return toElement(document, _xmlSerializer);
	} // public Element toElement(Document document)

	private void notXMLObjectAttributesToElement(Document document, Element element, XMLSerializer serializer) {
		List notXMLObjectAttributes = getContainedNotXMLObjectAttributes();
		if (serializer == null) {
			serializer = new StringXMLSerializer();
		}
		for (int i = 0; i < notXMLObjectAttributes.size(); i++) {
			SourceBeanAttribute sourceBeanAttribute = (SourceBeanAttribute) (notXMLObjectAttributes.get(i));
			try {
				serializer.serialize(document, element,
						(sourceBeanAttribute.getQName() != null) ? sourceBeanAttribute.getQName() : sourceBeanAttribute.getKey(),
						sourceBeanAttribute.getValue());
				// serializer.serialize(document, element, sourceBeanAttribute.getKey(), sourceBeanAttribute.getValue());
			} // try
			catch (Exception ex) {
				element.setAttribute(sourceBeanAttribute.getKey(), "NOT_AVAILABLE");
				TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL,
						"SourceBean::notXMLObjectAttributesToElement: error during XML transformation", ex);
			} // catch (Exception ex)
		} // for (int i = 0; i < notXMLObjectAttributes.size(); i++)
	}

	private void XMLObjectAttributesToElement(Document document, Element element) {
		List xmlObjectAttributes = getContainedXMLObjectAttributes();
		for (int i = 0; i < xmlObjectAttributes.size(); i++) {
			SourceBeanAttribute sourceBeanAttribute = (SourceBeanAttribute) (xmlObjectAttributes.get(i));
			XMLObject xmlObject = (XMLObject) (sourceBeanAttribute.getValue());
			if (xmlObject instanceof SourceBean)
				element.appendChild(xmlObject.toElement(document));
			else {
				String key = (_upperCase) ? sourceBeanAttribute.getKey().toUpperCase() : sourceBeanAttribute.getKey();
				Element keyElement = document.createElement(key);
				keyElement.appendChild(xmlObject.toElement(document));
				element.appendChild(keyElement);
			} // if (xmlObject instanceof SourceBean)
		} // for (int i = 0; i < xmlObjectAttributes.size(); i++)
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString() Return the XML representation of this object.
	 */
	@Override
	public String toString() {
		return toXML(false);
	} // public String toString()

	public boolean getTrim() {
		return _trim;
	}

	public void setTrim(boolean trim) {
		_trim = trim;
	}

	/**
	 * Return the list of the mappings alias<->namespace uri contained in this element.
	 *
	 * @return List of NamespaceMapping objects
	 */
	public List getNamespaceMappings() {
		return _namespaceMappings;
	}

	/**
	 * Allow to set the list of the mappings alias<->namespace uri contained in this element.
	 *
	 * @param mappings
	 *            List of NamespaceMapping objects
	 */
	public void setNamespaceMappings(List mappings) {
		_namespaceMappings = mappings;
	}

	/**
	 * @return The alias name, if any, of this element's namespace.
	 */
	public String getPrefix() {
		return _prefix;
	}

	/**
	 * Allow to set the alias name of the root element.
	 *
	 * @param prefix
	 *            The alias name of this element's namespace.
	 */
	public void setPrefix(String prefix) {
		this._prefix = prefix;
	}

	public XMLSerializer getXMLSerializer() {
		return _xmlSerializer;
	}

	public void setXMLSerializer(XMLSerializer serializer) {
		_xmlSerializer = serializer;
	}

} // public class SourceBean extends AbstractXMLObject implements CloneableObject, Serializable
