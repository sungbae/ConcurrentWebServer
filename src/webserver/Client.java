package webserver;

import java.net.*;
import java.io.*;

public class Client extends Thread {
	
	//private final static String USER_AGENT = "Mozilla/5.0";
	private final static int THREADS = 100;
	static Thread[] thread = new Thread[THREADS];
	private int threadId;
	
	public Client(int threadId) {
		this.threadId = threadId;
	}
	
	private static void sendGet() throws Exception {

		String url = "http://localhost:8080";
		
		URL myUrl = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) myUrl.openConnection();

		// Call GET method 
		conn.setRequestMethod("GET");

		// Add request header
		//conn.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = conn.getResponseCode();
		//Map<String, List<String>> responseCode = conn.getHeaderFields();

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

		//print result
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
		for (int i = 0; i < THREADS; i++) {
			thread[i] = new Client(i);
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
