# config/settings/dev.py

from .base import *
# import os # Commented out as not needed for SQLite DB path

# 개발 환경 설정
DEBUG = True

ALLOWED_HOSTS = ['localhost', '127.0.0.1', '0.0.0.0']

# 개발용 SQLite 데이터베이스
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3',
        'NAME': BASE_DIR / 'db.sqlite3',
    }
}

# 개발용 추가 앱들
INSTALLED_APPS += [
    'django_extensions',  # 개발 도구
]

# 개발용 로깅 설정
LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'handlers': {
        'console': {
            'class': 'logging.StreamHandler',
        },
    },
    'loggers': {
        'django.db.backends': {
            'level': 'DEBUG',
            'handlers': ['console'],
        }
    },
    'root': {
        'handlers': ['console'],
        'level': 'INFO',
    },
}

# CORS 설정 (개발환경용)
CORS_ALLOWED_ORIGINS = [
    "http://localhost:3000",  # React 개발 서버
    "http://127.0.0.1:3000",
]