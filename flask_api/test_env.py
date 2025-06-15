from dotenv import dotenv_values

config = dotenv_values(".env.potential")

print("=== LOADED CONFIG ===")
for key, value in config.items():
    if 'KEY' in key:
        print(f"{key}: {value[:20]}...")  # Hide sensitive data
    else:
        print(f"{key}: {value}")
print("====================")

print(f"DATABASE_URL exists: {bool(config.get('DATABASE_URL'))}")
print(f"SECRET_KEY exists: {bool(config.get('SECRET_KEY'))}")
