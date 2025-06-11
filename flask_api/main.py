from flask import Flask
from dotenv import dotenv_values
from models import db
from auth import auth_bp
from users import users_bp

# Load environment variables
config = dotenv_values(".env.potential")

def create_app():
    app = Flask(__name__)
    
    # Configuration
    app.config['SECRET_KEY'] = config.get("SECRET_KEY", "dev-secret-key")
    app.config['SQLALCHEMY_DATABASE_URI'] = config.get("DATABASE_URL")
    app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
    
    # Initialize database
    db.init_app(app)
    
    # Register blueprints
    app.register_blueprint(auth_bp)
    app.register_blueprint(users_bp)
    
    # Create tables
    with app.app_context():
        db.create_all()
        print("✅ Database tables created successfully!")
    
    # Health check endpoint
    @app.route('/health', methods=['GET'])
    def health_check():
        return {"status": "healthy", "database": "postgresql"}, 200
    
    # Test database endpoint
    @app.route('/api/test-db', methods=['GET'])
    def test_database():
        try:
            # Test database connection
            db.engine.execute('SELECT 1')
            return {"status": "Database connection successful!", "type": "PostgreSQL"}, 200
        except Exception as e:
            return {"status": "Database connection failed", "error": str(e)}, 500
    
    return app

app = create_app()

if __name__ == '__main__':
    app.run(debug=True)