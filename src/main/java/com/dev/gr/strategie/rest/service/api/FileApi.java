package com.dev.gr.strategie.rest.service.api;


import static com.dev.gr.strategie.rest.service.utils.Utils.data;
import static com.dev.gr.strategie.rest.service.utils.Utils.dataPath;
import static com.dev.gr.strategie.rest.service.utils.Utils.videosPath;
import static j2html.TagCreator.button;
import static j2html.TagCreator.form;
import static j2html.TagCreator.input;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.MultipartConfigElement;

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
				input().withType("file").withName("file"),
				button().withText("Upload picture")
				).render();			
	};

	public static Route uploadFile = (req, res) -> {
		res.header("Access-Control-Allow-Origin", "*");
		req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
		req.raw().getParts().stream()
			.filter(p -> p.getName().equals("file"))
			.forEach(p -> {
		        try {
					Files.copy(p.getInputStream(), dataPath().resolve(Paths.get(p.getSubmittedFileName()).getFileName()), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					log.error("Exception raised : ", e);
					res.status(500);
					res.body(e.toString());
				}
			});
		res.type("application/json");		
        return new StandardResponse(StatusResponse.SUCCESS);
	};
	
	public static Route listFiles = (req, res) -> {	
		res.header("Access-Control-Allow-Origin", "*");
		res.type("application/json");	
		
		if(data().isDirectory()) {
			Optional<File[]> fileOptional = Optional.ofNullable(data().listFiles());				
			List<String> filenameList = Arrays.asList(fileOptional.orElse(new File[]{})).
					stream().
					filter(f -> f.isFile()).
					map(f -> f.getName()).
					collect(Collectors.toList());
			return new StandardResponse(StatusResponse.SUCCESS, gson.toJsonTree(filenameList));
		} else {
			return new StandardResponse(StatusResponse.ERROR, "Path " + data().getAbsolutePath() + " is not a directory");
		}		
	};
				
	public static Route downloadFile = (req, res) -> {	
		Path filePath = videosPath().resolve(req.params(":filename"));
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
		Path filePath = videosPath().resolve(req.params(":filename"));
		res.type("application/json");
		try {
			Files.delete(filePath);
			log.info("File " + filePath + " has been successfully deleted");
			res.status(200);
			return new StandardResponse(StatusResponse.SUCCESS, "File " + filePath + " has been successfully deleted");
		} catch (IOException e) {
			log.error("Exception raised :" , e);
			res.status(404);
			return new StandardResponse(StatusResponse.ERROR, gson.toJsonTree(e.toString()));
		}
	};
}
