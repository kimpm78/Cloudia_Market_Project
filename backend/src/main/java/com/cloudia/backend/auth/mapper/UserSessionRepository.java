package com.cloudia.backend.auth.mapper;

import com.cloudia.backend.auth.model.UserSession;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepository extends CrudRepository<UserSession, String> {
    Optional<UserSession> findByAccessToken(String accessToken);
}