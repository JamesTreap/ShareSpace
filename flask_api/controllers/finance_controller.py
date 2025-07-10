from flask import Blueprint, jsonify, abort, g, request
from utils import token_required
from entities.user import User
from services.finance_service import FinanceService, RoomService

finance_bp = Blueprint('finance', __name__, url_prefix='/finance')

# =============================================================================
# DUMMY API ENDPOINTS FOR BASIC SETUP
@finance_bp.route('/transaction_list/<room_id>', methods=['GET'])
def get_room_financial_activity(room_id):
    items = FinanceService.get_room_financial_activity(room_id)
    return jsonify(items), 200


@finance_bp.route('/create_bill/<room_id>', methods=['POST'])
@token_required
def create_bill(room_id):
    user: User = g.current_user
    if not user:
        abort(404, description="User not found")

    if not RoomService.validate_room_user(user.id, int(room_id)):
        abort(404, description="User does not belong to the room")

    data = request.get_json(silent=True) or {}
    title = data.get("title")
    category = data.get("category")
    users = data.get("users", [])
    frequency = data.get("frequency").strip()
    repeat = data.get("repeat", 0)
    payer_id = data.get("payer_id")
    amount = data.get("amount")

    try:
        repeat = int(repeat)
    except ValueError:
        abort(400, "Repeat must be an integer.")

    bill = FinanceService.create_bill_service(
        room_id=int(room_id),
        title=title,
        category=category,
        users=users,
        payer_id=int(payer_id),
        amount=int(amount),
        frequency=frequency,
        repeat=repeat
    )

    return {
               "message": "Bill created successfully",
               "bill_id": bill.id
           }, 200

@finance_bp.route('/pay_user/<room_id>', methods=['POST'])
@token_required
def pay_user(room_id):
    user: User = g.current_user
    if not user:
        abort(404, description="User not found")

    # Check if the user belongs to the room
    if not any(room.room_id == int(room_id) for room in user.rooms):
        abort(404, description="User does not belong to the room")

    # Get the input data from the request
    data = request.get_json(silent=True) or {}
    title = data.get("title")
    category = data.get("category")
    amount = data.get("amount")
    payer_id = data.get("payer_id")
    payee_id = data.get("payee_id")

    try:
        amount = float(amount)
    except ValueError:
        abort(400, "Amount must be a valid number.")

    if amount <= 0:
        abort(400, "Amount must be greater than 0.")

    # Call the service to create the payment
    payment = FinanceService.create_payment_service(
        room_id=int(room_id), title=title, category=category, amount=amount,
        payer_id=int(payer_id), payee_id=int(payee_id)
    )

    return {
        "message": "Payment created successfully",
        "payment_id": payment.id
    }, 200
