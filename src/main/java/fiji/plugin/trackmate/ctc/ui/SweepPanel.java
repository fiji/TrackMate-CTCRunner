package fiji.plugin.trackmate.ctc.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel.ModelListener;
import fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.BooleanRangeSweepPanel;
import fiji.plugin.trackmate.ctc.ui.components.EnumParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.EnumRangeSweepPanel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberRangeSweepPanel;
import fiji.plugin.trackmate.ctc.ui.components.StringRangeParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.StringRangeParamSweepPanel;
import fiji.plugin.trackmate.gui.Fonts;

public class SweepPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	public SweepPanel( final AbstractSweepModel< ? > model )
	{
		setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		setLayout( new BorderLayout() );

		// Top panel.
		final JPanel topPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		final JLabel lblInfo = new JLabel( " " );
		lblInfo.setFont( Fonts.SMALL_FONT );
		topPanel.add( lblInfo );
		add( topPanel, BorderLayout.NORTH );

		// Main panel.
		final JPanel mainPanel = new JPanel();
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 40, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		mainPanel.setLayout( gridBagLayout );

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTH;

		// Model components.
		for ( final AbstractParamSweepModel< ? > cm : model.models.values() )
		{
			final JPanel p = createPanelFor( cm );
			mainPanel.add( p, c );
			c.gridy++;

			mainPanel.add( new JSeparator(), c );
			c.gridy++;
		}

		final int n = c.gridy == 0 ? 1 : c.gridy;
		gridBagLayout.rowWeights = new double[ n ];
		gridBagLayout.rowWeights[ n - 1 ] = Double.MIN_VALUE;

		// Scroll pane.
		final JScrollPane scrollPane = new JScrollPane( mainPanel );
		scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
		scrollPane.getVerticalScrollBar().setUnitIncrement( 16 );

		add( scrollPane, BorderLayout.CENTER );

		final ModelListener infoListener = () -> {
			final int ns = model.generateSettings( new Settings(), 1 ).size();
			final String str = ( ns == 1 )
					? "Sweep over one setting for this detector."
					: String.format( "Sweep over %d different settings for this detector.", ns );
			lblInfo.setText( str );
		};
		model.listeners().add( infoListener );
		infoListener.modelChanged();
	}

	private static final JPanel createPanelFor( final AbstractParamSweepModel< ? > cm )
	{
		if ( cm instanceof BooleanParamSweepModel )
			return new BooleanRangeSweepPanel( ( BooleanParamSweepModel ) cm );
		else if ( cm instanceof NumberParamSweepModel )
			return new NumberRangeSweepPanel( ( NumberParamSweepModel ) cm );
		else if ( cm instanceof StringRangeParamSweepModel )
			return new StringRangeParamSweepPanel( ( StringRangeParamSweepModel ) cm );
		else if ( cm instanceof EnumParamSweepModel< ? > )
			return new EnumRangeSweepPanel<>( ( EnumParamSweepModel< ? > ) cm );
		else
			throw new IllegalArgumentException( "Do not know how to create a panel for model: " + cm );
	}
}