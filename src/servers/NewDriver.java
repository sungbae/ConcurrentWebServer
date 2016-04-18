package servers;

import java.io.BufferedReader;
//import java.io.File;
//import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
//import java.util.logging.*;
	
public class NewDriver extends Thread {
	
	//private static final Logger logger = Logger.getLogger(Driver.class.getCanonicalName());
	private final static String USER_AGENT = "Mozilla/5.0";
	
	private final static int THREADS = 100;
	static Thread[] thread = new Thread[THREADS];
	private int threadId;
	
	public NewDriver(int threadId) {
		this.threadId = threadId;
	}
	
	private static void sendGet() throws Exception {

		String url = "http://localhost:8080";
		
		URL myUrl = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) myUrl.openConnection();

		// Call GET method 
		conn.setRequestMethod("GET");

		// Add request header
		conn.setRequestProperty("User-Agent", USER_AGENT);

		//int responseCode = conn.getResponseCode();
		Map<String, List<String>> responseCode = conn.getHeaderFields();

		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}

		in.close();

		// print result
		System.out.println(response.toString() + "\n");
	}

	public void run() {
		try {
			sendGet();
		} catch (Exception e) {
			System.out.println("sendGet() overload...");
		}
	}	
	
	public static void main(String[] args) {
		//ThreadPooledServer server = new ThreadPooledServer(File("file_directory/index.html"), 8080);
		//new Thread(server).start();
		/*
		try {
		    Thread.sleep(20 * 1000);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		System.out.println("Stopping Server");
		server.stop();
		*/
		
		for (int i = 0; i < THREADS; i++) {
			thread[i] = new NewDriver(i);
		}
		
		long startTime = System.currentTimeMillis();
		
		for (int i = 0; i < THREADS; i++) {
			thread[i].start();
		}
		try {
			for (int i = 0; i < THREADS; i++) {
				thread[i].join();
			}
		} catch (InterruptedException e) {
			System.out.println("Interrupted exception caught ...");
		}
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println("The elapsed time is: " + elapsedTime + " miliseconds");
	}
}
