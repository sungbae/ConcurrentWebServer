package servers;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

public class ThreadPooledServer implements Runnable{

	private static final Logger logger = Logger.getLogger(ThreadPooledServer.class.getCanonicalName());
    protected int          serverPort   = 8080;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
    protected ExecutorService threadPool =
        Executors.newFixedThreadPool(1);
    
    private final File rootDirectory;
    private static int port;
    private static final String INDEX_FILE = "index.html";
    

    public ThreadPooledServer(File rootDirectory, int port) throws IOException{
		if (!rootDirectory.isDirectory()) {
			throw new IOException(rootDirectory + " does not exist as a directory");
		}
		this.rootDirectory = rootDirectory;
        this.serverPort = port;
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
        while(! isStopped()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
    			logger.info("Accepting  connections on port " + serverSocket.getLocalPort());
    			logger.info("Document Root: " + rootDirectory);
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    break;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);
            }
            this.threadPool.execute(
                new WorkerRunnable(rootDirectory, INDEX_FILE, clientSocket,
                    "Thread Pooled Server"));
        }
        this.threadPool.shutdown();
        System.out.println("Server Stopped.") ;
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port 8080", e);
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
			ThreadPooledServer webServer = new ThreadPooledServer(docRoot, port);
			webServer.run();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Server could not start", e);
		}
	}
}
