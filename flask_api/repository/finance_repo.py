from sqlalchemy import select
from sqlalchemy.orm import joinedload
from entities.finance import Bill, Payment
from entities import db
from typing import List, Optional
from datetime import datetime



class FinanceRepo:
    @staticmethod
    def get_bills_for_room(room_id: int) -> List[Bill]:
        stmt = (
            select(Bill)
            .where(Bill.room_id == room_id, Bill.scheduled_date <= db.func.now())
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

    @staticmethod
    def create_bill(title: str, category: str, amount: float, payer_user_id: int, frequency: Optional[str], repeat: int, room_id: int, scheduled_date: datetime, meta_data=None) -> Bill:
        bill = Bill(
            title=title,
            category=category,
            amount=amount,
            room_id=room_id,
            payer_user_id=payer_user_id,
            frequency=frequency,
            repeat=repeat,
            scheduled_date=scheduled_date,
            meta_data=meta_data,
            status="waiting"
        )
        db.session.add(bill)
        db.session.commit()
        return bill