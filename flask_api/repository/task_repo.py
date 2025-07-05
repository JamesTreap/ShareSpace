from sqlalchemy import select
from entities.task import Task, TaskUser
from entities import db
from typing import List, Optional

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
