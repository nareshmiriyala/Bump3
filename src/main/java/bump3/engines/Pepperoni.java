package bump3.engines;
import bump3.Downloader;
import bump3.GetUrl;
import bump3.Main;
import bump3.Methods;
/** searches pepperoni-mp3.com */
public class Pepperoni {
	/** print line*/
	public static void p(String txt) {
		Methods.p(txt);
	}
	/** print line without \n */
	public static void pr(String txt) {
		Methods.pr(txt);
	}
	
	/** search the site for mp3 based on artist and title 
	 * @param theArtist the name of the artist
	 * @param theTitle name of the song
	 
	 * @return -1 invalid results
	 *          0 no song found
	 *          1 song found and downloaded
	 */
	public static int search(String theArtist, String theTitle) {
		// http://www.pepperoni-mp3.com/index.php?q=lady+gaga+paparazzi&it=song
		
		String theURL = "http://www.pepperoni-mp3.com/index.php?q=" +
					theArtist.replaceAll(" ", "+") + "+" +
					theTitle.replaceAll(" ", "+") + "&it=song";
		
		String artist = theArtist.replaceAll(" ", "").toLowerCase();
		String title  =  theTitle.replaceAll(" ", "").toLowerCase();
		
		pr(Main.GR+"[+]"+Main.GR+" Loading "+Main.G+"Pepperoni"+Main.GR+" search results... ");
		Methods.status("loading pepperoni");
		
		String page = GetUrl.getURL(theURL);
		
		if (page.equals("")) {
			// p(Main.GR+"[+]"+Main.R+" Pepperoni returned invalid search results");
			p(Main.R+"Invalid results");
			return -1;
		}
		 
		Methods.status("searching pepperoni");
		
		int istart = 0, istop = 0;
		boolean checked = false;
		
		// grab the ID, which we can use with get.php to get a faster link to the mp3
		istart = page.indexOf("/download.php?id=");
		while (istart >= 0) {
			istop = page.indexOf("&", istart + 17);
			theURL = "http://www.pepperoni-mp3.com/download.php?id=" + page.substring(istart + 17, istop);
			
			istart = page.indexOf("_blank\">Download ", istop);
			istop = page.indexOf("</a>", istart + 17);
			
			if (istart < 0)
				break;
			
			String name = page.substring(istart + 17, istop);
			name = name.replaceAll("<b>", "");
			name = name.replaceAll("</b>", "");
			name = name.replaceAll("mp3", "");
			name = name.replaceAll("[^a-zA-Z0-9]", "");
			name = name.toLowerCase();
			name = name.replaceAll("the", "");
			name = name.replaceAll("and", "");
			
			artist = artist.replaceAll("the", "");
			artist = artist.replaceAll("and", "");
			
			title = title.replaceAll("the", "");
			title = title.replaceAll("and", "");
			
			Methods.pv((checked ? "" : "\n") + Main.GR + "[+] Comparing " +
						Main.G + name + Main.GR + " to " +
						Main.G + artist+title + Main.GR + "");
			
			if (name.equalsIgnoreCase(artist+title)) {
				String page2 = GetUrl.getURL(theURL);
				
				int istart2, istop2 = 0;
				istart2 = page2.indexOf("flashvars='mp3=");
				istop2  = page2.indexOf("\"", istart2 + 15);
				int istop3 = page2.indexOf("&amp;", istart2 + 15);
				if (istop3 != -1 && istop3 < istop2)
					istop2 = istop3;
				
				if (istart2 < 0) {
					istart = page.indexOf("/download.php?id=", istop);
					continue;
				}
				
				String mid = page2.substring(istart2 + 15, istop2);
				
				if ( ( mid.substring(mid.length() - 3).equalsIgnoreCase("mp3") && Main.SEARCH_MP3) || 
					 ( mid.substring(mid.length() - 3).equalsIgnoreCase("ogg") && Main.SEARCH_OGG) ) {
					
					mid = mid.replaceAll(" ", "%20");
					
					if (!checked) {
						checked = true;
						p("");
					}
					
					// checking the filesize of a url can take a while, so
					// we will output to the user and let them know what we're doing
					if (mid.length() > 25)
						Methods.pr(Main.GR+"[+]"+Main.GR+" Checking    " + 
								Main.BR+mid.substring(0, 25) +Main.GR+ "... " + Main.W);
								
					else
						Methods.pr(Main.GR+"[+]"+Main.GR+" Checking    " + 
								Main.BR+mid+Main.GR+ "... " + Main.W);
					
					int size = GetUrl.getFilesize(mid);
					if (Methods.GuiStop())
						return -1;
					if (size == -2) {
						// file not found!
						p(Main.R+"Not found");
					} else if (size == -3) {
						p(Main.R+"Unable to connect");
					} else if (size >= Main.MIN_FILESIZE) {
						// file on site is bigger than our minimum
						String save = theArtist + " - " + theTitle + "." + mid.substring(mid.length() - 3);
						p(Main.G+"Found song; Downloading..." + Main.W);
						p(Main.GR+"[+]"+Main.GR+" Source:     " +Main.BR+mid + Main.W);
						p(Main.GR+"[+]"+Main.GR+" Desination: " +Main.GR+
								Main.SAVE_DIR + (Main.NOSPACES ? Methods.trimspaces(save) : save) + Main.W);
						
						int dl = Downloader.download(
								mid,
								Main.SAVE_DIR + save,
								size
						);
						
						if (dl >= 0)
							return 1;
						
					} else {
						// file on site was too small
						p(Main.R+"Filesize is too small (" + size + " < " +Main.GR+Main.MIN_FILESIZE+Main.R+ ")");
					}
					
				} else {
					p(Main.GR+"[-]"+Main.R+" Does not end in mp3 or ogg");
					
				} // end of mp3/ogg check
			
			} // end of if name = artist+title
			
			istart = page.indexOf("/download.php?id=", istop);
		} // end of while loop through all results
		
		// if we didn't print anything (like 'checking...') then tell the user what happened
		if (!checked)
			p(Main.R+"No results");
		
		return 0;
	}
}
