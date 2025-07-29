# pylint: disable=all
from typing import List
from flask import Blueprint, jsonify, abort, g, request

from repository.user_repo import UserRepo
from schemas.user_schemas import UserPublicSchema
from services.user_service import UserService
from services.room_service import RoomService
from services.finance_service import FinanceService
from utils import token_required
from entities.user import User

users_bp = Blueprint('users', __name__, url_prefix='/users')
user_schema = UserPublicSchema(many=False)

# =============================================================================
# DUMMY API ENDPOINTS FOR BASIC SETUP
@users_bp.route('/user_details/<user_id>', methods=['GET'])
def user_details_by_id(user_id):
    print(user_id)
    user = UserService.get_user_by_id(user_id)
    if not user:
        abort(404, description="User not found")

    return jsonify(user_schema.dump(user)), 200

@users_bp.route('/update_profile', methods=['PATCH'])
@token_required
def update_profile():
    user: User = g.current_user
    if not user:
        abort(404, description="User not found")

    data = request.get_json(silent=True) or {}
    username = (data.get("username") or "").strip()
    name = (data.get("name") or "").strip()
    profile_picture_url = (data.get("profile_picture_url") or "").strip()

    UserService.update_user_profile(user, username, name, profile_picture_url)
    return {"message": "Profile updated successfully"}, 200

@users_bp.route('/user_details', methods=['GET'])
@token_required
def user_details():
    user = g.current_user
    if not user:
        abort(404, description="User not found")

    # Serialize the user object into a dictionary
    user_data = user_schema.dump(user)
    
    # Add a print statement to inspect the data in your terminal
    print(f"🔍 User Details Data: {user_data}")

    # Return the JSON response
    return jsonify(user_data), 200

@users_bp.route('/room_members_with_debts/<room_id>', methods=['GET'])
@token_required
def get_room_members_with_debts(room_id):
    """Get all room members with their debt information"""
    user: User = g.current_user
    if not user:
        abort(404, description="User not found")

    try:
        room_id_int = int(room_id)
    except ValueError:
        abort(400, description="Invalid room ID")

    print(f"🔍 DEBUG: User {user.id} requesting room {room_id_int}")

    # Validate user belongs to room
    if not RoomService.validate_room_user(user.id, room_id_int):
        print(f"🔍 DEBUG: User {user.id} does NOT belong to room {room_id_int}")
        abort(403, description="User does not belong to this room")
    
    print(f"🔍 DEBUG: User {user.id} belongs to room {room_id_int}")

    # Let FinanceService handle all the debt logic
    members_with_debts = FinanceService.get_room_members_with_debts(room_id_int)
    
    print(f"🔍 DEBUG: Got {len(members_with_debts) if members_with_debts else 0} members")
    print(f"🔍 DEBUG: Members data: {members_with_debts}")
    
    if not members_with_debts:
        print(f"🔍 DEBUG: No members found, returning 404")
        abort(404, description="Room not found")

    print(f"🔍 DEBUG: Returning {len(members_with_debts)} members")
    return jsonify(members_with_debts), 200


@users_bp.route('/cleanup_room_debts/<room_id>', methods=['POST'])
# @token_required  # Comment this out temporarily
def cleanup_room_debts(room_id):
    # user: User = g.current_user  # Comment this out too
    # if not user:
    #     abort(404, description="User not found")

    try:
        room_id_int = int(room_id)
    except ValueError:
        abort(400, description="Invalid room ID")

    # Skip user validation for now
    # if not RoomService.validate_room_user(user.id, room_id_int):
    #     abort(403, description="User does not belong to this room")

    FinanceService.cleanup_debt_data(room_id_int)
    
    return {"message": f"Cleaned up debt data for room {room_id_int}"}, 200