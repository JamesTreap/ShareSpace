from sqlalchemy import select
from sqlalchemy.orm import joinedload
from entities.finance import Bill, Payment
from entities import db
from typing import List, Optional



class FinanceRepo:
    @staticmethod
    def get_bills_for_room(room_id: int) -> List[Bill]:
        stmt = (
            select(Bill)
            .where(Bill.room_id == room_id)
            .order_by(Bill.created_at.desc())
            .options(joinedload(Bill.payer))
        )
        return db.session.scalars(stmt).all()

    @staticmethod
    def get_payments_for_room(room_id: int) -> List[Payment]:
        stmt = (
            select(Payment)
            .where(Payment.room_id == room_id)
            .order_by(Payment.created_at.desc())
            .options(
                joinedload(Payment.payer),
                joinedload(Payment.payee)
            )
        )
        return db.session.scalars(stmt).all()
