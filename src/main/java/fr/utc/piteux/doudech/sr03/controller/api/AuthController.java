package fr.utc.piteux.doudech.sr03.controller.api;


import fr.utc.piteux.doudech.sr03.models.Token;
import fr.utc.piteux.doudech.sr03.models.UserDetailsImpl;
import fr.utc.piteux.doudech.sr03.models.UserPasswordReset;
import fr.utc.piteux.doudech.sr03.models.Users;
import fr.utc.piteux.doudech.sr03.models.roles.ERole;
import fr.utc.piteux.doudech.sr03.models.roles.Role;
import fr.utc.piteux.doudech.sr03.payload.requests.*;
import fr.utc.piteux.doudech.sr03.payload.response.JwtResponse;
import fr.utc.piteux.doudech.sr03.payload.response.MessageResponse;
import fr.utc.piteux.doudech.sr03.repository.PasswordResetRepository;
import fr.utc.piteux.doudech.sr03.repository.RoleRepository;
import fr.utc.piteux.doudech.sr03.repository.TokenRepository;
import fr.utc.piteux.doudech.sr03.repository.UserRepository;
import fr.utc.piteux.doudech.sr03.utils.EmailSender;
import fr.utc.piteux.doudech.sr03.utils.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    PasswordResetRepository userPasswordResetRepository;

    // Route pour authentifier un utilisateur
    @CrossOrigin(origins = "*")
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        List<Object> tokenJwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Sauvegarde du token JWT généré
        tokenRepository.save(new Token((String) tokenJwt.get(0), userDetails.getUsername(), userDetails.getUser(), (Date) tokenJwt.get(1), (Date) tokenJwt.get(2)));

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(
                (String) tokenJwt.get(0),
                userDetails.getId(),
                userDetails.getUser().getFirstName() + " " + userDetails.getUser().getLastName(),
                userDetails.getEmail(),
                roles));
    }

    // Route pour enregistrer un nouvel utilisateur
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest, BindingResult bindingResult) {

        Map<String, Object> response = new HashMap<>();
        if (bindingResult.hasErrors()) {
            response.put("ok", false);
            response.put("message", "Invalid payload data");

            List<String> errors = new ArrayList<String>();
            bindingResult.getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));

            response.put("errors", errors);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        //Vérifier si l'email est déjà utilisé
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            response.put("ok", false);
            response.put("message", "Error: Email is already in use!");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Users user = new Users(signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        user.setRoles(roles);

        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        userRepository.save(user);

        response.put("ok", true);
        response.put("message", "User registered successfully!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Route pour demander une réinitialisation de mot de passe avec un envoi de mail
    @PostMapping("/password/forgotten")
    public ResponseEntity<?> forgottenPassword(@Valid @RequestBody EmailRequest emailRequest, BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        if (bindingResult.hasErrors()) {
            response.put("ok", false);
            response.put("message", "Invalid payload data");

            List<String> errors = new ArrayList<String>();
            bindingResult.getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));

            response.put("errors", errors);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        Optional<Users> user = userRepository.findByEmail(emailRequest.getEmail());

        if (user.isEmpty()) {
            response.put("ok", false);
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 15);
        // Génération du token de réinitialisation
        String token = UUID.randomUUID().toString();

        userPasswordResetRepository.save(new UserPasswordReset(user.get(), token, new Date(), cal.getTime()));

        try {
            EmailSender.sendMail(user.get().getEmail(), "Reset your password", "<a href='http://localhost/password-reset?token=" + token + "'> Reset your password here </a>");
        } catch (Exception e) {
            response.put("ok", false);
            response.put("message", "Error sending mail");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        response.put("ok", true);
        response.put("message", "Email sent successfully!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Route pour réinitialiser le mot de passe si le token est valide et non expiré
    @PostMapping("/password/reset/{token}")
    public ResponseEntity<?> resetPassword(@PathVariable("token") String token, @Valid @RequestBody PasswordResetRequest passwordResetRequest, BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        if (bindingResult.hasErrors()) {
            response.put("ok", false);
            response.put("message", "Invalid payload data");

            List<String> errors = new ArrayList<String>();
            bindingResult.getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));

            response.put("errors", errors);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        UserPasswordReset userPasswordReset = userPasswordResetRepository.findUserPasswordResetByToken(token);

        if (userPasswordReset == null) {
            response.put("ok", false);
            response.put("message", "Invalid token");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (userPasswordReset.getExpirationDate().before(new Date())) {
            response.put("ok", false);
            response.put("message", "Token expired");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Users u = userPasswordReset.getUser();

        if (u == null) {
            response.put("ok", false);
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        u.setPasswordHash(encoder.encode(passwordResetRequest.getPassword()));
        userRepository.save(u);

        // Suppression du token de réinitialisation utilisé
        userPasswordResetRepository.delete(userPasswordReset);

        response.put("ok", true);
        response.put("message", "Password reset successfully!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Route pour déconnecter un utilisateur
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logoutUser(@Valid @RequestBody LogoutRequest logoutRequest, BindingResult bindingResult){
        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            response.put("ok", false);
            response.put("message", "Invalid Canal data");

            List<String> errors = new ArrayList<String>();
            bindingResult.getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));

            response.put("errors", errors);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        Optional<Token> tokenData = tokenRepository.findTokenByTokenAndUsername(logoutRequest.getToken(), logoutRequest.getEmail());
        tokenData.ifPresent(token -> tokenRepository.delete(token));

        response.put("ok", true);
        response.put("message", "User logged out successfully!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
