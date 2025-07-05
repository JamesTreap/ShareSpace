from flask import Blueprint, jsonify, abort

from repository.user_repo import UserRepo
from schemas.user_schemas import UserPublicSchema
from services.user_service import UserService
from utils import token_required

users_bp = Blueprint('users', __name__, url_prefix='/users')
user_schema = UserPublicSchema(many=True)
# =============================================================================
# DUMMY API ENDPOINTS FOR BASIC SETUP
@users_bp.route('/user_details/<user_id>', methods=['GET'])
def user_details(user_id):
    user = UserService.get_user_by_id(user_id)
    if not user:
        abort(404, description="User not found")

    return jsonify(user_schema.dump(user)), 200

