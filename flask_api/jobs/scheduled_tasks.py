from datetime import datetime, timedelta
from firebase_admin import messaging
from entities import db
from entities.task import Task
from entities.user import User
from entities.device_token import DeviceToken
from repository.task_repo import TaskRepo
from repository.user_repo import UserRepo

def send_upcoming_deadline_notifications(app):
    with app.app_context():
        print("send_upcoming_deadline_notifications started...")
        now = datetime.utcnow()
        print(f"Current time: {now}")
        upcoming = now + timedelta(days=2)

        tasks = TaskRepo.get_upcoming_tasks(upcoming)
        print(f"Found {len(tasks)} tasks with upcoming deadlines.")
        for task in tasks:
            print(f"Processing task: {task.title} with deadline {task.deadline}")
            task_users = TaskRepo.get_uncompleted_task_users(task.id)
            for task_user in task_users:
                user = User.query.get(task_user.user_id)
                token = DeviceToken.query.filter_by(user_id=user.id).first()
                if not token:
                    continue

                try:
                    msg = messaging.Message(
                        notification=messaging.Notification(
                            title="⏰ Task Reminder",
                            body=f"Dear '{user.name}', '{task.title}' is due in less than 1 hour!"
                        ),
                        token=token.token
                    )
                    messaging.send(msg)
                    TaskRepo.set_task_notified(task.id)

                    print(f"Notified user {user.username} for task {task.title}")
                except Exception as e:
                    print(f"Failed to notify user {user.username}: {e}")
