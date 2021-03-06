/*
 * Copyright 1998-2015 John Caron and University Corporation for Atmospheric Research/Unidata
 *
 *  Portions of this software were developed by the Unidata Program at the
 *  University Corporation for Atmospheric Research.
 *
 *  Access and use of this software shall impose the following obligations
 *  and understandings on the user. The user is granted the right, without
 *  any fee or cost, to use, copy, modify, alter, enhance and distribute
 *  this software, and any derivative works thereof, and its supporting
 *  documentation for any purpose whatsoever, provided that this entire
 *  notice appears in all copies of the software, derivative works and
 *  supporting documentation.  Further, UCAR requests that the user credit
 *  UCAR/Unidata in any publications that result from the use of this
 *  software or in any product that includes this software. The names UCAR
 *  and/or Unidata, however, may not be used in any advertising or publicity
 *  to endorse or promote any products or commercial entity unless specific
 *  written permission is obtained from UCAR/Unidata. The user also
 *  understands that UCAR/Unidata is not obligated to provide the user with
 *  any support, consulting, training or assistance of any kind with regard
 *  to the use, operation and performance of this software nor to provide
 *  the user with any updates, revisions, new versions or "bug fixes."
 *
 *  THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 *  INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 *  FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *  NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 *  WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package ucar.nc2.ft;

import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;

import ucar.ma2.StructureData;
import ucar.nc2.ft.point.StationFeature;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;

/**
 * Time series of ProfileFeature at named locations.
 * @author caron
 * @since Feb 29, 2008
 */
public interface StationProfileFeature extends StationFeature, PointFeatureCC, Iterable<ProfileFeature> {

  /**
   * The number of profiles in the time series. May not be known until after iterating through the collection.
   * @return number of profiles in the time series, or -1 if not known.
   */
  int size();

  /**
   * Subset this collection by dateRange
   * @param dateRange only points in this date range. may be null.
   * @return subsetted collection, may be null if empty
   * @throws java.io.IOException on read error
   */
  StationProfileFeature subset(CalendarDateRange dateRange) throws IOException;


  /**
   * Get the list of times in the time series of profiles. Note that this may be as costly as iterating over the collection.
   * @return list of times in the time series of profiles.
   * @throws java.io.IOException on read error
   */
  List<CalendarDate> getTimes() throws IOException;

  /**
   * Get a particular profile by date. Note that this may be as costly as iterating over the collection.
   * @param date get profile matching this date.
   * @return profile whose date matches the given date
   * @throws java.io.IOException on read error
   */
  ProfileFeature getProfileByDate(CalendarDate date) throws IOException;

  /**
   * The data associated with the StationProfile feature.
   * @return the actual data of this section. may be empty, not null.
   * @throws java.io.IOException on i/o error
   */
  @Nonnull
  StructureData getFeatureData() throws IOException;

  ////////////////////////////////////////////////////////////////////////////

  /**
   * Use the internal iterator to check if there is another ProfileFeature in the iteration.
   * @return true is there is another ProfileFeature in the iteration.
   * @throws java.io.IOException on read error
   * @deprecated use foreach
   */
  boolean hasNext() throws java.io.IOException;

  /**
   * Use the internal iterator to get the next ProfileFeature in the iteration.
   * You must call hasNext() before you call this.
   * @return the next ProfileFeature in the iteration
   * @throws java.io.IOException on read error
   * @deprecated use foreach
   */
  ProfileFeature next() throws java.io.IOException;

  /**
   * Reset the internal iterator for another iteration over the ProfileFeature in this Collection.
   * @throws java.io.IOException on read error
   * @deprecated use foreach
   */
  void resetIteration() throws IOException;

  /**
   * @deprecated use foreach
   */
  PointFeatureCollectionIterator getPointFeatureCollectionIterator() throws java.io.IOException;


}
