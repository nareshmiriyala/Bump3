package bump3.engines;

import java.util.ArrayList;
import java.util.List;
import bump3.Downloader;
import bump3.GetUrl;
import bump3.Main;
import bump3.Methods;
public class Prostopleer {
	
	public static void p(String txt) {
		Methods.p(txt);
	}
	public static void pr(String txt) {
		Methods.pr(txt);
	}
	
	public static int search(String theArtist, String theTitle) {
		// http://prostopleer.com/search?q=artist%3Agreen+day+track%3Alongview
		
		String theURL = "http://prostopleer.com/search?q=" + 
					theArtist.replaceAll(" ", "+") + "+" +
					theTitle.replaceAll(" ", "+") + "";
		pr(Main.GR+"[+]"+Main.GR+" Loading "+Main.G+"prostopleer"+Main.GR+" search results... ");
		Methods.status("loading prostopleer");
		
		String page = GetUrl.getURL(theURL);
		
		if (page.equals("")) {
			p(Main.GR+"[+]"+Main.R+" prostopleer returned invalid search results");
			return -1;
		}
		
		Methods.status("searching prostopleer");
		
		String maintitle = theTitle.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
		String mainartist = theArtist.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
		
		boolean checked = false;
		List<String> chunks = between(page, "file_id=\"", "\">");
		for (String chunk : chunks) { 
			List<String> titles = between(chunk, "song=\"", "\" ");
			if (titles.size() == 0) continue;
			String title = titles.get(0).replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
			if (title.endsWith("mp3")) title = title.substring(0, title.length() - 3);
			
			List<String> artists = between(chunk, "singer=\"", "\" ");
			if (artists.size() == 0) continue;
			String artist = artists.get(0).replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
			
			checked = true;
			Methods.pv(Main.GR + "[+] Comparing " +
						Main.G + title + Main.GR + " to " +
						Main.G + maintitle + Main.GR + " and " +
						Main.G + artist + Main.GR + " to " +
						Main.G + mainartist + Main.GR + ""); 
			
			List<String> ids = between(chunk, "link=\"", "\" ");
			if (ids.size() == 0) continue;
			String id = ids.get(0);
			if (title.equals( maintitle) && artist.equals(mainartist)) {
				String page2 = GetUrl.getURL("http://prostopleer.com/site_api/files/get_url?action=play&id=" + id);
				List<String> links = between(page2, "\"track_link\":\"", "\"");
				if (links.size() == 0) continue;
				String link = links.get(0);
				String mid = link;
				if ( ( mid.substring(mid.length() - 3).equalsIgnoreCase("mp3") && Main.SEARCH_MP3) || 
					( mid.substring(mid.length() - 3).equalsIgnoreCase("ogg") && Main.SEARCH_OGG) ) {
					// link ends in mp3 or ogg...
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
						String save = theArtist + " - " + theTitle + ".mp3"; 
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
				}
				
			}
			
		}
		
		if (!checked)
			p(Main.R+"No results");
		
		return 0;
	}
	
	public static List<String> between(String source, String start, String finish) {
		List<String> result = new ArrayList<String>();
		int i, j;
		i = source.indexOf(start);
		j = source.indexOf(finish, i + start.length());
		while (i >= 0 && j >= 0) {
			result.add(source.substring(i + start.length(), j));
			i = source.indexOf(start, j + finish.length());
			j = source.indexOf(finish, i + start.length());
		}
		return result;
	}
}
