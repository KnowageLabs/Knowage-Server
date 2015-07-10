/*
CKANClient-J - Data Catalogue Software client in Java
Copyright (C) 2012 Open Knowledge Foundation

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package it.eng.spagobi.tools.dataset.ckan.exception;

import java.util.ArrayList;

/**
 * Represents an error in talking to CKAN, in most cases this will
 * be as a result of False being returned in the JSON response success
 * field.
 *
 * @author      Ross Jones <ross.jones@okfn.org>
 * @version     1.7
 * @since       2012-05-01
 */
public class CKANException extends Exception {

    private ArrayList<String> messages = new ArrayList<String>();

    public CKANException( String message ) {
        messages.add( message );
    }

    public void addError( String error ) {
        messages.add( error );
    }

    public ArrayList<String> getErrorMessages() {
        return messages;
    }

    public String toString() {
        return messages.toString();
    }
}