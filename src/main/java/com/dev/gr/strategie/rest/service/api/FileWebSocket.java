package com.dev.gr.strategie.rest.service.api;

import static com.dev.gr.strategie.rest.service.utils.Utils.data;
import static com.dev.gr.strategie.rest.service.utils.Utils.dataPath;
import static com.dev.gr.strategie.rest.service.utils.Utils.getFilenameList;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dev.gr.strategie.rest.service.utils.StandardResponse;
import com.dev.gr.strategie.rest.service.utils.StatusResponse;
import com.google.gson.Gson;

@WebSocket
public class FileWebSocket {
	private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
	private static final Gson gson = new Gson();
	private static final Logger log = LoggerFactory.getLogger(FileWebSocket.class);

	@OnWebSocketConnect
	public void connected(Session session) {
		sessions.add(session);
		log.info("Session " + session.getRemoteAddress() + " is connected");		
		sendSessionsData(StatusResponse.SUCCESS, getFilenameList(data()));
		
		try {		
			WatchService watcher = FileSystems.getDefault().newWatchService();
			WatchKey key = dataPath().register(watcher, ENTRY_CREATE, ENTRY_DELETE);
		
			while ((key = watcher.take()) != null) {
				for (WatchEvent<?> event : key.pollEvents()) {
					if(event.kind() == OVERFLOW) continue;
					log.info("Session size:" + sessions.size());
					sendSessionsData(StatusResponse.SUCCESS, getFilenameList(data()));
				}				
				key.reset();
			}
		} catch (IOException | InterruptedException | NullPointerException e) {
			log.error("Exception raised :", e);
		}
	}

	@OnWebSocketClose
	public void closed(Session session, int statusCode, String reason) {
		sessions.remove(session);
		log.warn("Session " + session.getRemoteAddress() + " has been closed : " + reason);
	}

	@OnWebSocketMessage
	public void message(Session session, String message) throws IOException {
		//System.out.println("Got: " + message);
		//session.getRemote().sendString(new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS)));      
	}
	
	private void sendSessionsData(StatusResponse statusResponse, List<String> data) {
		sessions.stream()
		.filter(s -> s.isOpen())
		.forEach(s -> {
			log.info("Sending data to " + s.getRemoteAddress());
			s.getRemote().sendStringByFuture(gson.toJson(new StandardResponse(statusResponse, gson.toJsonTree(data))));
		});
	}
}