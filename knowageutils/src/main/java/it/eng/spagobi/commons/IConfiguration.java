package it.eng.spagobi.commons;

/**
 * Interface of a configuration entry.
 *
 * @author Marco Libanori
 */
public interface IConfiguration {
	/**
	 * @return the id
	 */
	public Integer getId();

	/**
	 * @return the label
	 */
	public String getLabel();

	/**
	 * @return the name
	 */
	public String getName();

	/**
	 * @return the description
	 */
	public String getDescription();

	/**
	 * @return the isActive
	 */
	public boolean isActive();

	/**
	 * @return the valueCheck
	 */
	public String getValueCheck();

	/**
	 * @return the valueTypeId
	 */
	public Integer getValueTypeId();

	/**
	 * @return the category to get
	 */
	public String getCategory();

}
