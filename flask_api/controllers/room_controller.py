# flask_api/controllers/rooms.py
from __future__ import annotations

from flask import Blueprint, abort, g, jsonify, request

from flask_api.repository.user_repo import UserRepo
from flask_api.schemas.user_schemas import UserPublicSchema
from flask_api.utils import token_required
from flask_api.repository.room_repo import RoomRepo
from flask_api.schemas.room_schemas import (
    RoomSchema,
    RoomInvitationSchema,
)
from flask_api.entities.user import User

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
    joined = RoomRepo.list_rooms_for_user(user.id)
    return jsonify({"joinedRooms": rooms_schema.dump(joined)}), 200


@rooms_bp.route("/invites", methods=["GET"])
@token_required
def room_invites():
    user: User = g.current_user
    status = request.args.get("status", "waiting")
    invites = RoomRepo.list_invitations_for_user_by_status(user.id, status)
    return jsonify({"roomInvites": invs_schema.dump(invites)}), 200


@rooms_bp.route("/<int:room_id>/members", methods=["GET"])
@token_required
def get_roommates(room_id: int):
    room = RoomRepo.get_room_with_members(room_id)
    if room is None:
        abort(404, "Room not found")

    current = g.current_user
    if current.id not in (m.user_id for m in room.members):
        abort(403, "You are not a member of this room")

    mates = user_public_schema.dump([m.user for m in room.members])
    return jsonify({"roommates": mates}), 200


@rooms_bp.route("/<int:room_id>", methods=["GET"])
@token_required
def get_room(room_id: int):
    room = RoomRepo.get_room_with_members(room_id)
    if room is None:
        abort(404, "Room not found")
    return jsonify(room_schema.dump(room)), 200


@rooms_bp.route("/create", methods=["POST"])
@token_required
def create_room():
    data = request.get_json(silent=True) or {}
    name = (data.get("name")).strip()
    if not name:
        abort(400, "Room name is required")

    picture_url = data.get("picture_url")
    room = RoomRepo.create_room(name, picture_url)

    user: User = g.current_user
    RoomRepo.add_member(room.id, user.id)
    return jsonify(room_schema.dump(room)), 201


@rooms_bp.route("/<int:room_id>/invite", methods=["POST"])
@token_required
def invite_to_room(room_id: int):
    data = request.get_json(silent=True) or {}
    invitee_username = data.get("invitee_username")

    if invitee_username is None:
        abort(400, "invitee_username is required")

    room = RoomRepo.get_room_with_members(room_id)
    if room is None:
        abort(404, "Room not found")

    user: User = g.current_user
    if user.id not in (m.user_id for m in room.members):
        abort(403, "You are not a member of this room")
    invitee = UserRepo.find_by_username(invitee_username)

    invite = RoomRepo.create_invitation(room_id, user.id, invitee.id)
    return jsonify(inv_schema.dump(invite)), 201


@rooms_bp.route("/invites/<int:room_id>/respond", methods=["POST"])
@token_required
def respond_invitation(room_id: int):

    data = request.get_json(silent=True) or {}
    status = data.get("status")
    if status not in {"accepted", "rejected"}:
        abort(400, "status must be 'accepted' or 'rejected'")

    user: User = g.current_user
    invitation = RoomRepo.waiting_invitations_for_user_by_room(user.id, room_id)
    if invitation is None:
        abort(404, "Invitation not found")


    updated = RoomRepo.respond_to_invitation(invitation.id, status)

    if status == "accepted":
        RoomRepo.add_member(updated.room_id, user.id)

    return jsonify(inv_schema.dump(updated)), 200
