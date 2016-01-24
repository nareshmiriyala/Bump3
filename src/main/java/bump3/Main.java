package bump3;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * arch:3F96E32739
 * <p/>
 * BuMP3 - java mp3 downloader
 * <p/>
 * started out as an automated tool that searches google for mp3's and downloads the files automatically
 * now uses other mp3 search sites as well as google
 * <p/>
 * *** TO DO ***
 * <p/>
 * convert the entire project to use packages
 * -already successfully did it (../bump3.test/)
 * -move all search engine classes into subdirectory 'engines'
 * -googlecode may not like it ... maybe not
 * -would end up with svn/trunk/bump3/bump3/
 * <p/>
 * make and make install files, maybe. omeglespy had them, not sure how useful they are
 * <p/>
 * VERY verbose mode needs to be just that
 * -output EVERYTHING to the user
 * <p/>
 * new search engines
 */
// for user input
// for path
// for CHANGELOG

public class Main {

	// change AFTER we commit a new revision!
    public static int     REVISION     = 58;
	
	public static String  PATHSEP      = System.getProperty("file.separator");
	public static int     MIN_FILESIZE = 2 * 1024 * 1024; // 2 MB default
	public static String  SAVE_DIR     = "songs" + PATHSEP;  // current_folder/songs/
	public static String  PROG_DIR     = "";        // current program's directory; where the .ini is located
	public static String  FILE_NAME    = "";        // current .jar file name
	public static boolean SEARCH_MP3   = true;      // these two were more of an afterthought
	public static boolean SEARCH_OGG   = true;      // most of the files are mp3, so i never gave the user a choice on these
	public static boolean AUTOPLAY     = true; // start playing song
	public static int     AUTOPLAY_WAIT= 10;   // percentage to wait for
	public static String USER_AGENT    = 
			"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
	
	public static String PLAYSTRING   = "";
	public static String STOPSTRING   = "";
	
	public static int CONNECT_TIMEOUT = 8000; // max time to wait for site to connect, in milliseconds
	public static int READ_TIMEOUT    = 8000; // max time to wait for site to send data in milliseconds
	
	public static boolean[] ENGINES	  = new boolean[]{true,true,true,true,true,false,true,true,true,true,true,true,true,true,true};
	public static String[] RECENTS    = new String[]{};
	
	// COLORS, for use in the linux console only
	public static String W  = "\033[0m";  // white (normal)
	public static String BLA= "\033[30m"; // black
	public static String R  = "\033[31m"; // red
	public static String G  = "\033[32m"; // green
	public static String BR = "\033[33m"; // brown
	public static String B  = "\033[34m"; // blue
	public static String P  = "\033[35m"; // purple
	public static String C  = "\033[36m"; // cyan
	public static String GR = "\033[37m"; // gray
	
	// the following variables are used when receiving command-line arguments
	public static String ARTIST = "";
	public static String TITLE  = "";
	public static String ALBUM  = "";	
	public static String ENGINE = "";
	
	public static Gui theGUI = null;
	
	public static String OS = "Linux";
	
	public static boolean QUIET = true;
	public static boolean VERBOSE = false;
	public static boolean GUI = true;
	public static boolean THEME = false;
	public static boolean AGREED = false;
	public static boolean NOSPACES = false;
	
	public static boolean NODUPES = false;
	public static String[] DOWNLOADED = new String[] {};
	
	//public static boolean USE_COOKIES = false;
	//public static String COOKIE = ""; // deprecated feature
	
	/** prints text
	 * @param txt text to print
	 */
	public static void p(String txt) {
		Methods.p(txt);
	}
	
	/** initializes variables
		checks the operating system
		finds default media players
		loads a random user agent string
	 */
	public static void initialize() {
		// OS check
		String dir = "";
		
		// get path to the .JAR file
		try {
			dir = (new File (Main.class.getProtectionDomain().getCodeSource().getLocation().toURI())).getPath();
			FILE_NAME = dir.substring(dir.lastIndexOf(PATHSEP) + 1);
			dir = dir.substring(0, dir.lastIndexOf(PATHSEP));
			if (!dir.endsWith(PATHSEP))
				dir += PATHSEP;
		} catch (URISyntaxException use) {
			use.printStackTrace();
			try {
				dir = new File(".").getCanonicalPath();
			} catch (IOException ioe) { ioe.printStackTrace(); }
		}
		if (!dir.substring(dir.length() -1).equals(PATHSEP))
			dir += PATHSEP;
		
		OS = System.getProperty("os.name");
		if ( OS.equals("Linux") || OS.startsWith("Mac") ) {
			// linux machine
			// check for mp3 players (vlc, mpg123, totem)
			String[][] apps = new String[][]{
				{"vlc", "--one-instance --play-and-exit --playlist-enqueue %s"}, 
				{"mpg123", "%s"},
				{"mp3-decoder", "%s"},
				{"mp3-blaster", "%s"},
				{"totem", "%s"},
				{"afplay", "%s"},
			};
			String found = "";
			Runtime runtime = Runtime.getRuntime();
			for (int i = 0; i < apps.length && found.equals(""); i++) {
				try {
					if (runtime.exec(new String[] {"which", apps[i][0]}).waitFor() == 0) {
						found = apps[i][0] + " " + apps[i][1];
						break;
					}
				} catch (InterruptedException ie) {
				} catch (IOException ioe) {}
			}
			
			if (!found.equals(""))
				PLAYSTRING = found;
			else
				PLAYSTRING = "open %s";

		} else if ( OS.indexOf("Windows") >= 0) {
			// Windows
			OS = "Windows";
			
			//dir.replaceAll("\\\\", "\\\\\\\\"); // LOL
			PLAYSTRING = "explorer \"%s\"";
			String[][] apps = new String[][] {
				{"VideoLAN\\vlc.exe", "--one-instance --playlist-enqueue %s"},
				{"VideoLAN\\VLC\\vlc.exe", "--one-instance --playlist-enqueue %s"},
				{"VLC\\vlc.exe", "--one-instance --playlist-enqueue %s"},
				{"Windows Media Player\\wmplayer.exe", "/play /close %s"},
				{"Winamp\\winamp.exe", "/ADD %s"},
				{"iTunes\\iTunes.exe", "%s"},
				{"QuickTime\\QuickTimePlayer.exe", "/Play %s"}
			};
			
			for (int i = 0; i < apps.length; i++) {
				// check for both regular program files and non-64-bit prog files
				if (Methods.fileExists("C:\\Program Files\\"       + apps[i][0])) {
					
					PLAYSTRING = "C:\\Program Files\\" + apps[i][0] + " " + apps[i][1];
					break;
				} else if (Methods.fileExists("C:\\Program Files (x86)\\" + apps[i][0])) {
					PLAYSTRING = "C:\\Program Files (x86)\\" + apps[i][0] + " " + apps[i][1];
					break;
				}
			}
			
			//System.out.println("PLAYSTRING=" + PLAYSTRING);
			// fix color scheme
			W=""; BLA=""; R=""; G=""; BR=""; B=""; P=""; C=""; GR="";
		
		} else {
			W=""; BLA=""; R=""; G=""; BR=""; B=""; P=""; C=""; GR="";
			System.out.println("Unknown operating system: " + OS);
			int answer = JOptionPane.showOptionDialog(
				null,
				"you are running an unsupported operating system: " + OS + "\n\n" +
				"                      do you want to continue anyway?\n\n" +
				"WARNING: BuMP3 *may* not work correctly. it can't hurt to try!",
				"BuMP3 - Error",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.ERROR_MESSAGE,
				null,
				new String[] {"yes", "no"},
				"yes"
				);
			
			if (answer == 1)
				System.exit(0);
			PLAYSTRING = "open %s";
		}
		
		try {
			(new File(dir)).mkdirs();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		PROG_DIR = dir;
		SAVE_DIR = dir + "songs" + PATHSEP;
		
		// create songs directory if it doesn't already exist!
		new File(SAVE_DIR).mkdirs();
		
		// set user agent
		String[] temp = new String[] {
			"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.8.1.6) Gecko/20070725 Firefox/2.0.0.6",
			"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)",
			"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)",
			"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322)",
			"Mozilla/4.0 (compatible; MSIE 5.0; Windows NT 5.1; .NET CLR 1.1.4322)",
			"Opera/9.20 (Windows NT 6.0; U; en)",
			"Mozilla/5.0 (Windows; U; Windows NT 6.1; ru; rv:1.9.2.3) Gecko/20100401 Firefox/4.0 (.NET CLR 3.5.30729)"
		};
		USER_AGENT = temp[new java.util.Random().nextInt(temp.length)];
	}
	
	/** where the magic happens
	 *  the interactive mode all happens within the main
	 *  if the GUI is loaded, the code is then passed onto Gui.java
	 *  @param args argument switches to be applied at command-line
	 */
	public static void main(String[] args) {
		
		initialize();
		
		/*Plugin pl = new Plugin("plugin.txt");
		pl.searchPlugin("kansas", "dust in the wind");
		System.exit(0);
		*/
		
		if (args.length > 0) {
			handleArgs(args);
		}
		
		printbanner();
		
		Methods.loadSettings();
		
		/*
		while (!AGREED) {
			TOS tos = new TOS();
			tos.t.start();
			while (tos.isVisible()) { }
			
			// if they didn't agree, exit!
			if (!AGREED) {
				System.exit(0);
			}
		}*/
		
		if (GUI == true) {
			// GUI needs to be loaded
			theGUI = new Gui();
			theGUI.run();
			return;
		}
		
		// hide splash screen if we're only doing command-line work!
		SplashScreen ss = SplashScreen.getSplashScreen();
		if (ss != null) 
			ss.close();
		
		Methods.saveSettings("");
		
		int searchResult;
		Scanner console = new Scanner(System.in);
		String artist="", title="", album="";
		if ( !ARTIST.equals("") && !TITLE.equals("") ) {
			// artist + title
			artist = ARTIST;
			title = TITLE;
			Methods.p(GR+"[+] Artist: "+G+artist+GR+"; Title: "+G+title+GR+"");
		
		} else if ( !ARTIST.equals("") && !ALBUM.equals("") ) {
			// artist + title
			// attempt to download an entire album! yikes!
			artist = ARTIST;
			album = ALBUM;
			searchResult = Methods.searchAlbum(artist, album);
			switch (searchResult) {
				case 0:
					// could not find
					break;
				case 1:
					// found!
					break;
				default:
					// unknown error
			}
			System.exit(0);
		} else {
			// don't have enough info; ask the user!
			if (QUIET == true) {
				// quiet mode is on, not enough info, so gtfo
				System.exit(0);
			}
			Methods.pr(GR+"[+]"+GR+" Enter artist name: "+G);
			artist = console.nextLine().toLowerCase();
			Methods.pr(GR+"[+]"+GR+" Enter song title:  "+G);
			title = console.nextLine().toLowerCase();
		}
		
		searchResult = Methods.searchSong(artist, title);
		switch(searchResult) {
			case 0:
				// no results found
				p(GR+"[+]"+R+" Unable to locate song.");
				break;
			case 1:
				// download successful
				//Methods.p(GR+"[+]"+B+" Download "+G+"Successful!");
				System.exit(0);
				break;
			default:
				// weird error
				p(GR+"[+]"+BR+" Unexpected result: " +R+ searchResult);
		}
	}
	
	/** prints the program's banner to console
	*/
	public static void printbanner() {
		int ban = new java.util.Random().nextInt(5);
		p("");
		switch (ban) {
		case 0:
			p(G+"  /\\,      "+W+"B  "+W+"u  "+W + "M  "+W+"P  "+W+"3       "+G+",/\\");
			p(G+"  \\,                            ,/");
			p(GR+"       java music downloader       ");
			break;
		case 1:
			p(G + "      _/_/_/              _/      _/  _/_/_/    _/_/_/    ");
			p(G + "     _/    _/  _/    _/  _/_/  _/_/  _/    _/        _/   ");
			p(G + "    _/_/_/    _/    _/  _/  _/  _/  _/_/_/      _/_/      ");
			p(G + "   _/    _/  _/    _/  _/      _/  _/              _/     ");
			p(G + "  _/_/_/      _/_/_/  _/      _/  _/        _/_/_/        ");
			p(G + "                                                          ");
			p(GR+ "               java  music  downloader                    ");
			break;
		case 2:
			p(R+ "  @@@@@@@   @@@  @@@  @@@@@@@@@@   @@@@@@@   @@@@@@   ");
			p(R+ "  @@@@@@@@  @@@  @@@  @@@@@@@@@@@  @@@@@@@@  @@@@@@@  ");
			p(R+ "  @@!  @@@  @@!  @@@  @@! @@! @@!  @@!  @@@      @@@  ");
			p(R+ "  !@   @!@  !@!  @!@  !@! !@! !@!  !@!  @!@      @!@  ");
			p(R+ "  @!@!@!@   @!@  !@!  @!! !!@ @!@  @!@@!@!   @!@!!@   ");
			p(R+ "  !!!@!!!!  !@!  !!!  !@!   ! !@!  !!@!!!    !!@!@!   ");
			p(R+ "  !!:  !!!  !!:  !!!  !!:     !!:  !!:           !!:  ");
			p(R+ "  :!:  !:!  :!:  !:!  :!:     :!:  :!:           :!:  ");
			p(R+ "   :: ::::  ::::: ::  :::     ::    ::       :: ::::  ");
			p(R+ "  :: : ::    : :  :    :      :     :         : : :   ");
			p("");
			p(GR+"                java  music  downloader               ");
			break;
		case 3:
			p(R + "    _____ "+G+"   "+BR+" _____ "+B+"_____ "+C+"___ ");
			p(R + "   | __  |"+G+"_ _"+BR+"|     |"+B+"  _  |"+C+"__ |");
			p(R + "   | __ -|"+G+" | |"+BR+" | | |"+B+"   __|"+C+"__ |");
			p(R + "   |_____|"+G+"___|"+BR+"_|_|_|"+B+"__|  "+C+"|___|");
			p("");
			p(GR+ "      java music downloader   ");
			break;
		case 4:
			p(W+" ____ ____ ____ ____ ____ ");
			p(W+"||B |||u |||M |||P |||3 ||");
			p(W+"||__|||__|||__|||__|||__||");
			p(W+"|/__\\|/__\\|/__\\|/__\\|/__\\|");
			p("");
			p(GR+ "  java music downloader  ");
			break;
		}
		p(W);
	}
	/** handles arguments
	 *  @param args string array of arguments to read through
	 */
	public static void handleArgs(String[] argz) {
		String[] args = argz;
		char last = ' ';
		
		// fix arguments (if passed to /usr/bin/
		if (args.length == 1 && args[0].indexOf(" ") >= 0) {
			// invalid args! must make them work!
			String full = "";
			boolean inQuotes = false;
			for (int i = 0; i < args[0].length(); i++) {
				if (args[0].charAt(i) == ' ' && !inQuotes)
					full += "|";
				else if (args[0].charAt(i) == '"')
					inQuotes = !inQuotes;
				else
					full += args[0].charAt(i);
			}
			args = full.split("\\|");
		}
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("help") || args[i].equals("-help") || args[i].equals("--help") || 
				args[i].equals("?") || args[i].equals("-?") || args[i].equals("-h") || 
				args[i].equals("h") ) {
				
				QUIET=false;
				printbanner();
				
				p(W+"\nABOUT");
				p(GR+"  this program downloads song files using online search engines\n");
				p(W+"WEBSITE");
				p(B+"  http://bump3.googlecode.com\n");
				p(W+"VERSION");
				p(GR+"  BuMP3 v0.1 r" + REVISION + "\n");
				p(W+"USAGE");
				p(BR+"  java -jar " + FILE_NAME + " "+G+"[OPTIONS]");
				p(GR+"  run bump3 without options for skinned gui mode\n");
				p(W+"OPTIONS");
				p(G+"  -h  "+GR+"show this help screen\n");
				p(G+"  -g  "+GR+"BuMP3's GUI mode (non-command-line)");
				p(G+"  -s  "+GR+"GUI mode with skinned theme         "+BR+"*default*");
				p(G+"  -i  "+GR+"interactive mode (command-line)\n");
				p(G+"  -v  "+GR+"verbose - as verbose as -i mode");
				p(G+"  -V  "+GR+"VERY verbose mode - lots of info on search results");
				p(G+"  -q  "+GR+"quiet - warning: no output at all   "+BR+"*default*\n");
				p(G+"  -a  "+GR+"artist name (interactive)");
				p(G+"  -t  "+GR+"song title  (interactive)");
				p(G+"  -e  "+GR+"search engine to use (google, pepperoni, etc)");
				p(G+"      "+GR+"default is all engines.\n");
				p(G+"  -c  "+GR+"show the program's change log\n");
				
				p(W+"ENGINES");
				Methods.pr(B+"  dreammedia.ru,");
				Methods.pr(B+" espew.net,");
				Methods.pr(B+" findmp3s.com,");
				Methods.pr(B+" google.com,");
				Methods.pr(B+" oth.net,\n ");
				Methods.pr(B+" seekasong.com,");
				Methods.pr(B+" dilandau.eu,");
				Methods.pr(B+" pepperoni-mp3.com,");
				Methods.pr(B+" emp3world.com,\n ");
				Methods.pr(B+" mp3skull.com,");
				Methods.pr(B+" mp3-center.org,");
				Methods.pr(B+" downloads.nl,");
				Methods.pr(B+" 4shared.com,");
				Methods.pr(B+" prostopleer.com,");
				Methods.pr(B+" myzuka.ru,");
				Methods.p(W+"\n");
				
				System.exit(0);
				
			} else if (args[i].equals("changes") || args[i].equals("c") || args[i].equals("-c") ) {
				QUIET=false;
				printbanner();
				
				// open the file "CHANGES" located in the jar as a stream
				// print the stream
				InputStream in = null;
				try {
					JarFile jar = new JarFile(PROG_DIR + FILE_NAME);
					ZipEntry entry = jar.getEntry("CHANGES");
					
					in = new BufferedInputStream(jar.getInputStream(entry));
					Scanner reader = new Scanner(in);
					
					while (reader.hasNext()) {
						String line = reader.nextLine();
						if (line.equals("CHANGE LOG"))
							line = G + line;
						else if (line.trim().startsWith("-"))
							line = GR + line;
						else
							line = W + line;
						System.out.println(line);
					}
					System.out.println();
				} catch (NullPointerException npe) {
					System.out.println("Error! " + npe.getMessage());
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} finally {
					try {
						if (in != null)
							in.close();
					} catch (IOException ioe) {}
				}
				System.exit(0);
				
			} else if (args[i].equals("-a") && i + 1 < args.length) {
				GUI = false;
				QUIET = false;
				ARTIST = args[i+1];
				i+=1;
				last = 'a';
				
			} else if (args[i].equals("-t") && i + 1 < args.length) {
				GUI = false;
				QUIET = false;
				TITLE = args[i+1];
				i+=1;
				last = 't';
				
			} else if (args[i].equals("-e") && i + 1 < args.length) {
				GUI = false;
				QUIET = false;
				ENGINE = args[i+1];
				i+=1;
				last = 'e';
				
			} else if (args[i].equals("-album") && i + 1 < args.length) {
				ALBUM = args[i+1];
				i+=1;
				
			} else if (args[i].equals("-q")) {
				// quiet
				QUIET = true;
				
			} else if (args[i].equals("-i")) {
				// interactive mode
				QUIET = false;
				GUI = false;
				
			} else if (args[i].equals("-v")) {
				//verbose mode
				QUIET = false;
			
			} else if (args[i].equals("-V")) {
				// VERY verbose mode
				QUIET = false;
				VERBOSE = true;
			
			} else if (args[i].equals("-s")) {
				// themed!
				THEME = true;
				GUI = true; // turn GUI on automatically
				
			} else if (args[i].equals("-u")) {
				// upgrade!
				QUIET = false;
				printbanner();
				
				p(GR+"[+]"+W+" Checking project homepage for "+G+"UPGRAYEDD"+GR+"...");
				String up = Methods.getLatestRevisionChanges();
				
				if (up.equals(""))
					p(GR+"[+]"+R+" Unable to check for updates at this time"+W);
					
				else if (up.equals("up2date")) 
					p(GR+"[+]"+G+" You are already running the latest version of BuMP3");
					
				else {
					p(GR+"[+]"+G+" A new update is available!"+W);
					p(GR+"[+]"+W+" Details about the upgrade are below:\n"+W);
					p(GR+"   "+up.replaceAll("\n", "\n    "));
					p("");
					
					Methods.p(GR+"[+]"+W+" Do you want to download and upgrade to the latest version?");
					Methods.pr(GR+"[+]"+W+" Enter "+G+"y"+W+" to upgrade, "+G+"n"+W+" to exit: "+G);
					Scanner console = new Scanner(System.in);
					String ans = console.nextLine();
					
					if (ans.toLowerCase().charAt(0) == 'y') {
						if (Methods.upgrayedd("http://bump3.googlecode.com/svn/trunk/bump3/bump3.jar"))
							p(GR+"[+]"+G+" Upgrade complete!");
							
						else
							p(GR+"[+]"+R+" Unable to upgrade");
					}
				}
				System.exit(0);
			
			} else if ( args[i].equals("-g") || 
						args[i].equals("-gui") || 
						args[i].equals("--gui") || 
						args[i].equals("gui") ) {
				GUI = true;
				// QUIET = true;
			} else {
				switch(last) {
				case 'a':
					ARTIST += " " + args[i];
					break;
				case 't':
					TITLE += " " + args[i];
					break;
				case 'e':
					ENGINE += " " + args[i];
					break;
				default:
					// display help?
					args[i] = "-h";
					i--;
				}
				
			}
		}
		
	}
}
