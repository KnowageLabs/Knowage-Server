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
package it.eng.spago.validation;

import it.eng.spago.base.CloneableObject;
import it.eng.spago.error.EMFErrorCategory;
import it.eng.spago.error.EMFUserError;

import java.util.List;

/**
 * Represent an error related to a field.
 */
public class EMFFieldUserError extends EMFUserError {

    private static final long serialVersionUID = 1L;
    
    // The name of the field that has originated the error
	private String fieldName = null;
    
    /**
     * Costruisce un oggetto di tipo <code>EMFValidationError</code> identificandolo  tramite  una severity e un
     * codice di errore .
     * @param severity severity dell'errore.
     * @param fieldName il nome del field che ha generato l'errore
     * @param code codice di errore.
     */
    public EMFFieldUserError(String severity, String fieldName, String code, String bundle) {
        super(severity, code, null, null, bundle);
        this.fieldName = fieldName;
    } // public EMFUserError(String severity, int code)
    
    /**
     * Costruisce un oggetto di tipo <code>EMFValidationError</code> identificandolo  tramite  una severity e un
     * codice di errore .
     * @param severity severity dell'errore.
     * @param fieldName il nome del field che ha generato l'errore
     * @param code codice di errore.
     */
    public EMFFieldUserError(String severity, String fieldName, String code) {
        this(severity, fieldName, code, (String)null);
    } // public EMFUserError(String severity, int code)

    /**
     * Costruisce un oggetto di tipo <code>EMFValidationError</code> identificandolo  tramite  una severity ,un
     * codice di errore e una collezione di parametri che andranno a sostituire i caratteri <em>%</em> nella
     * stringa di descrizione.
     * @param severity severity dell'errore.
     * @param code codice di errore.
     * @parma fieldName il nome del field che ha generato l'errore
     * @param params vettore di parametri che  verranno inseriti nella stringa di descrizione.
     */
    public EMFFieldUserError(String severity, String fieldName, String code, List params, String bundle) {
        super(severity, code, params, null, bundle);
        this.fieldName = fieldName;
    } // public EMFUserError(String severity, int code, List params)

    /**
     * Costruisce un oggetto di tipo <code>EMFValidationError</code> identificandolo  tramite  una severity ,un
     * codice di errore e una collezione di parametri che andranno a sostituire i caratteri <em>%</em> nella
     * stringa di descrizione.
     * @param severity severity dell'errore.
     * @param code codice di errore.
     * @parma fieldName il nome del field che ha generato l'errore
     * @param params vettore di parametri che  verranno inseriti nella stringa di descrizione.
     */
    public EMFFieldUserError(String severity, String fieldName, String code, List params) {
        this(severity, fieldName, code, params, (String)null);
    } // public EMFUserError(String severity, int code, List params)
    
    /**
     * Costruisce un oggetto di tipo <code>EMFUserError</code> identificandolo  tramite  una severity ,un
     * codice di errore , una collezione di parametri che andranno a sostituire i caratteri <em>%</em> nella
     * stringa di descrizione e un oggetto di qualsiasi natura.
     * @param severity severity dell'errore.
     * @param code codice di errore.
     * @param params vettore di parametri che  verranno inseriti nella stringa di descrizione.
     * @param additionalInfo oggetto di qualsiasi natura.
     */
    public EMFFieldUserError(String severity, String fieldName, String code, List params, Object additionalInfo, String bundle) {
        super(severity, code, params, additionalInfo, bundle);
        this.fieldName = fieldName;
    } // public EMFUserError(String severity, int code, List params, Object additionalInfo)

    /**
     * Costruisce un oggetto di tipo <code>EMFUserError</code> identificandolo  tramite  una severity ,un
     * codice di errore , una collezione di parametri che andranno a sostituire i caratteri <em>%</em> nella
     * stringa di descrizione e un oggetto di qualsiasi natura.
     * @param severity severity dell'errore.
     * @param code codice di errore.
     * @param params vettore di parametri che  verranno inseriti nella stringa di descrizione.
     * @param additionalInfo oggetto di qualsiasi natura.
     */
    public EMFFieldUserError(String severity, String fieldName, String code, List params, Object additionalInfo) {
        this(severity, fieldName, code, params, additionalInfo, null);
    } // public EMFUserError(String severity, int code, List params, Object additionalInfo)

    /**
     * Costruisce un oggetto di tipo <code>EMFValidationError</code> identificandolo  tramite  una severity e un
     * codice di errore .
     * @deprecated Use constructor with String error code identifier instead.
     * @param severity severity dell'errore.
     * @param fieldName il nome del field che ha generato l'errore
     * @param code codice di errore.
     */
    public EMFFieldUserError(String severity, String fieldName, int code) {
        this(severity, fieldName, String.valueOf(code));
    } // public EMFUserError(String severity, int code)

	/**
     * Costruisce un oggetto di tipo <code>EMFValidationError</code> identificandolo  tramite  una severity e un
     * codice di errore .
     * @deprecated Use constructor with String error code identifier instead.
     * @param severity severity dell'errore.
     * @param code codice di errore.
     */
    public EMFFieldUserError(String severity, int code) {
        this(severity, (String)null, String.valueOf(code));
    } // public EMFUserError(String severity, int code)

    /**
     * Costruisce un oggetto di tipo <code>EMFValidationError</code> identificandolo  tramite  una severity ,un
     * codice di errore e una collezione di parametri che andranno a sostituire i caratteri <em>%</em> nella
     * stringa di descrizione.
     * @deprecated Use constructor with String error code identifier instead.
     * @param severity severity dell'errore.
     * @param code codice di errore.
     * @parma fieldName il nome del field che ha generato l'errore
     * @param params vettore di parametri che  verranno inseriti nella stringa di descrizione.
     */
    public EMFFieldUserError(String severity, String fieldName, int code, List params) {
        this(severity, fieldName, String.valueOf(code), params);
    } // public EMFUserError(String severity, int code, List params)

    
    /**
     * Costruisce un oggetto di tipo <code>EMFValidationError</code> identificandolo  tramite  una severity ,un
     * codice di errore e una collezione di parametri che andranno a sostituire i caratteri <em>%</em> nella
     * stringa di descrizione.
     * @deprecated Use constructor with String error code identifier instead.
     * @param severity severity dell'errore.
     * @param code codice di errore.
     * @param params vettore di parametri che  verranno inseriti nella stringa di descrizione.
     */
    public EMFFieldUserError(String severity, int code, List params) {
        this(severity, (String)null, String.valueOf(code), params);
    } // public EMFUserError(String severity, int code, List params)
    
    /**
     * Costruisce un oggetto di tipo <code>EMFUserError</code> identificandolo  tramite  una severity ,un
     * codice di errore , una collezione di parametri che andranno a sostituire i caratteri <em>%</em> nella
     * stringa di descrizione e un oggetto di qualsiasi natura.
     * @deprecated Use constructor with String error code identifier instead.
     * @param severity severity dell'errore.
     * @param code codice di errore.
     * @param params vettore di parametri che  verranno inseriti nella stringa di descrizione.
     * @param additionalInfo oggetto di qualsiasi natura.
     */
    public EMFFieldUserError(String severity, int code, List params, Object additionalInfo) {
        super(severity, String.valueOf(code), params, additionalInfo);
    } // public EMFUserError(String severity, int code, List params, Object additionalInfo)

    /**
     * Costruisce un oggetto di tipo <code>EMFUserError</code> utilizzando lo stato del parametro
     * in input .
     * @param EMFUserError oggetto della stessa classe.
     */
    public EMFFieldUserError(EMFFieldUserError valError) {
        super(valError);
        this.fieldName = valError.getFieldName();
    } // public EMFUserError(EMFUserError userError)

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

    /* (non-Javadoc)
     * @see it.eng.spago.error.EMFAbstractError#getCategory()
     */
    public String getCategory() {
        return EMFErrorCategory.USER_ERROR;
    }

    /**
     * Ritorna un clone dell'oggetto stesso.
     * @return CloneableObject  il clone dell'oggetto.
     * @see CloneableObject
     */
    public CloneableObject cloneObject() {
        return new EMFFieldUserError(this);
    } // public CloneableObject cloneObject()

}
