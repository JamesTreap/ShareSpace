import jwt
from flask import Blueprint, jsonify, abort, g, request
from werkzeug.security import generate_password_hash, check_password_hash
from repository.user_repo import UserRepo
from services.room_service import RoomService
from services.user_service import UserService
from entities.user import User
from utils import token_required
from datetime import date, timedelta, datetime
from services.calendar_service import CalendarService

calendar_bp = Blueprint('calendar', __name__, url_prefix='/calendar')

@calendar_bp.route('/<room_id>', methods=['POST'])
@token_required
def fetch_calendar(room_id):
    user: User = g.current_user
    if not user:
        abort(404, description="User not found")

    if not RoomService.validate_room_user(user.id, int(room_id)):
        abort(404, description="User does not belong to the room")


    data = request.get_json(silent=True) or {}
    date_str = data.get("date")

    if not date_str:
        return jsonify({"error": "Missing 'date' parameter"}), 400
    try:
        target_date = datetime.strptime(date_str, "%Y-%m-%d").date()
    except ValueError:
        return jsonify({"error": "Invalid date format. Use YYYY-MM-DD."}), 400

    result = CalendarService.get_schedule_for_date(room_id, target_date)
    return jsonify(result), 200