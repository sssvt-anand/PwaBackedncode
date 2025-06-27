package com.room.app.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "app_user")
public class User implements UserDetails { // Implement UserDetails
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Email is required")
    @Email
    @Column(nullable = false)
    private String email;

    @Column(name = "is_deleted", columnDefinition = "CHAR(1)")
    private String isDeleted = "N";

    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'ROLE_USER'")
    private String role = "ROLE_USER"; // Consistent role prefix

	// Getters and Setters
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public void promoteToAdmin() {
		this.role = "ADMIN";
	}

	public boolean isAdmin() {
		return "ADMIN".equals(this.role);
	}

	// Override getAuthorities() to return roles as SimpleGrantedAuthority
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(role)); // Use role as authority directly
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setUsername(String username) {
		this.name = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	

}