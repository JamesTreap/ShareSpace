from flask import Flask
from dotenv import dotenv_values


from flask_api.controllers.auth import auth_bp
from flask_api.controllers.users import users_bp
from flask_api.controllers.rooms import rooms_bp
from flask_api.controllers.tasks import tasks_bp
from flask_api.controllers.finance import finance_bp
from sqlalchemy import text
from flask_api.entities import db
from importlib import import_module


# Load environment variables
config = dotenv_values(".env.potential")

def create_app() -> Flask:
    app = Flask(__name__)

    # ── Config ─────────────────────────────────────────────
    app.config.update(
        SECRET_KEY=config.get("SECRET_KEY", "dev-secret-key"),
        SQLALCHEMY_DATABASE_URI=config.get("DATABASE_URL"),
        SQLALCHEMY_TRACK_MODIFICATIONS=False,
    )
    if not app.config["SQLALCHEMY_DATABASE_URI"]:
        raise RuntimeError("DATABASE_URL missing in .env")

    # ── Extensions ────────────────────────────────────────
    db.init_app(app)

    # ── Blueprints ────────────────────────────────────────
    app.register_blueprint(auth_bp,    url_prefix="/auth")
    app.register_blueprint(users_bp,   url_prefix="/users")
    app.register_blueprint(rooms_bp,   url_prefix="/rooms")
    app.register_blueprint(tasks_bp,   url_prefix="/tasks")
    app.register_blueprint(finance_bp, url_prefix="/finance")

    # ── Models registration (dev-only create_all) ─────────
    with app.app_context():
        # import all model modules so their tables are registered
        for m in ("user", "room", "task", "finance"):
            import_module(f"flask_api.entities.{m}")

        db.create_all()

    # Health check endpoint
    @app.route('/health', methods=['GET'])
    def health_check():
        return {"status": "healthy", "database": "postgresql"}, 200


    # Test database endpoint
    @app.route('/api/test-db', methods=['GET'])
    def test_database():
        try:
            with db.engine.connect() as conn:
                conn.execute(text("SELECT 1"))

            return {
                       "status": "Database connection successful!",
                       "type": "PostgreSQL"
                   }, 200
        except Exception as e:
            return {
                       "status": "Database connection failed",
                       "error": str(e)
                   }, 500

    return app

app = create_app()

if __name__ == '__main__':
    app.run(debug=True)