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
package fiji.plugin.trackmate.helper.spt.measure;

/**
 * This is a simple implementation of the Hungarian/Munkres-Kuhn algorithm for
 * the rectangulat assignment problem
 * 
 * @version February 3, 2012
 * 
 * @author Nicolas Chenouard
 *
 */

public class HungarianMatching
{

	private final double[][] costs;

	private boolean[][] starMat;

	private final boolean[][] primeMat;

	private final boolean[] columnCoverage;

	private final boolean[] rowCoverage;

	private final int numRows;

	private final int numColumns;

	private boolean feasibleAssociation;

	/**
	 * * Create the optimizer
	 * 
	 * @param costs
	 *            table of assignment costs. The number of lines has to be less
	 *            are equal to the number of columns (i.e., costs.length &lt;=
	 *            costs[0]. length == true).
	 */
	public HungarianMatching( final double[][] costs )
	{
		// subtract the smallest value of each row to the row
		this.costs = costs;
		numRows = costs.length;
		numColumns = costs[ 0 ].length;

		primeMat = new boolean[ numRows ][ numColumns ];
		starMat = new boolean[ numRows ][ numColumns ];
		rowCoverage = new boolean[ numRows ];
		columnCoverage = new boolean[ numColumns ];
		for ( int i = 0; i < numRows; i++ )
		{
			double minCost = costs[ i ][ 0 ];
			for ( int j = 1; j < numColumns; j++ )
				if ( costs[ i ][ j ] < minCost )
					minCost = costs[ i ][ j ];
			for ( int j = 0; j < numColumns; j++ )
				costs[ i ][ j ] -= minCost;
		}
		feasibleAssociation = false;
	}

	private int numStep;

	private int step4_row;

	private int step4_col;

	/**
	 * Build the optimal assignment
	 * 
	 * @return a table indicating in each row element is assigned to each column
	 *         element
	 */
	public boolean[][] optimize() throws Exception
	{
//		System.out.println( "Num rows: " + numRows );
//		System.out.println( "Num cols: " + numColumns );

		step1();
		numStep = 2;
		while ( !feasibleAssociation )
		{
			switch ( numStep )
			{
			case 2:
				step2();
				break;
			case 3:
				step3();
				break;
			case 4:
				step4( step4_row, step4_col );
				break;
			case 5:
				step5();
				break;
			}
		}

		return starMat;
	}

	private void step1() throws Exception
	{
//		System.out.println( "Step 1" );
		for ( int i = 0; i < numRows; i++ )
		{
			for ( int j = 0; j < numColumns; j++ )
			{
				if ( costs[ i ][ j ] == 0 )
				{
					if ( !columnCoverage[ j ] )
					{
						starMat[ i ][ j ] = true;
						columnCoverage[ j ] = true;
						break;
					}
				}
			}
		}
	}

	private void step2() throws Exception
	{
//		System.out.println( "Step 2" );
		int cntColumnCoverage = 0;
		for ( int j = 0; j < numColumns; j++ )
		{
			for ( int i = 0; i < numRows; i++ )
			{
				if ( starMat[ i ][ j ] )
				{
					columnCoverage[ j ] = true;
					cntColumnCoverage++;
					break;
				}
			}
		}
		feasibleAssociation = ( cntColumnCoverage == numRows );
		numStep = 3;
	}

	private void step3() throws Exception
	{
//		System.out.println( "Step 3" );
		boolean zerosFound = true;
		while ( zerosFound )
		{
			zerosFound = false;
			for ( int j = 0; j < numColumns; j++ )
			{
				if ( !columnCoverage[ j ] )
				{
					for ( int i = 0; i < numRows; i++ )
					{
						if ( ( !rowCoverage[ i ] ) && ( costs[ i ][ j ] == 0 ) )
						{
							primeMat[ i ][ j ] = true;
							boolean foundStarCol = false;
							for ( int j2 = 0; j2 < numColumns; j2++ )
							{
								if ( starMat[ i ][ j2 ] )
								{
									foundStarCol = true;
									columnCoverage[ j2 ] = false;
									break;
								}
							}
							if ( !foundStarCol )
							{
								step4_col = j;
								step4_row = i;
								numStep = 4;
								return;
							}
							else
							{
								rowCoverage[ i ] = true;
								zerosFound = true;
								break; // go to next column
							}
						}
					}
				}
			}
		}
		numStep = 5;
	}

	private void step4( final int row, final int col ) throws Exception
	{
//		System.out.println( "Step 4" );
		final boolean[][] starMat2 = new boolean[ numRows ][ numColumns ];
		for ( int i = 0; i < numRows; i++ )
			System.arraycopy( starMat[ i ], 0, starMat2[ i ], 0, numColumns );
		starMat2[ row ][ col ] = true;

		int starCol = col;
		int starRow = -1;
		for ( int i = 0; i < numRows; i++ )
		{
			if ( starMat[ i ][ starCol ] )
			{
				starRow = i;
				break; // there is only one starred zero per column
			}
		}
		while ( starRow >= 0 )
		{
			// unstar the starred zero
			starMat2[ starRow ][ starCol ] = false;
			// find a starred prime
			final int primeRow = starRow;
			int primeCol = -1;
			for ( int j = 0; j < numColumns; j++ )
			{
				if ( primeMat[ primeRow ][ j ] )
				{
					primeCol = j;
					break;
				}
			}
			// star the primed zero
			starMat2[ primeRow ][ primeCol ] = true;
			// find a starred zero in the column
			starCol = primeCol;
			starRow = -1;
			for ( int i = 0; i < numRows; i++ )
			{
				if ( starMat[ i ][ starCol ] )
				{
					starRow = i;
					break; // there is only one starred zero per column
				}
			}
		}
		// update star matrix
		starMat = starMat2;
		// clear prime matrix and coverred rows
		for ( int i = 0; i < numRows; i++ )
		{
			for ( int j = 0; j < numColumns; j++ )
			{
				primeMat[ i ][ j ] = false;
			}
			rowCoverage[ i ] = false;
		}
		numStep = 2;
	}

	private void step5() throws Exception
	{
//		System.out.println( "Step 5" );
		// find the smallest uncovered element
		double minUncoveredCost = Double.MAX_VALUE;
		for ( int j = 0; j < numColumns; j++ )
		{
			if ( !columnCoverage[ j ] )
				for ( int i = 0; i < numRows; i++ )
				{
					if ( !rowCoverage[ i ] )
					{
						if ( minUncoveredCost > costs[ i ][ j ] )
							minUncoveredCost = costs[ i ][ j ];
					}
				}
		}
		// add the min cost to each covered row
		for ( int i = 0; i < numRows; i++ )
			if ( rowCoverage[ i ] )
				for ( int j = 0; j < numColumns; j++ )
					costs[ i ][ j ] += minUncoveredCost;
		// subtract the min cost to each uncovered column
		for ( int j = 0; j < numColumns; j++ )
			if ( !columnCoverage[ j ] )
				for ( int i = 0; i < numRows; i++ )
					costs[ i ][ j ] -= minUncoveredCost;
		numStep = 3;
	}
}
