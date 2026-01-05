package com.shelflife.project.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shelflife.project.dto.ChangePasswordRequest;
import com.shelflife.project.dto.ChangeUserDataRequest;
import com.shelflife.project.dto.LoginRequest;
import com.shelflife.project.dto.SignUpRequest;
import com.shelflife.project.exception.EmailExistsException;
import com.shelflife.project.exception.InvalidPasswordException;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.exception.PasswordsDontMatchException;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder encoder;

    public Optional<User> getOptionalUserByAuth(Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return Optional.empty();

        return repo.findByEmail(auth.getName());
    }

    public User getUserByAuth(Authentication auth) throws AccessDeniedException {
        if (auth == null || !auth.isAuthenticated())
            throw new AccessDeniedException(null);

        Optional<User> user = repo.findByEmail(auth.getName());

        if (!user.isPresent())
            throw new AccessDeniedException(null);

        return user.get();
    }

    public boolean isAuthenticated(Authentication auth) {
        try {
            getUserByAuth(auth);
            return true;
        } catch (AccessDeniedException e) {
            return false;
        }
    }

    public boolean isAdmin(Authentication auth) {
        try {
            User user = getUserByAuth(auth);
            return user.isAdmin();
        } catch (AccessDeniedException e) {
            return false;
        }
    }

    public List<User> getUsers() {
        return repo.findAll();
    }

    public List<User> getUsers(Authentication auth) throws AccessDeniedException {
        if (!isAdmin(auth))
            throw new AccessDeniedException(null);

        return repo.findAll();
    }

    public User getUserById(long id) throws ItemNotFoundException {
        Optional<User> user = repo.findById(id);

        if (!user.isPresent())
            throw new ItemNotFoundException("id", "User with this id was not found");

        return user.get();
    }

    public User getUserById(long id, Authentication auth) throws ItemNotFoundException, AccessDeniedException {
        if (isAuthenticated(auth))
            return getUserById(id);

        throw new AccessDeniedException(null);
    }

    public User getUserByEmail(String email) throws ItemNotFoundException {
        Optional<User> user = repo.findByEmail(email);

        if (!user.isPresent())
            throw new ItemNotFoundException("email", "User with this email was not found");

        return user.get();
    }

    public User getUserByEmail(String email, Authentication auth) throws ItemNotFoundException, AccessDeniedException {
        if (isAuthenticated(auth))
            return getUserByEmail(email);

        throw new AccessDeniedException(null);
    }

    @Transactional
    public User signUp(@Valid SignUpRequest request, Authentication auth)
            throws AccessDeniedException, EmailExistsException, PasswordsDontMatchException {

        if (isAuthenticated(auth))
            throw new AccessDeniedException(null);

        if (repo.existsByEmail(request.getEmail().toLowerCase()))
            throw new EmailExistsException();

        if (!request.getPassword().equals(request.getPasswordRepeat()))
            throw new PasswordsDontMatchException();

        User newUser = new User();
        newUser.setEmail(request.getEmail().toLowerCase());
        newUser.setUsername(request.getUsername());
        newUser.setPassword(encoder.encode(request.getPassword()));
        newUser.setAdmin(false);

        return repo.save(newUser);
    }

    @Transactional
    public String login(@Valid LoginRequest request, Authentication auth)
            throws AccessDeniedException, ItemNotFoundException {

        if (isAuthenticated(auth))
            throw new AccessDeniedException(null);

        User dbUser = getUserByEmail(request.getEmail().toLowerCase());
        if (!encoder.matches(request.getPassword(), dbUser.getPassword()))
            throw new AccessDeniedException(null);

        return jwtService.generateToken(request.getEmail());
    }

    @Transactional
    public void logout(Authentication auth) throws AccessDeniedException {
        if (!isAuthenticated(auth))
            throw new AccessDeniedException(null);

        jwtService.removeExpiredInvalidatedTokens();
        jwtService.invalidateToken((String) auth.getCredentials());
    }

    @Transactional
    public void changePassword(@Valid ChangePasswordRequest request, Authentication auth)
            throws AccessDeniedException, InvalidPasswordException, PasswordsDontMatchException {
        User currentUser = getUserByAuth(auth);

        if (!encoder.matches(request.getOldPassword(), currentUser.getPassword()))
            throw new InvalidPasswordException();

        if (!request.getNewPassword().equals(request.getNewPasswordRepeat()))
            throw new PasswordsDontMatchException();

        currentUser.setPassword(encoder.encode(request.getNewPassword()));
        repo.save(currentUser);
    }

    @Transactional
    public User updateUser(long id, ChangeUserDataRequest request, Authentication auth)
            throws ItemNotFoundException, AccessDeniedException, EmailExistsException, IllegalArgumentException {

        User currentUser = getUserByAuth(auth);
        User dbUser = getUserById(id);

        if (!currentUser.isAdmin() && currentUser.getId() != dbUser.getId())
            throw new AccessDeniedException(null);

        if (request.getUsername() != null) {
            if (request.getUsername().isBlank())
                throw new IllegalArgumentException("username");

            dbUser.setUsername(request.getUsername());
        }

        if (request.getEmail() != null) {
            if (request.getEmail().isBlank())
                throw new IllegalArgumentException("email");

            if (repo.existsByEmail(request.getEmail().toLowerCase()))
                throw new EmailExistsException();

            dbUser.setEmail(request.getEmail().toLowerCase());
        }

        if (request.getIsAdmin() != null) {
            // Cant set your own admin priviliges
            if (currentUser.getId() == dbUser.getId())
                throw new AccessDeniedException(null);

            if (!currentUser.isAdmin())
                throw new AccessDeniedException(null);

            dbUser.setAdmin(request.getIsAdmin());
        }

        return repo.save(dbUser);
    }

    @Transactional
    public void removeUser(long id, Authentication auth) throws ItemNotFoundException, AccessDeniedException {
        User currentUser = getUserByAuth(auth);

        if (!currentUser.isAdmin())
            throw new AccessDeniedException(null);

        if (currentUser.getId() == id)
            throw new AccessDeniedException(null);

        if (!repo.existsById(id))
            throw new ItemNotFoundException("id", "User ID was not found");
        repo.deleteById(id);
    }
}
