FROM python:3.9-slim

WORKDIR /app

RUN pip install -i https://pypi.tuna.tsinghua.edu.cn/simple requests

COPY verify.py .

CMD ["python3", "verify.py"]
