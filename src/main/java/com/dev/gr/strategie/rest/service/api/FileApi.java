package com.dev.gr.strategie.rest.service.api;


import static j2html.TagCreator.button;
import static j2html.TagCreator.form;
import static j2html.TagCreator.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dev.gr.strategie.rest.service.utils.StandardResponse;
import com.dev.gr.strategie.rest.service.utils.StatusResponse;
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
		Path filePath = Paths.get("Videos", Paths.get(getFileName(reqFilePart)).toString());		
        Files.copy(reqFilePart.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return "<h1>You uploaded this image:<h1><img src='" + filePath.getFileName() + "'>";
	};
	
			
	public static Route downloadFile = (req, res) -> {
		Path filePath = Paths.get("Videos", req.params(":filename"));		
		try {
			res.status(200);
			return Files.readAllBytes(filePath);
		} catch(IOException e) {
			log.error("Exception raised : ", e);
			res.status(404);
			res.type("application/json");
			return  gson.toJson(new StandardResponse(StatusResponse.ERROR, gson.toJsonTree(e.toString())));
		}		
	};
	
	public static Route deleteFile = (req, res) -> {
		Path filePath = Paths.get("Videos", req.params(":filename"));
		try {
			Files.delete(filePath);
			res.status(204);
			return res;
		} catch (IOException e) {
			log.error("Exception raised :" , e);
			res.status(404);
			res.type("application/json");
			return gson.toJson(new StandardResponse(StatusResponse.ERROR, gson.toJsonTree(e.toString())));
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
