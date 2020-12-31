package de.dl2sba.common;

/**
 * Filderwetter - framework for weather aquisistion and analysis
 * 
 * Copyright (C) 2003 Dietmar Krause, DL2SBA
 */
public class ProcessingException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 6546513216L;

	/**
     * Constructor for ProcessingException.
     */
    public ProcessingException() {
        super();
    }

    /**
     * Constructor for ProcessingException.
     * 
     * @param message
     */
    public ProcessingException(String message) {
        super(message);
    }

    /**
     * Constructor for ProcessingException.
     * 
     * @param message
     * @param cause
     */
    public ProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor for ProcessingException.
     * 
     * @param cause
     */
    public ProcessingException(Throwable cause) {
        super(cause);
    }
}