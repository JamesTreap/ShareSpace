from sqlalchemy import select, func
from sqlalchemy.orm import joinedload
from entities.finance import Bill, Payment
from entities import db
from typing import List, Optional
from datetime import datetime, date



class FinanceRepo:
    @staticmethod
    def get_bills_for_room(room_id: int) -> List[Bill]:
        stmt = (
            select(Bill)
            .where(Bill.room_id == room_id, Bill.scheduled_date <= db.func.now())
            .order_by(Bill.scheduled_date.desc())
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
    def get_bills_for_room_by_date(room_id: int, target_date: date) -> List[Bill]:
        stmt = (
            select(Bill)
                .where(
                Bill.room_id == room_id,
                func.date(Bill.scheduled_date) == target_date,
                Bill.scheduled_date <= datetime.utcnow()
            )
            .order_by(Bill.scheduled_date.desc())
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

    @staticmethod
    def create_payment(room_id: int, title: str, category: str, amount: float, payer_user_id: int, payee_user_id: int) -> Payment:
        payment = Payment(
            room_id=room_id,  # Assuming room_id is passed or set elsewhere
            title=title,
            category=category,
            amount=amount,
            payer_user_id=payer_user_id,
            payee_user_id=payee_user_id
        )
        db.session.add(payment)
        db.session.commit()
        return payment