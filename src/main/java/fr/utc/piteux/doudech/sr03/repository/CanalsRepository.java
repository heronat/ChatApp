package fr.utc.piteux.doudech.sr03.repository;

import fr.utc.piteux.doudech.sr03.models.Canals;
import fr.utc.piteux.doudech.sr03.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CanalsRepository extends JpaRepository<Canals, Long> {
    Optional<Canals> findCanalsByCid(int cid);
    Optional<Canals> findCanalsByCanalTitle(String inviter);

    @Query("SELECT c FROM Canals c JOIN c.usersJoined u WHERE u = :user")
    Optional<List<Canals>> findAllByUserIdJoined(Users user);
}