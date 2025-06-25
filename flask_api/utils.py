from functools import wraps
import jwt
from flask import request, jsonify, current_app, g
from flask_api.repository.user_repo import UserRepo

def token_required(view_func):
    @wraps(view_func)
    def wrapped(*args, **kwargs):
        auth_header = request.headers.get("Authorization", "")
        parts = auth_header.split()

        if len(parts) == 2 and parts[0].lower() == "bearer":
            token = parts[1]
        elif len(parts) == 1:
            token = parts[0]
        else:
            return jsonify(error="Invalid token format"), 401
        try:
            payload = jwt.decode(
                token,
                current_app.config["SECRET_KEY"],
                algorithms=["HS256"],
            )
        except jwt.ExpiredSignatureError:
            return jsonify(error="Token expired"), 401
        except jwt.InvalidTokenError:
            return jsonify(error="Invalid token"), 401

        user = UserRepo.find_by_username(payload.get("username"))
        if user is None:
            return jsonify(error="User not found"), 401
        g.current_user = user

        return view_func(*args, **kwargs)

    return wrapped
