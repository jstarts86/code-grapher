def log_info(message: str):
    print(f"[INFO] {message}")

def log_error(message: str, code: int = 500):
    print(f"[ERROR {code}] {message}")
