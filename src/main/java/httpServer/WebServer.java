// Specify the package name for this Java file
package httpServer;

// Import necessary Java I/O and networking classes
import java.io.*;
import java.net.*;

// Define a class named WebServer
class WebServer {
    public static void main(String args[]) {
        // Check if the program is run with a specified port number
        if (args.length != 1) {
            System.out.println("Usage: WebServer <port>");
            System.exit(1); // Exit the program with an error code
        }
        // Create an instance of the WebServer class with the specified port
        WebServer w_server = new WebServer(Integer.parseInt(args[0]));
    }

    // Constructor for the WebServer class that takes a port number
    public WebServer(int port) {
        ServerSocket w_server = null; // Declare a ServerSocket object
        Socket w_sock = null; // Declare a Socket object
        InputStream w_in_stream = null; // Declare an InputStream
        OutputStream out = null; // Declare an OutputStream
    }

    // Function to create a response to an HTTP request
    public byte[] createResponse(InputStream w_inStream) {
        byte[] response = null; // Initialize a byte array for the response
        BufferedReader w_in_stream = null; // Declare a BufferedReader
        try {
            w_in_stream = new BufferedReader(new InputStreamReader(w_inStream, "UTF-8"));
            String w_file_name_rqst = null; // Initialize a variable for the requested w_file_name_rqst
            boolean w_done_rqst = false; // Initialize a flag to track the end of the request
            while (!w_done_rqst) {
                String line = w_in_stream.readLine(); // Read a line from the input
                System.out.println("Received: " + line); // Print the received line
                if (line == null || line.equals("")) // Check if the line is null or empty
                    w_done_rqst = true; // Set the w_done_rqst flag to true
                else if (line.startsWith("GET")) {
                    int firstSpace = line.indexOf(" "); // Find the index of the first space
                    int secondSpace = line.indexOf(" ", firstSpace + 1); // Find the index of the second space
                    w_file_name_rqst = line.substring(firstSpace + 2, secondSpace); // Extract the requested w_file_name_rqst
                }
            }
            System.out.println("FINISHED\n"); // Print a message to indicate the end of request parsing
            if (w_file_name_rqst == null) {
                response = "<html>Illegal request: no GET</html>".getBytes(); // If no w_file_name_rqst is specified, set an
                                                                              // error response
            } else {
                File file = new File(w_file_name_rqst); // Create a File object for the requested file
                if (!file.exists()) {
                    response = ("<html>File not found: " + w_file_name_rqst + "</html>").getBytes(); // If the file doesn't
                                                                                             // exist, set a "not found"
                                                                                             // response
                } else {
                    response = readFileInBytes(file); // Read the file and set it as the response
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Print the exception stack trace
            response = ("<html>ERROR: " + e.getMessage() + "</html>").getBytes(); // Set an error response with the
                                                                                  // exception message
        }
        return response; // Return the response byte array
    }

    // Function to read a file into a byte array
    public static byte[] readFileInBytes(File f) throws IOException {
        FileInputStream file = new FileInputStream(f); // Create a FileInputStream for the file
        ByteArrayOutputStream data = new ByteArrayOutputStream(file.available()); // Create a ByteArrayOutputStream
        byte buffer[] = new byte[512]; // Create a byte buffer
        int numRead = file.read(buffer); // Read data into the buffer
        while (numRead > 0) {
            data.write(buffer, 0, numRead); // Write data from the buffer to the ByteArrayOutputStream
            numRead = file.read(buffer); // Read more data into the buffer
        }
        file.close(); // Close the FileInputStream
        byte[] result = data.toByteArray(); // Convert the ByteArrayOutputStream to a byte array
        data.close(); // Close the ByteArrayOutputStream
        return result; // Return the byte array containing the file data
    }
}
