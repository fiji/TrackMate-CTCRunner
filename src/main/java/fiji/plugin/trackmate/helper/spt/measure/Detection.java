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
/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package fiji.plugin.trackmate.helper.spt.measure;

import java.awt.Color;

/**
 * Detection is the basic detection class. Extends Detection to create more
 * complete Detection.
 * 
 * @author Fabrice de Chaumont
 */

public class Detection
{

	@Override
	public Object clone() throws CloneNotSupportedException
	{

		final Detection clone = ( Detection ) super.clone();

		clone.x = x;
		clone.y = y;
		clone.z = z;
		clone.t = t;
		clone.detectionType = detectionType;
		clone.selected = selected;
		clone.enabled = enabled;
		clone.color = new Color( color.getRed(), color.getGreen(), color.getBlue() );
		clone.originalColor = new Color( originalColor.getRGB() );

		return clone;
	}

	/** x position of detection. */
	protected double x;

	/** y position of detection. */
	protected double y;

	/** z position of detection. */
	protected double z;

	/** t position of detection. */
	protected int t;

	/** default detection type */
	protected int detectionType = DETECTIONTYPE_REAL_DETECTION;

	/** Selected */
	protected boolean selected = false;

	/**
	 * Detection enabled/disable is the internal mechanism to filter track with
	 * TrackProcessor. At the start of TrackProcessor process, the enable track
	 * are all set to true. any TSP can then disable it.
	 */
	protected boolean enabled = true;

	/**
	 * This color is used each time the TrackProcessor start, as it call the
	 * detection.reset() function. This color is loaded when using an XML file.
	 * While saving, the current color of the track ( color propertie ) is used.
	 * So at load it will become the new originalColor.
	 */
	protected Color originalColor = Color.blue;

	public final static int DETECTIONTYPE_REAL_DETECTION = 1;

	public final static int DETECTIONTYPE_VIRTUAL_DETECTION = 2;

	public Detection( final double x, final double y, final double z, final int t )
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.t = t;
		reset();
	}

	public Detection()
	{
		reset();
	}

	public int getT()
	{
		return t;
	}

	public void setT( final int t )
	{
		this.t = t;
	}

	public double getX()
	{
		return x;
	}

	public void setX( final double x )
	{
		this.x = x;
	}

	public double getY()
	{
		return y;
	}

	public void setY( final double y )
	{
		this.y = y;
	}

	public double getZ()
	{
		return z;
	}

	public void setZ( final double z )
	{
		this.z = z;
	}

	@Override
	public String toString()
	{
		return "Detection [x:" + x + " y:" + y + " z:" + z + " t:" + t + "]";
	}

	Color color;

	public int getDetectionType()
	{
		return detectionType;
	}

	public void setDetectionType( final int detectionType )
	{
		this.detectionType = detectionType;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected( final boolean selected )
	{
		this.selected = selected;
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor( final Color color )
	{
		this.color = color;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled( final boolean enabled )
	{
		this.enabled = enabled;
	}

	public void reset()
	{
		this.color = originalColor;
		this.setEnabled( true );
	}
}
