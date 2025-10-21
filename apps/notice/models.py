from django.db import models

class Notice(models.Model):
    class Category(models.TextChoices):
        MAJOR = 'major', '주요'
        EVENT = 'event', '이벤트'
        GENERAL = 'general', '일반'
        GUIDE = 'guide', '가이드'

    category = models.CharField(max_length=20, choices=Category.choices, default=Category.GENERAL)
    title = models.CharField(max_length=255)
    content = models.TextField()
    isImportant = models.BooleanField(default=False)
    isActive = models.BooleanField(default=True)
    createdAt = models.DateTimeField(auto_now_add=True)
    updatedAt = models.DateTimeField(auto_now=True)

    def __str__(self):
        return self.title