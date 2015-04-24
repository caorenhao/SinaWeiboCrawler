package com.caorenhao.vcrawler.vextractor;

public class CssTemplateException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3395283806708952285L;
	
    public CssTemplateException() {
    	super();
    }
    
    public CssTemplateException(String message) {
    	super(message);
    }

  
    public CssTemplateException(String message, Throwable cause) {
        super(message, cause);
    }

    public CssTemplateException(Throwable cause) {
        super(cause);
    }
}
