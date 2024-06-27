package fr.utc.piteux.doudech.sr03.controller;


import fr.utc.piteux.doudech.sr03.models.Users;
import fr.utc.piteux.doudech.sr03.services.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
@Controller
public class AdminController {
    @Resource
    private UserService userServicesRequest;

    //Retourne la page d'accueil de l'admin, redirige vers la page des utilisateurs actifs si l'utilisateur est connecté, sinon redirige vers la page de login
    @GetMapping("/admin")
    public String index(HttpServletRequest request){
        if(!userServicesRequest.checkSession(request)){
            return "redirect:/admin/login";
        }
        return "redirect:/admin/active_users";
    }

    //Retourne la liste paginée des utilisateurs actifs
    @GetMapping("/admin/active_users")
    public String active_users(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "8") int size, @RequestParam(defaultValue = "") String search, HttpServletRequest request){
        if(!userServicesRequest.checkSession(request)){
            return "redirect:/admin/login";
        }

        List<Users> users = userServicesRequest.getUsers(page, size, search, true);
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("search", search);
        model.addAttribute("totalPages", (int) Math.ceil((double)userServicesRequest.getNbUsers(search, true)/ size));
        return "Admin_active_users";
    }

    //Retourne la liste paginée des utilisateurs inactifs
    @GetMapping("/admin/inactive_users")
    public String inactive_users(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "8") int size, @RequestParam(defaultValue = "") String search, HttpServletRequest request){
        if(!userServicesRequest.checkSession(request)){
            return "redirect:/admin/login";
        }

        List<Users> users = userServicesRequest.getUsers(page, size, search, false);
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("search", search);
        model.addAttribute("totalPages", (int) Math.ceil((double)userServicesRequest.getNbUsers(search, false)/ size));
        return "Admin_inactive_users";
    }
}
