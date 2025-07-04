from flask import Blueprint, jsonify

from flask_api.services.finance_service import FinanceService

finance_bp = Blueprint('finance', __name__, url_prefix='/finance')

# =============================================================================
# DUMMY API ENDPOINTS FOR BASIC SETUP
@finance_bp.route('/transaction_list/<room_id>', methods=['GET'])
def get_room_financial_activity(room_id):
    items = FinanceService.get_room_financial_activity(room_id)
    return jsonify(items), 200



# =============================================================================
