package com.aispeech.aicsv;

import java.io.IOException;
import java.nio.charset.Charset;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestCsvReader extends TestCase {

	public static class SimpleCsvRecord extends CsvBean {

		@Column(name = "hanzi", nullable = false)
		private String hanzi;

		@Column(name = "mark-pinyin", nullable = false)
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

	private static <T extends CsvBean> CsvReader<T> newCsvReader(String resource, Class<T> typeOfT) throws IOException,
			CsvException {

		CsvReader<T> reader = new CsvReader<T>(ClassLoader.getSystemResourceAsStream(resource), resource, ',', Charset
				.forName("gbk"), typeOfT);

		return reader;
	}

	public void testNormal() throws IOException, CsvException {

		CsvReader<SimpleCsvRecord> reader = newCsvReader("testNormal.csv", SimpleCsvRecord.class);

		try {
			while (true) {

				SimpleCsvRecord bean = reader.readRecord();
				if (bean == null) {
					break;
				}
				Assert.assertEquals("你好", bean.getHanzi());
				Assert.assertEquals("ni hao", bean.getMarkPinyin());

			}
		} finally {
			reader.close();
		}
	}

	public void testEmptyCsv() throws IOException {

		try {
			newCsvReader("testEmptyCsv.csv", SimpleCsvRecord.class).close();
		} catch (CsvException e) {

			System.out.println(e.getMessage());
			Assert.assertTrue(e.getMessage().matches(".*empty csv.*"));
			return;

		}

		Assert.assertTrue(false);

	}

	public void testMissingHeader() throws IOException {

		try {
			newCsvReader("testMissingHeader.csv", SimpleCsvRecord.class).close();
		} catch (CsvException e) {

			System.out.println(e.getMessage());
			Assert.assertTrue(e.getMessage().contains("missing header"));
			return;

		}

		Assert.assertTrue(false);

	}

	public void testDuplicateHeader() throws IOException {

		System.out.println("\n-----testDuplicateHeader-----");

		try {

			newCsvReader("testDuplicateHeader.csv", SimpleCsvRecord.class).close();

		} catch (CsvException e) {

			System.out.println(e.getMessage());
			TestCase.assertTrue(e.getMessage().contains("\nduplicate header"));
			return;

		}

		TestCase.assertTrue(false);

	}

	private static class ForTestDuplicateColumnNameRecord extends CsvBean {

		@SuppressWarnings("unused")
		@Column(name = "hanzi")
		private String hanzi;

		@SuppressWarnings("unused")
		@Column(name = "hanzi")
		private String markPinyin;

	}

	public static class ForTestUniqueRecord extends CsvBean {

		@SuppressWarnings("unused")
		@Column(name = "hanzi", unique = true)
		private String hanzi;

		@SuppressWarnings("unused")
		@Column(name = "mark-pinyin")
		private String markPinyin;

	}

	public void testUnique() throws IOException, CsvException {

		System.out.println("\n--------testUnique----------");

		CsvReader<ForTestUniqueRecord> reader = newCsvReader("testUnique.csv", ForTestUniqueRecord.class);

		try {

			@SuppressWarnings("unused")
			ForTestUniqueRecord record = null;
			while ((record = reader.readRecord()) != null) {
			}

		} catch (CsvException e) {

			System.out.println(e.getMessage());
			TestCase.assertTrue(e.getMessage().contains("duplicate [你好]"));
			return;

		} finally {
			reader.close();
		}

		TestCase.assertTrue(false);

	}

	public static class ForTestDefaultColumnNameRecord extends CsvBean {
		@Column
		private String hanzi;
	}

	public void testDefaultColumnName() throws IOException, CsvException {

		CsvReader<ForTestDefaultColumnNameRecord> reader = newCsvReader("testDefaultColumnName.csv",
				ForTestDefaultColumnNameRecord.class);

		TestCase.assertEquals("你好", reader.readRecord().hanzi);

		reader.close();
	}

	public void testNoColumnName() throws IOException {

		class ForTestNoColumnName extends CsvBean {

		}

		System.out.println("\n---------testNoColumnName---------");
		try {

			newCsvReader("testNoColumnName.csv", ForTestNoColumnName.class);

		} catch (CsvException e) {

			System.out.println(e.getMessage());
			TestCase.assertTrue(e.getMessage().startsWith("There's no @Column"));
			return;

		}

		TestCase.assertTrue(false);

	}

	public void testDuplicateColumnName() throws IOException {

		try {

			newCsvReader("testDuplicateColumnName.csv", ForTestDuplicateColumnNameRecord.class);

		} catch (CsvException e) {
			TestCase.assertTrue(e.getMessage().startsWith("Duplicate column"));
			return;
		}

		TestCase.assertTrue(false);

	}

	public static class ForTestMatchesRecord extends CsvBean {

		@SuppressWarnings("unused")
		@Column(name = "number-pinyin", matches = "[a-z0-4\\-]+")
		private String numberPinyin;

	}

	public void testMatches() throws IOException {

		System.out.println("\n-------testMatches---------");

		try {

			CsvReader<ForTestMatchesRecord> reader = newCsvReader("testMatches.csv", ForTestMatchesRecord.class);

			@SuppressWarnings("unused")
			ForTestMatchesRecord record = null;
			while ((record = reader.readRecord()) != null) {
			}

		} catch (CsvException e) {

			System.out.println(e.getMessage());
			TestCase.assertTrue(e.getMessage().matches(".*你好.*match.*"));
			return;

		}

		TestCase.assertTrue(false);

	}

	public void testNullable() throws IOException, CsvException {

		CsvReader<SimpleCsvRecord> reader = newCsvReader("testNullable.csv", SimpleCsvRecord.class);
		try {
			reader.readRecord();
		} catch (CsvException e) {
			Assert.assertTrue(e.getMessage().matches(".*not.*blank.*"));
			return;
		} finally {
			reader.close();
		}

		Assert.assertTrue(false);

	}

	// public static class ForTestTrimRecord extends CsvBean {
	//
	// @Column(name = "hanzi")
	// private String hanzi;
	//
	// @Column(name = "mark-pinyin", trim = false)
	// private String markPinyin;
	//
	// }

	// public void testTrim() throws Exception {
	//
	// // CsvReader<ForTestTrimRecord> reader = newCsvReader("testTrim.csv", ForTestTrimRecord.class);
	// CsvReader<ForTestTrimRecord> reader = new CsvReader<ForTestTrimRecord>(new File(
	// "E:\\svn\\aid201003\\branches\\sprint1\\aicsv\\src\\test\\resources\\testTrim.csv"),
	// ForTestTrimRecord.class);
	//
	// ForTestTrimRecord record = reader.readRecord();
	// TestCase.assertEquals("你  好", record.hanzi);
	// TestCase.assertEquals(" ni hao　", record.markPinyin);
	//
	// }
}