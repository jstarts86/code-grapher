from typing import List, Dict, Tuple, Set, Optional, Union, Any, Callable, TypeVar, Generic

# 1. Simple built-in types
x: int = 10
y: float = 3.14
z: bool = True
name: str = "Alice"

# 2. Collection types (typing module)
names: List[str] = ["Alice", "Bob"]
scores: Dict[str, int] = {"Alice": 100, "Bob": 95}
coordinates: Tuple[int, int] = (10, 20)
unique_ids: Set[int] = {1, 2, 3}

# 3. Optional and Union
maybe_int: Optional[int] = None
int_or_str: Union[int, str] = "hello"

# 4. New style generics (Python 3.9+)
new_list: list[int] = [1, 2, 3]
new_dict: dict[str, float] = {"pi": 3.14}

# 5. Complex nested types
complex_structure: List[Dict[str, Union[int, float]]] = [
    {"a": 1, "b": 2.5},
    {"a": 3, "b": 4.5}
]

# 6. Type Aliases
UserId = int
SessionId = str
UserSession = Tuple[UserId, SessionId]

def get_session(user_id: UserId) -> UserSession:
    return (user_id, "session_123")

# 7. Custom Classes as types
class Person:
    def __init__(self, name: str):
        self.name = name

def greet(p: Person) -> None:
    print(f"Hello, {p.name}")

# 8. Forward references
def create_node() -> "Node":
    return Node()

class Node:
    def __init__(self, value: int = 0):
        self.value = value
        self.next: Optional["Node"] = None

# 9. Callable
Action = Callable[[int, int], int]

def perform_action(a: int, b: int, action: Action) -> int:
    return action(a, b)

# 10. Generics with TypeVar
T = TypeVar("T")

class Box(Generic[T]):
    def __init__(self, content: T):
        self.content = content

    def get_content(self) -> T:
        return self.content

int_box: Box[int] = Box(10)
str_box: Box[str] = Box("hello")

# 11. Modern Python 3.10+ Syntax (Union types with |)
modern_union: int | str = 10
modern_optional: int | None = None
modern_nested: list[dict[str, int | None]] = [{"a": 1}, {"b": None}]
modern_tuple: tuple[int, str | bool] = (1, True)
modern_set: set[int | str] = {1, "a"}

def modern_func(a: int | float, b: list[int] | None) -> str | None:
    return "ok" if a else None

# 12. Recursive / Self-referencing types (Modern style)
class ModernNode:
    def __init__(self, value: int):
        self.value = value
        self.next: ModernNode | None = None

