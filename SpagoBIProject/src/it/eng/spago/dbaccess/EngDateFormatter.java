/* SpagoBI, the Open Source Business Intelligence suite

* © 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this file,
* You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spago.dbaccess;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.StringTokenizer;

public class EngDateFormatter {
    private int _dayPositionInMessageFormat = -1;
    private int _monthPositionInMessageFormat = -1;
    private int _yearPositionInMessageFormat = -1;
    private int _hourPositionInMessageFormat = -1;
    private int _minutesPositionInMessageFormat = -1;
    private int _secondsPositionInMessageFormat = -1;
    private MessageFormat _mf = null;
    private int _mfNumberOfParameters = -1;
    private Object _parsedObjects[] = null;
    private int _dayNumberOfDigit = -1;
    private int _monthNumberOfDigit = -1;
    private int _yearNumberOfDigit = -1;
    private int _hourNumberOfDigit = -1;
    private int _minutesNumberOfDigit = -1;
    private int _secondsNumberOfDigit = -1;

    public EngDateFormatter(String format) throws ParseException {
        StringBuffer messageFormatStringBuffer = new StringBuffer();
        String delimitiers = new String("-:/. ");
        StringTokenizer st = new StringTokenizer(format, delimitiers, true);
        String token = null;
        int position = 0;
        while (st.hasMoreTokens()) {
            token = st.nextToken();
            if (delimitiers.indexOf(token) > -1) {
                // The token is a delimitiers so i append
                messageFormatStringBuffer.append(token);
            }
            else if (token.startsWith("d")) {
                //DAY IDENTIFIER
                _dayPositionInMessageFormat = position;
                _dayNumberOfDigit = token.length();
                messageFormatStringBuffer.append("{" + position + "}");
                position++;
            }
            else if (token.startsWith("M")) {
                //MONTHS
                _monthPositionInMessageFormat = position;
                _monthNumberOfDigit = token.length();
                messageFormatStringBuffer.append("{" + position + "}");
                position++;
            }
            else if (token.startsWith("y")) {
                //YEAR
                _yearPositionInMessageFormat = position;
                _yearNumberOfDigit = token.length();
                messageFormatStringBuffer.append("{" + position + "}");
                position++;
            }
            else if (token.startsWith("h") || token.startsWith("H")) {
                //HOUR
                _hourPositionInMessageFormat = position;
                _hourNumberOfDigit = token.length();
                messageFormatStringBuffer.append("{" + position + "}");
                position++;
            }
            else if (token.startsWith("m")) {
                //MINUTES
                _minutesPositionInMessageFormat = position;
                _minutesNumberOfDigit = token.length();
                messageFormatStringBuffer.append("{" + position + "}");
                position++;
            }
            else if (token.startsWith("s")) {
                //SECONDS
                _secondsPositionInMessageFormat = position;
                _secondsNumberOfDigit = token.length();
                messageFormatStringBuffer.append("{" + position + "}");
                position++;
            }
        } //end while
        _mf = new MessageFormat(messageFormatStringBuffer.toString());
        _mfNumberOfParameters = position;
    } //public EngDateFormatter(String format) throws ParseException

    public void format(String value) throws ParseException {
        if (_mf != null)
            _parsedObjects = _mf.parse(value);
        else
            throw new ParseException("No Message Format Defined ", 0);
    } //public EngDateFormatter(String value) throws ParseException

    public MessageFormat getMessageFormat() {
        return _mf;
    }

    public int getMessageFormatNumberOfParameters() {
        return _mfNumberOfParameters;
    }

    public int getDay() {
        if (_dayPositionInMessageFormat > -1)
            return Integer.parseInt((String)_parsedObjects[_dayPositionInMessageFormat]);
        else
            return _dayPositionInMessageFormat;
    }

    public int getMonth() {
        if (_monthPositionInMessageFormat > -1)
            return Integer.parseInt((String)_parsedObjects[_monthPositionInMessageFormat]);
        else
            return _monthPositionInMessageFormat;
    }

    public int getYear() {
        if (_yearPositionInMessageFormat > -1)
            return Integer.parseInt((String)_parsedObjects[_yearPositionInMessageFormat]);
        else
            return _yearPositionInMessageFormat;
    }

    public int getHours() {
        if (_hourPositionInMessageFormat > -1)
            return Integer.parseInt((String)_parsedObjects[_hourPositionInMessageFormat]);
        else
            return _hourPositionInMessageFormat;
    }

    public int getMinutes() {
        if (_minutesPositionInMessageFormat > -1)
            return Integer.parseInt((String)_parsedObjects[_minutesPositionInMessageFormat]);
        else
            return _minutesPositionInMessageFormat;
    }

    public int getSeconds() {
        if (_secondsPositionInMessageFormat > -1)
            return Integer.parseInt((String)_parsedObjects[_secondsPositionInMessageFormat]);
        else
            return _secondsPositionInMessageFormat;
    }

    public int get_dayPositionInMessageFormat() {
        return _dayPositionInMessageFormat;
    }

    public void set_dayPositionInMessageFormat(int new_dayPositionInMessageFormat) {
        _dayPositionInMessageFormat = new_dayPositionInMessageFormat;
    }

    public int get_hourPositionInMessageFormat() {
        return _hourPositionInMessageFormat;
    }

    public void set_hourPositionInMessageFormat(int new_hourPositionInMessageFormat) {
        _hourPositionInMessageFormat = new_hourPositionInMessageFormat;
    }

    public int get_minutesPositionInMessageFormat() {
        return _minutesPositionInMessageFormat;
    }

    public void set_minutesPositionInMessageFormat(int new_minutesPositionInMessageFormat) {
        _minutesPositionInMessageFormat = new_minutesPositionInMessageFormat;
    }

    public int get_monthPositionInMessageFormat() {
        return _monthPositionInMessageFormat;
    }

    public void set_monthPositionInMessageFormat(int new_monthPositionInMessageFormat) {
        _monthPositionInMessageFormat = new_monthPositionInMessageFormat;
    }

    public int get_secondsPositionInMessageFormat() {
        return _secondsPositionInMessageFormat;
    }

    public void set_secondsPositionInMessageFormat(int new_secondsPositionInMessageFormat) {
        _secondsPositionInMessageFormat = new_secondsPositionInMessageFormat;
    }

    public int get_yearPositionInMessageFormat() {
        return _yearPositionInMessageFormat;
    }

    public void set_yearPositionInMessageFormat(int new_yearPositionInMessageFormat) {
        _yearPositionInMessageFormat = new_yearPositionInMessageFormat;
    }

    public int get_dayNumberOfDigit() {
        return _dayNumberOfDigit;
    }

    public int get_hourNumberOfDigit() {
        return _hourNumberOfDigit;
    }

    public int get_minutesNumberOfDigit() {
        return _minutesNumberOfDigit;
    }

    public int get_monthNumberOfDigit() {
        return _monthNumberOfDigit;
    }

    public int get_secondsNumberOfDigit() {
        return _secondsNumberOfDigit;
    }

    public int get_yearNumberOfDigit() {
        return _yearNumberOfDigit;
    }
} //end class engDateFormatter
