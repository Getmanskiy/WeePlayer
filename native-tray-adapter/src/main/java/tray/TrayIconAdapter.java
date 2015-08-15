package tray;

import java.awt.TrayIcon.MessageType;
import java.awt.event.MouseListener;
import java.net.URL;

public interface TrayIconAdapter {

	public void displayMessage(String caption, String text, MessageType messageType);

	public void setImageAutoSize(boolean autoSize);

	public void addMouseListener(MouseListener mouseListener);

	public void setImage(URL imageUrl);

}
