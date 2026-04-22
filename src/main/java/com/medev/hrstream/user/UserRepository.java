package com.medev.hrstream.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPasswordResetTokenHash(String passwordResetTokenHash);

    Page<User> findByRoleAndIsDeletedFalse(Role role, Pageable pageable);

    Page<User> findByIsDeletedFalse(Pageable pageable);

    long countByIsDeletedFalse();

    long countByRoleAndIsDeletedFalse(Role role);

    long countByIsActiveTrueAndIsDeletedFalse();

    @Query("select count(u) from User u where u.isDeleted = false and function('month', u.createdAt) = function('month', current_date) and function('year', u.createdAt) = function('year', current_date)")
    long countNewUsersThisMonth();
}
