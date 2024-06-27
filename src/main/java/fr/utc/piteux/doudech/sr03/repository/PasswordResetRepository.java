package fr.utc.piteux.doudech.sr03.repository;

import fr.utc.piteux.doudech.sr03.models.Token;
import fr.utc.piteux.doudech.sr03.models.UserPasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<UserPasswordReset, Long> {
    UserPasswordReset findUserPasswordResetById(Long id);

    UserPasswordReset findUserPasswordResetByToken(String token);


}
