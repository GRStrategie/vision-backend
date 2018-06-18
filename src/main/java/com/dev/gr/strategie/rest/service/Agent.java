package com.dev.gr.strategie.rest.service;

import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.delete;
import static spark.Spark.staticFiles;
import static spark.Spark.stop;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.dev.gr.strategie.rest.service.api.FileApi;
import com.google.gson.Gson;

public class Agent {

	public Agent() {
		this(10000);
	}

	public Agent(int port) {
		port(port);
		staticFiles.externalLocation("Videos");

		path("/api", () -> {
			path("/files", () -> {
				post("",										FileApi.uploadFile);	
				get("/:filename",		 						FileApi.downloadFile);
				delete("/:filename",							FileApi.deleteFile);	
			});
		});
				
		get("/files/list", (req, res) -> {
			Gson gson = new Gson();		
			return gson.toJson(Arrays.asList(new File("Videos").listFiles())
					.stream()
					.filter(f -> f.isFile())
					.map(f -> f.getName())
					.collect(Collectors.toList()));			
		});
	}

	public void stopAgent() {
		stop();
	}
	
	public static void main(String[] args) {
		new Agent();
	}
}