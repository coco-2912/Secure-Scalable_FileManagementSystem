package io.ahakim.file.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    User findByVerificationToken(String token);
    User findByResetToken(String token);

    @Query("SELECT f FROM AttachFile f WHERE f.id = :id")
    AttachFile findFileById(@Param("id") Integer id);
}