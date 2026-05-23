from dataclasses import dataclass
import os

@dataclass
class Config:
    api_key: str

def load_config(path: str = None) -> Config:
    #return Config(api_key=os.getenv(''))
    pass

config = load_config()