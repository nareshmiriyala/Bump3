package bump3.engines;

import java.util.ArrayList;
import java.util.List;
import bump3.Downloader;
import bump3.GetUrl;
import bump3.Main;
import bump3.Methods;
public class Mp3skull {
	
	public static void p(String txt) {
		Methods.p(txt);
	}
	public static void pr(String txt) {
		Methods.pr(txt);
	}
	
	public static int search(String theArtist, String theTitle) {
		// http://mp3skull.com/mp3/arist_name_song_name.html  
		
		String theURL = "http://mp3skull.com/mp3/" +
						theArtist.replaceAll(" ", "_") + "_" +
						theTitle.replaceAll(" ", "_") + ".html";
		
		pr(Main.GR+"[+]"+Main.GR+" Loading "+Main.G+"Mp3skull"+Main.GR+" search results... ");
		Methods.status("loading mp3skull");
		
		String page = GetUrl.getURL(theURL);
		
		if (page.equals("")) {
			p(Main.GR+"[+]"+Main.R+" mp3skull returned invalid search results");
			// p(Main.R+"Invalid results");
			return -1;
		}
		
		Methods.status("searching mp3skull");
		
		String artisttitle = theArtist.replaceAll("[^a-zA-Z0-9]", "") + theTitle.replaceAll("[^a-zA-Z0-9]", "");
		artisttitle = artisttitle.toLowerCase();
		
		boolean checked = false;
		
		for (String chunk : between(page, "<div id=\"right_song\">", "<!-- info mp3 here -->")) {
			checked = true;
			List<String> titles = between(chunk, "<div style=\"font-size:15px;\"><b>", "</b>");
			if (titles.size() == 0) continue;
			String title = titles.get(0);
			title = title.replaceAll("[^a-zA-Z0-9]", "");
			title = title.toLowerCase();
			if (title.endsWith("mp3")) title = title.substring(0, title.length() - 3);
			
			Methods.pv(Main.GR + "[+] Comparing " +
						Main.G + title + Main.GR + " to " +
						Main.G + artisttitle + Main.GR + "");
			
			if (artisttitle.equals(title)) {
				List<String> links = between(chunk, ":left;\"><a href=\"", "\"");
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
