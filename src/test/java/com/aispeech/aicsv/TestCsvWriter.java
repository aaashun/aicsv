package com.aispeech.aicsv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.TestCase;

public class TestCsvWriter extends TestCase {

	public static class SimpleCsvRecord extends CsvBean {

		@Column(name = "hanzi")
		private String hanzi;

		@Column(name = "mark-pinyin")
		private String markPinyin;

		public void setHanzi(String hanzi) {
			this.hanzi = hanzi;
		}

		public String getHanzi() {
			return hanzi;
		}

		public void setMarkPinyin(String markPinyin) {
			this.markPinyin = markPinyin;
		}

		public String getMarkPinyin() {
			return markPinyin;
		}

	}

	public void testWriteRecord_normal() throws IOException, CsvException {

		File tempCsvFile = File.createTempFile("ttt1", "csv");
		System.out.println(tempCsvFile.getAbsolutePath());

		CsvWriter<SimpleCsvRecord> writer = new CsvWriter<SimpleCsvRecord>(tempCsvFile, SimpleCsvRecord.class);

		SimpleCsvRecord record = new SimpleCsvRecord();
		record.setHanzi("你好");
		record.setMarkPinyin("ni hao");

		writer.writeRecord(record);
		writer.close();

		String content = readFileToString(tempCsvFile, "gbk");
		System.out.println(content);
		TestCase.assertEquals("你好,ni hao", content.split("\r?\n")[1]);

		tempCsvFile.delete();
	}

	public static void main(String[] args) {
		System.out.println("你好,ni hao".matches(".*你好.*ni hao.*"));
	}

	public void testWriteRecord_blank_hanzi() throws IOException, CsvException {

		File tempCsvFile = File.createTempFile("ttt2", "csv");
		System.out.println(tempCsvFile.getAbsolutePath());

		CsvWriter<SimpleCsvRecord> writer = new CsvWriter<SimpleCsvRecord>(tempCsvFile, SimpleCsvRecord.class);

		SimpleCsvRecord record = new SimpleCsvRecord();
		record.setHanzi("");
		record.setMarkPinyin("ni hao");

		try {
			writer.writeRecord(record);
		} catch (CsvException e) {
			TestCase.assertTrue(e.getMessage().matches(".*hanzi.*is required"));
			return;
		} finally {
			writer.close();
			tempCsvFile.delete();
		}

		TestCase.assertTrue(true);
	}

	public void testWriteRecord_null_column() throws IOException, CsvException {

		File tempCsvFile = new File(System.getProperty("java.io.tmpdir"), "ttt.csv");
		System.out.println(tempCsvFile.getAbsolutePath());

		CsvWriter<SimpleCsvRecord> writer = new CsvWriter<SimpleCsvRecord>(tempCsvFile, SimpleCsvRecord.class);

		SimpleCsvRecord record = new SimpleCsvRecord();
		record.setHanzi("你好");

		writer.writeRecord(record);
		writer.close();

		String content = readFileToString(tempCsvFile, "gbk");
		System.out.println(content);
		TestCase.assertEquals("你好,", content.split("\r?\n")[1]);

		tempCsvFile.delete();
	}

	private String readFileToString(File file, String encoding) throws IOException {

		FileInputStream fis = new FileInputStream(file);
		InputStreamReader inr = new InputStreamReader(fis, encoding);

		char[] buffer = new char[1024];
		int charRead = 0;

		StringBuffer sb = new StringBuffer();

		while ((charRead = inr.read(buffer)) != -1) {
			sb.append(buffer, 0, charRead);
		}

		return sb.toString();
	}

}