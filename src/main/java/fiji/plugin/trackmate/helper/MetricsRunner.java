package fiji.plugin.trackmate.helper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.function.BiFunction;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.TrackModel;
import fiji.plugin.trackmate.helper.ctc.CTCMetrics;
import fiji.plugin.trackmate.util.TMUtils;
import net.imglib2.util.ValuePair;

public abstract class MetricsRunner
{

	/**
	 * Logger to supervise the batch.
	 */
	protected Logger batchLogger = Logger.DEFAULT_LOGGER;

	/**
	 * Logger to pass to TrackMate instances.
	 */
	protected Logger trackmateLogger = Logger.VOID_LOGGER;

	/**
	 * Where to save metrics results files.
	 */
	protected final Path resultsRootPath;

	/**
	 * Generator for CSV file names.
	 */
	private final BiFunction< String, Integer, String > nameGenWithID;

	public MetricsRunner( final Path resultsRootPath, final String resultSuffix )
	{
		this.resultsRootPath = resultsRootPath;
		this.nameGenWithID = ( imName, i ) -> String.format( "%s_" + resultSuffix + "_%02d.csv", imName, i );
	}

	public abstract void performMetricsMeasurements( TrackMate trackmate, double detectionTiming, double trackingTiming );

	public ValuePair< TrackMate, Double > execDetection( final Settings settings )
	{
		batchLogger.log( "Executing detection.\n" );
		batchLogger.log( "Configured detector: " );
		batchLogger.log( settings.detectorFactory.getName(), Logger.BLUE_COLOR );
		batchLogger.log( " with settings:\n" );
		batchLogger.log( TMUtils.echoMap( settings.detectorSettings, 2 ) );
	
		final long start = System.currentTimeMillis();
		final TrackMate trackmate = new TrackMate( settings );
		trackmate.getModel().setLogger( trackmateLogger );
		if ( !trackmate.execDetection()
				|| !trackmate.execInitialSpotFiltering()
				|| !trackmate.computeSpotFeatures( true )
				|| !trackmate.execSpotFiltering( true ) )
		{
			batchLogger.error( "Error in the detection step:\n" + trackmate.getErrorMessage() );
			return null;
		}
		final long end = System.currentTimeMillis();
		final double detectionTiming = ( end - start ) / 1000.;
	
		batchLogger.log( String.format( "Detection done in %.1f s.\n", ( end - start ) / 1e3f ) );
		batchLogger.log( String.format( "Found %d visible spots over %d in total.\n",
				trackmate.getModel().getSpots().getNSpots( true ),
				trackmate.getModel().getSpots().getNSpots( false ) ) );
	
		return new ValuePair<>( trackmate, detectionTiming );
	}

	public double execTracking( final TrackMate trackmate )
	{
		batchLogger.log( "Executing tracking.\n" );
		batchLogger.log( "Configured detector: " );
		batchLogger.log( trackmate.getSettings().detectorFactory.getName(), Logger.BLUE_COLOR );
		batchLogger.log( " with settings:\n" );
		batchLogger.log( TMUtils.echoMap( trackmate.getSettings().detectorSettings, 2 ) );
		batchLogger.log( "Configured tracker: " );
		batchLogger.log( trackmate.getSettings().trackerFactory.getName(), Logger.BLUE_COLOR );
		batchLogger.log( " with settings:\n" );
		batchLogger.log( TMUtils.echoMap( trackmate.getSettings().trackerSettings, 2 ) );
	
		final long start = System.currentTimeMillis();
		if ( !trackmate.checkInput()
				|| !trackmate.execTracking()
				|| !trackmate.computeEdgeFeatures( true )
				|| !trackmate.computeTrackFeatures( true )
				|| !trackmate.execTrackFiltering( true ) )
		{
			System.err.println( "Error in tracking step:\n" + trackmate.getErrorMessage() );
			return Double.NaN;
		}
		final long end = System.currentTimeMillis();
		final double trackingTiming = ( end - start ) / 1000.;
	
		batchLogger.log( String.format( "Tracking done in %.1f s.\n", trackingTiming ) );
		final TrackModel trackModel = trackmate.getModel().getTrackModel();
		final IntSummaryStatistics stats = trackModel.unsortedTrackIDs( true ).stream()
				.mapToInt( id -> trackModel.trackSpots( id ).size() )
				.summaryStatistics();
		batchLogger.log( "Found " + trackModel.nTracks( true ) + " visible tracks over "
				+ trackModel.nTracks( false ) + " in total.\n" );
		batchLogger.log( String.format( "  - avg size: %.1f spots.\n", stats.getAverage() ) );
		batchLogger.log( String.format( "  - min size: %d spots.\n", stats.getMin() ) );
		batchLogger.log( String.format( "  - max size: %d spots.\n", stats.getMax() ) );
	
		return trackingTiming;
	}

	protected File findSuitableCSVFile( final Settings settings )
	{
		final String imFileName = settings.imp.getShortTitle();
		// Prepare CSV headers.
		final String[] csvHeader1 = toCSVHeader( settings );
		final String[] csvHeader = CTCMetrics.concatWithCSVHeader( csvHeader1 );
	
		// Init.
		int i = 0;
	
		while ( i < 100 )
		{
			i++;
			final File csvFile = getCSVFile( resultsRootPath.toString(), imFileName, i );
	
			// Does the target CSV file exist?
			if ( !csvFile.exists() )
			{
				try (CSVWriter csvWriter = new CSVWriter( new FileWriter( csvFile ),
						CSVWriter.DEFAULT_SEPARATOR,
						CSVWriter.NO_QUOTE_CHARACTER,
						CSVWriter.DEFAULT_ESCAPE_CHARACTER,
						CSVWriter.DEFAULT_LINE_END ))
				{
					// CSV header.
					csvWriter.writeNext( csvHeader );
				}
				catch ( final IOException e )
				{
					batchLogger.error( "Cannot open CSV file " + csvFile + " for writing:\n" + e.getMessage() );
					e.printStackTrace();
				}
				batchLogger.log( "CSV file " + csvFile + " does not exist. Created it.\n" );
				return csvFile;
			}
	
			// If yes, is it compatible for appending?
			if ( csvFileIsCompatible( settings, csvFile ) )
			{
				batchLogger.log( "Found a compatible CSV file for appending: " + csvFile + '\n' );
				return csvFile;
			}
		}
	
		batchLogger.error( "Could not create a proper CSV file in : " + resultsRootPath + '\n' );
		return null;
	}

	private final boolean csvFileIsCompatible( final Settings settings, final File csvFile )
	{
		// Prepare CSV headers.
		final String[] csvHeader1 = toCSVHeader( settings );
		final String[] csvHeader = CTCMetrics.concatWithCSVHeader( csvHeader1 );
	
		try (CSVReader csvReader = new CSVReaderBuilder( new FileReader( csvFile ) ).build())
		{
			final String[] readHeader = csvReader.readNext();
			return Arrays.equals( csvHeader, readHeader );
		}
		catch ( final IOException | CsvValidationException e )
		{
			batchLogger.error( "Cannot open CSV file " + csvFile + " for reading:\n" + e.getMessage() );
			e.printStackTrace();
		}
		return false;
	}

	public void setBatchLogger( final Logger batchLogger )
	{
		this.batchLogger = batchLogger;
	}

	public void setTrackmateLogger( final Logger trackmateLogger )
	{
		this.trackmateLogger = trackmateLogger;
	}

	private final File getCSVFile( final String resultsRootPath, final String imageName, final int id )
	{
		final Path csvFilePath = Paths.get( resultsRootPath, nameGenWithID.apply( imageName, id ) );
		return csvFilePath.toFile();
	}

	protected static final String[] toCSVHeader( final Settings settings )
	{
		final int nDetectorParams = settings.detectorSettings.size();
		final int nTrackerParams = settings.trackerSettings.size();
		final int nCols = 1 + nDetectorParams + 1 + nTrackerParams;
		final String[] out = new String[ nCols ];

		int i = 0;
		out[ i++ ] = "DETECTOR";
		for ( final String key : settings.detectorSettings.keySet() )
			out[ i++ ] = key;
		out[ i++ ] = "TRACKER";
		for ( final String key : settings.trackerSettings.keySet() )
			out[ i++ ] = key;
		return out;
	}

	protected static final String[] toCSVLine( final Settings settings, final String[] csvHeader )
	{
		final int nDetectorParams = settings.detectorSettings.size();
		final int nTrackerParams = settings.trackerSettings.size();
		final int nCols = 1 + nDetectorParams + 1 + nTrackerParams;
		final String[] out = new String[ nCols ];

		int i = 0;
		out[ i++ ] = settings.detectorFactory.getKey();
		for ( int j = 0; j < nDetectorParams; j++ )
		{
			out[ i ] = settings.detectorSettings.get( csvHeader[ i ] ).toString();
			i++;
		}
		out[ i++ ] = settings.trackerFactory.getKey();
		for ( int j = 0; j < nTrackerParams; j++ )
		{
			out[ i ] = settings.trackerSettings.get( csvHeader[ i ] ).toString();
			i++;
		}
		return out;
	}

}