# flask_api/entities/task.py
from datetime import datetime
from sqlalchemy.orm import relationship, Mapped, mapped_column
from . import db, TimestampMixin
from typing import Optional, List, Dict
from sqlalchemy import Boolean

class Task(db.Model, TimestampMixin):
    __tablename__ = "tasks"

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    room_id: Mapped[int] = mapped_column(db.ForeignKey("rooms.id"))
    title: Mapped[str] = mapped_column(db.String(200))
    description: Mapped[Optional[str]] = mapped_column(db.Text)
    frequency: Mapped[Optional[str]] = mapped_column(db.String(50))
    repeat: Mapped[Optional[int]] = mapped_column(db.Integer, default=1)
    deadline: Mapped[Optional[datetime]] = mapped_column(db.DateTime)

    scheduled_date: Mapped[Optional[datetime]] = mapped_column(db.DateTime)
    notified: Mapped[bool] = mapped_column(Boolean, default=False)

    room  = relationship("Room", back_populates="tasks")
    users = relationship(
        "TaskUser",
        back_populates="task",
        cascade="all, delete-orphan",
        passive_deletes=True
    )


class TaskUser(db.Model, TimestampMixin):
    __tablename__ = "task_users"

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    task_id: Mapped[int] = mapped_column(db.ForeignKey("tasks.id", ondelete="CASCADE"))
    user_id: Mapped[int] = mapped_column(db.ForeignKey("users.id"))
    status: Mapped[str] = mapped_column(
        db.Enum("todo", "in-progress", "complete", name="taskuser_status"),
        default="todo",
    )

    task = relationship("Task", back_populates="users")
    user = relationship("User", back_populates="tasks")
