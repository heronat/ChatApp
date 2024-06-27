package fr.utc.piteux.doudech.sr03.controller.api;

import fr.utc.piteux.doudech.sr03.models.Canals;
import fr.utc.piteux.doudech.sr03.models.Invitation;
import fr.utc.piteux.doudech.sr03.models.UserDetailsImpl;
import fr.utc.piteux.doudech.sr03.models.Users;
import fr.utc.piteux.doudech.sr03.payload.requests.InvitationRequest;
import fr.utc.piteux.doudech.sr03.payload.requests.InvitationRequestResponse;
import fr.utc.piteux.doudech.sr03.repository.CanalsRepository;
import fr.utc.piteux.doudech.sr03.repository.InvitationRepository;
import fr.utc.piteux.doudech.sr03.repository.UserRepository;
import fr.utc.piteux.doudech.sr03.services.InvitationService;
import fr.utc.piteux.doudech.sr03.services.UserService;
import fr.utc.piteux.doudech.sr03.websockets.WebSocketHandler;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(path = "api/v1/invitations")
public class InvitationController {
    private final Logger logger = Logger.getLogger(WebSocketHandler.class.getName());

    @Autowired
    InvitationRepository invitationRepository;
    @Autowired
    CanalsRepository canalsRepository;

    @Autowired
    UserRepository userRepository;

    @Resource
    private UserService userService;

    @Resource
    private InvitationService  invitationService;

    // Récupérer les invitations de l'utilisateur courant
    @GetMapping(value = "/getMyInvitations")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MOD') or hasRole('USER') ")
    public ResponseEntity<?> getMyInvitations(){
        Map<String, Object> response = new HashMap<>();

        UserDetailsImpl asker = userService.getCurrentUserDetails();
        List<Invitation> invitations = invitationRepository.findAllByInvitee(asker.getUser()).get();

        if(invitations.isEmpty()){
            response.put("ok", false);
            response.put("message", "Users have not been invited to any canal yet");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.put("invitations", invitationService.convertInvitationListToInvitationDto(invitations));
        response.put("ok", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Envoyer une invitation à un utilisateur pour un canal donné
    @PostMapping(value = "/invite")
    public ResponseEntity<?> inviteUserToCanal(@Valid @RequestBody InvitationRequest invitationRequest, BindingResult bindingResult){
        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            response.put("ok", false);
            response.put("message", "Invalid User data");

            List<String> errors = new ArrayList<String>();
            bindingResult.getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));

            response.put("errors", errors);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(userRepository.findByUid(invitationRequest.getUserId()).isEmpty() || canalsRepository.findCanalsByCid(invitationRequest.getCanalId()).isEmpty()){
            response.put("ok", false);
            response.put("message", "User or Canal not found");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        UserDetailsImpl asker = userService.getCurrentUserDetails();
        Users inviter = userService.getCurrentUserDetails().getUser();
        Users invitee = userRepository.findByUid(invitationRequest.getUserId()).get();
        Canals canal = canalsRepository.findCanalsByCid(invitationRequest.getCanalId()).get();

        // Vérification des permissions pour inviter un utilisateur au canal
        if (!canal.getCreatorId().equals(inviter) && !userService.isUserAdminOrModerator(asker)) {
            response.put("ok", false);
            response.put("message", "You are not allowed to invite users to this Canal");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Vérification si l'utilisateur a déjà été invité au canal
        if (invitationRepository.existsByInviteeAndCanal(invitee, canal)) {
            response.put("ok", false);
            response.put("message", "User have already been invited to this canal");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (canal.getUsersJoined().contains(invitee)) {
            response.put("ok", false);
            response.put("message", "User have already joined the Canal");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        invitationRepository.save(new Invitation(canal, inviter, invitee));

        response.put("ok", true);
        response.put("message", "user invited successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Méthode pour répondre à une invitation (ACCEPTER ou REFUSER)
    @PostMapping(value = "/answer")
    public ResponseEntity<?> answer(@Valid @RequestBody InvitationRequestResponse invitationRequestResponse, BindingResult bindingResult){
        Map<String, Object> response = new HashMap<>();

        logger.warn(invitationRequestResponse.toString());

        if (bindingResult.hasErrors()) {
            response.put("ok", false);
            response.put("message", "Invalid User data");

            List<String> errors = new ArrayList<String>();
            bindingResult.getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));

            response.put("errors", errors);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (invitationRepository.findById(invitationRequestResponse.getInvitationId()).isEmpty()) {
            logger.warn(invitationRequestResponse.getInvitationId());
            response.put("ok", false);
            response.put("message", "Invitation not found");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        UserDetailsImpl asker = userService.getCurrentUserDetails();
        Invitation invitation = invitationRepository.findById(invitationRequestResponse.getInvitationId()).get();

        if (    invitation.getCanal().getCid() != invitationRequestResponse.getCanalId() ||
                (!Objects.equals(invitationRequestResponse.getStatus(), "ACCEPTED") &&
                !Objects.equals(invitationRequestResponse.getStatus(), "DECLINED") ) ||
                invitation.getInvitee().getUid() != asker.getUser().getUid() || invitation.getStatus() != Invitation.Status.PENDING
        ) {
            response.put("ok", false);
            response.put("message", "Invitation not found OR information mismatch OR invalid status");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        switch (invitationRequestResponse.getStatus()) {
            case "ACCEPTED":
                invitation.setStatus(Invitation.Status.ACCEPTED);
                invitation.getCanal().addUserJoined(invitation.getInvitee());
                break;
            case "DECLINED":
                invitation.setStatus(Invitation.Status.DECLINED);
                break;
            default:
                response.put("ok", false);
                response.put("message", "Invalid status");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        invitationRepository.save(invitation);
        canalsRepository.save(invitation.getCanal());

        response.put("ok", true);
        response.put("message", "invitation answered successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Méthode pour récupérer les invitations d'un canal spécifique
    @GetMapping(value = "/getcanalinvitations")
    // Donne moi le code pour récupérer un query params dans une requete GET
    public ResponseEntity<?> getCanalInvitations(@RequestParam("canalId") Integer canalId){
        Map<String, Object> response = new HashMap<>();

        logger.warn(canalId.toString());

        if (canalsRepository.findCanalsByCid(canalId).isEmpty()) {
            response.put("ok", false);
            response.put("message", "Canal not found");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Canals canal = canalsRepository.findCanalsByCid(canalId).get();
        UserDetailsImpl asker = userService.getCurrentUserDetails();

        if (
                !canal.getUsersJoined().contains(asker.getUser()) && !userService.isUserAdminOrModerator(asker)
        ) {
            response.put("ok", false);
            response.put("message", "You are not allowed to see this Canal's invitations");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        List<Invitation> invitations = invitationRepository.findAllByCanal(canal).get();

        if(invitations.isEmpty()){
            response.put("ok", false);
            response.put("message", "No invitations for this Canal");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.put("ok", true);
        response.put("invitations", invitationService.convertInvitationListToInviterInvitationDto(invitations));

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
