package bump3.engines;

import java.io.FileWriter;
import java.io.IOException;
import bump3.Downloader;
import bump3.GetUrl;
import bump3.Main;
import bump3.Methods;
/** grabs mp3s from esnips
 *  at least it's supposed to
 *  haven't had much luck with it lately...
 *  wow, on second thought, esnips sucks.
 */
public class Esnips {
	public static void p(String txt) {
		Methods.p(txt);
	}
	public static void pr(String txt) {
		Methods.pr(txt);
	}
	
	public static int search(String theArtist, String theTitle) {
		// http://www.esnips.com/_t_/GREEN+DAY+HOLIDAY?m=4&q=GREEN+DAY+HOLIDAY
		
		// Main.USE_COOKIES = true;
		
		String theURL = "http://www.esnips.com/_t_/" + 
						theArtist.replaceAll(" ", "+") + "+" +
						theTitle.replaceAll(" ", "+") + "?m=4&q=" +
						theArtist.replaceAll(" ", "+") + "+" +
						theTitle.replaceAll(" ", "+");
		
		pr(Main.GR+"[+]"+Main.GR+" Loading "+Main.G+"Esnips"+Main.GR+" search results...  ");
		Methods.status("loading esnips");
		
		String page = GetUrl.getURL(theURL);
		
		if (page.equals("")) {
			// p(Main.GR+"[+]"+Main.R+" Esnips returned invalid search results.");
			p(Main.R+"Invalid results");
			return -1; // return error status
		}
		
		Methods.status("searching esnips");
		
		int istart = 0, istop = 0;
		boolean checked = false;
		
		istart = page.indexOf("<a href=\"/doc/", 0);
		while (istart >= 0) {
			if (Methods.GuiStop())
				return -1;
			istop = page.indexOf("\">", istart + 9);
			
			String referrer = page.substring(istart + 9, istop);
			
			istart = istop + 2;
			istop = page.indexOf("</a>", istop);
			
			String name = page.substring(istart, istop);
			name = name.replaceAll(".mp3", "");
			name = name.replaceAll("[^a-zA-Z0-9]", "");;
			
			// remove all non-alphanumeric characters
			String artist = theArtist.replaceAll("[^a-zA-Z0-9]", "");
			String title = theTitle.replaceAll("[^a-zA-Z0-9]", "");
			
			Methods.pv((checked?"":"\n")+Main.GR + "[+] Comparing " +
						Main.G + name + Main.GR + " for " +
						Main.G + artist+title + Main.GR + "");
			
			if (name.equalsIgnoreCase(artist + title)) {
				// no way to check for MP3 or OGG
				
				String id = referrer.substring(0, referrer.lastIndexOf("/"));
				id = id.substring(id.indexOf("/doc/") + 5);
				System.out.println("ID=" + id);
				
				// http://www.esnips.com/nsdoc/" + id + "/?action=forceDL
				String temp = GetUrl.getURL("http://www.esnips.com/nsdoc/" + id + "/?action=forceDL");
				try {
					FileWriter fw = new FileWriter("esnips.html");
					fw.write(temp);
					fw.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				System.out.println("GOT " + temp.length() + " bytes");
				System.exit(0);
			}
			
			istart = page.indexOf("<a href=\"/doc/", istop);
		} // end of while loop
		
		return 0;
	}
}