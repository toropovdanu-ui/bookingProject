package com.skillbox.repository;

import com.skillbox.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByEmail(String email);

    @Query("""
            SELECT u.email
            FROM UserEntity u
            WHERE u.notificationSettings.notifyNewEvents = true
            """)
    List<String> findSubscribedUserEmails();
}
