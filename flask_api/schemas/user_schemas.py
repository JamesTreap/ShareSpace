from marshmallow import validate, post_load, EXCLUDE
from marshmallow_sqlalchemy import SQLAlchemySchema, auto_field
from werkzeug.security import generate_password_hash

from entities.user import User


class UserSchema(SQLAlchemySchema):

    class Meta:
        model = User
        load_instance = True
        include_fk = True
        unknown = EXCLUDE

    id          = auto_field(dump_only=True)
    username    = auto_field(
        required=True,
        validate=validate.Length(min=3, max=50)
    )
    email       = auto_field(required=True)
    name   = auto_field()
    profile_picture_url  = auto_field()

    password_hash    = auto_field(
        load_only=True,
        required=True,
        validate=validate.Length(min=6)
    )
    created_at  = auto_field(dump_only=True)
    updated_at  = auto_field(dump_only=True)

    @post_load
    def hash_password(self, data, **_):
        raw = data.pop("password", None)
        user = User(**data)
        if raw:
            generate_password_hash(raw)
        return user

class UserPublicSchema(UserSchema):
    class Meta(UserSchema.Meta):
        fields = ("id", "username", "name", "profile_picture_url")
