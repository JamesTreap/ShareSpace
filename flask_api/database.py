from models import db, User
from dotenv import dotenv_values

# Load environment variables
config = dotenv_values(".env.potential")

class UsersCollection:
    """SQLAlchemy-based users interface - keeps your existing auth.py working"""
    
    def find_one(self, query, projection=None):
        """Find one user by username"""
        try:
            if "username" in query:
                user = User.query.filter_by(username=query["username"]).first()
                if user:
                    result = {
                        'id': user.id,
                        'username': user.username,
                        'password': user.password,  # Include password for auth
                        'name': user.name,
                        'created_at': user.created_at
                    }
                    
                    # Handle projection (exclude fields if requested)
                    if projection and "_id" in projection and projection["_id"] == 0:
                        result.pop('id', None)
                    
                    return result
                return None
        except Exception as e:
            print(f"Database error in find_one: {e}")
            return None
    
    def insert_one(self, document):
        """Insert one user"""
        try:
            # Check if user already exists
            existing_user = User.query.filter_by(username=document["username"]).first()
            if existing_user:
                return None  # User already exists (auth.py handles this)
            
            user = User(
                username=document["username"],
                password=document["password"],
                name=document.get("name", document["username"])  # Default name to username
            )
            
            db.session.add(user)
            db.session.commit()
            
            return {"inserted_id": user.id}
        except Exception as e:
            print(f"Database error in insert_one: {e}")
            db.session.rollback()
            return None

# Create the users interface (keeps your existing auth.py working)
users_collection = UsersCollection()