import jwt
from functools import wraps
from flask import request, jsonify, current_app

def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = None
        
        # Debug: Print all headers
        print("=== DEBUG HEADERS ===")
        for header, value in request.headers:
            print(f"{header}: {value}")
        print("====================")
        
        # Check if token is passed in headers
        if 'Authorization' in request.headers:
            auth_header = request.headers['Authorization']
            print(f"Auth header found: {auth_header}")
            try:
                # Expected format: "Bearer <token>"
                token = auth_header.split(" ")[1]
                print(f"Extracted token: {token[:50]}...")
            except IndexError:
                print("Error: Invalid token format")
                return jsonify({'error': 'Invalid token format'}), 401
        else:
            print("No Authorization header found!")
              
        if not token:
            print("Error: No token found")
            return jsonify({'error': 'AuthToken is missing'}), 401
        
        try:
            # Decode the token
            print(f"Trying to decode with secret: {current_app.config['SECRET_KEY']}")
            token_data = jwt.decode(token, current_app.config['SECRET_KEY'], algorithms=['HS256'])
            print(f"Token decoded successfully: {token_data}")
            if not token_data:
                return jsonify({'error': 'Invalid token - user not found'}), 401
        except jwt.ExpiredSignatureError:
            print("Error: Token expired")
            return jsonify({'error': 'Token has expired'}), 401
        except jwt.InvalidTokenError as e:
            print(f"Error: Invalid token - {e}")
            return jsonify({'error': 'Invalid token'}), 401
        
        # Pass the current user to the route
        return f(token_data, *args, **kwargs)
    
    return decorated