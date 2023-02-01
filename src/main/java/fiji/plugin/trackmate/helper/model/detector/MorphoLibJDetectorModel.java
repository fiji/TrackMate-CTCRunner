/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2023 TrackMate developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package fiji.plugin.trackmate.helper.model.detector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.InfoParamSweepModel;
import fiji.plugin.trackmate.providers.DetectorProvider;

@Plugin( type = DetectorSweepModel.class, priority = 1000000 - 7 )
public class MorphoLibJDetectorModel extends DetectorSweepModel
{

	public MorphoLibJDetectorModel()
	{
		super( "MorphoLibJ detector", createModels(), createFactory() );
	}

	@Override
	public Iterator< Settings > iterator( final Settings base, final int targetChannel )
	{
		if ( null == new DetectorProvider().getFactory( "MORPHOLIBJ_DETECTOR" ) )
			return Collections.emptyIterator();
		else
			return MorphoLibJOpt.iterator( models, base, targetChannel );
	}

	private static SpotDetectorFactoryBase< ? > createFactory()
	{
		if ( null == new DetectorProvider().getFactory( "MORPHOLIBJ_DETECTOR" ) )
			return null;
		else
			return MorphoLibJOpt.createFactory();
	}

	private static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		if ( null == new DetectorProvider().getFactory( "MORPHOLIBJ_DETECTOR" ) )
		{
			final Map< String, AbstractParamSweepModel< ? > > models = new HashMap<>();
			models.put( "", new InfoParamSweepModel()
					.info( "The TrackMate-MorphoLibJ module seems to be missing<br>"
							+ "from your Fiji installation. Please follow the link<br>"
							+ "below for installation instructions." )
					.url( "https://imagej.net/plugins/trackmate/trackmate-morpholibj" ) );
			return models;
		}
		else
		{
			return MorphoLibJOpt.createModels();
		}
	}
}
