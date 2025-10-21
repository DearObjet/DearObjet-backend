from django.contrib import admin
from .models import User, BusinessInfo, TermsAgreement

# Register your models here.
admin.site.register(User)
admin.site.register(BusinessInfo)
admin.site.register(TermsAgreement)