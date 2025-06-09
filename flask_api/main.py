from flask import Flask
from dotenv import dotenv_values
from auth import auth_bp
from users import users_bp

# Load environment variables
config = dotenv_values(".env")

app = Flask(__name__)
app.config['SECRET_KEY'] = config.get("SECRET_KEY", "")  # Use env variable for secret key

# Register blueprints/routes
app.register_blueprint(auth_bp)
app.register_blueprint(users_bp)

# Optional: Add a health check endpoint
@app.route('/health', methods=['GET'])
def health_check():
    return {"status": "healthy"}, 200

if __name__ == '__main__':
    app.run(debug=True)