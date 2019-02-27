package ca.sqrlab.arc.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * The {@code ARCWindow} acts as a window for the user interface for ARC. It
 * contains a main content panel to contain a view (which is set through
 * {@link #setView(ARCView)}). Note: by default, if an ARC window is closed,
 * the entire program is terminated.
 * 
 * @author Devon McGrath
 * @see ARCView
 * @since 1.0
 */
public class ARCWindow extends JFrame {

	private static final long serialVersionUID = 3849129188201047713L;
	
	/** The default title for the ARC window. */
	public static final String DEFAULT_TITLE = "ARC";
	
	/** The panel which contains all the window's content. */
	private JPanel contentPanel;
	
	/**
	 * Creates an ARC window with the default width, height, and title.
	 * @since 1.0
	 */
	public ARCWindow() {
		this(getDefaultWidth(), getDefaultHeight(), DEFAULT_TITLE);
	}
	
	/**
	 * Creates an ARC window with the specified dimensions and title.
	 * 
	 * @param width		the width of the window.
	 * @param height	the height of the window.
	 * @param title		the title of the window.
	 * @since 1.0
	 */
	public ARCWindow(int width, int height, String title) {
		if (title != null) {
			super.setTitle(title);
		} else {
			super.setTitle(DEFAULT_TITLE);
		}
		super.setSize(width, height);
		super.setLocationByPlatform(true);
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		init();
	}
	
	/**
	 * Initializes the window, and its required components.
	 * @since 1.0
	 */
	private void init() {
		
		// Create the panel which contains all the content
		this.contentPanel = new JPanel(new GridLayout(1, 1, 0, 0));
		super.getContentPane().removeAll();
		super.getContentPane().add(contentPanel);
	}
	
	/**
	 * Sets the content of the window to the specified view. If the view is
	 * null, this method does nothing.
	 * 
	 * <p>The view's ARC window reference is updated to reference this window.
	 * 
	 * @param view	the view to show.
	 * @since 1.0
	 */
	public void setView(ARCView view) {
		if (view != null) {
			this.contentPanel.removeAll();
			this.contentPanel.add(view);
			view.window = this;
			this.contentPanel.setVisible(false);
			this.contentPanel.setVisible(true);
		}
	}
	
	/**
	 * Gets the main content panel for the window. This panel contains all of
	 * the window's content.
	 * 
	 * @return the content panel.
	 * @since 1.0
	 */
	public JPanel getContentPanel() {
		return contentPanel;
	}
	
	/**
	 * Gets the screen size of the user's device.
	 * 
	 * @return the dimensions of the screen.
	 * @since 1.0
	 */
	public static Dimension getScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}
	
	/**
	 * Gets the default width of a window, which is guaranteed not to be wider
	 * than the actual screen's width.
	 * 
	 * @return the default window width.
	 * @see #getDefaultHeight()
	 * @since 1.0
	 */
	public static int getDefaultWidth() {
		Dimension ss = getScreenSize();
		return Math.min(ss.width - 50, 1000);
	}
	
	/**
	 * Gets the default height of a window, which is guaranteed not to be
	 * taller than the actual screen's height.
	 * 
	 * @return the default window height.
	 * @see #getDefaultWidth()
	 * @since 1.0
	 */
	public static int getDefaultHeight() {
		Dimension ss = getScreenSize();
		return Math.min(ss.height - 50, 600);
	}
}
