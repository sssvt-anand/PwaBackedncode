package com.room.app.dto;

public class UserResponse  {
    private Long id;
    private String name;
	private String email;
    private String role;


    public UserResponse(Long id, String username, String role,String email) {
        this.id = id;
        this.name = username;
        this.role = role;
		this.email=email;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return name;
	}

	public void setUsername(String username) {
		this.name = username;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}