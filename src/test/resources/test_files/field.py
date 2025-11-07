class Alpha:
    x = 1
    y: int = 5
    CONST = "data"

    def method(self):
        z = 3       # not a Field; should be VariableEntity inside method
