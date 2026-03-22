package com.authentication.api.repository;

import com.authentication.api.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    Optional<Account> findFirstByUsernameAndStatusNot(String username, int status);

    Optional<Account> findFirstByPhoneAndStatusNot(String phone, int status);

    Optional<Account> findFirstByEmail(String email);

    Optional<Account> findFirstByEmailAndStatusNot(String email, int status);

    Optional<Account> findFirstByEmailAndStatus(String email, int status);

    Account findAccountByUsername(String username);

    Optional<Account> findAccountByEmail(String email);

    Boolean existsByPhoneAndStatusNot(String phone, int status);

    Boolean existsByEmailAndStatusNot(String email, int status);

    Boolean existsByUsernameAndStatusNot(String username, int status);

    Optional<Account> findByIdAndStatus(Long id, Integer status);
}
