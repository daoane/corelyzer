package corelyzer.ui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import corelyzer.controller.CRExperimentController;
import corelyzer.data.CoreSection;
import corelyzer.data.CoreSectionImage;
import corelyzer.data.TrackSceneNode;
import corelyzer.data.coregraph.CoreGraph;
import corelyzer.data.lists.CRDefaultListModel;
import corelyzer.graphics.SceneGraph;

public class ImageSplitDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6459943312826814994L;

	public static void main(final String[] args) {
		ImageSplitDialog dialog = new ImageSplitDialog(null);
		dialog.pack();
		dialog.setVisible(true);
		System.exit(0);
	}

	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JTextField intervalStartField;
	private JTextField intervalEndField;
	private TripleSlider intervalSlider;
	private JCheckBox copyToAnotherTrackCheckBox;

	private JComboBox<Object> trackListComboBox;
	int trackId = -1;
	int sectionId = -1;

	float width = 0.0f;

	{
		// GUI initializer generated by IntelliJ IDEA GUI Designer
		// >>> IMPORTANT!! <<<
		// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}

	public ImageSplitDialog(final JFrame f) {
		super(f);

		myInit();

		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);

		buttonOK.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent e) {
				onOK();
			}
		});

		buttonCancel.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent e) {
				onCancel();
			}
		});

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent e) {
				onCancel();
			}
		});

		contentPane.registerKeyboardAction(new ActionListener() {

			public void actionPerformed(final ActionEvent e) {
				onCancel();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		intervalSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(final ChangeEvent event) {
				DecimalFormat myF = new DecimalFormat("###,###,###.##");

				double intervalStart = intervalSlider.getIntervalStart();
				double intervalEnd = intervalSlider.getIntervalEnd();

				intervalStartField.setText(myF.format(intervalStart));
				intervalEndField.setText(myF.format(intervalEnd));

				// call to native
				if (CorelyzerApp.getApp() != null) {
					// todo visual cue update
					CorelyzerApp.getApp().updateGLWindows();
				}
			}
		});

		copyToAnotherTrackCheckBox.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent e) {
				onCopyToTrackAction();
			}
		});
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
		contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
				| GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
		panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
				| GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		buttonOK = new JButton();
		buttonOK.setText("OK");
		panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		buttonCancel = new JButton();
		buttonCancel.setText("Cancel");
		panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
				| GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200,
				200), null, 0, false));
		final JPanel panel4 = new JPanel();
		panel4.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
		panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
				| GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setText("Cut off interval (cm): ");
		panel4.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
				GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel5 = new JPanel();
		panel5.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
		panel4.add(panel5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
				| GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		intervalSlider = new TripleSlider();
		panel5.add(intervalSlider, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
						| GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JPanel panel6 = new JPanel();
		panel6.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
		panel5.add(panel6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
				| GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		copyToAnotherTrackCheckBox = new JCheckBox();
		copyToAnotherTrackCheckBox.setText("Copy to another track?");
		panel6.add(copyToAnotherTrackCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		trackListComboBox = new JComboBox<Object>();
		trackListComboBox.setEditable(true);
		trackListComboBox.setEnabled(false);
		panel6.add(trackListComboBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel7 = new JPanel();
		panel7.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
		panel4.add(panel7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
				| GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Start: ");
		panel7.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
				GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		intervalStartField = new JTextField();
		intervalStartField.setHorizontalAlignment(11);
		intervalStartField.setText("0.0");
		panel7.add(intervalStartField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label3 = new JLabel();
		label3.setText("End: ");
		panel7.add(label3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
				GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		intervalEndField = new JTextField();
		intervalEndField.setHorizontalAlignment(11);
		intervalEndField.setText("1.0");
		panel7.add(intervalEndField, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
	}

	private void copySplit() {
		CorelyzerApp app = CorelyzerApp.getApp();

		if (app != null) {
			String newTrackName = this.trackListComboBox.getSelectedItem().toString();
			if (newTrackName == null || newTrackName.equals("")) {
				newTrackName = "New Track";
			}

			int[] location = { this.trackId, this.sectionId };
			float intervalStart, intervalEnd;

			try {
				intervalStart = Float.parseFloat(intervalStartField.getText()) / 100.0f;
				intervalEnd = Float.parseFloat(intervalEndField.getText()) / 100.0f;
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Split point input error! " + e);
				return;
			}

			CRExperimentController.cutIntervalToNewTrack(location, 0.0f, intervalStart, CRExperimentController.DOWNCORE_DEPTH, newTrackName);

			CRExperimentController.cutIntervalToNewTrack(location, intervalEnd, width, CRExperimentController.DOWNCORE_DEPTH, newTrackName);

			app.updateGLWindows();
		}

		dispose();
	}

	private void inPlaceSplit() {
		float intervalStart, intervalEnd;

		try {
			intervalStart = Float.parseFloat(intervalStartField.getText());
			intervalEnd = Float.parseFloat(intervalEndField.getText());
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Split point input error! " + e);
			return;
		}

		System.out.println("---> Cut off interval: " + intervalStart + " - " + intervalEnd + " (cm)");

		// this section remain the first half
		SceneGraph.setSectionIntervalTop(trackId, sectionId, 0);
		SceneGraph.setSectionIntervalBottom(trackId, sectionId, intervalStart);

		// Ops to make duplicate CoreSection but with the same texture model
		CoreGraph cg = CoreGraph.getInstance();
		TrackSceneNode t = cg == null ? null : cg.getCurrentTrack();

		if (t == null) {
			System.err.println("---> NULL TrackSceneNode, return");
			return;
		}

		// C side
		int newSectionId = SceneGraph.duplicateSection(trackId, sectionId);
		System.out.println("---> [INFO] Duplicate section Id: " + newSectionId);

		if (newSectionId != -1) {
			// assign visibility
			SceneGraph.setSectionIntervalTop(trackId, newSectionId, intervalEnd);
			SceneGraph.setSectionIntervalBottom(trackId, newSectionId, width);

			// Java side with CoreGraph
			int imageId = SceneGraph.getImageIdForSection(trackId, newSectionId);
			System.out.println("---> [INFO] Duplicate image id: " + imageId);

			String name = JOptionPane.showInputDialog(this, "Please input new section name");

			CoreSection sec = new CoreSection(name, newSectionId);
			t.addCoreSection(sec);
			
			SceneGraph.setSectionName( trackId, newSectionId, name );

			String imageFilePath = SceneGraph.getImageName(imageId);
			CoreSectionImage node = new CoreSectionImage(t, imageFilePath, imageId, name);
			t.addChild(node, newSectionId, imageId);
			t.Update();
			cg.notifyListeners();
		}

		// refresh screen
		CorelyzerApp app = CorelyzerApp.getApp();
		if (app != null) {
			app.updateGLWindows();
		}

		dispose();
	}

	private void myInit() {
		CorelyzerApp app = CorelyzerApp.getApp();

		if (app != null) {
			CRDefaultListModel model = app.getTrackListModel();
			this.setListModel(model);
		}
	}

	private void onCancel() {
		dispose();
	}

	private void onCopyToTrackAction() {
		this.trackListComboBox.setEnabled(this.copyToAnotherTrackCheckBox.isSelected());
	}

	private void onOK() {
		split();
	}

	public void setListModel(final CRDefaultListModel model) {
		DefaultComboBoxModel<Object> cbModel = new DefaultComboBoxModel<Object>();

		for (int i = 0; i < model.size(); i++) {
			cbModel.addElement(model.elementAt(i));
		}

		this.trackListComboBox.setModel(cbModel);
	}

	public void setProperties(final int trackId, final int sectionId) {
		this.trackId = trackId;
		this.sectionId = sectionId;

		// original section properties
		boolean orientation = SceneGraph.getSectionOrientation(trackId, sectionId);
		float dpix = SceneGraph.getSectionDPIX(trackId, sectionId);

		int imageId = SceneGraph.getImageIdForSection(trackId, sectionId);
		float imageHeight = SceneGraph.getImageHeight(imageId);
		float imageWidth = SceneGraph.getImageWidth(imageId);

		float width;
		if (orientation == SceneGraph.PORTRAIT) { // true: portrait
			width = imageHeight / dpix * 2.54f;
		} else { // false: landscape
			width = imageWidth / dpix * 2.54f;
		}

		this.width = width;
		this.intervalSlider.setScaleValue(width);
	}

	public void setSectionName(final String aName) {
		this.setTitle("Section Image: " + aName);
	}

	private void split() {
		if (this.copyToAnotherTrackCheckBox.isSelected()) {
			this.copySplit();
		} else {
			this.inPlaceSplit();
		}
	}
}
