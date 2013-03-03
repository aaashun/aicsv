package com.aispeech.aicsv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * csv writer, you can write a java bean into csv file, the java bean will be mapping to record automatically
 * 
 * @author shun.zhang@aispeech.com
 * 
 * @param <T>
 */
public class CsvWriter<T extends CsvBean> extends CsvRW<T> {

	private com.csvreader.CsvWriter writer = null;

	/**
	 * create a new csv writer<br/>
	 * default charset "gbk"<br/>
	 * default delimiter ','<br/>
	 * do not trim whitespace
	 * 
	 * @param csv
	 * @param t
	 * @throws IOException
	 * @throws CsvException
	 */
	public CsvWriter(File csv, Class<T> t) throws IOException, CsvException {
		this(new FileOutputStream(csv), DEFAULT_DELIMITER, DEFAULT_CHARSET, t);
		this.path = csv.getCanonicalPath();
	}

	/**
	 * create a new csv writer
	 * 
	 * @param outputStream
	 * @param delimiter
	 * @param charset
	 * @param trimWhitespace
	 * @param t
	 * @throws IOException
	 * @throws CsvException
	 */
	public CsvWriter(OutputStream outputStream, char delimiter, Charset charset, Class<T> t) throws IOException,
			CsvException {

		super(t);

		recordClass = t;

		writer = new com.csvreader.CsvWriter(outputStream, delimiter, charset);

		writeHeaders();

	}

	/**
	 * write headers to a new csv
	 * 
	 * @throws IOException
	 * @throws CsvException
	 */
	private void writeHeaders() throws IOException, CsvException {

		Field[] fields = recordClass.getDeclaredFields();

		List<String> headers = new ArrayList<String>();

		for (Field field : fields) {

			Column column = getColumn(field);

			if (column != null) {
				headers.add(getColumnName(field));
			}

		}

		writeRecord(headers.toArray(new String[0]));
	}

	private void writeRecord(String[] record) throws IOException, CsvException {

		if (writer == null) {
			throw new CsvException("writer was already closed.");
		}

		writer.writeRecord(record, true);

	}

	/**
	 * write a record
	 * 
	 * @param record
	 * @throws CsvException
	 * @throws IOException
	 */
	public void writeRecord(T record) throws CsvException, IOException {

		record.setLine(++line);

		List<String> recordArr = new ArrayList<String>();
		Field[] fields = recordClass.getDeclaredFields();

		for (Field field : fields) {

			Column column = getColumn(field);
			if (column != null) {

				String value = normalizeValue(getFieldValue(record, field), field);

				checkValue(value, field);

				recordArr.add(value.toString());

			}

		}

		writeRecord(recordArr.toArray(new String[0]));

	}

	/**
	 * close this writer
	 */
	public void close() {
		if (writer != null) {
			writer.close();
			writer = null;
		}
	}

	/**
	 * write records into csv file
	 * 
	 * @param <T>
	 * @param csvFile
	 * @param t
	 * @param recordList
	 * @throws IOException
	 * @throws CsvException
	 */
	public static <T extends CsvBean> void writeRecords(File csvFile, Class<T> t, List<T> recordList)
			throws IOException, CsvException {

		CsvWriter<T> writer = new CsvWriter<T>(csvFile, t);

		for (T record : recordList) {
			writer.writeRecord(record);
		}

		writer.close();

	}

	/**
	 * write records into csv file
	 * 
	 * @param <T>
	 * @param csvFile
	 * @param t
	 * @param records
	 * @throws IOException
	 * @throws CsvException
	 */
	public static <T extends CsvBean> void writeRecords(File csvFile, Class<T> t, T... records) throws IOException,
			CsvException {
		writeRecords(csvFile, t, Arrays.asList(records));
	}
}