/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2024 TrackMate developers.
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
package fiji.plugin.trackmate.helper;

import javax.swing.SwingUtilities;

import fiji.plugin.trackmate.helper.ui.MetricsLauncherController;
import ij.plugin.PlugIn;
import net.imagej.ImageJ;

public class TrackMateComputeMetricsPlugin implements PlugIn
{

	@Override
	public void run( final String arg )
	{
		SwingUtilities.invokeLater( () -> new MetricsLauncherController() );
	}

	public static void main( final String[] args )
	{
		final ImageJ ij = new ImageJ();
		ij.launch( args );
		new TrackMateComputeMetricsPlugin().run( null );
	}
}
