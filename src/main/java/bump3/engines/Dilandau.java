package bump3.engines;
import bump3.Downloader;
import bump3.GetUrl;
import bump3.Main;
import bump3.Methods;
public class Dilandau {
	
	public static void p(String txt) {
		Methods.p(txt);
	}
	public static void pr(String txt) {
		Methods.pr(txt);
	}
	
	public static int search(String theArtist, String theTitle) {
		// http://en.dilandau.eu/download_music/ARTIST-NAME-TITLE-1.html
		
		String theURL = "http://en.dilandau.eu/download_music/" +
						theArtist.replaceAll(" ", "-") + "-" +
						theTitle.replaceAll(" ", "-") + "-1.html";
		
		pr(Main.GR+"[+]"+Main.GR+" Loading "+Main.G+"Dilandau"+Main.GR+" search results... ");
		Methods.status("loading dilandau");
		
		String page = GetUrl.getURL(theURL);
		
		if (page.equals("")) {
			p(Main.GR+"[+]"+Main.R+" Dilandau returned invalid search results");
			// p(Main.R+"Invalid results");
			return -1;
		}
		
		Methods.status("searching dilandau");
		
		int istart = 0, istop = 0;
		boolean checked = false;
		int pagenum = 2;
		
		istart = page.indexOf(";;return false;\" href=\"", 0);
		while (istart >= 0) {
			if (Methods.GuiStop())
				return -1;
			
			istop = page.indexOf("\"", istart + 23);
			if (istart < 0) return -1;
			theURL = page.substring(istart + 23, istop);
			
			// get title - artist
			istart = page.lastIndexOf("class=\"title_song\" title=\"", istop);
			istop = page.indexOf("\">", istart + 26);
			if (istart < 0) return -1;
			String tempsong = page.substring(istart + 26, istop);
			tempsong = tempsong.replaceAll("[^a-zA-Z0-9]", "");
			tempsong = tempsong.toLowerCase();
			
			// remove all non-alphanumeric characters (and the 'and' and the 'the')
			String artist = theArtist.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
			String title = theTitle.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
			artist = artist.replaceAll("the", "");
			artist = artist.replaceAll("and", "");
			title = title.replaceAll("the", "");
			title = title.replaceAll("and", "");
			tempsong = tempsong.replaceAll("the", "");
			tempsong = tempsong.replaceAll("and", "");
			
			Methods.pv((checked ? "" : "\n") + Main.GR + "[+] Comparing " +
						Main.G + tempsong + Main.GR + " to " +
						Main.G + title + artist + Main.GR + "");
			
			if (tempsong.startsWith(title + artist)) {
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
								//Main.SAVE_DIR + artist + "-" + title + "." + mid.substring(mid.length() - 3),
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
			}
			
			// look up the next link to perpetuate the loop
			istart = page.indexOf(";;return false;\" href=\"", page.indexOf(";;return false;", istop) + 1);
		}
		
		if (!checked)
			p(Main.R+"No results");
		
		return 0;
	}
}
