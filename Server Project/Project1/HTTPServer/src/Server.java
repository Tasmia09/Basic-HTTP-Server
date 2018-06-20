import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Server {

  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
    //Create context fetches pages 
    //this context would take you to a information page 
    server.createContext("/info", new InfoHandler());
    //the context would "get" a html page and also do the processing of "post"
    server.createContext("/get", new GetHandler());
    // Adding '/testPost' context
    server.createContext("/testPost", new TestHandler());
    //this context is for listing directories
    server.createContext("/index", new DirectoryHandler());
    //create context for cgi file parsing
    server.createContext("/getCGI", new CGIHandler());
    server.setExecutor(null); // creates a default executor
    server.start();
  }
static class DirectoryHandler implements HttpHandler{
    @Override
    public void handle(HttpExchange t)throws IOException{
        //this handler lists directory files
            //first we write all the files under directory into directories.html
    	String root = "/home/tasmia/Documents/Project1/Root/";
        Path outputPath=Paths.get(root+"directories.html");
        Path directoryPath=Paths.get(root);
          try (FileWriter target = new FileWriter(outputPath.toFile());
            BufferedWriter buffered = new BufferedWriter(target);
            PrintWriter writer = new PrintWriter(buffered)) {
        writer.write("<html>\n\t<body>\n\t\t<h1>Contents of "
                        + directoryPath.toRealPath()  + ":</h1>\n\t\t<ul>\n");
            File rootDirec=new File(root);
            //listing all the files of list directories and saving them in the paths array
            String[] paths = rootDirec.list();
            //listing each file
            for(String path :paths){
                writer.write("<li><a href=<q>"+path+"</q></a></li>");
            }
            writer.write("\t\t</ul>\n\t</body>\n</html>");
    }   catch (MalformedURLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
          //Now that directories.html is ready, we are going to show the file to the client
          // add the required response header for a text/html file
          Headers h = t.getResponseHeaders();
          h.add("Content-Type", "text/html");
          File file = new File (root+"directories.html");
          byte [] bytearray  = new byte [(int)file.length()];
          //open the file
          FileInputStream fis = new FileInputStream(file);
          //read the file
          BufferedInputStream bis = new BufferedInputStream(fis);
          //read the file into bytearray
          bis.read(bytearray, 0, bytearray.length);
          t.sendResponseHeaders(200, file.length());
            try (OutputStream os = t.getResponseBody()) {
                os.write(bytearray,0,bytearray.length);
            }
    }
}
  static class InfoHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
      String response = "Use /get to get a page\n/index for directory listing\n/getCGI for hosting cgi script";
      t.sendResponseHeaders(200, response.length());
        try (OutputStream os = t.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
  }
  
 static class CGIHandler implements HttpHandler {
	    @Override
	    public void handle(HttpExchange t) throws IOException {
	    	String root = "/home/tasmia/Documents/Project1/Root/";
	    	String text;
	    	Headers h = t.getResponseHeaders();
	        h.add("Content-Type", "text/html");
	        File f = new File(root+"test.sh");
	        
				PrintWriter writer = new PrintWriter(root+"convert.html", "UTF-8");
				
				BufferedReader bf = new BufferedReader(new FileReader(f));
				//loop for parsing the cgi script
				while ((text = bf.readLine()) != null) {
			    	//skipping the cgi contents 			        
			        if(text.contains("#!/bin/bash") || text.contains("Content-type: text/html")||text.contains("cat")||text.contains("EOT")){
			        	
			        }else if(text.contains("echo")) {
			        	//if cgi has echo command, take the echo part out
			        	String[] parts = text.split(" ",2);
			        	//System.out.printf("******\nPart 1: %s  Part 2:%s", parts[0],parts[1]);
			        	writer.print("\n"+parts[1]);
			        }
			        else {
			        	//writing the parsed html contents in convert.html file
			        	writer.println(text);
			        }
			        
			    }
				writer.close();
				f=new File (root+"convert.html");
				//now passing the html that got generated from parsing the cgi file
				byte [] bytearray  = new byte [(int)f.length()];
			      FileInputStream fis = new FileInputStream(f);
			      BufferedInputStream bis = new BufferedInputStream(fis);
			      bis.read(bytearray, 0, bytearray.length);

			      // sending response of the parsed html file
			      t.sendResponseHeaders(200, f.length());
			      
			        try (OutputStream os = t.getResponseBody()) {
			            os.write(bytearray,0,bytearray.length);
			
	    }
}
  }

  static class GetHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
        String response="";
    	String root = "/home/tasmia/Documents/Project1/Root/";
    	
		
    	
      // add the required response header for a text/html file
      Headers h = t.getResponseHeaders();
      h.add("Content-Type", "text/html");
      File file = new File (root+"testPost.html");
      byte [] bytearray  = new byte [(int)file.length()];
      FileInputStream fis = new FileInputStream(file);
      BufferedInputStream bis = new BufferedInputStream(fis);
      bis.read(bytearray, 0, bytearray.length);
      t.sendResponseHeaders(200, file.length());

        try (OutputStream os = t.getResponseBody()) {
            os.write(bytearray,0,bytearray.length);
        }
    }
  }
      // Handler for '/test' context
    static class TestHandler implements HttpHandler {
 
        @Override
        public void handle(HttpExchange he) throws IOException {
            System.out.println("Serving the post request");
            String root = "/home/tasmia/Documents/Project1/Root/";

            // Serve for POST requests only
            if (he.getRequestMethod().equalsIgnoreCase("POST")) {
 
                try {
 
                    // REQUEST Headers
                    Headers requestHeaders = he.getRequestHeaders();
                    //mapping request headers
                    Set<Map.Entry<String, List<String>>> entries = requestHeaders.entrySet();
                    int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));
                    //read request body
                    InputStream is = he.getRequestBody();
                    byte[] data = new byte[contentLength];
                    int length = is.read(data);
                    // RESPONSE Headers
                    Headers responseHeaders = he.getResponseHeaders();
                    // Send RESPONSE Headers
                    he.sendResponseHeaders(HttpURLConnection.HTTP_OK, contentLength);
                    // RESPONSE Body
                    OutputStream os = he.getResponseBody();
                    String s = root+"testing.txt";
                    FileOutputStream fous=new FileOutputStream(s);
                    os.write(data);
                    fous.write(data);
                    he.close();
 
                } catch (NumberFormatException | IOException e) {
                }
            }
 
        }
    }
}