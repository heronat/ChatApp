package fr.utc.piteux.doudech.sr03.repository;

import fr.utc.piteux.doudech.sr03.models.Canals;
import fr.utc.piteux.doudech.sr03.models.Invitation;
import fr.utc.piteux.doudech.sr03.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<List<Invitation>> findAllByInvitee(Users invitee);
    Optional<Invitation> findByInvitee(Users invitee);
    Optional<Invitation> findByInviter(Users inviter);
    Optional<Invitation> findByInviterAndCanal(Users inviter, Canals canal);
    Optional<Invitation> findByInviteeAndCanal(Users inviter, Canals canal);
    Boolean existsByInviteeAndCanal(Users invitee, Canals canal);
    Optional<Invitation> findByCanal(Canals canal);

    // Fais moi une requete pour mon repository qui fait un find all by canal
    Optional<List<Invitation>> findAllByCanal(Canals canal);
}