package tray.java;

import java.awt.PopupMenu;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import tray.ImageLoader;
import tray.TrayIconAdapter;

public class JavaIconAdapter implements TrayIconAdapter {

	private final TrayIcon trayIcon;

	public JavaIconAdapter(URL imageUrl, String tooltip, PopupMenu popup) {
		trayIcon = new TrayIcon(ImageLoader.load(imageUrl), tooltip, popup);
	}

	@Override
	public void displayMessage(String caption, String text, MessageType messageType) {
		trayIcon.displayMessage(caption, text, messageType);
	}

	@Override
	public void setImageAutoSize(boolean autosize) {
		trayIcon.setImageAutoSize(autosize);
	}

	@Override
	public void addMouseListener(MouseListener mouseListener) {
	    trayIcon.addMouseListener(mouseListener);
	}
	
	@Override
	public void setImage(URL imageUrl) {
		trayIcon.setImage(ImageLoader.load(imageUrl));
	}

	public TrayIcon getTrayIcon() {
		return trayIcon;
	}
}
