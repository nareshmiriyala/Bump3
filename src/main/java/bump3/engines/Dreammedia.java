package bump3.engines;

import bump3.Downloader;
import bump3.GetUrl;
import bump3.Main;
import bump3.Methods;

public class Dreammedia {
	public static void p(String txt) {
		Methods.p(txt);
	}
	
	public static int search(String theArtist, String theTitle){
		// searches dreammedia.ru
		// http://dreammedia.ru/index.php?action=showartist&artist=ARTIST%20NAME
		String tempart = FirstLetterUpper(theArtist);
		tempart = tempart.replace(" ", "%20");
		String theURL = "http://dreammedia.ru/show/music/" + tempart + "/";
		
		Methods.pr(Main.GR+"[+]"+Main.GR+" Loading "+Main.G+"Dreammedia"+Main.GR+" search results...");
		Methods.status("loading dreammedia");
		
		String page = GetUrl.getURL(theURL);
		
		if (page.equals("")) { // if google returned nothing...
			p(Main.GR+"\n[+]"+Main.R+" Dreammedia returned invalid search results.");
			// p(Main.R+"Invalid results");
			return -1; // return error status
		}
		
		Methods.status("searching dreammedia");
		
		int istart = 0, istop = 0;
		boolean checked = false;
		
		istart = page.indexOf("<td valign=\"top\">", 0);
		while (istart >= 0) {
			if (Methods.GuiStop())
				return -1;
			istop = page.indexOf("[<a", istart + 19);
			
			// name is the full name of the song
			String name = page.substring(istart + 19, istop);
			name = name.replaceAll("[^a-zA-Z0-9]", "");
			
			name = name.toLowerCase();
			name = name.replaceAll("and", "");
			name = name.replaceAll("the", "");
			
			// remove all non-alphanumeric characters
			String artist = theArtist.replaceAll("[^a-zA-Z0-9]", "");
			String title = theTitle.replaceAll("[^a-zA-Z0-9]", "");
			
			title = title.toLowerCase();
			title = title.replaceAll("and", "");
			title = title.replaceAll("the", "");
			
			//p("[+] Looking for " + title + " inside of " + name);
			Methods.pv((checked?"":"")+
					Main.GR + "[+] Comparing " +
					Main.G + name + Main.GR + " to " +
					Main.G + title + Main.GR + "");
			if (name.equalsIgnoreCase(title)) {
			// if ( name.regionMatches(true, 0, artist + title, 0, artist.length() + title.length()) ) {
				// we got a winner!
				
				// get url of album
				int istart2, istop2;
				istart2 = page.indexOf("<a href=\"/music/", istop);
				istop2	= page.indexOf("\">", istart2 + 16);
				if (istart2 == -1) {
					// p("breaking");
					break;
				}
				
				if (!checked) {
					checked = true;
					p("");
				}
				
				theURL = page.substring(istart2 + 9, istop2);
				theURL = "http://dreammedia.ru" + theURL;
				System.out.println(theURL);
				
				int size = GetUrl.getFilesize(theURL);
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
					p(Main.GR+"[+]"+Main.GR+" Source:      " +Main.BR+theURL+ Main.W);
					p(Main.GR+"[+]"+Main.GR+" Desination: " +Main.GR+
									Main.SAVE_DIR +
									(Main.NOSPACES ?
				Methods.trimspaces(theArtist + " - " + theTitle + "." + theURL.substring(theURL.length() - 3)) :
									theArtist + " - " + theTitle + "." + theURL.substring(theURL.length() - 3)) +
									Main.W);
				
					int dlt = Downloader.download(
							theURL,
							Main.SAVE_DIR + theArtist + " - " + theTitle + "." + theURL.substring(theURL.length() - 3),
							size
					);
					
					if (dlt >= 0)
						return 1;
				} else {
					// file on site was too small
					p(Main.R+"Filesize is too small (" + size + " < " +Main.GR+Main.MIN_FILESIZE+Main.R+ ")");
				}
			} // end of big IF name equalsIgnoreCase title
			
			
			// look up the next link to perpetuate the loop
			istart = page.indexOf("<td valign=\"top\">", istart + 12);
		} // end of whiel loop through all results
		
		if (!checked)
			p(Main.R+"No results");
		
		return 0;
	} // end of search method
	
	public static int searchDreammediaAlbum(String artist, String album){
		// searches for and attempts to download an entire album
		return 0;
	}
	
	public static String FirstLetterUpper(String s) {
		String result = "";
		boolean first = true;
		for (int i = 0; i < s.length(); i++) {
			if (first) {
				String c = "" + s.charAt(i);
				c = c.toUpperCase();
				result += c;
				first = false;
			} else {
				String c = "" + s.charAt(i);
				c = c.toLowerCase();
				result += c;
				if (s.charAt(i) == ' ')
					first = true;
			}
		}
		return result;
	}		
}
