from django.db import models
from apps.user.models import User

class Shop(models.Model):
    owner = models.ForeignKey(User, on_delete=models.CASCADE)
    name = models.CharField(max_length=255)
    phoneNumber = models.CharField(max_length=20)
    address = models.CharField(max_length=255)
    businessHours = models.TextField()
    instagramId = models.CharField(max_length=255, blank=True, null=True)
    latitude = models.DecimalField(max_digits=9, decimal_places=6)
    longitude = models.DecimalField(max_digits=9, decimal_places=6)
    isActive = models.BooleanField(default=True)
    createdAt = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return self.name

class Artist(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    instagramId = models.CharField(max_length=255, blank=True, null=True)
    description = models.TextField()
    isActive = models.BooleanField(default=True)

    def __str__(self):
        return self.user.name

class Product(models.Model):
    artist = models.ForeignKey(Artist, on_delete=models.CASCADE)
    name = models.CharField(max_length=255)
    price = models.PositiveIntegerField()
    stock = models.PositiveIntegerField()
    description = models.TextField()
    image = models.CharField(max_length=255)
    category = models.CharField(max_length=255)
    memo = models.TextField(blank=True, null=True)
    isActive = models.BooleanField(default=True)
    createdAt = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return self.name

class Class(models.Model):
    shop = models.ForeignKey(Shop, on_delete=models.CASCADE)
    title = models.CharField(max_length=255)
    description = models.TextField()
    price = models.PositiveIntegerField()
    maxParticipants = models.PositiveIntegerField()
    images = models.JSONField()
    importantNotes = models.TextField()
    guidelines = models.TextField()
    isActive = models.BooleanField(default=True)
    timeSlotDuration = models.PositiveIntegerField()
    operatingStartTime = models.TimeField()
    operatingEndTime = models.TimeField()
    operatingDays = models.JSONField()
    createdAt = models.DateTimeField(auto_now_add=True)
    updatedAt = models.DateTimeField(auto_now=True)

    def __str__(self):
        return self.title

class Reservation(models.Model):
    classId = models.ForeignKey(Class, on_delete=models.CASCADE)
    userId = models.ForeignKey(User, on_delete=models.CASCADE)
    reservationNumber = models.CharField(max_length=255, unique=True)
    reservationDate = models.DateField()
    startTime = models.TimeField()
    endTime = models.TimeField()
    participantCount = models.PositiveIntegerField()
    status = models.CharField(max_length=20, choices=[('pending', 'Pending'), ('confirmed', 'Confirmed'), ('cancelled', 'Cancelled')])
    memo = models.TextField(blank=True, null=True)
    createdAt = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return self.reservationNumber

class Contract(models.Model):
    shop = models.ForeignKey(Shop, on_delete=models.CASCADE)
    artist = models.ForeignKey(Artist, on_delete=models.CASCADE)
    contractContent = models.TextField()
    startDate = models.DateField()
    endDate = models.DateField()
    status = models.CharField(max_length=20, choices=[('pending', 'Pending'), ('active', 'Active'), ('expired', 'Expired'), ('terminated', 'Terminated')])
    signedAt = models.DateTimeField(blank=True, null=True)
    createdAt = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"Contract between {self.shop.name} and {self.artist.user.name}"

class Settlement(models.Model):
    shop = models.ForeignKey(Shop, on_delete=models.CASCADE)
    artist = models.ForeignKey(Artist, on_delete=models.CASCADE)
    settlementDate = models.DateField()
    totalAmount = models.DecimalField(max_digits=10, decimal_places=2)
    commissionRate = models.DecimalField(max_digits=5, decimal_places=2)
    commissionAmount = models.DecimalField(max_digits=10, decimal_places=2)
    settlementAmount = models.DecimalField(max_digits=10, decimal_places=2)
    status = models.CharField(max_length=20, choices=[('pending', 'Pending'), ('completed', 'Completed'), ('delayed', 'Delayed')])
    paymentMethod = models.CharField(max_length=20, choices=[('pg', 'PG'), ('manual', 'Manual')])
    processedAt = models.DateTimeField(blank=True, null=True)
    identificationNumber = models.CharField(max_length=255, blank=True, null=True)

    def __str__(self):
        return f"Settlement for {self.settlementDate} - {self.shop.name}"

class InventoryItem(models.Model):
    shop = models.ForeignKey(Shop, on_delete=models.CASCADE)
    product = models.ForeignKey(Product, on_delete=models.CASCADE)
    stockQuantity = models.PositiveIntegerField()
    commissionRate = models.DecimalField(max_digits=5, decimal_places=2)
    lastRestockDate = models.DateField(blank=True, null=True)
    memo = models.TextField(blank=True, null=True)
    status = models.CharField(max_length=20, choices=[('active', 'Active'), ('discontinued', 'Discontinued')])
    createdAt = models.DateTimeField(auto_now_add=True)
    updatedAt = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.product.name} in {self.shop.name}"

class Shipment(models.Model):
    artist = models.ForeignKey(Artist, on_delete=models.CASCADE)
    shop = models.ForeignKey(Shop, on_delete=models.CASCADE)
    status = models.CharField(max_length=20, choices=[('pending', 'Pending'), ('confirmed', 'Confirmed'), ('completed', 'Completed')])
    createdAt = models.DateTimeField(auto_now_add=True)
    shippedAt = models.DateTimeField(blank=True, null=True)
    confirmedAt = models.DateTimeField(blank=True, null=True)

    def __str__(self):
        return f"Shipment from {self.artist.user.name} to {self.shop.name}"

class ShipmentItem(models.Model):
    shipment = models.ForeignKey(Shipment, on_delete=models.CASCADE)
    product = models.ForeignKey(Product, on_delete=models.CASCADE)
    quantity = models.PositiveIntegerField()
    createdAt = models.DateTimeField(auto_now_add=True)
    memo = models.TextField(blank=True, null=True)

    def __str__(self):
        return f"{self.quantity} of {self.product.name} for Shipment {self.shipment.id}"

class BusinessHour(models.Model):
    shop = models.ForeignKey(Shop, on_delete=models.CASCADE)
    dayOfWeek = models.CharField(max_length=20, choices=[
        ('monday', 'Monday'), ('tuesday', 'Tuesday'), ('wednesday', 'Wednesday'),
        ('thursday', 'Thursday'), ('friday', 'Friday'), ('saturday', 'Saturday'),
        ('sunday', 'Sunday')
    ])
    isOpen = models.BooleanField(default=True)
    openTime = models.TimeField(blank=True, null=True)
    closeTime = models.TimeField(blank=True, null=True)

    def __str__(self):
        return f"{self.shop.name} - {self.dayOfWeek}"

class ContractTemplate(models.Model):
    shop = models.ForeignKey(Shop, on_delete=models.CASCADE)
    title = models.CharField(max_length=255)
    content = models.TextField()
    contractorName = models.CharField(max_length=255)
    contractDate = models.DateField()
    createdAt = models.DateTimeField(auto_now_add=True)
    updatedAt = models.DateTimeField(auto_now=True)

    def __str__(self):
        return self.title

class Story(models.Model):
    shop = models.ForeignKey(Shop, on_delete=models.CASCADE)
    title = models.CharField(max_length=255)
    content = models.TextField()
    images = models.JSONField()
    isActive = models.BooleanField(default=True)
    createdAt = models.DateTimeField(auto_now_add=True)
    updatedAt = models.DateTimeField(auto_now=True)

    def __str__(self):
        return self.title

class Bookmark(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    shop = models.ForeignKey(Shop, on_delete=models.CASCADE)
    createdAt = models.DateTimeField(auto_now_add=True)

    class Meta:
        unique_together = ('user', 'shop')

    def __str__(self):
        return f"{self.user.userId} bookmarked {self.shop.name}"