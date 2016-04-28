/**

    Copyright 2004, 2007 Engineering Ingegneria Informatica S.p.A.

    This file is part of Spago.

    Spago is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    any later version.

    Spago is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Spago; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/
package it.eng.spago.base;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Definisce l'interfaccia che deve essere implementata da tutti gli oggetti il cui stato pu� essere pubblicato in XML.
 */
public interface XMLObject {
	/**
	 * Ritorna la rappresentazione XML dell'oggetto . La stringa XML generata contiene la sezione del doc type Entity.
	 */
	String toXML();

	/**
	 * Ritorna la rappresentazione XML dell'oggetto in formato stringa.
	 * 
	 * @param inlineEntity
	 *            boolean indica se la stringa XML generata deve contenere la sezione del doc type Entity.
	 * @return <code>String<code> la rappresentazione XML dell'oggetto.
	 */
	String toXML(boolean inlineEntity);

	/**
	 * Ritorna la rappresentazione XML dell'oggetto in formato stringa.
	 * 
	 * @return <code>String<code> la rappresentazione XML dell'oggetto.
	 */
	String toXML(int level);

	/**
	 * Ritorna un oggetto di tipo Document .
	 * 
	 * @return <code>Document<code> un oggetto di tipo Document.
	 */
	Document toDocument();

	/**
	 * Ritorna un oggetto di tipo Document .
	 * 
	 * @return <code>Document<code> un oggetto di tipo Document.
	 */
	Document toDocument(XMLSerializer serializer);

	/**
	 * Ritorna un oggetto di tipo Element che verr� utilizzato nella rappresentazione in XML dell'oggetto.
	 * 
	 * @return <code>Document<code> un oggetto di tipo Document.
	 */
	Element toElement(Document document);

	/**
	 * Ritorna un oggetto di tipo Element che verr� utilizzato nella rappresentazione in XML dell'oggetto.
	 * 
	 * @return <code>Document<code> un oggetto di tipo Document.
	 */
	Element toElement(Document document, XMLSerializer serializer);
} // public interface XMLObject
