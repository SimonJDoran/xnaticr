
package shellCommands;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

/**
 *
 * @author simond
 */
public class ProxyTests
{
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
   {
		ArrayList<String> servers = new ArrayList<>();
		servers.add("https://xnatcrukarchive.icr.ac.uk:8443/XNAT_CRUK_ARCHIVE");
		servers.add("https://xnatcruk.icr.ac.uk/XNAT_CRUK_ARCHIVE");
		
		for (String server : servers)
		{
			System.out.println("\n\nTests on server " + server);
			
			System.out.println("\nTest 1: authentication, no chunking");
			String JSessionID = testAuthenticate(server, false, false);
			System.out.println("JSESSIONID = " + JSessionID);
			
         System.out.println("\nTest 3: authentication, explicit setting of Content Length");
			JSessionID = testAuthenticate(server, false, true);
			System.out.println("JSESSIONID = " + JSessionID);
         
         System.out.println("\nTest 2: authentication, chunking");
			JSessionID = testAuthenticate(server, true, false);
			System.out.println("JSESSIONID = " + JSessionID);
         
			
			System.out.println("\nTest 4: GET");
			String outputXml = testGet(server, JSessionID);
			System.out.println("Project XML = " + outputXml.substring(0, 100) + "...");
		}
	}
	
	private static String testAuthenticate(String server, boolean useChunkedStreaming, boolean setContentLength)
	{
      InputStream bis;
		
	// Authenticate and return JSESSIONID token.
		String namePasswordEncoded = new base64.Base64().encodeToString("testuser:test".getBytes()).trim();
		String auth = "Basic " + namePasswordEncoded;	
		
		try
		{
			URL restUrl = new URL(server + "/data/JSESSION");	
				
			HttpURLConnection connection = (HttpURLConnection) restUrl.openConnection();
			
			connection.setDoOutput(true);
         connection.setDoInput(true);
         connection.setRequestMethod("POST");
			connection.setRequestProperty("Authorization", auth);
			connection.setConnectTimeout(5000); // 5 seconds
			
         String urlWithoutHost = restUrl.getPath();
         int contentLength = restUrl.getPath().length();
			if (useChunkedStreaming) connection.setChunkedStreamingMode(-1);
         if (setContentLength)    connection.setRequestProperty("Content-Length", Integer.toString(contentLength));
			
			connection.connect();
         int    responseCode    = connection.getResponseCode();
         String responseMessage = connection.getResponseMessage();
			
			System.out.println("Response code = "    + responseCode);
			System.out.println("Response message = " + responseMessage);
         
         bis = new BufferedInputStream(connection.getInputStream());
		}
		catch (SocketTimeoutException exST)
      {
         System.out.println("The attempted connection to timed out after 5 s.");
			return null;
      }

      catch (IOException | IllegalStateException ex)
      {
         System.out.println("Unexpected response from server:" + ex.getMessage());
			return null;
      }
		
		return getOutput(bis);

   }
	
	
	private static String testGet(String server, String JSessionID)
	{
      InputStream bis;

		try
		{
			URL restUrl = new URL(server + "/data/archive/projects?format=xml");	
				
			HttpURLConnection connection = (HttpURLConnection) restUrl.openConnection();
			
			connection.setDoOutput(true);
         connection.setDoInput(true);
         connection.setRequestMethod("GET");
			connection.setRequestProperty("Cookie", "JSESSIONID=" + JSessionID);
			connection.setConnectTimeout(5000); // 5 seconds
			
			connection.connect();
         int    responseCode    = connection.getResponseCode();
         String responseMessage = connection.getResponseMessage();
			
			System.out.println("Response code = "    + responseCode);
			System.out.println("Response message = " + responseMessage);
         
         bis = new BufferedInputStream(connection.getInputStream());
		}
		catch (SocketTimeoutException exST)
      {
         System.out.println("The attempted connection to timed out after 5 s.");
			return null;
      }

      catch (IOException | IllegalStateException ex)
      {
         System.out.println("Unexpected response from server:" + ex.getMessage());
			return null;
      }
		
		return getOutput(bis);

   }
	
	
	private static String getOutput(InputStream is)
	{
		try
      {
         StringBuilder sb = new StringBuilder();
         int           b;
         while ((b = is.read()) != -1) sb.append((char) b);
         
			return sb.toString();
      }
		catch (IOException exIO)
		{
			return "Failed to retrieve String output";
		}
	}
	

	
}
