/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.event.find;

import java.util.List;

import edu.harvard.hms.dbmi.bd2k.irct.event.IRCTEvent;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

/**
 * An event listener that is run after a find is executed
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface AfterFind extends IRCTEvent {

	void fire(List<Entity> matches, FindInformationInterface findInformation, SecureSession session);

}
