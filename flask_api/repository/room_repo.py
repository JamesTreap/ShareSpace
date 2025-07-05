# flask_api/repos/room_repo.py
from typing import List, Optional
from sqlalchemy import select
from sqlalchemy.orm import joinedload
from entities import db
from entities.room import Room, RoomMember, RoomInvitation
from entities.user import User


class RoomRepo:
    @staticmethod
    def create_room(name: str, picture_url: Optional[str] = None) -> Room:
        room = Room(name=name, picture_url=picture_url)
        db.session.add(room)
        db.session.commit()
        return room

    @staticmethod
    def get_room(room_id: int) -> Optional[Room]:
        return db.session.get(Room, room_id)

    @staticmethod
    def add_member(room_id: int, user_id: int) -> RoomMember:
        member = RoomMember(room_id=room_id, user_id=user_id)
        db.session.add(member)
        db.session.commit()
        return member

    @staticmethod
    def create_invitation(
        room_id: int,
        inviter_user_id: int,
        invitee_user_id: int,
        status: str = "waiting",
    ) -> RoomInvitation:

        invitation = RoomInvitation(
            room_id=room_id,
            inviter_user_id=inviter_user_id,
            invitee_user_id=invitee_user_id,
            status=status,
        )
        db.session.add(invitation)
        db.session.commit()
        return invitation

    @staticmethod
    def list_rooms_for_user(user_id: int) -> List[Room]:
        stmt = (
            select(Room)
                .join(RoomMember)
                .where(RoomMember.user_id == user_id)
                .order_by(Room.created_at.desc())
        )
        return db.session.scalars(stmt).all()

    @staticmethod
    def list_invites_for_user(user_id: int) -> List[Room]:
        stmt = (
            select(Room)
            .join(RoomInvitation)
            .where(RoomInvitation.invitee_user_id == user_id)
            .order_by(Room.created_at.desc())
        )
        return db.session.scalars(stmt).all()

    @staticmethod
    def list_members(room_id: int) -> List[User]:
        stmt = (
            select(User)
                .join(RoomMember, RoomMember.user_id == User.id)
                .where(RoomMember.room_id == room_id)
                .order_by(User.username)
        )
        return db.session.scalars(stmt).all()

    @staticmethod
    def get_invitation(invitation_id: int) -> Optional[RoomInvitation]:
        return db.session.get(RoomInvitation, invitation_id)

    @staticmethod
    def get_room_with_members(room_id: int) -> Optional[Room]:

        stmt = (
            select(Room)
                .options(
                joinedload(Room.members).joinedload(RoomMember.user)
            )
                .where(Room.id == room_id)
        )
        return db.session.scalar(stmt)

    @staticmethod
    def respond_to_invitation(
            invitation_id: int, status: str
    ) -> RoomInvitation:
        inv = db.session.get(RoomInvitation, invitation_id)
        if inv is None:
            raise ValueError("Invitation not found")
        inv.status = status
        db.session.commit()
        return inv

    @staticmethod
    def list_invitations_for_user_by_status(user_id: int, status: str) -> List[RoomInvitation]:
        stmt = (
            select(RoomInvitation)
                .where(
                RoomInvitation.invitee_user_id == user_id,
                RoomInvitation.status == status,
            )
                .order_by(RoomInvitation.created_at.desc())
        )
        return db.session.scalars(stmt).all()

    @staticmethod
    def waiting_invitations_for_user_by_room(user_id: int, room_id: int) -> RoomInvitation:
        stmt = (
            select(RoomInvitation)
                .where(
                RoomInvitation.invitee_user_id == user_id,
                RoomInvitation.room_id == room_id,
            )
                .order_by(RoomInvitation.created_at.desc())
        )
        return db.session.scalars(stmt).first()