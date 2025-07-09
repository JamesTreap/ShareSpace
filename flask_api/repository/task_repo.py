from sqlalchemy import select
from entities.task import Task, TaskUser
from entities import db
from typing import List, Optional
from datetime import datetime

class TaskRepo:

    @staticmethod
    def get_tasks_for_room(room_id: int) -> List[Task]:
        stmt = (
            select(Task)
                .where(Task.room_id == room_id)
                .order_by(Task.deadline)
        )
        return db.session.scalars(stmt).all()

    @staticmethod
    def get_tasks_for_user(user_id: int) -> List[Task]:
        stmt = (
            select(Task)
            .join(TaskUser)
            .where(TaskUser.user_id == user_id)
            .order_by(Task.deadline)
        )
        return db.session.scalars(stmt).all()

    @staticmethod
    def get_task_for_user(task_id: int, user_id: int) -> Optional[TaskUser]:
        stmt = (
            select(TaskUser)
            .where(TaskUser.task_id == task_id, TaskUser.user_id == user_id)
        )
        return db.session.scalar(stmt)

    @staticmethod
    def create_task(room_id: int, title: str, description: str, scheduled_date: Optional[datetime],
                    deadline: Optional[datetime]) -> Task:

        task = Task(
            room_id=room_id,
            title=title,
            description=description,
            scheduled_date=scheduled_date,
            deadline=deadline
        )

        db.session.add(task)
        db.session.commit()
        return task

    @staticmethod
    def create_task_user(user_id: int, task_id: int):
        task_user = TaskUser(task_id=task_id, user_id=user_id)
        db.session.add(task_user)
        db.session.commit()
        return task_user