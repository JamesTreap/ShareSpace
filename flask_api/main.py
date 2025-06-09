import jwt
from functools import wraps
from flask import Flask, request, jsonify
from pymongo import MongoClient
from dotenv import dotenv_values
from bson.objectid import ObjectId
import os

# Load environment variables
config = dotenv_values(".env")

# Connect to MongoDB
client = MongoClient(config["MONGO_URI"])
db = client[config["DATABASE_ENV"]]
users_collection = db["users"]

app = Flask(__name__)
app.config['SECRET_KEY'] = config.get("SECRET_KEY", "")  # Use env variable for secret key

def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = None
        
        # Check if token is passed in headers
        if 'Authorization' in request.headers:
            auth_header = request.headers['Authorization']
            try:
                # Expected format: "Bearer <token>"
                token = auth_header.split(" ")[1]
            except IndexError:
                return jsonify({'error': 'Invalid token format'}), 401
              
        if not token:
            return jsonify({'error': 'AuthToken is missing'}), 401
        
        try:
            # Decode the token
            token_data = jwt.decode(token, app.config['SECRET_KEY'], algorithms=['HS256'])

            if not token_data:
                return jsonify({'error': 'Invalid token - user not found'}), 401
        except jwt.ExpiredSignatureError:
            return jsonify({'error': 'Token has expired'}), 401
        except jwt.InvalidTokenError:
            return jsonify({'error': 'Invalid token'}), 401
        
        # Pass the current user to the route
        return f(token_data, *args, **kwargs)
    
    return decorated


@app.route('/create-user', methods=['POST'])
def create_user():
    data = request.get_json()
    if not data or 'username' not in data or 'password' not in data:
        return jsonify({'error': 'Invalid input'}), 400

    if users_collection.find_one({"username": data["username"]}):
        return jsonify({'error': 'User already exists'}), 409

    # TODO: Hash the password before storing
    users_collection.insert_one({
        "username": data["username"],
        "password": data["password"]
    })
    return jsonify(f"Successfully created user with username {data['username']}"), 201


@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    if not data or 'username' not in data or 'password' not in data:
        return jsonify({'error': 'Invalid input'}), 400

    user = users_collection.find_one({"username": data["username"]})
    if not user or user['password'] != data['password']:
        return jsonify({'error': 'Invalid username or password'}), 401

    token = jwt.encode({
        'username': user['username'] # no expiration
    }, app.config['SECRET_KEY'], algorithm='HS256')

    return jsonify({'token': f"{token}"}), 200


@app.route('/get-user/<user_id>', methods=['GET'])
@token_required
def get_user(token_data, user_id):
    user_data = users_collection.find_one({"username": user_id}, {"_id": 0})
    if not user_data:
        return jsonify({'error': 'User not found'}), 404
    return jsonify(user_data), 200

if __name__ == '__main__':
    app.run(debug=True)