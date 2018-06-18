package com.dev.gr.strategie.rest.test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dev.gr.strategie.rest.service.Agent;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class TestAgent {

	private static final String BASE_URL ="http://localhost:10000/api";
	
	private static Agent agent;
	private static CloseableHttpClient client;
	private static Gson gson;
	
	@Before
	public void before() {
		client = HttpClients.createDefault();
	}
	
	@BeforeClass
	public static void start() {
		Arrays.asList("testDownloadFile.txt", "testDeleteFile.txt")
			.forEach(f -> {
				try {
					FileUtils.copyFile(fromPath("Videos","testData", f), fromPath("Videos", f));
				} catch (IOException e) {
						e.printStackTrace();
				}
			});				
		agent = new Agent();
		gson = new Gson();
	}
	
	@After
	public void after() {
		try {
			client.close();
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@AfterClass
	public static void stop() {
		agent.stopAgent();
	}

	//@Test
	public void testListFile() {
		HttpGet get = new HttpGet(buildURL("/file/list"));
		try {
			HttpResponse response = client.execute(get);
			assertEquals(200, response.getStatusLine().getStatusCode());

			String[] files = gson.fromJson(new JsonReader(new InputStreamReader(response.getEntity().getContent())), String[].class);
			assertNotNull(files);
			
			List<String> listFiles = Arrays.asList(files);
			assertNotEquals(0, listFiles.size());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}		
	}
	
	//@Test
	public void testUploadFile() {
		given().
			contentType("multipart/form-data").
			multiPart("file", fromPath("D:\\test.txt")).
		when().
			post(buildURL("/files")).
		then().
			statusCode(200);
	}
	
	@Test
	public void  testDownloadFile() {
		String fileName = "testDownloadFile.txt";
		File sourceFile = fromPath("Videos", fileName);
		File downloadedFile = fromPath("Videos", suffixFileName(fileName, "_downloaded"));
		try(InputStream is =
			given().
				log().ifValidationFails().
			when().
				get(buildURL("/files/" + fileName)).
			then().
				statusCode(200).
			extract().
				response().asInputStream()) {			
			FileUtils.copyInputStreamToFile(is, downloadedFile);			
			assertTrue(FileUtils.contentEquals(sourceFile, downloadedFile));
			
			fileName = "notExistingFile.txt";
			given().
				log().ifValidationFails().
			when().
				get(buildURL("/files/" + fileName)).
			then().
				statusCode(404).
				body("status", equalTo("ERROR")).
				body("data", containsString("NoSuchFileException"));
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	@Test
	public void  testDeleteFile() {
		String fileName = "testDeleteFile.txt";
		given().
			log().ifValidationFails().
		when().
			delete(buildURL("/files/" + fileName)).
		then().
			statusCode(204);
		
		fileName = "notExistingFile.txt";
		given().
			log().ifValidationFails().
		when().
			delete(buildURL("/files/" + fileName)).
		then().
			statusCode(404).
			body("status", equalTo("ERROR")).
			body("data", containsString("NoSuchFileException"));
	}
	
	private final String buildURL(String uri) {
		return new StringBuilder(BASE_URL)
				.append(uri)
				.toString();
	}
	
	/**
	 * @see Paths#get(String first, String... more)
	 * @param first the path string or initial part of the path string
	 * @param more additional strings to be joined to form the path string
	 * @return the resulting file from the path
	 */
	private static final File fromPath(String first, String... more) {
		return Paths.get(first, more).toFile();
	}
	
	private final String suffixFileName(String fileName, String suffix) {
		return new StringBuilder(FilenameUtils.getBaseName(fileName))
				.append(suffix)
				.append(".")
				.append(FilenameUtils.getExtension(fileName))
				.toString();
	}
	/*
	private final String prefixFileName(String fileName, String prefix) {
		return new StringBuilder(prefix)
				.append(FilenameUtils.getName(fileName))
				.toString();
	}
	*/
}