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
                .where(Task.room_id == room_id, Task.scheduled_date <= datetime.now())
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
    def create_task(room_id: int, title: str, description: str, frequency: int, repeat: int, scheduled_date: Optional[datetime],
                    deadline: Optional[datetime]) -> Task:

        task = Task(
            room_id=room_id,
            title=title,
            description=description,
            frequency=frequency,
            repeat=repeat,
            scheduled_date=scheduled_date,
            deadline=deadline
        )

        db.session.add(task)
        db.session.commit()
        return task

    @staticmethod
    def create_task_user(user_id: int, task_id: int, status: Optional[str]):
        task_user = TaskUser(task_id=task_id, user_id=user_id, status=status)
        db.session.add(task_user)
        db.session.commit()
        return task_user

    @staticmethod
    def get_task_by_id(task_id: int):
        return Task.query.get(task_id)

    @staticmethod
    def update_task_user(user_id: int, task_id: int, status: str):
        task_user = TaskUser.query.filter_by(user_id=user_id, task_id=task_id).first()
        if task_user:
            task_user.status = status
            db.session.commit()

    @staticmethod
    def update_task(task_id: int, title: Optional[str] = None, description: Optional[str] = None, deadline: Optional[datetime] = None):
        task = Task.query.get(task_id)
        if task:
            if title is not None:
                task.title = title
            if description is not None:
                task.description = description
            if deadline is not None:
                task.deadline = deadline
            db.session.commit()
        return task