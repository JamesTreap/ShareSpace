# flask_api/services/room_service.py
from sqlalchemy.exc import IntegrityError
from flask_api.entities import db
from flask_api.repository.room_repo import RoomRepo, RoomMember, RoomInvitation


class RoomService:
    @staticmethod
    def accept_invitation(invitation_id: int) -> RoomMember:

        with db.session.begin():
            inv: RoomInvitation = RoomRepo.get_invitation(invitation_id)
            if inv is None:
                raise ValueError("Invitation not found")
            inv.status = "accepted"
            try:
                member = RoomRepo.add_member(inv.room_id, inv.invitee_user_id)
            except IntegrityError:
                db.session.rollback()
                member = RoomRepo.find_member(inv.room_id, inv.invitee_user_id)

        return member
