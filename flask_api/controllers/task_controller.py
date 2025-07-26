from flask import Blueprint, jsonify, abort, g, request
from entities.user import User
from services.task_service import TaskService, RoomService
from utils import token_required
from repository.task_repo import TaskRepo

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

    if not RoomService.validate_room_user(user.id, int(room_id)):
        abort(404, description="User does not belong to the room")
    # Get the input data from the request
    data = request.get_json(silent=True) or {}
    title = data.get("title")
    description = data.get("description")
    deadline = data.get("date")
    assignees = data.get("assignees", [])
    frequency = (data.get("frequency")).strip()
    repeat = data.get("repeat", 0)

    try:
        repeat = int(repeat)
    except ValueError:
        abort(400, "Repeat must be an integer.")

    TaskService.create_task_service(
        int(room_id), title, description, frequency, repeat, deadline, assignees
    )

    return {
        "message": "Tasks created successfully"
    }, 200

@tasks_bp.route('/update_task/<task_id>', methods=['PATCH'])
@token_required
def update_task_details(task_id):
    user: User = g.current_user
    if not user:
        abort(404, description="User not found")

    task = TaskRepo.get_task_by_id(task_id)
    if not task:
        abort(404, description="Task not found.")

    if not RoomService.validate_room_user(user.id, int(task.room_id)):
        abort(404, description="User does not belong to the room")

    data = request.get_json(silent=True) or {}
    title = data.get("title")
    description = data.get("description")
    from datetime import datetime

    date_str = data.get("date")
    try:
        deadline = datetime.fromisoformat(date_str) if date_str else None
    except ValueError:
        abort(400, description="Invalid date format. Use ISO 8601 (e.g., '2024-12-10T08:24:00').")
    assignees = data.get("assignees", [])

    TaskService.update_task(task_id, title, description, deadline, assignees)

    return {
        "message": "Task updated successfully"
    }, 200

@tasks_bp.route('/delete/<task_id>', methods=['DELETE'])
@token_required
def delete_task(task_id):
    user: User = g.current_user
    if not user:
        abort(404, description="User not found")

    TaskService.delete_task(user.id, int(task_id))

    return {
        "message": "Task deleted successfully"
    }, 200
