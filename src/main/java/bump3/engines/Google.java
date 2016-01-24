package bump3.engines;
import bump3.Downloader;
import bump3.GetUrl;
import bump3.Main;
import bump3.Methods;
public class Google {
	public static String FILTER = "+-null3d+-listen77+-vmp3.eu+-indexofmp3.net" +
								  "+-doxic.com+-mp3globe.net+-musicbag.org+-mp3bot.ws";
	
	public static void p(String txt) {
		Methods.p(txt);
	}
	public static int search(String theArtist, String theTitle) {
		// convert spaces to plus signs for the URL, also, lowercase!
		String artist = theArtist.replaceAll(" ", ".");
		artist = artist.toLowerCase();
		String title = theTitle.replaceAll(" ", ".");
		title = title.toLowerCase();
		
		// lower google search's timeout length
		// some sites are LAGTASTIC and we need to avoid them!
		int oldconnect = Main.CONNECT_TIMEOUT;
		int oldread    = Main.READ_TIMEOUT;
		Main.CONNECT_TIMEOUT = 5000;
		Main.READ_TIMEOUT    = 3000;
		
		String theURL = "http://www.google.com/search?hl=en&safe=off&q=";
		theURL += "\"parent+directory\"+(MP3|OGG)+" + artist + "." + title + "+-html+-htm+-download+-links";
		theURL += FILTER;
		
		Methods.p(Main.GR+"[+]"+Main.GR+" Loading "+Main.G+"Google"+Main.GR+" search results... ");
		Methods.status("loading google");
		
		// use the GetUrl class to grab the HTML from the webpage
		String page = GetUrl.getURL(theURL);
		
		if (page.equals("")) { // if google returned nothing...
			p(Main.GR+"[+]"+Main.R+" Google returned invalid search results"+Main.W);
			//p(Main.R+"Invalid results");
			Main.CONNECT_TIMEOUT = oldconnect;
			Main.READ_TIMEOUT    = oldread;
			return -1; // return error status
		}
		
		//output google results to a file (for further investigation)
		/*try {
			FileWriter fw = new FileWriter("google.htm");
			fw.write(page);
			fw.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}*/
		
		Methods.status("searching google");
		String SEARCHSTRING = "/url?q=";
		int SEARCHLENGTH    = 7;
		
		// istart is the starting location of a search result, istop is the ending location of the same url
		int istart = 0, istop = 0;
		int pagecount = 0;
		do {
			// istart = page.indexOf("<h3 class=\"r\"><a href=\"", 0);
			istart = page.indexOf(SEARCHSTRING);
			if (istart < 0) {
				SEARCHSTRING = "<h3 class=\"r\"><a href=\"";
				SEARCHLENGTH = 23;
			}
			istart = page.indexOf(SEARCHSTRING, 0);
			while (istart >= 0) {
				if (Methods.GuiStop()) {
					Main.CONNECT_TIMEOUT = oldconnect;
					Main.READ_TIMEOUT    = oldread;
					return -1;
				}
				
				istop = page.indexOf("\"", istart + SEARCHLENGTH + 1);
				
				// mid is the text between the ahref=" and the next "
				// it's the link we want from the google results
				String mid = page.substring(istart + SEARCHLENGTH, istop);
				
				if (mid.indexOf("interstitial") >= 0 || mid.substring(10, mid.length() - 1).indexOf("/") < 0 ) {
					// istart = page.indexOf("<h3 class=\"r\"><a href=\"", istart + 1);
					istart = page.indexOf(SEARCHSTRING, istart + 1);
					continue;
				}
				
				if (mid.contains("&amp;sa="))
					mid = mid.substring(0, mid.indexOf("&amp;sa="));
				mid = mid.replace("&amp;", "&");
				
				if (tryToDownload(
						mid, 
						artist.replaceAll("[^a-zA-Z0-9]", ""), 
						title.replaceAll("[^a-zA-Z0-9]", ""), 
						theArtist + " - " + theTitle)) {
					// if we successfully downloaded
					Main.CONNECT_TIMEOUT = oldconnect;
					Main.READ_TIMEOUT    = oldread;
					return 1;
				}
				
				// look up the next link to perpetuate the loop
				// istart = page.indexOf("<h3 class=\"r\"><a href=\"", istart + 1);
				istart = page.indexOf(SEARCHSTRING, istart + 1);
			}
			
			pagecount += 10;
			theURL = "http://www.google.com/search?hl=en&safe=off&q=";
			theURL += "\"parent+directory\"+(MP3|OGG)+" + artist + "." + title + "+-html+-htm+-download+-links";
			theURL += FILTER;
			theURL += "&start=" + pagecount;
			
			if (page.indexOf("&amp;start="+pagecount) < 0)
				break;
			
			Methods.p(Main.GR+"[+]"+Main.GR+" Loading "+Main.G+"Google"+Main.GR+" search results (page " + 
							(1+(pagecount / 10)) + ")... ");
			
			page = GetUrl.getURL(theURL);
			if (page.equals(""))
				break;
		} while (1 == 1);
		
		Main.CONNECT_TIMEOUT = oldconnect;
		Main.READ_TIMEOUT    = oldread;
		
		return 0;
	} // end of search google method
	
	/** downloads theURL's url and looks through every link on the page
	 *  looks to see if any links contain both the 'artist' and 'title' names
	 *  @param theURL url of the page to look through
	 *  @param artist name of the artist we are looking for
	 *  @param title title of the song we want
	 *  @param artistTitle format of the artist and title we are going to save the song as
	 *  @return true if download is successful, false otherwise
	 */
	public static boolean tryToDownload(String theURL, String artist, String title, String artistTitle) {
		if (theURL.length() > 20)
			p(Main.GR+"[+]"+Main.GR+" Loading page: " +Main.B+ theURL.substring(0,20) +Main.GR+ "..." + Main.W);
		
		else
			p(Main.GR+"[+]"+Main.GR+" Loading page: " +Main.B+ theURL + Main.GR+"..." + Main.W);
		
		String page = GetUrl.getURL(theURL);
		
		// istart = where ahref="http://site.com" begins; istop = where the ahref="..." ends
		int istart = 0, istop = 0;
		
		istart = page.indexOf("a href=\"", 0);
		while (istart >= 0) {
			if (Methods.GuiStop())
				return false;
			istop = page.indexOf("\"", istart + 10);
			
			// mid is the text between the ahref=" and the next "
			// it's the next link we want to look at
			String mid = page.substring(istart + 8, istop);
			if (mid.length() < 4) {
				istart = page.indexOf("a href=\"", istart + 8);
				continue;
			}
			if ( ( mid.substring(mid.length() - 3).equals("mp3") && Main.SEARCH_MP3) || 
				 ( mid.substring(mid.length() - 3).equals("ogg") && Main.SEARCH_OGG) ) {
				// link ends in mp3 or ogg...
				String tempmid;
				
				String domain = theURL.substring(0, theURL.indexOf("/", 10));
				
				// tempmid is the stripped url we're looking at
				tempmid = Methods.hexToString(mid);
				tempmid = tempmid.replaceAll("[^a-zA-Z0-9]", "");
				tempmid = tempmid.toLowerCase();
				
				//if (tempmid.indexOf(artist) >=0 && tempmid.indexOf(title) >= 0) {
				Methods.pv(Main.GR + "[+] Looking through " +
							Main.G + tempmid + Main.GR + " for " +
							Main.G + artist + Main.GR + " and " + 
							Main.G + title + Main.GR + "");
				if (tempmid.indexOf(artist + title) >= 0) {
					// url contains artist and title next to each other
					
					// add proper domain/path to url if need be
					if ( mid.substring(0, 1).equals("/") )
						mid = domain + mid;
					else if ( mid.substring(0, 7).equals("http://") ) {
						// nothing
					} else if ( mid.substring(0, 6).equals("ftp://") )  {
						// nothing
					} else
						mid = theURL + mid;
					
					mid = mid.replaceAll(" ", "%20");
					
					if (mid.length() > 25)
						Methods.pr(Main.GR+"[+]"+Main.GR+" Checking    " + 
								Main.B+mid.substring(0, 25) +Main.GR+ "... " + Main.W);
								
					else
						Methods.pr(Main.GR+"[+]"+Main.GR+" Checking    " + 
								Main.BR+mid +Main.GR+ "... " + Main.W);
					
					int size = GetUrl.getFilesize(mid);
					if (Methods.GuiStop())
						return false;
						
					if (size == -2) {
						p(Main.R+"Not found.");
					} else if (size == -3) {
						p(Main.R+"Unable to connect");
					} else if (size >= Main.MIN_FILESIZE) {
						p(Main.GR+"[+]"+Main.G+ " Found song; "+Main.GR+"Downloading..."+Main.W);
						p(Main.GR+"[+]"+Main.GR+" Source:     " +Main.B+mid + Main.W);
						p(Main.GR+"[+]"+Main.GR+" Desination: " +Main.GR+
							Main.SAVE_DIR + artistTitle + "." + mid.substring(mid.length() - 3) + Main.W);
						
						int dl = Downloader.download(
								mid, // URL
								Main.SAVE_DIR + artistTitle + "." + mid.substring(mid.length() - 3),
								/*Main.SAVE_DIR + 
									artist + "-" + 
									title + "." + 
									mid.substring(mid.length() - 3),  // file extension*/
								size
						);
						
						if (dl >= 0)
							return true;
					} else {
						p(Main.GR+"[-]"+Main.R+"Filesize is too small (" +size+" < "+Main.MIN_FILESIZE+")"+Main.W);
					}
					
				} else {
					// p("[-] Artist (" + artist + ") and title(" + title + ") were not found in url (" + tempmid + ")");
				}
				
			} else {
				// p("    [-] Doesn't end in mp3 or ogg.");
			}
			istart = page.indexOf("a href=\"", istart + 8);
		}
		
		return false;
	}
	
} // end of class
