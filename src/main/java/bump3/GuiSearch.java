package bump3;
import bump3.engines.*;

/** the Gui.java would lock up when running a search,
 *  so I created this threaded class to do the search within
 *  this class implements runnable, so we are able to avoid the Gui lockup problem
 */
public class GuiSearch implements Runnable {
	public Thread t;
	public static boolean STOP = false;
	public String artist = "", title = "";
	
	/** constructor, sets the artist and title
	 *  @param art the artist we are going to search for
	 *  @param tit the title of the song we want
	 */
	public GuiSearch(String art, String tit) {
		STOP = false;
		artist=art;
		title=tit;
		t = new Thread(this, "GuiSearch");
	}
	
	/** threaded search
	 *  only engines that are checked in the mnuEngines[] array are searched
	 */
	public void run() {
		STOP = false;
		
		int searchResult = -1;
		
		int enginesTotal = 0, enginesCurrent = 0;
		for (int i = 0; i < Gui.mnuEngines.length; i++) {
			if (Gui.mnuEngines[i].getState())
				enginesTotal++;
		}
		
		// loop through every engine in the GUI's menu
		for (
			int i = 0;
			i < Gui.mnuEngines.length &&
				searchResult < 1 &&
				STOP == false;
			i++
		) {
			if (Methods.GuiStop())
				break;
			
			if (Gui.mnuEngines[i].getState()) {
				
				enginesCurrent++;
				Gui.progBar.setValue( (100 * enginesCurrent) / enginesTotal);
				
				switch (i) {
				case 0:
					// dilandau.com
					searchResult = Dilandau.search(artist,title);
					break;
				case 1:
					// mp3skull.com
					searchResult = Mp3skull.search(artist, title);
					break;
				case 2:
					// pepperoni-mp3.com
					searchResult = Pepperoni.search(artist, title);
					break;
				case 3:
					// dreammedia.ru
					searchResult = Dreammedia.search(artist,title);
					break;
				case 4:
					// seekasong.com
					searchResult = Seekasong.search(artist,title);
					break;
				case 5:
					// google.com
					searchResult = Google.search(artist, title);
					break;
				case 6:
					// emp3world.com
					searchResult = Emp3World.search(artist,title);
					break;
				case 7:
					// findmp3s.com
					searchResult = Findmp3s.search(artist,title);
					break;
				case 8:
					// oth.net
					searchResult = Oth.search(artist,title);
					break;
				case 9:
					// espew.net
					searchResult = Espew.search(artist,title);
					break;
				case 10:
					// mp3-center.org
					searchResult = Mp3center.search(artist,title);
					break;
				case 11:
					// downloads.nl
					searchResult = Downloadsnl.search(artist,title);
					break;
				case 12:
					// 4shared.com
					searchResult = FourShared.search(artist,title);
					break;
				case 13:
					// prostopleer.com
					searchResult = Prostopleer.search(artist,title);
					break;
				case 14:
					// myzuka.ru
					searchResult = Myzuka.search(artist,title);
					break;
				/*case 9:
					// esnips.com
					searchResult = Esnips.search(artist,title);
					break;*/
				}
			}
		} // end of for-loop through every engine
		
		Gui.progBar.setValue(0);
		
		if (Methods.GuiStop()) {
			//Methods.status("stopped; inactive");
			Methods.status("inactive");
			
		} else if (searchResult == -1) {
			Methods.status("not found");
			
		} else if (searchResult == 1) {
			// Gui.progBar.setValue(100);
			Methods.status("song" + (Gui.SONGS ? "s" : "") + " downloaded");
		} else {
			Methods.status("not found");
		}
		
		/*STOP = true;
		Gui.btnSearch.setEnabled(true);
		Gui.btnStop.setEnabled(false);
		*/
		// not so fast!
		
		// need to check the queue for more
		Gui.lstModel.remove(0);
		Gui.checkQueue();
		
	} // end of run method
	
} // end of class
