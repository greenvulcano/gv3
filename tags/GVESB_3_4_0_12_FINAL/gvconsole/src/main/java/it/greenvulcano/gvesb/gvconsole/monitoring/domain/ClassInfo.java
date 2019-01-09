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
 * @version 3.0.0 Apr 6, 2010
 * @author Angelo
 *
 */
public class ClassInfo {

	private long classesLoad;
	private long classesUnload;
	private long classesTotal;

	/**
	 * @return the classesLoad
	 */
	public long getClassesLoad() {
		return classesLoad;
	}

	/**
	 * @param classesLoad
	 *            the classesLoad to set
	 */
	public void setClassesLoad(long classesLoad) {
		this.classesLoad = classesLoad;
	}

	/**
	 * @return the classesUnlad
	 */
	public long getClassesUnload() {
		return classesUnload;
	}

	/**
	 * @param classesUnload
	 *            the classesUnlad to set
	 */
	public void setClassesUnload(long classesUnload) {
		this.classesUnload = classesUnload;
	}

	/**
	 * @return the classesTotal
	 */
	public long getClassesTotal() {
		return classesTotal;
	}

	/**
	 * @param classesTotal
	 *            the classesTotal to set
	 */
	public void setClassesTotal(long classesTotal) {
		this.classesTotal = classesTotal;
	}

}
