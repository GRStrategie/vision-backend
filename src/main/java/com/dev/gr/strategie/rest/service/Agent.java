package com.dev.gr.strategie.rest.service;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;
//import static spark.Spark.staticFiles;
import static spark.Spark.stop;

import com.dev.gr.strategie.rest.service.api.FileApi;
import com.dev.gr.strategie.rest.service.api.ScheduleApi;
import com.dev.gr.strategie.rest.service.utils.JsonTransformer;

public class Agent {
	
	public Agent() {
		this(10000);
	}

	public Agent(int port) {
		port(port);
		//staticFiles.externalLocation("Videos");

		path("/api", () -> {
			path("/files", () -> {
				get("/sendFile",					FileApi.sendFile);		
				post("",							FileApi.uploadFile);
				get("",		 						FileApi.listFiles,					new JsonTransformer());
				get("/:filename",		 			FileApi.downloadFile);
				delete("/:filename",				FileApi.deleteFile,					new JsonTransformer());	
			});
			path("/schedule", () -> {
				post("/playlist",					ScheduleApi.schedulePlaylist,		new JsonTransformer());
			});
		});	
	}

	public void stopAgent() {
		stop();
	}
	
	public static void main(String[] args) {
		new Agent();
	}
}