from flask import Flask, request, jsonify
import random
import pymysql
from pymysql.cursors import DictCursor
from dotenv import dotenv_values
import os

config = dotenv_values(".env") 
connection = pymysql.connect(
    host=config["HOST"],
    user=config["USER"],
    password=config["PASSWORD"],
    database=config["DATABASE"],
    cursorclass=DictCursor 
)
cursor = connection.cursor()
app = Flask(__name__)

@app.route('/get-user/<user_id>', methods=['GET'])
def get_user(user_id):
    cursor.execute("SELECT * FROM user WHERE username = %s", (user_id,))
    user_data = cursor.fetchone()
    if not user_data:
        return jsonify({'error': 'User not found'}), 404
    return jsonify(user_data), 200

@app.route('/create-user', methods=['POST'])
def create_user():
    data = request.get_json()
    if not data or 'username' not in data or 'password' not in data:
        return jsonify({'error': 'Invalid input'}), 400
    
    cursor.execute("INSERT INTO user (username, password) VALUES (%s, %s)", (data['username'], data['password']))
    connection.commit()
    return jsonify("succesfully created user with username " + data['username']), 201

if __name__ == '__main__':
    app.run(debug=True)