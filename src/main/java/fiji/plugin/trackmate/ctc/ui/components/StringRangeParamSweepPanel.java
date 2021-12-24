package fiji.plugin.trackmate.ctc.ui.components;

import static fiji.plugin.trackmate.gui.Fonts.FONT;
import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;
import static fiji.plugin.trackmate.gui.Icons.ADD_ICON;
import static fiji.plugin.trackmate.gui.Icons.REMOVE_ICON;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import com.itextpdf.text.Font;

import fiji.plugin.trackmate.util.EverythingDisablerAndReenabler;
import fiji.plugin.trackmate.util.FileChooser;
import fiji.plugin.trackmate.util.FileChooser.DialogType;
import fiji.plugin.trackmate.util.FileChooser.SelectionMode;

public class StringRangeParamSweepPanel extends JPanel
{

	private static final long serialVersionUID = -1L;

	private final List< StringPanel > stringPanels = new ArrayList<>();

	private final JPanel allStringPanels;

	private final StringRangeParamSweepModel values;

	private final EverythingDisablerAndReenabler enabler;

	private final boolean showBrowse;

	private final JPanel panelButton;

	/*
	 * CONSTRUCTOR
	 */

	public StringRangeParamSweepPanel( final StringRangeParamSweepModel val )
	{
		this.showBrowse = val.isFile();
		this.enabler = new EverythingDisablerAndReenabler( this, new Class[] { JLabel.class } );

		this.setLayout( new BorderLayout() );
		setPreferredSize( new Dimension( 270, 150 ) );

		final JPanel topPanel = new JPanel();
		final JLabel lblName = new JLabel( val.paramName );
		lblName.setFont( FONT.deriveFont( Font.BOLD ) );
		topPanel.add( lblName );
		add( topPanel, BorderLayout.NORTH );

		final JScrollPane scrollPane = new JScrollPane();
		this.add( scrollPane, BorderLayout.CENTER );
		scrollPane.setPreferredSize( new java.awt.Dimension( 250, 389 ) );
		scrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		scrollPane.setBorder( null );
		scrollPane.getVerticalScrollBar().setUnitIncrement( 16 );

		allStringPanels = new JPanel();
		final BoxLayout jPanelAllThresholdsLayout = new BoxLayout( allStringPanels, BoxLayout.Y_AXIS );
		allStringPanels.setLayout( jPanelAllThresholdsLayout );
		scrollPane.setViewportView( allStringPanels );

		final JButton btnAdd = new JButton();
		btnAdd.setIcon( ADD_ICON );
		btnAdd.setFont( SMALL_FONT );
		btnAdd.setPreferredSize( new java.awt.Dimension( 24, 24 ) );
		btnAdd.setSize( 24, 24 );
		btnAdd.setMinimumSize( new java.awt.Dimension( 24, 24 ) );
		panelButton = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		panelButton.add( btnAdd );

		/*
		 * Default values.
		 */

		for ( final String string : val.getRange() )
			addStringPanel( string );

		// We assign the field only now so that the lines above do not result in
		// adding the strings to the model a second time.
		this.values = val;

		/*
		 * Listeners & co.
		 */

		btnAdd.addActionListener( e -> addStringPanel() );
	}

	public void addStringPanel()
	{
		addStringPanel( System.getProperty( "user.home" ) );
	}

	public void addStringPanel( final String str )
	{
		final StringPanel panel = new StringPanel( str, showBrowse );
		addStringPanel( panel );
	}

	public void addStringPanel( final StringPanel panel )
	{
		allStringPanels.remove( panelButton );

		final Component strut = Box.createVerticalStrut( 5 );
		stringPanels.add( panel );
		allStringPanels.add( panel );
		allStringPanels.add( strut );
		allStringPanels.add( panelButton );

		if ( stringPanels.size() > 1 )
			stringPanels.forEach( sp -> sp.btnRemove.setVisible( true ) );

		panel.btnRemove.addActionListener( e -> removeStringPanel( panel, strut ) );
		if ( values != null )
			values.add( panel.tfStr.getText() );

		allStringPanels.revalidate();
	}

	/*
	 * PRIVATE METHODS
	 */

	private void removeStringPanel( final StringPanel stringPanel, final Component strut )
	{
		final int id = stringPanels.indexOf( stringPanel );
		values.remove( id );
		stringPanels.remove( stringPanel );

		if ( stringPanels.size() < 2 )
			stringPanels.forEach( sp -> sp.btnRemove.setVisible( false ) );
		allStringPanels.remove( strut );
		allStringPanels.remove( stringPanel );
		allStringPanels.revalidate();
		allStringPanels.repaint();
	}

	/*
	 * PRIVATE CLASS.
	 */

	private class StringPanel extends JPanel
	{

		private static final long serialVersionUID = 1L;

		private final JButton btnRemove;

		private final JTextField tfStr;

		public StringPanel( final String str, final boolean showBrowse )
		{
			setLayout( new BoxLayout( this, BoxLayout.X_AXIS ) );

			btnRemove = new JButton();
			add( btnRemove );
			btnRemove.setIcon( REMOVE_ICON );
			btnRemove.setFont( SMALL_FONT );
			btnRemove.setPreferredSize( new java.awt.Dimension( 24, 24 ) );
			btnRemove.setSize( 24, 24 );
			btnRemove.setMinimumSize( new java.awt.Dimension( 24, 24 ) );

			add( Box.createHorizontalStrut( 5 ) );
			tfStr = new JTextField( str );
			tfStr.setFont( SMALL_FONT );
			add( tfStr );

			if ( showBrowse )
			{
				add( Box.createHorizontalStrut( 5 ) );
				final JButton btnBrowse = new JButton( "Browse" );
				btnBrowse.setFont( SMALL_FONT );
				btnBrowse.addActionListener( e -> browse() );
				add( btnBrowse );
			}

			setMinimumSize( new java.awt.Dimension( 24, 24 ) );
			setPreferredSize( new java.awt.Dimension( 100, 24 ) );
			setMaximumSize( new java.awt.Dimension( 6000, 30 ) );
			setBorder( BorderFactory.createEmptyBorder( 2, 5, 2, 5 ) );

			// Listeners.
			fiji.plugin.trackmate.gui.GuiUtils.selectAllOnFocus( tfStr );

			final ActionListener al = e -> {
				final int id = stringPanels.indexOf( this );
				values.set( id, tfStr.getText() );
			};
			tfStr.addActionListener( al );
			final FocusAdapter fa = new FocusAdapter()
			{
				@Override
				public void focusLost( final java.awt.event.FocusEvent e )
				{
					al.actionPerformed( null );
				}
			};
			tfStr.addFocusListener( fa );
		}

		private void browse()
		{
			enabler.disable();
			try
			{
				final File file = FileChooser.chooseFile(
						this,
						tfStr.getText(),
						null,
						"Browse to a file",
						DialogType.LOAD,
						SelectionMode.FILES_AND_DIRECTORIES );
				if ( file != null )
					tfStr.setText( file.getAbsolutePath() );
			}
			finally
			{
				enabler.reenable();
			}
		}
	}

	public static void main( final String[] args )
	{
		final StringRangeParamSweepModel model = new StringRangeParamSweepModel()
				.paramName( "Test string list" )
				.isFile( true )
				.add( "Test 1" )
				.add( "Rather long string that should be able to show, maybe, how the UI behaves against such long strings. That's it." )
				.add( "With a ut8 char: €μ" );
		model.listeners().add( () -> System.out.println( model.toString() ) );
		
		final StringRangeParamSweepPanel panel = new StringRangeParamSweepPanel( model );
		final JFrame frame = new JFrame();
		frame.getContentPane().add( panel );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}
}