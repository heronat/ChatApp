package fr.utc.piteux.doudech.sr03.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import fr.utc.piteux.doudech.sr03.models.Users;
import fr.utc.piteux.doudech.sr03.models.roles.ERole;
import fr.utc.piteux.doudech.sr03.models.roles.Role;
import fr.utc.piteux.doudech.sr03.repository.RoleRepository;
import fr.utc.piteux.doudech.sr03.services.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

@Controller
public class UserController {
    @Autowired
    RoleRepository roleRepository;

    @Resource
    private UserService userServicesRequest;

    @Autowired
    PasswordEncoder encoder;


    //Retourne le formulaire de création d'un utilisateur
    @GetMapping("/admin/add")
    public String createUser(Model model, HttpServletRequest request){
        if(!userServicesRequest.checkSession(request)){
            return "redirect:/admin/login";
        }

        Users user = new Users();
        model.addAttribute("user", user);
        return "Admin_new_user";
    }

    //Ajoute un utilisateur dans la base de données
    @PostMapping("/admin/add")
    public String addUser(@ModelAttribute("user") Users user, HttpServletRequest request) {
        if(!userServicesRequest.checkSession(request)){
            return "redirect:/admin/login";
        }

        String email_pattern = "^[\\w-\\.]+@[\\w-\\.]+\\.[a-z]{2,}$";
        Pattern pattern_email = Pattern.compile(email_pattern);

        String password_pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        Pattern pattern_password = Pattern.compile(password_pattern);

        if (user.getPasswordHash() == null || user.getEmail() == null) {
            return "Admin_new_user";
        }

        Matcher matcher_email = pattern_email.matcher(user.getEmail());
        Matcher matcher_password = pattern_password.matcher(user.getPasswordHash());

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        user.setRoles(roles);

        if (matcher_password.matches() && matcher_email.matches()) {
            user.setPasswordHash(encoder.encode(user.getPasswordHash()));
            user.setUserEnabled(true);
            userServicesRequest.addUser(user);
            return "redirect:/admin/active_users";
        }
        return "Admin_new_user";
    }

    //Retourne le formulaire d'édition d'un utilisateur en fonction de son id
    @GetMapping("/admin/edit/{uid}")
    public String showEditForm(@PathVariable("uid") int uid, Model model, HttpServletRequest request) {
        if(!userServicesRequest.checkSession(request)){
            return "redirect:/admin/login";
        }

        Users u = userServicesRequest.getOneUser(uid);
        if (u!=null){
            model.addAttribute("user", u);
            return "Admin_edit_user";
        }
        else {
            return "redirect:/admin/active_users";
        }

    }

    //Met à jour les informations d'un utilisateur en fonction de son id s'il existe toujours en base
    @PostMapping("/admin/edit/{uid}")
    public String editUser(@PathVariable("uid") int uid, @ModelAttribute("user") Users user, HttpServletRequest request) {
        if(!userServicesRequest.checkSession(request)){
            return "redirect:/admin/login";
        }

        Users u = userServicesRequest.getOneUser(uid);
        if (u!=null){
            u.setFirstName(user.getFirstName());
            u.setLastName(user.getLastName());
            u.setEmail(user.getEmail());
            userServicesRequest.updateUser(u);
        }
        return "redirect:/admin/active_users";
    }

    //Supprime un utilisateur en fonction de son id
    @GetMapping("/admin/delete/{uid}")
    public String deleteUser(@PathVariable("uid") int uid, Model model, HttpServletRequest request) {
        if(!userServicesRequest.checkSession(request)){
            return "redirect:/admin/login";
        }

        Users user = userServicesRequest.getOneUser(uid);
        if (user!=null){
            userServicesRequest.deleteOneUser(uid);
        }
        return "redirect:/admin/active_users";
    }

    //Change le statut d'un utilisateur en actif
    @GetMapping("/admin/set_inactive/{uid}")
    public String setInactive(@PathVariable("uid") int uid, Model model, HttpServletRequest request) {
        if(!userServicesRequest.checkSession(request)){
            return "redirect:/admin/login";
        }

        Users u = userServicesRequest.getOneUser(uid);
        if (u!=null){
            u.setUserEnabled(false);
            userServicesRequest.updateUser(u);
        }
        return "redirect:/admin/active_users";
    }

    //Change le statut d'un utilisateur en inactif
    @GetMapping("/admin/set_active/{uid}")
    public String setActive(@PathVariable("uid") int uid, Model model, HttpServletRequest request) {
        if(!userServicesRequest.checkSession(request)){
            return "redirect:/admin/login";
        }

        Users u = userServicesRequest.getOneUser(uid);
        if (u!=null){
            u.setUserEnabled(true);
            userServicesRequest.updateUser(u);
        }
        return "redirect:/admin/inactive_users";
    }

    //Change le rôle d'un compte en administrateur
    @GetMapping("/admin/setAdmin/{uid}")
    public String setAdmin(@PathVariable("uid") int uid, Model model, HttpServletRequest request) {
        if(!userServicesRequest.checkSession(request)){
            return "redirect:/admin/login";
        }

        Users u = userServicesRequest.getOneUser(uid);
        if (u!=null){
            u.setAdmin(true);
            userServicesRequest.updateUser(u);
        }
        return "redirect:/admin/active_users";
    }

    //Change le rôle d'un compte en utilisateur
    @GetMapping("/admin/setUser/{uid}")
    public String setUser(@PathVariable("uid") int uid, Model model, HttpServletRequest request) {
        if(!userServicesRequest.checkSession(request)){
            return "redirect:/admin/login";
        }

        Users u = userServicesRequest.getOneUser(uid);
        if (u!=null){
            u.setAdmin(false);
            userServicesRequest.updateUser(u);
        }
        return "redirect:/admin/active_users";
    }
}
