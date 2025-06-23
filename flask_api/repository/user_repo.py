from typing import Optional, List
from flask_api.entities.user import User

def get_by_id(user_id: int) -> Optional[User]:
    return User.query.get(user_id)

# def get_user_by_room(limit: int = 50, offset: int = 0, room_id) -> List[User]:
#     return (
#         User.query
#         .filter_by(is_active=True)
#         .order_by(User.created_at.desc())
#         .limit(limit)
#         .offset(offset)
#         .all()
#     )
