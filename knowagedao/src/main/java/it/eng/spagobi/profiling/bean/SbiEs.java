package it.eng.spagobi.profiling.bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;

public class SbiEs implements Serializable {

	private static final long serialVersionUID = 5039339701462158235L;

	public static class Builder {

		private final SbiEs es = new SbiEs();

		private Builder() {
			super();
		}

		public static Builder newBuilder() {
			return new Builder();
		}

		public Builder withOrganization(String organization) {
			es.setOrganization(organization);
			return this;
		}

		public Builder withTimestamp(Timestamp timestamp) {
			es.setTimestamp(timestamp);
			return this;
		}

		public Builder withId(int id) {
			es.setId(Integer.toString(id));
			return this;
		}

		public Builder withEventId(UUID eventId) {
			es.setEventId(eventId.toString());
			return this;
		}

		public Builder withEvent(String event) {
			es.setEvent(event);
			return this;
		}

		public Builder withData(String data) {
			es.setData(data);
			return this;
		}

		public Builder withData(JsonObject data) {
			es.setData(data.toString());
			return this;
		}

		public Builder withType(String type) {
			es.setType(type);
			return this;
		}

		public SbiEs build() {

			Tenant tenant = TenantManager.getTenant();
			String organization = Optional.ofNullable(tenant).map(Tenant::getName).orElse("");

			if (Objects.isNull(es.getOrganization())) {
				es.setOrganization(organization);
			}

			Objects.requireNonNull(es.getOrganization(), "The organization field cannot be null");
			Objects.requireNonNull(es.getTimestamp(), "The timestamp field cannot be null");
			Objects.requireNonNull(es.getType(), "The type field cannot be null");
			Objects.requireNonNull(es.getId(), "The id field cannot be null");
			Objects.requireNonNull(es.getEventId(), "The event id field cannot be null");
			Objects.requireNonNull(es.getEvent(), "The event field cannot be null");
			Objects.requireNonNull(es.getData(), "The data field cannot be null");

			return es;
		}
	}

	private Long prog;
	private String organization;
	private Timestamp timestamp = Timestamp.from(Instant.now());
	private String type;
	private String id;
	private String eventId = UUID.randomUUID().toString();
	private String event;
	private String data;

	public SbiEs() {
		super();
	}

	/**
	 * @return the prog
	 */
	public Long getProg() {
		return prog;
	}

	/**
	 * @param prog the prog to set
	 */
	public void setProg(Long prog) {
		this.prog = prog;
	}

	/**
	 * @return the organization
	 */
	public String getOrganization() {
		return organization;
	}

	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	/**
	 * @return the timestamp
	 */
	public Timestamp getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the eventId
	 */
	public String getEventId() {
		return eventId;
	}

	/**
	 * @param eventId the eventId to set
	 */
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	/**
	 * @return the event
	 */
	public String getEvent() {
		return event;
	}

	/**
	 * @param event the event to set
	 */
	public void setEvent(String event) {
		this.event = event;
	}

	/**
	 * @return the data
	 */
	public String getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(String data) {
		this.data = data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((event == null) ? 0 : event.hashCode());
		result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((organization == null) ? 0 : organization.hashCode());
		result = prime * result + ((prog == null) ? 0 : prog.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SbiEs other = (SbiEs) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (event == null) {
			if (other.event != null)
				return false;
		} else if (!event.equals(other.event))
			return false;
		if (eventId == null) {
			if (other.eventId != null)
				return false;
		} else if (!eventId.equals(other.eventId))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (organization == null) {
			if (other.organization != null)
				return false;
		} else if (!organization.equals(other.organization))
			return false;
		if (prog == null) {
			if (other.prog != null)
				return false;
		} else if (!prog.equals(other.prog))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SbiEs [prog=" + prog + ", organization=" + organization + ", timestamp=" + timestamp + ", type=" + type + ", id=" + id + ", eventId=" + eventId
				+ ", event=" + event + ", data=" + data + "]";
	}

}
