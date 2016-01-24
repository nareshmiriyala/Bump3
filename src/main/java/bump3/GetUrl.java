package bump3;

import java.io.*;
import java.net.*;

/** this class gets the text from a webpage and stores it in a string
 *  this class also has a method to get the filesize of an item on a website
 */
public class GetUrl {
	/** returns the text from a webpage (HTML or whatever)
	 *  @param theURL url of the website to visit, must begin with the protocol (http, ftp, etc)
	 *  @return the url's plain text
	 */
	public static String getURL(String theURL) {
		URL u;
		BufferedReader dis = null;
		String s, result = "";
		try {
			u = new URL(theURL);
			//u = new URL((URL)null, theURL, new HttpTimeoutHandler(10000));
			URLConnection uc = u.openConnection();
			uc.setConnectTimeout(Main.CONNECT_TIMEOUT);
			uc.setReadTimeout(Main.READ_TIMEOUT);
			uc.setRequestProperty("User-Agent", Main.USER_AGENT);
			
			/*if (Main.USE_COOKIES && !Main.COOKIE.equals("")) {
				String[] c = Main.COOKIE.split("; ");
				for (int i = 0; i < c.length; i++) {
					if (!c[i].trim().equals("")) {
						System.out.println("Setting cookie: " + c[i]);
						uc.setRequestProperty("Cookie", c[i]);
					}
				}
				Main.COOKIE = "";
			}*/
			
			dis = new BufferedReader(
					new InputStreamReader(
						uc.getInputStream()));
			
			while ((s = dis.readLine()) != null) {
				result = result + s + "\n";
			}
			
			/*//Main.COOKIE="";
			if (Main.USE_COOKIES) {
				String headerName = null;
				System.out.println("");
				for (int i=1; (headerName = uc.getHeaderFieldKey(i))!=null; i++) {
					if (headerName.equals("Set-Cookie")) {
						String cookie = uc.getHeaderField(i);
						// System.out.println("  COOKIE=" + cookie);
						cookie = cookie.substring(0, cookie.indexOf(";"));
						Main.COOKIE += cookie + "; ";
						String cookieName = cookie.substring(0, cookie.indexOf("="));
						String cookieValue = cookie.substring(cookie.indexOf("=") + 1, cookie.length());
					}
				}
			}*/
			
		} catch (MalformedURLException mue) {
			Methods.pv("MalformedURLException Occurred for \'" + theURL + "\'");
			// mue.printStackTrace();
			return "";
			
		} catch (SocketTimeoutException ste) {
			Methods.pv("SocketTimeoutException for \'" + theURL + "\'");
			//ste.printStackTrace();
			
		} catch (IOException ioe) {
			Methods.pv("IOException for \'" + theURL + "\'");
			// System.out.println("Error occurred!");
			// System.out.println(ioe.getMessage());
			//ioe.printStackTrace();
			return "";
			
		} finally {
			try {
				if (dis != null)
					dis.close();
			} catch (IOException ioe) {
			}
		} // end of finally
		return result;
	}  // end of getURL method
	
	/** returns how large a file on a website is
	 *  useful in checking if a link is valid
	 *  @param theURL url of the file to check
	 *  @return size of the file, -1 if file is not found
	*/
	public static int getFilesize(String theURL) {
		URL url;
	    URLConnection conn;
    	int size = -1;
		
    	try {
    		url = new URL(theURL);
			
    		conn = url.openConnection();
    		conn.setRequestProperty("User-Agent", Main.USER_AGENT);
    		// checking a site for a file can take a while...
			conn.setConnectTimeout(Main.CONNECT_TIMEOUT * 2);
			conn.setReadTimeout(Main.READ_TIMEOUT * 2);
			
			/*if (Main.USE_COOKIES && !Main.COOKIE.equals(""))
				conn.setRequestProperty("Cookie", Main.COOKIE);*/
			
    		size = conn.getContentLength();
    		conn.getInputStream().close();
    	} catch(FileNotFoundException fnfe) {
    		return -2;
    	} catch (ConnectException ce) {
    		return -3;
    	} catch (ProtocolException fpe) {
    		// Methods.p("[!] FTP Procol Exception");
    		return -2;
    	} catch (IOException ioe) {
    		// Methods.p("[!] Invalid HTTP response code");
    		return -2;
    	/*} catch (MalformedURLException mue) {
    		// we are attempted to access an invalid URL
    		// return as 'file not found' just to keep it simple
    		return -2;*/
    	} catch(Exception e) {
			e.printStackTrace();
		}
		
		return size;
	}
	
	public static String sendPostData(String url, String postdata) {
		URL u;
		BufferedReader dis = null;
		String s, result = "";
		
		try {
			u = new URL(url);
			//u = new URL((URL)null, theURL, new HttpTimeoutHandler(10000));
			URLConnection uc = u.openConnection();
			uc.setRequestProperty("User-Agent", Main.USER_AGENT);
			uc.setConnectTimeout(Main.CONNECT_TIMEOUT);
			uc.setReadTimeout(Main.READ_TIMEOUT);
			
			uc.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(uc.getOutputStream());
			wr.write(postdata);
			wr.close();
			
			dis = new BufferedReader(
					new InputStreamReader(
						uc.getInputStream()));
			
			while ((s = dis.readLine()) != null) {
				result = result + s + "\n";
			}
			
			dis.close();
			
		} catch (MalformedURLException mue) {
			System.out.println("MalformedURLException Occurred for \'" + url + "\'");
			// mue.printStackTrace();
			return "";
			
		} catch (SocketTimeoutException ste) {
			//ste.printStackTrace();
			
		} catch (IOException ioe) {
			// System.out.println("Error occurred!");
			// System.out.println(ioe.getMessage());
			//ioe.printStackTrace();
			return "";
			
		} // end of try
		
		return result;
	}
	
} // end of class
