from repository.finance_repo import FinanceRepo
from repository.room_repo import RoomRepo
from typing import List, Optional
from flask import abort
from datetime import datetime, timedelta, date
from entities.finance import Bill
from services.room_service import RoomService


class FinanceService:

    @staticmethod
    def get_room_bills_by_date(room_id: int, target_date: date) -> List[dict]:
        bills = FinanceRepo.get_bills_for_room_by_date(room_id, target_date)
        result = []
        for bill in bills:
            result.append({
                "type": "bill",
                "id": bill.id,
                "title": bill.title,
                "amount": float(bill.amount),
                "category": bill.category,
                "payer_user_id": bill.payer_user_id,
                "deadline": bill.deadline.isoformat() if bill.deadline else None,
                "scheduled_date": bill.scheduled_date.isoformat() if bill.scheduled_date else None,
                "created_at": bill.created_at.isoformat(),
            })
        return result

    @staticmethod
    def get_room_financial_activity(room_id: int) -> List[dict]:
        bills = FinanceRepo.get_bills_for_room(room_id)
        payments = FinanceRepo.get_payments_for_room(room_id)

        merged = []

        for bill in bills:
            scheduled_sort_key = bill.scheduled_date or bill.created_at
            merged.append({
                "type": "bill",
                "id": bill.id,
                "title": bill.title,
                "amount": float(bill.amount),
                "category": bill.category,
                "payer_user_id": bill.payer_user_id,
                "deadline": bill.deadline.isoformat() if bill.deadline else None,
                "scheduled_date": bill.scheduled_date.isoformat() if bill.scheduled_date else None,
                "created_at": bill.created_at.isoformat(),
                "_sort_key": scheduled_sort_key
            })

        for payment in payments:
            merged.append({
                "type": "payment",
                "id": payment.id,
                "amount": float(payment.amount),
                "payer_user_id": payment.payer_user_id,
                "payee_user_id": payment.payee_user_id,
                "created_at": payment.created_at.isoformat(),
                "_sort_key": payment.created_at
            })

        merged.sort(key=lambda x: x["_sort_key"], reverse=True)

        for item in merged:
            item.pop("_sort_key", None)

        return merged

    @staticmethod
    def validate_users(users: Optional[List[dict]], room_id: int) -> List[tuple]:
        if not users:
            abort(400, "At least one user is required.")

        validated_users = []
        for user in users:
            user_id = user.get("user_id")
            amount_due = user.get("amount_due")

            if not user_id or amount_due is None:
                abort(400, "Each user must include both 'user_id' and 'amount_due'.")

            try:
                amount_due = float(amount_due)
            except ValueError:
                abort(400, f"Invalid amount_due value '{amount_due}', it must be a number.")
            if not RoomService.validate_room_user(user_id, room_id):
                abort(404, "Not all users belong to the specified room.")
            validated_users.append((user_id, amount_due))

        return validated_users

    @staticmethod
    def validate_input_create_bill(title: Optional[str], category: Optional[str], frequency: Optional[str], repeat: int):
        if not title:
            abort(400, "Title is required.")
        if not category:
            abort(400, "Category is required.")
        if frequency and frequency[-1] not in ['d', 'w', 'm']:
            abort(400, "Frequency must be in the format like '1d', '2w', or '3m'.")
        if frequency and not repeat:
            abort(400, "Repeat is required and must be a number.")

        freq_value, unit = None, None
        if frequency:
            try:
                freq_value = int(frequency[:-1])
                freq_unit = frequency[-1]
                if freq_unit == 'd':
                    unit = 'days'
                elif freq_unit == 'w':
                    unit = 'weeks'
                elif freq_unit == 'm':
                    unit = 'months'
                else:
                    raise ValueError("Invalid frequency unit")
            except ValueError:
                abort(400, "Invalid frequency value or unit.")

        return freq_value, unit

    @staticmethod
    def create_bill_service(room_id: int, title: Optional[str], category: Optional[str], users: List[dict],
                            payer_id: int, amount: int, frequency: Optional[str], repeat: int):
        print(payer_id)
        if not RoomService.validate_room_user(payer_id, room_id):
            print("kir")
            abort(404, "Not all users belong to the specified room.")


        freq_value, unit = FinanceService.validate_input_create_bill(title, category, frequency, repeat)
        user_data = FinanceService.validate_users(users, room_id)
        scheduled_dates = FinanceService.calculate_scheduled_dates(freq_value, unit, repeat)

        if amount != sum([amount_due for _, amount_due in user_data]):
            abort(400, "amount should be equal to the sum of all users' amounts due.")

        bills = []
        for scheduled_date in scheduled_dates:
            bill = FinanceRepo.create_bill(
                title=title,
                category=category,
                amount=amount,
                payer_user_id=payer_id,
                frequency=frequency,
                repeat=repeat,
                room_id=room_id,
                scheduled_date=scheduled_date,
                meta_data={
                    "users": [{"user_id": user_id, "amount_due": amount_due} for user_id, amount_due in user_data]}
            )
            bills.append(bill)

        return bills[0]

    @staticmethod
    def calculate_scheduled_dates(freq_value: int, unit: str, repeat: int) -> List[datetime]:
        scheduled_dates = []
        now = datetime.now()
        scheduled_dates.append(now)
        if repeat > 0:
            for i in range(1, repeat):
                scheduled_dates.append(now + timedelta(**{unit: freq_value * i}))

        return scheduled_dates

    @staticmethod
    def create_payment_service(room_id: int, title: str, category: str, amount: float, payer_id: int, payee_id: int):

        if not RoomService.validate_room_users([payee_id, payer_id], room_id):
            abort(404, "Not all users belong to the specified room.")

        # Create the payment record
        payment = FinanceRepo.create_payment(
            room_id=room_id,
            title=title,
            category=category,
            amount=amount,
            payer_user_id=payer_id,
            payee_user_id=payee_id,
        )

        return payment

    @staticmethod
    def delete_bill(user_id: int, bill_id: int):
        if not FinanceService.is_user_in_room_of_bill(user_id, bill_id):
            abort(403, "User does not belong to the room.")
        FinanceRepo.delete_bill(bill_id)

    @staticmethod
    def delete_payment(user_id: int, payment_id: int):
        if not FinanceService.is_user_in_room_of_payment(user_id, payment_id):
            abort(403, "User does not belong to the room.")
        FinanceRepo.delete_payment(payment_id)

    @staticmethod
    def is_user_in_room_of_bill(user_id: int, bill_id: int) -> bool:
        room_id = FinanceRepo.find_bill_by_id(bill_id).room_id
        if room_id is None:
            return False
        return RoomService.validate_room_user(user_id, room_id)

    @staticmethod
    def is_user_in_room_of_payment(user_id: int, payment_id: int) -> bool:
        room_id = FinanceRepo.find_payment_by_id(payment_id).room_id
        if room_id is None:
            return False
        return RoomRepo.validate_room_user(user_id, room_id)