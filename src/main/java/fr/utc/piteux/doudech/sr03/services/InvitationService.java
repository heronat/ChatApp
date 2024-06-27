package fr.utc.piteux.doudech.sr03.services;

import fr.utc.piteux.doudech.sr03.models.Invitation;
import fr.utc.piteux.doudech.sr03.models.dto.InvitationDTO;
import fr.utc.piteux.doudech.sr03.models.dto.InvitationInviterDTO;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
@Service
public class InvitationService {

    @Resource
    private CanalService canalsService;

    @Resource
    private UserService usersService;

    public InvitationDTO getInvitationDTO(Invitation invitation) {
        return new InvitationDTO(
                invitation.getId(),
                canalsService.getCanalDTO(invitation.getCanal()),
                usersService.getUserDTO(invitation.getInviter()),
                invitation.getStatus().name()
        );
    }

    public InvitationInviterDTO getInviterDTO(Invitation invitation) {
        return new InvitationInviterDTO(
                invitation.getId(),
                canalsService.getCanalDTO(invitation.getCanal()),
                usersService.getUserDTO(invitation.getInvitee()),
                invitation.getStatus().name()
        );
    }

    public List<InvitationInviterDTO> convertInvitationListToInviterInvitationDto(List<Invitation> invitation) {
        List<InvitationInviterDTO> invitationsList = new ArrayList<>();
        for (Invitation i : invitation) {
            invitationsList.add(getInviterDTO(i));
        }
        return invitationsList;
    }

    public List<InvitationDTO> convertInvitationListToInvitationDto(List<Invitation> invitation) {
        List<InvitationDTO> invitationsList = new ArrayList<>();
        for (Invitation i : invitation) {
            invitationsList.add(getInvitationDTO(i));
        }
        return invitationsList;
    }

}
