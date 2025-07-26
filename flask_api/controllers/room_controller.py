# flask_api/controllers/rooms.py
from __future__ import annotations
from datetime import date, timedelta, datetime

from flask import Blueprint, abort, g, jsonify, request

from repository.user_repo import UserRepo
from schemas.user_schemas import UserPublicSchema
from services.room_service import RoomService
from utils import token_required
from repository.room_repo import RoomRepo
from schemas.room_schemas import (
    RoomSchema,
    RoomInvitationSchema,
)
from entities.user import User

rooms_bp = Blueprint("room", __name__, url_prefix="/rooms")

room_schema = RoomSchema()
rooms_schema = RoomSchema(many=True)
inv_schema = RoomInvitationSchema()
invs_schema = RoomInvitationSchema(many=True)
user_public_schema = UserPublicSchema(many=True)


@rooms_bp.route("", methods=["GET"])
@token_required
def get_rooms():
    user: User = g.current_user
    joined = RoomService.get_rooms_for_user(user.id)
    return jsonify({"joinedRooms": rooms_schema.dump(joined)}), 200


@rooms_bp.route("/invites", methods=["GET"])
@token_required
def room_invites():
    user: User = g.current_user
    joined = RoomService.get_invites_for_user(user.id)
    return jsonify({"invitedRooms": rooms_schema.dump(joined)}), 200


@rooms_bp.route("/rooms-and-invitations", methods=["GET"])
@token_required
def get_user_rooms_and_invitations():
    user: User = g.current_user
    joined_rooms = [member.room for member in user.rooms]

    pending_invitations = [
        invitation for invitation in user.invitations_received
        if invitation.status == "waiting"
    ]

    joined_rooms_data = room_schema.dump(joined_rooms, many=True)
    invitations_data = invs_schema.dump(pending_invitations, many=True)
    alert_window = datetime.utcnow() + timedelta(days=1)

    for i, room_data in enumerate(joined_rooms_data):
        room = joined_rooms[i]
        user_task_count = sum(
            1 for task in room.tasks
            for task_user in task.users
            if (task_user.user_id == user.id and
                task.deadline is not None and
                task.deadline <= alert_window and
                task_user.status != "complete")
        )
        room_data['alerts'] = user_task_count

        # TO-DO: Implement owing logic
        # ⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️
        room_data['balance_due'] = 0
        # ⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️⚠️

    return jsonify({
        "joined_rooms": joined_rooms_data,
        "room_invitations": invitations_data,
    }), 200


@rooms_bp.route("/<int:room_id>/members", methods=["GET"])
@token_required
def get_roommates(room_id: int):
    user: User = g.current_user
    room = RoomService.get_room_with_members_if_user_is_member(room_id, user)
    mates = user_public_schema.dump([m.user for m in room.members])
    return jsonify({"roommates": mates}), 200

@rooms_bp.route("/leave/<int:room_id>", methods=["POST"])
@token_required
def leave_room(room_id: int):
    user: User = g.current_user

    RoomService.leave_room(user.id, room_id)

    return jsonify({"message": "Successfully left the room"}), 200

@rooms_bp.route("/<int:room_id>", methods=["GET"])
@token_required
def get_room(room_id: int):
    room = RoomService.get_room_by_id_with_members(room_id)
    if room is None:
        abort(404, "Room not found")
    return jsonify(room_schema.dump(room)), 200


@rooms_bp.route("/create", methods=["POST"])
@token_required
def create_room():
    data = request.get_json(silent=True) or {}
    name = (data.get("name") or "").strip()
    address = (data.get("address") or "").strip()
    description = data.get("description", "").strip()
    if not name:
        abort(400, "Room name is required")

    picture_url = data.get("picture_url")
    user: User = g.current_user
    room = RoomService.create_room_for_user(name, address, description, picture_url, user)
    return jsonify(room_schema.dump(room)), 201


@rooms_bp.route("/update/<int:room_id>", methods=["PATCH"])
@token_required
def update_room(room_id: int):
    user: User = g.current_user
    if not RoomService.validate_room_user(user.id, int(room_id)):
        abort(404, description="User does not belong to the room")

    data = request.get_json(silent=True) or {}
    name = (data.get("name") or "").strip()
    address = (data.get("address") or "").strip()
    description = data.get("description", "").strip()
    if not name:
        abort(400, "Room name is required")

    picture_url = data.get("picture_url")

    room = RoomService.update_room(room_id, name, address, description, picture_url)
    return jsonify(room_schema.dump(room)), 200


@rooms_bp.route("/<int:room_id>/invite", methods=["POST"])
@token_required
def invite_to_room(room_id: int):
    data = request.get_json(silent=True) or {}
    invitee_username = data.get("invitee_username")
    if not invitee_username:
        abort(400, "invitee_username is required")

    user: User = g.current_user
    invite = RoomService.invite_user_to_room(room_id, user, invitee_username)
    return jsonify(inv_schema.dump(invite)), 201


@rooms_bp.route("/invites/<int:room_id>/respond", methods=["POST"])
@token_required
def respond_invitation(room_id: int):
    data = request.get_json(silent=True) or {}
    status = data.get("status")
    if status not in {"accepted", "rejected"}:
        abort(400, "status must be 'accepted' or 'rejected'")

    user: User = g.current_user
    updated = RoomService.respond_to_invitation(room_id, user, status)
    return jsonify(inv_schema.dump(updated)), 200
