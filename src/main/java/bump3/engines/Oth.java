package bump3.engines;
import bump3.Downloader;
import bump3.GetUrl;
import bump3.Main;
import bump3.Methods;
public class Oth {
	
	public static void p(String txt) {
		Methods.p(txt);
	}
	
	public static int search(String theArtist, String theTitle) {
		// http://www.oth.net/s/s?q=  &cl=1
		String theURL = "http://www.oth.net/s/s?q=" + 
						theArtist.replaceAll(" ", "+") + "+" + 
						theTitle.replaceAll(" ", "+") + "&cl=1";
		
		Methods.pr(Main.GR+"[+]"+Main.GR+" Loading "+Main.G+"Oth"+Main.GR+" search results...       ");
		Methods.status("loading oth");
		
		String page = GetUrl.getURL(theURL);
		
		if (page.equals("")) {
			// p(Main.GR+"[+]"+Main.R+" Oth returned invalid search results.");
			p(Main.R+"Invalid results");
			return -1; // return error status
		}
		
		Methods.status("searching oth");
		
		int istart = 0, istop = 0;
		boolean checked = false;
		
		istart = page.indexOf("</a> <a href=\"", 0);
		while (istart >= 0) {
			if (Methods.GuiStop())
				return -1;
			
			istop = page.indexOf("\">", istart + 14);
			
			// name is the full name of the song
			theURL = page.substring(istart + 14, istop);
			String name = theURL;
			name = name.replaceAll(".mp3", "");
			name = name.replaceAll("[^a-zA-Z0-9]", "");
			
			// remove all non-alphanumeric characters
			String artist = theArtist.replaceAll("[^a-zA-Z0-9]", "");
			String title = theTitle.replaceAll("[^a-zA-Z0-9]", "");
			
			Methods.pv((checked ? "" : "\n") + Main.GR + "[+] Looking at the end of " +
						Main.G + name + Main.GR + " for " +
						Main.G + title + Main.GR + "");
			if ( name.regionMatches(true, name.length() - title.length(), title, 0, title.length()) ) {
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
					p(Main.GR+"[-]"+Main.R+" Does not end in mp3 or ogg.");
				}
			} // end of region matches if statement
			
			// look up the next link to perpetuate the loop
			istart = page.indexOf("</a> <a href=\"", istop + 14);
		}
		
		if (!checked)
			p(Main.R+"No results");
		
		return 0;
	}
}