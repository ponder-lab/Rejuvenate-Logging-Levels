/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.security;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;

/**
 * A token representation that is associated with a user
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public class Token implements Serializable {
	private static final long serialVersionUID = -2974837028914026971L;
	
	@Id
	@GeneratedValue(generator = "tokenSequencer")
	@SequenceGenerator(name = "tokenSequencer", sequenceName = "tokenSeq")
	private Long id;

	/**
	 * Returns the id of this token
	 * 
	 * @return Id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the id of this token
	 * @param id Id
	 */
	public void setId(Long id) {
		this.id = id;
	}
}
