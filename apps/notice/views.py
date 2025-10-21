from rest_framework import viewsets
from .models import Notice
from .serializers import NoticeSerializer

class NoticeViewSet(viewsets.ModelViewSet):
    queryset = Notice.objects.all()
    serializer_class = NoticeSerializer