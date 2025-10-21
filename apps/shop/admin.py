from django.contrib import admin
from .models import (
    Shop, Artist, Product, Class, Reservation, Contract, Settlement,
    InventoryItem, Shipment, ShipmentItem, BusinessHour, ContractTemplate,
    Story, Bookmark
)

# Register your models here.
admin.site.register(Shop)
admin.site.register(Artist)
admin.site.register(Product)
admin.site.register(Class)
admin.site.register(Reservation)
admin.site.register(Contract)
admin.site.register(Settlement)
admin.site.register(InventoryItem)
admin.site.register(Shipment)
admin.site.register(ShipmentItem)
admin.site.register(BusinessHour)
admin.site.register(ContractTemplate)
admin.site.register(Story)
admin.site.register(Bookmark)