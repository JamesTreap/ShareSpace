from flask_api.repository.user_repo import UserRepo
from flask_api.entities.user import User
from typing import Optional

class UserService:
    @staticmethod
    def get_user_by_id(user_id: int) -> Optional[User]:
        return UserRepo.find_by_id(user_id)