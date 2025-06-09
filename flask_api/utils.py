
import jwt
from functools import wraps
from flask import request, jsonify, current_app

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
            token_data = jwt.decode(token, current_app.config['SECRET_KEY'], algorithms=['HS256'])

            if not token_data:
                return jsonify({'error': 'Invalid token - user not found'}), 401
        except jwt.ExpiredSignatureError:
            return jsonify({'error': 'Token has expired'}), 401
        except jwt.InvalidTokenError:
            return jsonify({'error': 'Invalid token'}), 401
        
        # Pass the current user to the route
        return f(token_data, *args, **kwargs)
    
    return decorated