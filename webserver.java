/*	Kou Vang
 * 	ICS 460.01
 *  PA 2 - Single Thread Webserver with MIME
 *  Sources:
 *  3-29-2016: http://stackoverflow.com/questions/17763889/webserver-not-showing-images 
 *  3-30-2016: http://tutorials.jenkov.com/java-multithreaded-servers/singlethreaded-server.html
 *  4-1-2016: http://www.java2s.com/Code/Java/Network-Protocol/ASimpleWebServer.htm
 *  4-2-2016: https://www.seas.gwu.edu/~cheng/6431/Projects/Project1WebServer/webserver.html
 */

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class webserver {
	
	private static ServerSocket serverSocket;

	public static void main(String[] args) throws IOException {
		System.out.println("Starting the webserver...");
		System.out.println("Open up a browswer (Chrome, Firefox, etc...) and type localhost in the browser bar.");
		System.out.println("Type the filename + extension after localhost/ to access the file.");
		System.out.println("I.E. localhost/index.html");
		System.out.println("Press Ctrl + C to quit at any time.");
		
		serverSocket = new ServerSocket(80);  // Listening port 80
    
		// Waits for the client to connect to the server and then creates
		// a separate thread to handle the exchange
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				new ConnectionHandler(socket);
			}
			catch (Exception e) {
				System.out.println("Error!: " + e);
			}
		}
	}
}

// The ConnectionHandler class accepts a request and responds accordingly
class ConnectionHandler extends Thread {
	private Socket socket;

	public ConnectionHandler(Socket s) {
		socket = s;
		start();
	}

  // This method receives the requests, responds, and closes the connection
	public void run() {
		try {
			// Reads and process the input and output streams
			BufferedReader input = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			PrintStream output = new PrintStream(new BufferedOutputStream(
					socket.getOutputStream()));

			// Assign the input to a string
			String string = input.readLine();
			System.out.println(string);

			String filename = "";
			StringTokenizer stringTokenizer = new StringTokenizer(string);
			try {

				// Parse the filename
				if (stringTokenizer.hasMoreElements() && stringTokenizer.nextToken().equalsIgnoreCase("GET")
						&& stringTokenizer.hasMoreElements())
					filename=stringTokenizer.nextToken();
				else
					throw new FileNotFoundException();

				// Sets default page to index.html if no url is specified
				if (filename.endsWith("/"))
					filename += "index.html";

				// Removes the "/" from the filename
				while (filename.indexOf("/") == 0)
					filename = filename.substring(1);

				// Choose the file
				InputStream file = new FileInputStream(filename);

				// Determine MIME types and print the headers
				String mimeType = "text/plain";
				if (filename.endsWith(".html") || filename.endsWith(".htm"))
					mimeType = "text/html";
				else if (filename.endsWith(".css"))
					mimeType = "text/css";
				else if (filename.endsWith(".js"))
					mimeType = "application/javascript";
				else if (filename.endsWith(".pdf"))
					mimeType = "application/pdf";
				else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg"))
					mimeType = "image/jpeg";
				else if (filename.endsWith(".gif"))
					mimeType = "image/gif";
				else if (filename.endsWith(".png"))
					mimeType = "image/png";
				output.print("HTTP/1.0 200 OK\r\n"+
						"Content-type: " + mimeType + "\r\n\r\n");

				// Serve files then close connection
				byte[] b = new byte[4096];
				int i;
				while ((i = file.read(b))>0)
					output.write(b, 0, i);
				output.close();
			}
			catch (FileNotFoundException f) {
				output.println("HTTP/1.0 404 Not Found\r\n"+
						"Content-type: text/html\r\n\r\n"+
						"<html><head></head><body>" + filename + " not found</body></html>\n");
				output.close();
			}
		}
		catch (IOException e) {
			System.out.println(e);
		}
  }
}

