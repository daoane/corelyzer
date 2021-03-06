package corelyzer.ui.annotation.sampling;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import corelyzer.data.ChatGroup;
import corelyzer.data.CoreSection;
import corelyzer.data.TrackSceneNode;
import corelyzer.graphics.SceneGraph;
import corelyzer.ui.CorelyzerApp;
import corelyzer.util.PropertyListUtility;
import corelyzer.util.StringUtility;
import corelyzer.util.TableSorter;

public class SampleRequestListDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8147115086015596223L;

	public static void main(final String[] args) {
		SampleRequestListDialog dialog = new SampleRequestListDialog( null );
		dialog.pack();
		dialog.setVisible(true);
		// System.exit(0);
	}

	private JPanel contentPane;
	private JButton refreshButton;
	private JButton closeButton;
	private JTable table;

	private JButton sendButton;

	private final SampleRequestTableModel model;

	{
		// GUI initializer generated by IntelliJ IDEA GUI Designer
		// >>> IMPORTANT!! <<<
		// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}

	public SampleRequestListDialog( final JFrame owner ) {
		super( owner );
		setTitle("Sample Request List");
		setContentPane(contentPane);
		// setModal(true);
		getRootPane().setDefaultButton(sendButton);

		refreshButton.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent e) {
				onRefresh();
			}
		});

		closeButton.addActionListener(new ActionListener() {

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
		sendButton.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent actionEvent) {
				onSend();
			}
		});

		// Table decorations
		this.table.setShowVerticalLines(true);
		this.table.setDragEnabled(true);
		this.table.getTableHeader().setReorderingAllowed(true);

		model = new SampleRequestTableModel();
		TableSorter sorter = new TableSorter();
		sorter.setTableModel(model);
		sorter.setTableHeader(table.getTableHeader());

		this.table.setModel(sorter);

		// pre-process header labels
		for (int i = 0; i < model.indexKeyMapping.length; i++) {
			String header = model.indexKeyMapping[i];
			header = this.appendUnit(header);

			if (header.equalsIgnoreCase("requestid")) {
				header = "request ID";
			}

			header = StringUtility.capitalizeHeadingCharacter(header);
			table.getColumnModel().getColumn(i).setHeaderValue(header);
		}
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
		panel2.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
		panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
				| GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		refreshButton = new JButton();
		refreshButton.setText("Refresh");
		panel2.add(refreshButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		sendButton = new JButton();
		sendButton.setEnabled(false);
		sendButton.setText("Send...");
		panel2.add(sendButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		closeButton = new JButton();
		closeButton.setText("Cancel");
		panel2.add(closeButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		contentPane
				.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
						| GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
						0, false));
		panel3.setBorder(BorderFactory.createTitledBorder("Sample Requests"));
		final JScrollPane scrollPane1 = new JScrollPane();
		panel3.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
				| GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0,
				false));
		table = new JTable();
		scrollPane1.setViewportView(table);
	}

	private String appendUnit(String value) {
		if (value.equalsIgnoreCase("samplelocation")) {
			value += " (cm)";
		} else if (value.equalsIgnoreCase("interval")) {
			value += " (mbsf)";
		}

		return value;
	}

	private void getRequestStatus(final Hashtable<String, String> attribs) {
		// TODO async refresh status from requestid
		String status;
		String requestid = attribs.get("requestid");

		if (requestid == null || requestid.equalsIgnoreCase("n/a")) {
			status = "Haven't Submit Yet";
		} else {
			status = "Pending";
		}

		attribs.put("status", status);
	}

	private void onCancel() {
		dispose();
	}

	// Refresh table
	public void onRefresh() {
		if (model != null) {
			model.clear();
		}

		CorelyzerApp app = CorelyzerApp.getApp();
		if (app == null) {
			return;
		}

		int numberOfTracks = app.getTrackListModel().size();

		// Traverse current scene and collect sampleReq-group annotation info
		// Track
		for (int i = 0; i < numberOfTracks; i++) {
			TrackSceneNode t = (TrackSceneNode) app.getTrackListModel().elementAt(i);

			int tId = t.getId();
			String trackName = t.getName();
			float trackOffsetX = SceneGraph.getTrackXPos(tId);
			float trackOffsetY = SceneGraph.getTrackYPos(tId);

			System.out.println("---> [Track] " + trackName + " @ " + trackOffsetX + ", " + trackOffsetY);

			// Section
			for (int j = 0; j < t.getNumCores(); j++) {
				CoreSection cs = t.getCoreSection(j);

				int csId = cs.getId();
				String csName = cs.getName();
				float sectionOffsetX = SceneGraph.getSectionXPos(tId, csId);
				float sectionOffsetY = SceneGraph.getSectionYPos(tId, csId);

				System.out.println("---> [Section] " + csName + " @ " + sectionOffsetX + ", " + sectionOffsetY);

				// Annotations
				int numberOfMarkers = SceneGraph.getNumCoreSectionMarkers(tId, csId);

				for (int k = 0; k < numberOfMarkers; k++) {
					int group = SceneGraph.getCoreSectionMarkerGroup(tId, csId, k);

					String localFile = SceneGraph.getCoreSectionMarkerLocal(tId, csId, k);
					File propFile = new File(localFile);

					if (group != ChatGroup.SAMPLE || !propFile.exists()) {
						continue;
					}

					Hashtable<String, String> attribs = PropertyListUtility.generateHashtableFromFile(propFile);
					if (attribs == null) {
						continue;
					}

					// TODO refresh request status
					// try http://localhost/test/status.php?req=1234
					getRequestStatus(attribs);

					this.model.addAClast(attribs);
				} // end of markers
			} // end of section
		} // end of track
	}

	private void onSend() {
		// TODO send sample request lists to sampleRequest backend database
		JOptionPane.showMessageDialog(this, "[TODO] Send sample request list to backend");

		if (model != null) {
			model.clear();
		}

		CorelyzerApp app = CorelyzerApp.getApp();
		if (app == null) {
			return;
		}

		int numberOfTracks = app.getTrackListModel().size();

		// Traverse current scene and collect sampleReq-group annotation info
		// Track
		for (int i = 0; i < numberOfTracks; i++) {
			TrackSceneNode t = (TrackSceneNode) app.getTrackListModel().elementAt(i);

			int tId = t.getId();

			// Section
			for (int j = 0; j < t.getNumCores(); j++) {
				CoreSection cs = t.getCoreSection(j);
				int csId = cs.getId();

				// Annotations
				int numberOfMarkers = SceneGraph.getNumCoreSectionMarkers(tId, csId);

				for (int k = 0; k < numberOfMarkers; k++) {
					int group = SceneGraph.getCoreSectionMarkerGroup(tId, csId, k);

					String localFile = SceneGraph.getCoreSectionMarkerLocal(tId, csId, k);
					File propFile = new File(localFile);

					if (group != ChatGroup.SAMPLE || !propFile.exists()) {
						continue;
					}

					Hashtable<String, String> attribs = PropertyListUtility.generateHashtableFromFile(propFile);
					if (attribs == null) {
						continue;
					}

					// testing integration with the ANDRILL SMS system...
					/*
					 * try { // add a sampling plan id
					 * attribs.put("samplingPlan.id", "1");
					 * 
					 * // build our payload for POSTing StringBuffer payload =
					 * new StringBuffer(); for (Map.Entry<String, String> entry
					 * : attribs.entrySet()) {
					 * payload.append(URLEncoder.encode(entry.getKey(),
					 * "UTF-8")); payload.append("=");
					 * payload.append(URLEncoder.encode(entry.getValue(),
					 * "UTF-8")); payload.append("&"); }
					 * 
					 * // connect to the service and POST the data URL url = new
					 * URL("http://localhost:8080/sms/api/requestSample");
					 * URLConnection conn = url.openConnection();
					 * conn.setDoOutput(true); OutputStreamWriter wr = new
					 * OutputStreamWriter(conn.getOutputStream());
					 * wr.write(payload.toString()); wr.flush();
					 * 
					 * // get the response BufferedReader rd = new
					 * BufferedReader(new
					 * InputStreamReader(conn.getInputStream())); String line;
					 * 
					 * while ((line = rd.readLine()) != null) {
					 * JOptionPane.showMessageDialog(this, "SMS Response: " +
					 * line); }
					 * 
					 * wr.close(); rd.close(); } catch (Exception e) {
					 * e.printStackTrace(); }
					 */
					// -- end of each send

				} // end of markers
			} // end of section
		} // end of track

	}
}
