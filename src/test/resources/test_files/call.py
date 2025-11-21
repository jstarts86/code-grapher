def outer():
    foo()                     # simple call
    bar(1, 2)                 # call with arguments
    obj.method(3)             # method call
    nested(foo(bar(1)))       # nested call
