package funHttpServer;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Map;
import java.util.LinkedHashMap;
import java.nio.charset.Charset;
import java.lang.*;

class WebServer {
  // Main method to start the WebServer
  public static void main(String args[]) {
    // Create a WebServer instance with port 8888
    WebServer server = new WebServer(8888);
  }

  // Constructor for the WebServer
  public WebServer(int port) {
    ServerSocket server = null;
    Socket sock = null;
    InputStream in = null;
    OutputStream out = null;
    try {
      // Create a ServerSocket bound to the specified port
      server = new ServerSocket(port);
      while (true) {
        // Accept incoming client connections
        sock = server.accept();
        out = sock.getOutputStream();
        in = sock.getInputStream();
        // Generate a response and send it to the client
        byte[] response = createResponse(in);
        out.write(response);
        out.flush();
        in.close();
        out.close();
        sock.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (sock != null) {
        try {
          server.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  // HashMap to store image URLs with associated keys
  private final static HashMap<String, String> _images = new HashMap<>() {
    {
      put("streets", "https://iili.io/JV1pSV.jpg");
      put("bread", "https://iili.io/Jj9MWG.jpg");
    }
  };

  // Random number generator for generating random values
  private Random random = new Random();

  public byte[] createResponse(InputStream inStream) {
    // Initialize a byte array to store the response
    byte[] response = null;

    // Initialize a BufferedReader to read from the input stream
    BufferedReader in = null;

    try {
      // Create a BufferedReader with UTF-8 encoding for the input stream
      in = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));

      // Initialize a string to store the HTTP request
      String request = null;

      // Initialize a flag to indicate when reading is done
      boolean done = false;

      // Read lines from the input stream
      while (!done) {
        // Read a line from the input stream
        String line = in.readLine();

        // Print the received line to the console for debugging
        System.out.println("Received: " + line);

        // Check if the line is null or empty to mark reading as done
        if (line == null || line.equals(""))
          done = true;
        // Check if the line starts with "GET" to extract the request
        else if (line.startsWith("GET")) {
          // Find the positions of the first and second spaces in the line
          int firstSpace = line.indexOf(" ");
          int secondSpace = line.indexOf(" ", firstSpace + 1);

          // Extract the request path from the line
          request = line.substring(firstSpace + 2, secondSpace);
        }
      }

      System.out.println("FINISHED PARSING HEADER\n");
      if (request == null) {
        response = "<html>Illegal request: no GET</html>".getBytes();
      } else {
        StringBuilder w_builder_string = new StringBuilder();
        if (request.length() == 0) {
          String page = new String(readFileInBytes(new File("www/root.html")));
          page = page.replace("${links}", buildFileList());
          w_builder_string.append("HTTP/1.1 200 OK\n");
          w_builder_string.append("Content-Type: text/html; charset=utf-8\n");
          w_builder_string.append("\n");
          w_builder_string.append(page);
          w_builder_string.append("Listed below are additional options you can choose from");
          w_builder_string.append("\n");
          w_builder_string.append("You can enter profile? as an option, copy paste the below example");
          w_builder_string.append("\n");
          w_builder_string.append("/profile?Name=JohnDoe&Birthday=09/09/1999");
          w_builder_string.append("\n");
          w_builder_string.append("You can enter repeat? as an option, copy paste the below example");
          w_builder_string.append("\n");
          w_builder_string.append("/repeat?string=ThisIsAString&num=30");
          w_builder_string.append("\n");
          w_builder_string.append(
              "You can enter github?query=users/amehlhase316/repos (or other GitHub repo owners) will lead to receiving JSON which will for now only be printed in the console. See the todo below");
          w_builder_string.append("\n");
        } else if (request.equalsIgnoreCase("json")) {
          int index = random.nextInt(_images.size());
          String header = (String) _images.keySet().toArray()[index];
          String url = _images.get(header);
          w_builder_string.append("HTTP/1.1 200 OK\n");
          w_builder_string.append("Content-Type: application/json; charset=utf-8\n");
          w_builder_string.append("\n");
          w_builder_string.append("{");
          w_builder_string.append("\"header\":\"").append(header).append("\",");
          w_builder_string.append("\"image\":\"").append(url).append("\"");
          w_builder_string.append("}");
        } else if (request.equalsIgnoreCase("random")) {
          File w_file_stream = new File("www/index.html");
          w_builder_string.append("HTTP/1.1 200 OK\n");
          w_builder_string.append("Content-Type: text/html; charset=utf-8\n");
          w_builder_string.append("\n");
          w_builder_string.append(new String(readFileInBytes(w_file_stream)));
        } else if (request.contains("file/")) {
          File w_file_stream = new File(request.replace("w_file_stream/", ""));
          if (w_file_stream.exists()) {
            w_builder_string.append("HTTP/1.1 200 OK\n");
            w_builder_string.append("Content-Type: text/html; charset=utf-8\n");
            w_builder_string.append("\n");
            w_builder_string.append(new String(readFileInBytes(w_file_stream)));
          } else {
            w_builder_string.append("HTTP/1.1 404 Not Found\n");
            w_builder_string.append("Content-Type: text/html; charset=utf-8\n");
            w_builder_string.append("\n");
            w_builder_string.append("File not found: " + w_file_stream);
          }
        } else if (request.contains("repeat?")) {
          try {
            Map<String, String> query_pairs = new LinkedHashMap<String, String>();
            query_pairs = splitQuery(request.replace("repeat?", ""));
            String string = "";
            int num = 0;
            try {
              string = query_pairs.get("string");
            } catch (Exception e) {
              w_builder_string.append(
                  "400 Bad Request The server cannot or will not process the request due to something that is perceived to be a client error (e.g., malformed request syntax, invalid request message framing, or deceptive request routing).");
              w_builder_string.append("\n");
              w_builder_string.append("Please enter a valid input for string");
            }
            try {
              num = Integer.parseInt(query_pairs.get("num"));
            } catch (Exception e) {
              w_builder_string.append(
                  "400 Bad Request The server cannot or will not process the request due to something that is perceived to be a client error (e.g., malformed request syntax, invalid request message framing, or deceptive request routing).");
              w_builder_string.append("\n");
              w_builder_string.append("Please enter a valid input for num");
              System.out.println("Please enter a valid input for num");
            }
            System.out.println("String: " + string);
            System.out.println("Num: " + num);
            if ((string == null) || (string == "")) {
              w_builder_string.append("\n");
              w_builder_string.append(
                  "400 Bad Request The server cannot or will not process the request due to something that is perceived to be a client error (e.g., malformed request syntax, invalid request message framing, or deceptive request routing).");
              w_builder_string.append("Please enter a valid string");
            }
            if (num <= 0) {
              w_builder_string.append("\n");
              w_builder_string.append(
                  "400 Bad Request The server cannot or will not process the request due to something that is perceived to be a client error (e.g., malformed request syntax, invalid request message framing, or deceptive request routing).");
              w_builder_string.append("Please enter a valid number. Num times must be greater than zero. ");
            }
            if ((string != null) && (num >= 0)) {
              w_builder_string.append(
                  "200 OK The request succeeded. The result meaning of success depends on the HTTP method: GET: The resource has been fetched and transmitted in the message body.");
              w_builder_string.append("\n");
              for (int i = 0; i < num; i++) {
                System.out.println(string + " ");
                w_builder_string.append(" " + string);
                w_builder_string.append(" ");
              }
            }
          } catch (Exception e) {
            w_builder_string.append("\n");
            w_builder_string.append("Client Error. Error Code 400\n");
            w_builder_string.append("Please enter a valid input. Format /repeat?word=anyWord&num=anyNumber");
            System.out.println("Caught an error");
          }
        } else if (request.contains("multiply?")) {
          try {
            Map<String, String> query_pairs = new LinkedHashMap<String, String>();
            query_pairs = splitQuery(request.replace("multiply?", ""));
            int num1 = 0;
            int num2 = 0;
            num1 = Integer.parseInt(query_pairs.get("num1"));
            num2 = Integer.parseInt(query_pairs.get("num2"));
            Integer result = num1 * num2;
            w_builder_string.append("\n");
            w_builder_string.append(
                "200 OK The request succeeded. The result meaning of success depends on the HTTP method: GET: The resource has been fetched and transmitted in the message body.");
            w_builder_string.append("\n");
            w_builder_string.append("Result is: " + result);
          } catch (Exception e) {
            w_builder_string.append(
                "400 Bad Request The server cannot or will not process the request due to something that is perceived to be a client error (e.g., malformed request syntax, invalid request message framing, or deceptive request routing).");
            w_builder_string.append("\n");
            w_builder_string.append("Please enter valid input in the future");
          }
        } else if (request.contains("profile?")) {
          try {
            // Create a LinkedHashMap to store key-value pairs from the query
            Map<String, String> query_pairs = new LinkedHashMap<String, String>();

            // Call the splitQuery function to parse the query string and remove "profile?"
            // prefix
            query_pairs = splitQuery(request.replace("profile?", ""));

            // Initialize strings to store Name and Birthday values
            String Name = "";
            String Birthday = "";

            // Retrieve Name and Birthday values from the parsed query
            Name = query_pairs.get("Name");
            Birthday = query_pairs.get("Birthday");

            // Check if Name or Birthday are empty
            if (Name.equals("") || Birthday.equals("")) {
              // If either is empty, indicate a 400 error and request valid data
              w_builder_string.append("400 error. Please enter a valid data");
            } else {
              // Append an HTTP response status line with "200 OK"
              w_builder_string.append("HTTP/1.1 200 OK\n");

              // Append a content type header specifying HTML with UTF-8 charset
              w_builder_string.append("Content-Type: text/html; charset=utf-8\n");

              // Append blank lines for proper HTTP response formatting
              w_builder_string.append("\n");
              w_builder_string.append("\n");

              // Append a line indicating the extracted Name value
              w_builder_string.append("Your Name is: " + Name);

              // Append a newline character
              w_builder_string.append("\n");

              // Append a line indicating the extracted Birthday value
              w_builder_string.append("Your Birthday is: " + Birthday);

              // Append a newline character
              w_builder_string.append("\n");

            }
          } catch (Exception e) {
            // Append newline for formatting
            w_builder_string.append("\n");

            // Append a line indicating a client error with status code 400
            w_builder_string.append("Client Error. Error Code 400\n");

            // Append newline for formatting
            w_builder_string.append("\n");

            // Append a message instructing the user to enter a valid input with the
            // specified format
            w_builder_string.append("Please enter a valid input. Format: profile?Name=X&Birthday=Y \n");

            // Print a message to the console to indicate that a 400 error was caught
            System.out.println("Caught a 400 error");

          }
        } else if (request.contains("github?")) {
          try {
            // Create a LinkedHashMap to store key-value pairs from the query
            Map<String, String> query_pairs = new LinkedHashMap<String, String>();

            // Call the splitQuery function to parse the query string
            query_pairs = splitQuery(request.replace("github?", ""));

            // Fetch JSON data from a GitHub API endpoint based on the "query" parameter
            String json = fetchURL("https://api.github.com/" + query_pairs.get("query"));

            // Debugging output to print the retrieved JSON data
            System.out.println(json);

            // Initialize a string to store the full repository name
            String fullRepoName = "";

            // Loop through the JSON string character by character
            for (int i = 0; i < json.length(); i++) {
              // Check for the presence of a key called "full_name" in the JSON string
              if ((json.charAt(i) == 'f') && (json.charAt(i + 1) == 'u') && (json.charAt(i + 2) == 'l')
                  && (json.charAt(i + 3) == 'l') && (json.charAt(i + 4) == '_') &&
                  (json.charAt(i + 5) == 'n') && (json.charAt(i + 6) == 'a') && (json.charAt(i + 7) == 'm')
                  && (json.charAt(i + 8) == 'e') && (json.charAt(i + 9) == '"') &&
                  (json.charAt(i + 10) == ':') && (json.charAt(i + 11) == '"')) {
                // Move the index to the value associated with "full_name"
                i = i + 12;

                // Extract the value associated with the "full_name" key
                while (json.charAt(i) != '"') {
                  fullRepoName += json.charAt(i);
                  i++;
                }
                fullRepoName += ' ';
              }
            }

            // Debugging output
            System.out.println("should be printing fullRepoName " + fullRepoName);

            // Initialize a string to store login owners of each repo
            String loginOwnerOfEachRepo = "";

            // Loop through the JSON string character by character
            for (int i = 0; i < json.length(); i++) {
              // Check for the presence of a key called "id" in the JSON string
              if ((json.charAt(i) == '"') && (json.charAt(i + 1) == 'i') && (json.charAt(i + 2) == 'd')
                  && (json.charAt(i + 3) == '"') && (json.charAt(i + 4) == ':')) {
                // Move the index to the value associated with "id"
                i = i + 5;

                // Extract the value associated with the "id" key
                while (json.charAt(i) != ',') {
                  loginOwnerOfEachRepo += json.charAt(i);
                  i++;
                }
                loginOwnerOfEachRepo += ' ';
              }
            }

            System.out.println("");
            System.out.println("should be printing loginOwnerOfEachRepo " + loginOwnerOfEachRepo);
            String repoIDS = "";
            // Loop through the JSON string character by character
            for (int i = 0; i < json.length(); i++) {
              // Check for the presence of a key called "login" in the JSON string
              if ((json.charAt(i) == '"') && (json.charAt(i + 1) == 'l') && (json.charAt(i + 2) == 'o')
                  && (json.charAt(i + 3) == 'g') && (json.charAt(i + 4) == 'i') &&
                  (json.charAt(i + 5) == 'n') && (json.charAt(i + 6) == '"') && (json.charAt(i + 7) == ':')
                  && (json.charAt(i + 8) == '"')) {
                // Move the index to the value associated with "login"
                i = i + 9;

                // Extract the value associated with the "login" key
                while ((json.charAt(i) != '"') && (json.charAt(i) != ',')) {
                  repoIDS += json.charAt(i);
                  i++;
                }
                repoIDS += ' ';
              }
            }
            System.out.println("Print debug data");
            System.out.println("repoIDS " + repoIDS);
            // Append an HTTP response status line with "200 OK"
            w_builder_string.append("HTTP/1.1 200 OK\n");

            // Append blank lines for proper HTTP response formatting
            w_builder_string.append("\n");
            w_builder_string.append("\n");
            w_builder_string.append("\n");

            // Append "Full Repo Names: " followed by the value of fullRepoName
            w_builder_string.append("Full Repo Names: ");
            w_builder_string.append(fullRepoName);

            // Append a newline character
            w_builder_string.append("\n");

            // Append "Repo IDS: " followed by the value of repoIDS
            w_builder_string.append("Repo IDS: ");
            w_builder_string.append(repoIDS);

            // Append a newline character
            w_builder_string.append("\n");

            // Append "Login Owner Of Each Repo: " followed by the value of
            // loginOwnerOfEachRepo
            w_builder_string.append("Login Owner Of Each Repo: ");
            w_builder_string.append(loginOwnerOfEachRepo);

          } catch (Exception e) {
            w_builder_string.append("400 error incorrect query");
            w_builder_string.append("Please enter proper query in the future");
          }
        } else {
          w_builder_string.append("HTTP/1.1 400 Bad Request\n");
          w_builder_string.append("Content-Type: text/html; charset=utf-8\n");
          w_builder_string.append("\n");
          w_builder_string.append("I am not sure what you want me to do...");
        }
        response = w_builder_string.toString().getBytes();
      }
    } catch (

    IOException e) {
      e.printStackTrace();
      response = ("<html>ERROR: " + e.getMessage() + "</html>").getBytes();
    }
    return response;
  }

  public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
    // Create a LinkedHashMap to store key-value pairs
    Map<String, String> query_pairs = new LinkedHashMap<>();
    // Split the query string into individual key-value pairs
    String[] pairs = query.split("&");
    for (String pair : pairs) {
      // Find the index of the equal sign to separate the key and value
      int idx = pair.indexOf("=");
      // Decode and put the key and value into the map
      query_pairs.put(
          URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
          URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
    }
    // Return the map of key-value pairs
    return query_pairs;
  }

  public static String buildFileList() {
    // Create an ArrayList to store filenames
    ArrayList<String> w_filenames = new ArrayList<>();
    // Specify the directory path
    File directoryPath = new File("www/");
    // Add the filenames in the directory to the ArrayList
    w_filenames.addAll(Arrays.asList(directoryPath.list()));
    // Check if there are filenames in the list
    if (w_filenames.size() > 0) {
      // Create a StringBuilder to build an HTML list
      StringBuilder w_builder_string = new StringBuilder();
      // Add an opening unordered list tag
      w_builder_string.append("<ul>\n");
      // Iterate through the filenames and add them as list items
      for (var filename : w_filenames) {
        w_builder_string.append("<li>" + filename + "</li>");
      }
      // Add a closing unordered list tag
      w_builder_string.append("</ul>\n");
      // Return the HTML list as a string
      return w_builder_string.toString();
    } else {
      // Return a message indicating no files in the directory
      return "No files in directory";
    }
  }

  public static byte[] readFileInBytes(File f) throws IOException {
    // Create a FileInputStream for the specified w_file_stream
    FileInputStream w_file_stream = new FileInputStream(f);
    // Create a ByteArrayOutputStream with initial capacity based on available bytes
    // in the w_file_stream
    ByteArrayOutputStream data = new ByteArrayOutputStream(w_file_stream.available());
    // Create a byte buffer to read data from the w_file_stream
    byte buffer[] = new byte[512];
    int numRead = w_file_stream.read(buffer);
    // Read data from the w_file_stream and write it to the ByteArrayOutputStream
    while (numRead > 0) {
      data.write(buffer, 0, numRead);
      numRead = w_file_stream.read(buffer);
    }
    // Close the w_file_stream input stream
    w_file_stream.close();
    // Convert the ByteArrayOutputStream to a byte array
    byte[] result = data.toByteArray();
    // Close the ByteArrayOutputStream
    data.close();
    // Return the byte array containing the w_file_stream data
    return result;
  }

  public String fetchURL(String aUrl) {
    // Create a StringBuilder to store the fetched content
    StringBuilder w_sb = new StringBuilder();
    URLConnection w_conn = null;
    InputStreamReader in = null;
    try {
      // Create a URL object from the specified URL string
      URL url = new URL(aUrl);
      // Open a connection to the URL
      w_conn = url.openConnection();
      // Set a read timeout for the connection (20 seconds)
      if (w_conn != null)
        w_conn.setReadTimeout(20 * 1000);
      // Check if the connection and input stream are available
      if (w_conn != null && w_conn.getInputStream() != null) {
        // Create an InputStreamReader from the input stream with the default character
        // encoding
        in = new InputStreamReader(w_conn.getInputStream(), Charset.defaultCharset());
        // Create a BufferedReader for reading from the InputStreamReader
        BufferedReader br = new BufferedReader(in);
        if (br != null) {
          int ch;
          // Read characters from the BufferedReader and append them to the StringBuilder
          while ((ch = br.read()) != -1) {
            w_sb.append((char) ch);
          }
          // Close the BufferedReader
          br.close();
        }
      }
      // Close the input stream
      in.close();
    } catch (Exception ex) {
      System.out.println("Exception in url request:" + ex.getMessage());
    }
    // Return the fetched content as a string
    return w_sb.toString();
  }
}