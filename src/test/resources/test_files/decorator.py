@staticmethod
def foo():
    pass


@custom_decorator(arg1=10)
class Bar:
    @classmethod
    def baz(cls):
        pass
