import jwt
from flask import Blueprint, request, jsonify, current_app, g
from werkzeug.security import generate_password_hash, check_password_hash
from repository.user_repo import UserRepo
from services.auth_service import AuthService

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


