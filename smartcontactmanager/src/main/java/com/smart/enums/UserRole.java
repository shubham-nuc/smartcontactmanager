package com.smart.enums;

public enum UserRole {
	
	USER("ROLE_USER","Role For User"),
	ADMIN("ROLE_ADMIN","Role For Admin");

	UserRole(String role, String description) {
		this.role=role;
		this.description=description;
	}
	
	private String role;
	private String description;
	
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
