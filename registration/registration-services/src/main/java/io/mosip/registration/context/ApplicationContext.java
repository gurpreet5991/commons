package io.mosip.registration.context;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import io.mosip.registration.config.AppConfig;

public class ApplicationContext {

       private static ApplicationContext applicationContext;

       private ApplicationContext() {
   		applicationLanguageBundle = ResourceBundle.getBundle("labels",
				new Locale(AppConfig.getApplicationProperty("application_language")));
		localLanguageBundle = ResourceBundle.getBundle("labels",
				new Locale(AppConfig.getApplicationProperty("local_language")));
		applicationMessagesBundle = ResourceBundle.getBundle("messages",
				new Locale(AppConfig.getApplicationProperty("application_language")));
		localMessagesBundle = ResourceBundle.getBundle("messages",
				new Locale(AppConfig.getApplicationProperty("local_language")));
		applicationLanguagevalidationBundle = ResourceBundle.getBundle("validations",
				new Locale(AppConfig.getApplicationProperty("application_language")));
       }
       private ResourceBundle applicationLanguagevalidationBundle;
       public ResourceBundle getApplicationLanguagevalidationBundle() {
		return applicationLanguagevalidationBundle;
	}

	public void setApplicationLanguagevalidationBundle(ResourceBundle applicationLanguagevalidationBundle) {
		this.applicationLanguagevalidationBundle = applicationLanguagevalidationBundle;
	}
	private ResourceBundle applicationLanguageBundle;
       private ResourceBundle localLanguageBundle;
       private ResourceBundle applicationMessagesBundle;
       private ResourceBundle localMessagesBundle;
       private Map<String, Object> applicationMap;

       public static ApplicationContext getInstance() {
             if (applicationContext == null) {
                    applicationContext = new ApplicationContext();
                    return applicationContext;
             } else {
                    return applicationContext;
             }
       }

       /**
	 * @return the applicationContext
	 */
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
       * @return the applicationMap
       */
       public Map<String, Object> getApplicationMap() {
             return applicationMap;
       }

       /**
       * @param applicationMap
       *            the applicationMap to set
       */
       public void setApplicationMap(Map<String, Object> applicationMap) {
             this.applicationMap = applicationMap;
       }

       public ResourceBundle getApplicationLanguageBundle() {
             return applicationLanguageBundle;
       }

       public void setApplicationLanguageBundle() {
             applicationLanguageBundle = ResourceBundle.getBundle("labels",
                          new Locale(AppConfig.getApplicationProperty("application_language")));
       }

       public ResourceBundle getLocalLanguageProperty() {
             return localLanguageBundle;
       }

       public void setLocalLanguageProperty() {
             localLanguageBundle = ResourceBundle.getBundle("labels",
                          new Locale(AppConfig.getApplicationProperty("local_language")));
       }

       /**
       * @return the applicationMessagesBundle
       */
       public ResourceBundle getApplicationMessagesBundle() {
             return applicationMessagesBundle;
       }

       public void setApplicationMessagesBundle() {
             applicationMessagesBundle = ResourceBundle.getBundle("messages",
                          new Locale(AppConfig.getApplicationProperty("application_language")));
       }

       /**
       * @return the localMessagesBundle
       */
       public ResourceBundle getLocalMessagesBundle() {
             return localMessagesBundle;
       }

       public void setLocalMessagesBundle() {
             localMessagesBundle = ResourceBundle.getBundle("messages",
                          new Locale(AppConfig.getApplicationProperty("local_language")));
       }

}


