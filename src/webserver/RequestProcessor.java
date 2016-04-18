package webserver;

import java.io.*;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.*;

public class RequestProcessor implements Runnable {
	
	private final static Logger logger = Logger.getLogger(RequestProcessor.class.getCanonicalName());
	private File rootDirectory;
	private String indexFileName = "index.html";
	private SocketChannel channel;
	
	public RequestProcessor(File rootDirectory, String indexFileName, SocketChannel socketChannel) {
		if (rootDirectory.isFile()) {
			throw new IllegalArgumentException("rootDirectory must be a directory, not a file");
		}
		try {
			rootDirectory = rootDirectory.getCanonicalFile();
		} catch (IOException e) {
		}
		this.rootDirectory = rootDirectory;
		
		if (indexFileName != null) this.indexFileName = indexFileName;
		this.channel = socketChannel;
	}

	@Override
	public void run() {
		// for security checks
		String root = rootDirectory.getPath();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(channel.socket().getOutputStream());
			Writer out = new OutputStreamWriter(channel.socket().getOutputStream());
			Reader in = new InputStreamReader(
						new BufferedInputStream(
							(channel.socket().getInputStream())
							), "US-ASCII");
			StringBuilder requestLine = new StringBuilder();
			while (true) {
				int c = in.read();
				if (c == '\r' || c == '\n') break;
				requestLine.append((char) c);
			}
			String get = requestLine.toString();
			logger.info(channel.socket().getRemoteSocketAddress() + " " + get);
			
			String[] tokens = get.split("\\s+");
			String method = tokens[0];
			String version = "";
			if (method.equals("GET")) {
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
					oos.writeObject(theData);
					oos.flush();
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
			} else { // method does not equal "GET"
				String body = new StringBuilder("<HTML>\r\n")
						.append("<HEAD><TITLE>Not Implemented</TITLE>\r\n")
						.append("</HEAD>\r\n")
						.append("<BODY>")
						.append("<H1>HTTP Error 501: Not Implemented</H1>\r\n")
						.append("</BODY></HTML>\r\n").toString();
				if (version.startsWith("HTTP/")) { // send a MIME header
					sendHeader(out, "HTTP/1.0 501 Not Implemented",
							"text/html; charset=utf-8", body.length());
				}
				out.write(body);
				out.flush();
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "Error talking to " + channel.socket().getRemoteSocketAddress(), e);
		} finally {
			try {
				channel.close();
			} catch (IOException e) {}
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
