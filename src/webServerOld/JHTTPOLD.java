package webServerOld;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class JHTTPOLD {
	
	private static final Logger logger = Logger.getLogger(JHTTPOLD.class.getCanonicalName());
	private static final int NUM_THREADS = 16;
	private static final String INDEX_FILE = "index.html";
	
	private final File rootDirectory;
	private static int port;
	
	public JHTTPOLD(File rootDirectory, int port) throws IOException {
		if (!rootDirectory.isDirectory()) {
			throw new IOException(rootDirectory + " does not exist as a directory");
		}
		this.rootDirectory = rootDirectory;
		this.port = port;
	}
	
	public void start() throws IOException {
		ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
		try (ServerSocket server = new ServerSocket(port)) {
			logger.info("Accepting  connections on port " + server.getLocalPort());
			logger.info("Document Root: " + rootDirectory);
			
			while (true) {
				try {
					Socket request = server.accept();
					Runnable r = new RequestProcessorOLD(rootDirectory, INDEX_FILE, request);
					pool.submit(r);
				} catch (IOException ex) {
					logger.log(Level.WARNING, "Error accepting connection", ex);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		// Get the document root
		File docRoot;
		try {
			docRoot = new File(args[0]);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Usage: java JHTTP docroot port");
			return;
		}
		// Set the port to listen on
		try {
			port = Integer.parseInt(args[1]);
			if (port < 0 || port > 65535) port = 80;
		} catch (RuntimeException e) {
			logger.log(Level.SEVERE, "Server did not get the correct port", e);
		}
		
		try {
			JHTTPOLD webServer = new JHTTPOLD(docRoot, port);
			webServer.start();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Server could not start", e);
		}
	}
}
