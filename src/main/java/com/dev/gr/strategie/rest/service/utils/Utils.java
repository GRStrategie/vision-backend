package com.dev.gr.strategie.rest.service.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;

public class Utils {
	private static final ResourceBundle rb = ResourceBundle.getBundle("config");
	private static final String BASE_URL = rb.getString("base_url");
	private static final String DATA_ROOT_DIR= rb.getString("data_root_dir");
	private static final String DATA_TEST_DIR = rb.getString("data_test_dir");
	
	private static final Path dataPath = Paths.get(DATA_ROOT_DIR);	
	private static final Path testDataPath = dataPath.resolve(DATA_TEST_DIR);
	
	public static final String baseUrl() {
		return BASE_URL;
	}
	
	public static final Path dataPath() {
		return dataPath;
	}
	
	public static final File data() {
		return dataPath.toFile();
	}
	
	public static final Path testDataPath() {
		return testDataPath;
	}
	
	public static final File testData() {
		return testDataPath.toFile();
	}
	
	public static final String suffixFileName(String fileName, String suffix) {
		return new StringBuilder(FilenameUtils.getBaseName(fileName))
				.append(suffix)
				.append(".")
				.append(FilenameUtils.getExtension(fileName))
				.toString();
	}
	
	public static final String prefixFileName(String fileName, String prefix) {
		return new StringBuilder(prefix)
				.append(FilenameUtils.getName(fileName))
				.toString();
	}
}
