import jwt
from flask import current_app
from werkzeug.security import generate_password_hash, check_password_hash
from typing import Optional, Tuple
from repository.user_repo import UserRepo
from entities.user import User
from services.finance_service import FinanceService
from services.task_service import TaskService
from datetime import datetime, date

class CalendarService:
    @staticmethod
    def get_schedule_for_date(room_id: int, target_date: date):
        tasks = TaskService.get_tasks_for_room_by_date(room_id, target_date)
        bills = FinanceService.get_room_bills_by_date(room_id, target_date)

        return {
            "tasks": tasks,
            "bills": bills,
        }

    