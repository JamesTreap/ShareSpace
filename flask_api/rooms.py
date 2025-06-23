from flask import Blueprint, jsonify


rooms_bp = Blueprint('room', __name__, url_prefix='/rooms')

# =============================================================================
# DUMMY API ENDPOINTS FOR BASIC SETUP
@rooms_bp.route('/get_rooms', methods=['GET'])
def get_rooms():
    data = {
        "joinedRooms": [
            "roomUUID1",
            "roomUUID2",
            "roomUUID3"
        ]
    }
    return jsonify(data), 200

@rooms_bp.route('/room_invites', methods=['GET'])
def room_invites():
    data = {
        "roomInvites": [
            "roomUUID5",
            "roomUUID45",
            "roomUUID2"
        ]
    }
    return jsonify(data), 200

@rooms_bp.route('/get_roommates/<room_id>', methods=['GET'])
def get_roomates(room_id):
    data = {
	    "roommates": [
            {
                "username": "alice123" 
            },
            {
                "username": "johnpork"
            },
            {
                "username": "cookieWookie"
            }
        ]
    }
    return jsonify(data), 200


# =============================================================================
