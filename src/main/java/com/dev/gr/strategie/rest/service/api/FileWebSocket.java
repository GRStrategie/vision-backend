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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
	// Store sessions if you want to, for example, broadcast a message to all users
	private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
	private static final Gson gson = new Gson();
	private static final Logger log = LoggerFactory.getLogger(FileWebSocket.class);

	@OnWebSocketConnect
	public void connected(Session session) {
		sessions.add(session);
		log.info("Session " + session.getRemote().getInetSocketAddress() + " is connected");		
		
		try {
			session.getRemote().sendString(gson.toJson(
					new StandardResponse(StatusResponse.SUCCESS, gson.toJsonTree(getFilenameList(data())))));
					
			WatchService watcher = FileSystems.getDefault().newWatchService();
			WatchKey key = dataPath().register(watcher, ENTRY_CREATE, ENTRY_DELETE);
			while ((key = watcher.take()) != null) {
				for (WatchEvent<?> event : key.pollEvents()) {
					if(event.kind() == OVERFLOW) continue;	
					session.getRemote().sendString(gson.toJson(
							new StandardResponse(StatusResponse.SUCCESS, gson.toJsonTree(getFilenameList(data())))));
				}				
				key.reset();
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

	}

	@OnWebSocketClose
	public void closed(Session session, int statusCode, String reason) {
		sessions.remove(session);
	}

	@OnWebSocketMessage
	public void message(Session session, String message) throws IOException {
		System.out.println("Got: " + message);
		session.getRemote().sendString(new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS)));      
	}
}