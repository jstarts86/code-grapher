from utils import log_info
from models.user import User

def main():
    log_info("Starting application...")
    user = User("john_doe", "john@example.com")
    print(f"User created: {user.get_display_name()}")

if __name__ == "__main__":
    main()
