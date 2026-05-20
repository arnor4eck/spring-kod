package com.arnor4eck.springkod.repository;

import com.arnor4eck.springkod.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u.email FROM User u WHERE u.id = :id")
    String getEmailById(@Param("id") long id);
}
