package fr.utc.piteux.doudech.sr03.repository;

import fr.utc.piteux.doudech.sr03.models.Token;
import fr.utc.piteux.doudech.sr03.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    void deleteTokenByToken(String token);
    void deleteTokenByUser(Users user);

    Optional<Token> findTokenByToken(String token);
    Optional<Token> findTokenByTokenAndUsername(String token, String username);
    Optional<Token> findTokenByUser(Users user);


}