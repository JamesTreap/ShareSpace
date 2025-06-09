from pymongo import MongoClient
from dotenv import dotenv_values

# Load environment variables
config = dotenv_values(".env")

# Connect to MongoDB
client = MongoClient(config["MONGO_URI"])
db = client[config["DATABASE_ENV"]]
users_collection = db["users"]