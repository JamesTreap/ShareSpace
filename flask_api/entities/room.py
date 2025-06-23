# flask_api/entities/room.py
from sqlalchemy.orm import relationship, Mapped, mapped_column
from . import db, TimestampMixin
from typing import Optional, List, Dict

class Room(db.Model, TimestampMixin):
    __tablename__ = "rooms"

    id: Mapped[int]   = mapped_column(primary_key=True)
    name: Mapped[str] = mapped_column(db.String(100), nullable=False)
    picture_url: Mapped[Optional[str]] = mapped_column(db.Text)

    members   = relationship("RoomMember", back_populates="room")
    tasks     = relationship("Task", back_populates="room")
    bills     = relationship("Bill", back_populates="room")
    payments  = relationship("Payment", back_populates="room")
    invites   = relationship("RoomInvitation", back_populates="room")

class RoomMember(db.Model, TimestampMixin):
    __tablename__ = "room_members"
    id: Mapped[int] = mapped_column(primary_key=True)
    room_id: Mapped[int] = mapped_column(db.ForeignKey("rooms.id"), index=True)
    user_id: Mapped[int] = mapped_column(db.ForeignKey("users.id"), index=True)

    room = relationship("Room", back_populates="members")
    user = relationship("User", back_populates="rooms")

class RoomInvitation(db.Model, TimestampMixin):
    __tablename__ = "room_invitations"

    id:              Mapped[int] = mapped_column(primary_key=True)
    room_id:         Mapped[int] = mapped_column(db.ForeignKey("rooms.id"))
    inviter_user_id: Mapped[int] = mapped_column(db.ForeignKey("users.id"))
    invitee_user_id: Mapped[int] = mapped_column(db.ForeignKey("users.id"))
    status: Mapped[str] = mapped_column(
        db.Enum("waiting", "accepted", "declined", name="inv_status"),
        default="waiting",
    )

    room    = relationship("Room", back_populates="invites")

    inviter = relationship(
        "User",
        back_populates="invitations_sent",
        foreign_keys=[inviter_user_id],
    )
    invitee = relationship(
        "User",
        back_populates="invitations_received",
        foreign_keys=[invitee_user_id],
    )