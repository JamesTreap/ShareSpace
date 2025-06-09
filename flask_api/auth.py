import jwt
from flask import Blueprint, request, jsonify, current_app
from werkzeug.security import generate_password_hash, check_password_hash
from database import users_collection

auth_bp = Blueprint('auth', __name__, url_prefix='/auth')

@auth_bp.route('/create-user', methods=['POST'])
def create_user():
    data = request.get_json()
    if not data or 'username' not in data or 'password' not in data:
        return jsonify({'error': 'Invalid input'}), 400

    if users_collection.find_one({"username": data["username"]}):
        return jsonify({'error': 'User already exists'}), 409

    hashed_password = generate_password_hash(data["password"])
    users_collection.insert_one({
        "username": data["username"],
        "password": hashed_password
    })

    token = jwt.encode({
        'username': data["username"]
    }, current_app.config['SECRET_KEY'], algorithm='HS256')
    
    return jsonify({
        "message": f"Successfully created user with username {data['username']}",
        "token": token
    }), 201


@auth_bp.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    if not data or 'username' not in data or 'password' not in data:
        return jsonify({'error': 'Invalid input'}), 400

    user = users_collection.find_one({"username": data["username"]})
    if not user:
        return jsonify({'error': 'Invalid username or password'}), 401
    
    if not check_password_hash(user['password'], data['password']):
        return jsonify({'error': 'Invalid username or password'}), 401

    token = jwt.encode({
        'username': user['username'] # no expiration
    }, current_app.config['SECRET_KEY'], algorithm='HS256')

    return jsonify({'token': f"{token}"}), 200