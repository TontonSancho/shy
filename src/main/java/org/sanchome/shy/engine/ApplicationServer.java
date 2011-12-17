package org.sanchome.shy.engine;

import java.io.IOException;
import java.util.logging.Logger;

import com.jme3.app.SimpleApplication;
import com.jme3.network.Network;

public class ApplicationServer extends SimpleApplication {
	private static final Logger logger = Logger.getLogger(ApplicationServer.class.getName());
	
	private int serverPort = 1664;
	private com.jme3.network.Server server;
	
	@Override
	public void simpleInitApp() {
		logger.info("Starting shy server ...");
		
		try {
			logger.info("Creating shy server on port: "+serverPort+" ...");
			server = Network.createServer(serverPort);
		} catch (IOException e) {
			logger.severe("Unable to create shy server on port: "+serverPort);
			throw new RuntimeException(e);
		}
		
		logger.info("Starting shy server ...");
		server.start();
		logger.info("shy server started.");
		
	}
	
	@Override
	public void destroy() {
		server.close();
		super.destroy();
	}
}
