# flask_api/entities/user.py
from werkzeug.security import generate_password_hash, check_password_hash
from sqlalchemy.orm import relationship, Mapped, mapped_column
from . import db, TimestampMixin
from typing import Optional, List

class User(db.Model, TimestampMixin):
    __tablename__ = "users"

    id:            Mapped[int] = mapped_column(primary_key=True)
    username:      Mapped[str] = mapped_column(db.String(50), unique=True, nullable=False)
    email:         Mapped[str] = mapped_column(db.String(120), unique=True, nullable=False)
    password_hash: Mapped[str] = mapped_column(db.String(255), nullable=False)
    name:          Mapped[Optional[str]] = mapped_column(db.String(100))
    profile_picture_url: Mapped[Optional[str]] = mapped_column(db.Text)

    # ───── Many-to-one / one-to-many collections ─────
    rooms: Mapped[List["RoomMember"]] = relationship(back_populates="user")


    invitations_sent: Mapped[List["RoomInvitation"]] = relationship(
        back_populates="inviter",
        foreign_keys="[RoomInvitation.inviter_user_id]",
    )
    invitations_received: Mapped[List["RoomInvitation"]] = relationship(
        back_populates="invitee",
        foreign_keys="[RoomInvitation.invitee_user_id]",
    )

    tasks:        Mapped[List["TaskUser"]]  = relationship(back_populates="user")
    bills_paid:   Mapped[List["Bill"]]      = relationship(back_populates="payer")
    payments_sent: Mapped[List["Payment"]]  = relationship(
        back_populates="payer",
        foreign_keys="[Payment.payer_user_id]",
    )
    payments_recv: Mapped[List["Payment"]]  = relationship(
        back_populates="payee",
        foreign_keys="[Payment.payee_user_id]",
    )

    def __repr__(self) -> str:
        return f"<User {self.username}>"