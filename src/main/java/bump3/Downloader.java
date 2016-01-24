package bump3;

import java.io.*;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

/** class designed purely for downloading a file */
public class Downloader {
	
	/** downloads a song from theURL, saves it as saveAs 
		@param theURL location of the file to download
		@param saveAs location of the file to save to
		@param size   assuming we have the total file size, 
						this is needed for the progress bars
		
		@return number, 0 if successful, negative if unsuccessful;
		  -1 : BAD URL - unable to form url
		  -2 : CANNOT OPEN STREAM - unable to open url stream
		  -3 : FILE NOT FOUND - i really doubt we'll get this error ever
		  -4 : IOEXCEPTION - something went wrong while reading from the site or writing to disk.
	*/
	public static int download(String theURL, String saveAs, int size) {
		String save = saveAs;
		
		// check for duplicates
		if (Main.NODUPES && Methods.hasBeenDownloaded(theURL)) {
			Methods.p(Main.GR + "\n[+] " + Main.R+"Previously downloaded; moving on");
			return -1;
		}
		
		// check if we need to trim spaces from the filename
		if (Main.NOSPACES) {
			// change saveAs to not have spaces in the filename
			int i = save.lastIndexOf(Main.PATHSEP);
			save = save.substring(0, i + 1) + Methods.trimspaces(save.substring(i + 1));
		}
		
		// check if file already exists (don't overwrite)
		if (!Main.NODUPES && Methods.fileExists(save)) {
			// file already exists, save to a new file name
			String s;
			int i = 2;
			do {
				s = save.substring(0, save.length() - 4) + "-" + i + "" + save.substring(save.length() - 4);
				i++;
			} while (Methods.fileExists(s));
			save = s;
		}
		
		BufferedInputStream in;
		URLConnection uc;
		try {
			uc = new URL(theURL).openConnection();
			uc.setConnectTimeout(Main.CONNECT_TIMEOUT);
			uc.setReadTimeout(Main.READ_TIMEOUT);
			uc.setRequestProperty("User-Agent", Main.USER_AGENT);
			/*System.out.println("COOKIE=\"" + Main.COOKIE + "\"");
			uc.setRequestProperty("Cookie", Main.COOKIE);*/
			
			in = new BufferedInputStream(
				uc.getInputStream()
				);
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
			return -1;
		} catch (SocketTimeoutException ste) {
			return -5;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return -2;
		}
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(save);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			return -3;
		}
		
		Main.DOWNLOADED = Methods.add(Main.DOWNLOADED, theURL);
		
		int BYTE_SIZE = 2048;
		BufferedOutputStream bout = new BufferedOutputStream(fos,BYTE_SIZE);
		byte data[] = new byte[BYTE_SIZE];
		int count, current = 0, per = 0, perd = 0;
		
		Methods.status("downloading...");
		
		boolean played = false; // to know if we've started autoplaying yet
		try {
			long timer = System.currentTimeMillis(), lastSize = 0;
			while( (count = in.read(data,0,BYTE_SIZE)) != -1)
			{
				bout.write(data,0,count);
				
				if (Methods.GuiStop()) // user clicked stop!
					return -5;
				
				current += count;
				lastSize += count;
				per = (100 * current) / size;
				Methods.progbar(per);
				if (Main.AUTOPLAY && per >= Main.AUTOPLAY_WAIT && !played) {
					played = true;
					String app = Main.PLAYSTRING;
					if (app.indexOf(" ") >= 0)
						app = app.substring(0, app.indexOf(" "));
					Methods.p("\r"+Main.GR+"[+] "+Main.GR+"Music:      "+Main.G+ 
								"Auto-played song at "+Main.AUTOPLAY_WAIT+"%"+
								"                                                      "+
								Main.W);
					// need the long blank line to cover up the run-off
					
					Methods.playSong(save);
				}
				Methods.pr("\r"+Main.GR+"[+]"+Main.GR+
								" Progress:  "+Main.G+
								" " + per + "% "+Main.GR+
								"(" + Methods.bytesToSize(current) + "/" + Methods.bytesToSize(size) + ") "+
								Main.G+"[");
				// every 5%, print a bar | or a space
				perd = per / 5;
				for (int i = 0; i < 20; i++) {
					if (i < perd) 
						Methods.pr("=");
					else
						Methods.pr(" ");
				}
				Methods.pr("]"+Main.BLA);
				
				// this is for finding speed of download (kbps) and estimated time of completion
				long temptimer = System.currentTimeMillis();
				if (temptimer - timer > 3000) {
					lastSize = lastSize / 1024;         // convert data received in last 3 seconds to KB
					timer = (temptimer - timer) / 1000; // change timer to ~3 seconds
					int kbs = ((int)lastSize / (int)timer); // get kbps
					int bps = kbs * 1024; // find the bytes per second
					
					// don't divide by zero!!!
					if (bps == 0)
						Methods.pr(" "+Main.GR+ "ETA: Infinity @ "+kbs+"kbps   ");
					else {
						int remain = (size - current) / bps; // calculate seconds before completion
						Methods.pr(" "+Main.GR+ "ETA: "+Methods.secToTime(remain)+" @ "+kbs+"kbps        ");
					}
					lastSize = 0;
					timer = System.currentTimeMillis();
				}
			}
			bout.close();
			in.close();
			
			Methods.pr("\n"+Main.GR+"[+]"+Main.G+" Download complete");
			if (Main.AUTOPLAY && !played) {
				String app = Main.PLAYSTRING;
				if (app.indexOf(" ") >= 0)
					app = app.substring(0, app.indexOf(" "));
				Methods.p("; "+Main.G+
							"Auto-playing"+
							(Main.STOPSTRING.equals("") 
							? "" 
							: Main.B+" (type "+Main.R+Main.STOPSTRING+Main.B+ " to stop)"+Main.W) 
						  );
				
				Methods.playSong(save);
				
			} else {
				Methods.p(Main.W);
			}
			
		} catch (SocketTimeoutException ste) {
			// socket timed out!
			return -6;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return -4;
			
		} finally {
			try {
				bout.close();
			} catch (IOException ioe){}
			
			try {
				in.close();
			} catch (IOException ioe){}
		}
		
		// only add this file to the recents list if it's NOT the upgrade file!
		if (save.indexOf("bump3_new.jar") < 0) {
			Main.RECENTS = Methods.add(Main.RECENTS, save);
			try {
				Main.theGUI.reloadRecents();
			} catch (NullPointerException npe) {}
		}
		
		return 0;
		
		/*if (Math.abs(size - current) < 10240) {
			// if the difference between what we downloaded and the real file is less than 10kb...
			return 0;
		} else {
			// we didn't download all of it! return false
			return -1;
		}*/
	}
}