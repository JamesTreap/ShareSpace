from flask import Blueprint, jsonify
from database import users_collection
from utils import token_required

users_bp = Blueprint('users', __name__, url_prefix='/users')

# =============================================================================
# DUMMY API ENDPOINTS FOR BASIC SETUP
@users_bp.route('/user_details/<user_id>', methods=['GET'])
def user_details(user_id):
    data = {
        "username": user_id,
        "password": "password",
        "birthday": "1990-05-14T00:00:00Z",
        "phone number": "666-666-6666",
    }

    return jsonify(data), 200

# =============================================================================

@users_bp.route('/get-user/<user_id>', methods=['GET'])
@token_required
def get_user(token_data, user_id):
    user_data = users_collection.find_one({"username": user_id}, {"_id": 0})
    if not user_data:
        return jsonify({'error': 'User not found'}), 404
    return jsonify(user_data), 200
