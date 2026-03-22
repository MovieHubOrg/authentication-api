package com.authentication.api.repository;

import com.authentication.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByIdAndAccountStatus(Long id, Integer status);

    Optional<User> findFirstByAccountEmailAndAccountStatusNot(String email, Integer status);

    boolean existsByAccountUsernameAndAccountStatusNot(String username, Integer status);

    boolean existsByAccountPhoneAndAccountStatusNot(String phone, Integer status);
}
