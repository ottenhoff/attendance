/*
 *  Copyright (c) 2015, The Apereo Foundation
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.tool.pages;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.*;

import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.tool.dataproviders.EventDataProvider;
import org.sakaiproject.attendance.tool.pages.panels.EventInputPanel;
import org.sakaiproject.attendance.tool.util.ConfirmationLink;

/**
 * A simple page which allows for events to be added.
 * 
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 *
 */
public class AddEventPage extends BasePage {
	protected AttendanceEvent attendanceEvent;

	EventDataProvider eventDataProvider;

	WebMarkupContainer eventListContainer;
	WebMarkupContainer eventFormContainer;

	Form eventForm;
	ConfirmationLink<Void> deleteItem;

	public AddEventPage() {
		this(null);
	}

	public AddEventPage(AttendanceEvent aE) {
		disableLink(addEventLink);

		this.attendanceEvent = aE;

		eventFormContainer = new WebMarkupContainer("event-form-container");
		eventFormContainer.setOutputMarkupId(true);


		if(attendanceEvent != null) {
			deleteItem = new ConfirmationLink<Void>("delete-event", getString("attendance.delete.confirm")) {
				@Override
				public void onClick(AjaxRequestTarget ajaxRequestTarget) {
					String name = attendanceEvent.getName();
					if(attendanceLogic.deleteAttendanceEvent(attendanceEvent)) {
						getSession().info(name + " deleted successfully.");
						refreshPageComponents(ajaxRequestTarget);
					}
				}
			};
			eventFormContainer.add(deleteItem);
		} else {
			// Add dummy/hidden delete link
			deleteItem = new ConfirmationLink<Void>("delete-event", "") {
				@Override
				public void onClick(AjaxRequestTarget ajaxRequestTarget) {
					// Do nothing
				}
			};
			deleteItem.setVisible(false);
			eventFormContainer.add(deleteItem);
		}

		eventForm = createForm();
		eventFormContainer.add(eventForm);

		add(eventFormContainer);

		eventListContainer = new WebMarkupContainer("event-list-container");
		eventListContainer.setOutputMarkupId(true);

		eventDataProvider = new EventDataProvider();

		DataView<AttendanceEvent> attendanceEventDataView = new DataView<AttendanceEvent>("event-list", eventDataProvider) {
			@Override
			protected void populateItem(final Item<AttendanceEvent> item) {
				ConfirmationLink<Void> deleteLink = new ConfirmationLink<Void>("delete-link", getString("attendance.delete.confirm")) {
					@Override
					public void onClick(AjaxRequestTarget ajaxRequestTarget) {
						String name = item.getModelObject().getName();
						if(attendanceLogic.deleteAttendanceEvent(item.getModelObject())) {
							getSession().info(name + " deleted successfully.");
							refreshPageComponents(ajaxRequestTarget);
						}
					}
				};
				item.add(deleteLink);
				Link<Void> editLink = new Link<Void>("edit-link") {
					@Override
					public void onClick() {
						setResponsePage(new AddEventPage(item.getModelObject()));
					}
				};
				editLink.add(new Label("event-name", item.getModelObject().getName()));
				item.add(editLink);
			}
		};

		eventListContainer.add(attendanceEventDataView);

		add(eventListContainer);
	}

	private Form createForm() {
		if(attendanceEvent == null) {
			attendanceEvent = new AttendanceEvent();
		}

		Form form = new Form("form");
		form.add(new EventInputPanel("event", new CompoundPropertyModel<AttendanceEvent>(attendanceEvent)));
		form.add(new AjaxSubmitLink("submit") {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				refreshPageComponents(target);
			}
		});

		return form;
	}

	private void refreshPageComponents(AjaxRequestTarget target) {
		attendanceEvent = null;
		eventForm = createForm();
		eventFormContainer.addOrReplace(eventForm);
		deleteItem.setVisible(false);
		eventFormContainer.addOrReplace(deleteItem);
		target.add(eventFormContainer);
		target.add(eventListContainer);
		target.add(feedbackPanel);
	}
}
