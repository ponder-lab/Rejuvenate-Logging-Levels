package edu.harvard.hms.dbmi.bd2k.irct.model.process;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

/**
 * An IRCT Process Parameter is used by an IRCT Process to describe a set of
 * values that can be passed into it.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Entity
public class IRCTProcessParameter implements Serializable {

	private static final long serialVersionUID = 4141141792005064323L;

	@Id
	private long id;
	private String name;
	@Enumerated(EnumType.STRING)
	private IRCTProcessParameterType type;
	private String value;
	
	/**
	 * Returns the id
	 * 
	 * @return ID
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the id
	 * 
	 * @param id ID
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns the name of the parameter
	 * 
	 * @return Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the parameter
	 * 
	 * @param name Name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the IRCT Parameter Type
	 * 
	 * @return Parameter Type
	 */
	public IRCTProcessParameterType getType() {
		return type;
	}

	/**
	 * Sets the IRCT Parameter Type
	 * 
	 * @param type Parameter Type
	 */
	public void setType(IRCTProcessParameterType type) {
		this.type = type;
	}

	/**
	 * Returns the value of the parameter
	 * 
	 * @return Value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value of the parameter
	 * 
	 * @param value Value
	 */
	public void setValue(String value) {
		this.value = value;
	}

}
