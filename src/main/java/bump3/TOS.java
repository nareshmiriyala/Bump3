package bump3;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.EventListener;
import java.util.Scanner;

/** Terms of Service for this program and every website used by the program
 *  this is mostly to make sure that the users of this program understand
 *  the agreements requried to use the other websites.
 */
public class TOS extends JFrame implements Runnable, WindowListener, ActionListener, EventListener, ListSelectionListener {
	Thread t = null;
	public String[] SITES;
	public String[] TOSES;
	
	public boolean THEME = Main.THEME;
	public Font FONT = new Font("Default", Font.BOLD, 15);
	public Color BGCOLOR = null;
	public Color FGCOLOR = null;
	
	public JList 			lst 		= null;
	public DefaultListModel lstModel 	= null;
	public JTextArea 		txt 		= null;
	public JScrollPane		textScroll	= null;
	public JButton			btnAccept	= null;
	public JButton			btnDecline	= null;
	
	/** constructor, builds the window*/
	public TOS() {
		super("BuMP3 - Terms of Service Agreement");
		setSize(480, 500);
		setLocationRelativeTo(null);
		
		setResizable(false);
		
		if (THEME) {
			// load font from JAR, set it as the default font
			try {
				FONT = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream("uni0553.TTF")); // 04B.TTF
				FONT = FONT.deriveFont(16.0f);
			} catch (FontFormatException ffe) {
				ffe.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			BGCOLOR = Color.DARK_GRAY;
			FGCOLOR = Color.LIGHT_GRAY;
			
			getContentPane().setBackground(BGCOLOR);
			getContentPane().setForeground(FGCOLOR);
		}
		
		setLayout(new FlowLayout());
		setFont(FONT);
		
		buildControls();
		
		doLayout();
		
		setVisible(true);
		
		do {
			loadTOS();
		} while (lstModel.getSize() == 0);
		lst.setSelectedIndex(0);
		
		t = new Thread(this, "TOS");
	}
	
	/** needed by Runnable for threading
	 */
	public void run() {
		
	}
	
	/** loads the terms of services into two arrays
	 */
	public void loadTOS() {
		Scanner reader = new Scanner(
					this.getClass().getResourceAsStream("TOS.txt")
					);
		String site = "", tos = "";
		int count = 0;
		SITES = new String[12];
		TOSES = new String[12];
		while (reader.hasNext()) {
			String line = reader.nextLine();
			if (line.length() > 6 && 
				line.substring(0, 3).equals("(((") && 
				line.substring(line.length() - 3).equals(")))")) {
				// new site's TOS
				if (!site.equals("")) {
					SITES[count] = site;
					TOSES[count] = tos;
					lstModel.addElement(site);
					count++;
				}
				site = line.substring(3, line.length() - 3);
				tos = "";
			} else {
				tos += line + "\n";
			}
		}
	}
	
	/** creates and adds the components used by the form
	 */
	public void buildControls() {
		// window is 480x500
		
		lstModel = new DefaultListModel();
		lst = new JList(lstModel);
		FONT = FONT.deriveFont(14.0f);
		lst.setFont(FONT);
		FONT = FONT.deriveFont(16.0f);
		if (BGCOLOR != null) lst.setBackground(BGCOLOR);
		if (FGCOLOR != null) lst.setForeground(FGCOLOR);
		
		lst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lst.addListSelectionListener(this);
		lst.setMinimumSize(new Dimension(140, 460));
		JScrollPane listScroll = new JScrollPane(lst);
		listScroll.setPreferredSize(new Dimension(140, 460));
		if (THEME) {
			lst.setBackground( new Color(0.0f, 0.0f, 0.0f, 0.0f));
			lst.setOpaque(false);
			lst.setSelectionBackground(BGCOLOR);
			listScroll.setOpaque(false);
			listScroll.getViewport().setOpaque(false);
		}
		//add(lst);
		add(listScroll);
		lst.addListSelectionListener(this);
		
		JPanel jp = new JPanel();
		jp.setPreferredSize(new Dimension(320, 460));
		jp.setBackground(BGCOLOR);
		//jp.setOpaque(false);
		jp.setLayout(new FlowLayout());
		add(jp);
		
		txt = new JTextArea();
		txt.setEditable(false);
		txt.setLineWrap(true);
		txt.setWrapStyleWord(true);
		FONT = FONT.deriveFont(12.0f);
		txt.setFont(FONT);
		FONT = FONT.deriveFont(16.0f);
		if (BGCOLOR != null) txt.setBackground(BGCOLOR);
		if (FGCOLOR != null) txt.setForeground(FGCOLOR);
		txt.setBorder(BorderFactory.createEtchedBorder(0));
		textScroll = new JScrollPane(txt);
		textScroll.setPreferredSize(new Dimension(290, 405));
		textScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		//txt.setPreferredSize(new Dimension(300, 415));
		jp.add(textScroll);
		
		btnAccept = new JButton("accept all");
		btnAccept.setFont(FONT);
		if (BGCOLOR != null) btnAccept.setBackground(BGCOLOR);
		if (FGCOLOR != null) btnAccept.setForeground(FGCOLOR);
		btnAccept.setPreferredSize(new Dimension(130, 25));
		jp.add(btnAccept);
		btnAccept.addActionListener(this);
		
		btnDecline = new JButton("decline");
		btnDecline.setFont(FONT);
		if (BGCOLOR != null) btnDecline.setBackground(BGCOLOR);
		if (FGCOLOR != null) btnDecline.setForeground(FGCOLOR);
		btnDecline.setPreferredSize(new Dimension(100, 25));
		jp.add(btnDecline);
		btnDecline.addActionListener(this);
		
		jp.doLayout();
	}
	
	/** event occurrs when a component that has added this class as an action listener
	 *  @param event object with information on the action event
	 */
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == btnAccept) {
			Main.AGREED = true;
			setVisible(false);
			dispose();
			
		} else {
			Main.AGREED = false;
			int answer = JOptionPane.showOptionDialog(
				null,
				"BuMP3 will not open until you have agreed to the Terms of Service\n" +
				"for every website used by this program.  do you want to go back and agree?",
				"BuMP3 - Error",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.ERROR_MESSAGE,
				null,
				new String[] {"yes, I will read it again", "no, I refuse"},
				"yes, I will read it again"
			);
			if (answer == 1) {
				setVisible(false);
				dispose();
			}
		}
	}
	
	/** event occurs when a component has added this class as a list selection event listener
	 *  @param event object with information on the event
	 */
	public void valueChanged(ListSelectionEvent event) {
		int i = lst.getSelectedIndex();
		if (i >= 0 && i < lstModel.getSize()) {
			txt.setText(TOSES[i]);
			txt.setSelectionStart(0);
			txt.setSelectionEnd(0);
			textScroll.updateUI();
		}
			
	}
	
	/** whole lot of window events
	 *  these needed to be overloaded because we inherit JFrame in this class
	 */
	public void windowOpened(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	/** event occurs when the window is closing
	 *  @param e object with information on the window event
	 */
	public void windowClosing(WindowEvent e) {
		
		dispose();
		System.exit(0);
	}
}