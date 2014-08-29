/**
 * Copyright (c) 2000-2014 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.liferay.faces.alloy.component.inputtime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.faces.FacesException;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.component.behavior.Behavior;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import com.liferay.faces.util.component.ComponentUtil;


/**
 * @author  Bruno Basto
 * @author  Kyle Stiemann
 */
@FacesComponent(value = InputTime.COMPONENT_TYPE)
public class InputTime extends InputTimeBase {

	// Public Constants
	public static final String COMPONENT_TYPE = "com.liferay.faces.alloy.component.inputtime.InputTime";
	public static final String RENDERER_TYPE = "com.liferay.faces.alloy.component.inputtime.InputTimeRenderer";
	public static final String STYLE_CLASS_NAME = "alloy-input-time";

	// Private Constants
	private static final String DEFAULT_TIME_PATTERN = "hh:mm a";
	private static final Collection<String> EVENT_NAMES = Collections.unmodifiableCollection(Arrays.asList(
				TimeSelectEvent.TIME_SELECT));
	private static final String MIN_TIME = "00:00:00";
	private static final String MIN_MAX_TIME_PATTERN = "HH:mm:ss";
	private static final String MAX_TIME = "23:59:59";

	public InputTime() {
		super();
		setRendererType(RENDERER_TYPE);
	}

	@Override
	public void addClientBehavior(String eventName, ClientBehavior clientBehavior) {

		if (clientBehavior instanceof AjaxBehavior) {
			AjaxBehavior ajaxBehavior = (AjaxBehavior) clientBehavior;
			ajaxBehavior.addAjaxBehaviorListener(new InputTimeBehaviorListener());
		}

		super.addClientBehavior(eventName, clientBehavior);
	}

	@Override
	protected AjaxBehaviorEvent createInputDateTimeEvent(UIComponent uiComponent, Behavior behavior, Date selected) {
		return new TimeSelectEvent(uiComponent, behavior, selected);
	}

	@Override
	protected void validateValue(FacesContext facesContext, Object newValue) {

		super.validateValue(facesContext, newValue);

		if (isValid() && (newValue != null)) {

			// Get all necessary dates.
			String timeZoneString = getTimeZone();
			TimeZone timeZone = TimeZone.getTimeZone(timeZoneString);
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(MIN_MAX_TIME_PATTERN);
			simpleDateFormat.setTimeZone(timeZone);

			String minTimeString = getMinTime();
			String maxTimeString = getMaxTime();
			Date minTime = null;
			Date maxTime = null;

			try {
				minTime = simpleDateFormat.parse(minTimeString);
				maxTime = simpleDateFormat.parse(maxTimeString);
			}
			catch (ParseException e) {

				FacesException facesException = new FacesException(e);
				throw facesException;
			}

			super.validateValue(facesContext, newValue, minTime, maxTime, timeZone);
		}
	}

	@Override
	public String getDefaultEventName() {
		return TimeSelectEvent.TIME_SELECT;
	}

	@Override
	public Collection<String> getEventNames() {

		List<String> eventNames = new ArrayList<String>();
		eventNames.addAll(super.getEventNames());
		eventNames.addAll(EVENT_NAMES);

		return Collections.unmodifiableList(eventNames);
	}

	@Override
	public String getMaxTime() {
		return (String) getStateHelper().eval(InputTimePropertyKeys.maxTime, MAX_TIME);
	}

	@Override
	public String getMinTime() {
		return (String) getStateHelper().eval(InputTimePropertyKeys.minTime, MIN_TIME);
	}

	@Override
	protected String getPattern() {
		return getTimePattern();
	}

	@Override
	public String getStyleClass() {

		// getStateHelper().eval(PropertyKeys.styleClass, null) is called because super.getStyleClass() may return the
		// STYLE_CLASS_NAME of the super class.
		String styleClass = (String) getStateHelper().eval(PropertyKeys.styleClass, null);

		return ComponentUtil.concatCssClasses(styleClass, STYLE_CLASS_NAME);
	}

	@Override
	public String getTimePattern() {
		return (String) getStateHelper().eval(InputTimePropertyKeys.timePattern, DEFAULT_TIME_PATTERN);
	}
}
