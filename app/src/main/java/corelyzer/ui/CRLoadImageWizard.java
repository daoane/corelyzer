package corelyzer.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;

import net.miginfocom.swing.MigLayout;

import corelyzer.data.*;
import corelyzer.data.ImagePropertyTable.ImageProperties;
import corelyzer.data.coregraph.*;
import corelyzer.graphics.SceneGraph;
import corelyzer.util.FileUtility;

// This "wizard" attempts to simplify the section image loading process. It allows multiple
// tracks to be created and populated in a single transaction. It makes best guesses as to
// where selected image files should be added to existing tracks (if possible) based on their
// names. Users can then reorder these images as they wish before loading occurs.

public class CRLoadImageWizard extends JDialog {
	public static void main(final String[] args) {
		CRLoadImageWizard dialog = new CRLoadImageWizard(null, null);
		dialog.pack();
		dialog.setVisible(true);
		
		System.exit(0);
	}

	private JPanel contentPane, activePane;
	private JButton nextButton, previousButton, finishButton, cancelButton;
	private SectionListPane sectionListPane;
	private ImagePropertiesPane imagePropertiesPane;
	private boolean firstOpenOfPropertiesPane = true;
	private TrackSectionListModel trackSectionModel;
	private Vector<File> newFiles;
	
	public static String UNRECOGNIZED_SECTIONS_TRACK = "Unrecognized Sections";

	public CRLoadImageWizard(final Frame owner, final Vector<File> newFiles) {
		super(owner);
		this.newFiles = newFiles;
		
		loadTrackData();		
		setupUI();
		updateUI();
	}

	// add panel and buttons for active pane
	private void updateUI()
	{
		contentPane.removeAll();
		contentPane.add( activePane );
		
		if ( activePane.equals( sectionListPane ))
		{
			setTitle("Arrange New Sections");
			contentPane.add(nextButton, "gapy 10, split 2, align right");
			contentPane.add(cancelButton);
			getRootPane().setDefaultButton( nextButton );
		}
		else
		{
			setTitle("Set Section Image Properties");
			contentPane.add(previousButton, "gapy 10, split 3, align right");
			contentPane.add(finishButton);
			contentPane.add(cancelButton);
			getRootPane().setDefaultButton( finishButton );
		}

		pack();
		repaint();
	}
	
	private void onNext()
	{
		if ( firstOpenOfPropertiesPane )
		{
			// User may modify values in properties pane, then return to previous pane -
			// Make sure we don't overwrite potential edits by initializing again!
			initializeSectionImageProperties();
			firstOpenOfPropertiesPane = false;
		}
		
		// create vector of new sections and hand off to image properties dialog
		Vector<TrackSectionListElement> newSections = new Vector<TrackSectionListElement>();
		for ( Vector<TrackSectionListElement> track : trackSectionModel.getTrackSectionVector() ) {
			for ( TrackSectionListElement section : track ) {
				if ( section.isNewSection() ) {
					newSections.add( section );
				}
			}
		}
		
		imagePropertiesPane.setNewSections( newSections );
		activePane = imagePropertiesPane;

		updateUI();
	}
	
	private void onPrevious()
	{
		imagePropertiesPane.updateSectionProperties();
		activePane = sectionListPane;
		updateUI();
	}
	
	private void onFinish() { 
		imagePropertiesPane.updateSectionProperties();
		Runnable loading = new Runnable() {
			public void run() {
				onConfirmLoad();
			}
		};
		new Thread(loading).start();
		
		dispose();
	}
	
	// create UI components, they'll be added to the content pane in updateUI()
	private void setupUI()
	{
		sectionListPane = new SectionListPane( trackSectionModel );
		imagePropertiesPane = new ImagePropertiesPane();
		
		contentPane = new JPanel( new MigLayout( "wrap 1" ));
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});
		
		nextButton = new JButton("Next >");
		nextButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				onNext();
			}
		});
		
		previousButton = new JButton("< Previous");
		previousButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				onPrevious();
			}
		});
		
		finishButton = new JButton("Finish");
		finishButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				onFinish();
			}
		});
		
		setContentPane( contentPane );
		
		activePane = sectionListPane; // set initial pane in preparation for updateUI()
	}
	
	private TrackSceneNode createTrack(final Session session, final String trackName)
	{
		// we'll stifle duplication in GUI
		TrackSceneNode newTrack = null;
		int newTrackId = SceneGraph.addTrack( session.getName(), trackName );
		if ( newTrackId >= 0 )
		{
			// create the node and add it to the listing of tracks
			newTrack = new TrackSceneNode( trackName, newTrackId );
			CoreGraph.getInstance().addTrack( session, newTrack );
		}

		return newTrack;
	}
	
	private void loadTrackData()
	{
		if ( newFiles == null )
			return;
		
		Vector<Vector<TrackSectionListElement>> tsVec = new Vector<Vector<TrackSectionListElement>>();
		Vector<String> apparentTrack = new Vector<String>(); // one per vector (each corresponds to a track) in tsVec
		Vector<String> trackNameVec = new Vector<String>();
		Vector<String> sectionNameVec = new Vector<String>();
		
		// First, build vector of vectors of existing tracks and sections, one vector per track
		CoreGraph cg = CoreGraph.getInstance();
		for ( Session s : cg.getSessions() )
		{
			for ( TrackSceneNode tsn : s.getTrackSceneNodes() )
			{
				Vector<TrackSectionListElement> trackVec = new Vector<TrackSectionListElement>();
				boolean firstSection = true;
				trackNameVec.add( tsn.getName() );
				
				// add entry for empty track to keep apparentTrack indices aligned with tracks
				if ( tsn.getNumCores() == 0 )
					apparentTrack.add( tsn.getName() );
				
				for ( CoreSection cs : tsn.getCoreSections() )
				{
					if ( firstSection ) {
						// make best guess of track name based on name of first section in each
						// track: thus the Nth element of apparentTrack corresponds to the
						// Nth element (a track) in tsVec.
						final String fullTrackID = FileUtility.parseFullTrackID( cs.getName() );
						if ( fullTrackID == null ) {
							// couldn't parse track out of filename
							apparentTrack.add( null );
						} else {
							apparentTrack.add( fullTrackID );
						}
						firstSection = false;
					}
					
					TrackSectionListElement newSection = new TrackSectionListElement( cs.getName(), false );
					
					// grab properties from scenegraph
					newSection.setImageProperties( getSectionProperties( tsn.getId(), cs.getId() ));

					trackVec.add( newSection );
					sectionNameVec.add( cs.getName() );
				}
				
				tsVec.add( trackVec );
			}
		}
		
		// If any loaded files are duplicates of existing sections, remove them
		Vector<File> cleanedNewFiles = (Vector<File>)newFiles.clone();
		for ( File f : newFiles )
		{
			final String strippedFilename = FileUtility.stripExtension( f.getName() );
			if ( sectionNameVec.indexOf( strippedFilename ) != -1 ) {
				System.out.println( "ignoring apparent duplicate section " + strippedFilename );
				cleanedNewFiles.remove( f );
			}
		}
		
		// For each new file, attempt to find a matching track: add if found, else create a new track vector.
		// Sections whose names can't be parsed properly are added to an "unrecognized" track so the user can
		// position them manually if desired.
		Vector<TrackSectionListElement> unrecognizedSectionsVec = new Vector<TrackSectionListElement>();
		unrecognizedSectionsVec.add( new TrackSectionListElement( UNRECOGNIZED_SECTIONS_TRACK, false /* prevent renaming */, true ));
		for ( File curFile : cleanedNewFiles )
		{
			final String strippedFilename = FileUtility.stripExtension( curFile.getName() );
			TrackSectionListElement newSection = new TrackSectionListElement( strippedFilename, true, false, curFile );
			
			
			final String fullTrackID = FileUtility.parseFullTrackID( strippedFilename ); 
			if ( fullTrackID == null )
			{
				unrecognizedSectionsVec.add( newSection );
				continue;
			}
			
			int matchingTrackIndex = -1;
			if (( matchingTrackIndex = apparentTrack.indexOf( fullTrackID )) != -1 )
			{
				// found matching track
				tsVec.get( matchingTrackIndex ).add( newSection );
				Collections.sort( tsVec.get( matchingTrackIndex ), new AlphanumComparator.TSLEAlphanumComparator() );
			}
			else
			{
				// no match, create new track and add section to it
				Vector<TrackSectionListElement> newTrack = new Vector<TrackSectionListElement>();
				newTrack.add( newSection );
				tsVec.add( newTrack );
				apparentTrack.add( fullTrackID );
			}
		}
		
		// now that they won't disrupt sorting, add track elements
		int oldTrackIndex = 0, newTrackIndex = 1;
		for ( Vector<TrackSectionListElement> trackVec : tsVec )
		{
			if ( oldTrackIndex < trackNameVec.size() ) {
				trackVec.insertElementAt( new TrackSectionListElement( trackNameVec.elementAt( oldTrackIndex ), false, true ), 0 );
				oldTrackIndex++;
			} else {
				String newTrackName = FileUtility.parseFullTrackID( trackVec.elementAt( 0 ).getName() );
				if ( newTrackName == null )
					newTrackName = "New Track" + newTrackIndex++;
				trackVec.insertElementAt( new TrackSectionListElement( newTrackName, true, true ), 0 );
			}
		}
		
		// finally, add unrecognized sections vector if it contains any sections (remember that the first element
		// represents the track).
		if ( unrecognizedSectionsVec.size() > 1 )
		{
			tsVec.add( unrecognizedSectionsVec );
			JOptionPane.showMessageDialog( this, "One or more images could not be auto-sorted. They have been added to the Unrecognized Sections" +
					"\nlist and should be moved. Images remaining in Unrecognized Sections will not be loaded." );
		}
		
		trackSectionModel = new TrackSectionListModel( tsVec );
	}
	
	private void onConfirmLoad()
	{
		int curProgressValue = 1;
		JProgressBar progress = CorelyzerApp.getApp().getProgressUI();
		progress.setString("Loading Images");
		progress.setMaximum( trackSectionModel.getNewSectionCount() + 1 );
		progress.setValue( curProgressValue );
		
		final Vector<Vector<TrackSectionListElement>> tsVec = trackSectionModel.getTrackSectionVector();
		
		Session session = CoreGraph.getInstance().getCurrentSession();
		
		for ( int tsVecIndex = 0; tsVecIndex < tsVec.size(); tsVecIndex++ )
		{
			Vector<TrackSectionListElement> trackVec = tsVec.elementAt( tsVecIndex );
			TrackSceneNode curTrack = null;
			final TrackSectionListElement trackElt = trackVec.elementAt( 0 );
			if ( trackElt.isNewTrack() )
			{
				// never load sections left in Unrecognized Sections track
				if ( trackElt.getName().equals( UNRECOGNIZED_SECTIONS_TRACK ))
					continue;
				
				curTrack = createTrack( session, trackElt.getName() );
			}
			else
				curTrack = session.getTrackSceneNodeWithIndex( tsVecIndex );
			if ( curTrack != null )
			{
				for ( int eltIndex = 1; eltIndex < trackVec.size(); eltIndex++ )
				{
					final TrackSectionListElement sectionElt = trackVec.elementAt( eltIndex );
					if ( sectionElt.isNew() )
					{						
						File imageFile = sectionElt.getImageFile();
						if ( imageFile != null ) {
							// add section to track
							progress.setString("Loading " + imageFile.getName() );
							
							final ImagePropertyTable.ImageProperties imageProps = sectionElt.getImageProperties();
							boolean isVertical = imageProps.orientation.equals("Vertical");
							//System.out.println(imageFile.getName() + " depth pixels = " + 
							//		SceneGraph.getImageDepthPix( imageFile.toString(), isVertical ));
							
							final int insertIndex = eltIndex - 1;
							final int sectionId = FileUtility.loadImageFile( imageFile, null, sectionElt.getName(), curTrack, insertIndex );
							if ( sectionId != -1 )
							{
								FileUtility.setSectionImageProperties(curTrack, sectionElt.getName(), sectionId,
										imageProps.length, imageProps.depth,
										imageProps.dpix, imageProps.dpiy, imageProps.orientation);
							}
							progress.setValue(++curProgressValue);
						} else {
							System.out.println("New section has null imageFile");
						}
					}
					else
					{
						// if sections were inserted, need to adjust existing sections' depth
						final int sectionId = curTrack.getCoreSection( sectionElt.getName() ).getId();
						final float oldDepthInCM = SceneGraph.getSectionDepth( curTrack.getId(), sectionId );
						final float newDepthInCM = sectionElt.getImageProperties().depth * 100.0f; // convert m to cm
						final float depthChangeInPix = ( newDepthInCM - oldDepthInCM ) * SceneGraph.getCanvasDPIX( 0 ) * ( 1.0f / 2.54f );
						if ( Math.abs( depthChangeInPix ) > 0.0f )
							SceneGraph.moveSection( curTrack.getId(), sectionId, depthChangeInPix, 0.0f );
					}
				}
			}
			else {
				System.out.println("Couldn't create/load track");
			}
		}
		
		progress.setString("Section image loading complete");
		progress.setValue(0);
	}
	
	private void initializeSectionImageProperties()
	{
		for ( Vector<TrackSectionListElement> track : trackSectionModel.getTrackSectionVector() )
		{
			// If the track has a pre-existing section, note its image properties
			// to use as defaults for new sections.
			ImagePropertyTable.ImageProperties defaultProps = null;
			for ( TrackSectionListElement section : track ) {
				if ( !section.isNewSection() ) {
					defaultProps = section.getImageProperties();
					break;
				}
			}
			
			float curDepth = 0.0f;
			for ( int secIndex = 1; secIndex < track.size(); secIndex++ )
			{
				TrackSectionListElement section = track.elementAt( secIndex );
				if ( !section.isNew() ) {
					curDepth = section.getImageProperties().depth + section.getImageProperties().length;
				} else {
					// new section, init properties with defaults
					section.getImageProperties().depth = curDepth;
					section.getImageProperties().length = 1.5f; // meters

					if ( defaultProps != null ) {
						section.getImageProperties().orientation = defaultProps.orientation;
						section.getImageProperties().dpix = defaultProps.dpix;
						section.getImageProperties().dpiy = defaultProps.dpiy;
					} else {
						defaultProps = new ImagePropertyTable.ImageProperties();
					}

					// attempt to determine section's actual length
					File imageFile = section.getImageFile();
					if ( imageFile != null ) {
						final boolean isVertical = defaultProps.orientation.equals("Vertical");
						final float depthDPI = isVertical ? defaultProps.dpiy : defaultProps.dpix;
						final int lengthInPix = SceneGraph.getImageDepthPix( imageFile.toString(), isVertical );
						section.getImageProperties().length = (( lengthInPix / depthDPI ) * 2.54f ) / 100.0f;
					} else {
						System.out.println("New section has null imageFile");
					}
					
					curDepth += section.getImageProperties().length;
					
					// if necessary, push subsequent pre-existing sections deeper to create space
					// TODO: only push if there isn't sufficient space for the new core to be
					// added without overlapping.
					boolean firstSubSec = true;
					float depthOffset = 0.0f;
					for ( int subSecIndex = secIndex + 1; subSecIndex < track.size(); subSecIndex++ ) {
						TrackSectionListElement subSection = track.elementAt( subSecIndex );
						if ( !subSection.isNew() )
						{
							if ( firstSubSec )
							{
								depthOffset = curDepth - subSection.getImageProperties().depth;
								subSection.getImageProperties().depth = curDepth;
								firstSubSec = false;
							}
							else
							{
								subSection.getImageProperties().depth += depthOffset;
							}
						}
					}
				}
			}
		}
	}
	
	private ImagePropertyTable.ImageProperties getSectionProperties( final int trackId, final int sectionId )
	{
		ImagePropertyTable.ImageProperties props = new ImagePropertyTable.ImageProperties();
		// use getImageIDForSection to determine whether section is legitimate
		if ( SceneGraph.getImageIdForSection( trackId, sectionId ) != -1 )
		{
			props.depth = SceneGraph.getSectionDepth( trackId, sectionId ) / 100.0f; // convert cm depth to m
			props.length = SceneGraph.getSectionLength( trackId, sectionId ) / 100.0f; // convert cm length to m
			props.dpix = SceneGraph.getSectionDPIX( trackId, sectionId );
			props.dpiy = SceneGraph.getSectionDPIY( trackId, sectionId );
			props.orientation = SceneGraph.getSectionOrientation( trackId, sectionId ) ? "Vertical" : "Horizontal";
		}
		
		return props;
	}
}


class SectionListPane extends JPanel implements ListSelectionListener {
	private JButton renameButton, deleteButton, moveUpButton, moveDownButton, newButton, imagePropsButton;
	private JScrollPane tslScrollPane;
	private JList trackSectionList;
	private TrackSectionListModel trackSectionModel;
	
	public SectionListPane( TrackSectionListModel trackSectionModel )
	{
		super( new MigLayout( "wrap 2, fillx" ));
		
		this.trackSectionModel = trackSectionModel;
		setupUI();
		
		trackSectionList.setModel( trackSectionModel );
	}
	
	// The lone ListSelectionListener interface method
	public void valueChanged(ListSelectionEvent e)
	{
		// This is called twice when the selection changes, first with getValueIsAdjusting()
		// returning true, second time false. No idea why, but there's no need to do things twice.
		if ( !e.getValueIsAdjusting() )
		{
			final int[] selectedIndices = trackSectionList.getSelectedIndices();
			
			int trackCount = 0, newTrackCount = 0, newSectionCount = 0, oldSectionCount = 0;
			for ( int curSelIndex : selectedIndices )
			{
				TrackSectionListElement tsle = (TrackSectionListElement)trackSectionModel.getElementAt( curSelIndex );
				if ( tsle.isTrack() ) {
					trackCount++; // count each track
					if ( tsle.isNew() )
						newTrackCount++; // of those tracks, how many are new?
				} else if (	tsle.isNew() ) {
					newSectionCount++;
				} else {
					oldSectionCount++;
				}
			}
				
			boolean deletableTrack = false;
			if  ( trackCount == 1 && newSectionCount + oldSectionCount == 0 )
			{
				final int curSelIndex = selectedIndices[0];
				TrackSectionListElement tsle = (TrackSectionListElement)trackSectionModel.getElementAt( curSelIndex );
				if ( tsle.isNewTrack() )
				{
					// empty tracks can be deleted
					if ( curSelIndex == trackSectionModel.getSize() - 1 ) {
						// if current track is the last item in the list, it contains no sections
						deletableTrack = true;
					} else {
						// if next element is a track, current track contains no sections
						final TrackSectionListElement nextElt = (TrackSectionListElement)trackSectionModel.getElementAt( curSelIndex + 1 );
						if ( nextElt.isTrack() )
							deletableTrack = true;
					}
				}
			}
			
			// Only newly added sections can be moved up/down and deleted
			enableSectionButtons( newSectionCount > 0 && trackCount + oldSectionCount == 0 );
			
			// Only a single new track can be renamed at a time
			enableTrackButtons( trackCount == 1 && newTrackCount == 1 && newSectionCount + oldSectionCount == 0);
			
			// Only new sections and empty tracks can be deleted
			enableDeleteButton(( newSectionCount > 0 && oldSectionCount + trackCount == 0 ) || deletableTrack );
		}
	}
	
	private void doRenameTrack()
	{
		String newTrackName = JOptionPane.showInputDialog( this, "Please enter new track name", "[new name]" );
		if ( newTrackName != null )
		{
			trackSectionModel.renameTrack( trackSectionList.getSelectedIndex(), newTrackName );
			trackSectionList.repaint();
		}
	}
	
	private void enableSectionButtons( final boolean enable ) {
		moveUpButton.setEnabled( enable );
		moveDownButton.setEnabled( enable );
	}
	
	private void enableTrackButtons( final boolean enable ) { renameButton.setEnabled( enable ); }
	private void enableDeleteButton( final boolean enable ) { deleteButton.setEnabled( enable ); }
	
	private void setupUI() {
		JLabel iconExplainLabel = new JLabel("Indicates loaded section or newly-created track");
		iconExplainLabel.setIcon( new ImageIcon( "resources/icons/newCircle.gif" ));
		this.add( iconExplainLabel, "span 2, align left" );

		trackSectionList = new JList();
		trackSectionList.setCellRenderer(new TrackSectionListCellRenderer());
		trackSectionList.addListSelectionListener( this );
		
		tslScrollPane = new JScrollPane();
		tslScrollPane.setViewportView(trackSectionList);
		this.add(tslScrollPane, "width 250::, height 400::, growx");
		
		moveUpButton = new JButton("Move Up");
		moveUpButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// the array of selected indices is guaranteed to be in ascending order
				int[] selectedIndices = trackSectionList.getSelectedIndices();
				for ( int i = 0; i < selectedIndices.length; i++ ) {
					final boolean moved = trackSectionModel.moveItemUp( selectedIndices[i] );
					
					// When moving up, we try to move the uppermost item first. If it can't be
					// moved, nothing beneath it can be moved either.
					if ( !moved )
						return;
				}

				// if we make it here, all items were moved successfully
				for ( int i = 0; i < selectedIndices.length; i++ ) {
					selectedIndices[i]--;
				}
				trackSectionList.setSelectedIndices( selectedIndices );
				trackSectionList.repaint();
			}
		});
		this.add(moveUpButton, "split 5, flowy");
		
		moveDownButton = new JButton("Move Down");
		moveDownButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// the array of selected indices is guaranteed to be in ascending order
				int[] selectedIndices = trackSectionList.getSelectedIndices();
				for ( int i = selectedIndices.length - 1; i >= 0; i-- ) {
					final boolean moved = trackSectionModel.moveItemDown( selectedIndices[i] );
					
					// When moving down, we try move the bottommost item first. If it can't be moved,
					// nothing above it can be moved either.
					if ( !moved )
						return;
				}
				
				for ( int i = 0; i < selectedIndices.length; i++ ) {
					selectedIndices[i]++;
				}
				trackSectionList.setSelectedIndices( selectedIndices );
				trackSectionList.repaint();
			}
		});
		this.add(moveDownButton);

		deleteButton = new JButton("Delete");
		deleteButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				final int[] selectedIndices = trackSectionList.getSelectedIndices();
				
				for ( int i = 0; i < selectedIndices.length; i++ ) {
					trackSectionModel.deleteItem( selectedIndices[i] );
					
					// adjust subsequent indices to account for the just-deleted item
					for ( int j = i + 1; j < selectedIndices.length; j++ )
						selectedIndices[j]--;
				}
				trackSectionList.repaint();
			}
		});
		this.add(deleteButton);
		
		renameButton = new JButton("Rename Track");
		renameButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				doRenameTrack();
			}
		});
		this.add(renameButton);
		
		newButton = new JButton("New Track");
		newButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				trackSectionModel.newTrack();
				trackSectionList.repaint();
			}
		});
		this.add(newButton);
	}
}

class ImagePropertiesPane extends JPanel {
	
	ImagePropertyTable imageTable;
	BatchInputPanel batchPanel;
	Vector<TrackSectionListElement> newSections;
	
	public ImagePropertiesPane()
	{
		super( new MigLayout( "wrap 1, fillx" ));
		
		setupUI();
	}
	
	public void setNewSections( Vector<TrackSectionListElement> newSections )
	{
		this.newSections = newSections;

		// load section properties into table
		imageTable.clearTable();
		for ( TrackSectionListElement section : newSections )
		{
			ImagePropertyTable.ImageProperties props = section.getImageProperties();
			imageTable.addImageAndProperties( section.getName(), props.orientation, props.length, props.dpix, props.dpiy, props.depth );
		}
	}
	
	// sync table values with section properties
	public void updateSectionProperties()
	{
		for (int i = 0; i < imageTable.getRowCount(); i++) {
			TrackSectionListElement section = newSections.elementAt( i );
			
			section.getImageProperties().orientation = (String) imageTable.model.getValueAt(i, 1);
			section.getImageProperties().length = (Float) imageTable.model.getValueAt(i, 2);
			section.getImageProperties().dpix = (Float) imageTable.model.getValueAt(i, 3);
			section.getImageProperties().dpiy = (Float) imageTable.model.getValueAt(i, 4);
			section.getImageProperties().depth = (Float) imageTable.model.getValueAt(i, 5);
		}
	}

	private void setupUI()
	{
		imageTable = new ImagePropertyTable();

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView( imageTable );
		this.add( scrollPane, "height 200:400:, growx");
		
		batchPanel = new BatchInputPanel( imageTable );
		this.add( batchPanel, "growx" );
		
		imageTable.updateUI();
	}	
}


class TrackSectionListModel extends AbstractListModel
{
	// Each Vector<TrackSectionListElement> represents a track and the sections it includes.
	// The track is always the 0th element of the vector, followed by sections in order.
	Vector<Vector<TrackSectionListElement>> tsVec;
	int newTrackCount = 1;
	
	TrackSectionListModel(Vector<Vector<TrackSectionListElement>> tsVec) {
		this.tsVec = tsVec;
	}
	
	public Vector<Vector<TrackSectionListElement>> getTrackSectionVector()
	{
		return tsVec;
	}
	
	public void renameTrack(final int index, final String newTrackName)
	{
		Point p = getEltIndex( index );
		if ( p != null && p.y == 0 )
			tsVec.elementAt( p.x ).elementAt( p.y ).setName( newTrackName );
	}
	
	public void newTrack()
	{
		final int origSize = this.getSize();
		Vector<TrackSectionListElement> newTrack = new Vector<TrackSectionListElement>();
		final String name = "New Track" + newTrackCount++;
		newTrack.add( new TrackSectionListElement( name, true, true ));
		tsVec.add( newTrack );

		fireIntervalAdded(this, origSize, origSize);
	}
	
	public void deleteItem(final int index)
	{
		Point p = getEltIndex( index );
		if ( p != null )
		{
			final TrackSectionListElement curElt = tsVec.elementAt( p.x ).elementAt( p.y );
			
			tsVec.elementAt( p.x ).remove( p.y );

			// if deleted element is a track, remove track vector
			if ( curElt.isTrack() && tsVec.elementAt( p.x ).isEmpty() )
				tsVec.remove( p.x );
		}
		
		fireIntervalRemoved( this, index, index );
	}
	
	// Convert JList's 1-D index to a 2-D vector index/section index pair: returned
	// Point.x indicates vector index, Point.y indicates element (section) in that vector
	private Point getEltIndex(final int index)
	{
		Point result = null;
		int vecIndex = 0, curIndex = 0;
		for ( Vector<TrackSectionListElement> v : tsVec )
		{
			int lowIndex = curIndex, hiIndex = curIndex + v.size() - 1;
			if ( index >= lowIndex && index <= hiIndex )
			{
				result = new Point( vecIndex, index - lowIndex );
				break;
			}
			
			curIndex += v.size();
			vecIndex++;
		}

		return result;
	}
	
	public boolean moveItemUp(final int index)
	{
		boolean moved = false;
		
		// figure out what index maps to
		Point p = getEltIndex( index );
		if ( p.y > 1 )
		{
			// swap within vector
			TrackSectionListElement elt = tsVec.elementAt( p.x ).remove( p.y );
			tsVec.elementAt( p.x ).insertElementAt( elt, p.y - 1 );
			moved = true;
		}
		else
		{
			// append to previous vector
			if ( p.x > 0 )
			{
				TrackSectionListElement elt = tsVec.elementAt( p.x ).remove( p.y );
				tsVec.elementAt( p.x - 1 ).add( elt );
				moved = true;
			}
		}
		
		if ( moved )
			fireContentsChanged( this, index - 1, index );

		return moved;
	}
	
	public boolean moveItemDown(final int index)
	{
		boolean moved = false;
		
		// figure out what index maps to
		Point p = getEltIndex( index );
		if ( p.y == tsVec.elementAt( p.x ).size() - 1 )
		{
			// move to top of next vector if possible
			if ( p.x + 1 < tsVec.size() )
			{
				TrackSectionListElement elt = tsVec.elementAt( p.x ).remove( p.y );
				tsVec.elementAt( p.x + 1 ).insertElementAt( elt, 1 );
				moved = true;
			}
		}
		else
		{
			// swap within vector
			TrackSectionListElement elt = tsVec.elementAt( p.x ).remove( p.y );
			tsVec.elementAt( p.x ).insertElementAt( elt, p.y + 1 );
			moved = true;
		}
		
		if ( moved )
			fireContentsChanged( this, index, index + 1 );

		return moved;
	}
	
	public int getNewSectionCount()
	{
		int count = 0;
		for ( Vector<TrackSectionListElement> trackVec : tsVec ) {
			for ( TrackSectionListElement tsle : trackVec ) {
				if ( tsle.isNewSection() )
					count++;
			}
		}
		return count;
	}

	// ListModel methods
	public Object getElementAt(int index)
	{ 
		Object element = null;
		Point p = getEltIndex( index );
		if ( p != null )
			element = tsVec.elementAt( p.x ).elementAt( p.y );

		return element;
	}
	
	public int getSize()
	{ 
		int size = 0;
		for ( Vector<TrackSectionListElement> v : tsVec )
			size += v.size();
		return size;
	}
}

class TrackSectionListCellRenderer extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected,
			final boolean cellHasFocus)
	{
		JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		TrackSectionListElement tsle = (TrackSectionListElement)value;
		if ( tsle.isNew() )
		{
			// Indicate newly-added tracks and sections with an icon
			label.setIcon(new ImageIcon("resources/icons/newCircle.gif"));
		}
		
		if ( tsle.isTrack() )
		{
			// Wrap tracks in text, and add distinct background color so they're more easily
			// distinguishable from sections.
			final boolean isUnrecognizedTrack = tsle.getName().equals( CRLoadImageWizard.UNRECOGNIZED_SECTIONS_TRACK );
			
			final String trackName = isUnrecognizedTrack ? tsle.getName() : ( "[Track: " + tsle.getName() + "]" );
			label.setText( trackName );

			if ( !isSelected )
			{
				// Red for Unrecognized Sections "track", beige for regular tracks
				Color bgColor = isUnrecognizedTrack ? new Color( 255, 102, 102 ) : new Color( 245, 245, 220 );  
				label.setBackground( bgColor );	
			}
		}
		
		return label;
	}
}
