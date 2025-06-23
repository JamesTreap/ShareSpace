from marshmallow import Schema, fields, validate, post_load, EXCLUDE
from flask_api.entities.user import User

class UserOut(Schema):
    id          = fields.Int()
    username    = fields.Str()
    email       = fields.Email()
    full_name   = fields.Str()
    created_at  = fields.DateTime()
    updated_at  = fields.DateTime()

class UserIn(Schema):
    # Validation rules on incoming JSON
    username  = fields.Str(required=True,
                           validate=validate.Length(min=3, max=50))
    email     = fields.Email(required=True)
    password  = fields.Str(required=True,
                           load_only=True,
                           validate=validate.Length(min=6))
    full_name = fields.Str()
    avatar_url = fields.Url(allow_none=True)

    class Meta:
        unknown = EXCLUDE          # ignore extra fields

    @post_load
    def make_user(self, data, **kwargs):
        """Convert validated data → User model instance."""
        pw = data.pop("password")
        user = User(**data)
        user.set_password(pw)
        return user
