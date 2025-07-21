import jwt
from flask import Blueprint, jsonify, abort, g, request
from werkzeug.security import generate_password_hash, check_password_hash
from repository.user_repo import UserRepo
from services.auth_service import AuthService
from services.user_service import UserService
from entities.user import User
from utils import token_required

auth_bp = Blueprint('auth', __name__, url_prefix='/auth')

@auth_bp.route('/create-user', methods=['POST'])
def create_user():
    data = request.get_json(silent=True) or {}
    username = data.get("username")
    password = data.get("password")
    email = data.get("email")

    if not username or not password or not email:
        return jsonify({"error": "username and password and email required"}), 400

    result = AuthService.create_user(username, password, email)
    if not result:
        return jsonify({"error": "User already exists"}), 409

    username, token = result
    return (
        jsonify(
            message=f"Successfully created user '{username}'",
            token=token,
        ),
        201,
    )
@auth_bp.route('/login', methods=['POST'])
def login():
    data = request.get_json(silent=True) or {}
    username = data.get("username")
    password = data.get("password")

    if not username or not password:
        return jsonify({'error': 'Invalid input'}), 400

    token = AuthService.authenticate_user(username, password)
    if not token:
        return jsonify({'error': 'Invalid username or password'}), 401

    return jsonify({'token': token}), 200

@auth_bp.route('/register-device', methods=['POST'])
@token_required
def register_device():
    user: User = g.current_user
    if not user:
        abort(404, description="User not found")
    data = request.get_json(silent=True) or {}
    device_token = data.get('device_token')
    if not device_token:
        return jsonify({'error': 'Missing device_token'}), 400

    success = AuthService.save_device_token(user.id, device_token)
    if success:
        return jsonify({'message': 'Device token registered successfully'}), 200
    else:
        return jsonify({'error': 'Failed to save device token'}), 500

