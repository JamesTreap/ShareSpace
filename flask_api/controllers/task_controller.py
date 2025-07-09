from flask import Blueprint, jsonify, abort, g, request
from entities.user import User
from services.task_service import TaskService
from utils import token_required

tasks_bp = Blueprint('tasks', __name__, url_prefix='/tasks')

# =============================================================================
# DUMMY API ENDPOINTS FOR BASIC SETUP
@tasks_bp.route('/list/<room_id>', methods=['GET'])
def tasks_list(room_id):
    enriched_tasks = TaskService.get_enriched_tasks_for_room(room_id)
    return jsonify(enriched_tasks), 200

@tasks_bp.route('/create_task/<room_id>', methods=['POST'])
@token_required
def task_details(room_id):

    user: User = g.current_user
    if not user:
        abort(404, description="User not found")

    
    if not any(room.room_id == int(room_id) for room in user.rooms):
        abort(404, description="User does not belong to the room")
    # Get the input data from the request
    data = request.get_json(silent=True) or {}
    title = data.get("title")
    description = data.get("description")
    deadline = data.get("date")
    assignee = data.get("assignee", [])
    frequency = (data.get("frequency") or "").strip()
    repeat = data.get("repeat", 0)
    try:
        repeat = int(repeat)
    except ValueError:
        abort(400, "Repeat must be an integer.")


    TaskService.create_task_service(int(room_id), title, description, frequency, repeat, deadline, assignee)

    return {
        "message": "Tasks created successfully"
    }, 201
# =============================================================================
