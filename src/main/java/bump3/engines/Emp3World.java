package bump3.engines;

import bump3.Downloader;
import bump3.GetUrl;
import bump3.Main;
import bump3.Methods;

public class Emp3World {
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
		// http://www.emp3world.com/search.php?phrase=manu+chao+clandestino&type=mp3s&submit=Search
	
		String theURL = "http://www.emp3world.com/search.php?phrase=" +
					theArtist.replaceAll(" ", "+") + "+" +
					theTitle.replaceAll(" ", "+") + "&type=mp3s&submit=Search";
		
		String artist = theArtist.replaceAll(" ", "").toLowerCase();
		String title  =  theTitle.replaceAll(" ", "").toLowerCase();
		
		pr(Main.GR+"[+]"+Main.GR+" Loading "+Main.G+"Emp3World"+Main.GR+" search results... ");
		Methods.status("loading emp3world");
		
		String page = GetUrl.getURL(theURL);
		
		if (page.equals("")) {
			// p(Main.GR+"[+]"+Main.R+" Pepperoni returned invalid search results");
			p(Main.R+"Invalid results");
			return -1;
		}
		
		Methods.status("searching emp3world");
		
		int istart = 0, istop = 0;
		boolean checked = false;
		
		// grab the ID, which we can use with get.php to get a faster link to the mp3
		istart = page.indexOf("%\"><a href=\"/mp3/");
		while (istart >= 0) {
			istop = page.indexOf("\">", istart + 12);
			theURL = "http://www.emp3world.com" + page.substring(istart + 12, istop);
			
			String name = theURL.substring(theURL.lastIndexOf("/", theURL.lastIndexOf("/") - 1));
			name = name.replaceAll("/", "");
			name = name.replaceAll("mp3", "");
			name = name.replaceAll("[^a-zA-Z0-9]", "");
			
			name = name.toLowerCase();
			name = name.replaceAll("the", "");
			name = name.replaceAll("and", "");
			
			artist = artist.toLowerCase();
			artist = artist.replaceAll("the", "");
			artist = artist.replaceAll("and", "");
			title = title.toLowerCase();
			title = title.replaceAll("the", "");
			title = title.replaceAll("and", "");
			
			theURL = theURL.replaceAll(" ", "%20");
			
			Methods.pv((checked ? "" : "\n") + Main.GR + "[+] Comparing " +
						Main.G + name + Main.GR + " to " +
						Main.G + artist+title + Main.GR + "");
			if (name.equalsIgnoreCase(artist+title)) {
				if (Main.VERBOSE)
					checked = true;
				
				if (theURL.length() > 25)
					Methods.pv(Main.GR+"[+]"+Main.GR+" Loading     " + 
							Main.BR+theURL.substring(0, 25) +Main.GR+ "... " + Main.W);
							
				else
					Methods.pv(Main.GR+"[+]"+Main.GR+" Loading     " +
							Main.BR+theURL+Main.GR+ "... " + Main.W);
				
				String page2 = GetUrl.getURL(theURL);
				
				int istart2, istop2 = 0;
				istart2 = page2.indexOf(";file=http");
				istop2  = page2.indexOf("&amp;enable", istart2 + 6);
				
				if (istart2 < 0) {
					istart = page.indexOf("%\"><a href=\"/mp3/", istop);
					continue;
				}
				
				String mid = page2.substring(istart2 + 6, istop2);
				mid = Methods.hexToString(mid);
				
				if ( ( mid.substring(mid.length() - 3).equalsIgnoreCase("mp3") && Main.SEARCH_MP3) || 
					 ( mid.substring(mid.length() - 3).equalsIgnoreCase("ogg") && Main.SEARCH_OGG) ) {
					
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
			
			istart = page.indexOf("%\"><a href=\"/mp3/", istop);
		} // end of while loop through all results
		
		// if we didn't print anything (like 'checking...') then tell the user what happened
		if (!checked)
			p(Main.R+"No results");
		
		return 0;
	}
}
