package com.stark.vms_backend.service;

import com.stark.vms_backend.entity.AppUser;

/**
 * SRP: sole responsibility is producing a bearer token string for an authenticated user.
 * OCP: callers depend on this abstraction; a new token format (e.g. opaque, refresh) can be
 *      added by providing a different implementation without touching AuthServiceImpl.
 */
public interface TokenService {

    /**
     * Generates a signed bearer token for the given user.
     *
     * @param user the authenticated user
     * @return the raw token string
     */
    String generateToken(AppUser user);

    /**
     * Returns the token type (e.g. "Bearer").
     */
    String getTokenType();

    /**
     * Returns the token lifetime in seconds.
     */
    long getExpiresIn();
}

