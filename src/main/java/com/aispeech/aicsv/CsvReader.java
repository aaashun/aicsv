package com.aispeech.aicsv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * read record as java bean
 * 
 * @author shun.zhang@aispeech.com
 * 
 * @param <T>
 */
public class CsvReader<T extends CsvBean> extends CsvRW<T> {

	private com.csvreader.CsvReader reader = null;

	/**
	 * 
	 * @param csv
	 * @param t
	 * @throws IOException
	 * @throws CsvException
	 */
	public CsvReader(File csv, Class<T> t) throws IOException, CsvException {
		this(new FileInputStream(csv), csv.getCanonicalPath(), DEFAULT_DELIMITER, DEFAULT_CHARSET, t);
	}

	/**
	 * 
	 * @param inputStream
	 * @param path
	 *            represent the stream source, if it's a file, this parameter shoud be the file path, otherwise it can be
	 *            null
	 * @param delimiter
	 * @param charset
	 * @param t
	 * @throws IOException
	 * @throws CsvException
	 */
	public CsvReader(InputStream inputStream, String path, char delimiter, Charset charset, Class<T> t)
			throws IOException, CsvException {

		super(t);
		this.path = path;
		this.recordClass = t;
		reader = new com.csvreader.CsvReader(inputStream, delimiter, charset);
		reader.setTrimWhitespace(false);

		if (!reader.readHeaders()) {
			throw new CsvException(path + ": this is an empty csv");
		}

		verifyHeaders(reader.getHeaders());

	}

	/**
	 * verify headers by annotation[Column]
	 * 
	 * @param headers
	 * @throws IOException
	 * @throws IOException
	 * @throws DataException
	 */
	protected void verifyHeaders(String[] headers) throws CsvException {

		List<String> actualHeaders = Arrays.asList(headers);
		List<String> missingHeaders = new ArrayList<String>();
		Set<String> expectedHeaders = new HashSet<String>();
		Set<String> duplicateHeaders = new HashSet<String>();

		/*
		 * verify whether the csv contains expected headers
		 */
		Field[] fields = recordClass.getDeclaredFields();
		for (Field field : fields) {

			Column column = getColumn(field);
			if (column != null) {

				String columnName = getColumnName(field);
				expectedHeaders.add(columnName);

				if (!actualHeaders.contains(columnName)) {
					missingHeaders.add(columnName);
				}

			}

		}

		/*
		 * verify whether the csv contains duplicate header
		 */
		Set<String> headerSet = new HashSet<String>();
		for (String header : headers) {
			if (!isBlank(header) && !headerSet.add(header)) {
				duplicateHeaders.add(header);
			}
		}

		if (missingHeaders.size() != 0 || duplicateHeaders.size() != 0) {

			String msg = this.path + ": \n";

			if (missingHeaders.size() != 0) {
				msg += "missing header" + missingHeaders + "\n";
			}

			if (duplicateHeaders.size() != 0) {
				msg += "duplicate header" + duplicateHeaders + "\n";
			}

			msg += "expected" + expectedHeaders + "\n";
			msg += "actual" + actualHeaders;

			this.close();
			throw new CsvException(msg);

		}

	}

	private T getNewInstance() throws CsvException {

		T t;

		try {
			t = recordClass.newInstance();
		} catch (IllegalAccessException e) {
			throw new CsvException(e);
		} catch (InstantiationException e) {
			throw new CsvException(e);
		}

		return t;
	}

	/**
	 * read a record line and put them into object's fields
	 * 
	 * @return Whether another record was successfully read or not
	 * @throws IOException
	 * @throws CsvException
	 */
	public T readRecord() throws IOException, CsvException {

		if (reader == null) {
			return null;
		}

		if (!reader.readRecord()) {
			this.close();
			return null;
		}

		T t = getNewInstance();

		t.setLine(++this.line);

		Field[] fields = recordClass.getDeclaredFields();

		for (Field field : fields) {

			Column column = getColumn(field);
			if (column != null) {

				String name = getColumnName(field);
				String value = reader.get(name);
				value = normalizeValue(value, field);

				checkValue(value, field);

				setFieldValue(t, field, value);

			}

		}

		return t;
	}

	@Override
	public void close() {
		if (reader != null) {
			reader.close();
			reader = null;
		}
	}

	/**
	 * read csv, get all records
	 * 
	 * @param <T>
	 * @param csvFile
	 * @param t
	 * @return
	 * @throws IOException
	 * @throws CsvException
	 */
	public static <T extends CsvBean> List<T> readRecords(File csvFile, Class<T> t) throws IOException, CsvException {

		List<T> recordList = new ArrayList<T>();
		List<CsvException> exlist = new ArrayList<CsvException>();

		CsvReader<T> reader = new CsvReader<T>(csvFile, t);
		while (true) {
			try {

				T record = reader.readRecord();
				if (record == null) {
					break;
				}

				recordList.add(record);

			} catch (CsvException e) {
				exlist.add(e);
			}
		}
		reader.close();

		if (exlist.size() != 0) {
			throw CsvException.concat(exlist);
		}

		return recordList;

	}

	/**
	 * read csv, get the first record
	 * 
	 * @param <T>
	 * @param csvFile
	 * @param t
	 * @return
	 * @throws IOException
	 * @throws CsvException
	 */
	public static <T extends CsvBean> T readFirstRecord(File csvFile, Class<T> t) throws IOException, CsvException {

		CsvReader<T> reader = new CsvReader<T>(csvFile, t);
		T record = reader.readRecord();
		reader.close();

		return record;

	}

}