package webserver;

import java.io.*;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;
import java.util.logging.*;

public class JHTTP {
	
	private static final Logger logger = Logger.getLogger(JHTTP.class.getCanonicalName());
	private static final int NUM_THREADS = 16;
	private static final String INDEX_FILE = "index.html";
	
	private final File rootDirectory;
	private static int port;
	
	public JHTTP(File rootDirectory, int port) throws IOException {
		if (!rootDirectory.isDirectory()) {
			throw new IOException(rootDirectory + " does not exist as a directory");
		}
		this.rootDirectory = rootDirectory;
		this.port = port;
	}
	
	public void start() throws IOException {
		ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
		try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.socket().bind(new InetSocketAddress(port));
			logger.info("Accepting  connections on port " + port);
			logger.info("Document Root: " + rootDirectory);
			
			while (true) {
				try {
					//Socket request = server.accept(); 
					SocketChannel socketChannel = serverSocketChannel.accept();
					if (socketChannel != null) {
						Runnable r = new RequestProcessor(rootDirectory, INDEX_FILE, socketChannel);
						pool.submit(r);	
					}
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
			JHTTP webServer = new JHTTP(docRoot, port);
			webServer.start();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Server could not start", e);
		}
	}
}
