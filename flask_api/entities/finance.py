# flask_api/entities/finance_controller.py
from sqlalchemy.dialects.postgresql import JSON, NUMERIC
from sqlalchemy.orm import relationship, Mapped, mapped_column
from . import db, TimestampMixin
from typing import Optional, List, Dict
from datetime import datetime

class Bill(db.Model, TimestampMixin):
    __tablename__ = "bills"

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    room_id:  Mapped[int] = mapped_column(db.ForeignKey("rooms.id"))
    amount:   Mapped[float] = mapped_column(NUMERIC(12, 2))
    title:    Mapped[str] = mapped_column(db.String(200))
    category: Mapped[str] = mapped_column(db.String(100))
    payer_user_id: Mapped[int] = mapped_column(db.ForeignKey("users.id"))
    meta_data: Mapped[Optional[dict]] = mapped_column(JSON)
    frequency: Mapped[Optional[str]] = mapped_column(db.String(50))
    repeat: Mapped[Optional[int]] = mapped_column(db.Integer, default=1)
    status: Mapped[str] = mapped_column(
        db.Enum("waiting", "created", name="bill_status"), default="waiting"
    )
    scheduled_date = mapped_column(db.Date)

    room  = relationship("Room", back_populates="bills")
    payer = relationship("User", back_populates="bills_paid")

class Payment(db.Model, TimestampMixin):
    __tablename__ = "payments"

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    room_id:       Mapped[int] = mapped_column(db.ForeignKey("rooms.id"))
    amount:        Mapped[float] = mapped_column(NUMERIC(12, 2))
    title: Mapped[str] = mapped_column(db.String(200))
    category: Mapped[str] = mapped_column(db.String(100))
    payer_user_id: Mapped[int] = mapped_column(db.ForeignKey("users.id"))
    payee_user_id: Mapped[int] = mapped_column(db.ForeignKey("users.id"))

    room  = relationship("Room", back_populates="payments")
    payer = relationship("User", back_populates="payments_sent",
                         foreign_keys=[payer_user_id])
    payee = relationship("User", back_populates="payments_recv",
                         foreign_keys=[payee_user_id])

class FinanceSummary(db.Model):

    __tablename__ = "finance_summaries"
    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    room_id: Mapped[int] = mapped_column(db.ForeignKey("rooms.id"))
    user_id: Mapped[int] = mapped_column(db.ForeignKey("users.id"))
    owes:  Mapped[dict] = mapped_column(JSON)
    debts: Mapped[dict] = mapped_column(JSON)
    updated_at = mapped_column(db.DateTime, default=datetime.utcnow,
                               onupdate=datetime.utcnow)

    __table_args__ = (
        db.UniqueConstraint("room_id", "user_id", name="uniq_room_user"),
    )
