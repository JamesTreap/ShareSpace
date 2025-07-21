from flask import Flask
from dotenv import dotenv_values
from entities import db
from importlib import import_module
from sqlalchemy.orm import configure_mappers
import firebase_admin
from firebase_admin import credentials
from apscheduler.schedulers.background import BackgroundScheduler
from jobs.scheduled_tasks import send_upcoming_deadline_notifications

def create_app() -> Flask:
    app = Flask(__name__)

    # ── Config ─────────────────────────────────────────
    cfg = dotenv_values(".env.potential")
    
    # Determine which database to use
    use_local = cfg.get("USE_LOCAL_DB", "false").lower() == "true"
    
    if use_local:
        database_uri = cfg.get("LOCAL_DATABASE_URL", "sqlite:///local_shareSpace.db")
        print("🔧 Using local database")
    else:
        database_uri = cfg.get("DATABASE_URL")
        print("🌐 Using remote database")
    
    app.config.update(
        SECRET_KEY                = cfg.get("SECRET_KEY", "dev-secret-key"),
        SQLALCHEMY_DATABASE_URI   = database_uri,
        SQLALCHEMY_TRACK_MODIFICATIONS = False,
    )
    
    if not app.config["SQLALCHEMY_DATABASE_URI"]:
        raise RuntimeError("DATABASE_URL missing in .env")

    # ── Bind extensions FIRST ─────────────────────────
    db.init_app(app)

    # ── Load all models while a context is active ─────
    with app.app_context():
        for m in ("user", "room", "task", "finance"):
            import_module(f"entities.{m}")
        configure_mappers()
        db.create_all()

    # ── NOW import & register blueprints ──────────────
    from controllers.auth_controller   import auth_bp
    from controllers.user_controller   import users_bp
    from controllers.room_controller   import rooms_bp
    from controllers.task_controller   import tasks_bp
    from controllers.finance_controller import finance_bp

    app.register_blueprint(auth_bp,   url_prefix="/auth")
    app.register_blueprint(users_bp,  url_prefix="/users")
    app.register_blueprint(rooms_bp,  url_prefix="/rooms")
    app.register_blueprint(tasks_bp,  url_prefix="/tasks")
    app.register_blueprint(finance_bp, url_prefix="/finance")

    return app

def init_firebase():
    cred = credentials.Certificate("sharespace-firebase.json")
    firebase_admin.initialize_app(cred)

app = create_app()
init_firebase()

scheduler = BackgroundScheduler()
scheduler.add_job(
    lambda: send_upcoming_deadline_notifications(app),
    'interval',
    minutes=15
)
scheduler.start()


if __name__ == "__main__":
    app.run(debug=True)