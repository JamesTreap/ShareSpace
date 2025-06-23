import jwt
from flask import Blueprint, request, jsonify, current_app
from werkzeug.security import generate_password_hash, check_password_hash

from flask_api.repository.user_repo import UserRepo
auth_bp = Blueprint('auth', __name__, url_prefix='/auth')

@auth_bp.route('/create-user', methods=['POST'])
def create_user():
    data = request.get_json(silent=True) or {}
    username = data.get("username")
    raw_password = data.get("password")
    email = data.get("email")

    if not username or not raw_password:
        return jsonify({"error": "username and password required"}), 400

    if UserRepo.find_by_username(username):
        return jsonify({"error": "User already exists"}), 409

    user = UserRepo.create(
        username=username,
        raw_password=raw_password,
        email=email or f"{username}@example.com",
    )
    if user is None:
        return jsonify({"error": "User already exists"}), 409

    token = jwt.encode(
        {"username": username},
        current_app.config["SECRET_KEY"],
        algorithm="HS256",
    )

    return (
        jsonify(
            message=f"Successfully created user '{username}'",
            token=token,
        ),
        201,
    )
@auth_bp.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    if not data or 'username' not in data or 'password' not in data:
        return jsonify({'error': 'Invalid input'}), 400

    user = UserRepo.find_by_username({"username": data["username"]})
    if not user:
        return jsonify({'error': 'Invalid username or password'}), 401

    if not check_password_hash(user['password'], data['password']):
        return jsonify({'error': 'Invalid username or password'}), 401

    token = jwt.encode({
        'username': user['username'] # no expiration
    }, current_app.config['SECRET_KEY'], algorithm='HS256')

    return jsonify({'token': f"{token}"}), 200