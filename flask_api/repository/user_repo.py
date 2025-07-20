# flask_api/repos/user_repo.py
from __future__ import annotations

from typing import Optional
from sqlalchemy import select
from werkzeug.security import generate_password_hash

from entities import db
from entities.user import User


class UserRepo:

    @staticmethod
    def find_by_id(user_id: int) -> Optional[User]:
        return db.session.get(User, user_id)

    @staticmethod
    def find_by_username(username: str) -> Optional[User]:
        username = username.strip()
        return db.session.scalar(
            select(User).where(User.username == username)
        )

    @staticmethod
    def get_users_by_ids(user_ids: list[int]) -> list[User]:
        if not user_ids:
            return []
        return db.session.scalars(
            select(User).where(User.id.in_(user_ids))
        ).all()

    @staticmethod
    def create(
        username: str,
        raw_password: str,
        email: str,
        name: str | None = None,
        profile_picture_url: str | None = None,
    ) -> Optional[User]:
        if UserRepo.find_by_username(username):
            return None

        user = User(
            username=username,
            email=email,
            password_hash=generate_password_hash(raw_password),
            name=name or username,
            profile_picture_url=profile_picture_url,
        )
        db.session.add(user)
        db.session.commit()
        return user

    @staticmethod
    def update_user_profile(user_id: int, username: str, password: str) -> Optional[User]:
        user = db.session.get(User, user_id)
        if not user:
            return None

        user.username = username
        user.password_hash = generate_password_hash(password)
        db.session.commit()
        return user

    @staticmethod
    def update_user_profile(user_id: int, username: str, name: str, profile_picture_url: str) -> Optional[User]:
        user = db.session.get(User, user_id)
        if not user:
            return None

        user.username = username
        user.name = name
        user.profile_picture_url = profile_picture_url
        db.session.commit()
        return user