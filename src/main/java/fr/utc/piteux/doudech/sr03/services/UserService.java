package fr.utc.piteux.doudech.sr03.services;


import fr.utc.piteux.doudech.sr03.models.UserDetailsImpl;
import fr.utc.piteux.doudech.sr03.models.Users;
import fr.utc.piteux.doudech.sr03.models.dto.UsersDTO;
import fr.utc.piteux.doudech.sr03.repository.UserRepository;
import fr.utc.piteux.doudech.sr03.websockets.WebSocketHandler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    private final Logger logger = Logger.getLogger(WebSocketHandler.class.getName());

    @PersistenceContext
    EntityManager em;

    public void addUser(Users user){
        em.persist(user);
    }

    public void updateUser(Users user){
        em.merge(user);
    }

    public Users getOneUser(int id){
        //return un user via la clé primaire
        return em.find(Users.class, id);
    }

    public Users getOneUserByEmail(String email){
        Query q = em.createQuery("select u from Users u where u.email = :email");
        q.setParameter("email", email);
        List<Users> l = q.getResultList();
        if (l.isEmpty()) {
            return null;
        }
        return l.get(0);
    }



    public void deleteOneUser(int id){
        //return un user via la clé primaire
        em.remove(em.find(Users.class, id));
    }

    @SuppressWarnings("unchecked")
    public List<Users> getUsers(){
        Query q = em.createQuery("select u from Users u");
        return q.getResultList();
    }

    public Query listUsersQuery(boolean enable, String search){
        String queryString = "select u from Users u where u.isUserEnabled = :enable ";

        if (search != null && !search.isEmpty()) {
            queryString += " and (u.firstName like :firstname or u.lastName like :lastname or u.email like :email) ";
        }

        queryString += " order by u.email ";

        Query q = em.createQuery(queryString);

        if (search != null && !search.isEmpty()) {
            q.setParameter("firstname", "%" + search + "%");
            q.setParameter("lastname", "%" + search + "%");
            q.setParameter("email", "%" + search + "%");
        }
        q.setParameter("enable", enable);

        return q;
    }
    public int getNbUsers(String search, boolean enable){
        Query q = listUsersQuery(enable, search);
        return q.getResultList().size();
    }

    public List<Users> getUsers(int page, int size, String search, boolean enable){
        Query q = listUsersQuery(enable, search);

        q.setFirstResult(page * size);
        q.setMaxResults(size);
        return q.getResultList();
    }

    public boolean checkSession(HttpServletRequest request){
        HttpSession session = request.getSession(false);

        if(session != null){
            Integer userId = (Integer) session.getAttribute("adminId");
            Users admin = getOneUser(userId);
            //on vérifie si l'admin renseigné dans la session existe encore dans la base et si il a bien les droits administrateurs
            return admin != null && admin.isAdmin();
        }
        else {
            return false;
        }
    }

    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        Query query = em.createQuery("SELECT u FROM Users u WHERE u.email = :email");
        query.setParameter("email", email);

        List<Users> users = query.getResultList();
        if (users.size() != 1){
            throw new UsernameNotFoundException("User not found");
        }

        Users user = users.get(0);
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPasswordHash(), new ArrayList<>());
    }

    public Users getUserById(Integer id) {
        if (userRepository.findById(Long.valueOf(id)).isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        return userRepository.findById(Long.valueOf(id)).get();
    }

    public int getCurrentUserId() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            logger.warn(userDetails);
            Users user = userRepository.findByEmail(userDetails.getEmail()).get();
            return user.getUid();
        }
        return -1;
    }

    public UserDetailsImpl getCurrentUserDetails() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }
        return null;
    }

    public UsersDTO getUserDTO(Users user) {
        return new UsersDTO(
                user.getUid(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isAdmin(),
                user.isUserEnabled()
        );
    }

    public Boolean isUserAdminOrModerator(UserDetails userDetails) {
        return userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MODERATOR"));
    }

}