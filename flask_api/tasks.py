from flask import Blueprint, jsonify

tasks_bp = Blueprint('tasks', __name__, url_prefix='/tasks')

# =============================================================================
# DUMMY API ENDPOINTS FOR BASIC SETUP
@tasks_bp.route('/list/<room_id>', methods=['GET'])
def tasks_list(room_id):
    data = [
        {
            "id": "taskUUIDsdhjsdfhjsfdjk",
            "title": "walk the dog",
            "time": "1990-05-14T00:00:00Z",
            "description": "WHERE IS OMNIMAN",
            "statuses": {
                "roommate1id": "COMPLETE",
                "roommate2id": "TO-DO"
            }
        },
        {
            "id": "taskUUID002",
            "title": "vacuum the living room",
            "time": "2025-06-17T14:00:00Z",
            "description": "Especially under the couch.",
            "statuses": {
                "roommate1uuid": "IN-PROGRESS",
                "roommate3uuid": "TO-DO"
            }
        },
        {
            "id": "taskUUID003",
            "title": "clean the bathroom",
            "time": "2025-06-18T11:30:00Z",
            "description": "Scrub shower, toilet, and sink.",
            "statuses": {
                "roommate2uuid": "COMPLETE",
                "roommate3uuid": "COMPLETE"
            }
        },
        {
            "id": "taskUUID004",
            "title": "feed the cat",
            "time": "2025-06-19T07:00:00Z",
            "description": "Twice a day—don’t forget the treats.",
            "statuses": {
                "roommate1uuid": "COMPLETE"
            }
        },
        {
            "id": "taskUUID005",
            "title": "buy toilet paper",
            "time": "2025-06-20T17:00:00Z",
            "description": "We're down to one roll!",
            "statuses": {
                "roommate2uuid": "TO-DO",
                "roommate3uuid": "TO-DO"
            }
        }
        ]
    return jsonify(data), 200

# =============================================================================
