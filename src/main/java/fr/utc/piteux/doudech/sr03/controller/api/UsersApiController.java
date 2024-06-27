package fr.utc.piteux.doudech.sr03.controller.api;

import fr.utc.piteux.doudech.sr03.models.Users;
import fr.utc.piteux.doudech.sr03.models.dto.UsersDTO;
import fr.utc.piteux.doudech.sr03.payload.requests.ProfileUpdateRequest;
import fr.utc.piteux.doudech.sr03.repository.UserRepository;
import fr.utc.piteux.doudech.sr03.services.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "api/v1/users")
public class UsersApiController {


    @Autowired
    UserRepository userRepository;

    @Resource
    private UserService userService;

    @Autowired
    PasswordEncoder encoder;

    // Récupérer la liste de tous les utilisateurs, peu importe le rôle
    @GetMapping(value = "/getUsers")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getUsers(){
        Map<String, Object> response = new HashMap<>();
        List<UsersDTO> users = new ArrayList<>();

        userRepository.findAll().forEach(user -> {
            users.add(userService.getUserDTO(user));
        });

        if(users.isEmpty()){
            response.put("ok", false);
            response.put("message", "User list is empty");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }


        response.put("ok", true);
        response.put("users", users);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Récupérer le profil de l'utilisateur courant
    @GetMapping(value = "/getProfile")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getUser(){
        Map<String, Object> response = new HashMap<>();
        Users asker = userService.getCurrentUserDetails().getUser();

        response.put("ok", true);
        response.put("profile", userService.getUserDTO(asker));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Mettre à jour le profil de l'utilisateur courant
    @PostMapping(value = "/updateProfile")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileUpdateRequest profileUpdateRequest){
        Map<String, Object> response = new HashMap<>();
        Users asker = userService.getCurrentUserDetails().getUser();

        if (profileUpdateRequest.getFirstName() != null) asker.setFirstName(profileUpdateRequest.getFirstName());
        if (profileUpdateRequest.getLastName() != null) asker.setLastName(profileUpdateRequest.getLastName());
        if (profileUpdateRequest.getEmail() != null) asker.setEmail(profileUpdateRequest.getEmail());
        if (profileUpdateRequest.getPassword() != null) asker.setPasswordHash(encoder.encode(profileUpdateRequest.getPassword()));

        userRepository.save(asker);

        response.put("ok", true);
        response.put("message", "Profile updated successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
