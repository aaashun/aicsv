package com.aispeech.aicsv;

import java.lang.reflect.Field;

abstract public class CsvBean {

	private Integer line = 1;

	@Override
	public String toString() {

		StringBuffer sb = new StringBuffer("line:" + line + " ");

		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {

			Column column = field.getAnnotation(Column.class);
			if (column != null) {

				String name = CsvRW.getColumnName(field);
				String value = "";
				try {
					value = CsvRW.getFieldValue(this, field) + "";
				} catch (CsvException e) {
					value = e.getMessage();
				}

				sb.append(name + "[" + value + "]" + ", ");

			}

		}

		int len = sb.length();
		if (len > 2) {
			sb.replace(len - 2, len, "");
		}

		return sb.toString();
	}

	void setLine(Integer line) {
		this.line = line;
	}

	public Integer getLine() {
		return line;
	}
}
