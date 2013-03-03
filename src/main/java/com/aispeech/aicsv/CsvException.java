package com.aispeech.aicsv;

import java.util.List;

public class CsvException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1549901725606496472L;

	public CsvException(String msg) {
		super(msg);
	}

	public CsvException(Exception e) {
		super(e);
	}

	public static <T extends Exception> CsvException concat(List<T> exlist) {

		StringBuilder exmsg = new StringBuilder();
		for (Exception ex : exlist) {
			exmsg.append(ex.getMessage() + "\n");
		}

		return new CsvException(exmsg.toString());

	}

}
