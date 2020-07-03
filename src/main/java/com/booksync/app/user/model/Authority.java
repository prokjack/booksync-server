package com.booksync.app.user.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Document(collection = "authority")
@NoArgsConstructor
@AllArgsConstructor
public class Authority implements Serializable, GrantedAuthority {

	private static final long serialVersionUID = 4L;

	@NotNull
	@Size(max = 50)
	@Id
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Authority authority = (Authority) o;

		return !(name != null ? !name.equals(authority.name) : authority.name != null);
	}

	@Override
	public String getAuthority() {
		return name;
	}
}