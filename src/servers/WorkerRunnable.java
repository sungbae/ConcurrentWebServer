package servers;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Date;
import java.util.logging.*;



public class WorkerRunnable implements Runnable{

	private final static Logger logger = Logger.getLogger(WorkerRunnable.class.getCanonicalName());
	
    protected Socket clientSocket = null;
    protected String serverText   = null;
	private File rootDirectory;
	private String indexFileName = "index.html";

    public WorkerRunnable(File rootDirectory, String indexFileName, Socket clientSocket, String serverText) {
		if (rootDirectory.isFile()) {
			throw new IllegalArgumentException("rootDirectory must be a directory, not a file");
		}
		try {
			rootDirectory = rootDirectory.getCanonicalFile();
		} catch (IOException e) {
		}
		this.rootDirectory = rootDirectory;
		
		if (indexFileName != null) this.indexFileName = indexFileName;
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
    }

    public void run() {
    	String root = rootDirectory.getPath();
        try {
            InputStream input  = clientSocket.getInputStream();
            //OutputStream output = clientSocket.getOutputStream();
			OutputStream raw = new BufferedOutputStream(clientSocket.getOutputStream());
			Writer out = new OutputStreamWriter(raw);
            long time = System.currentTimeMillis();
            
			StringBuilder requestLine = new StringBuilder();
			while (true) {
				int c = input.read();
				if (c == '\r' || c == '\n') break;
				requestLine.append((char) c);
			}
			String get = requestLine.toString();
			logger.info(clientSocket.getRemoteSocketAddress() + " " + get);
			
			String[] tokens = get.split("\\s+");
			//String method = tokens[0];
			String version = "";
			String fileName = tokens[1];
			if (fileName.endsWith("/")) fileName += indexFileName;
			String contentType = 
					URLConnection.getFileNameMap().getContentTypeFor(fileName);
			if (tokens.length > 2) {
				version = tokens[2];
			}
			File theFile = new File(rootDirectory, fileName.substring(1, fileName.length()));
			if (theFile.canRead() 
					// Don't let clients outside the document root
					&& theFile.getCanonicalPath().startsWith(root)) {
				byte[] theData = Files.readAllBytes(theFile.toPath());
				if (version.startsWith("HTTP/")) { // send a MIME header
					sendHeader(out, "HTTP/1.0 200 OK", contentType, theData.length);
				}
				
				// send the file; it may be an image or other binary data
				// so use the underlying output stream
				// instead of the writer
				raw.write(theData);
				raw.flush();
			} else { // can't find the file
				String body = new StringBuilder("<HTML>\r\n")
						.append("<HEAD><TITLE>File Not Found</TITLE>\r\n")
						.append("</HEAD>\r\n")
						.append("<BODY>")
						.append("<H1>HTTP Error 404: File Not Found</H1>\r\n")
						.append("</BODY></HTML>\r\n").toString();
				if (version.startsWith("HTTP/")) { // send a MIME header
					sendHeader(out, "HTTP/1.0 404 File Not Found", 
							"text/html; charset=utf-8", body.length());
				}
				out.write(body);
				out.flush();
			}
            
            //output.write(("HTTP/1.1 200 OK\n\nWorkerRunnable: " +
            //		this.serverText + " - " + time + "").getBytes());
            //output.close();
			
            //raw.write(("HTTP/1.1 200 OK\n\nWorkerRunnable: " +
            //		this.serverText + " - " + time + "").getBytes());
            //raw.close();
			
            input.close();
            System.out.println("Request processed: " + time);
        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        }
    }
    
	private void sendHeader(Writer out, String responseCode, String contentType, int length) throws IOException {
		out.write(responseCode + "\r\n");
		Date now = new Date();
		out.write("Date: " + now + "\r\n");
		out.write("Server: JHTTP 2.0\r\n");
		out.write("Content-length: " + length + "\r\n");
		out.write("Content-type: " + contentType + "\r\n\r\n");
		out.flush();
	}
}