package bump3.engines;

import bump3.Downloader;
import bump3.GetUrl;
import bump3.Main;
import bump3.Methods;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

/* here's how it works
 create a text file
 each line corresponds to input for the program
 line on the left is the line number, starting at 0
 0 plugin name
 1 [blank] - i'm retarded
 2 [blank] - still stupid
 3 website to query,
      use $a$ for where the artist should go,
          $t$ for the title;
      ex google.com/?q=$a$+$t$&safe=off
 4 website space filler, to fill in the spaces
 	  usually it's a plus sign +
 	  but sometimes it's %20 or -
 5 string signifying start of url;
 	  ex: <a href="/download/mp3/...
 6 length until url starts;
 	  ex: 9
 7 end of url;
 	  ex: ">
 8 what to strip from the url;
 	  ex: .mp3
 9 same as 7, blank for nothing;
 	  ex: /download/mp3/
10 same as 7, blank for nothing;
	  ex:
11 RR=region matches right side, RL=region matcthes left side, E=equals ignore case
	  RR checks only the right side of the URL (after it's been stripped of lines 7-9 and non-alphanumeric characters
	  RL checks the left side of the URL, same as RR
	  region matches - if the url is 
	      "/download/01 - lady gaga - pokerface.mp3", 
	  after it's stripped (/download/ and .mp3) it would be:
	      "01ladygagapokerface"
	  checking the RR (right region) we would compare it to the artist title, so
	       "at" = artisttitle = "ladygagapokerface"
	  comparing "at" and the url gives us:
	  
	  01ladygagapokerface
	    ladygagapokerface
	  we have a match on the right side.
	  so we would want to have these settings:
	  line11=RR
	  line12=at
	  
12 comparison string (a, t, at, ta) 
	  a=artist, t=title, 
      "at" would look for artisttitle (or ladygagapokerface), 
      "ta" would be reversed (pokerfaceladygaga)
      
13 beginning of url (domain name, usually) - blank if it's not needed
	  some urls don't include the domain. this is to fill the domain if need be.
	  
14 url to the next page's results
	  program will look for this link in the page's results, if it's found, it will
	    load the next page of search results and continue searching
	  $a$ artist
	  $t$ title
	  $p$ page number
	  leave this line blank if you don't want to continue onto other search results (or if it's buggy)

15 page increment, used by line 14
	  to get to the next search result page, we need to know how much the pages go up by...
	  some pages go up by 1, some by 10
	  ex: http://site1.com/?q=search&page=2
	  	  http://site1.com/?q=search&page=3
	  so we would put '1' here
	  
	  or: http://site2.com/?q=search&result=11
	  	  http://site2.com/?q=search&result=21
	  so we would put '11' here

16 [optional] the url we just got links to another site; the mp3 is on this other site...
17 [optional] string signifying start of url
18 [optional] length until url starts
19 [optional] end of url
*/

public class Plugin {
	public static void p(String txt) {
		Methods.p(txt);
	}
	public static void pr(String txt) {
		Methods.pr(txt);
	}
	
	private String plugin[];
	
	public Plugin(String file) {
		Scanner sc = null;
		FileReader fr = null;
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException fnfe) {
			// we need a file for the plugin to work!
			p(Main.GR+"[+] "+Main.R+"Plugin not found; exiting");
			System.exit(0);
		}
		
		sc = new Scanner(fr);
		String everything = "";
		while (sc.hasNext()) {
			everything = everything + sc.nextLine() + "\n";
		}
		
		plugin = everything.split("\n");
		if (plugin.length < 13) {
			p(Main.GR+"[+] "+Main.R+"Invalid plugin, not enough lines; exiting");
			System.exit(0);
		}
		
		boolean removeLeft = false;
		if (plugin[0].regionMatches(true, 0, "name=", 0, 5) ) {
			for (int i = 0; i < plugin.length; i++) {
				plugin[i] = plugin[i].substring(plugin[i].indexOf("=") + 1);
			}
		}
		
	}
	
	public int search(String theArtist, String theTitle) {
		/*String theArtist = plugin[1];
		String theTitle = plugin[2];*/
		
		String theURL = plugin[3];
		theURL = theURL.replace("$a$", theArtist.replaceAll(" ", plugin[4]));
		theURL = theURL.replace("$t$", theTitle.replaceAll(" ", plugin[4]));
		
		pr(Main.GR+"[+]"+Main.B+" Loading "+Main.G+plugin[0]+Main.B+" search results...  ");
		Methods.status("loading " + plugin[0] + "");
		
		String page = GetUrl.getURL(theURL);
		
		if (page.equals("")) {
			// p(Main.GR+"[+]"+Main.R+" FindMp3s returned invalid search results.");
			p(Main.R+"Invalid results");
			return -1; // return error status
		}
		
		Methods.status("searching " + plugin[0] + "");
		
		int istart = 0, istop = 0;
		boolean checked = false;
		int pagecount = 1;
		do {
			istart = page.indexOf(plugin[5], 0);
			while (istart >= 0) {
				if (Methods.GuiStop())
					return -1;
				
				istop = page.indexOf(plugin[7], istart + Integer.parseInt(plugin[6]));
				
				/*p("ISTART=" +istart);
				p("ISTOP=" + istop);
				p("PLUGIN[6]="+Integer.parseInt(plugin[6]));*/
				theURL = page.substring(istart + Integer.parseInt(plugin[6]), istop);
				String name = theURL;
				
				theURL = plugin[13] + theURL;
				
				String theFile = name;
				if (!plugin[8].equals(""))
					name = name.replaceAll(plugin[8], "");
				if (!plugin[9].equals(""))
					name = name.replaceAll(plugin[9], "");
				if (!plugin[10].equals(""))
					name = name.replaceAll(plugin[10], "");
				
				name = name.replaceAll("[^a-zA-Z0-9]", "");
				
				// remove all non-alphanumeric characters
				String artist = theArtist.replaceAll("[^a-zA-Z0-9]", "");
				String title = theTitle.replaceAll("[^a-zA-Z0-9]", "");
				
				String comp = "";
				for (int i = 0; i < plugin[12].length(); i++) {
					if (plugin[12].charAt(i) == 't')
						comp += title;
					else if (plugin[12].charAt(i) == 'a')
						comp += artist;
				}
				
				Methods.pv(Main.GR + "[+] Comparing " +
							Main.G + name + Main.GR + " to " +
							Main.G + title + Main.GR + "");
				if ( 
						( 
							plugin[11].equals("RR") && 
							name.regionMatches(true, 
								name.length() - comp.length(), 
								comp, 
								0, 
								comp.length()
						)
						) || (
							plugin[11].equals("E") && 
							name.equalsIgnoreCase(title) 
						) || (
							plugin[11].equals("RL") && 
							name.regionMatches(true,
								0,
								comp,
								0,
								comp.length()
							)	
						)
					) {
					// we got a winner!
					
					String mid = theURL;
					
					if ( ( theFile.substring(theFile.length() - 3).equalsIgnoreCase("mp3") && Main.SEARCH_MP3) || 
						( theFile.substring(theFile.length() - 3).equalsIgnoreCase("ogg") && Main.SEARCH_OGG) ) {
						// link ends in mp3 or ogg...
						
						if (!checked) {
							checked = true;
							p("");
						}
						
						/*if (mid.length() > 25)
							Methods.pr(Main.GR+"[+]"+Main.B+" Checking    " + 
									Main.BR+mid.substring(0, 25) +Main.B+ "... " + Main.W);
									
						else*/
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
									Main.SAVE_DIR + artist + "-" + title + "." + theFile.substring(theFile.length() - 3) + Main.W);
							
							int dl = Downloader.download(
									mid,
									Main.SAVE_DIR + artist + "-" + title + "." + theFile.substring(theFile.length() - 3),
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
				istart = page.indexOf(plugin[5], istop + Integer.parseInt(plugin[6]));
				
			} // end of loop through each result
			
			// go to next page of results
			
			// if the plugin doesn't want to continue searching...
			if (plugin.length < 15 || plugin[14].equals(""))
				break;
			
			pagecount += Integer.parseInt(plugin[15]);
			
			theURL = plugin[14];
			theURL = theURL.replace("$a$", theArtist.replaceAll(" ", plugin[4]));
			theURL = theURL.replace("$t$", theTitle.replaceAll(" ", plugin[4]));
			theURL = theURL.replace("$p$", "" + pagecount);
			
			// if the url we're looking for is on the page...
			if (page.indexOf(theURL) < 0 )
				break;
			
			page = GetUrl.getURL(theURL);
			if (page.equals(""))
				break;
		} while (1 == 1);
		
		if (!checked)
			p(Main.R+"No results");
		
		return 0;
	}
}