from fastapi import FastAPI

app = FastAPI(title="AI Security Service", version="0.0.1")

from app.api.routes import router

app.include_router(router)
