package bump3.engines;
import bump3.Downloader;
import bump3.GetUrl;
import bump3.Main;
import bump3.Methods;
public class Espew {
	public static void p(String txt) {
		Methods.p(txt);
	}
	
	public static int search(String theArtist, String theTitle) {
		// http://www.espew.net/cgi-bin/search?searchstring=linkin+park+breaking+the+habit&searchby=song&results=10&search=Search
		String theURL = "http://www.espew.net/cgi-bin/search?searchstring=" + 
						theArtist.replaceAll(" ", "+") + "+" + 
						theTitle.replaceAll(" ", "+") + "&searchby=song&results=10&search=Search";
		
		Methods.pr(Main.GR+"[+]"+Main.GR+" Loading "+Main.G+"Espew"+Main.GR+" search results...     ");
		Methods.status("loading espew");
		
		String page = GetUrl.getURL(theURL);
		
		if (page.equals("")) {
			// p(Main.GR+"[+]"+Main.R+" Espew returned invalid search results.");
			p(Main.R+"Invalid results");
			return -1; // return error status
		}
		
		Methods.status("searching espew");
		
		int istart = 0, istop = 0;
		boolean checked = false;
		int pagecount = 1;
		
		do {
			istart = page.indexOf("href=\"/cgi-bin/spew/", 0);
			while (istart >= 0) {
				if (Methods.GuiStop())
					return -1;
					
				istop = page.indexOf("\">", istart + 6);
				
				// name is the full name of the song
				theURL = page.substring(istart + 6, istop);
				String name = theURL;
				theURL = "http://www.espew.net" + theURL;
				
				name = name.toLowerCase();
				name = name.replaceAll(".mp3", "");
				name = name.replaceAll("%20", "");
				name = name.substring(name.lastIndexOf("/") + 1);
				name = name.replaceAll("[^a-zA-Z0-9]", "");
				
				// remove all non-alphanumeric characters
				String artist = theArtist.replaceAll("[^a-zA-Z0-9]", "");
				artist = artist.toLowerCase();
				String title = theTitle.replaceAll("[^a-zA-Z0-9]", "");
				title = title.toLowerCase();
				
				Methods.pv((checked ? "" : "\n") + Main.GR + "[+] Comparing " + 
						Main.G + name.substring(name.length()-title.length()) + Main.GR +
						   " to " + Main.G + title + Main.GR + "");
				if ( name.endsWith(artist+title) ) {
					// we got a winner!
					
					String mid = theURL;
					
					if ( ( mid.substring(mid.length() - 3).equalsIgnoreCase("mp3") && Main.SEARCH_MP3) || 
						( mid.substring(mid.length() - 3).equalsIgnoreCase("ogg") && Main.SEARCH_OGG) ) {
						// link ends in mp3 or ogg...
						
						if (!checked) {
							checked = true;
							p("");
						}
						
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
							p(Main.R+"Not found.");
						} else if (size == -3) {
							p(Main.R+"Unable to connect");
						} else if (size >= Main.MIN_FILESIZE) {
							// file on site is bigger than our minimum
							p(Main.G+"Found song; Downloading..." + Main.W);
							p(Main.GR+"[+]"+Main.GR+" Source:     " +Main.BR+mid + Main.W);
							p(Main.GR+"[+]"+Main.GR+" Desination: " +Main.GR+
									Main.SAVE_DIR + artist + "-" + title + "." + mid.substring(mid.length() - 3) + Main.W);
							
							int dl = Downloader.download(
									mid,
									Main.SAVE_DIR + artist + "-" + title + "." + mid.substring(mid.length() - 3),
									size
							);
							
							if (dl >= 0)
								return 1; // found it and downloaded it!
							
						} else {
							// file on site was too small
							p(Main.R+"Filesize is too small (" + size + " < " +Main.GR+Main.MIN_FILESIZE+Main.R+ ")");
						}
						
					} else {
						p(Main.GR+"[-]"+Main.R+" Does not end in mp3 or ogg.");
					} // end of ogg/mp3 check
					
				} // end of region-matches check for artist/title
				
				// look up the next link to perpetuate the loop
				istart = page.indexOf("href=\"/cgi-bin/spew/", istart + 6);
				
			} // end of while-loop through all of the results
			
			// the below code checks for another page of results and, if it finds it in the HTML, loads it and continues
			pagecount++;
			if (page.indexOf("&amp;page=" + pagecount) < 0)
				break;
			theURL = "http://www.espew.net/cgi-bin/search?searchstring=" + 
						theArtist.replaceAll(" ", "+") + "+" + 
						theTitle.replaceAll(" ", "+") + "&searchby=song&results=10&search=Search&page="+pagecount+"";
			page = GetUrl.getURL(theURL);
		} while (1 == 1); // loop forever; we can break out if need be.
		
		if (!checked)
			p(Main.R+"No results");
		
		return 0; // did not find it.
		
	} // end of search method
}
