FROM python:3.13-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_INDEX_URL=https://mirrors.aliyun.com/pypi/simple \
    HK_AGENT_HOST=0.0.0.0 \
    HK_AGENT_PORT=8493 \
    HK_AGENT_RELOAD=0 \
    HK_AGENT_REPORT_DIR=/data/reports

WORKDIR /app

COPY . .
RUN pip install --no-cache-dir .

EXPOSE 8493

CMD ["python", "-m", "uvicorn", "web.app:app", "--host", "0.0.0.0", "--port", "8493"]
