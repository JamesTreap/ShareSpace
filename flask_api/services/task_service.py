from repository.task_repo import TaskRepo
from repository.user_repo import UserRepo
from typing import List, Optional
from datetime import datetime, timedelta, date
from flask import abort
from services.room_service import RoomService

class TaskService:
    @staticmethod
    def get_enriched_tasks_for_room(room_id: int) -> List[dict]:
        tasks = TaskRepo.get_tasks_for_room(room_id)
        enriched = TaskService.prepare_tasks(tasks)

        return enriched

    @staticmethod
    def get_tasks_for_room_by_date(room_id: int, date: date) -> List[dict]:
        tasks = TaskRepo.get_tasks_for_room_by_date(room_id, date)
        enriched = TaskService.prepare_tasks(tasks)

        return enriched

    @staticmethod
    def prepare_tasks(tasks):
        enriched = []
        for task in tasks:
            statuses = {
                str(task_user.user_id): task_user.status.upper()
                for task_user in task.users
            }

            enriched.append({
                "id": task.id,
                "title": task.title,
                "deadline": task.deadline.isoformat() if task.deadline else None,
                "description": task.description,
                "frequency": task.frequency,
                "repeat": task.repeat,
                "statuses": statuses,
                "scheduled_date": task.scheduled_date.isoformat() if task.scheduled_date else None,
            })
        return enriched

    @staticmethod
    def validate_input_create_task(title: Optional[str], frequency: Optional[str], repeat: Optional[int]):
        if not title:
            abort(400, "Title is required.")
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
    def parse_dates(deadline: str):
        try:
            deadline_obj = datetime.strptime(deadline, "%Y-%m-%dT%H:%M")
        except ValueError:
            abort(400, "Invalid date format, expected 'YYYY-MM-DDTHH:MM' for deadline.")

        return deadline_obj


    @staticmethod
    def check_users(user_ids: List[int], room_id: int) -> List[int]:
        if not user_ids:
            abort(400, "At least one user ID is required.")

        if not RoomService.validate_room_users(user_ids, room_id):
            abort(404, "Not all users belong to the specified room.")

        return user_ids

    @staticmethod
    def validate_assignees(assignees: List[dict]) -> List[tuple]:
        if not assignees:
            abort(400, "At least one assignee is required.")

        validated_assignees = []
        for assignee in assignees:
            user_id = assignee.get('user_id')
            status = assignee.get('status')

            if not user_id or not status:
                abort(400, "Each assignee must include both 'user_id' and 'status'.")

            if status not in ['todo', 'in-progress', 'complete']:
                abort(400, f"Invalid status '{status}', must be one of 'todo', 'in-progress', or 'complete'.")

            validated_assignees.append((user_id, status))  # Store as a tuple (user_id, status)

        return validated_assignees

    @staticmethod
    def update_task(task_id: int, title: str, description: Optional[str] = None,
                    deadline: Optional[str] = None, assignees: Optional[List[dict]] = None):

        task = TaskRepo.get_task_by_id(task_id)
        if not task:
            abort(404, "Task not found.")
        if not title:
            abort(400, "Title is required.")
        if not deadline:
            abort(400, "Deadline is required.")

        if assignees is not None:
            assignee_data = TaskService.validate_assignees(assignees)

            for user_id, status in assignee_data:
                TaskRepo.update_task_user(user_id, task.id, status)

        TaskRepo.update_task(task_id, title, description, deadline)

    @staticmethod
    def create_task_service(room_id: int, title: Optional[str], description: Optional[str], frequency: Optional[str],
                            repeat: Optional[int], deadline: Optional[str], assignees: List[dict]):

        freq_value, unit = TaskService.validate_input_create_task(title, frequency, repeat)
        if not deadline:
            abort(400, "Deadline is required.")
        deadline_date = TaskService.parse_dates(deadline)
        assignee_data = TaskService.validate_assignees(assignees)

        user_ids = [assignee[0] for assignee in assignee_data]
        user_ids = TaskService.check_users(user_ids, room_id)

        tasks = []
        scheduled_date = datetime.now()

        task = TaskRepo.create_task(
            room_id=room_id,
            title=title,
            description=description,
            frequency=frequency,
            repeat=repeat,
            scheduled_date=scheduled_date,
            deadline=deadline_date,
        )
        tasks.append(task)
        for user_id, status in assignee_data:
            TaskRepo.create_task_user(user_id, task.id, status)

        if repeat > 0:
            for i in range(1, repeat + 1):
                scheduled_date = scheduled_date + timedelta(**{unit: freq_value})
                deadline_date = deadline_date + timedelta(**{unit: freq_value})

                task = TaskRepo.create_task(
                    room_id=room_id,
                    title=title,
                    description=description,
                    frequency=frequency,
                    repeat=repeat,
                    scheduled_date=scheduled_date,
                    deadline=deadline_date,
                )
                tasks.append(task)

                for user_id, status in assignee_data:
                    TaskRepo.create_task_user(user_id, task.id, None)

        return tasks

    @staticmethod
    def delete_task(user_id: int, task_id: int) -> bool:
        if not TaskService.is_user_in_room_of_task(user_id, task_id):
            abort(403, "User does not have permission to delete this task.")
        return TaskRepo.delete_task(task_id)

    @staticmethod
    def is_user_in_room_of_task(user_id: int, task_id: int) -> bool:
        room_id = TaskRepo.get_task_by_id(task_id).room_id
        if room_id is None:
            return False
        return RoomService.validate_room_user(user_id, room_id)