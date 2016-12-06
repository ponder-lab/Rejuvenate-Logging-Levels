/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Singleton;

import edu.harvard.hms.dbmi.bd2k.irct.action.Action;
import edu.harvard.hms.dbmi.bd2k.irct.event.action.AfterAction;
import edu.harvard.hms.dbmi.bd2k.irct.event.action.AfterExecutionPlan;
import edu.harvard.hms.dbmi.bd2k.irct.event.action.AfterJoin;
import edu.harvard.hms.dbmi.bd2k.irct.event.action.AfterProcess;
import edu.harvard.hms.dbmi.bd2k.irct.event.action.AfterQuery;
import edu.harvard.hms.dbmi.bd2k.irct.event.action.BeforeAction;
import edu.harvard.hms.dbmi.bd2k.irct.event.action.BeforeExecutionPlan;
import edu.harvard.hms.dbmi.bd2k.irct.event.action.BeforeJoin;
import edu.harvard.hms.dbmi.bd2k.irct.event.action.BeforeProcess;
import edu.harvard.hms.dbmi.bd2k.irct.event.action.BeforeQuery;
import edu.harvard.hms.dbmi.bd2k.irct.event.find.AfterFind;
import edu.harvard.hms.dbmi.bd2k.irct.event.find.BeforeFind;
import edu.harvard.hms.dbmi.bd2k.irct.event.result.AfterGetResult;
import edu.harvard.hms.dbmi.bd2k.irct.event.result.AfterSaveResult;
import edu.harvard.hms.dbmi.bd2k.irct.event.result.BeforeGetResult;
import edu.harvard.hms.dbmi.bd2k.irct.event.result.BeforeSaveResult;
import edu.harvard.hms.dbmi.bd2k.irct.executable.Executable;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.IRCTJoin;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

/**
 * Manages the event listeners
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Singleton
public class IRCTEventListener {
	private Map<String, List<IRCTEvent>> events;

	/**
	 * Initiates the even listener
	 */
	public void init() {
		events = new HashMap<String, List<IRCTEvent>>();
	}

	/**
	 * Registers a new event listener
	 * 
	 * @param eci Event Implementation
	 */
	public void registerListener(EventConverterImplementation eci) {

		IRCTEvent irctEvent = eci.getEventListener();
		String eventType = irctEvent.getClass().getInterfaces()[0]
				.getSimpleName();

		if (!events.containsKey(eventType)) {
			events.put(eventType, new ArrayList<IRCTEvent>());
		}

		irctEvent.init(eci.getParameters());

		events.get(eventType).add(irctEvent);

	}

	// Action Events
	/**
	 * Runs the event listeners after an action is run
	 * 
	 * @param session
	 *            Session
	 * @param action
	 *            Action
	 */
	public void afterAction(SecureSession session, Action action) {
		List<IRCTEvent> irctEvents = events.get("AfterAction");
		if (irctEvents == null)
			return;
		for (IRCTEvent irctEvent : irctEvents) {
			((AfterAction) irctEvent).fire(session, action);
		}
	}

	/**
	 * Runs the event listeners after an execution plan is run
	 * 
	 * @param session
	 *            Session
	 * @param executable
	 *            Execution Plan
	 */
	public void afterExecutionPlan(SecureSession session, Executable executable) {
		List<IRCTEvent> irctEvents = events.get("AfterExecutionPlan");
		if (irctEvents == null)
			return;
		for (IRCTEvent irctEvent : irctEvents) {
			((AfterExecutionPlan) irctEvent).fire(session, executable);
		}
	}

	/**
	 * Runs the event listeners after a join is run
	 * 
	 * @param session
	 *            Session
	 * @param joinType
	 *            Type of Join
	 */
	public void afterJoin(SecureSession session, IRCTJoin joinType) {
		List<IRCTEvent> irctEvents = events.get("AfterJoin");
		if (irctEvents == null)
			return;
		for (IRCTEvent irctEvent : irctEvents) {
			((AfterJoin) irctEvent).fire(session, joinType);
		}
	}

	/**
	 * Runs the event listeners after a process is run
	 * 
	 * @param session
	 *            Session
	 * @param process
	 *            Process
	 */
	public void afterProcess(SecureSession session, IRCTProcess process) {
		List<IRCTEvent> irctEvents = events.get("AfterProcess");
		if (irctEvents == null)
			return;
		for (IRCTEvent irctEvent : irctEvents) {
			((AfterProcess) irctEvent).fire(session, process);
		}
	}

	/**
	 * Runs the event listeners after a query is run
	 * 
	 * @param session
	 *            Session
	 * @param resource
	 *            Resource
	 * @param query
	 *            Query
	 */
	public void afterQuery(SecureSession session, Resource resource, Query query) {
		List<IRCTEvent> irctEvents = events.get("AfterQuery");
		if (irctEvents == null)
			return;
		for (IRCTEvent irctEvent : irctEvents) {
			((AfterQuery) irctEvent).fire(session, resource, query);
		}
	}

	public void afterVisualization() {
		// TODO: IMPLEMENTATION OF VISUALIZATION ACTION NEEDED
	}

	/**
	 * Runs the event listeners before an action is run
	 * 
	 * @param session
	 *            Session
	 * @param action
	 *            Action
	 */
	public void beforeAction(SecureSession session, Action action) {
		List<IRCTEvent> irctEvents = events.get("BeforeAction");
		if (irctEvents == null)
			return;
		for (IRCTEvent irctEvent : irctEvents) {
			((BeforeAction) irctEvent).fire(session, action);
		}
	}

	/**
	 * Runs the event listener before an execution plan is run
	 * 
	 * @param session
	 *            Session
	 * @param executable
	 *            Execution Plan
	 */
	public void beforeExecutionPlan(SecureSession session, Executable executable) {
		List<IRCTEvent> irctEvents = events.get("BeforeExecutionPlan");
		if (irctEvents == null)
			return;
		for (IRCTEvent irctEvent : irctEvents) {
			((BeforeExecutionPlan) irctEvent).fire(session, executable);
		}
	}

	/**
	 * Runs the event listeners before a join is run
	 * 
	 * @param session
	 *            Session
	 * @param joinType
	 *            Join
	 */
	public void beforeJoin(SecureSession session, IRCTJoin joinType) {
		List<IRCTEvent> irctEvents = events.get("BeforeJoin");
		if (irctEvents == null)
			return;
		for (IRCTEvent irctEvent : irctEvents) {
			((BeforeJoin) irctEvent).fire(session, joinType);
		}
	}

	/**
	 * Runs the event listeners before a process is run
	 * 
	 * @param session
	 *            Session
	 * @param process
	 *            Process
	 */
	public void beforeProcess(SecureSession session, IRCTProcess process) {
		List<IRCTEvent> irctEvents = events.get("BeforeProcess");
		if (irctEvents == null)
			return;
		for (IRCTEvent irctEvent : irctEvents) {
			((BeforeProcess) irctEvent).fire(session, process);
		}
	}

	/**
	 * Runs the event listeners before a query is run
	 * 
	 * @param session
	 *            Session
	 * @param resource
	 *            Resource
	 * @param query
	 *            Query
	 */
	public void beforeQuery(SecureSession session, Resource resource,
			Query query) {
		List<IRCTEvent> irctEvents = events.get("BeforeQuery");
		if (irctEvents == null)
			return;
		for (IRCTEvent irctEvent : irctEvents) {
			((BeforeQuery) irctEvent).fire(session, resource, query);
		}
	}

	public void beforeVisualization() {
		// TODO: IMPLEMENTATION OF VISUALIZATION ACTION NEEDED
	}

	// Result Events
	/**
	 * Runs the event listeners after a result is retrieved
	 * 
	 * @param result
	 *            Result
	 */
	public void afterGetResult(Result result) {
		List<IRCTEvent> irctEvents = events.get("AfterGetResult");
		if (irctEvents == null)
			return;
		for (IRCTEvent irctEvent : irctEvents) {
			((AfterGetResult) irctEvent).fire(result);
		}
	}

	/**
	 * Runs the event listers after a result is saved
	 * 
	 * @param result
	 *            Result
	 */
	public void afterSaveResult(Result result) {
		List<IRCTEvent> irctEvents = events.get("AfterSaveResult");
		if (irctEvents == null)
			return;
		for (IRCTEvent irctEvent : irctEvents) {
			((AfterSaveResult) irctEvent).fire(result);
		}
	}

	/**
	 * Runs the event listeners before a result is retrieved
	 * 
	 * @param user
	 *            User
	 * @param resultId
	 *            Result
	 */
	public void beforeGetResult(User user, Long resultId) {
		List<IRCTEvent> irctEvents = events.get("BeforeGetResult");
		if (irctEvents == null)
			return;
		for (IRCTEvent irctEvent : irctEvents) {
			((BeforeGetResult) irctEvent).fire(user, resultId);
		}
	}

	/**
	 * Runs the event listers before a result is saved
	 * 
	 * @param result
	 *            Result
	 */
	public void beforeSaveResult(Result result) {
		List<IRCTEvent> irctEvents = events.get("BeforeSaveResult");
		if (irctEvents == null)
			return;
		for (IRCTEvent irctEvent : irctEvents) {
			((BeforeSaveResult) irctEvent).fire(result);
		}
	}

	// FIND EVENTS
	/**
	 * Runs the event listeners before a find is executed
	 * 
	 * @param resource
	 *            Resource
	 * @param resourcePath
	 *            Path on the resource
	 * @param findInformation
	 *            Information for the find
	 * @param session
	 *            Session Information
	 */
	public void beforeFind(Resource resource, Entity resourcePath,
			List<FindInformationInterface> findInformation,
			SecureSession session) {
		List<IRCTEvent> irctEvents = events.get("BeforeFind");
		if (irctEvents == null)
			return;
		for (IRCTEvent irctEvent : irctEvents) {
			((BeforeFind) irctEvent).fire(resource, resourcePath,
					findInformation, session);
		}
	}

	/**
	 * Runs the listeners after a find is executed
	 * 
	 * @param matches A list of entities
	 * @param findInformation Information for the find
	 * @param session Session Information
	 */
	public void afterFind(List<Entity> matches,
			FindInformationInterface findInformation, SecureSession session) {

		List<IRCTEvent> irctEvents = events.get("AfterFind");
		if (irctEvents == null)
			return;
		for (IRCTEvent irctEvent : irctEvents) {
			((AfterFind) irctEvent).fire(matches, findInformation, session);
		}
	}
}
