def main():
    import uvicorn
    import os

    host = os.getenv("HK_AGENT_HOST", "0.0.0.0")
    port = int(os.getenv("HK_AGENT_PORT", "8000"))
    reload = os.getenv("HK_AGENT_RELOAD", "1") != "0"
    uvicorn.run("web.app:app", host=host, port=port, reload=reload)


if __name__ == "__main__":
    main()
