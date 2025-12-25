class User:
    def __init__(self, username: str, email: str):
        self.username = username
        self.email = email

    def get_display_name(self) -> str:
        return f"{self.username} <{self.email}>"
