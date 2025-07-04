from flask import Blueprint, jsonify, abort

from flask_api.repository.user_repo import UserRepo
from flask_api.schemas.user_schemas import UserPublicSchema
from flask_api.services.user_service import UserService
from flask_api.utils import token_required

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

