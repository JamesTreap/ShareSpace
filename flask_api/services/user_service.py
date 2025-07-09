from repository.user_repo import UserRepo
from entities.user import User
from typing import Optional
from flask import abort

class UserService:
    @staticmethod
    def get_user_by_id(user_id: int) -> Optional[User]:
        return UserRepo.find_by_id(user_id)

    @staticmethod
    def update_user_profile(user, username: str, password: str) -> User:
        if not username or not password:
            abort(400, "Username and password are required")

        updated_user = UserRepo.update_user_profile(user.id, username, password)
        if not updated_user:
            abort(404, "User not found")

        return updated_user


