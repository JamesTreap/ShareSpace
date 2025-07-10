# flask_api/services/room_service.py
from sqlalchemy.exc import IntegrityError
from entities import db
from entities.room import Room
from repository.room_repo import RoomRepo, RoomMember, RoomInvitation
from typing import Optional, List
from flask import abort
from repository.user_repo import UserRepo
from entities.user import User


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

    @staticmethod
    def get_rooms_for_user(user_id: int):
        return RoomRepo.list_rooms_for_user(user_id)

    @staticmethod
    def get_invites_for_user(user_id: int):
        return RoomRepo.list_invites_for_user(user_id)

    @staticmethod
    def get_room_with_members_if_user_is_member(room_id: int, user: User) -> Room:
        room = RoomRepo.get_room_with_members(room_id)
        if room is None:
            abort(404, "Room not found")
        if user.id not in (m.user_id for m in room.members):
            abort(403, "You are not a member of this room")
        return room

    @staticmethod
    def create_room_for_user(name: str, picture_url: Optional[str], user: User) -> Room:
        room = RoomRepo.create_room(name, picture_url)
        RoomRepo.add_member(room.id, user.id)
        return room

    @staticmethod
    def invite_user_to_room(room_id: int, inviter: User, invitee_username: str) -> RoomInvitation:
        room = RoomRepo.get_room_with_members(room_id)
        if room is None:
            abort(404, "Room not found")

        if inviter.id not in (m.user_id for m in room.members):
            abort(403, "You are not a member of this room")

        invitee = UserRepo.find_by_username(invitee_username)
        if not invitee:
            abort(404, "User not found")

        return RoomRepo.create_invitation(room_id, inviter.id, invitee.id)

    @staticmethod
    def respond_to_invitation(room_id: int, user: User, status: str) -> RoomInvitation:
        invitation = RoomRepo.waiting_invitations_for_user_by_room(user.id, room_id)
        if invitation is None:
            abort(404, "Invitation not found")

        updated = RoomRepo.respond_to_invitation(invitation.id, status)
        if status == "accepted":
            RoomRepo.add_member(updated.room_id, user.id)
        return updated

    @staticmethod
    def get_room_by_id_with_members(room_id: int):
        return RoomRepo.get_room_with_members(room_id)

    @staticmethod
    def validate_room_users(user_ids: List[int], room_id: int) -> bool:
        users = UserRepo.get_users_by_ids(user_ids)
        if not all(any(room_member.room_id == room_id for room_member in user.rooms) for user in users):
            return False
        return True

    @staticmethod
    def validate_room_user(user_id: int, room_id: int) -> bool:
        user = UserRepo.find_by_id(user_id)

        if not user:
            return False
        return any(room_member.room_id == room_id for room_member in user.rooms)
