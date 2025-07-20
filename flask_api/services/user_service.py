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
    
    @staticmethod
    def update_user_profile(user: User, username: str, name: str, profile_picture_url: str) -> User:
        if not username:
            abort(400, "Username is required")

        other_user = UserRepo.find_by_username(username)

        if other_user and other_user.id != user.id:
            print("Username already exists")
            abort(400, "username already taken") 

        updated_user = UserRepo.update_user_profile(
            user.id, username, name, profile_picture_url
        )
        if not updated_user:
            abort(404, "User not found")

        return updated_user


