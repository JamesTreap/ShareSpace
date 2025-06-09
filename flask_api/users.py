from flask import Blueprint, jsonify
from database import users_collection
from utils import token_required

users_bp = Blueprint('users', __name__, url_prefix='/users')

@users_bp.route('/get-user/<user_id>', methods=['GET'])
@token_required
def get_user(token_data, user_id):
    user_data = users_collection.find_one({"username": user_id}, {"_id": 0})
    if not user_data:
        return jsonify({'error': 'User not found'}), 404
    return jsonify(user_data), 200
