package com.dev.gr.strategie.rest.service.api;


import static j2html.TagCreator.button;
import static j2html.TagCreator.form;
import static j2html.TagCreator.input;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dev.gr.strategie.rest.service.utils.StandardResponse;
import com.dev.gr.strategie.rest.service.utils.StatusResponse;
import com.dev.gr.strategie.rest.service.utils.Utils;
import com.google.gson.Gson;

import spark.Route;

public class FileApi {

	private static final Logger log = LoggerFactory.getLogger(FileApi.class);
	private static final Gson gson = new Gson();

	public static Route sendFile = (req, res) -> {
		return form().withMethod("post").attr("enctype", "multipart/form-data").attr("accept", ".*").with(
				input().withType("file").withName("uploaded_file"),
				button().withText("Upload picture")
				).render();			
	};

	public static Route uploadFile = (req, res) -> {	
		req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
		Part reqFilePart = req.raw().getPart("file");
		Path filePath = Utils.dataPath().resolve(getFileName(reqFilePart).toString());		
        Files.copy(reqFilePart.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return "<h1>You uploaded this image:<h1><img src='" + filePath.getFileName() + "'>";
	};
	
	public static Route listFiles = (req, res) -> {	
		File directory = Utils.data();
		res.type("application/json");
		
		if(directory.isDirectory()) {
			Optional<String[]> filenameOptional = Optional.ofNullable(directory.list());				
			List<String> filenameList = Arrays.asList(filenameOptional.orElse(new String[]{})).
					stream().
					map(f -> FilenameUtils.getName(f)).
					collect(Collectors.toList());
			return new StandardResponse(StatusResponse.SUCCESS, gson.toJsonTree(filenameList));
		} else {
			return new StandardResponse(StatusResponse.ERROR, "Path " + directory.getAbsolutePath() + " is not a directory");
		}		
	};
				
	public static Route downloadFile = (req, res) -> {	
		Path filePath = Utils.dataPath().resolve(req.params(":filename"));
		res.type("application/json");
		try {
			res.status(200);
			return Files.newInputStream(filePath);
		} catch(IOException e) {
			log.error("Exception raised : ", e);
			res.status(404);
			res.type("application/json");
			return gson.toJson(new StandardResponse(StatusResponse.ERROR, gson.toJsonTree(e.toString())));
		}		
	};
	
	public static Route deleteFile = (req, res) -> {
		Path filePath = Utils.dataPath().resolve(req.params(":filename"));
		res.type("application/json");
		try {
			Files.delete(filePath);
			res.status(200);
			return new StandardResponse(StatusResponse.SUCCESS, "File " + filePath + " has been successfully deleted");
		} catch (IOException e) {
			log.error("Exception raised :" , e);
			res.status(404);
			return new StandardResponse(StatusResponse.ERROR, gson.toJsonTree(e.toString()));
		}
	};
	
	private static String getFileName(Part part) {
		for (String cd : part.getHeader("content-disposition").split(";")) {
			if (cd.trim().startsWith("filename")) {
				return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;
	}
}
