package com.dev.gr.strategie.rest.service;

import static spark.Spark.webSocket;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;
//import static spark.Spark.staticFiles;
import static spark.Spark.stop;

import com.dev.gr.strategie.rest.service.api.FileWebSocket;
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
		
		webSocket("/files", FileWebSocket.class);

		path("/api", () -> {
			path("/files", () -> {
				get("/sendFile",					FileApi.sendFile);		
				post("",							FileApi.uploadFile,					new JsonTransformer());
				get("",		 						FileApi.listFiles,					new JsonTransformer());
				get("/:filename",		 			FileApi.downloadFile);
				get("/delete/:filename",			FileApi.deleteFile,					new JsonTransformer());	
			});
			path("/playlist", () -> {
				post("/schedule",					ScheduleApi.schedulePlaylist,		new JsonTransformer());
				delete("/:playlistName",			ScheduleApi.deletePlaylist, 		new JsonTransformer());
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