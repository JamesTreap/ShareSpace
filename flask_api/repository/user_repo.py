# flask_api/repository/user_repo.py
from __future__ import annotations

from typing import Optional, Dict, Any

from werkzeug.security import generate_password_hash, check_password_hash

from flask_api.entities import db
from flask_api.entities.user import User


class UserRepo:
    @staticmethod
    def find_by_username(
        username: str,
        exclude_id: bool = False,
        include_password_hash: bool = False,
    ) -> Optional[Dict[str, Any]]:

        user: User | None = User.query.filter_by(username=username).first()
        if user is None:
            return None

        doc = {
            "id":            user.id,
            "username":      user.username,
            "email":         user.email,
            "name":          user.name,
            "profile_pic":   user.profile_picture_url,
            "created_at":    user.created_at,
        }
        if include_password_hash:
            doc["password_hash"] = user.password_hash
        if exclude_id:
            doc.pop("id", None)
        return doc


    @staticmethod
    def create(
        username: str,
        raw_password: str,
        email: str,
        name: str | None = None,
        profile_picture_url: str | None = None,
    ) -> User | None:

        if User.query.filter_by(username=username).first():
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


