# flask_api/entities/user.py
from werkzeug.security import generate_password_hash, check_password_hash
from sqlalchemy.orm import relationship, Mapped, mapped_column
from . import db, TimestampMixin
from typing import Optional, List

class User(db.Model, TimestampMixin):
    __tablename__ = "users"


    id:            Mapped[int]           = mapped_column(primary_key=True)
    username:      Mapped[str]           = mapped_column(db.String(50), unique=True, nullable=False)
    email:         Mapped[str]           = mapped_column(db.String(120), unique=True, nullable=False)  # NEW
    password_hash: Mapped[str]           = mapped_column(db.String(255), nullable=False)
    name:          Mapped[Optional[str]] = mapped_column(db.String(100))
    profile_picture_url: Mapped[Optional[str]] = mapped_column(db.Text)

def __repr__(self) -> str:
        return f"<User {self.username}>"
