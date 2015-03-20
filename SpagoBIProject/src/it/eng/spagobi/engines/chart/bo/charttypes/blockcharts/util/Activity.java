/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.blockcharts.util;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.chart.bo.charttypes.blockcharts.BlockCharts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;


public class Activity {

	// Fields from Database
	String code;
	Date beginDate;
//	Double hour;
	Hour hour;
	Minute minutes;
	Integer hourCod;
	Integer duration;
	String pattern;

	private static transient Logger logger=Logger.getLogger(Activity.class);


	public Activity(String code, Date beginDate, Hour hour, Integer hourCod,
			Integer duration, String pattern) {
		super();
		this.code = code;
		this.beginDate = beginDate;
		this.hour = hour;
		this.hourCod = hourCod;
		this.duration = duration;
		this.pattern = pattern;
	}

	public Activity(SourceBean sb, String dateFormatS, String hourFormatS) throws ParseException {
		logger.error("IN");
		code=(String)sb.getAttribute(BlockCharts.ANNOTATION);
		String data=(String)sb.getAttribute(BlockCharts.BEGIN_ACTIVITY_DATE);
		//String tempo=(String)sb.getAttribute(BlockCharts.BEGIN_ACTIVITY_TIME);
		
		
		//SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dateFormat=new SimpleDateFormat(dateFormatS);		
		SimpleDateFormat hourFormat=new SimpleDateFormat(hourFormatS);	
		Date date=null;
		Date hour=null;
		try{
			logger.debug("date retrieved to convert is "+data.toString());
			date=dateFormat.parse(data);
		}
		catch (ParseException e) {
			logger.error("Wrong format specified: could not convert date "+data);
			throw e;
		}
		logger.debug("time date is "+date.toString());
		try{
			java.sql.Timestamp timestamp = new java.sql.Timestamp(hourFormat.parse(data).getTime());
			hour=new Date(timestamp.getTime());
			//hour=hourFormat.parse(data);
		}
		catch (ParseException e) {
			logger.error("Wrong format specified: could not convert time "+data);
			throw e;
		}
		logger.debug("hour date is "+hour.toString());
		beginDate=date;
		//beginDate=new Date(milliseconds);
//		String hourS=(String)sb.getAttribute(BlockCharts.HOUR);
//		Double hour=Double.valueOf(hourS);
		minutes=new Minute(hour);

//		String hourCode=(String)sb.getAttribute();
//		if(hourCode!=null){
//		hourCod=Integer.valueOf(hourCode);
//		}
		pattern=(String)sb.getAttribute(BlockCharts.PATTERN);
		if(pattern==null)pattern="";
		String durationS=(String)sb.getAttribute(BlockCharts.DURATION);
		duration=Integer.valueOf(durationS);
		logger.debug("OUT");

		// calculate valueFase from fase

	}



	public Date getBeginDate() {
		return beginDate;
	}
	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}
	public Integer getHourCod() {
		return hourCod;
	}
	public void setHourCod(Integer hourCod) {
		this.hourCod = hourCod;
	}
	public Integer getDuration() {
		return duration;
	}
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Hour getHour() {
		return hour;
	}

	public void setHour(Hour hour) {
		this.hour = hour;
	}

	public Minute getMinutes() {
		return minutes;
	}

	public void setMinutes(Minute minutes) {
		this.minutes = minutes;
	}

	public double getStringTime(){
		String hourS=Integer.valueOf(this.hour.getHour()).toString();
		String minutesS=Integer.valueOf(this.minutes.getMinute()).toString();
		double d=Double.valueOf(hourS+"."+minutesS);
		return d;
	}


}
