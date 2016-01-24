package bump3.engines;

import java.util.ArrayList;
import java.util.List;
import bump3.Downloader;
import bump3.GetUrl;
import bump3.Main;
import bump3.Methods;
public class FourShared {
	
	public static void p(String txt) {
		Methods.p(txt);
	}
	public static void pr(String txt) {
		Methods.pr(txt);
	}
	
	public static int search(String theArtist, String theTitle) {
		// http://search.4shared.com/q/1/artist%20title%20mp3
		
		String theURL = "http://search.4shared.com/q/1/" +
					theArtist.replaceAll(" ", "%20") + "%20" +
					theTitle.replaceAll(" ", "%20") + "%20mp3";
		
		pr(Main.GR+"[+]"+Main.GR+" Loading "+Main.G+"4shared"+Main.GR+" search results... ");
		Methods.status("loading 4shared");
		
		String page = GetUrl.getURL(theURL);
		
		if (page.equals("")) {
			p(Main.GR+"[+]"+Main.R+" 4shared returned invalid search results");
			return -1;
		}
		
		Methods.status("searching 4shared");
		
		String maintitle = theTitle.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
		String mainartist = theArtist.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
		
		boolean checked = false;
		
		for (String chunk : between(page, "ass=\"simpleThumb\">", "</h1>")) {
			List<String> titles = between(chunk, " target=\"_blank\">", "</a>");
			if (titles.size() == 0) continue;
			String title = titles.get(0).replaceAll("\\<.*?\\>", "");
			title = title.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
			if (title.endsWith("mp3")) title = title.substring(0, title.length() - 3);
			
			checked = true;
			Methods.pv(Main.GR + "[+] Comparing " +
						Main.G + title + Main.GR + " to " +
						Main.G + mainartist + maintitle + Main.GR + ""); 
			
			List<String> links = between(chunk, "showMediaPreview(event, '", "','");
			if (links.size() == 0) continue;
			String link = links.get(0);
			if (title.equals(mainartist + maintitle) || title.equals(maintitle + mainartist)) {
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
