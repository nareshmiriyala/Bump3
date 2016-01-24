package bump3.engines;
import bump3.Downloader;
import bump3.GetUrl;
import bump3.Main;
import bump3.Methods;
public class Mp3realm {
	// http://mp3realm.org/search?q=linkin%20park%20breaking%20the%20habit&bitrate=128&pp=10&dur=60
	public static void p(String txt) {
		Methods.p(txt);
	}
	
	public static int search(String theArtist, String theTitle) {
		// http://skreemr.org/results.jsp?q=
		String theURL = "http://mp3realm.org/search?q=" + 
						theArtist.replaceAll(" ", "%20") + "%20" + 
						theTitle.replaceAll(" ", "%20") + "&bitrate=128&pp=10&dur=60";
		
		Methods.pr(Main.GR+"[+]"+Main.GR+" Loading "+Main.G+"Mp3realm"+Main.GR+" search results...  ");
		Methods.status("loading mp3realm");
		
		String page = GetUrl.getURL(theURL);
		
		if (page.equals("")) { // if google returned nothing...
			// p(Main.GR+"[+]"+Main.R+" Mp3realm returned invalid search results.");
			p(Main.R+"Invalid results");
			return -1; // return error status
		}
		
		Methods.status("searching mp3realm");
		
		int istart = 0, istop = 0;
		boolean checked = false;
		istart = page.indexOf("<font size=\"2\" color=\'#1875bf\'>", 0);
		while (istart >= 0) {
			if (Methods.GuiStop())
				return -1;
			istop = page.indexOf("</font>", istart + 31);
			
			// name is the full name of the song
			String name = page.substring(istart + 31, istop);
			name = name.replaceAll(" - x - ", "");
			name = name.replaceAll("<b>", "");
			name = name.replaceAll("</b>", "");
			name = name.replaceAll("[^a-zA-Z0-9]", "");
			name = name.toLowerCase();
			name = name.replaceAll("and", "");
			name = name.replaceAll("the", "");
			
			// remove all non-alphanumeric characters and the and the and
			String artist = theArtist.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
			artist = artist.replaceAll("and", "");
			artist = artist.replaceAll("the", "");
			
			String title = theTitle.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
			title = title.replaceAll("and", "");
			title = title.replaceAll("the", "");
			
			Methods.pv((checked ? "" : "\n") + Main.GR + "[+] Looking at the beginning of  " +
						Main.G + name+ Main.GR + " for " +
						Main.G + artist+title + Main.GR + "");
			
			if ( name.startsWith(artist + title) ) {
				// we got a winner!
				
				int istart2, istop2;
				istart2 = page.indexOf(".loadAndPlay(\'", istop);
				istop2  = page.indexOf("\')", istart2 + 14);
				if (istart2 == -1) {
					istart = page.indexOf("<font size=\"2\" color=\'#1875bf\'>", istart + 31);
					continue;
				}
				
				String mid = page.substring(istart2 + 14, istop2);
				
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
								Main.BR+mid +Main.GR+ "... " + Main.W);
					
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
				} // end of ogg/mp3 check
				
			} // end of region matches loop
			
			// look up the next link to perpetuate the loop
			istart = page.indexOf("<font size=\"2\" color=\'#1875bf\'>", istart + 31);
		}
		
		if (!checked)
			p(Main.R+"No results");
		
		return 0;
	}
}