from flask import Blueprint, jsonify


finance_bp = Blueprint('finance', __name__, url_prefix='/finance')

# =============================================================================
# DUMMY API ENDPOINTS FOR BASIC SETUP
@finance_bp.route('/transaction_list/<room_id>', methods=['GET'])
def get_transactions(room_id):
    data = [
        {
            "type": "bill",
            "id": "billuuid1001",
            "title": "June Rent",
            "time": "2025-06-01T00:00:00Z",
            "category": "rent",
            "breakdown": {
                "roommate1uuid": "500.00",
                "roommate2uuid": "500.00"
            }
        },
        {
            "type": "bill",
            "id": "billuuid1002",
            "title": "Internet Bill",
            "time": "2025-06-05T00:00:00Z",
            "category": "utilities",
            "breakdown": {
                "roommate1uuid": "30.00",
                "roommate2uuid": "30.00",
                "roommate3uuid": "30.00"
            }
        },
        {
            "type": "payment",
            "id": "paymentuuid2001",
            "time": "2025-06-07T00:00:00Z",
            "from": "roommate1uuid",
            "recipient": "roommate3uuid"
        },
        {
            "type": "bill",
            "id": "billuuid1003",
            "title": "Groceries",
            "time": "2025-06-10T00:00:00Z",
            "category": "groceries",
            "breakdown": {
                "roommate2uuid": "75.00",
                "roommate3uuid": "75.00"
            }
        },
        {
            "type": "payment",
            "id": "paymentuuid2002",
            "time": "2025-06-12T00:00:00Z",
            "from": "roommate3uuid",
            "recipient": "roommate2uuid"
        }
    ]
    return jsonify(data), 200



# =============================================================================
