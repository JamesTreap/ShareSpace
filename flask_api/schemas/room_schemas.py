# flask_api/schemas/room_schema.py
from marshmallow_sqlalchemy import SQLAlchemySchema, auto_field
from marshmallow.fields import Nested

from flask_api.entities.room import Room, RoomMember, RoomInvitation
from flask_api.entities.user import User


class RoomMemberSchema(SQLAlchemySchema):
    class Meta:
        model = RoomMember
        load_instance = True
        include_fk = True

    user_id = auto_field()

class RoomInvitationSchema(SQLAlchemySchema):
    class Meta:
        model = RoomInvitation
        load_instance = True
        include_fk = True

    room_id = auto_field()
    inviter_user_id = auto_field()
    invitee_user_id = auto_field()
    status = auto_field()


class RoomSchema(SQLAlchemySchema):
    class Meta:
        model = Room
        load_instance = True
        include_fk = True

    id = auto_field()
    name = auto_field()
    picture_url = auto_field()
    members = Nested(RoomMemberSchema, many=True, dump_only=True)
