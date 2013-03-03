package com.aispeech.aicsv;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

abstract class CsvRW<T extends CsvBean> implements Closeable {

	private static String FULL_WIDTH_WHITESPACE = "　";
	protected static char DEFAULT_DELIMITER = ',';
	protected static Charset DEFAULT_CHARSET = Charset.forName("gbk");

	protected Class<T> recordClass;

	protected int line = 1;
	protected String path;

	public int getLine() {
		return line;
	}

	protected Map<String, Set<String>> uniqueValueMap = new HashMap<String, Set<String>>();

	protected CsvRW(Class<T> t) throws CsvException {

		this.recordClass = t;
		verifyRecordClass(t);

	}

	static Column getColumn(Field field) {
		return field.getAnnotation(Column.class);
	}

	static String getColumnName(Field field) {

		Column column = getColumn(field);
		if (column == null) {
			return null;
		}

		String name = column.name();
		if (name.equals("")) {
			name = field.getName();
		}

		return name;
	}

	private void verifyRecordClass(Class<T> t) throws CsvException {

		Set<String> columnNames = new HashSet<String>();
		Set<String> duplicateColumnNames = new HashSet<String>();

		Field[] fields = t.getDeclaredFields();
		for (Field field : fields) {

			Column column = getColumn(field);
			if (column != null) {

				String columnName = getColumnName(field);
				if (!columnNames.add(columnName)) {
					duplicateColumnNames.add(columnName);
				}

			}

		}

		if (columnNames.size() == 0) {
			throw new CsvException("There's no @Column in class[" + t.getName() + "]");
		}

		if (duplicateColumnNames.size() != 0) {
			throw new CsvException("Duplicate column name " + duplicateColumnNames + " in class[" + t.getName() + "]");
		}

	}

	/**
	 * normalize value
	 * 
	 * @param value
	 * @return
	 */
	protected String normalizeValue(Object value, Field field) {

		String strValue = "";

		if (value != null) {

			strValue = String.valueOf(value);
		}

		Column column = getColumn(field);
		if (column.trim()) {

			strValue = strValue.replaceAll("^[" + FULL_WIDTH_WHITESPACE + "\\s]+", "");
			strValue = strValue.replaceAll("[" + FULL_WIDTH_WHITESPACE + "\\s]+$", "");

		}

		/*
		 * trim disgusting '\t' '\r', this is a bug-fix, the bug was found by xiaomin.zhang
		 */
		strValue = strValue.replaceAll("[ \t\r]$", "");

		return strValue;

	}

	/**
	 * verify value
	 * 
	 * @param value
	 * @param name
	 * @param column
	 * @throws CsvException
	 */
	protected void checkValue(String value, Field field) throws CsvException {

		verifyNullable(value, field);
		verifyMatches(value, field);
		verifyUnique(value, field);

	}

	/**
	 * 
	 * verify whether this value is unique(except blank value)
	 * 
	 * @param value
	 * @param field
	 * @throws CsvException
	 */
	protected void verifyUnique(String value, Field field) throws CsvException {

		Column column = getColumn(field);

		if (column.unique()) {

			String columnName = getColumnName(field);
			Set<String> valueSet = uniqueValueMap.get(columnName);
			if (valueSet == null) {
				valueSet = new HashSet<String>();
				uniqueValueMap.put(columnName, valueSet);
			}

			/*
			 * blank value is special
			 */
			if (!isBlank(value) && !valueSet.add(value)) {
				throw new CsvException(getAbsolutePosition() + ": duplicate [" + value + "] for column[" + columnName
						+ "]");
			}

		}

	}

	protected boolean isBlank(String str) {
		return str == null || str.matches("[" + FULL_WIDTH_WHITESPACE + "\\s]*");
	}

	/**
	 * verify whether this value matches the given regular expression
	 * 
	 * @param value
	 * @param column
	 * @throws CsvException
	 */
	protected void verifyMatches(String value, Field field) throws CsvException {

		Column column = getColumn(field);

		String regex = column.matches();
		if (!regex.equals(".*")) {
			if (!value.matches(regex)) {
				throw new CsvException(getAbsolutePosition() + ": column[" + getColumnName(field) + "]'s value["
						+ value + "] should match regex [" + regex + "]");
			}
		}

	}

	/**
	 * verify whether this value is blank
	 * 
	 * @param value
	 * @param column
	 * @throws CsvException
	 */
	protected void verifyNullable(String value, Field field) throws CsvException {

		Column column = getColumn(field);

		if (!column.nullable()) {
			if (isBlank(value)) {
				throw new CsvException(getAbsolutePosition() + ":" + " column[" + getColumnName(field)
						+ "] should not be blank!");
			}
		}

	}

	static <T extends CsvBean> Object getFieldValue(T t, Field field) throws CsvException {

		Object value;

		try {
			field.setAccessible(true);
			value = field.get(t);
		} catch (IllegalArgumentException e) {
			throw new CsvException(e);
		} catch (IllegalAccessException e) {
			throw new CsvException(e);
		}

		return value;

	}

	protected void setFieldValue(T t, Field field, Object value) throws CsvException {

		try {

			field.setAccessible(true);
			field.set(t, value);

		} catch (IllegalArgumentException e) {
			throw new CsvException(e);
		} catch (IllegalAccessException e) {
			throw new CsvException(e);
		}

	}

	@Override
	protected void finalize() throws Throwable {
		this.close();
	}

	protected String getAbsolutePosition() {
		return path + "#" + line;
	}

}