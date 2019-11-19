/******************************************************************************
 *
 * CoreWall / Corelyzer - An Initial Core Description Tool
 * Copyright (C) 2008 Julian Yu-Chung Chen
 * Electronic Visualization Laboratory, University of Illinois at Chicago
 *
 * This software is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either Version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with this software; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Questions or comments about CoreWall should be directed to
 * cavern@evl.uic.edu
 *
 *****************************************************************************/
package corelyzer.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import corelyzer.graphics.SceneGraph;

public class CRToolPalette extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4086625321583357717L;
	private JPanel contentPane;
	private JToggleButton normalToggleButton;
	private JToggleButton measureToggleButton;
	private JToggleButton markerToggleButton;
	private JToggleButton clastToggleButton;
	private JToggleButton navigationToggleButton;
	private JToggleButton crossHairToggleButton;
	private JToggleButton mainUIToggleButton;
	private JButton minimizeButton;
	private JButton quitButton;
	private JLabel usageLabel;
	private JButton helpButton;
	private JComboBox<String> measurefield;
	private JTextArea measureText;
	private JPanel measurePane;
	private JPanel toolPane;
	private JPanel framePane;
	private JToggleButton cutToggleButton;

	// declare data
	static final String NORMALMODE = "NormalMode";
	static final String MEASUREMODE = "MeasureMode";
	static final String MARKERMODE = "MarkerMode";
	static final String CLASTMODE = "ClastMode";
	static final String CUTMODE = "CutMode";

	static final String NAVIGATION = "Navigation";
	static final String CROSSHAIR = "CrossHair";

	static final String APPFRAMEMODE = "AppFrameMode";

	static final String MINIMIZEACTION = "MinAppMode";
	static final String CLOSEACTION = "CloseApplication";
	static final String MEASURESELECT = "MeasureSelec";

	public static void main(final String[] args) {
		CRToolPalette dialog = new CRToolPalette();
		dialog.pack();

		// position code
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension mydim = dialog.getSize();
		int myLocX = dim.width / 2 - mydim.width / 2;
		dialog.setLocation(myLocX, 0);

		dialog.setVisible(true);
	}

	// model
	private Hashtable<JComponent, String> descriptionMap;

	private JComponent helpTarget;
	// measure info panel
	private LinkedList<float[]> measureStack;
	private float[] measurePt;
	private boolean bNewMeasure;

	private JTextArea measureClipBoard;
	// Clast UpperLeft coord and LowerRight corners
	private float[] clastUpperLeft = { 0.0f, 0.0f };

	private float[] clastLowerRight = { 0.0f, 0.0f };

	{
		// GUI initializer generated by IntelliJ IDEA GUI Designer
		// >>> IMPORTANT!! <<<
		// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}

	public CRToolPalette() {
		setAlwaysOnTop(true);
		setUndecorated(true);

		setContentPane(contentPane);

		// setModal(true);
		// getRootPane().setDefaultButton(buttonOK);
		/*
		 * contentPane.registerKeyboardAction(new ActionListener() { public void
		 * actionPerformed(ActionEvent e) { onCancel(); } },
		 * KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
		 * JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		 */

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent e) {
				onCancel();
			}
		});

		MouseMotionAdapter listener = new MouseMotionAdapter() {

			@Override
			public void mouseMoved(final MouseEvent event) {
				super.mouseMoved(event);
				onMouseOver((AbstractButton) event.getSource());
			}
		};

		// mouse over events
		normalToggleButton.addMouseMotionListener(listener);
		measureToggleButton.addMouseMotionListener(listener);
		markerToggleButton.addMouseMotionListener(listener);
		clastToggleButton.addMouseMotionListener(listener);
		cutToggleButton.addMouseMotionListener(listener);
		mainUIToggleButton.addMouseMotionListener(listener);
		navigationToggleButton.addMouseMotionListener(listener);
		crossHairToggleButton.addMouseMotionListener(listener);
		minimizeButton.addMouseMotionListener(listener);
		quitButton.addMouseMotionListener(listener);

		// action events
		normalToggleButton.addActionListener(this);
		measureToggleButton.addActionListener(this);
		markerToggleButton.addActionListener(this);
		clastToggleButton.addActionListener(this);
		cutToggleButton.addActionListener(this);

		mainUIToggleButton.addActionListener(this);
		navigationToggleButton.addActionListener(this);
		crossHairToggleButton.addActionListener(this);
		minimizeButton.addActionListener(this);
		quitButton.addActionListener(this);

		CorelyzerApp app = CorelyzerApp.getApp();
		if (app == null) {
			helpButton.addActionListener(new ActionListener() {

				public void actionPerformed(final ActionEvent e) {
					onHelp();
				}
			});
		} else {
			ActionListener helpListener = app.getHelpActionListener();

			if (helpListener != null) {
				helpButton.addActionListener(helpListener);
			} else {
				helpButton.addActionListener(new ActionListener() {

					public void actionPerformed(final ActionEvent e) {
						onHelp();
					}
				});
			}
		}

		initModels();
		initMeasureUI();
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return contentPane;
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT
	 * edit this method OR call it in your code!
	 * 
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		contentPane = new JPanel();
		contentPane.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
		toolPane = new JPanel();
		toolPane.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(toolPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
						| GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JToolBar toolBar1 = new JToolBar();
		toolBar1.setFloatable(false);
		toolPane.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
		normalToggleButton = new JToggleButton();
		normalToggleButton.setIcon(new ImageIcon(getClass().getResource("/corelyzer/ui/resources/normal.gif")));
		normalToggleButton.setSelected(true);
		normalToggleButton.setText("");
		toolBar1.add(normalToggleButton);
		clastToggleButton = new JToggleButton();
		clastToggleButton.setIcon(new ImageIcon(getClass().getResource("/corelyzer/ui/resources/copyright.gif")));
		clastToggleButton.setText("");
		toolBar1.add(clastToggleButton);
		markerToggleButton = new JToggleButton();
		markerToggleButton.setIcon(new ImageIcon(getClass().getResource("/corelyzer/ui/resources/marker.gif")));
		markerToggleButton.setText("");
		toolBar1.add(markerToggleButton);
		measureToggleButton = new JToggleButton();
		measureToggleButton.setIcon(new ImageIcon(getClass().getResource("/corelyzer/ui/resources/ruler.gif")));
		measureToggleButton.setText("");
		toolBar1.add(measureToggleButton);
		cutToggleButton = new JToggleButton();
		cutToggleButton.setIcon(new ImageIcon(getClass().getResource("/corelyzer/ui/resources/cut.gif")));
		toolBar1.add(cutToggleButton);
		final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
		toolBar1.add(toolBar$Separator1);
		navigationToggleButton = new JToggleButton();
		navigationToggleButton.setIcon(new ImageIcon(getClass().getResource("/corelyzer/ui/resources/navigation.gif")));
		navigationToggleButton.setSelected(false);
		navigationToggleButton.setText("");
		toolBar1.add(navigationToggleButton);
		crossHairToggleButton = new JToggleButton();
		crossHairToggleButton.setIcon(new ImageIcon(getClass().getResource("/corelyzer/ui/resources/crosshairs.gif")));
		crossHairToggleButton.setSelected(true);
		crossHairToggleButton.setText("");
		toolBar1.add(crossHairToggleButton);
		final JToolBar.Separator toolBar$Separator2 = new JToolBar.Separator();
		toolBar1.add(toolBar$Separator2);
		framePane = new JPanel();
		framePane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		toolBar1.add(framePane);
		mainUIToggleButton = new JToggleButton();
		mainUIToggleButton.setIcon(new ImageIcon(getClass().getResource("/corelyzer/ui/resources/mainframe.gif")));
		mainUIToggleButton.setSelected(true);
		mainUIToggleButton.setText("");
		framePane.add(mainUIToggleButton);
		final JToolBar.Separator toolBar$Separator3 = new JToolBar.Separator();
		toolBar1.add(toolBar$Separator3);
		minimizeButton = new JButton();
		minimizeButton.setIcon(new ImageIcon(getClass().getResource("/corelyzer/ui/resources/minimize.gif")));
		minimizeButton.setText("");
		toolBar1.add(minimizeButton);
		quitButton = new JButton();
		quitButton.setIcon(new ImageIcon(getClass().getResource("/corelyzer/ui/resources/close.gif")));
		quitButton.setText("");
		toolBar1.add(quitButton);
		final Spacer spacer1 = new Spacer();
		toolPane.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
				GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
		toolPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
				| GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		usageLabel = new JLabel();
		usageLabel.setText("");
		panel1.add(usageLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
				GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		panel1.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		helpButton = new JButton();
		helpButton.setBorderPainted(false);
		helpButton.setEnabled(false);
		helpButton.setIconTextGap(4);
		helpButton.setOpaque(false);
		helpButton.setText("");
		helpButton.putClientProperty("html.disable", Boolean.FALSE);
		panel1.add(helpButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
				GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		measurePane = new JPanel();
		measurePane.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
		toolPane.add(measurePane, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
						| GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
		measurePane
				.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
						| GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
						0, false));
		panel2.setBorder(BorderFactory.createTitledBorder("Measure History"));
		measurefield = new JComboBox<String>();
		final DefaultComboBoxModel<String> defaultComboBoxModel1 = new DefaultComboBoxModel<String>();
		measurefield.setModel(defaultComboBoxModel1);
		panel2.add(measurefield, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer3 = new Spacer();
		measurePane.add(spacer3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
				GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		measureText = new JTextArea();
		measureText.setEditable(false);
		measurePane.add(measureText, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
		ButtonGroup buttonGroup;
		buttonGroup = new ButtonGroup();
		buttonGroup.add(navigationToggleButton);
		buttonGroup.add(crossHairToggleButton);
		buttonGroup = new ButtonGroup();
		buttonGroup.add(normalToggleButton);
		buttonGroup.add(measureToggleButton);
		buttonGroup.add(markerToggleButton);
		buttonGroup.add(clastToggleButton);
		buttonGroup.add(cutToggleButton);
	}

	public void actionPerformed(final ActionEvent event) {
		handleToolFrameActions(event);
	}

	public void addFrameToggleButton(final JToggleButton b, final String name, final String description) {
		b.setActionCommand(name);
		this.descriptionMap.put(b, description);
		this.framePane.add(b);
	}

	public void addMeasure(final float coord[], final int nPoint) {
		// check wether first or second point incoming
		if (nPoint == 1) {
			measurePt = new float[7]; // two vertex and one distance
			measurePt[0] = coord[0]; // store first point coord
			measurePt[1] = coord[1];
		} else if (nPoint == 2) {
			// update measure point info
			bNewMeasure = true; // complete measure entry
			measurePt[2] = coord[0]; // store second point coord
			measurePt[3] = coord[1];

			// make label for this measure
			// split meters and centimeters
			String label = "Measure@";
			if (Math.abs(measurePt[0]) > 100.0f) {
				float meter = measurePt[0] / 100.0f;
				meter = (float) Math.floor(meter);
				float centi = measurePt[0] - meter * 100;
				label += String.format(" %.0fm", meter);
				label += String.format(" %.1fcm", centi);
			} else {
				label += "0 m";
				label += String.format(" %.1fcm", measurePt[0]);
			}

			// calc distance
			double dist = (measurePt[2] - measurePt[0]) * (measurePt[2] - measurePt[0]) + (measurePt[3] - measurePt[1]) * (measurePt[3] - measurePt[1]);
			dist = Math.sqrt(dist);
			measurePt[4] = (float) dist; // store distance value

			// scene center
			measurePt[5] = SceneGraph.getSceneCenterX();
			measurePt[6] = SceneGraph.getSceneCenterY();

			// check size of measure history
			if (measureStack.size() == 20) {
				measurefield.removeItemAt(19);
				// measurefield.insertItemAt("measure " + measureCount, 0);
				measurefield.insertItemAt(label, 0);
				measurefield.setSelectedIndex(0);
				measureStack.removeLast();
				measureStack.addFirst(measurePt);
				// measureCount++;
			} else {
				// measurefield.insertItemAt("measure " + measureCount, 0);
				measurefield.insertItemAt(label, 0);
				measurefield.setSelectedIndex(0);
				measureStack.addFirst(measurePt);
				// measureCount++;
			}
			// update text area
			String result = "Point0 ";
			result = result + String.format("(%.2f,%.2f)\n", measurePt[0], measurePt[1]);
			result = result + "Point1 ";
			result = result + String.format("(%.2f,%.2f)\n", measurePt[2], measurePt[3]);
			result = result + "Distance: ";
			result = result + String.format("%.2f cm", measurePt[4]);
			measureText.setText(result);

			// copy to clipboard
			measureClipBoard.setText(label + "\n" + result);
			measureClipBoard.selectAll();
			measureClipBoard.copy();
		}
	}

	public float[] getClastLowerRight() {
		return clastLowerRight;
	}

	public float[] getClastUpperLeft() {
		return clastUpperLeft;
	}

	private void handleToolFrameActions(final ActionEvent e) {
		String cmd = e.getActionCommand();

		CorelyzerApp app = CorelyzerApp.getApp();
		if (app == null) {
			return;
		}

		// Maybe look at buttongroups instead

		if (NORMALMODE.equals(cmd)) {
			if (this.normalToggleButton.isSelected()) {
				app.setMode(0);

				this.setMeasurePaneVisibility(false);
			}
		} else if (MEASUREMODE.equals(cmd)) {
			if (this.measureToggleButton.isSelected()) {
				app.setMode(1);

				this.setMeasurePaneVisibility(true);
			} else {
				this.setMeasurePaneVisibility(false);
			}
		} else if (MARKERMODE.equals(cmd)) {
			if (this.markerToggleButton.isSelected()) {
				app.setMode(2);

				this.setMeasurePaneVisibility(false);
			}
		} else if (CLASTMODE.equals(cmd)) {
			if (this.clastToggleButton.isSelected()) {
				app.setMode(3);

				this.setMeasurePaneVisibility(false);
			}
		} else if (CUTMODE.equals(cmd)) {
			if (this.cutToggleButton.isSelected()) {
				app.setMode(4);

				this.setMeasurePaneVisibility(false);
			}
		} else if (NAVIGATION.equals(cmd)) {
			SceneGraph.setCrossHair(false);
			app.updateGLWindows();
		} else if (CROSSHAIR.equals(cmd)) {
			SceneGraph.setCrossHair(true);
			app.updateGLWindows();
		} else if (APPFRAMEMODE.equals(cmd)) {
			boolean isVisible;
			isVisible = this.mainUIToggleButton.isSelected();

			JFrame f;
			if (app.isUsePluginUI()) {
				f = app.getPluginFrame();
				app.getMainFrame().setVisible(isVisible);
			} else {
				f = app.getMainFrame();
			}

			f.setVisible(isVisible);
		} else if (MINIMIZEACTION.equals(cmd)) {
			// 4/8/2012 brg: When minimizing on Mac, CorelyzerApp strangely receives a deactivate event before a
			// window iconified event, which causes PaletteVisibilityManager to go haywire. I believe the deactivate
			// event results from the OS's animation of the minimization. Thus we suspend the PVM here, and
			// unsuspend it in CorelyzerApp.windowDeiconified().
			// 11/13/2019 brg: suspending no longer seems to be necessary
			// app.suspendPaletteVisibilityManager(true);
			
			app.getMainFrame().setVisible(true);
			this.mainUIToggleButton.setSelected(true);
			app.getMainFrame().setExtendedState(ICONIFIED);
		} else if (CLOSEACTION.equals(cmd)) {
			WindowEvent ee = new WindowEvent(app.getMainFrame(), WindowEvent.WINDOW_CLOSING);
			app.getMainFrame().dispatchEvent(ee);
		} else if (MEASURESELECT.equals(cmd)) {
			if (bNewMeasure) {
				bNewMeasure = false;
				return;
			}

			// update measure detail text area
			int measureIdx = measurefield.getSelectedIndex();
			float[] mdata = measureStack.get(measureIdx);

			String result = "Point0 ";
			result += String.format("(%.2f,%.2f)\n", mdata[0], mdata[1]);
			result += "Point1 ";
			result += String.format("(%.2f,%.2f)\n", mdata[2], mdata[3]);
			result += "Distance: ";
			result += String.format("%.2f cm", mdata[4]);
			measureText.setText(result);

			// make clipboard content with measure label
			String label = "measure @";
			if (Math.abs(mdata[0]) > 100.0f) {
				float meter = mdata[0] / 100.0f;
				meter = (float) Math.floor(meter);
				float centi = mdata[0] - meter * 100;
				label += String.format(" %.0f m ", meter);
				label += String.format(" %.1f cm\n", centi);
			} else {
				label += " 0 m";
				label += String.format(" %.1f cm\n", mdata[0]);
			}
			measureClipBoard.setText(label + result);
			measureClipBoard.selectAll();
			measureClipBoard.copy();

			// draw this measure data in scenegraph
			SceneGraph.lock();
			{
				SceneGraph.setMeasurePoint(mdata[0], mdata[1], mdata[2], mdata[3]);

				// move scene location to selected measure area
				SceneGraph.positionScene(mdata[5], mdata[6]);
			}
			SceneGraph.unlock();

			app.updateGLWindows();
		}
	}

	private void initMeasureUI() {
		// add measure info panel
		measurefield.setActionCommand(MEASURESELECT);
		measurefield.addActionListener(this);
		measureClipBoard = new JTextArea(4, 30);

		// hide measurePane
		this.toolPane.remove(this.measurePane);
	}

	private void initModels() {
		// init data
		descriptionMap = new Hashtable<JComponent, String>();

		// fill in models
		descriptionMap.put(normalToggleButton, "Normal mode");
		descriptionMap.put(measureToggleButton, "Measure mode");
		descriptionMap.put(markerToggleButton, "Modify annotation marker mode");
		descriptionMap.put(clastToggleButton, "Create annotation mode");
		descriptionMap.put(cutToggleButton, "Cut a section mode");

		descriptionMap.put(navigationToggleButton, "Regular navigation cursor");
		descriptionMap.put(crossHairToggleButton, "CrossHair cursor");
		descriptionMap.put(mainUIToggleButton, "Show/Hide main window");
		descriptionMap.put(minimizeButton, "Minimize Corelyzer");
		descriptionMap.put(quitButton, "Quit Corelyzer");

		normalToggleButton.setName("CRPaletteNormal");
		measureToggleButton.setName("CRPaletteMeasure");
		markerToggleButton.setName("CRPaletteMarker");
		clastToggleButton.setName("CRPaletteClast");
		cutToggleButton.setName("CRPaletteCut");

		navigationToggleButton.setName("CRPaletteNavigation");
		crossHairToggleButton.setName("CRPaletteCrosshair");
		mainUIToggleButton.setName("CRPaletteMainUI");
		minimizeButton.setName("CRPaletteMinimize");
		quitButton.setName("CRPaletteQuit");

		normalToggleButton.setActionCommand(CRToolPalette.NORMALMODE);
		measureToggleButton.setActionCommand(CRToolPalette.MEASUREMODE);
		markerToggleButton.setActionCommand(CRToolPalette.MARKERMODE);
		clastToggleButton.setActionCommand(CRToolPalette.CLASTMODE);
		cutToggleButton.setActionCommand(CRToolPalette.CUTMODE);

		navigationToggleButton.setActionCommand(CRToolPalette.NAVIGATION);
		crossHairToggleButton.setActionCommand(CRToolPalette.CROSSHAIR);
		mainUIToggleButton.setActionCommand(CRToolPalette.APPFRAMEMODE);
		minimizeButton.setActionCommand(CRToolPalette.MINIMIZEACTION);
		quitButton.setActionCommand(CRToolPalette.CLOSEACTION);

		// measure info
		this.measureStack = new LinkedList<float[]>();
		this.bNewMeasure = false;
	}

	public boolean isAppFrameSelected() {
		return mainUIToggleButton.isSelected();
	}

	private void onCancel() {
		dispose();
	}

	private void onHelp() {
		String helpTopic = "Unknown";
		if (helpTarget != null) {
			helpTopic = helpTarget.getName();
		}

		WikiHelpDialog dialog = new WikiHelpDialog(this, helpTopic);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
	}

	private void onMouseOver(final AbstractButton b) {
		String message;
		if (descriptionMap.containsKey(b)) {
			message = descriptionMap.get(b);
			helpTarget = b;
		} else {
			message = "Unknown Action";
		}

		usageLabel.setText(message);
	}

	public void setAppFrameSelected(final boolean value) {
		mainUIToggleButton.setSelected(value);
	}

	public void setClastLowerRight(final float[] clastLowerRight) {
		this.clastLowerRight = clastLowerRight;
	}

	public void setClastUpperLeft(final float[] clastUpperLeft) {
		this.clastUpperLeft = clastUpperLeft;
	}

	@Override
	public void setLocation(final int x, int y) {
		boolean MAC_OS_X = System.getProperty("os.name").toLowerCase().startsWith("mac os x");

		if (MAC_OS_X) {
			y += 20;
		}

		super.setLocation(x, y);
		// this.repaint();
	}

	private void setMeasurePaneVisibility(final boolean isShow) {
		if (isShow) {
			toolPane.add(measurePane, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
							| GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		} else {
			toolPane.remove(this.measurePane);
		}

		this.pack();
	}

	public void setMode(final int imode) {
		// this func is called by popup menu in glcanvas
		// update tool mode button selection
		switch (imode) {
			case 0: // Normal
				normalToggleButton.setSelected(true);
				this.setMeasurePaneVisibility(false);
				break;

			case 1: // Measure
				measureToggleButton.setSelected(true);
				this.setMeasurePaneVisibility(true);
				break;

			case 2: // Marker modification
				markerToggleButton.setSelected(true);
				this.setMeasurePaneVisibility(false);
				break;

			case 3: // Create annotation
				clastToggleButton.setSelected(true);
				this.setMeasurePaneVisibility(false);
				break;

			case 4: // Cut a section to new track
				cutToggleButton.setSelected(true);
				this.setMeasurePaneVisibility(false);
				break;
		}

		// set app to new mode
		CorelyzerApp.getApp().setMode(imode);
	}
}
