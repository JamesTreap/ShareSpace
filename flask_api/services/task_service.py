from repository.task_repo import TaskRepo
from repository.user_repo import UserRepo
from typing import List, Optional
from datetime import datetime, timedelta
from flask import abort

class TaskService:
    @staticmethod
    def get_enriched_tasks_for_room(room_id: int) -> List[dict]:
        tasks = TaskRepo.get_tasks_for_room(room_id)
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
                "statuses": statuses,
            })

        return enriched

    @staticmethod
    def validate_input_create_task(title: str, frequency: str, repeat: int):
        if not title:
            abort(400, "Title is required.")
        if frequency and frequency[-1] not in ['d', 'w', 'm']:
            abort(400, "Frequency must be in the format like '1d', '2w', or '3m'.")
        if frequency and not repeat:
            abort(400, "Repeat is required and must be a number.")

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
        else:
            freq_value, unit = None, None

        return freq_value, unit

    @staticmethod
    def parse_dates(deadline: str):
        try:
            deadline_obj = datetime.strptime(deadline, "%Y-%m-%dT%H:%M")
        except ValueError:
            abort(400, "Invalid date format, expected 'YYYY-MM-DDTHH:MM' for deadline.")

        return deadline_obj

    @staticmethod
    def calculate_scheduled_dates(deadline_obj: datetime, repeat: int, freq_value: int, unit: str):
        scheduled_dates = [deadline_obj]
        for i in range(1, repeat + 1):
            scheduled_dates.append(scheduled_dates[-1] + timedelta(**{unit: freq_value}))
        return scheduled_dates

    @staticmethod
    def check_users(user_ids: List[int], room_id: int) -> List[int]:
        if not user_ids:
            abort(400, "At least one user ID is required.")

        users = UserRepo.get_users_by_ids(user_ids)
        if not users:
            abort(404, "No users found with the provided IDs.")

        if not all(any(room_member.room_id == room_id for room_member in user.rooms) for user in users):
            abort(404, "Not all users belong to the specified room.")
        return [user.id for user in users]

    @staticmethod
    def create_task_service(room_id: int, title: str, description: str, frequency: str, repeat: int, deadline: str,
                            user_ids: List[int]):

        freq_value, unit = TaskService.validate_input_create_task(title, frequency, repeat)
        deadline_date = TaskService.parse_dates(deadline)
        user_ids = TaskService.check_users(user_ids, room_id)

        tasks = []
        scheduled_date = datetime.now()

        task = TaskRepo.create_task(
            room_id=room_id,
            title=title,
            description=description,
            scheduled_date=scheduled_date,
            deadline=deadline_date,
        )
        tasks.append(task)

        for i in range(1, repeat + 1):
            scheduled_date = scheduled_date + timedelta(**{unit: freq_value})
            deadline_date = deadline_date + timedelta(**{unit: freq_value})

            task = TaskRepo.create_task(
                room_id=room_id,
                title=title,
                description=description,
                scheduled_date=scheduled_date,
                deadline=deadline_date,
            )
            tasks.append(task)
            for user_id in user_ids:
                TaskRepo.create_task_user(user_id, task.id)
