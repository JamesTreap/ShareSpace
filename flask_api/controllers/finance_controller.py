# pylint: disable=all
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
        amount=float(amount),
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


@finance_bp.route('/delete/bill/<bill_id>', methods=['DELETE'])
@token_required
def delete_bill(bill_id):
    user: User = g.current_user
    if not user:
        abort(404, description="User not found")

    FinanceService.delete_bill(user.id, int(bill_id))

    return {"message": "Bill deleted successfully"}, 200


@finance_bp.route('/delete/payment/<payment_id>', methods=['DELETE'])
@token_required
def delete_payment(payment_id):
    user: User = g.current_user
    if not user:
        abort(404, description="User not found")

    FinanceService.delete_payment(user.id, int(payment_id))

    return {"message": "Payment deleted successfully"}, 200


@finance_bp.route('/debug/clear_room_transactions/<room_id>', methods=['DELETE'])
def clear_room_transactions(room_id):
    """Delete all bills and payments for this room and clear debt data"""
    try:
        room_id_int = int(room_id)
    except ValueError:
        abort(400, "Invalid room ID")
    
    # Import the necessary models
    from entities.finance import Bill, Payment, FinanceSummary
    from entities import db
    
    # Delete all bills for this room
    bills_deleted = Bill.query.filter_by(room_id=room_id_int).delete()
    
    # Delete all payments for this room  
    payments_deleted = Payment.query.filter_by(room_id=room_id_int).delete()
    
    # Clear all debt summaries for this room
    summaries_deleted = FinanceSummary.query.filter_by(room_id=room_id_int).delete()
    
    # Commit the deletions
    db.session.commit()
    
    return {
        "message": f"All transactions cleared for room {room_id}",
        "bills_deleted": bills_deleted,
        "payments_deleted": payments_deleted, 
        "summaries_deleted": summaries_deleted
    }, 200