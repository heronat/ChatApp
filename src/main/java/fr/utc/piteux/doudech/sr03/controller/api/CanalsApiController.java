package fr.utc.piteux.doudech.sr03.controller.api;

import fr.utc.piteux.doudech.sr03.models.Canals;
import fr.utc.piteux.doudech.sr03.models.Invitation;
import fr.utc.piteux.doudech.sr03.models.UserDetailsImpl;
import fr.utc.piteux.doudech.sr03.models.roles.ERole;
import fr.utc.piteux.doudech.sr03.payload.requests.CanalCreationRequest;
import fr.utc.piteux.doudech.sr03.payload.requests.CanalEditionRequest;
import fr.utc.piteux.doudech.sr03.payload.requests.OpenCanalRequest;
import fr.utc.piteux.doudech.sr03.repository.CanalsRepository;
import fr.utc.piteux.doudech.sr03.repository.InvitationRepository;
import fr.utc.piteux.doudech.sr03.repository.RoleRepository;
import fr.utc.piteux.doudech.sr03.services.CanalService;
import fr.utc.piteux.doudech.sr03.services.UserService;
import fr.utc.piteux.doudech.sr03.services.WebSocketService;
import fr.utc.piteux.doudech.sr03.websockets.WebSocketConfig;
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
@RequestMapping(path = "api/v1/canals")
public class CanalsApiController {

    private final Logger logger = Logger.getLogger(WebSocketHandler.class.getName());

    @Autowired
    InvitationRepository invitationRepository;

    @Autowired
    CanalsRepository canalsRepository;

    @Resource
    private CanalService canalServicesRequest;
    @Resource
    private UserService userService;
    @Resource
    private WebSocketService webSocketService;

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    private WebSocketConfig webSocketConfig;

    // Méthode pour créer un nouveau canal
    @PostMapping(value = "/create")
    public ResponseEntity<?> addCanal(@Valid @RequestBody CanalCreationRequest canalCreationRequest, BindingResult bindingResult){
        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            response.put("ok", false);
            response.put("message", "Invalid Canal data");

            List<String> errors = new ArrayList<String>();
            bindingResult.getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));

            response.put("errors", errors);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Définition des dates de création et de validité si elles ne sont pas fournies.
        if(canalCreationRequest.getCreationDate() == null) canalCreationRequest.setCreationDate(new Date());
        if(canalCreationRequest.getValidUntilDate() == null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, 90);
            canalCreationRequest.setValidUntilDate(cal.getTime());
        };

        UserDetailsImpl asker = userService.getCurrentUserDetails();
        canalsRepository.save(new Canals(asker.getUser(), canalCreationRequest.getCanalTitle(), canalCreationRequest.getCanalDescription(), canalCreationRequest.getCreationDate(), canalCreationRequest.getValidUntilDate()));

        response.put("ok", true);
        response.put("message", "Canal created successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Méthode pour éditer un canal existant
    @PostMapping(value = "/edit")
    public ResponseEntity<?> editCanal(@Valid @RequestBody CanalEditionRequest canalEditionRequest, BindingResult bindingResult){
        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            response.put("ok", false);
            response.put("message", "Invalid Canal data");

            List<String> errors = new ArrayList<String>();
            bindingResult.getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));

            response.put("errors", errors);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Canals canal = canalsRepository.findCanalsByCid(canalEditionRequest.getCanal()).orElse(null);
        UserDetailsImpl asker = userService.getCurrentUserDetails();

        if (canal == null) {
            response.put("ok", false);
            response.put("message", "Canal not found");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Vérification si l'utilisateur est autorisé à éditer le canal
        if (asker.getUser() == null ||
                (
                        !canal.getUsersJoined().contains(asker.getUser()) && !userService.isUserAdminOrModerator(asker)
                )
        ){
            response.put("ok", false);
            response.put("message", "User not found or not Authorized!");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (new Date(canal.getValidUntilDate().getTime()).before(new Date()) || !canal.isCanalEnabled()) {
            response.put("ok", false);
            response.put("message", "Canal is not valid anymore");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        canal.setCanalTitle(canalEditionRequest.getCanalTitle());
        canal.setCanalDescription(canalEditionRequest.getCanalDescription());
        canal.setValidUntilDate(canalEditionRequest.getValidUntilDate());

        canalsRepository.save(canal);

        response.put("ok", true);
        response.put("message", "Canal created successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Méthode pour rejoindre un canal
    @PostMapping(value = "/open")
    public ResponseEntity<?> joinCanal(@Valid @RequestBody OpenCanalRequest openCanalRequest, BindingResult bindingResult){
        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            response.put("ok", false);
            response.put("message", "Invalid Canal data");

            List<String> errors = new ArrayList<String>();
            bindingResult.getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));

            response.put("errors", errors);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(canalsRepository.findCanalsByCid(openCanalRequest.getCanalId()).isEmpty()){
            response.put("ok", false);
            response.put("message", "Canal not found");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Canals canal = canalsRepository.findCanalsByCid(openCanalRequest.getCanalId()).get();
        UserDetailsImpl asker = userService.getCurrentUserDetails();

        // Vérification si l'utilisateur est autorisé à rejoindre le canal
        if (asker.getUser() == null ||
                (
                        !canal.getUsersJoined().contains(asker.getUser()) && !userService.isUserAdminOrModerator(asker)
                )
        ){
            response.put("ok", false);
            response.put("message", "User not found or not Authorized!");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Vérification si le canal est toujours valide
        if (new Date(canal.getValidUntilDate().getTime()).before(new Date()) || !canal.isCanalEnabled()) {
            response.put("ok", false);
            response.put("message", "Canal is not valid anymore");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            String socketUrl = webSocketService.createWebSocketHandler(canal, asker.getUser());
            response.put("ok", true);
            response.put("message", "Canal opened successfully");
            response.put("socket_url", socketUrl);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            response.put("ok", false);
            response.put("message", "Error while creating websocket handler");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    // Méthode pour quitter un canal
    @DeleteMapping(value = "/leave/{canal}")
    public ResponseEntity<?> leaveCanal(@Valid @PathVariable String canal){
        Map<String, Object> response = new HashMap<>();

        try {
            Integer.parseInt(canal);
        } catch (NumberFormatException e) {
            response.put("ok", false);
            response.put("message", "Invalid Canal ID");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        int canalId = Integer.parseInt(canal);

        if(canalsRepository.findCanalsByCid(canalId).isEmpty()){
            response.put("ok", false);
            response.put("message", "Canal not found");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Canals canalObj = canalsRepository.findCanalsByCid(canalId).get();
        UserDetailsImpl asker = userService.getCurrentUserDetails();

        // Vérification si l'utilisateur est le créateur du canal
        if (asker.getUser().getUid() == canalObj.getCreatorId().getUid()){
            response.put("ok", false);
            response.put("message", "Creator cannot leave his own Canal");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (asker.getUser() == null ||
                        !canalObj.getUsersJoined().contains(asker.getUser())
        ){
            response.put("ok", false);
            response.put("message", "User not found or not Authorized!");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Mise à jour de l'invitation pour correspondre au départ de l'utilisateur
        Invitation invitation = invitationRepository.findByInviteeAndCanal(asker.getUser(), canalObj).orElse(new Invitation(canalObj, canalObj.getCreatorId(), asker.getUser()));
        invitation.setStatus(Invitation.Status.LEAVED);

        invitationRepository.save(invitation);

        canalObj.removeUser(asker.getUser());
        canalsRepository.save(canalObj);


        response.put("ok", true);
        response.put("message", "Canal leaved successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Méthode pour supprimer un canal
    @DeleteMapping(value = "/delete/{canal}")
    public ResponseEntity<?> deleteCanal(@Valid @PathVariable String canal){
        Map<String, Object> response = new HashMap<>();

        try {
            Integer.parseInt(canal);
        } catch (NumberFormatException e) {
            response.put("ok", false);
            response.put("message", "Invalid Canal ID");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        int canalId = Integer.parseInt(canal);

        if(canalsRepository.findCanalsByCid(canalId).isEmpty()){
            response.put("ok", false);
            response.put("message", "Canal not found");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Canals canalObj = canalsRepository.findCanalsByCid(canalId).get();
        UserDetailsImpl asker = userService.getCurrentUserDetails();

        // Vérification si l'utilisateur est le propriétaire et donc est autorisé à supprimer le canal
        if (asker.getUser() == null ||
                !canalObj.getUsersJoined().contains(asker.getUser()) ||
                (canalObj.getCreatorId().getUid() != asker.getUser().getUid() && !userService.isUserAdminOrModerator(asker))
        ){
            response.put("ok", false);
            response.put("message", "User not found or not Authorized!");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        canalsRepository.delete(canalObj);
        webSocketService.killChannel(canalObj);


        response.put("ok", true);
        response.put("message", "Canal deleted successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Méthode pour récupérer les canaux qu'un utilisateur a rejoint
    @GetMapping(value = "/getMyCanals")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MOD') or hasRole('USER') ")
    public ResponseEntity<?> getMyCanals(){
        Map<String, Object> response = new HashMap<>();

        UserDetailsImpl asker = userService.getCurrentUserDetails();
        List<Canals> canals = canalsRepository.findAllByUserIdJoined(asker.getUser()).get();


        if(canals.isEmpty()){
            response.put("ok", false);
            response.put("message", "Users have not joined any Canal");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.put("canals", canals.stream().map(canal -> canalServicesRequest.getCanalDTO(canal)));
        response.put("ok", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Méthode pour récupérer les informations d'un canal selon son id
    @GetMapping(value = "/getCanalInfo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MOD') or hasRole('USER') ")
    public ResponseEntity<?> getCanalInfo(@RequestParam(defaultValue = "-1") int canal){
        Map<String, Object> response = new HashMap<>();

        Canals canalObj = canalsRepository.findCanalsByCid(canal).orElse(null);

        if (canalObj == null) {
            response.put("ok", false);
            response.put("message", "Canal not found");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        UserDetailsImpl asker = userService.getCurrentUserDetails();

        if(!canalObj.getUsersJoined().contains(asker.getUser()) && !asker.getAuthorities().contains(ERole.ROLE_ADMIN)){
            for (int i = 0; i < canalObj.getUsersJoined().size(); i++) {
                logger.warn(canalObj.getUsersJoined().get(i).toString());
            }
            response.put("ok", false);
            response.put("message", "User not authorized to see the members of this Canal");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        response.put("canal", canalServicesRequest.getCanalDTO(canalObj));
        response.put("ok", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
