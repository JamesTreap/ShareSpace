from flask_api.repository.task_repo import TaskRepo
from typing import List, Optional

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
