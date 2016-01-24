package bump3.engines;

import java.util.Random;
import bump3.Downloader;
import bump3.GetUrl;
import bump3.Main;
import bump3.Methods;
public class Seekasong {
	public static void p(String txt) {
		Methods.p(txt);
	}
	public static void pr(String txt) {
		Methods.pr(txt);
	}
	
	public static int search(String theArtist, String theTitle) {
		// http://www.seekasong.com/search2.php?artist=lady+gaga&title=paparazzi
		Random rnd = new Random();
		rnd.nextInt(5);
		int x = rnd.nextInt(20) + 1;
		int y = rnd.nextInt(20) + 1;
		
		String theURL = "http://www.seekasong.com/search2.php?artist=" + 
						theArtist.replaceAll(" ", "+") + "&title=" + 
						theTitle.replaceAll(" ", "+") + "&what=1";
		
		pr(Main.GR+"[+]"+Main.GR+" Loading "+Main.G+"Seekasong"+Main.GR+" search results... ");
		Methods.status("loading seekasong");
		
		String page = GetUrl.getURL(theURL);
		if (page.equals("")) {
			// p(Main.GR+"[+]"+Main.R+" Seekasong returned invalid search results");
			p(Main.R+"Invalid results");
			return -1;
		}
		
		Methods.status("searching seekasong");
		
		int istart = 0, istop = 0;
		boolean checked = false;
		
		istart = page.indexOf("<a href=\"/download", 0);
		while (istart >= 0) {
			if (Methods.GuiStop())
				return -1;
			istop = page.indexOf("\">", istart + 19);
			
			// name is the full name of the song
			theURL = page.substring(istart + 9, istop);
			String name = theURL.substring(10, theURL.indexOf("-")); // strip out /download/ and the stuff after the dash
			name = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
			name = name.replaceAll("the", "");
			name = name.replaceAll("and", "");
			
			theURL = "http://www.seekasong.com" + theURL;
			
			// remove all non-alphanumeric characters
			String artist = theArtist.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
			artist = artist.replaceAll("the", "");
			artist = artist.replaceAll("and", "");
			
			String title = theTitle.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
			title = title.replaceAll("the", "");
			title = title.replaceAll("and", "");
			
			Methods.pv((checked ? "" : "\n") + Main.GR + "[+] Comparing  " +
						Main.G + name + Main.GR + " to " +
						Main.G + artist+title + Main.GR + "");
			if (name.equalsIgnoreCase(artist + title)) {
				
				String page2 = GetUrl.getURL(theURL);
				if (page2.equals("")) {
					// invalid results, next!
					istart = page.indexOf("<a href=\"/download", istart + 19);
					continue;
				}
				
				int istart2, istop2;
				istart2 = page2.indexOf("<a class=help href=\"", 0);
				istop2  = page2.indexOf("\"", istart2 + 21);
				if (istart2 == -1) {
					// p("breaking");
					istart = page.indexOf("<a href=\"/download", istart + 19);
					continue;
				}
				
				String mid = page2.substring(istart2 + 20, istop2);
				
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
			istart = page.indexOf("<a href=\"/download", istart + 19);
		}
		
		if (!checked)
			p(Main.R+"No results");
		
		return 0;
	}
}
