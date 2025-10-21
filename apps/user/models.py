from django.contrib.auth.models import AbstractBaseUser, BaseUserManager, PermissionsMixin
from django.db import models

class UserManager(BaseUserManager):
    def create_user(self, userId, password=None, **extra_fields):
        if not userId:
            raise ValueError('The userId field must be set')
        user = self.model(userId=userId, **extra_fields)
        user.set_password(password)
        user.save(using=self._db)
        return user

    def create_superuser(self, userId, password=None, **extra_fields):
        extra_fields.setdefault('is_staff', True)
        extra_fields.setdefault('is_superuser', True)

        if extra_fields.get('is_staff') is not True:
            raise ValueError('Superuser must have is_staff=True.')
        if extra_fields.get('is_superuser') is not True:
            raise ValueError('Superuser must have is_superuser=True.')

        return self.create_user(userId, password, **extra_fields)

class User(AbstractBaseUser, PermissionsMixin):
    class UserType(models.TextChoices):
        GENERAL = 'general', '일반'
        SHOP_OWNER = 'shop_owner', '소품샵'
        ARTIST = 'artist', '작가'
        ADMIN = 'admin', '관리자'

    userId = models.CharField(max_length=255, unique=True)
    userType = models.CharField(max_length=20, choices=UserType.choices, default=UserType.GENERAL)
    phoneNumber = models.CharField(max_length=20)
    name = models.CharField(max_length=255)
    profileImage = models.CharField(max_length=255, blank=True, null=True)
    isActive = models.BooleanField(default=True)
    is_staff = models.BooleanField(default=False)
    createdAt = models.DateTimeField(auto_now_add=True)
    updatedAt = models.DateTimeField(auto_now=True)

    objects = UserManager()

    USERNAME_FIELD = 'userId'
    REQUIRED_FIELDS = ['name']

    def __str__(self):
        return self.userId

class BusinessInfo(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE, null=True, blank=True)
    businessName = models.CharField(max_length=255)
    businessRegistrationNumber = models.CharField(max_length=255)
    businessAddress = models.CharField(max_length=255)
    businessType = models.CharField(max_length=255)
    businessCategory = models.CharField(max_length=255)
    businessRegistrationFile = models.CharField(max_length=255)
    interests = models.CharField(max_length=255)
    bankName = models.CharField(max_length=255)
    accountNumber = models.CharField(max_length=255)
    accountHolder = models.CharField(max_length=255)
    taxInvoiceEmail = models.EmailField()
    hometaxApiKey = models.CharField(max_length=255, blank=True, null=True)

    def __str__(self):
        return self.businessName

class TermsAgreement(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    requiredTerms = models.BooleanField()
    optionalTerms1 = models.BooleanField()
    optionalTerms2 = models.BooleanField()
    optionalTerms3 = models.BooleanField()
    agreedAt = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.user.userId}'s Terms Agreement"
