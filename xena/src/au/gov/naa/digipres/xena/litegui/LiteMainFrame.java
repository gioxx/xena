/*
 * Created on 1/11/2005 andrek24
 * 
 */
package au.gov.naa.digipres.xena.litegui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.Preferences;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

import au.gov.naa.digipres.xena.core.NormalisedObjectViewFactory;
import au.gov.naa.digipres.xena.core.ReleaseInfo;
import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.IconFactory;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesManager;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesMenuListener;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.util.FileAndDirectorySelectionPanel;
import au.gov.naa.digipres.xena.util.MemoryFileChooser;
import au.gov.naa.digipres.xena.util.TableSorter;
import au.gov.naa.digipres.xena.util.logging.LogFrame;
import au.gov.naa.digipres.xena.util.logging.LogFrameHandler;
import au.gov.naa.digipres.xena.viewer.NormalisedObjectViewFrame;
import au.gov.naa.digipres.xena.viewer.XenaFileFilter;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;

/**
 * Main Frame for Xena Lite application. Usage in a nutshell:
 * The user adds files or directories
 * to the list of items to be normalised, and then clicks the Normalise
 * button to perform normalisation. Results are displayed in a table.
 * 
 * @author justinw5
 * created 6/12/2005
 * xena
 * Short desc of class:
 */
public class LiteMainFrame extends JFrame implements NormalisationStateChangeListener {
	private static final long serialVersionUID = 1L;
	private static final String XENA_LITE_TITLE = "Xena";
	private static final String NAA_TITLE = "National Archives of Australia";

	// Default preferences file
	private static final String DEFAULT_PREFERENCES_FILE = "etc/default-preferences.properties";

	// Preference variable tags
	private static final String homeDirTag = "{homedir}";
	private static final String fileSeperatorTag = "{sep}";

	// Preferences keys
	private static final String XENA_DEST_DIR_KEY = "dir/xenadest";
	private static final String XENA_LOG_FILE_KEY = "dir/xenalog";

	// Logging properties
	private static final String XENA_DEFAULT_LOG_PATTERN = "%t/xena%g.log";
	private static final String ROOT_LOGGING_PACKAGE = "au.gov.naa.digipres.xena";

	private static final String PAUSE_BUTTON_TEXT = "Pause";
	private static final String RESUME_BUTTON_TEXT = "Resume";

	// GUI items
	private FileAndDirectorySelectionPanel itemSelectionPanel;
	private JTable resultsTable;
	private NormalisationResultsTableModel tableModel;
	private TableSorter resultsSorter;
	private JPanel mainNormalisePanel;
	private JPanel mainResultsPanel;
	private JPanel mainPanel;
	private JPanel statusBarPanel;
	private JLabel statusLabel;
	private JLabel currentFileLabel;
	private JRadioButton guessTypeRadio;
	private JRadioButton binaryOnlyRadio;
	private JCheckBox retainDirectoryStructureCheckbox;
	private JCheckBox textNormalisationCheckbox;
	private JProgressBar progressBar;
	private JButton pauseButton;
	private JButton stopButton;
	private JButton cancelButton;
	private JButton doneButton;
	private JButton normErrorsButton;
	private JButton newSessionButton;
	private JMenu pluginPropertiesMenu = new JMenu("Plugin Preferences");

	private NormalisationThread normalisationThread;

	private Preferences prefs;
	private Xena xenaInterface;

	private Logger logger;
	private LogFrame logFrame;
	private FileHandler logFileHandler = null;

	private SplashScreen splashScreen;

	/**
	 * Basic constructor - calls logging and GUI initialisation methods, 
	 * and then makes the main frame visible
	 *
	 */
	public LiteMainFrame() throws UnsupportedLookAndFeelException {
		super(XENA_LITE_TITLE + " " + ReleaseInfo.getVersion() + " - " + NAA_TITLE);

		Plastic3DLookAndFeel plaf = new Plastic3DLookAndFeel();
		UIManager.setLookAndFeel(plaf);

		prefs = Preferences.userNodeForPackage(LiteMainFrame.class);

		// Show splash screen
		splashScreen = new SplashScreen(XENA_LITE_TITLE + " " + ReleaseInfo.getVersion(), getVersionString());
		splashScreen.setVisible(true);

		try {
			initPreferences();
		} catch (IOException e) {
			handleException(e);
		}

		initLogging();
		initNormaliseItemsPanel();
		initResultsPanel();

		try {
			initGUI();
		} catch (Exception e) {
			handleException(e);
		}

		// Hide splash screen
		logger.removeHandler(splashScreen.getLogHandler());
		splashScreen.setVisible(false);
		splashScreen.dispose();
		splashScreen = null;

	}

	private String getVersionString() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

		return "build " + ReleaseInfo.getVersionNum() + "." + ReleaseInfo.getRevisionNum() + "." + ReleaseInfo.getFixNum() + "-"
		       + ReleaseInfo.getBuildNumber() + "/" + formatter.format(ReleaseInfo.getBuildDate());
	}

	/**
	 * Load a set of preferences from a properties file. If any of these preferences
	 * have not previously been set, set the value to that contained in the properties file.
	 * Otherwise, do nothing.
	 * @throws IOException 
	 */
	private void initPreferences() throws IOException {
		Preferences rootPreferences = Preferences.userRoot();

		// Load default preferences properties file
		ClassLoader classLoader = this.getClass().getClassLoader();
		Properties defaultPreferences = new Properties();
		defaultPreferences.load(classLoader.getResourceAsStream(DEFAULT_PREFERENCES_FILE));

		// For each entry in the properties file
		Enumeration<?> propertyNames = defaultPreferences.propertyNames();
		while (propertyNames.hasMoreElements()) {

			// Split the property into the components required to set the preference
			String propertyName = (String) propertyNames.nextElement();
			String propertyValue = defaultPreferences.getProperty(propertyName);
			int separatorIndex = propertyName.indexOf("//");
			if (separatorIndex == -1) {
				// Invalid property, just output message and continue
				System.out.println("Invalid property name in default preferences file: " + propertyName
				                   + ". Property name must contain // to separate path from name.");
				continue;
			}
			String preferencePath = propertyName.substring(0, separatorIndex);
			String preferenceName = propertyName.substring(separatorIndex + 2);

			// Replace {homedir} with the "user.home" java property (/home/user, or C:\documents..). 
			if (propertyValue.contains(homeDirTag)) {
				propertyValue = propertyValue.replace(homeDirTag, System.getProperty("user.home"));
			}
			if (propertyValue.contains(fileSeperatorTag)) {
				propertyValue = propertyValue.replace(fileSeperatorTag, System.getProperty("file.separator"));
			}

			System.out.println("Path=" + preferencePath + ", Name=" + preferenceName + ", value=" + propertyValue);

			// If the preference has not previously been set, set it to the value from the properties file
			Preferences preferenceNode = rootPreferences.node(preferencePath);
			if (preferenceNode.get(preferenceName, "").equals("")) {
				preferenceNode.put(preferenceName, propertyValue);

				// If preferenceName contains XENA_DEST_DIR_KEY then create the location if it doesn't exist.
				if (preferenceName.contains(XENA_DEST_DIR_KEY) || preferenceName.contains(XENA_LOG_FILE_KEY)) {
					File loc = new File(propertyValue);

					if (preferenceName.contains(XENA_LOG_FILE_KEY)) {
						// We only want to create the parent folder if it doesn't exist.
						loc = loc.getParentFile();
					}

					if (!loc.exists()) {
						if (!loc.mkdirs()) {
							System.out.println("Failed to make directory: " + loc.getAbsolutePath());
						}
					}
				}

				System.out.println("Setting preference " + preferencePath + "/" + preferenceName + " to " + propertyValue);
			}

		}
	}

	/**
	 * Initialises logging for the application. Three handlers are 
	 * added - a ConsoleHandler (logs to System.err), a FileHandler
	 * (logs to the file(s) specified in XENA_DEFAULT_LOG_PATTERN)
	 * and a LogFrameHandler, which logs to a frame which can be
	 * viewed from within the application. The logger variable
	 * can then be used to log to all 3 handlers at once.
	 *
	 */
	private void initLogging() {
		// Main logger object
		logger = Logger.getLogger(ROOT_LOGGING_PACKAGE);
		logger.setLevel(Level.ALL);

		// Console handler initialisation
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		logger.addHandler(consoleHandler);

		// Add FileHandler
		initLogFileHandler();

		// LogFrameHandler initialisation
		logFrame = new LogFrame(XENA_LITE_TITLE + " Log");
		LogFrameHandler lfHandler = new LogFrameHandler(logFrame);
		logger.addHandler(lfHandler);
		lfHandler.setLevel(Level.ALL);

		// Splash screen logger
		logger.addHandler(splashScreen.getLogHandler());

		logger.finest("Logging initialised");
	}

	/**
	 * Creates a file handler and adds it to the handlers for
	 * the current logger. If the logFileHandler is not null,
	 * then one has already been added to the logger, and thus
	 * it needs to be removed before adding a new file handler.
	 *
	 */
	private void initLogFileHandler() {
		if (logFileHandler != null) {
			logger.removeHandler(logFileHandler);
			logFileHandler.flush();
			logFileHandler.close();
		}

		try {
			String logFilePattern = prefs.get(XENA_LOG_FILE_KEY, XENA_DEFAULT_LOG_PATTERN);
			logFileHandler = new FileHandler(logFilePattern, 1000000, 2, true);
			logFileHandler.setFormatter(new SimpleFormatter());
			logger.addHandler(logFileHandler);
		} catch (Exception e) {
			logger.log(Level.FINER, "Could not start logging File Handler", e);
		}

	}

	/**
	 * Initialises the "Choose Normalise Items" panel, which is
	 * the screen first presented to the user on application startup.
	 * This method creates: 
	 * <LI> The JList to display the items to be
	 * normalised, and buttons to add and remove files and directories
	 * from this list.
	 * <LI> A panel to display normalisation options (currently "Binary
	 * Normalisation Only" is the sole option).
	 * <LI> A button to do the Normalisation.
	 *
	 */
	private void initNormaliseItemsPanel() {
		// Setup normalise items panel
		itemSelectionPanel = new FileAndDirectorySelectionPanel();
		TitledBorder itemsBorder = new TitledBorder(new EtchedBorder(), "Items to Normalise");
		itemsBorder.setTitleFont(itemsBorder.getTitleFont().deriveFont(13.0f));
		itemSelectionPanel.setBorder(itemsBorder);

		// Setup normalise options panel

		// Guess type radio
		JPanel binaryRadioPanel = new JPanel();
		binaryRadioPanel.setLayout(new BoxLayout(binaryRadioPanel, BoxLayout.Y_AXIS));
		guessTypeRadio = new JRadioButton("Guess type for all files");
		guessTypeRadio.setFont(guessTypeRadio.getFont().deriveFont(12.0f));
		binaryOnlyRadio = new JRadioButton("Binary normalisation only");
		binaryOnlyRadio.setFont(binaryOnlyRadio.getFont().deriveFont(12.0f));
		binaryRadioPanel.add(guessTypeRadio);
		binaryRadioPanel.add(binaryOnlyRadio);
		ButtonGroup binaryRadioGroup = new ButtonGroup();
		binaryRadioGroup.add(guessTypeRadio);
		binaryRadioGroup.add(binaryOnlyRadio);
		guessTypeRadio.setSelected(true);

		// Retain directory structure checkbox
		retainDirectoryStructureCheckbox = new JCheckBox("Retain Directory Structure");
		retainDirectoryStructureCheckbox.setFont(guessTypeRadio.getFont().deriveFont(12.0f));
		retainDirectoryStructureCheckbox.setSelected(true);

		// Perform text normalisation checkbox
		textNormalisationCheckbox = new JCheckBox("Produce Text Version");
		textNormalisationCheckbox.setFont(guessTypeRadio.getFont().deriveFont(12.0f));
		textNormalisationCheckbox.setSelected(false);

		JPanel normaliseOptionsPanel = new JPanel(new BorderLayout());
		normaliseOptionsPanel.add(binaryRadioPanel, BorderLayout.NORTH);
		normaliseOptionsPanel.add(retainDirectoryStructureCheckbox, BorderLayout.CENTER);
		normaliseOptionsPanel.add(textNormalisationCheckbox, BorderLayout.SOUTH);
		TitledBorder optionsBorder = new TitledBorder(new EtchedBorder(), "Normalisation Options");
		optionsBorder.setTitleFont(optionsBorder.getTitleFont().deriveFont(13.0f));
		normaliseOptionsPanel.setBorder(optionsBorder);

		// Setup main button panel
		JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton normaliseButton = new JButton("Normalise");
		normaliseButton.setIcon(IconFactory.getIconByName("images/icons/green_r_arrow.png"));
		normaliseButton.setFont(normaliseButton.getFont().deriveFont(18.0f));
		bottomButtonPanel.add(normaliseButton);

		// Setup main normalise panel
		mainNormalisePanel = new JPanel(new GridBagLayout());

		addToGridBag(mainNormalisePanel, itemSelectionPanel, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0, GridBagConstraints.NORTH,
		             GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0);

		addToGridBag(mainNormalisePanel, normaliseOptionsPanel, 0, 1, GridBagConstraints.REMAINDER, GridBagConstraints.RELATIVE, 1.0, 0.0,
		             GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 10);

		addToGridBag(mainNormalisePanel, bottomButtonPanel, 0, 2, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 1.0, 0.0,
		             GridBagConstraints.SOUTH, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0);

		// Action Listeners
		normaliseButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Check if binary only option has been selected
				int mode = binaryOnlyRadio.isSelected() ? NormalisationThread.BINARY_MODE : NormalisationThread.STANDARD_MODE;
				doNormalisation(mode, retainDirectoryStructureCheckbox.isSelected(), textNormalisationCheckbox.isSelected());
			}

		});

		logger.finest("Normalise Items panel initialised");
	}

	/**
	 * Initialises the "Normalisation Results" panel. A table, with
	 * an associated TableModel and TableSorter, is used to display 
	 * the results. Double-clicking an item in the table will bring
	 * up a window with a more detailed view and more options for
	 * that particular NormalisationResults object. 
	 * Four buttons are also created, a Pause button, a Stop button, 
	 * a Cancel button and a Binary Normalise Errors button.
	 * The Pause and Stop buttons control the Normalisation thread, which can
	 * be paused or stopped completely after the completion of the current item.
	 * When the pause button is clicked, it is renamed to a Resume button.
	 * The Cancel button returns the user to the Normalisation Items screen,
	 * in the same state which the user left it, ie with all items still
	 * listed. If the user then clicked "Normalise" again, this would create
	 * duplicate normalised items in the same output directory, and thus
	 * all normalised objects are deleted before returning to the "Normalise
	 * Items" screen. The user is presented with a confirm dialog before
	 * this is carried out.
	 * The Binary Normalise Errors button performs a binary normalisation
	 * for all items which were not successfully normalised.
	 *
	 */
	private void initResultsPanel() {
		// Initialise display table, with model, sorter and scrollpane
		tableModel = new NormalisationResultsTableModel();
		resultsSorter = new TableSorter(tableModel);
		resultsTable = new JTable(resultsSorter);
		resultsSorter.setTableHeader(resultsTable.getTableHeader());
		JScrollPane resultsTableSP = new JScrollPane(resultsTable);

		Font buttonFont = new JButton().getFont().deriveFont(13.0f);

		// Initialise Pause and Stop buttons
		pauseButton = new JButton(PAUSE_BUTTON_TEXT);
		pauseButton.setEnabled(false);
		pauseButton.setIcon(IconFactory.getIconByName("images/icons/pause.png"));
		pauseButton.setFont(buttonFont);
		stopButton = new JButton("Stop");
		stopButton.setEnabled(false);
		stopButton.setIcon(IconFactory.getIconByName("images/icons/stop.png"));
		stopButton.setFont(buttonFont);
		JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		leftButtonPanel.add(pauseButton);
		leftButtonPanel.add(stopButton);

		// Initialise Cancel, Done and Binary Normalise Errors buttons
		cancelButton = new JButton("Cancel");
		cancelButton.setIcon(IconFactory.getIconByName("images/icons/black_cross.png"));
		cancelButton.setFont(buttonFont);
		doneButton = new JButton("Done");
		doneButton.setIcon(IconFactory.getIconByName("images/icons/done.png"));
		doneButton.setFont(buttonFont);
		normErrorsButton = new JButton("Binary Normalise Failures");
		normErrorsButton.setIcon(IconFactory.getIconByName("images/icons/binary.png"));
		normErrorsButton.setFont(buttonFont);
		JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		rightButtonPanel.add(normErrorsButton);
		rightButtonPanel.add(doneButton);
		rightButtonPanel.add(cancelButton);

		// Layout buttons
		JPanel resultsButtonPanel = new JPanel(new BorderLayout());
		resultsButtonPanel.add(leftButtonPanel, BorderLayout.WEST);
		resultsButtonPanel.add(rightButtonPanel, BorderLayout.EAST);

		// Layout
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBorder(new EmptyBorder(3, 3, 10, 3));
		tablePanel.add(resultsTableSP, BorderLayout.CENTER);

		mainResultsPanel = new JPanel(new BorderLayout());
		TitledBorder titledBorder = new TitledBorder(new EmptyBorder(0, 3, 3, 3), "Normalisation Results");
		titledBorder.setTitleFont(titledBorder.getTitleFont().deriveFont(13.0f));
		mainResultsPanel.setBorder(titledBorder);
		mainResultsPanel.add(tablePanel, BorderLayout.CENTER);
		mainResultsPanel.add(resultsButtonPanel, BorderLayout.SOUTH);

		// Action Listeners
		resultsTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getModifiers() == InputEvent.BUTTON1_MASK && e.getClickCount() == 2) {
					try {
						int modelIndex = resultsSorter.modelIndex(resultsTable.getSelectedRow());
						displayResults(modelIndex);
					} catch (Exception ex) {
						handleException(ex);
					}
				}
			}

		});
		resultsTable.addKeyListener(new KeyAdapter() {

			/* (non-Javadoc)
			 * @see java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == ' ') {
					try {
						int modelIndex = resultsSorter.modelIndex(resultsTable.getSelectedRow());
						displayResults(modelIndex);
					} catch (Exception ex) {
						handleException(ex);
					}
				}
			}
		});

		pauseButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Check if we are pausing or resuming
				int newState = PAUSE_BUTTON_TEXT.equals(pauseButton.getText()) ? NormalisationThread.PAUSED : NormalisationThread.RUNNING;
				changeNormalisationState(newState);
			}

		});

		stopButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				changeNormalisationState(NormalisationThread.STOPPED);
			}

		});

		doneButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				startNewSession();
			}

		});

		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				doCancel();
			}

		});

		normErrorsButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				doNormalisation(NormalisationThread.BINARY_ERRORS_MODE, retainDirectoryStructureCheckbox.isSelected(), textNormalisationCheckbox
				        .isSelected());
			}

		});

		logger.finest("Results panel initialised");
	}

	/**
	 * Initialises the overall GUI for the application. The Menu,
	 * Toolbar and Status Bar are set up in this method.
	 * @throws IOException 
	 * @throws XenaException 
	 *
	 */
	private void initGUI() throws XenaException, IOException {
		this.setSize(800, 600);
		this.setLocation(120, 120);

		ImageIcon xenaImageIcon = IconFactory.getIconByName("images/xena-icon.png");
		setIconImage(xenaImageIcon.getImage());

		initMenu();

		// Setup toolbar
		JToolBar toolbar = new JToolBar();
		newSessionButton = new JButton("New Session");
		newSessionButton.setIcon(IconFactory.getIconByName("images/icons/window_new.png"));
		newSessionButton.setFont(newSessionButton.getFont().deriveFont(12.0f));
		JButton viewLogButton = new JButton("View Log");
		viewLogButton.setIcon(IconFactory.getIconByName("images/icons/open_book.png"));
		viewLogButton.setFont(viewLogButton.getFont().deriveFont(12.0f));
		toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
		toolbar.add(newSessionButton);
		toolbar.add(viewLogButton);

		logger.finest("Toolbar initialised");

		// Setup status bar
		statusBarPanel = new JPanel(new BorderLayout());
		statusBarPanel.add(new JLabel(" "), BorderLayout.CENTER);
		statusBarPanel.setBorder(new SoftBevelBorder(BevelBorder.RAISED));

		// Add to content pane
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new EtchedBorder());
		mainPanel.add(mainNormalisePanel, BorderLayout.CENTER);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		getContentPane().add(statusBarPanel, BorderLayout.SOUTH);

		// Action listeners

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				doShutdown();
			}
		});

		// Ensure window is not resized below 400x430
		addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent event) {
				LiteMainFrame.this.setSize(LiteMainFrame.this.getWidth() < 400 ? 400 : LiteMainFrame.this.getWidth(),
				                           LiteMainFrame.this.getHeight() < 430 ? 430 : LiteMainFrame.this.getHeight());
			}
		});

		newSessionButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				startNewSession();
			}

		});

		viewLogButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				logFrame.setLocationRelativeTo(LiteMainFrame.this);
				logFrame.setVisible(true);
			}

		});

		logger.finest("Main GUI initialised");
	}

	/**
	 * Initialise the menu bar and menu items.
	 */
	private void initMenu() {
		// Setup menu bar
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic('T');
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		menuBar.add(fileMenu);
		menuBar.add(toolsMenu);
		menuBar.add(helpMenu);

		// Menu items		
		JMenuItem openItem = new JMenuItem("Open", 'O');
		openItem.setIcon(IconFactory.getIconByName("images/icons/fileopen_16.png"));
		fileMenu.add(openItem);

		JMenuItem exitItem = new JMenuItem("Exit", 'E');
		exitItem.setIcon(IconFactory.getIconByName("images/icons/exit.png"));
		fileMenu.add(exitItem);

		JMenuItem prefsItem = new JMenuItem(XENA_LITE_TITLE + " Preferences", 'X');
		prefsItem.setIcon(IconFactory.getIconByName("images/icons/spanner.png"));
		toolsMenu.add(prefsItem);

		JMenuItem helpItem = new JMenuItem("Help", 'H');
		helpItem.setIcon(IconFactory.getIconByName("images/icons/help.png"));
		try {
			helpItem.addActionListener(new CSH.DisplayHelpFromSource(getHelpBroker()));
			helpMenu.add(helpItem);
		} catch (XenaException xe) {
			xe.printStackTrace();
		}

		JMenuItem aboutItem = new JMenuItem("About", 'A');
		aboutItem.setIcon(IconFactory.getIconByName("images/icons/info.png"));
		helpMenu.add(aboutItem);

		JMenuItem aboutPluginsItem = new JMenuItem("About Plugins", 'P');
		aboutPluginsItem.setIcon(IconFactory.getIconByName("images/icons/plug.png"));
		helpMenu.add(aboutPluginsItem);

		// Initialise properties menu
		initPluginPropertiesMenu();
		pluginPropertiesMenu.setIcon(IconFactory.getIconByName("images/icons/plug_lightning.png"));
		toolsMenu.add(pluginPropertiesMenu);

		setJMenuBar(menuBar);

		// Menu actions
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					openSelectedXenaFile();
				} catch (IOException ioex) {
					handleException(ioex);
				}
			}
		});

		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doShutdown();
			}
		});

		prefsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showPreferencesDialog();
			}
		});

		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LiteAboutDialog.showAboutDialog(LiteMainFrame.this, XENA_LITE_TITLE, getVersionString());
			}
		});

		aboutPluginsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					AboutPluginsDialog.showPluginsDialog(LiteMainFrame.this, getXenaInterface(), XENA_LITE_TITLE + " Plugins");
				} catch (Exception ex) {
					handleException(ex);
				}
			}
		});

		logger.finest("Menu initialised");
	}

	/**
	 * Create Plugin Properties menu, and populate with a menu item for each
	 * plugin with properties to set
	 * 
	 * @return
	 * @throws IOException 
	 * @throws XenaException 
	 */
	private void initPluginPropertiesMenu() {
		if (pluginPropertiesMenu == null) {
			throw new IllegalStateException("Developer error - method should not be " + "called until properties menu object has "
			                                + "been initialised");
		}

		pluginPropertiesMenu.removeAll();
		pluginPropertiesMenu.setMnemonic('P');

		// Add plugin properties
		try {
			PropertiesManager manager = getXenaInterface().getPluginManager().getPropertiesManager();
			List<PluginProperties> pluginProperties = manager.getPluginProperties();

			for (PluginProperties pluginProp : pluginProperties) {
				JMenuItem propItem = new JMenuItem(pluginProp.getName() + "...");
				propItem.addActionListener(new PropertiesMenuListener(this, pluginProp));
				pluginPropertiesMenu.add(propItem);
			}

			logger.finest("Plugin Properties menu initialised");
		} catch (Exception ex) {
			// Not sure if we want to display this error or not... will for the moment
			handleException(ex);
		}

	}

	private HelpBroker getHelpBroker() throws XenaException {
		HelpBroker broker;
		String helpsetName = "xena-help";

		ClassLoader loader = getClass().getClassLoader();
		URL url = HelpSet.findHelpSet(loader, "doc/" + helpsetName);
		if (url != null) {
			HelpSet mainHS;
			try {
				mainHS = new HelpSet(loader, url);
				broker = mainHS.createHelpBroker();
				logger.finest("Help system initialised");
			} catch (HelpSetException e) {
				throw new XenaException("Could not create help set " + helpsetName);
			}

		} else {
			logger.log(Level.FINER, "Help Set " + helpsetName + " not found");
			throw new XenaException("Could not find help set " + helpsetName);
		}

		return broker;
	}

	/**
	 * Convenience method for adding a component to a container
	 * which is using a GridBagLayout
	 * 
	 * @param container
	 * @param component
	 * @param gridx
	 * @param gridy
	 * @param gridwidth
	 * @param gridheight
	 * @param weightx
	 * @param weighty
	 * @param anchor
	 * @param fill
	 * @param insets
	 * @param ipadx
	 * @param ipady
	 */
	private void addToGridBag(Container container, Component component, int gridx, int gridy, int gridwidth, int gridheight, double weightx,
	                          double weighty, int anchor, int fill, Insets insets, int ipadx, int ipady) {
		GridBagConstraints gbc = new GridBagConstraints(gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady);
		container.add(component, gbc);
	}

	/**
	 * Show "Edit Preferences" dialog. Existing preferences will be
	 * loaded using Java preferences, and saved after the dialog has
	 * been (successfully) closed.
	 *
	 */
	private void showPreferencesDialog() {
		LitePreferencesDialog prefsDialog = new LitePreferencesDialog(this, XENA_LITE_TITLE + " Preferences");
		prefsDialog.setXenaDestDir(prefs.get(XENA_DEST_DIR_KEY, ""));
		prefsDialog.setXenaLogFile(prefs.get(XENA_LOG_FILE_KEY, ""));
		prefsDialog.setLocationRelativeTo(this);
		prefsDialog.setVisible(true);

		// We have returned from the dialog
		if (prefsDialog.isApproved()) {
			if (!prefs.get(XENA_LOG_FILE_KEY, "").equals(prefsDialog.getXenaLogFile().trim())) {
				prefs.put(XENA_LOG_FILE_KEY, prefsDialog.getXenaLogFile());
				initLogFileHandler();
			}
			prefs.put(XENA_DEST_DIR_KEY, prefsDialog.getXenaDestDir());
		}
		logger.finest(XENA_LITE_TITLE + " preferences saved");
	}

	/**
	 * Starts the NormalisationThread to carry out normalisation
	 * of the selected objects, initialises the progress bar
	 * and status label, and switches to the Results panel.
	 * 
	 * The method can be called in one of three modes:
	 * <LI><B>STANDARD_MODE</B> will use a guesser to guess the
	 * correct type of each file;
	 * <LI><B>BINARY_MODE</B> will use the binary normaliser 
	 * for each file;
	 * <LI><B>BINARY_ERRORS_MODE</B> will use the binary normaliser
	 * to normalise any files that were not normalised in a previous
	 * attempt.
	 * @param retainDirectoryStructure 
	 *
	 */
	private void doNormalisation(int mode, boolean retainDirectoryStructure, boolean performTextNormalisation) {
		List<File> itemList = itemSelectionPanel.getAllItems();
		if (mode != NormalisationThread.BINARY_ERRORS_MODE) {
			logger.finest("Beginning normalisation process for " + itemList.size() + " items");

			// Ensure that at least one file or directory has been selected
			if (itemList.size() == 0) {
				JOptionPane
				        .showMessageDialog(this, "Please add files and/or directories.", "No Normalisation Items", JOptionPane.INFORMATION_MESSAGE);
				logger.finest("Attempted to normalise with no items");
				return;
			}
		} else {
			logger.finest("Beginning error normalisation process");
		}

		// Ensure destination directory has been set
		String destDir = prefs.get(XENA_DEST_DIR_KEY, "");
		if ("".equals(destDir.trim())) {
			JOptionPane.showMessageDialog(this, "Please set the destination directory" + " in Tools->" + XENA_LITE_TITLE + " Preferences.",
			                              "Destination Directory Not Set", JOptionPane.INFORMATION_MESSAGE);
			logger.finest("Attempted to normalise with no destination directory");
			return;
		}

		try {
			// Initialise status bar
			progressBar = new JProgressBar();
			progressBar.setForeground(Color.GREEN);
			progressBar.setMinimum(0);
			statusLabel = new JLabel();
			currentFileLabel = new JLabel();
			currentFileLabel.setHorizontalAlignment(SwingConstants.CENTER);

			// Refresh status bar
			statusBarPanel.removeAll();
			statusBarPanel.add(statusLabel, BorderLayout.WEST);
			statusBarPanel.add(currentFileLabel, BorderLayout.CENTER);
			statusBarPanel.add(progressBar, BorderLayout.EAST);

			if (mode != NormalisationThread.BINARY_ERRORS_MODE) {

				// Create the normalisation thread
				normalisationThread =
				    new NormalisationThread(mode, retainDirectoryStructure, performTextNormalisation, getXenaInterface(), tableModel, itemList,
				                            new File(destDir), this);

				// Display the results panel
				mainPanel.removeAll();
				mainPanel.add(mainResultsPanel, BorderLayout.CENTER);
			} else {
				// Create the normalisation thread
				normalisationThread =
				    new NormalisationThread(mode, retainDirectoryStructure, performTextNormalisation, getXenaInterface(), tableModel, itemList,
				                            new File(destDir), this);
			}

			// Add this object as a listener of the NormalisationThread,
			// so that the buttons on the Results panel can be enabled
			// and disabled appropriately
			normalisationThread.add(this);

			// Start the normalisation process
			normalisationThread.start();

			validate();
			this.repaint();
		} catch (Exception e) {
			handleException(e);
		}

	}

	/**
	 * Implementation of a NormalisationStateChangeListener,
	 * which is called whenever the NormalisationThread indicates
	 * that it has changed its running state. There are three states -
	 * RUNNING, PAUSED and STOPPED, and changing to any of these
	 * states causes buttons on the results panel to be enabled or
	 * disabled appropriately.
	 * The status bar components are also updated based on the
	 * values of the total items, error count, current file etc.
	 */
	public void normalisationStateChanged(int newState, int totalItems, int normalisedItems, int errorItems, String currentFile) {
		String statusText = normalisedItems + errorItems + " of " + totalItems + " completed (" + errorItems + " error(s))";
		switch (newState) {
		case NormalisationThread.RUNNING:
			// Update buttons
			pauseButton.setText(PAUSE_BUTTON_TEXT);
			pauseButton.setEnabled(true);
			pauseButton.setIcon(IconFactory.getIconByName("images/icons/pause.png"));
			stopButton.setEnabled(true);
			normErrorsButton.setEnabled(false);
			cancelButton.setEnabled(false);
			doneButton.setEnabled(false);
			newSessionButton.setEnabled(false);

			// Update progress bar
			progressBar.setMaximum(totalItems);
			progressBar.setValue(normalisedItems + errorItems);

			// Update status label
			statusLabel.setText(statusText);
			if (errorItems > 0) {
				statusLabel.setForeground(Color.RED);
			}

			// Update current file label
			currentFileLabel.setText("Normalising " + currentFile);

			break;
		case NormalisationThread.PAUSED:
			// Update buttons
			pauseButton.setText(RESUME_BUTTON_TEXT);
			pauseButton.setEnabled(true);
			pauseButton.setIcon(IconFactory.getIconByName("images/icons/green_r_arrow.png"));
			stopButton.setEnabled(true);
			normErrorsButton.setEnabled(false);
			cancelButton.setEnabled(true);
			doneButton.setEnabled(false);
			newSessionButton.setEnabled(true);

			currentFileLabel.setText("Paused");
			break;
		case NormalisationThread.STOPPED:
			// Update buttons
			pauseButton.setEnabled(false);
			stopButton.setEnabled(false);
			cancelButton.setEnabled(true);
			doneButton.setEnabled(true);
			newSessionButton.setEnabled(true);
			if (errorItems > 0) {
				normErrorsButton.setEnabled(true);
			} else {
				normErrorsButton.setEnabled(false);
			}

			// Update status label
			statusLabel.setText(statusText);
			if (errorItems > 0) {
				statusLabel.setForeground(Color.RED);
			}
			currentFileLabel.setText("");

			statusBarPanel.remove(progressBar);

			validate();
			this.repaint();

			displayConfirmationMessage("Normalisation Complete", totalItems, normalisedItems, errorItems);
			break;
		}
	}

	public void normalisationError(String message, Exception e) {
		handleException(e);
	}

	/**
	 * Displays a confirmation message with details of the number
	 * of normalised items and errors
	 * 
	 * @param title
	 * @param totalItems
	 * @param normalisedItems
	 * @param errorItems
	 */
	private void displayConfirmationMessage(String title, int totalItems, int normalisedItems, int errorItems) {
		new NormalisationCompleteDialog(this, totalItems, normalisedItems, errorItems).setVisible(true);
	}

	/**
	 * Clicking a button on the results panel will call this
	 * method, which will indicate to the NormalisationThread
	 * that it needs to take a certain action when next appropriate.
	 * @param newState
	 */
	private void changeNormalisationState(int newState) {
		normalisationThread.setThreadState(newState);
	}

	/**
	 * Displays the selected results row in a NormaliserResultsFrame.
	 * The results object is retrieved from the table model using a 
	 * special column index RESULTS_OBJECT_INDEX. This is because the
	 * NormalisationResultsTableModel can not be accessed directly as
	 * the request must go through the TableSorter so that the correct
	 * row is still selected when the table has been sorted. 
	 * 
	 * @param selectedRow
	 * @throws XenaException
	 * @throws IOException
	 */
	private void displayResults(int selectedRow) throws XenaException, IOException {
		NormaliserResults results = tableModel.getNormaliserResults(selectedRow);
		if (results.isNormalised()) {
			viewNormalisedFile(results);
		} else {
			// An error has occurred, so display the error in full
			ExceptionDialog.showExceptionDialog(this, results.getErrorDetails(), "Normalisation Error", "An error occurred during normalisation.");
		}
	}

	/**
	 * Allow the user to select a file, and call showXenaFile to display it.
	 * @throws IOException 
	 */
	private void openSelectedXenaFile() throws IOException {
		MemoryFileChooser fileChooser = new MemoryFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		// Create a filter that will show only .xena files
		fileChooser.setFileFilter(new XenaFileFilter());

		int retVal = fileChooser.showOpenDialog(this, this.getClass(), "OpenXenaFile");

		// Have returned from dialog
		if (retVal == JFileChooser.APPROVE_OPTION) {
			showXenaFile(fileChooser.getSelectedFile());
		}
	}

	/**
	 * Display the Xena output file in a NormalisedObjectViewFrame.
	 * @throws IOException 
	 *
	 */
	private void viewNormalisedFile(NormaliserResults results) throws IOException {
		File xenaFile = new File(results.getDestinationDirString() + File.separator + results.getOutputFileName());
		showXenaFile(xenaFile);
	}

	/**
	 * Load the given Xena file into a XenaView, and display it in a frame
	 * @param xenaFile
	 * @throws IOException
	 */
	private void showXenaFile(File xenaFile) throws IOException {
		try {
			NormalisedObjectViewFactory novFactory = new NormalisedObjectViewFactory(getXenaInterface());

			XenaView xenaView = novFactory.getView(xenaFile);
			Xena xena = getXenaInterface();

			// Show Export button on Xena View frames (and child frames)
			xena.getPluginManager().getViewManager().setShowExportButton(true);

			NormalisedObjectViewFrame viewFrame = new NormalisedObjectViewFrame(xenaView, xena, xenaFile);

			// Display frame
			viewFrame.setLocation(getX() + 50, getY() + 50);
			viewFrame.setVisible(true);
		} catch (XenaException e) {
			handleException(e);
		}
	}

	/**
	 * Clears the Normalisation Items List and normalisation
	 * results table, resets the normalisation options, and 
	 * displays the Normalisation Items screen. 
	 *
	 */
	private void startNewSession() {
		// Check that the user really wants to restart
		String[] msgArr = {"Are you sure you want to start a new session?", "This will not delete any output from this session."};

		int retVal = JOptionPane.showConfirmDialog(this, msgArr, "Confirm New Session", JOptionPane.OK_CANCEL_OPTION);
		if (retVal == JOptionPane.OK_OPTION) {
			// Reset item list, normalisation options and status bar
			itemSelectionPanel.clear();
			guessTypeRadio.setSelected(true);
			statusBarPanel.removeAll();
			statusBarPanel.add(new JLabel(" "), BorderLayout.CENTER);

			// Clear normalisation results table
			tableModel.clear();
			tableModel.fireTableDataChanged();

			// Display normalisation items screen
			mainPanel.removeAll();
			mainPanel.add(mainNormalisePanel, BorderLayout.CENTER);
			validate();
			this.repaint();

			logger.finest("Started new normalisation session");
		}
	}

	/**
	 * The Cancel action deletes all the normalised objects currently
	 * listed in the results table, and returns to the normalise
	 * items screen. The user is first asked to confirm this action.
	 *
	 */
	private void doCancel() {
		// Confirm file deletion
		String[] msgArr =
		    {"Using the Cancel button will cause the current set " + "of normalised output files to be deleted.", "Are you sure you want to do this?"};
		int retVal = JOptionPane.showConfirmDialog(this, msgArr, "Confirm File Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (retVal == JOptionPane.YES_OPTION) {
			// File deletion has been confirmed

			List<NormaliserResults> resultsList = tableModel.getAllNormaliserResults();

			// For all results objects displayed in the table
			for (NormaliserResults results : resultsList) {
				// Delete output file
				String destDir = results.getDestinationDirString();
				String destFile = results.getOutputFileName();
				if (destDir != null && !"".equals(destDir.trim()) && destFile != null && !"".equals(destFile.trim())) {
					File file = new File(destDir + File.separator + destFile);
					file.delete();

					logger.finest("Deleted file " + file);
				}
			}

			// Clear results table
			tableModel.clear();
			tableModel.fireTableDataChanged();

			// Reset status bar
			statusBarPanel.removeAll();
			statusBarPanel.add(new JLabel(" "), BorderLayout.CENTER);

			// Display normalisation items screen
			mainPanel.removeAll();
			mainPanel.add(mainNormalisePanel, BorderLayout.CENTER);
			validate();
			this.repaint();

			logger.finest("Cancel action completed");
		}
	}

	/**
	 * Shut down application
	 *
	 */
	private void doShutdown() {
		logger.finest("Shutting down " + XENA_LITE_TITLE);
		System.exit(0);
	}

	/**
	 * Initialises the Xena interface (currently loads plugins) if required
	 * @return
	 * @throws XenaException
	 * @throws IOException
	 */
	private Xena getXenaInterface() throws XenaException, IOException {
		if (xenaInterface == null) {
			xenaInterface = new Xena();
			xenaInterface.loadPlugins(getPluginsDirectory());
			logger.finest("Successfully loaded Xena Framework interface");
		}

		return xenaInterface;
	}

	/**
	 * Returns the xena lite plugins directory. This is set as being a directory named "plugins"
	 * which is a subdirectory of the directory containing the xena.jar file.
	 * First we assume that we are running xena lite from the directory containing the xena.jar file.
	 * If the plugins directory cannot be found, then the base directory could be different to the
	 * xena.jar directory. So first we get the URL of the litegui package. This URL
	 * will consist of the file system path to the jar file plus a path to the package directory. The
	 * directory containing the jar file can thus be extracted.
	 * @return
	 * @throws XenaException
	 */
	private File getPluginsDirectory() throws XenaException {
		File pluginsDir = new File("plugins");
		if (!pluginsDir.exists() || !pluginsDir.isDirectory()) {
			boolean pluginsDirFound = false;
			String resourcePath = this.getClass().getResource("/" + this.getClass().getPackage().getName().replace(".", "/")).getPath();

			String fileIdStr = "file:";

			if (resourcePath.indexOf(fileIdStr) >= 0 && resourcePath.lastIndexOf("!") >= 0) {
				String jarPath = resourcePath.substring(resourcePath.indexOf(fileIdStr) + fileIdStr.length(), resourcePath.lastIndexOf("!"));
				if (jarPath.lastIndexOf("/") >= 0) {
					pluginsDir = new File(jarPath.substring(0, jarPath.lastIndexOf("/") + 1) + "plugins");
					if (pluginsDir.exists() && pluginsDir.isDirectory()) {
						pluginsDirFound = true;

					}
				}
			}
			if (!pluginsDirFound) {
				throw new XenaException("Cannot find default plugins directory. " + "Try running Xena Lite from the same directory as xena.jar.");
			}
		}

		File[] pluginFiles = pluginsDir.listFiles();
		boolean foundPlugin = false;
		for (File pluginFile : pluginFiles) {
			if (pluginFile.getName().endsWith(".jar")) {
				foundPlugin = true;
				break;
			}
		}
		if (!foundPlugin) {
			JOptionPane.showMessageDialog(this, "No plugins found in plugin directory " + pluginsDir.getAbsolutePath(), "No Plugins Found",
			                              JOptionPane.WARNING_MESSAGE);
			logger.finer("No plugins found, proceding without plugins");
		}
		return pluginsDir;
	}

	/**
	 * Displays a message dialog containing the given exception
	 * @param ex
	 */
	private void handleException(Exception ex) {
		logger.log(Level.FINER, ex.toString(), ex);
		JOptionPane.showMessageDialog(this, ex.getMessage(), XENA_LITE_TITLE, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Main method for legacy use - should now use XenaMain to start the Xena GUI
	 * @throws UnsupportedLookAndFeelException 
	 */
	public static void main(String[] args) throws UnsupportedLookAndFeelException {
		LiteMainFrame liteMainFrame = new LiteMainFrame();
		liteMainFrame.setVisible(true);
	}
}
