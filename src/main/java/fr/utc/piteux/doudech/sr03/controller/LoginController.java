package fr.utc.piteux.doudech.sr03.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import fr.utc.piteux.doudech.sr03.models.UserPasswordReset;
import fr.utc.piteux.doudech.sr03.models.Users;
import fr.utc.piteux.doudech.sr03.payload.requests.EmailRequest;
import fr.utc.piteux.doudech.sr03.payload.requests.LoginRequest;
import fr.utc.piteux.doudech.sr03.services.UserService;
import fr.utc.piteux.doudech.sr03.utils.EmailSender;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import fr.utc.piteux.doudech.sr03.repository.PasswordResetRepository;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class LoginController {
    @Resource
    private UserService userServicesRequest;

    @Autowired
    PasswordResetRepository userPasswordResetRepository;

    //Retourne la page de login
    @GetMapping("/admin/login")
    public String getLoginPage(Model model, HttpServletRequest request){
        HttpSession session = request.getSession(false);

        if(session != null) {
            return "redirect:/admin/active_users";
        }

        LoginRequest loginRequest = new LoginRequest();
        model.addAttribute("loginRequest", loginRequest);
        return "Login";
    }

    //Vérifie les informations de connection d'un utilisateur et son rôle
    @PostMapping("/admin/login")
    public String requestLogin(@ModelAttribute("loginRequest") LoginRequest loginRequest, HttpServletRequest request){

        Users u = userServicesRequest.getOneUserByEmail(loginRequest.getEmail());

        if(u!=null){
            String passwordToCheck = loginRequest.getPassword();
            boolean passwordMatches = BCrypt.verifyer().verify(passwordToCheck.toCharArray(), u.getPasswordHash()).verified;

            if(passwordMatches && u.isAdmin()) {
                HttpSession session = request.getSession(true);

                session.setAttribute("adminId", u.getUid());
                return "redirect:/admin/active_users";
            }
        }
        return "Login";
    }

    //Déconnecte l'utilisateur
    @GetMapping("/admin/logout")
    public String logoutUser(Model model, HttpServletRequest request){
        HttpSession session = request.getSession(true);
        session.invalidate();
        return "redirect:/admin/login";
    }

    //Retourne la page de demande de réinitialisation de mot de passe
    @GetMapping("/admin/forgottenPassword")
    public String forgottenPassword(Model model, HttpServletRequest request){
        HttpSession session = request.getSession(false);

        if(session != null) {
            return "redirect:/admin/active_users";
        }

        EmailRequest emailRequest = new EmailRequest();
        model.addAttribute("emailRequest", emailRequest);
        return "ForgottenPassword";
    }

    //Envoie un mail de réinitialisation de mot de passe si l'email correspond à un utilisateur
    @PostMapping("/admin/forgottenPassword")
    public String requestForgottenPassword(@ModelAttribute("emailRequest") EmailRequest emailRequest, HttpServletRequest request){
        Users u = userServicesRequest.getOneUserByEmail(emailRequest.getEmail());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 15);

        if(u!=null){
            String token = UUID.randomUUID().toString();
            userPasswordResetRepository.save(new UserPasswordReset(u, token, new Date(), cal.getTime()));

            EmailSender.sendMail(u.getEmail(), "Reset your password", "<a href='http://localhost:8080/admin/resetPassword/" + token + "'> Reset your password here </a>");
            return "redirect:/admin/login";
        }
        return "redirect:/admin/forgottenPassword";
    }

    //Retourne la page de réinitialisation de mot de passe si le token est valide et non expiré
    @GetMapping("/admin/resetPassword/{token}")
    public String resetPassword(@PathVariable("token") String token, Model model, HttpServletRequest request){
        HttpSession session = request.getSession(false);

        if(session != null) {
            return "redirect:/admin/active_users";
        }

        UserPasswordReset u = userPasswordResetRepository.findUserPasswordResetByToken(token);

        if(u==null){
            return "redirect:/admin/forgottenPassword";
        }

        if(u.getExpirationDate().before(new Date())){
            return "redirect:/admin/forgottenPassword";
        }

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(u.getUser().getEmail());
        model.addAttribute("loginRequest", loginRequest);
        return "ResetPassword";
    }

    //Réinitialise le mot de passe de l'utilisateur si le mot de passe respecte les critères de sécurité
    @PostMapping("/admin/resetPassword")
    public String requestResetPassword(@ModelAttribute("loginRequest") LoginRequest loginRequest, HttpServletRequest request){
        HttpSession session = request.getSession(false);

        if(session != null) {
            return "redirect:/admin/active_users";
        }

        Users u = userServicesRequest.getOneUserByEmail(loginRequest.getEmail());

        if(u!=null){
            String password_pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
            Pattern pattern_password = Pattern.compile(password_pattern);
            Matcher matcher_password = pattern_password.matcher(loginRequest.getPassword());
            if (matcher_password.matches()) {
                String password = loginRequest.getPassword();
                u.setPasswordHash(BCrypt.withDefaults().hashToString(12, password.toCharArray()));
                userServicesRequest.updateUser(u);
            }
            return "redirect:/admin/login";
        }
        return "redirect:/admin/forgottenPassword";
    }
}
