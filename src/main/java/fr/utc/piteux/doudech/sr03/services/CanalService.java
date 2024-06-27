package fr.utc.piteux.doudech.sr03.services;


import fr.utc.piteux.doudech.sr03.models.Canals;
import fr.utc.piteux.doudech.sr03.models.Users;
import fr.utc.piteux.doudech.sr03.models.dto.CanalsDTO;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Repository
@Transactional
@Service
public class CanalService {

    @PersistenceContext
    EntityManager em;

    @Resource
    private UserService userService;

    @Resource
    private WebSocketService webSocketService;

    /**
        * Add Canal.
        * @deprecated Instead, of using this method, U should use the save method from the JPA repository.
        * As we're late in the semestre when we started using the JPA repository,
        * we decided to keep the old way of doing things. But to demonstrate our
        * understanding of the JPA repository, we will use it in the future.
        * Instead of using this method, U should use the save method from the JPA repository.
     */
    @Deprecated(forRemoval = true)
    public void addCanal(Canals canals){
        em.persist(canals);
    }

    /** Make User join Canal.
     * @deprecated Instead, Instead of using this method, U should use the save method from the JPA repository.
     * As we're late in the semestre when we started using the JPA repository,
     * we decided to keep the old way of doing things. But to demonstrate our
     * understanding of the JPA repository, we will use it in the future.
     * Instead of using this method, U should use the save method from the JPA repository.
     */
    @Deprecated(forRemoval = true)
    public void joinCanal(Canals canal, Users user){
        canal.addUserJoined(user);
        em.merge(canal);
    }

    /**
     * Update Canal.
     * @deprecated Instead, Instead of using this method, U should use the save method from the JPA repository.
     * As we're late in the semestre when we started using the JPA repository,
     * we decided to keep the old way of doing things. But to demonstrate our
     * understanding of the JPA repository, we will use it in the future.
     * Instead of using this method, U should use the save method from the JPA repository.
     */
    @Deprecated(forRemoval = true)
    public void updateCanal(Canals canals){
        em.merge(canals);
    }

    /**
     * Get one Canal.
     * @deprecated Instead, Instead of using this method, U should use the save method from the JPA repository.
     * As we're late in the semestre when we started using the JPA repository,
     * we decided to keep the old way of doing things. But to demonstrate our
     * understanding of the JPA repository, we will use it in the future.
     * Instead of using this method, U should use the save method from the JPA repository.
     */
    @Deprecated(forRemoval = true)
    public Canals getOneCanal(int id){
        //return un user via la clé primaire
        return em.find(Canals.class, id);
    }

    /** Delete Canal.
     * @deprecated Instead, Instead of using this method, U should use the save method from the JPA repository.
     * As we're late in the semestre when we started using the JPA repository,
     * we decided to keep the old way of doing things. But to demonstrate our
     * understanding of the JPA repository, we will use it in the future.
     * Instead of using this method, U should use the save method from the JPA repository.
     */
    @Deprecated(forRemoval = true)
    public void deleteOneCanal(int id){
        //return un user via la clé primaire
        em.remove(em.find(Canals.class, id));
    }

    /** Get Canals.
     * @deprecated Instead, Instead of using this method, U should use the save method from the JPA repository.
     * As we're late in the semestre when we started using the JPA repository,
     * we decided to keep the old way of doing things. But to demonstrate our
     * understanding of the JPA repository, we will use it in the future.
     * Instead of using this method, U should use the save method from the JPA repository.
     */
    @Deprecated(forRemoval = true)
    public List<Canals> getCanals(){
        Query q = em.createQuery("select u from Canals u");
        return q.getResultList();
    }

    public CanalsDTO getCanalDTO(Canals canal) {
        CanalsDTO canalDTO = new CanalsDTO(
                canal.getCid(),
                userService.getUserDTO(canal.getCreatorId()),
                canal.getCanalTitle(),
                canal.getCanalDescription(),
                canal.getCreationDate().toString(),
                canal.getValidUntilDate().toString(),
                canal.isCanalEnabled()
        );
        canal.getUsersJoined().forEach(user -> canalDTO.addJoinedUser(userService.getUserDTO(user)));

        webSocketService.getConnectedUsersDTOToCanal(canal).forEach(canalDTO::addConnectedUser);
        return canalDTO;
    }



}