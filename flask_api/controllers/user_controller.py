from flask import Blueprint, jsonify, abort, g, request

from repository.user_repo import UserRepo
from schemas.user_schemas import UserPublicSchema
from services.user_service import UserService
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

    return jsonify(user_schema.dump(user)), 200


