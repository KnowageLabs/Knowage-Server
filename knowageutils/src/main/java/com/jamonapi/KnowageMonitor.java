/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jamonapi;

import java.util.Date;

/**
 * Decorate {@link Monitor} with new functionalities.
 *
 * @author Marco Libanori
 */
public class KnowageMonitor extends Monitor implements AutoCloseable {

	private static final long serialVersionUID = -581630377501687248L;

	private final Monitor monitor;

	public KnowageMonitor(Monitor monitor) {
		super();
		this.monitor = monitor;
	}

	@Override
	public MonKey getMonKey() {
		return monitor.getMonKey();
	}

	@Override
	public String getLabel() {
		return monitor.getLabel();
	}

	@Override
	public String getUnits() {
		return monitor.getUnits();
	}

	@Override
	public int hashCode() {
		return monitor.hashCode();
	}

	@Override
	public void setAccessStats(long now) {
		monitor.setAccessStats(now);
	}

	@Override
	public void reset() {
		monitor.reset();
	}

	@Override
	public double getTotal() {
		return monitor.getTotal();
	}

	@Override
	public void setTotal(double value) {
		monitor.setTotal(value);
	}

	@Override
	public double getAvg() {
		return monitor.getAvg();
	}

	@Override
	public double getMin() {
		return monitor.getMin();
	}

	@Override
	public void setMin(double value) {
		monitor.setMin(value);
	}

	@Override
	public double getMax() {
		return monitor.getMax();
	}

	@Override
	public void setMax(double value) {
		monitor.setMax(value);
	}

	@Override
	public double getHits() {
		return monitor.getHits();
	}

	@Override
	public void setHits(double value) {
		monitor.setHits(value);
	}

	@Override
	public boolean equals(Object obj) {
		return monitor.equals(obj);
	}

	@Override
	public double getStdDev() {
		return monitor.getStdDev();
	}

	@Override
	public void setFirstAccess(Date date) {
		monitor.setFirstAccess(date);
	}

	@Override
	public Date getFirstAccess() {
		return monitor.getFirstAccess();
	}

	@Override
	public void setLastAccess(Date date) {
		monitor.setLastAccess(date);
	}

	@Override
	public Date getLastAccess() {
		return monitor.getLastAccess();
	}

	@Override
	public double getLastValue() {
		return monitor.getLastValue();
	}

	@Override
	public void setLastValue(double value) {
		monitor.setLastValue(value);
	}

	@Override
	public void disable() {
		monitor.disable();
	}

	@Override
	public void enable() {
		monitor.enable();
	}

	@Override
	public boolean isEnabled() {
		return monitor.isEnabled();
	}

	@Override
	public ListenerType getListenerType(String listenerType) {
		return monitor.getListenerType(listenerType);
	}

	@Override
	public boolean hasListeners(String listenerTypeName) {
		return monitor.hasListeners(listenerTypeName);
	}

	@Override
	public void addListener(String listenerTypeName, JAMonListener listener) {
		monitor.addListener(listenerTypeName, listener);
	}

	@Override
	public boolean hasListener(String listenerTypeName, String listenerName) {
		return monitor.hasListener(listenerTypeName, listenerName);
	}

	@Override
	public void removeListener(String listenerTypeName, String listenerName) {
		monitor.removeListener(listenerTypeName, listenerName);
	}

	@Override
	public Monitor start() {
		return monitor.start();
	}

	@Override
	public Monitor skip() {
		return monitor.skip();
	}

	@Override
	public Monitor stop() {
		return monitor.stop();
	}

	@Override
	public Monitor add(double value) {
		return monitor.add(value);
	}

	@Override
	public Range getRange() {
		return monitor.getRange();
	}

	@Override
	public double getActive() {
		return monitor.getActive();
	}

	@Override
	public void setActive(double value) {
		monitor.setActive(value);
	}

	@Override
	public double getMaxActive() {
		return monitor.getMaxActive();
	}

	@Override
	public void setMaxActive(double value) {
		monitor.setMaxActive(value);
	}

	@Override
	public void setTotalActive(double value) {
		monitor.setTotalActive(value);
	}

	@Override
	public boolean isPrimary() {
		return monitor.isPrimary();
	}

	@Override
	public void setPrimary(boolean isPrimary) {
		monitor.setPrimary(isPrimary);
	}

	@Override
	public boolean hasListeners() {
		return monitor.hasListeners();
	}

	@Override
	public Object getValue(String key) {
		return monitor.getValue(key);
	}

	@Override
	public String toString() {
		return monitor.toString();
	}

	@Override
	public void setActivityTracking(boolean trackActivity) {
		monitor.setActivityTracking(trackActivity);
	}

	@Override
	public boolean isActivityTracking() {
		return monitor.isActivityTracking();
	}

	@Override
	public double getAvgActive() {
		return monitor.getAvgActive();
	}

	@Override
	public double getAvgGlobalActive() {
		return monitor.getAvgGlobalActive();
	}

	@Override
	public double getAvgPrimaryActive() {
		return monitor.getAvgPrimaryActive();
	}

	@Override
	public JAMonDetailValue getJAMonDetailRow() {
		return monitor.getJAMonDetailRow();
	}

	@Override
	public void close() throws Exception {
		monitor.stop();
	}

}
