# flask_api/services/auth_service.py
import jwt
from flask import current_app
from werkzeug.security import generate_password_hash, check_password_hash
from typing import Optional, Tuple
from repository.user_repo import UserRepo
from entities.user import User


class AuthService:
    @staticmethod
    def create_user(username: str, password: str, email: Optional[str] = None) -> Optional[Tuple[str, str]]:
        if UserRepo.find_by_username(username):
            return None

        user = UserRepo.create(
            username=username,
            raw_password=password,
            email=email or f"{username}@example.com",
        )
        if user is None:
            return None

        token = jwt.encode(
            {"username": username},
            current_app.config["SECRET_KEY"],
            algorithm="HS256",
        )
        return user.username, token

    @staticmethod
    def authenticate_user(username: str, password: str) -> Optional[str]:
        user: User = UserRepo.find_by_username(username)
        if not user or not check_password_hash(user.password_hash, password):
            return None

        token = jwt.encode(
            {"username": username},
            current_app.config["SECRET_KEY"],
            algorithm="HS256",
        )
        return token
