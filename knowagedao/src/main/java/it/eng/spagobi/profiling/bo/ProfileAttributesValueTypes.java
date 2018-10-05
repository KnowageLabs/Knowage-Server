package it.eng.spagobi.profiling.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ProfileAttributesEnumSerializer.class)

public enum ProfileAttributesValueTypes {
	NUM("Number"), STRING("String"), DATE("Date");
	@JsonProperty("value")
	private String type;

	private ProfileAttributesValueTypes(String type) {
		this.type = type;
	}

	private ProfileAttributesValueTypes(ProfileAttributesValueTypes type) {
		this.type = type.type;
	}

	private ProfileAttributesValueTypes(TypeObject type) {
		this.type = type.type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setType(TypeObject type) {
		this.type = type.type;
	}

	class TypeObject {
		String name;
		String type;

		public TypeObject() {
		}

		public TypeObject(String name, String type) {
			this.name = name;
			this.type = type;
		}

		public TypeObject(TypeObject to) {
			this.name = to.name;
			this.type = to.type;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

	}
}
