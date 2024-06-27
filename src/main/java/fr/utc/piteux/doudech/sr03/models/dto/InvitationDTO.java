package fr.utc.piteux.doudech.sr03.models.dto;

public record InvitationDTO(Long id, CanalsDTO canal, UsersDTO inviter, String status) {

}
