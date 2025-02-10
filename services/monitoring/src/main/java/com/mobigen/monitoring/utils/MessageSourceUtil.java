package com.mobigen.monitoring.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageSourceUtil {

    private final MessageSource messageSource;

    public MessageSourceUtil( MessageSource messageSource ) {
        this.messageSource = messageSource;
    }

    public String getMessage( String code ) {
        return getMessage( code, null );
    }

    public String getMessage( String code, Object[] args ) {
        return getMessage( code, args, "" );
    }

    public String getMessage( String code, Object[] args, String defaultMsg ) {
        //This is a convenient way to use and does not rely on request
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage( code, args, defaultMsg, locale );
    }
}