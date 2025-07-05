from repository.finance_repo import FinanceRepo
from typing import List, Optional

class FinanceService:
    @staticmethod
    def get_room_financial_activity(room_id: int) -> List[dict]:
        bills = FinanceRepo.get_bills_for_room(room_id)
        payments = FinanceRepo.get_payments_for_room(room_id)

        merged = []

        for bill in bills:
            merged.append({
                "type": "bill",
                "id": bill.id,
                "title": bill.title,
                "amount": float(bill.amount),
                "category": bill.category,
                "payer_user_id": bill.payer_user_id,
                "status": bill.status,
                "scheduled_date": bill.scheduled_date.isoformat() if bill.scheduled_date else None,
                "created_at": bill.created_at.isoformat(),
            })

        for payment in payments:
            merged.append({
                "type": "payment",
                "id": payment.id,
                "amount": float(payment.amount),
                "payer_user_id": payment.payer_user_id,
                "payee_user_id": payment.payee_user_id,
                "created_at": payment.created_at.isoformat(),
            })

        merged.sort(key=lambda x: x["created_at"], reverse=True)
        return merged