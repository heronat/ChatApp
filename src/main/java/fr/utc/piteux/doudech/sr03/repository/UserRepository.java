package fr.utc.piteux.doudech.sr03.repository;

import fr.utc.piteux.doudech.sr03.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);
    Optional<Users> findByUid(int uid);
    Boolean existsByEmail(String email);
}
