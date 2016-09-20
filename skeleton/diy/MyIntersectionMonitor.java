package traffic.diy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;

import traffic.core.Intersection;
import traffic.core.Phase;
import traffic.core.TrafficStream;
import traffic.demo.DemoClydeCreyke;
import traffic.misc.TranscriptPane;
import traffic.phaseplan.PhasePlan;
import traffic.signal.SignalFace;
import traffic.util.State;

public class MyIntersectionMonitor extends JFrame implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The application's menu bar. We need to know its name in case we decide to
	 * add new menus etc later.
	 */
	protected JMenuBar mb = new JMenuBar();
	/**
	 * The File menu. Need to know its name in case new menu items need to be
	 * added later.
	 */
	protected JMenu fm = new JMenu("File");

	/**
	 * This menu item kills off the application.
	 */
	protected JMenuItem fmq = new JMenuItem("Quit");

	/**
	 * This menu item opens a file containing an intersection description.
	 */
	protected JMenuItem fmo = new JMenuItem("Open...");

	/**
	 * This is the main application area. Need to know its name so we can create
	 * components (text areas, buttons etc) and add them in as needed.
	 */
	protected JPanel mainPanel = new JPanel();

	/**
	 * A place to log the activity displayed in the monitor.
	 */
	protected JTextArea transcript;

	protected TranscriptPane tp;

	private static Intersection myIntersection;

	private HashMap<TrafficStream, ArrayList<JPanel>> streamStateChips = new HashMap<TrafficStream, ArrayList<JPanel>>();

	private HashMap<Phase, JPanel> phasePanels = new HashMap<Phase, JPanel>();
	private Phase thePhase = null;

	private Color bluish = Color.blue.brighter().brighter().brighter().brighter();
	private Color labelBackground = Color.orange;
	private Color phasePanelBackground = Color.pink;
	private Color streamBackground = Color.magenta;
	private Color inactivePhaseBackground = Color.GRAY;
	private Color activePhaseBackground = Color.CYAN;

	/**
	 * For menu items etc that you should complete yourself.
	 */
	private Color diyColour = Color.PINK;

	/**
	 * The standard <CODE>main</CODE> method creates the frame and contents. A
	 * title for the main window may be supplied as a command line argument.
	 */
	public static void main(String Args[]) {
		MyIntersectionMonitor me = new MyIntersectionMonitor(Args.length > 0 ? Args[0] : "Untitled");
		me.setSize(500, 950);
		me.pack();
		me.setVisible(true);
	}

	/**
	 * Constructor for class. Assembles the basic GUI elements.
	 * 
	 * @param title
	 *            Title for the application window
	 */
	public MyIntersectionMonitor(String title) {
		super(title);
		this.setJMenuBar(mb);

		// How to open an intersection file
		fmo.setBackground(diyColour);
		fmo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				openIntersection();
			}
		});

		// How to save a serialised intersection
		JMenuItem fms = new JMenuItem("Save intersection...");
		fms.setBackground(diyColour);
		fms.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveSerialisedIntersection();

			}
		});

		// How to load a serialised intersection
		JMenuItem fml = new JMenuItem("Load saved intersection...");
		fml.setBackground(diyColour);
		fml.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				loadSerialisedIntersection();
			}
		});

		JMenuItem tms = new JMenuItem("Save Transcript...");
		// How to save transcript
		tms.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				tp.saveToFile();
			}
		});

		JMenuItem tmc = new JMenuItem("Clear Transcript...");
		// How to clear transcript
		tmc.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				tp.clear();
			}
		});

		// How to quit...
		fmq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
				System.exit(0);
			}
		});

		// When the window manager tries to get rid of us...
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				dispose();
				System.exit(0);
			}
		});

		JMenuItem fmd = new JMenuItem("Demo...");

		// How to run a demo
		fmd.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				myIntersection = DemoClydeCreyke.pretimedMultiPhase();

				if (myIntersection == null) {
					// not good :-(
					JOptionPane.showMessageDialog(null, "This intersection isn't valid and can't be displayed",
							"Malformed Intersection", JOptionPane.ERROR_MESSAGE);
					return;
				}
				mainPanel.add(intersectionGUI(myIntersection));
				getContentPane().validate();
			}
		});

		JMenuItem fmMine = new JMenuItem("My Demo...");
		fmMine.setBackground(diyColour);
		// How to run the demo I wrote myself
		fmMine.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// for example...
				myIntersection = ModelIntersection.preTimedIntersection();
				if (myIntersection == null) {
					// not good :-(
					JOptionPane.showMessageDialog(null, "My demo failed --- keep trying!", "Null Intersection",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				mainPanel.add(intersectionGUI(myIntersection));
				getContentPane().validate();
			}
		});

		mb.add(fm);

		fm.add(fmo);
		fm.add(fml);
		fm.add(fms);
		fm.addSeparator();
		fm.add(fmd);
		fm.addSeparator();

		fm.add(fmMine);

		fm.add(fmq);

		JMenu im = new JMenu("Intersection");

		JMenuItem iStart = new JMenuItem("Start");
		im.add(iStart);
		iStart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (myIntersection != null) {
					start();
				}
			}
		});

		JMenuItem iStop = new JMenuItem("Stop");
		im.add(iStop);

		mb.add(im);

		iStop.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (myIntersection != null) {
					tp.log("Stopping");
					myIntersection.finish();
				}
			}
		});

		JMenu tm = new JMenu("Transcript");
		tm.add(tms);
		tm.add(tmc);

		mb.add(tm);

		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		add(new JScrollPane(mainPanel));

		tp = new TranscriptPane(5);
		add(tp);

	}

	/**
	 * Add a visual representation of an intersection. Basic intersection
	 * details appear at the top. Next come the phases -- one row each --
	 * followed by the signal faces. ToolTips are used to reduce the amount of
	 * text displayed.
	 * 
	 * @param i
	 *            The intersection to display.
	 * @return
	 */
	private JPanel intersectionGUI(Intersection i) {
		JPanel thisIntersection = new JPanel();
		thisIntersection.setLayout(new BoxLayout(thisIntersection, BoxLayout.Y_AXIS));

		JLabel lName = new JLabel(i.getName(), JLabel.CENTER);
		JLabel lDesc = new JLabel(i.getDescription(), JLabel.CENTER);

		JPanel labels = new JPanel();
		// 2 rows, 1 column
		labels.setLayout(new GridLayout(2, 1));
		labels.add(lName);
		labels.add(lDesc);
		labels.setBackground(labelBackground);
		thisIntersection.add(labels);

		// Now assemble the SignalFaces
		List<SignalFace> faces = i.getSignalFaces();
		JPanel physicalSignalFaces = new JPanel();

		for (SignalFace sf : faces) {
			JPanel thisFacePanel = new JPanel();
			thisFacePanel.setLayout(new BoxLayout(thisFacePanel, BoxLayout.Y_AXIS));

			String at = sf.location().label();
			String facing = sf.orientation().label();

			JLabel atLabel = new JLabel(at, JLabel.CENTER);
			JLabel facingLabel = new JLabel(facing, JLabel.CENTER);

			JPanel faceLabels = new JPanel();
			faceLabels.setLayout(new BoxLayout(faceLabels, BoxLayout.Y_AXIS));
			faceLabels.setToolTipText(sf.location() + " facing " + sf.orientation());
			faceLabels.add(atLabel);
			faceLabels.add(facingLabel);

			thisFacePanel.add(faceLabels);
			thisFacePanel.add(sf.getGUI()); // the 3 lights

			thisFacePanel.setBorder(BorderFactory.createEtchedBorder());

			physicalSignalFaces.add(thisFacePanel);
		}
		thisIntersection.add(physicalSignalFaces);

		JPanel phasesPanel = new JPanel();

		// Each phase gets same vertical space. (1 column)
		// One row per phase
		phasesPanel.setLayout(new BoxLayout(phasesPanel, BoxLayout.Y_AXIS));
		PhasePlan plan = i.getPlan();

		// Collect the GUI representation for each phase in plan
		for (Phase currPhase : plan.phases()) {
			JPanel currPhasePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			currPhasePanel.setBackground(phasePanelBackground);
			currPhasePanel.setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.RAISED));
			// We'll need to keep up to date as phases change
			currPhase.addObserver(this);
			phasePanels.put(currPhase, currPhasePanel);

			// Name and description in a text area
			JTextArea tpp = new JTextArea(4, 9);
			tpp.setLineWrap(true);
			tpp.setWrapStyleWord(true);
			tpp.append(currPhase.getName() + " \n");
			tpp.append(currPhase.getDescription());
			tpp.setEditable(false);
			currPhasePanel.add(tpp);

			JPanel currPhaseStreams = new JPanel();
			// For each phase, collect the traffic stream info...
			Collection<TrafficStream> streams = currPhase.getTrafficStreams();

			for (TrafficStream ts : streams) {
				ts.addObserver(this);
				JPanel streamPanel = new JPanel(new BorderLayout());
				String si = ts.toString();
				String sii = si.substring(si.indexOf(" ") + 1, si.length() - 1);

				JLabel streamInfo = new JLabel(ts.getname(), JLabel.CENTER);
				streamInfo.setToolTipText(sii);

				streamPanel.add(streamInfo, BorderLayout.NORTH);
				streamPanel.setBackground(streamBackground);
				streamPanel.setBorder(BorderFactory.createSoftBevelBorder(SoftBevelBorder.RAISED));
				JPanel streamPanelChip = new JPanel();
				streamPanelChip.setPreferredSize(new Dimension(25, 25));
				streamPanelChip.setBackground(colourForState(currPhase.streamStates().get(ts)));

				addChip(ts, streamPanelChip);

				streamPanel.add(streamPanelChip);
				currPhaseStreams.add(streamPanel);
			}
			currPhasePanel.add(currPhaseStreams);
			phasesPanel.add(currPhasePanel);
		}

		phasesPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

		thisIntersection.add(phasesPanel);
		return thisIntersection;
	}

	/**
	 * Open menu item selected so go ahead and load the corresponding file.
	 * Doesn't start its cycle.
	 */
	private void openIntersection() {
		File f;
		String cwd = System.getProperty("user.dir");

		JFileChooser jfc = new JFileChooser(cwd);
		int userChoice = jfc.showOpenDialog(this);
		switch (userChoice) {
		case JFileChooser.APPROVE_OPTION:
			f = jfc.getSelectedFile();
			if (f.exists() && f.isFile() && f.canRead()) {
				// so far, so good
				try {
					BufferedReader br = new BufferedReader(new FileReader(f.getPath()));
					// OK, file is opened so read & process content
					MyIntersectionLoader il = new MyIntersectionLoader(br);
					myIntersection = il.buildIntersection();
					if (myIntersection == null) {
						// not good :-(
						JOptionPane.showMessageDialog(null, "This intersection isn't valid and can't be displayed",
								"Malformed Intersection", JOptionPane.ERROR_MESSAGE);
						return;
					}
					mainPanel.add(intersectionGUI(myIntersection));
					// super.validate();
					super.getContentPane().validate();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			break;
		case JFileChooser.CANCEL_OPTION:
			// fall through
		case JFileChooser.ERROR_OPTION:
			return;
		}
	}

	/**
	 * Save the current intersection to file system in serialised form.
	 */
	private void saveSerialisedIntersection() {
		JOptionPane.showMessageDialog(null, "You haven't implemented this method yet.", "To Do", JOptionPane.INFORMATION_MESSAGE);

	}

	private void loadSerialisedIntersection() {
		JOptionPane.showMessageDialog(null, "You haven't implemented this method yet.", "To Do", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Start intersection cycle
	 */
	private void start() {
		myIntersection.start();
	}

	/**
	 * Constructor for class.
	 */
	public MyIntersectionMonitor() {
		this("Untitled");
	}

	/**
	 * Given the current state, what colour should we use?
	 * 
	 * @param state
	 *            The state we're in.
	 * @return the colour to use.
	 */
	private Color colourForState(State state) {
		Color col;

		switch (state) {
		case RED:
			col = Color.RED;
			break;
		case YELLOW:
			col = Color.YELLOW;
			break;
		case GREEN:
			col = Color.GREEN;
			break;
		case OFF:
			col = Color.BLACK;
			break;
		default:
			col = Color.BLUE;
			break;
		}
		return col;
	}

	/*
	 * Which chips refer to each stream
	 */
	private void addChip(TrafficStream ts, JPanel jp) {
		if (streamStateChips.containsKey(ts)) {
			ArrayList<JPanel> panels = streamStateChips.get(ts);
			panels.add(jp);
			streamStateChips.replace(ts, panels);
		} else {
			ArrayList<JPanel> panels = new ArrayList<JPanel>();
			panels.add(jp);
			streamStateChips.put(ts, panels);
		}

	}

	/**
	 * Implement the Observable interface. We're possibly observing
	 * TrafficStreams - when they change state they will notify us and we'll
	 * update our GUI accordingly.
	 * 
	 * Could also be observing phases. Maybe highlight the current one?
	 * 
	 * We could potentially notify other observers of our own (e.g. to update a
	 * log)
	 */
	@Override
	public void update(Observable o, Object arg) {
		if ((arg instanceof State) && (o instanceof TrafficStream)) {
			TrafficStream ts = (TrafficStream) o;
			State state = (State) arg;
			tp.log(ts + " going " + state);

			update(this.getGraphics());
		} else {
			if (o instanceof Phase) {
				Phase p = (Phase) o;
				JPanel jp;
				if (thePhase != null) {
					jp = phasePanels.get(thePhase);
					jp.setBackground(inactivePhaseBackground);
				}
				jp = phasePanels.get(p);
				jp.setBackground(activePhaseBackground);
				thePhase = p;
				// transcript.append((String) arg);
				tp.log((String) arg);
				update(this.getGraphics());
			}
		}
	}

}
