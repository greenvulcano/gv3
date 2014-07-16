/*
 * Copyright (c) 2009-2010 GreenVulcano ESB Open Source Project. All rights
 * reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 */
package it.greenvulcano.gvesb.gvconsole.monitoring.domain;

/**
 * @version 3.0.0 Apr 12, 2010
 * @author Angelo
 *
 */
public class ThreadInfo {

	private int liveThread;
	private int peakThread;
	private int totalThread;

	/**
	 * @return the liveThread
	 */
	public int getLiveThread() {
		return liveThread;
	}

	/**
	 * @param liveThread
	 *            the liveThread to set
	 */
	public void setLiveThread(int liveThread) {
		this.liveThread = liveThread;
	}

	/**
	 * @return the peakThread
	 */
	public int getPeakThread() {
		return peakThread;
	}

	/**
	 * @param peakThread
	 *            the peakThread to set
	 */
	public void setPeakThread(int peakThread) {
		this.peakThread = peakThread;
	}

	/**
	 * @return the totalThread
	 */
	public int getTotalThread() {
		return totalThread;
	}

	/**
	 * @param totalThread
	 *            the totalThread to set
	 */
	public void setTotalThread(int totalThread) {
		this.totalThread = totalThread;
	}

}
