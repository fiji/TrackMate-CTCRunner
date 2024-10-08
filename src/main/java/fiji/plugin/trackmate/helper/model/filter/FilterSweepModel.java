package fiji.plugin.trackmate.helper.model.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings.TrackMateObject;
import fiji.plugin.trackmate.helper.model.AbstractSweepModelBase;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.ArrayParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.BooleanParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.Combinations;
import fiji.plugin.trackmate.helper.model.parameter.DoubleParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.NumberParamSweepModel;

public class FilterSweepModel extends AbstractSweepModelBase
{

	protected static final String FEATURE = "FEATURE";

	protected static final String VALUE = "VALUE";

	protected static final String ISABOVE = "ISABOVE";

	private final TrackMateObject target;

	public FilterSweepModel( final TrackMateObject target, final Map< String, String > featureNames )
	{
		super( "Filter on " + target.toString(), createModels( featureNames ) );
		this.target = target;
	}

	@Override
	public Iterator< Settings > iterator( final Settings base, final int targetChannel )
	{
		return new MySettingsIterator( base );
	}

	private static Map< String, AbstractParamSweepModel< ? > > createModels( final Map< String, String > featureNames )
	{
		final String[] arr = new ArrayList<>( featureNames.values() ).toArray( new String[] {} );
		final ArrayParamSweepModel< String > featureNameParam = new ArrayParamSweepModel<>( arr )
				.paramName( "Feature" )
				.fixedValue( arr[ 0 ] )
				.rangeType( ArrayParamSweepModel.RangeType.FIXED );

		final DoubleParamSweepModel thresholdParam = new DoubleParamSweepModel()
				.paramName( "Threshold" )
				.dimension( Dimension.NONE )
				.rangeType( NumberParamSweepModel.RangeType.FIXED )
				.min( 5. );

		final BooleanParamSweepModel isAboveParam = new BooleanParamSweepModel()
				.paramName( "Above?" )
				.fixedValue( true )
				.rangeType( BooleanParamSweepModel.RangeType.FIXED );

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( FEATURE, featureNameParam );
		models.put( VALUE, thresholdParam );
		models.put( ISABOVE, isAboveParam );
		return models;
	}
	
	private final class MySettingsIterator implements Iterator< Settings >
	{

		private final Settings base;

		private final FeatureFilterIterator it;

		public MySettingsIterator( final Settings base )
		{
			this.base = base;
			this.it = new FeatureFilterIterator( models );
		}

		@Override
		public boolean hasNext()
		{
			return it.hasNext();
		}

		@Override
		public Settings next()
		{
			final FeatureFilter ff = it.next();
			final Settings copy = base.copyOn( base.imp );
			switch ( target )
			{
			case SPOTS:
				copy.addSpotFilter( ff );
				break;
			case TRACKS:
				copy.addTrackFilter( ff );
				break;
			default:
				throw new IllegalArgumentException( "Cannot create filter for TrackMate object: " + target );
			}
			return copy;
		}
	}

	private static class FeatureFilterIterator implements Iterator< FeatureFilter >
	{

		private final Combinations combinations;

		public FeatureFilterIterator( final Map< String, AbstractParamSweepModel< ? > > models )
		{
			final Map< String, List< Object > > values = new LinkedHashMap<>( models.size() );
			for ( final String key : models.keySet() )
			{
				@SuppressWarnings( "unchecked" )
				final List< Object > range = ( List< Object > ) models.get( key ).getRange();
				values.put( key, range );
			}
			this.combinations = new Combinations( values );
		}

		@Override
		public boolean hasNext()
		{
			return combinations.hasNext();
		}

		@Override
		public FeatureFilter next()
		{
			final Map< String, Object > params = combinations.next();
			final String feature = ( String ) params.get( FEATURE );
			final Double value = ( Double ) params.get( VALUE );
			final Boolean isAbove = ( Boolean ) params.get( ISABOVE );
			return new FeatureFilter( feature, value, isAbove );
		}
	}
}