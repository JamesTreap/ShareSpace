from datetime import datetime
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import Enum as PgEnum

db = SQLAlchemy()

# ── reusable timestamp mixin ─────────────────────────────
class TimestampMixin(object):
    created_at = db.Column(
        db.DateTime, default=datetime.utcnow, nullable=False
    )
    updated_at = db.Column(
        db.DateTime,
        default=datetime.utcnow,
        onupdate=datetime.utcnow,
        nullable=False,
    )
