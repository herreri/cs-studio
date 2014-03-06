/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser2.model;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.archive.vtype.VTypeHelper;
import org.csstudio.swt.xygraph.dataprovider.IDataProviderListener;
import org.csstudio.swt.xygraph.linearscale.Range;
import org.csstudio.trends.databrowser2.Messages;
import org.epics.util.time.Timestamp;
import org.epics.vtype.AlarmSeverity;
import org.epics.vtype.VType;
import org.epics.vtype.ValueUtil;

/** Samples of a {@link PVItem}.
 *  <p>
 *  Made up of two sections,
 *  {@link HistoricSamples} and {@link LiveSamples},
 *  and presenting them as one long stream of samples.
 *
 *  In addition, if the last sample is valid, it's
 *  extended to 'now' assuming no new data means
 *  that the last value is still valid.
 *
 *  @author Kay Kasemir
 *  @author Takashi Nakamoto changed PVSamples to handle waveform index.
 */
public class PVSamples extends PlotSamples
{
    /** Historic samples */
    final private HistoricSamples history = new HistoricSamples();

    /** Live samples. Should start after end of historic samples */
    final private LiveSamples live = new LiveSamples();

    private ArrayList<IDataProviderListener> listeners = new ArrayList<IDataProviderListener>();
    
    private boolean emptyHistoryOnAdd = false;
    private int samplesAddedSinceLastRefresh = 0;

    /** {@inheritDoc} */
    @Override
    public void addDataProviderListener(IDataProviderListener listener)
    {
    	synchronized (listeners)
    	{
    		listeners.add(listener);
    	}
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeDataProviderListener(IDataProviderListener listener)
    {
        synchronized (listeners)
        {
        	return listeners.remove(listener);
        }
    }

    /** @param index Waveform index to show */
    public void setWaveformIndex(int index)
    {
    	live.setWaveformIndex(index);
    	history.setWaveformIndex(index);

    	synchronized (listeners)
    	{
    		for (IDataProviderListener listener : listeners)
    		{
    			// Notify listeners of the change of the waveform index
    			// mainly in order to update the position of snapped
    			// annotations. For more details, see the comment in
    			// Annotation.dataChanged(IDataProviderListener).
    			listener.dataChanged(this);
    		}
    	}
    }

    /** @return Maximum number of live samples in ring buffer */
    public int getLiveCapacity()
    {
        return live.getCapacity();
    }

    /** Set new capacity for live sample ring buffer
     *  <p>
     *  @param new_capacity New sample count capacity
     *  @throws Exception on out-of-memory error
     */
    public void setLiveCapacity(final int new_capacity) throws Exception
    {
        live.setCapacity(new_capacity);
    }

    /** @return Combined count of historic and live samples */
    @Override
    synchronized public int getSize()
    {
        final int raw = getRawSize();
        if (raw <= 0)
            return raw;
        final PlotSample last = getSample(raw-1);
        if (VTypeHelper.getSeverity(last.getValue()) == AlarmSeverity.UNDEFINED)
            return raw;
        // Last sample is valid, so it should still apply 'now'
        return raw+1;
    }

    /** @return Size of the actual historic and live samples
     *          without the continuation to 'now'
     */
    private int getRawSize()
    {
        return history.getSize() + live.getSize();
    }

    /** @param index 0... getSize()-1
     *  @return Sample from historic or live sample subsection
     */
    @Override
    synchronized public PlotSample getSample(final int index)
    {
        final int raw_count = getRawSize();
        if (index < raw_count)
            return getRawSample(index);
        // Last sample is valid, so it should still apply 'now'
        final PlotSample sample = getRawSample(raw_count-1);
        if (Timestamp.now().compareTo(sample.getTime()) < 0) {
        	return sample;
        } else {
        	return new PlotSample(sample.getSource(), VTypeHelper.transformTimestampToNow(sample.getValue()));
        }
    }

    /** Get 'raw' sample, no continuation until 'now'
     *  @param index 0... getRawSize()-1
     *  @return Sample from historic or live sample subsection
     */
    private PlotSample getRawSample(final int index)
    {
        final int num_old = history.getSize();
        if (index < num_old)
            return history.getSample(index);
        return live.getSample(index - num_old);
    }

    /** @return Overall time (x axis) range of historic and live samples */
    @Override
    synchronized public Range getXDataMinMax()
    {
        final Range old_range = history.getXDataMinMax();
        final Range new_range = live.getXDataMinMax();
        if (old_range == null)
            return new_range;
        if (new_range == null)
            return old_range;
        // Both are not-null
        return new Range(old_range.getLower(), new_range.getUpper());
    }

    /** @return Overall value (y axis) range of historic and live samples */
    @Override
    synchronized public Range getYDataMinMax()
    {
        final Range old_range = history.getYDataMinMax();
        final Range new_range = live.getYDataMinMax();
        if (old_range == null)
            return new_range;
        if (new_range == null)
            return old_range;
        // Both are not-null
        final double min = Math.min(old_range.getLower(), new_range.getLower());
        final double max = Math.max(old_range.getUpper(), new_range.getUpper());
        return new Range(min, max);
    }

    /** Test if samples changed since the last time
     *  <code>testAndClearNewSamplesFlag</code> was called.
     *  @return <code>true</code> if there were new samples
     */
    @Override
    synchronized public boolean hasNewSamples()
    {
        return history.hasNewSamples() | live.hasNewSamples();
    }

    /** Test if samples changed since the last time this method was called.
     *  @return <code>true</code> if there were new samples
     */
    @Override
    synchronized public boolean testAndClearNewSamplesFlag()
    {
        // Must check & __clear__ both subsections!
        // return hist.test() | live.test() would skip
        // the live.test if hist.test() was already true!
        final boolean hist_change = history.testAndClearNewSamplesFlag();
        final boolean live_change = live.testAndClearNewSamplesFlag();
        return hist_change | live_change;
    }

    /** Add data retrieved from an archive to the 'historic' section
     *  @param source Source of the samples
     *  @param result Historic data
     */
    synchronized public void mergeArchivedData(final String source,
            final List<VType> result)
    {
    	if (emptyHistoryOnAdd) {
    		emptyHistoryOnAdd = false;
    		history.clear();
    	}
        history.mergeArchivedData(source, result);
    }

    /** Add another 'live' sample
     *  @param value 'Live' sample
     */
    synchronized public void addLiveSample(VType value)
    {
        if (! ValueUtil.timeOf(value).isTimeValid())
            value = VTypeHelper.transformTimestampToNow(value);
        addLiveSample(new PlotSample(Messages.LiveData, value));
    }

    /** Add another 'live' sample
     *  @param value 'Live' sample
     */
    synchronized public void addLiveSample(final PlotSample sample)
    {
        live.add(sample);
        // History ends before the start of 'live' samples.
        // Adding a live sample might have moved the ring buffer,
        // so need to update whenever live data is extended.
        history.setBorderTime(live.getSample(0).getTime());
        samplesAddedSinceLastRefresh++;
    }

    /** Delete all samples */
    synchronized public void clear()
    {
        history.clear();
        live.clear();
    }
    
    /**
     * Check if the current data matches the criteria to refresh the history data.
     * History data is refreshed when the live buffer is full and there exists a gap
     * between the last history data and the current live data. This method will
     * also trigger purge of all currnetly cached history data of this sample.
     * 
     * This method can only ever be called from the PVItem.
     * 
     * @param startTime the start time of the current visible window on the chart
     * @param endTime the end time of the current visible window on the chart
     * @return true if the history data needs to be refreshed or false otherwise
     */
   synchronized boolean isHistoryRefreshNeeded(Timestamp startTime, Timestamp endTime) {
    	//if live data hasn't reached capacity, do not refresh anything
    	if (live.getSize() < live.getCapacity() || live.getSize() == 0) return false;
    	//if there is no history data, there is nothing to refresh anyway
    	if (history.getRawSize() == 0) return false;
    	PlotSample first = live.getSample(0);
    	//if the first time in the live data is smaller than the visible start time,
    	//the buffer is large enough to contain all the "currently" visible data
    	if (first.getTime().compareTo(startTime) <= 0) return false;
    	PlotSample last = live.getSample(live.getSize()-1);
    	//if the las sample is greater than the current end time than we are not
    	//looking at the live data
    	if (last.getTime().compareTo(endTime) > 0) return false;
    	PlotSample historyLast = history.getRawSample(history.getRawSize()-1);
    	//if the last raw history data is smaller than the first live sample, do refresh
    	if (historyLast.getTime().compareTo(first.getTime()) < 0) {
    		samplesAddedSinceLastRefresh = 0;
    		emptyHistoryOnAdd = true;
    		return true;
    	}
    	//maybe we are looking at live data with a window extending into the future:
    	//in such case check the number of samples that arrived since the previous refresh
    	if (samplesAddedSinceLastRefresh > live.getCapacity()) {
    		samplesAddedSinceLastRefresh = 0;
    		emptyHistoryOnAdd = true;
    		return true;
    	}
    	return false;
    }

    /** @return (Long) string representation for debugging */
    @SuppressWarnings("nls")
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append("PV Samples\nHistory: ");
        buf.append(history.toString());
        buf.append("\nLive Buffer: ");
        buf.append(live.toString());

        final int count = getSize();
        if (count != getRawSize())
        {
            buf.append("\nContinuation to 'now':\n");
            buf.append("     " + getSample(count-1));
        }
        return buf.toString();
    }
}
