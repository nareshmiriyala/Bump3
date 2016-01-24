package bump3.engines;
import bump3.Downloader;
import bump3.GetUrl;
import bump3.Main;
import bump3.Methods;
public class Findmp3s {
	public static void p(String txt) {
		Methods.p(txt);
	}
	public static void pr(String txt) {
		Methods.pr(txt);
	}
	
	public static int search(String theArtist, String theTitle) {
		// http://www.espew.net/cgi-bin/search?searchstring=linkin+park+breaking+the+habit&searchby=song&results=10&search=Search
		String theURL = "http://findmp3s.com/search/mp3/1/" + 
						theArtist.replaceAll(" ", "-") + "-" + 
						theTitle.replaceAll(" ", "-") + ".html";
		
		pr(Main.GR+"[+]"+Main.GR+" Loading "+Main.G+"FindMp3s"+Main.GR+" search results...  ");
		Methods.status("loading findmp3s");
		
		String page = GetUrl.getURL(theURL);
		
		if (page.equals("")) {
			// p(Main.GR+"[+]"+Main.R+" FindMp3s returned invalid search results.");
			p(Main.R+"Invalid results");
			return -1; // return error status
		}
		
		Methods.status("searching findmp3s");
		
		int istart = 0, istop = 0;
		boolean checked = false;
		
		//int pagecount = 1;
		//do {
		// the page system on this site is iffy at best
		// continuously loops to next page.
		
			istart = page.indexOf("songbox\" href=\"", 0);
			while (istart >= 0) {
				if (Methods.GuiStop())
					return -1;
				istop = page.indexOf("\"", istart + 15);
				
				// name is the full name of the song
				theURL = page.substring(istart + 15, istop);
				String name = theURL;
				
				name = name.substring(name.lastIndexOf("/") + 1);
				String theFile = name;
				name = name.replaceAll("_mp3", "");
				name = name.replaceAll(".html", "");
				name = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
				name = name.replaceAll("the", "");
				name = name.replaceAll("and", "");
				
				String artist = theArtist.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
				artist = artist.replaceAll("the", "");
				artist = artist.replaceAll("and", "");
				
				String title = theTitle.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
				title = title.replaceAll("the", "");
				title = title.replaceAll("and", "");
				
				Methods.pv((checked ? "" : "\n") + Main.GR + "[+] Looking through " +
							Main.G + name + Main.GR + " for " +
							Main.G + artist+title + Main.GR + " or just " + 
							Main.G + title + Main.GR + "");
				
				if ( name.endsWith(artist+title) || name.equalsIgnoreCase(title) ) {
					// we got a winner!
					String page2 = GetUrl.getURL(theURL);
					int istart2 = page2.indexOf("<div id=\"download_box\"><a target=\"_blank\" href=\"");
					int istop2 = page2.indexOf("\"", istart2 + "<div id=\"download_box\"><a target=\"_blank\" href=\"".length());
					if (istart2 < 0 || istop2 < 0) {
						istart = page.indexOf("songbox\" href=\"", istop + 15);
						continue;
					}
					
					theURL = page2.substring(istart2 + "<div id=\"download_box\"><a target=\"_blank\" href=\"".length(), istop2);
					theURL = theURL.replaceAll("&amp;", "&");
					theURL = theURL.replaceAll(" ", "%20");
					
					theFile = theURL.substring(0, theURL.indexOf("&mode="));
					
					String mid = theURL;
					
					if ( ( theFile.substring(theFile.length() - 3).equalsIgnoreCase("mp3") && Main.SEARCH_MP3) || 
						( theFile.substring(theFile.length() - 3).equalsIgnoreCase("ogg") && Main.SEARCH_OGG) ) {
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
							String save = theArtist + " - " + theTitle + "." + theFile.substring(theFile.length() - 3);
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
				} else {
					//p("LOSE");
				}
				
				// look up the next link to perpetuate the loop
				istart = page.indexOf("songbox\" href=\"", istop + 15);
			}
			/*
			pagecount++;
			theURL = "http://findmp3s.com/search/mp3/"+pagecount+"/" + 
						theArtist.replaceAll(" ", "-") + "-" + 
						theTitle.replaceAll(" ", "-") + ".html";
			if (page.indexOf(theURL) < 0)
				break;
			page = GetUrl.getURL(theURL);
		} while (1==1);*/
		
		if (!checked)
			p(Main.R+"No results");
		
		return 0;
	}
}
