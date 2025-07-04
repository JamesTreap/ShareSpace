from flask import Blueprint, jsonify

from flask_api.services.task_service import TaskService

tasks_bp = Blueprint('tasks', __name__, url_prefix='/tasks')

# =============================================================================
# DUMMY API ENDPOINTS FOR BASIC SETUP
@tasks_bp.route('/list/<room_id>', methods=['GET'])
def tasks_list(room_id):
    enriched_tasks = TaskService.get_enriched_tasks_for_room(room_id)
    return jsonify(enriched_tasks), 200

# =============================================================================
