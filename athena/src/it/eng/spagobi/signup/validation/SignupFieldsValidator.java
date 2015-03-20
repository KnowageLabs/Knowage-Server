package it.eng.spagobi.signup.validation;

import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.rest.validation.IFieldsValidator;
import it.eng.spagobi.utilities.json.JSONUtils;

import java.net.URLDecoder;
import java.util.Locale;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SignupFieldsValidator implements IFieldsValidator {

	private static transient Logger logger = Logger.getLogger(SignupFieldsValidator.class);
	private static final String regex_password = "[^\\d][a-zA-Z0-9]{7,15}";
	private static final String regex_email = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}";
	private static final String regex_date = "(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/(19|20)\\d\\d";
	private static final String defaultPassword = "Password";
	private static final String defaultPasswordConfirm = "Confirm Password";
	
	private boolean validatePassword( String password, String username ){
		
	  if( username != null && password.indexOf(username) != -1 ) return false;
	  return password.matches(regex_password);
	}
	private boolean validateEmail( String email ){
		
	  return email.matches(regex_email);
	}
	private boolean validateDate( String date ){
		
      return date.matches(regex_date);
	}
	public JSONArray validateFields(MultivaluedMap<String, String> parameters) {
		IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
		
        JSONArray validationErrors = new JSONArray();
        
        String strLocale = GeneralUtilities.trim(parameters.getFirst("locale"));
        Locale locale = new Locale(strLocale.substring(0, strLocale.indexOf("_")), strLocale.substring(strLocale.indexOf("_")+1));
        
        String nome     = GeneralUtilities.trim(parameters.getFirst("nome"));
        String cognome  = GeneralUtilities.trim(parameters.getFirst("cognome"));
        String username = GeneralUtilities.trim(parameters.getFirst("username"));
        String password = GeneralUtilities.trim(parameters.getFirst("password"));
        String confermaPassword 
                        = GeneralUtilities.trim(parameters.getFirst("confermaPassword"));
        String email    = GeneralUtilities.trim(parameters.getFirst("email"));
        String dataNascita  
                        = GeneralUtilities.trim(parameters.getFirst("dataNascita"));
        String strUseCaptcha = (parameters.getFirst("useCaptcha")==null)?"true":parameters.getFirst("useCaptcha");
        boolean useCaptcha = Boolean.valueOf(strUseCaptcha);
        String captcha  = GeneralUtilities.trim(parameters.getFirst("captcha"));
        String termini  = parameters.getFirst("termini");
        String modify  = GeneralUtilities.trim(parameters.getFirst("modify"));
        
        try{ 
          if( nome != null )             nome = URLDecoder.decode(nome, "ISO-8859-1");
          if( cognome != null )          cognome = URLDecoder.decode(cognome, "ISO-8859-1");
          if( username != null )         username = URLDecoder.decode(username, "ISO-8859-1");
          if( password != null )         password = URLDecoder.decode(password, "ISO-8859-1");
          if( confermaPassword != null ) confermaPassword = URLDecoder.decode(confermaPassword, "ISO-8859-1");
          if( email != null )            email = URLDecoder.decode(email, "ISO-8859-1");
          if( dataNascita != null )      dataNascita = URLDecoder.decode(dataNascita, "ISO-8859-1");
        	
        }
        catch( Exception ex ){ logger.error(ex.getMessage());  throw new RuntimeException( ex ); }
        
        try{
          
          if( email == null ) 
        	 // validationErrors.put( new JSONObject("{message: 'Field Email mandatory'}") );
          	 validationErrors.put( new JSONObject("{message: \""+msgBuilder.getMessage("signup.check.emailMandatory",locale)+"\"}") );
          else{
        	  if( !validateEmail( email )) 
//                validationErrors.put( new JSONObject("{message: 'Field Email invalid syntax'}") );
        		  validationErrors.put( new JSONObject("{message: \""+msgBuilder.getMessage("signup.check.emailInvalid",locale)+"\"}") );
          }
          if( dataNascita != null )
            if( !validateDate(dataNascita) )
//        	  validationErrors.put( new JSONObject("{message: 'Field Birthday invalid syntax'}") );
          	  validationErrors.put( new JSONObject("{message: \""+msgBuilder.getMessage("signup.check.birthdayInvalid",locale)+"\"}") );
        	  
          if( nome == null) 
//        	  validationErrors.put( new JSONObject("{message: 'Field Name mandatory'}") );
        	  validationErrors.put( new JSONObject("{message: \""+msgBuilder.getMessage("signup.check.nameMandatory",locale)+"\"}") );
          if( cognome == null ) 
//        	  validationErrors.put( new JSONObject("{message: 'Field Surname mandatory'}") );
          	  validationErrors.put( new JSONObject("{message: \""+msgBuilder.getMessage("signup.check.surnameMandatory",locale)+"\"}") );
            
          if( modify == null ){	  
            if( password == null ) 
//              	  validationErrors.put( new JSONObject("{message: 'Field Password mandatory'}") );
            	  validationErrors.put( new JSONObject("{message: \""+msgBuilder.getMessage("signup.check.pwdMandatory",locale)+"\"}") );
            else{
              	 if( !validatePassword(password, username )) {
//              		 String errorMsg = "Field Password invalid syntax. \n " +
//							  		 	" Correct syntax: \n "+
//										" 	- minimum 8 chars \n "+
//							  		 	"	- not start with number \n "+
//										"	- not contain the usename ";
              		 String errorMsg = msgBuilder.getMessage("signup.check.pwdInvalid",locale);
              		 validationErrors.put( new JSONObject("{message: '"+  JSONUtils.escapeJsonString(errorMsg) +"'}") );
              	 }
            }	  
            
            if( username == null ) 
//        	  validationErrors.put( new JSONObject("{message: 'Field Username mandatory'}") );
              validationErrors.put( new JSONObject("{message: \""+msgBuilder.getMessage("signup.check.usernameMandatory",locale)+"\"}") );
          
            if( confermaPassword == null ) 
//        	  validationErrors.put( new JSONObject("{message: 'Field Confirm Password mandatory'}") );
              validationErrors.put( new JSONObject("{message: \""+msgBuilder.getMessage("signup.check.confirmPwdMandatory",locale)+"\"}") );
          
            if( useCaptcha && !Boolean.valueOf(termini) ) 
//        	  validationErrors.put( new JSONObject("{message: 'Agree with the terms of service mandatory'}") );
             validationErrors.put( new JSONObject("{message: \""+msgBuilder.getMessage("signup.check.agreeMandatory",locale)+"\"}") );
          
        	  
            if( password != null && !password.equals(defaultPassword) && 
            		confermaPassword != null && !confermaPassword.equals(defaultPasswordConfirm))
        	 if( !password.equals(confermaPassword)) 
//        	   validationErrors.put( new JSONObject("{message: 'Field Password and Confirm Password not equal'}") );
               validationErrors.put( new JSONObject("{message: \""+msgBuilder.getMessage("signup.check.pwdNotEqual",locale)+"\"}") );
            if( useCaptcha && captcha == null ) 
//        	  validationErrors.put( new JSONObject("{message: 'Field Captcha mandatory'}") );
              validationErrors.put( new JSONObject("{message: \""+msgBuilder.getMessage("signup.check.captchaMandatory",locale)+"\"}") );
          }
		} catch (JSONException e1) {
		  logger.error(e1.getMessage());
		  throw new RuntimeException( e1 );
		}
		
        return validationErrors;
		
	}
}
