from flask import Flask
from dotenv import dotenv_values
from entities import db  # Use the shared db instance
from importlib import import_module
from sqlalchemy.orm import configure_mappers
import sqlite3

def create_app() -> Flask:
    app = Flask(__name__)
    
    # Use local database configuration
    cfg = dotenv_values(".env.potential")
    database_uri = cfg.get("LOCAL_DATABASE_URL", "sqlite:///local_shareSpace.db")
    
    app.config.update(
        SECRET_KEY = cfg.get("SECRET_KEY", "dev-secret-key"),
        SQLALCHEMY_DATABASE_URI = database_uri,
        SQLALCHEMY_TRACK_MODIFICATIONS = False,
    )

    # Initialize the shared db instance
    db.init_app(app)

    with app.app_context():
        # Load models and create schema
        for m in ("user", "room", "task", "finance"):
            import_module(f"entities.{m}")
        configure_mappers()
        db.create_all()
    return app


def run_sql_script(db_path="instance/local_shareSpace.db", script_path="sql_data/seed_db.sql"):
    with open(script_path, "r", encoding="utf-8") as f:
        sql = f.read()

    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    cursor.executescript(sql)
    conn.commit()
    conn.close()


if __name__ == "__main__":
    app = create_app()
    run_sql_script()  
