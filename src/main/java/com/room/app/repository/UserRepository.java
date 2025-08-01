package com.room.app.repository;

import java.util.Optional;

import com.room.app.dto.UserAuthProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.room.app.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
	boolean existsByEmail(String email);
	Optional<User> findByEmail(String email);

	@Query("SELECT u.email as email, u.password as password FROM User u WHERE u.email = :email")
	Optional<UserAuthProjection> findEmailAndPasswordByEmail(@Param("email") String email);
}
    

