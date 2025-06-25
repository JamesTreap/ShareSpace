# flask_api/main.py
from flask import Flask
from dotenv import dotenv_values
from flask_api.entities import db
from importlib import import_module
from sqlalchemy.orm import configure_mappers

def create_app() -> Flask:
    app = Flask(__name__)

    # ── Config ─────────────────────────────────────────
    cfg = dotenv_values(".env.potential")
    app.config.update(
        SECRET_KEY                = cfg.get("SECRET_KEY", "dev-secret-key"),
        SQLALCHEMY_DATABASE_URI   = cfg.get("DATABASE_URL"),
        SQLALCHEMY_TRACK_MODIFICATIONS = False,
    )
    if not app.config["SQLALCHEMY_DATABASE_URI"]:
        raise RuntimeError("DATABASE_URL missing in .env")

    # ── Bind extensions FIRST ─────────────────────────
    db.init_app(app)

    # ── Load all models while a context is active ─────
    with app.app_context():
        for m in ("user", "room", "task", "finance"):
            import_module(f"flask_api.entities.{m}")
        configure_mappers()
        db.create_all()

    # ── NOW import & register blueprints ──────────────
    from flask_api.controllers.auth_controller   import auth_bp
    from flask_api.controllers.user_controller   import users_bp
    from flask_api.controllers.room_controller   import rooms_bp
    from flask_api.controllers.task_controller   import tasks_bp
    from flask_api.controllers.finance_controller import finance_bp

    app.register_blueprint(auth_bp,   url_prefix="/auth")
    app.register_blueprint(users_bp,  url_prefix="/users")
    app.register_blueprint(rooms_bp,  url_prefix="/rooms")
    app.register_blueprint(tasks_bp,  url_prefix="/tasks")
    app.register_blueprint(finance_bp, url_prefix="/finance")

    return app

app = create_app()

if __name__ == "__main__":
    app.run(debug=True)
